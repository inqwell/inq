/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/Server.java $
 * $Author: sanderst $
 * $Revision: 1.10 $
 * $Date: 2011-04-09 18:16:45 $
 */

package com.inqwell.any.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFuncHolder;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyString;
import com.inqwell.any.AnyTimeZone;
import com.inqwell.any.AnyURL;
import com.inqwell.any.Array;
import com.inqwell.any.BuildNodeMap;
import com.inqwell.any.Call;
import com.inqwell.any.Catalog;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstString;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.LocateNode;
import com.inqwell.any.LockManager;
import com.inqwell.any.Map;
import com.inqwell.any.MultiHierarchyMap;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.NullService;
import com.inqwell.any.Process;
import com.inqwell.any.PropertyAccessMap;
import com.inqwell.any.RunInq;
import com.inqwell.any.SendRequest;
import com.inqwell.any.ServerConstants;
import com.inqwell.any.StringI;
import com.inqwell.any.UserProcess;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.client.AnyComponent;
import com.inqwell.any.net.InqStreamHandlerFactory;
import com.inqwell.any.server.json.MetaToJson;
import com.inqwell.any.util.CommandArgs;

/**
 * Server start up and singleton handling
 * @author $Author: sanderst $
 * @version $Revision: 1.10 $
 */
public final class Server extends PropertyAccessMap
{
  public static Any startup__ = new ConstString("startup");
  private static Logger logger = Logger.getLogger("inq");

  public static StringI serverPath__ = new ConstString("$catalog.inq.system.Server");

	private static Server theServer__ = null;

	private LockManager        lockManager_;
	private Properties         serverProperties_ = new Properties();

	private NodeSpecification  domain_;
	private boolean            domainSet_ = false;

  private Map                propertyMap_;
  private Call               createMetaF_;
  
  // This one has to be a FuncHolder because that's what exceptions
  // carry and these two things come together in ExceptionToFunc
  private AnyFuncHolder.FuncHolder defExcHandlerF_;

	public static Server instance()
	{
		if (theServer__ == null)
		{
			synchronized (Server.class)
			{
				if (theServer__ == null)
					theServer__ = new Server();
			}
		}
		return theServer__;
	}

	private Server ()
	{
		lockManager_  = new LockManager();

    Globals.lockManager__ = lockManager_;
	}

	public boolean lock(Process p, Any a) throws AnyException
	{
		return lockManager_.lock(p, a);
	}

	public boolean lock(Process p, Any a, long timeout) throws AnyException
	{
		return lockManager_.lock(p, a, timeout);
	}

	public Process locker(Any a) throws AnyException
	{
		return lockManager_.locker(a);
	}

	public int unlock(Process p, Any a) throws AnyException
	{
		return lockManager_.unlock(p, a);
	}

	public void unlock(Process p, Any a, boolean force) throws AnyException
	{
		lockManager_.unlock(p, a, force);
	}

	public void unlockList(Process p, Composite c, boolean force) throws AnyException
	{
		lockManager_.unlockList(p, c, force);
	}

	public Array scanForDeadlock()
	{
		return lockManager_.scanForDeadlock();
	}

	// Load the properties at the given URL.  These define externally
	// configurable server parameters.
	public void loadProperties(String propURL)
	{
		InputStream is = makeStream(propURL);

		if (is != null)
		{
			try
			{
				serverProperties_.load(is);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				serverProperties_.clear();
			}
		}
	}

	/**
	 * Return the domain name of this <code>inq</code> server.  The
	 * domain name is read from the server's properties file which, in
	 * turn, is specified by the <code>-properties {file}</code>
	 * command line option.  The domain is the value of the
	 * <code>inq.server.domain</code> property.
	 * @return the domain as a node specification or null if there is
	 * no domain specified
	 */
	public NodeSpecification getDomain()
	{
		return domain_;
	}

	public void setDomain()
	{
		setDomain(null);
	}

	/**
	 * Sets the domain of this server.  If the supplied argument
	 * is <code>null</code> then we look for the property
	 * <code>inq.server.domain</code>.
	 * <p>
	 * If an argument is given then the domain is set to this value
	 * but this can only be performed once.
	 */
	public void setDomain(Any d)
	{
		if (d == null)
		{
			if (!domainSet_)
			{
				String s = serverProperties_.getProperty("inq.server.domain");
				if (s != null)
				{
					domain_ = new NodeSpecification(s);
				}
			}
		}
		else
		{
			if (!domainSet_)
			{
				if (d instanceof NodeSpecification)
				{
					domain_ = (NodeSpecification)d;
				}
				else
				{
					domain_ = new NodeSpecification(d.toString());
				}
				domainSet_ = true;
			}
		}
	}

	private InputStream makeStream(String propURL)
	{
		boolean isURL = false;

		InputStream is = null;

		try
		{
			URL url = new URL(propURL);

			isURL = true;

			// No exception so open
			is = url.openStream();
		}
		catch (MalformedURLException urle)
		{
			isURL = false;
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		if (!isURL)
		{
			try
			{
				// Not a URL so assume to be a plain file
				is = new FileInputStream(propURL);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return is;
	}

	public static void main (String args[])
	{
		// Set up globals
		Globals.channelOutputReplacements__ = Replacements.serverToClient__;
		Globals.channelInputReplacements__  = Replacements.serverFromClient__;
		Globals.streamOutputReplacements__  = Replacements.serverToNativeStream__;
		Globals.streamInputReplacements__   = Replacements.serverFromNativeStream__;

		// XML Replacements
		Globals.xmlStreamOutputReplacements__ = Replacements.serverToXMLStream__;
		Globals.xmlStreamInputReplacements__  = Replacements.serverFromXMLStream__;
		
		Globals.sessionList__               = SessionManager.instance();

    System.out.println("Inq Server");
    System.out.println("Copyright (c) InqWell Ltd 2002-2016");
    System.out.println("JavaCC Copyright (c) 2006, Sun Microsystems, Inc.");

		try
		{

			Runtime.getRuntime().addShutdownHook(new Thread()
                                       		 {
                                       			 public void run()
                                       			 {
                                       			   setName("Shutdown");
                                       			   logger.info("Server stopped");
                                       			 }
                                       		 });

			// Establish speakinq:// protocol and other url types
		  InqStreamHandlerFactory.install();

			CommandArgs cArgs = new CommandArgs(args);

      Map argsMap = cArgs.toMap();
      Catalog.instance().getCatalog().add(CommandArgs.commandLine__, argsMap);

      AnyString propURL = new AnyString();
			if (cArgs.arg("-properties", propURL))
			{
				Server.instance().loadProperties(propURL.toString());
			}

			Map catalog = Catalog.instance().getCatalog();

      AnyString config = new AnyString();
      Any catalogConfig;
      if (!cArgs.arg("-configurator", config))
        catalogConfig = new ConstString("com.inqwellx.Configurator");
      else
        catalogConfig = new ConstString(config);
      catalog.add(AbstractValue.flyweightString("configurator"), catalogConfig);

      
			Server.instance().setDomain();

			AnyInt speakinqPort = new AnyInt();
			cArgs.arg("-speakinqport", speakinqPort, new AnyInt(6556));

			AnyInt speakinqsPort = new AnyInt();
			cArgs.arg("-speakinqsport", speakinqsPort, new AnyInt(6557));

//			AnyInt httpinqPort = new AnyInt();
//			cArgs.arg("-httpinqport", httpinqPort, new AnyInt(-1));

			Map processList = new MultiHierarchyMap();
			catalog.add(Process.PROCESSES, processList);
      catalog.add(AnyTimeZone.timezone__, new AnyTimeZone());

			new DeadlockScanner();

			new CompileInq();
			new LoadDescriptors();
			new MetaToJson();
			new NullService();
			new RunInq();
			new LoginOK();
			new LoginDenied();

      // Catalog ourselves before the startup script is run - it sets some
      // of our properties.
      BuildNodeMap bn = new BuildNodeMap();
      bn.build(serverPath__, Server.instance(), Catalog.instance().getCatalog());

      // Start a process to run the server's initial scripts. These are
      // the mandatory startup in Server.inq and anyting provided on the
      // command line via -init <URL>
      SpawnProcess startup =
        new SpawnProcess(startup__, // process name
                         Process.DETACHED,
                         null,      // make an input channel
                         null,      // there's no output channel
                         null,      // no call on start
                         null,      // no call on end
                         null);     // no syncext

      Process p = (Process)startup.exec(null);
      p.setRealPrivilegeLevel((short)0);
      p.setEffectivePrivilegeLevel((short)0);
      p.add(UserProcess.loginName__,  new ConstString("admin"));
      p.add(UserProcess.package__,  RunInq.system__);
      p.startThread();

      AnyURL u  = new AnyURL("classpath:///com/inqwell/any/server/Server.inq");
      URL    serverInq = u.getURL();

      AnyString serverStartupURL = new AnyString();
      serverStartupURL.setValue(serverInq.toString());

      // Send it a request to run it
      AnyChannel c = (AnyChannel)p.get(ServerConstants.ICHANNEL);
      Map sargs = AbstractComposite.simpleMap();
      sargs.add(RunInq.source__, serverStartupURL);
      SendRequest sr = new SendRequest(RunInq.servicePath__,
                                       null,
                                       sargs,
                                       c);
      sr.setTransaction(p.getTransaction());
      sr.exec(null);
      
      // Now for any -init on the command line.
      AnyString initURL = new AnyString();
			if (cArgs.arg("-init", initURL))
			{
        Map iargs = AbstractComposite.simpleMap();
        iargs.add(RunInq.source__, initURL);
        sr = new SendRequest(RunInq.servicePath__,
                             null,
                             iargs,
                             c);
        sr.setTransaction(p.getTransaction());
        sr.exec(null);
			}

      // If there is -test we are kicking off a unit test file. Send it
      AnyString test = new AnyString();
			if (cArgs.arg("-test", test))
			{
		    System.out.println("Running regression tests");
				// Load the test support
	      u  = new AnyURL("classpath:///inq/test/testing.inq");
	      serverInq = u.getURL();

	      serverStartupURL = new AnyString();
	      serverStartupURL.setValue(serverInq.toString());

	      // Send it a request to run it
	      sargs = AbstractComposite.simpleMap();
	      sargs.add(RunInq.source__, serverStartupURL);
	      sr = new SendRequest(RunInq.servicePath__,
	                                       null,
	                                       sargs,
	                                       c);
	      sr.setTransaction(p.getTransaction());
	      sr.exec(null);

	      // Run the test
        Map targs = AbstractComposite.simpleMap();
        targs.add(RunInq.source__, test);
        sr = new SendRequest(RunInq.servicePath__,
                             null,
                             targs,
                             c);
        sr.setTransaction(p.getTransaction());
        sr.exec(null);
        
        // Tests must call exit(<status>) so wait for the process
        // to terminate and pass the status back to the environment
        p.join();
        System.exit(((IntI)p.get(Process.STATUS)).getValue());
			}

      // Then close the channel to get rid of the process
      c.close();

      // Wait for server startup to complete before starting
      // listeners
      p.join();

			// Now start the socket listener for speakinq...
			new ServerListener(speakinqPort.getValue(),
			                   new SpeakinqProtocolHandler());

      // ... speakinqs if the ssl system property is there
			if (System.getProperty("javax.net.ssl.keyStore") != null)
				new ServerListener(speakinqsPort.getValue(),
				                   new SpeakinqsProtocolHandler());

      logger.info("Server started");
      
			// and one for httpinq TO BE RETIRED
//      if (httpinqPort.getValue() > 0)
//        new ServerListener(httpinqPort.getValue(),
//                           new ExceptionToStream(System.out),
//                           new HttpinqProtocolHandler());

		}
		catch (Exception e)
		{
			System.out.println("Server err: " + e.getMessage());
			e.printStackTrace();
		}
	}

  // Expire the typedefs that represent the state of the Inq
  // environment
  static void expireSystemTypes() throws AnyException
  {
    Map catalog = Catalog.instance().getCatalog();
    LocateNode l = new LocateNode("inq.system.types");
    Map m = (Map)l.exec(catalog);

    // There might not be any
    if (m != null)
    {
      // Iterate over the descriptors at this point and expire them
    }
  }

	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}

  public boolean isEmpty() { return false; }
  public Iter createIterator () { return DegenerateIter.i__; }

  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;

    return false;
  }

  public Any get (Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      handleNotExist(key); // throws
      return null;
    }
  }

  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      return null;
    }
  }

  public void setCreateMeta(Any createMeta)
  {
    createMetaF_ = AnyComponent.verifyCall(createMeta);
  }

  public Any getCreateMeta()
  {
    return createMetaF_;
  }

  public void setDefaultExceptionHandler(Any excHandler)
  {
    defExcHandlerF_ = (AnyFuncHolder.FuncHolder)excHandler;
  }

  public Any getDefaultExceptionHandler()
  {
    return defExcHandlerF_;
  }
}
