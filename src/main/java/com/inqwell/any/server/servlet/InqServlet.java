/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.server.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyString;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.Func;
import com.inqwell.any.IntI;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.SerializedProcess;
import com.inqwell.any.StringI;
import com.inqwell.any.connect.ServerConnection;

/**
 * Provide web access to Inq hosted services. Results are returned as
 * <code>JSON</code>.
 * @author tom
 * 
 */
public class InqServlet extends HttpServlet
{
	private static final String LOGIN_OK              = "{ \"response\": { \"errorCode\": 0, \"message\": \"Login OK\" } }";
	
	private static final int    ERR_NOT_LOGGED_IN     = 1001;
	private static final String ERR_NOT_LOGGED_IN_M   = "Not Logged In";

	private static final int    ERR_BAD_PACKAGE       = 1002;
	private static final String ERR_BAD_PACKAGE_M     = "Bad Package";
	
	private static final int    ERR_SVC_NOT_SPECIFIED   = 1003;
	private static final String ERR_SVC_NOT_SPECIFIED_M = "Service Not Specified";
	
	private static final int    ERR_NO_LOGIN_CRED     = 1004;
	private static final String ERR_NO_LOGIN_CRED_M   = "No Login Credentials";
	
	private static final int    ERR_INVALID_MIME      = 1005;
	private static final String ERR_INVALID_MIME_M    = "Invalid Mime Type";
	
	
	private static final int    ERR_UNRECOVERABLE     = 1006;
	private static final String ERR_UNRECOVERABLE_M   = "Unrecoverable";
	
	private static final String ERR_UNKNOWN           = "{ \"response\": { \"errorCode\": 1007, \"message\": \"Unknown Error\" } }";
	
	private static final int    ERR_GENERAL_EXCEPTION = 2001;
	
	private static final StringI response__   = new ConstString("response");
	private static final StringI errorCode__  = new ConstString("errorCode");
	private static final StringI message__    = new ConstString("message");
	private static final StringI stackTrace__ = new ConstString("stackTrace");
	
	private static final com.inqwell.any.Map    ERR_DYNAMIC = AbstractComposite.simpleMap();
	
	private static final String ALL_PACKAGES = "*";
	
	// Servlet session attributes
	private static final String ATTR_CONNECTION = "InqConnection";
	private static final String ATTR_LOGGEDIN   = "LoggedIn";
	
	// Servlet initialisation parameters
	private static final String INIT_USES_DEDICATED = "usesDedicated";
	private static final String INIT_PACKAGE        = "defaultPackage";
	private static final String INIT_PACKAGES       = "packages";
	private static final String INIT_INQ_URL        = "inqUrl";
	private static final String INIT_CONN_POOL_ID   = "connectionPoolId";
  private static final String INIT_CONN_POOL_LMT  = "connectionPoolLimit";
  private static final String INIT_LOGIN_REQD     = "loginRequired";
  private static final String INIT_LOGIN_USER     = "inqLoginUser";
  private static final String INIT_LOGIN_PWD      = "inqLoginPwd";
  private static final String INIT_LOGIN_PKG      = "inqLoginPkg";
  private static final String INIT_PING_TIME      = "pingTimeout";
  
  // The default number of Inq server connections this servlet
  // will make, if it is not using a dedicated connection per session.
  private static final int DEFAULT_LIMIT = 20;
  
  // Any equivalents for giving to the resource allocator
  public static final StringI INIT_LOGIN_USER_A  = new ConstString(INIT_LOGIN_USER);
  public static final StringI INIT_LOGIN_PWD_A   = new ConstString(INIT_LOGIN_PWD);
  public static final StringI INIT_LOGIN_PKG_A   = new ConstString(INIT_LOGIN_PKG);
  public static final StringI INIT_INQ_URL_A     = new ConstString(INIT_INQ_URL);
  public static final StringI INIT_PING_TIME_A   = new ConstString(INIT_PING_TIME);

  private static Logger logger__;
  
  // We use the Inq reqource manager, which allocates resources (in this
  // case ServerConnection objects) to processes. Since there are
  // no processes in the servlet environment, we just allocate
  // them to a dummy process.
  private static final Process process__ = new SerializedProcess(null);
  
  // When no args are supplied...
  private static final Map<String, String> noArgs__ = new HashMap<String, String>(0);
  
  // An exception container. We don't use this.
  private static final RuntimeContainedException e__ = new RuntimeContainedException(null);
  
	// Well-known arguments
	// The service to invoke
	private static final String ARG_SERVICE = "Service";
	// The package - ignored if this servlet has been configured
	// to have a package. TODO: or throw? or return error?
	private static final String ARG_PACKAGE = "Package";
	
	// Login service/credentials
	private static final String SVC_LOGIN = "Login";
	private static final String ARG_USER  = "User";
	private static final String ARG_PWD   = "Pwd";
	
	// Output format arg
	private static final String ARG_MIME  = "mime";
	
	/**
	 * Whether this servlet uses a dedicated Inq connection per
   * session.
	 * If <code>false</code> a suitable connection is acquired
   * from the pool per request. If <code>true</code> the
   * connection is stored in the session.
   * <p/>
   * When the connection is dedicated requests from
	 * the client are handled single-threaded  
	 */
	private boolean usesDedicated_;
	
	/**
	 * If this servlet does not use a dedicated connection, the
	 * id of the pool from which connections are acquired
	 */
	private StringI poolId_;
	
	/**
	 * The default Inq package this servlet has been configured with,
	 * from the <code>defaultPackage</code> servlet initialisation parameter.
	 * If set to "*" then services in any package can be invoked,
	 * as specified by the <code>package</code> request argument.
	 * A specific package allows the <code>package</code> request argument
	 * to be omitted. This package is then used when invoking
	 * the Inq service. Must be set by an initialisation
	 * parameter - <code>null</code> represents an illegal
	 * state.
	 */
	private String defaultPkg_;
	
	/**
	 * An optional list of packages that this servlet is
	 * permitted to invoke the services of. Configured
	 * by the <code>packages</code> servlet initialisation parameter.
	 * If not specified then the <code>defaultPackage</code>
	 * is used.
	 */
	private Set<String> pkgs_;
	
	/**
	 * The URL of the inq server to connect to
	 */
	private String inqUrl_;
	
	/**
	 * When no web login is required, this servlet must be given
	 * the Inq server login arguments as initialisation parameters.
	 * The package for logging in is specified separately to any
	 * "package" parameter used to limit general service invocations
	 * to a specific package. 
	 */
	private String inqLoginUser_;
	private String inqLoginPwd_;
	private String inqLoginPkg_;
	
	/**
	 * Whether this servlet requires a web login.
	 */
	private boolean loginRequired_ = true;
	
	/**
	 * The timeout to wait for a response when pinging an Inq server connection.
	 * Default is 20s.
	 */
	private IntI pingTimeout_ = new ConstInt(20000); 
	
	static
	{
		// Set up a Map which we clone and mutate to create dynamic
		// responses.
		com.inqwell.any.Map response = AbstractComposite.simpleMap();
		ERR_DYNAMIC.add(response__, response);
		response.add(errorCode__,   new AnyInt());
		response.add(message__,     new AnyString());
		response.add(stackTrace__,  new AnyString());
		
		logger__ = Logger.getLogger(InqServlet.class.getName());
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		doRequest(req, resp);
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		BufferedReader r = req.getReader();
		char[] c = new char[2048];
		int count = r.read(c);
		String s = new String(c, 0, count);
		
		doRequest(req, resp);
	}
	
	private void doRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		ServletOutputStream strm = null;
		String mime = "json";

		try
		{
			Map<String, String> args = getArgs(req);
			
			mime = validateMime(resp, args);
						
			strm = resp.getOutputStream();
			
			// TODO: Buffer the response ?
			
			// TODO: Define an error page ? (prob not - all done with json)
			
			//logger__.log(Level.INFO, "Validating {0})", args);
			
			if (validateRequest(req, resp, strm, mime, args))
			{
				invokeService(req, resp, strm, mime, args);
				strm.flush();
			}
		}
		catch(Throwable t)
		{
			if (strm == null)
				strm = resp.getOutputStream();

			strm.print(errorResponse(ERR_GENERAL_EXCEPTION, null, mime, t));
			strm.flush();
		}
	}

	public void init() throws ServletException
  {
		logger__.info("Initialising...");
		
		// "package" must be specified, even if it is "*"
		defaultPkg_ = getServletConfig().getInitParameter(INIT_PACKAGE);
		if (defaultPkg_ == null)
			throw new UnavailableException("Default package was not specified");
		
		pkgs_ = parsePackages(getServletConfig().getInitParameter(INIT_PACKAGES));
		
		// The Inq server URL must be present. It can be specified
		// at the servlet level (as an override) or at the web-app
		// level for all instances of this servlet.
		inqUrl_ = getContextOrServletParameter(INIT_INQ_URL);
		if (inqUrl_ == null)
			throw new UnavailableException("Inq server URL was not specified");
		
		// Fetch possible Inq server login arguments.
		inqLoginUser_ = getServletConfig().getInitParameter(INIT_LOGIN_USER);
		inqLoginPwd_  = getServletConfig().getInitParameter(INIT_LOGIN_PWD);
		inqLoginPkg_  = getServletConfig().getInitParameter(INIT_LOGIN_PKG);
		if (inqLoginPkg_ == null && !defaultPkg_.equals(ALL_PACKAGES))
			inqLoginPkg_ = defaultPkg_;
		
		usesDedicated_ = Boolean.valueOf(getServletConfig().getInitParameter(INIT_USES_DEDICATED));
		
		loginRequired_ = Boolean.valueOf(getServletConfig().getInitParameter(INIT_LOGIN_REQD));
		
		if (getServletConfig().getInitParameter(INIT_PING_TIME) != null)
			pingTimeout_ = new ConstInt(Integer.valueOf(getServletConfig().getInitParameter(INIT_PING_TIME)).intValue());
		
		// If the connection to the Inq server is not dedicated to the session
		// then there must be login details configured for the servlet
		if (!usesDedicated_)
		{
			if (inqLoginUser_ == null || inqLoginPwd_ == null || inqLoginPkg_ == null)
				throw new UnavailableException("Inq server user, password or package was not specified");
			
			// There must be a connection pool id as well
			poolId_ = new ConstString(getInitParameter(INIT_CONN_POOL_ID));
			if (poolId_.isNull())
				poolId_ = new ConstString(getServletConfig().getServletName());

			int limit = DEFAULT_LIMIT;
			if (getInitParameter(INIT_CONN_POOL_LMT) != null)
			{
				limit = Integer.valueOf(getInitParameter(INIT_CONN_POOL_LMT));
			}
			
			com.inqwell.any.Map spec = AbstractComposite.simpleMap();
			spec.add(INIT_INQ_URL_A, new ConstString(inqUrl_));
			spec.add(INIT_LOGIN_USER_A, new ConstString(inqLoginUser_));
			spec.add(INIT_LOGIN_PWD_A, new ConstString(inqLoginPwd_));
			spec.add(INIT_LOGIN_PKG_A, new ConstString(inqLoginPkg_));
			spec.add(INIT_PING_TIME_A, pingTimeout_);
			
			// Tell the resource manager about this pool id and the
			// specification for ServerConnection instances to be
			// created.
			ServerConnectionManager.instance().addSpec(poolId_, spec, new ConstInt(limit));
		}
  }
	
	private void invokeService(HttpServletRequest  req,
                             HttpServletResponse resp,
  													 ServletOutputStream strm,
  													 String              mime,
                             Map<String, String> args) throws AnyException, IOException
	{
		ServerConnection sc = null;
		Func             t  = null;
		try
		{
			sc = getServerConnection(req, args);
    	String service = getServiceName(req);
    	String pkg = defaultPkg_.equals(ALL_PACKAGES) ? args.get(ARG_PACKAGE)
                                                    : defaultPkg_;
    	
    	Object logargs[] = { pkg, service, args };
    	
			logger__.log(Level.INFO, "Invoking {0}.{1} ({2})", logargs);
			
			// Mime type defaults to json but if it is xml we need to
			// change the transformer
			if (args.containsKey(ARG_MIME) && args.get(ARG_MIME).equals("xml"))
				t = sc.setTransformer(new Transformer.XMLTransfomer(false, true));
			
    	// TODO: Config timeout
    	Any result = sc.request(service, pkg, args, 20000);
    	
    	// A result of null means we timed out or were disconnected somehow
    	strm.print((result != null) ? result.toString()
    		                          : errorResponse(ERR_UNRECOVERABLE, ERR_UNRECOVERABLE_M, mime, null));
		}
		finally
		{
			if (sc != null && t != null)
				sc.setTransformer(t);
			
			if (sc != null && !usesDedicated_)
				ServerConnectionManager.instance().release(poolId_, process__, sc, null, e__);

		}
	}
	
	/**
	 * Obtain a connection 
	 * @return
	 */
	private ServerConnection getServerConnection(HttpServletRequest  req,
                                               Map<String, String> args) throws AnyException
	{
		ServerConnection ret = null;
		
		if (usesDedicated_)
		{
			logger__.info("Getting dedicated connection");
			ret = getConnectionFromSession(req, args);
		}
		else
		{
			logger__.info("Getting pooled connection");
			ret = getConnectionFromPool(req, args);
		}
		return ret;
	}
	
	/**
	 * Get the dedicated connection from the session.
	 * If there is no connection in the session then
	 * one is made, which implies a login to the Inq
	 * server.
	 * 
	 * @param req the servlet request
	 * @param args the arguments is the original request
	 * @return the connection
	 */
	private ServerConnection getConnectionFromSession(HttpServletRequest  req,
                                                    Map<String, String> args) throws AnyException
	{
		ServerConnection ret = null;
		
		HttpSession session = req.getSession();
		ret = (ServerConnection)session.getAttribute(ATTR_CONNECTION);
		
		try
		{
  		if (ret == null)
  		{
  			String pkg = defaultPkg_.equals(ALL_PACKAGES) ? args.get(ARG_PACKAGE)
  					                                          : defaultPkg_;
  			ret = new ServerConnection(inqUrl_,
  					                       args.get(ARG_USER),
  					                       args.get(ARG_PWD),
  					                       pkg,
  					                       true);
  			
  		  session.setAttribute(ATTR_CONNECTION, ret);
  		}
  		else
  		{
  			// Ping it to see if its still good
  			if (!ret.ping(15000))
  			{
  				// If its not good then remove from the session and try to make a new
  				// one.
    		  session.removeAttribute(ATTR_CONNECTION);
    		  
    			String pkg = defaultPkg_.equals(ALL_PACKAGES) ? args.get(ARG_PACKAGE)
                                                        : defaultPkg_;
    			ret = new ServerConnection(inqUrl_,
                                     args.get(ARG_USER),
                                     args.get(ARG_PWD),
                                     pkg,
                                     true);
    			
    		  session.setAttribute(ATTR_CONNECTION, ret);
  			}
  		}
		}
		catch(AnyException e)
		{
			// TODO: log
		  session.removeAttribute(ATTR_CONNECTION);
			ret = null;
			
			throw e;
		}

		return ret;
	}
	
	/**
	 * Get a connection from the pool specified by this
	 * servlet's pool id.
	 * 
	 * @param req the servlet request
	 * @param args the arguments is the original request
	 * @return the connection
	 */
	private ServerConnection getConnectionFromPool(HttpServletRequest  req,
			                                           Map<String, String> args) throws AnyException
	{
		ServerConnection ret = null;
		
		ret = (ServerConnection)ServerConnectionManager.instance().acquire(poolId_, process__, 20000);
		
		return ret;
	}
	
	/**
	 * Validate the request. If this method returns <code>true</code> then
	 * the request can be executed. If not it returns <code>false</code>
	 * having issued some sort of error response.
	 *   
	 * @param req
	 * @param resp
	 * @return <code>true</code> if request can proceed, <code>false</code>
	 * if it cannot.
	 */
	private boolean validateRequest(HttpServletRequest  req,
																	HttpServletResponse resp,
																	ServletOutputStream strm,
																	String              mime,
																	Map<String, String> args) throws IOException
	{
		boolean ret = false;
		
		if (args.containsKey(ARG_MIME))
		{
			String m = args.get(ARG_MIME);
			
			if (!m.equals("json") && !m.equals("xml"))
			{
				resp.getOutputStream().print(errorResponse(ERR_INVALID_MIME, ERR_INVALID_MIME_M, mime, null));
				return false;
			}
		}

		String service = getServiceName(req);
		
		if (service == null)
		{
			resp.getOutputStream().print(errorResponse(ERR_SVC_NOT_SPECIFIED, ERR_SVC_NOT_SPECIFIED_M, mime, null));
		}
		else
		{
			if (!loggedIn(req, service))
			{
				resp.getOutputStream().print(errorResponse(ERR_NOT_LOGGED_IN, ERR_NOT_LOGGED_IN_M, mime, null));
			}
			else
			{
  			String pkg = getPackage(req);
  			if ((pkg == null && !defaultPkg_.equals(ALL_PACKAGES)) ||
  					(pkg != null && defaultPkg_.equals(ALL_PACKAGES)) || 
  					(pkg != null && pkg.equals(defaultPkg_)) ||
  					(pkgs_ != null && pkgs_.contains(pkg)))
  			{
  				// If the service is "Login" then we expect
  				// user/pwd arguments
  				if (service.equals(SVC_LOGIN))
  				{
  					if (!args.containsKey(ARG_USER) ||
  							!args.containsKey(ARG_PWD))
  					{
  						resp.getOutputStream().print(errorResponse(ERR_NO_LOGIN_CRED, ERR_NO_LOGIN_CRED_M, mime, null));
  					}
  					else
  					{
  						ret = true;
  					}
  				}
  				else
  				{
  					ret = true;
  				}
  			}
  			else
  			{
  				resp.getOutputStream().print(errorResponse(ERR_BAD_PACKAGE, ERR_BAD_PACKAGE_M, mime, null));
  			}
			}
		}
		
		return ret;
	}
	
	private boolean loggedIn(HttpServletRequest req, String svc)
	{
		if (!loginRequired_)
			return true;
		
		// Its OK to not be logged in when that is what we are trying to do
		return svc.equals(SVC_LOGIN) || Boolean.valueOf(req.getSession().getAttribute(ATTR_LOGGEDIN).toString());
	}
	
	private String getServiceName(HttpServletRequest req)
	{
		return req.getParameter(ARG_SERVICE);
	}
	
	private String getPackage(HttpServletRequest req)
	{
		return req.getParameter(ARG_PACKAGE);
	}
	
	/**
	 * Convert the request parameters into a Map. The special
	 * arguments <code>Package</code> and <code>Service</code>
	 * are skipped.
	 * 
	 * @param req
	 * @return the Map or <code>null</code> if there are no
	 * arguments after excluding the special ones.
	 */
	private Map<String, String> getArgs(HttpServletRequest req)
	{
		Map<String, String> ret = noArgs__;
		
		Enumeration<java.lang.String> args = req.getParameterNames();
		
		while (args.hasMoreElements())
		{
			String argName = args.nextElement();
			
			if (argName.equals(ARG_PACKAGE) || argName.equals(ARG_SERVICE))
				continue;
			
			if (ret == noArgs__)
				ret = new HashMap<String, String>();
			
			String argValue = req.getParameter(argName);
			ret.put(argName,  argValue);
		}
		
		return ret;
	}
	
	private String errorResponse(int code, String message, String mime, Throwable t)
	{
		String ret = null;
		try
		{
  		com.inqwell.any.Map root = (com.inqwell.any.Map)ERR_DYNAMIC.cloneAny();
  		com.inqwell.any.Map response = (com.inqwell.any.Map)root.get(response__);
  		((IntI)response.get(errorCode__)).setValue(code);
  		((StringI)response.get(message__)).setValue(message);
  		
  		if (t != null)
  		{
  			if (t.getMessage() != null)
  			  ((StringI)response.get(message__)).setValue(t.getMessage());
  			
  			StringWriter sw = new StringWriter();
  			PrintWriter pw = new PrintWriter(sw);
  			t.printStackTrace(pw);
  			pw.close();
  			((StringI)response.get(stackTrace__)).setValue(sw.toString());
  		}
  		
  		Func tr = getTransformerForMime(mime);
  		
  		ret = tr.exec(root).toString();
		}
		catch(AnyException e)
		{
			// TODO: log
			ret = ERR_UNKNOWN;
		}
		return ret;
	}
	
	private String validateMime(HttpServletResponse resp, Map<String, String> args)
	{
		// Default mime type
		String mime = "json";
		
		if (args.containsKey(ARG_MIME))
		{
			mime = args.get(ARG_MIME);
			
			if (!mime.equals("json") && !mime.equals("xml"))
			{
				mime = "json";
			}
		}

    resp.setContentType("application/" + mime);
    
    return mime;
	}
	
	private Func getTransformerForMime(String mime)
	{
		if (mime.equals("xml"))
			return new Transformer.XMLTransfomer(false, true);
		else
			return new Transformer.JsonTransformer();
	}
	
	private Set<String> parsePackages(String pkg)
	{
		if (pkg == null)
			return null;
		
		String pkgs[] = pkg.split("\\s*,\\s*");
		HashSet<String> pkgss = new HashSet<String>(pkgs.length);
		for (String s : pkgs)
			pkgss.add(s);
			
		return pkgss;
	}
	
	/**
	 * Look for the initialisation parameter firstly in the
	 * servlet config (this servlet) and if not found in the
	 * context (web-app).
	 *  
	 * @param name the parameter name
	 * @return the parameter value or <code>null</code>
	 * if not found at either level. 
	 */
	private String getContextOrServletParameter(String name)
	{
		String ret = getServletConfig().getInitParameter(name);
		
		if (ret == null)
			ret = getServletContext().getInitParameter(name);
		
		return ret;
	}
}
