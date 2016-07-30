/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.connect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.ConstString;
import com.inqwell.any.ContainedException;
import com.inqwell.any.Crypt;
import com.inqwell.any.Event;
import com.inqwell.any.EventConstants;
import com.inqwell.any.EventDispatcher;
import com.inqwell.any.EventListener;
import com.inqwell.any.Globals;
import com.inqwell.any.InqInterpreter;
import com.inqwell.any.IntI;
import com.inqwell.any.InvokeService;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.SendRequest;
import com.inqwell.any.SimpleEvent;
import com.inqwell.any.StartProcessEvent;
import com.inqwell.any.StringI;
import com.inqwell.any.SystemProperties;
import com.inqwell.any.UserProcess;
import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.OutputChannel;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.io.XMLXStream;
import com.inqwell.any.net.InqStreamHandlerFactory;
import com.inqwell.any.util.CommandArgs;

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
  
  private static Array webResponseEventTypes__;
  private static Any   deniedReason__   = AbstractValue.flyweightString("reason");
  
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
   * The named service in the
   * specified package will be invoked. 
   * @param service the service to invoke
   * @param sPackage the package in which the service resides
   * @param args the service arguments, or null if no arguments are given
   * @param timeout 
   */
  public synchronized Any request(String service,
                                  String sPackage,
                                  java.util.Map<String, String> args,
                                  int timeout) throws AnyException
  {
    Map aArgs = makeArgs(args);
    serviceName_.setValue(sPackage + ".services." + service);
    
    Event e = SendRequest.makeRequestEvent(EventConstants.INVOKE_WEBSVC, serviceName_, null, null, aArgs, null);
    sequence_.setValue(e.getSequence());
    
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
    
    Any ret = null;
    while(connected_ && (ret = responder_.getResponse()) == null)
    {
      Any a = ich_.read(timeout);
      if (a == InputChannel.shutdown__)
      {
        close();
      }
      else
      {
        e = (Event)a;
        ed_.processEvent(e);
        if (ret != null)
        {
          // Check sequence number is as expected. If not discard and
          // continue to wait
          if (!sequence_.equals(e.getParameter()))
          {
            // TODO: Log warning
            System.err.println("Ignoring out-of-sequence response");
            ret = null;
          }
        }
      }
    }
    
    if (!connected_)
      ret = null;
    
    return ret;
  }
  
  public boolean isConnected() throws AnyException
  {
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
  
  private Map makeArgs(java.util.Map<String, String> args)
  {
    args_.empty();   // if we want to be safe
    if (args != null)
    {
      args_.empty();   // if we want to be safe
      
      Iterator<String> i = args.keySet().iterator();
      while (i.hasNext())
      {
        // Key
        String k = i.next();
        k_.setValue(k);
        
        // Value
        String v = args.get(k);
        Any av = args_.getIfContains(k_);
        if (av == null)
        {
          if (v == null)
            av = AnyString.NULL;
          else
            av = new AnyString(v);
          
          args_.add(k_.cloneAny(), av);
        }
        else
        {
          StringI s = (StringI)av;
          s.setValue(v);
        }
      }
    }
    return args_;
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
  
  private static class ServiceResponse extends    AbstractAny
                                       implements EventListener
  {
    Any response_;
    
    public boolean processEvent(Event e)
    {
      response_ = e.getContext();
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

  private static class ServerLost extends    AbstractAny
                                  implements EventListener
  {
    public boolean processEvent(Event e) throws AnyException
    {
      logger__.log(Level.SEVERE, "Server lost");
      return true;
    }

    public Array getDesiredEventTypes()
    {
      return UserProcess.serverLostEventTypes__;
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
    //Any resp = c.request("webGetAccount", "examples.petstore", null, -1);
    Any resp = c.request("metaToJson", "system.server", null, -1);

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
