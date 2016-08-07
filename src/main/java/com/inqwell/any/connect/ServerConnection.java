/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.connect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.net.ssl.SSLHandshakeException;
import javax.xml.transform.TransformerException;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyOrderedMap;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.Catalog;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.ContainedException;
import com.inqwell.any.Crypt;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventDispatcher;
import com.inqwell.any.EventListener;
import com.inqwell.any.Func;
import com.inqwell.any.Globals;
import com.inqwell.any.InqInterpreter;
import com.inqwell.any.IntI;
import com.inqwell.any.InvokeService;
import com.inqwell.any.KeyDef;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.SendRequest;
import com.inqwell.any.SimpleEvent;
import com.inqwell.any.StartProcessEvent;
import com.inqwell.any.StringI;
import com.inqwell.any.SystemProperties;
import com.inqwell.any.Transaction;
import com.inqwell.any.UserProcess;
import com.inqwell.any.Value;
import com.inqwell.any.Vectored;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.OutputChannel;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.io.XMLXStream;
import com.inqwell.any.net.InqStreamHandlerFactory;
import com.inqwell.any.util.CommandArgs;
import com.inqwell.json.Handler;
import com.inqwell.json.JSON;
import com.inqwell.json.ParseException;

/**
 * A connection to a Inq server for synchronous request/response.
 * This class may be used in a web container to support connections
 * to an Inq server.
 * 
 * @author tom
 *
 */
public class ServerConnection extends AbstractAny
{
  private static Logger logger__  = LogManager.getLogManager().getLogger("inq");
  
  private static final Array webResponseEventTypes__;
  private static final Any   deniedReason__   = AbstractValue.flyweightString("reason");
	private static final Event ping__ = new SimpleEvent(EventConstants.PING_KEEPALIVE);
  private static final Array pongEventTypes__;

  private OutputChannel   och_;
  private InputChannel    ich_;
  private EventDispatcher ed_;
  private ServiceResponse responder_;
  private boolean         loggedIn_;
  private boolean         connected_;
  private Map             args_ = AbstractComposite.simpleMap();
  private StringI         k_ = new AnyString();
  private StringI         serviceName_ = new AnyString(); 
  private IntI            sequence_ = new AnyInt();
  private Func            transformer_;
  private int             pingTimeout_ = 20000;
  private long            lastPong_;
  
  private JSON<Map, Vectored> parser_;
  private JSONHandler         handler_;

  static
  {
    // Set up globals
    // TODO move these to the connection allocator 
    Globals.channelOutputReplacements__ = com.inqwell.any.client.Replacements.clientToServer__;
    Globals.channelInputReplacements__  = com.inqwell.any.client.Replacements.clientFromServer__;
    Globals.streamOutputReplacements__  = com.inqwell.any.client.Replacements.clientToNativeStream__;
    Globals.streamInputReplacements__   = com.inqwell.any.client.Replacements.clientFromNativeStream__;
    Globals.interpreter__               = new InqInterpreter();
    InqStreamHandlerFactory.install();
    
    webResponseEventTypes__ = AbstractComposite.array();
    webResponseEventTypes__.add(EventConstants.WEBSVC_RESP);
    
    pongEventTypes__ = AbstractComposite.array();
    pongEventTypes__.add(EventConstants.PONG_KEEPALIVE);

  }

  /**
   * Connect to the specified Inq server. The <code>url</code> identifies
   * the server and must specify the <code>speakinq</code>
   * or <code>speakinqs</code> protocol. The user, passwd and package are
   * required credentials to the login procedure.
   * @param url a string of the form <code>speakinq://my.host.com:1234</code>
   * @param user the login user
   * @param passwd the login password
   * @param inqPackage the package to log in as
   * @param trust whether to trust a self-signed or expired certificate
   * when using a secure connection
   */
  public ServerConnection(StringI url,
                          StringI user,
                          StringI passwd,
                          StringI inqPackage,
                          boolean trust) throws AnyException
  {
    Map           cert = null; // in case its an SSL connection
    Socket        cd = new Socket();
    
    try
    {
      // Attempt to connect to the server
      cd.openURL(new URL(url.toString()), null);
    }
    catch (ContainedException e)
    {
      // If we get an exception check if its because of an untrusted
      // secure connection
      if (e.getThrowable() instanceof SSLHandshakeException)
      {
        cert = (Map)cd.getInqURLConnection().getTrusted();
        logger__.log(Level.WARNING, "Untrusted certificate received from {0}", url);
        logger__.log(Level.WARNING, "Certificate: {0}", cert);

        // TODO: exception handling here
//        if (trust)
//          cd.openURL(new URL(url), cert);
      }
      else
      {
        // Just throw what we got
        throw e;
      }
    }
    catch(Exception e)
    {
      throw new RuntimeContainedException(e);
    }

    // Make the output channel writing to the socket connection
    och_ = new AnyChannel(cd);
    ich_ = (InputChannel)och_;
    
    och_.write(new StartProcessEvent(EventConstants.START_WEBPROCESS,
                                    null,
                                    user));


    Map login = AbstractComposite.simpleMap();
    login.add(NodeSpecification.user__, new ConstString(user.toString()));
    login.add(UserProcess.passwd__, Crypt.crypt(passwd));
    login.add(UserProcess.package__, new ConstString(inqPackage.toString()));
    login.add(SystemProperties.localhostname,
              SystemProperties.instance().getSystemProperties().get(SystemProperties.localhostname));
    och_.write(new SimpleEvent(EventConstants.LOGIN_REQUEST, login));
    och_.flushOutput();

    ed_ = new EventDispatcher();
    ed_.addEventListener(new LoginOK());
    ed_.addEventListener(new ServiceInvoked());
    ed_.addEventListener(new LoginDenied());
    ed_.addEventListener(new PingResponder());
    
    responder_ = new ServiceResponse();
    ed_.addEventListener(responder_);
    
    // At the moment, given the stripped-down nature of the design,
    // we don't know the server is gone until tcp tells us (when
    // we next try to use it)
    ed_.addEventListener(new ServerLost());

    connected_ = true;
    
    // Await login response
    while(!loggedIn_ && connected_)
    {
      Any resp = ich_.read();
      
      if (resp == InputChannel.shutdown__)
        close();
      else
        ed_.processEvent((Event)resp);
    }
  
  }

  /**
   * Connect to the specified Inq server. The <code>url</code> identifies
   * the server and must specify the <code>speakinq</code>
   * or <code>speakinqs</code> protocol. The user, passwd and package are
   * required credentials to the login procedure.
   * @param url a string of the form <code>speakinq://my.host.com:1234</code>
   * @param user the login user
   * @param passwd the login password
   * @param inqPackage the package to log in as
   * @param trust whether to trust a self-signed or expired certificate
   * when using a secure connection
   */
  public ServerConnection(String url,
                          String user,
                          String passwd,
                          String inqPackage,
                          boolean trust) throws AnyException
  {
    this(new ConstString(url),
         new ConstString(user),
         new ConstString(passwd),
         new ConstString(inqPackage),
         trust);
  }

  /**
   * Make a service request to the connected server and wait for a response.
   * The named service in the specified package will be invoked.
   * @param service the service to invoke
   * @param sPackage the package in which the service resides
   * @param args the service arguments, or null if no arguments are given
   * @param timeout in ms to await response from Inq server
   * TODO: when instances are properly pooled synchronized can be removed...
   */
  public synchronized Any request(String service,
                                  String sPackage,
                                  java.util.Map<String, String> args,
                                  int timeout) throws AnyException
  {
    Map aArgs = makeArgs(args);
    serviceName_.setValue(sPackage + ".services." + service);
    
    Event e = SendRequest.makeRequestEvent(EventConstants.INVOKE_WEBSVC, serviceName_, null, aArgs);
    
    // Set the request event's parameter to its sequence number.
    // This is echoed back by the server so we can discard
    // any stale response we might have timed out previously
    e.setParameter(new ConstInt(e.getSequence()));
    
    // Remember the last sequence number that was sent  
    sequence_.setValue(e.getSequence());
    
    send(e);
    
    Any ret = null;
    while(connected_ && (ret = responder_.getResponse()) == null)
    {
    	ret = read(timeout);
    }
    
    if (!connected_)
      ret = null;
    
    if (ret != null && transformer_ != null)
    	ret = transformer_.exec(ret);
    
    return ret;
  }
  
  public boolean isConnected() throws AnyException
  {
  	return connected_;
  }
  
  public boolean ping() throws AnyException
  {
  	return ping(pingTimeout_);
  }
  
  public boolean ping(int timeout)
  {
  	long lastPong = lastPong_;
  	
    while(connected_ && (lastPong == lastPong_))
  	{
  		try
  		{
    		send(ping__);
    		if (read(timeout) == null)
    		{
    			// Timed out. Close
    			close();
    		}
  		}
  		catch(Exception e)
  		{
  			close();
  			// TODO: log
  		}
  	}
  	
  	return connected_;
  }
  
  public void close()
  {
    connected_ = false;
    loggedIn_  = false;
    try
    {
      ich_.close();
      och_.close();
    }
    catch(Exception e)
    {
      logger__.log(Level.WARNING, "Exception while closing", e);
      logger__.log(Level.WARNING, "Closed anyway");
    }
  }
  
  public Func setTransformer(Func transformer)
  {
  	Func ret = transformer_;
  	transformer_ = transformer;
  	return ret;
  }
  
  public void setPingTimeout(int pingTimeout)
  {
  	pingTimeout_ = pingTimeout;
  }
  
  public long getLastPongTime()
  {
  	return lastPong_;
  }
  
  protected void finalize()
  {
    logger__.log(Level.WARNING, "Closing on finalization");
    close();
  }
  
  private Map makeArgs(java.util.Map<String, String> args) throws AnyException
  {
    args_.empty();   // if we want to be safe
    if (args != null)
    {
      Iterator<String> i = args.keySet().iterator();
      while (i.hasNext())
      {
        // Key
        String k = i.next();
        k_.setValue(k);
        
        // Value
        Any av = AnyString.NULL;
        
        String v = args.get(k);
        if (v != null)
        {
        	if ((av = convertFromJSON(v)) == null)
        		av = new AnyString(v);
        }        	
        args_.add(k_.cloneAny(), av);
      }
    }
    return args_;
  }
  
  /**
   * Check if this argument looks like Inq JSON format. If it does, parse it 
   * into an Inq structure and return. Otherwise return null.
   * 
   * @param json
   * @return The Inq structure or null if the argument is not Inq json
   */
  private Any convertFromJSON(String json) throws AnyException
  {
  	if (!json.contains("inqJSON"))
  	  return null;
  	
  	try
  	{
    	JSON<Map, Vectored> parser = getParser(json);
    	return parser.parseObject(handler_);
  	}
  	catch(ParseException e)
  	{
  		throw new ContainedException(e);
  	}
  }
  
	private JSON<Map, Vectored> getParser(String source)
	{
		if (parser_ == null)
		{
			parser_ = new JSON<Map, Vectored>(new StringReader(source));
			handler_ = new JSONHandler();
		}
		else
			parser_.ReInit(new StringReader(source));
		
		return parser_;
	}


  private void send(Any e) throws AnyException
  {
    try
    {
      och_.write(e);
      och_.flushOutput();
    }
    catch(AnyException ex)
    {
      close();
      throw ex;
    }
  }
  
  private Any read(int timeout) throws AnyException
  {
    Any a = ich_.read(timeout);
    if (a == InputChannel.shutdown__)
    {
      close();
      a = null;
    }
    else if (a != null)
    {
    	Event e = (Event)a;
    	
    	// Dispatch the event. Service responses get
    	// processed by the WEBSVC_RESP listener
    	ed_.processEvent(e);
    }

    // null if timed out

    return a;
  }
  
  // We get the login response service request as part of the
  // initial message flow. This is not relevant in the
  // web container environment so this listener does nothing.
  private static class ServiceInvoked extends    AbstractAny
                                      implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      //System.out.println("InvokeService.processEvent " + e);
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return InvokeService.eventTypes__;
    }
  }
  
  private class ServiceResponse extends    AbstractAny
                                implements EventListener
  {
    Any response_;
    
    public boolean processEvent(Event e)
    {
    	// Check the response event has the sequence we are expecting. Ignore
    	// it if not
    	if (e.getParameter().equals(sequence_))
    	{
        response_ = e.getContext();
    	}
    	else
    	{
    		logger__.log(Level.WARNING, "Received stale response {0}", e.getContext());
    	}
    	
      return true;
    }
    
    public Array getDesiredEventTypes()
    {
      return webResponseEventTypes__;
    }

    private Any getResponse()
    {
      Any ret = response_;
      response_ = null;
      return ret;
    }
  }

  private class LoginOK extends    AbstractAny
                        implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      //System.out.println("LoginOK.processEvent " + e);
      loggedIn_ = true;
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return UserProcess.loginOKEventTypes__;
    }
  }

  private static class LoginDenied extends    AbstractAny
                                   implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      Map m = (Map)e.getContext();
      throw new AnyException(m.get(deniedReason__).toString());
    }

    public Array getDesiredEventTypes()
    {
      return UserProcess.loginDeniedEventTypes__;
    }
  }

  // TODO: Review this as there is presently no ServerLost event in the
  // web case.
  private class ServerLost extends    AbstractAny
                           implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
    	loggedIn_  = false;
    	connected_ = false;
    	
      logger__.log(Level.SEVERE, "Server lost");
      
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return UserProcess.serverLostEventTypes__;
    }
  }
  
	private class PingResponder extends AbstractAny implements EventListener
	{

		@Override
		public boolean processEvent(Event e) throws AnyException
		{
			lastPong_ = System.currentTimeMillis();
			return true;
		}

		@Override
		public Array getDesiredEventTypes()
		{
			return pongEventTypes__;
		}
	}

  
  // The handler for reading JSON. Objects are represented by
  private class JSONHandler implements Handler<Map, Vectored>
  {
  	// To identify the special container types required
  	// when deserializing typedef and key instances, special
  	// members must be present in the JSON form.
  	private String key_;
  	private String typedef_;
  	
  	private final AnyString str_ = new AnyString(); 
  	
  	private java.util.Map<String, Descriptor> typeCache_;
  	
  	@Override
  	/**
  	 * Clear down the special members. Object creation is deferred until
  	 * the first JSON member is found. If a key, these must be in the
  	 * order key followed by typedef. Note that the supported "special"
  	 * object productions (of typedef instance or key instance) do not
  	 * themselves contain objects, so we do not need to preserve key_
  	 * and typedef_ as we go.
  	 */
  	public Map startObject(String name, Map parentObject, Vectored parentArray)
  	{
  		key_     = null;
  		typedef_ = null;
  		
  		return null;
  	}
  	
  	@Override
  	public Vectored startArray(String name, Map parentObject, Vectored parentArray)
  	{
  		// TODO: We could include the node set attribute in the JSON
  		// form and even beef this up with what implementation to use,
  		// so at the moment this is as basic as it gets
  		return new AnyOrderedMap();
  	}
  	
		@Override
		public Vectored endArray(String name, Vectored array,
				Map parentObject, Vectored parentArray)
		{
			if (parentObject != null)
  			parentObject.add(AbstractValue.flyweightString(name), array);
			else if (parentArray != null)
			{
				// Arrays are, in fact, AnyOrderedMaps, see above
				Map m = (Map)parentArray;
  			m.add(AbstractValue.flyweightString(name), array);
			}
			
			return array;
		}

		@Override
		public Map endObject(String name, Map object, Map parentObject,
				Vectored parentArray)
		{
			if (parentObject != null)
  			parentObject.add(AbstractValue.flyweightString(name), object);
			else if (parentArray != null)
			{
				// Arrays are, in fact, AnyOrderedMaps, see above
				Map m = (Map)parentArray;
  			m.add(new ConstInt(System.identityHashCode(object)), object);
			}
			
			return object;
		}

		@Override
		public String name(String name, Map object)
		{
			return name;
		}

		@Override
		public Vectored valueToArray(String value, Vectored array, int count, boolean isNumeric)
		{
			// 1. We have no meta-data to drive the Any we should make, so
			// leave it as a string
			// 2. As we are using ordered maps for the array implementation,
			// we need a key to add the value with. Use its identity
			IntI k = new ConstInt(System.identityHashCode(value));
			Map m = (Map)array;
			m.add(k, new AnyString(value));
			
			return array;
		}

		@Override
		public Map valueToObject(String name, String value, Map object,	boolean isNumeric)
		{
			// The order of the "special" JSON members is important and should be
			// kept as follows if JSONWriter is modified:
			// 1. KeyDef.key__
			// 2. Descriptor.typedef__
			// There is no handling of nodesets yet, but this would be mutually exclusive
			// with keys/typedefs
			
			if (name.equals(KeyDef.key__.toString()))
			{
				// The special member indicating that this is a key. Just note
				// it down - until we know the typedef we cannot make the appropriate
				// map.
				key_ = value;
			}
			else if (name.equals(Descriptor.typedef__.toString()))
			{
				// TODO: we are only doing this for the case where the metadata
				// is embedded in the production and the typedef name is used
				// as a JSON member name. That doesn't happen in this part of
				// the production, so consider 
				typedef_ = value.replace('_', '.');
				
				Descriptor d = getTypedef(typedef_);
				
				if (key_ == null)
					object = (Map)d.newInstance();
				else
				{
					str_.setValue(value);
					KeyDef kd = d.getKey(str_);
					object = kd.getKeyProto(); 
				}
			}
			else
			{
				// Not a special member. If we are using an object
				// created by a special member then this already
				// has the expected fields in it. Copy the value
				// over.
				if (typedef_ != null)
				{
					str_.setValue(name);
					Any v = object.get(str_);

					// Is it JSON null ?
					if (com.inqwell.json.DefaultHandler.isNull(value))
					{
						((Value)v).setNull();
					}
					else if (com.inqwell.json.DefaultHandler.isTrue(value))
					{
						v.copyFrom(AnyBoolean.TRUE);
					}
					else if (com.inqwell.json.DefaultHandler.isFalse(value))
					{
						v.copyFrom(AnyBoolean.FALSE);
					}
					else
					{
						// Just copy from the string value. Because it is a string
						// this is fine for decimal values also.
						str_.setValue(value);
						v.copyFrom(str_);
					}
				}
				else
				{
					// Not reconstructing a particular instance type conveyed
					// via the special members. Just add the string to the
					// current map, creating one if not already done so.
					if (object == null)
						object = AbstractComposite.simpleMap();
					
					object.add(AbstractValue.flyweightString(name), new AnyString(value));
				}
			}
			
			return object;
		}
		
		private Descriptor getTypedef(String name)
		{
			Descriptor d = null;
			d = typeCache_.get(name);
			if (d == null)
			{
				// Find the typedef
        LocateNode ln = new LocateNode(name);
        try
        {
          d = (Descriptor)EvalExpr.evalFunc(Transaction.NULL_TRANSACTION,
              Catalog.instance().getCatalog(),
              ln,
              Descriptor.class);
        }
        catch(AnyException e)
        {
          throw new RuntimeContainedException(e);
        }
			}
			
			if (d == null)
				throw new AnyRuntimeException("Unknown type " + name);
			
			return d;
		}
  }

  public static void main(String args[]) throws AnyException,
                                                InterruptedException,
                                                MalformedURLException,
                                                IOException,
                                                TransformerException
  {
    CommandArgs cArgs = new CommandArgs(args);
    AnyString host = new AnyString();
    AnyString usr  = new AnyString();
    AnyString pwd  = new AnyString();
    AnyString pkg  = new AnyString();
    
    if (!cArgs.arg("-u", usr))
      usage();
    
    if (!cArgs.arg("-p", pwd))
      usage();
    
    //cArgs.arg("-pkg", pkg, "xy");
    cArgs.arg("-pkg", pkg, "examples.petstore");
    cArgs.arg("-h", host, "speakinq://localhost");
    
    // Create a connection to the server
    ServerConnection c = new ServerConnection(host.toString(),
                                              usr.toString(),
                                              pwd.toString(),
                                              pkg.toString(),
                                              true);
    
    System.out.println("Connected!");
    //Thread.sleep(5000);
    
    // Send a request
    //HashMap<String, String> svcArgs = new HashMap<String, String>();
    //svcArgs.put("setName", "myList");
    //Any resp = c.request("loadAllCountries", "xy.sm", svcArgs, -1);
    Any resp = c.request("webGetAccount", "examples.petstore", null, -1);
    //Any resp = c.request("metaToJson", "system.server", null, -1);

		XMLXStream s = new XMLXStream();
		
		s.setJsonOutput(true);
		s.setWriteMeta(true);
		s.setFormatOutput(false);

		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		s.setStreams(null, bs);
		s.write(resp, null);
		s.close();

		// we're all good.
		String op = bs.toString();
		System.out.println("datum is " + op);

    
    
//    System.out.println(resp);
//    
//    System.out.println("Trying DOM conversion");
//    XMLXStream s = new XMLXStream();
//    s.setDOMOutput(true);
//    s.setWriteMeta(true);
//    s.write(resp, null);
//    System.out.println("DOM result " + s.getDOMResult()); // doesn't say much!
//    
//    // Perform the identity transform to a stream target. Has the purpose
//    // of 1) doing a transform and 2) having a look at the DOM result
//    
//    // Identity transform is in the inq jar file. Get an InputStream
//    // for it.
//    URL u = new URL("classpath:///xsl/identity.xsl");
//    InputStream is = u.openStream();
//    
//    TransformerFactory tf = TransformerFactory.newInstance();
//    Transformer t = tf.newTransformer(new StreamSource(is));
//    t.setOutputProperty(OutputKeys.INDENT, "yes");
//    Result r = new StreamResult(new File("dom.xml"));
//    t.transform(new DOMSource(s.getDOMResult()), r);
//    
//    // Write the same result through the text xml production
//    // for comparison purposes
//    s.setDOMOutput(false);
//    s.open(null, new ConstString("text.xml"), PhysicalIO.write__);
//    s.write(resp, null);
//    s.close();
//    
//    // Write the same output through the json text production
//    s.setJsonOutput(true);
//    s.open(null, new ConstString("text.json"), PhysicalIO.write__);
//    s.write(resp, null);
//    s.close();
//    
//    // And again to a memory string. We supply the string ourselves
//    // so the URL's "host" doesn't matter
//    URL uu = new URL("string://dummy");
//    StringURLConnection suc = (StringURLConnection)uu.openConnection();
//    AnyString myStr = new AnyString();
//    OutputStream os = suc.getOutputStream(myStr);
//    s.setStreams(null, os);
//    s.write(resp, null);
//    s.close();
//    System.out.println("JSON text: ");
//    System.out.println(myStr);
//    
//    
//    System.out.println("Results in dom.xml and text.xml");
    
    
    c.close();
    
    System.exit(0);
  }

  private static void usage ()
  {
    System.out.println ("Usage");
    System.out.println ("  -u   <user>");
    System.out.println ("  -p   <passwd>");
    System.out.println ("  -pkg <package>");
    System.out.println ("  -h   <server host URL>");
    System.exit(1);
  }
}
