/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/HttpTunnel.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.Any;
import com.inqwell.any.IntI;
import com.inqwell.any.ConstInt;
import com.inqwell.any.AnyString;
import com.inqwell.any.ConstString;
import com.inqwell.any.StringI;
import com.inqwell.any.AnyException;
import com.inqwell.any.ContainedException;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Map;
import com.inqwell.any.Globals;
import com.inqwell.any.util.Util;
import com.inqwell.any.util.Base64;
import com.inqwell.any.net.NetUtil;
import com.inqwell.any.io.ResolvingInputStream;
import com.inqwell.any.io.ReplacingOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.SocketException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Date;
import java.util.StringTokenizer;
import java.text.SimpleDateFormat;


/**
 * A channel driver that forms output into an HTTP POST request
 * and awaits input from an issued HTTP GET request.
 * <B>Output:</B> The channel traffic is buffered until
 * <code>flushOutput</code> is called, when the POST request
 * is formed placing the data in the message body with the
 * appropriate content length.
 */
public class HttpTunnel extends    AbstractChannelDriver
                        implements ChannelDriver
{
  public static final String    POST   = "POST ";
  public static final String    GET    = "GET ";
  public static final String    HTTP   = "HTTP/";
  public static final String    OK200  = " 200 OK\r\n";
  public static final String    NS410  = " 410 NoSession\r\n";
  public static final String    SERVER = "Server: Inq/1.0\r\n";
  public static final Any inq__  = new ConstString("/httpinq");
  public static final Any inqs__ = new ConstString("/httpinqs");
	public static final Any id__   = new ConstString("id");
  public static final Any POST1  = new ConstString("POST");
  public static final Any GET1   = new ConstString("GET");
  public static final Any HEAD   = new ConstString("HEAD");

	private static Any wwwAuthenticate__   = new ConstString("www-authenticate");
	private static Any proxyAuthenticate__ = new ConstString("proxy-authenticate");

	// Relevant header sent
	private static String wwwAuthorize__   = "Authorization: ";
	private static String proxyAuthorize__ = "Proxy-Authorization: ";

	private static final String httpProxySet__  = "http.proxySet";
	private static final String proxySet__      = "proxySet";
	private static final String httpProxyHost__ = "http.proxyHost";
	private static final String proxyHost__     = "proxyHost";
	private static final String httpProxyPort__ = "http.proxyPort";
	private static final String proxyPort__     = "proxyPort";

	private boolean usingProxy_ = false;
	private String  proxyHost_;
	private int     proxyPort_ = -1;
	
	private static int defaultPort__  = -1;
	
	// and the one we will use when requested to provide authentication
  private String authHeader_;
  	
	
  private String                 httpCommand_; // can be null if we are the
                                               // the response side
  private String                 httpCommandLine_;
  private URL                    url_;
  private int                    port_;
  
  // If do not want to buffer in memory then consider a file
  private ByteArrayOutputStream         header_;
  private ExposedByteArrayOutputStream  body_; // these are
  private ReplacingOutputStream         oos_;  // connected
  
  private java.net.Socket        socket_;
  private Serialize              serializer_;
  private boolean                defunctWrite_   = false;
  
  private boolean                serverInput_  = false;
  private boolean                serverOutput_ = false;
  
  private InputStream            bufferingStream_;
  private byte[]                 buf_ = null; // for server error recovery
  
  //private Map                    urlQuery_ = AbstractComposite.simpleMap();
	private SimpleDateFormat dateFormat_ =
             new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");
	

	private PasswordAuthentication auth_;
	private String                 scheme_;
	
	private Any                    sessionId_;
	
  private ContentCipher          cipher_ = null;
  
  private String                 protocolLevel_ = "1.0";
 
  /**
   * Create a channel driver that will operate to the given
   * URL.  The traffic will be encoded/decoded as
   * serialized objects using the Inq standard
   * replacements specified in <code>Globals</code>.
   * <p>
   * A HttpTunnel constructed in this way will initiate the
   * HTTP <code>GET</code> or <code>POST</code> request.  A
   * <code>GET</code> request is used to open a connection
   * to an Inq server allowing the server to send messages
   * when it is ready to do so.  A <code>POST</code> request
   * is used to send messages from client to server.  In general,
   * two separate connections are used to provide asynchronous,
   * bi-directional communication as used by Inq channels.
   * A single <code>HttpTunnel</code> is used for both sent
   * and received messages to establish a session ID.
   * <p>
   * @param httpCommand one of HttpTunnel.POST or HttpTunnel.GET
   * @param url the URL for this HTTP request
   * @param port the port number for the connection (required if
   * the default port is being used)
   */
  public HttpTunnel(String  httpCommand,
                    URL     url,
                    int     port,
                    boolean encrypted) throws AnyException
  {
    initProxy();
    if (encrypted)
    {
    	cipher_ = ContentCipher.makeCipher();
    }
  	
    setHttpCommand(httpCommand);
    url_  = url;
    port_ = port;
    setupHttpCommand();
  }
  
  public HttpTunnel(java.net.Socket s) throws AnyException
  {
  	this(s, null, false);
  }

  /**
   * Create a channel driver around a given <code>Socket</code>.
   * The traffic will be encoded/decoded as
   * serialized objects using the Inq standard
   * replacements specified in <code>Globals</code>.
   * <p>
   * A HttpTunnel is constructed around a socket accepted
   * from an incoming connection and may act as an incoming
   * channel (from which we can read messages) or an outgoing
   * channel (to which we can send messages).
   */
  public HttpTunnel(java.net.Socket s, boolean encrypted) throws AnyException
  {
  	this(s, null, encrypted);
  }
  
  public HttpTunnel(java.net.Socket s, Any sessionId, boolean encrypted) throws AnyException
  {
    if (encrypted)
    {
    	cipher_ = ContentCipher.makeCipher();
    }
  	
    socket_ = s;
    setSessionId(sessionId);
  	//doHttpCommandLine();
  }
  
  public Any read() throws AnyException
  {
  	// If the serializer is already set up then we are
  	// a server tunnel constructed around an accepted
  	// socket or a flushed output tunnel that is also
  	// being used as an input channel (no session id yet).
  	// Then its OK to read the stream.
  	// If the serializer is null then we must set up
  	// a get request and wait on the serializer
  	if (serializer_ != null && serializer_.getInputStream() != null)
  	{
      //System.out.println("HttpTunnel.read() 1");
      Any ret = serializer_.read();
      //System.out.println("HttpTunnel.read() 1.1 " + ret);
      return ret;
  	}
  	else
  	{
      //System.out.println("HttpTunnel.read() 2");
  		if (httpCommand_ != GET)
  		  throw new AnyException("Tunnel is not a GET request");
  		
  		initHeaderStream();
  		initSerializer();
  		doHttpRequest();
  		flushOutput();
      //System.out.println("HttpTunnel.read() 3");
      return serializer_.read();
  	}
  }
  
  public void write(Any a) throws AnyException
  {
  	if (defunctWrite_)
  	  AnyException.throwExternalException(new IOException("Not in write state"));

    initOutputStreams(OK200);
    serializer_.write(a);
  }
  
  public void purgeReceived()
  {
  }
  
  public void shutdownInput() throws AnyException
  {
    if (socket_ != null)
    {
      try
      {
        socket_.shutdownInput();
      }
      catch (IOException e)
      {
        AnyException.throwExternalException (e);
      }
    }
    doClose();
  }
  
  public ChannelDriver resetOutput(ChannelDriver d) throws AnyException
  {
  	if (!(d instanceof HttpTunnel))
  	  throw new AnyException("Attempt to reset HttpTunnel with " +
  	                         d.getClass());
  	
  	HttpTunnel newDriver = (HttpTunnel)d;
  	if (defunctWrite_ && (buf_ != null))
  	{
  		// We have a buffer in this, the old tunnel, we were unable to
  		// send.  Put it in new driver and send it.
  		newDriver.buf_ = buf_;
  		newDriver.initHeaderStream();
  		newDriver.doHttpRequest(OK200);
  		newDriver.flushOutput();
  		newDriver.close();
  	}
  	return newDriver;
  }

  private void setHttpCommand(String httpCommand)
  {
    httpCommand_ = httpCommand;
  }
  
  public void setSessionId(Any sessionId)
  {
    sessionId_ = sessionId;
    setupHttpCommand();
    // Client tunnel is reused, server is not
    if (httpCommand_ != null)
  	  defunctWrite_ = false;
  }

  public void setCipher(ContentCipher cipher)
  {
  	cipher_ = cipher;
  }
  
  public ContentCipher getCipher()
  {
  	return cipher_;
  }
  
  /**
   * Writes all output sent to this channel driver since the
   * last call to <code>flushOutput</code>.  Forms an HTTP POST
   * request whose body is the pending output.
   */
  public void flushOutput() throws AnyException
  {
		if (header_ == null)
		  return;

    byte[] buf = null;

		try
		{
			buf  = (buf_ != null) ? buf_ : encrypt();
			buf_ = null;
      doContentLength(buf);
      //System.out.println(header_);
      
			int contentLength = -1;
			
	    InputStream  is = null;
	    OutputStream os = null;
	    
			while (contentLength < 0)
			{
				initTunnel();
		
	      is = socket_.getInputStream();
	      os = socket_.getOutputStream();
				
		    header_.writeTo(os);
				os.flush();
				
				writeBody(os, buf);

				
				os.flush();
			  //socket_.shutdownOutput();

				contentLength = doResponseHeaders(is, buf);
	      //System.out.println("Content length = " + contentLength);
			}
			
			closeHeader();
			
			// If we are a POST request with no session ID or a GET
			// request then we can set up the read stream, now that we
			// have read the http headers.
			if (isClientInput())
			{
        defunctWrite_ = true;
//	      ObjectInputStream ois = new ResolvingInputStream(is,
//	                                      Globals.channelInputReplacements__);
        ResolvingInputStream ois = bufferInput(is, contentLength);
	      //System.out.println("flushOutput setting read stream");
	      serializer_.setInputStream(ois);
	    }
	    else
	    {
	    	// No body is expected from the server or we are a server
	      // output channel.
	    	if (httpCommand_ != null)
	    	  doClose(); // client, so close
	    	else
	    	{
          defunctWrite_ = true;
	    	  closeWrite();
        }
	    }
    }
    catch (IOException e)
    {
    	if (serverOutput_)
    	{
        defunctWrite_ = true;
        buf_          = buf;
    	}
    	doClose();
  		AnyException.throwExternalException(e);
    }
  }

  public Any getSessionId()
  {
    return sessionId_;
  }

	public boolean isEmpty()
	{
		return false;
	}
	
  public boolean isFull()
  {
		return false;
  }

  public boolean canShutdown()
  {
    try
    {
      if ((bufferingStream_ != null) &&
          (bufferingStream_.available() != 0))
        return false;
    }
    catch (IOException e)
    {
      return true;
    }
    
    return true;
  }
  
	// Check if this tunnel can do input on the server side
  public boolean isServerInput()
  {
  	// if we have an http command we cannot be a server
  	if (httpCommand_ != null)
  	  return false;
    
    return (serverInput_);
  }
  
  public void doRequestHeaders() throws AnyException
  {
  	doRequestHeaders(OK200);
  }
  
  // Read the request headers from the Inq client (i.e. we are a
  // server-side tunnel
  public void doRequestHeaders(String responseCode) throws AnyException
  {
  	try
    {
    	InputStream  is = socket_.getInputStream();
    	
		  // and read the headers
		  Map headers = NetUtil.readHeaders(is);
		  
	    if (serverInput_)
	    {
        int contentLength = NetUtil.getContentLength(headers);
        
	    	// Its an input stream and, if there's no session id its
	    	// an output stream as well. Since we've consumed the
	    	// http headers its OK to create the object stream now.

	    	if (serverOutput_)
	    	{
	    		initOutputStreams(responseCode);
	    		// When we are bidirectional read all the input that is
	    		// available into a memory buffer so we avoid discarding
	    		// pending input by closing the socket on flushing output.
	    		ResolvingInputStream ois = bufferInput(is, contentLength);
	        serializer_.setInputStream(ois);
	    	}
	    	else
	    	{
	    		// When we are unidirectional we still buffer the input
	    		// because we must read the content length and then send
	    		// back the headers to the client.
	    		ResolvingInputStream ois = bufferInput(is, contentLength);
	    		
	    		// The initInputStreams method sets up the response
	    		// headers.
	    		initInputStreams(ois, responseCode);
	    		
	    		flushOutput();
	    	}
	    	//System.out.println("doRequestHeaders 1 " + getSessionId());
	    }
	    else
	    {
	    	// We are an output stream 
    		initOutputStreams(responseCode);
	    	//System.out.println("doRequestHeaders 2 " + getSessionId());
	    }
    }
    catch(Exception e)
    {
    	AnyException.throwExternalException(e);
    }
  }
  
  public static void setDefaultPort(int defaultPort)
  {
    defaultPort__ = defaultPort;
  }
  
  public static URL processHttpCommand(String    commandLine,
                                       StringI   httpCommand,
                                       String    host,
                                       StringI   path,
                                       StringI   protocolLevel)
  {
		StringTokenizer st = new StringTokenizer(commandLine);
		
		String command  = st.nextToken();
    String url      = st.nextToken();
    String protocol = st.nextToken();
    
    URL u = null;
    try
    {
      u = new URL(url);
    }
    catch(MalformedURLException mue)
    {
    	try
    	{
	    	u = new URL("http://" + host + url);
    	}
    	catch (Exception e) { e.printStackTrace(); }
    }
    httpCommand.setValue(command);
    path.setValue(u.getPath());
    
    int i = protocol.indexOf('/');
    protocolLevel.setValue(protocol.substring(i+1));

    return u;
  }
  
  protected synchronized void doClose() throws AnyException
  {
  	closeWrite();
  	
  	try
    {
	    if (serializer_ != null)
	      serializer_.close();
    }

    catch (AnyException e)
    {
    	e.printStackTrace();
  		//AnyException.throwExternalException(e);
    }
    
    finally
    {
      serializer_      = null;
      bufferingStream_ = null;
    }
  }
  
  private synchronized void closeWrite() throws AnyException
  {
  	closeSocket();
  	closeHeader();
  	closeBody();
  }
  
  private void closeSocket()
  {
  	try
    {
	    if (socket_ != null)
	    {
	      //socket_.getInputStream().close();
	      if (serverOutput_)
	      {
	      	socket_.shutdownOutput();
	      }
	      else
	      {
		      socket_.getOutputStream().close();
		      socket_.close();
	      }
	    }
    }
      
    catch (IOException e)
    {
    	//e.printStackTrace();
  		//AnyException.throwExternalException(e);
    }

    finally
    {
  		if (socket_ != null)
  		{
	    	try
	    	{
		      if (serverOutput_)
		      {
		      	socket_.shutdownOutput();
		      }
		      else
		      {
	    		  socket_.close();
		      }
	    	}
	    	catch (IOException e) {}
  		}
      socket_     = null;
    }
  }
  
  private void closeHeader() throws AnyException
  {  	
  	try
    {
	    if (header_ != null)
	      header_.close();
    }

    catch (IOException e)
    {
    	e.printStackTrace();
    }
    
    finally
    {
      header_ = null;
    }
  }
  
  private void closeBody() throws AnyException
  {  	
  	try
    {
	    if (body_ != null)
	      body_.close();
    }

    catch (IOException e)
    {
    	e.printStackTrace();
    }
    
    finally
    {
      body_ = null;
    }
  }
  
  private void doHttpRequest() throws AnyException
  {
  	doHttpRequest(null);
  }
  
  // Write the http headers, excluding the content-length
  private void doHttpRequest(String responseCode) throws AnyException
  {
  	try
  	{
      header_.reset();
      
  		if (httpCommandLine_ != null)
  	  {
				header_.write(httpCommandLine_.getBytes());
		
		    if (auth_ != null)
		    {
		    	// provide authentication header as appropriate
		    	header_.write(authHeader_.getBytes());
		    	
		    	// we saved the scheme, only doing basic auth at present
		    	header_.write(scheme_.getBytes());
		    	header_.write(' ');
		    	StringBuffer authStr = new StringBuffer();
		    	authStr.append(auth_.getUserName());
		    	authStr.append(':');
		    	authStr.append(auth_.getPassword());
		    	header_.write(Base64.base64Encode(authStr.toString().getBytes()));
		    	header_.write("\r\n".getBytes());
		    }
	    
				header_.write("User-Agent: Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 4.0)\r\n".getBytes());
				//outputBuffer.write("Connection: Keep-Alive\r\n".getBytes());
		
				header_.write(("Host: " +
		                url_.getHost() +
                         ((port_ != defaultPort__) ? (":" + port_)
                                                  : ("")) +
		                "\r\n").getBytes());
  	  }
  	  else
  	  {
        header_.write(HTTP.getBytes());
        header_.write(protocolLevel_.getBytes());
	      header_.write(responseCode.getBytes());
	      header_.write(SERVER.getBytes());
  	  }
	
			//outputBuffer.write("From: ellens@cs.washington.edu\r\n".getBytes());
			header_.write("Date: ".getBytes());
			header_.write(dateFormat_.format(new Date()).getBytes());
			header_.write("\r\n".getBytes());
			                   
			//outputBuffer.write("Content-Type: application/x-www-form-urlencoded\r\n".getBytes());
			//outputBuffer.write("Transfer-Encoding: identity\r\n".getBytes());
			//header_.write("Pragma: no-cache\r\n".getBytes());
			//header_.write("Cache-Control: no-cache\r\n".getBytes());
			header_.write("Connection: close\r\n".getBytes());
			//header_.write("Proxy-Connection: close\r\n".getBytes());
			header_.write("Content-Type: application/octet-stream\r\n".getBytes());
  	}
  	catch (IOException e)
  	{
  		AnyException.throwExternalException(e);
  	}
  }

  private void doContentLength(byte[] buf) throws AnyException
  {
    try
    {
	    header_.write(("Content-Length: " +
		                  ((buf != null) ? buf.length : 0) +
		                  "\r\n").getBytes());
			
			header_.write("\r\n".getBytes());
    }
  	catch (IOException e)
  	{
  		AnyException.throwExternalException(e);
  	}
  }
  
  public void setupTunnel(URL       u,
                          StringI   httpCommand,
                          StringI   protocolLevel) throws AnyException
  {
  	try
    {
      protocolLevel_ = protocolLevel.getValue();
	    String  q      = u.getQuery();

	    boolean sessionDefunct = false;
	    
	    if (q != null)
	    {
		    Map query = AbstractComposite.map();
		    NetUtil.parseURLQuery(q, query);
		    if (query.contains(id__))
		    {
		    	// only set the session id in the tunnel if it is
		    	// still active.
		    	IntI sessionId = new ConstInt(query.get(id__).toString());
		    	setSessionId(sessionId);
          if (!Globals.sessionList__.isSessionActive(sessionId))
            sessionDefunct = true;
		    }
	    }

	    if (httpCommand.equals(POST1))
	    {
	    	// Its an input stream and, if there's no session id its
	    	// an output stream as well.
	    	serverInput_ = true;

	    	if (getSessionId() == null || sessionDefunct)
	    	{
	    		serverOutput_ = true;
	    	}
	    }
	    else
	    {
	    	// We are an output stream 
	    	serverOutput_ = true;
	    }
    }
    catch(Exception e)
    {
    	AnyException.throwExternalException(e);
    }
  }
  
  // Read the response headers from the Inq server.  If we are the
  // server then there's nothing to do.  Return content length or
  // -1 if there's an error
  private int doResponseHeaders(InputStream is,
                                byte[]      buf) throws AnyException,
                                                        SocketException,
                                                        IOException
  {
  	if (url_ == null)
  	  return 0;
    
    try
    {
      while (true)
      {
        // read back the status line in the response
        //System.out.println ("Reading status line for command: " + httpCommand_);
        String statusLine = Util.readLine(is);
        //System.out.println ("Status: " + statusLine);

        // and read the headers
        Map headers = NetUtil.readHeaders(is);
        int contentLength = NetUtil.getContentLength(headers);
        if (contentLength < 0)
          contentLength = 0;

        // Check the response code
        int responseCode = getResponseCode(statusLine);
        //System.out.println ("ResponseCode: " + responseCode);

        if (responseCode >= 200 && responseCode < 300)
          return contentLength; // whoopeedo

        if (responseCode >= 100 && responseCode < 200)
          continue;

        if (responseCode == HttpURLConnection.HTTP_PROXY_AUTH ||
            responseCode == HttpURLConnection.HTTP_UNAUTHORIZED)
        {
          closeSocket();		
          handleAuthorisation(responseCode, headers, buf);
          return -1;
        }
        else
        {
          // Anything else we can't handle
          doClose();
          throw new SocketException(statusLine);
        }
      }
    }
    catch (IOException iox)
    {
      // reflect io exceptions as socket exceptions so we
      // keep the read open.
      //System.out.println ("doResponseHeaders caught");
      //iox.printStackTrace();
      throw new SocketException(iox.getMessage());
    }
    catch (Exception x)
    {
      // reflect io exceptions as socket exceptions so we
      // keep the read open.
      //System.out.println ("doResponseHeaders caught");
      //x.printStackTrace();
      throw new SocketException(x.getMessage());
    }
  }
  
  private byte[] encrypt() throws IOException
  {
  	byte[] buf = null;
  	
  	if (body_ != null)
  	{
    	oos_.close();
    	
    	buf = body_.toByteArray();
    	if (cipher_ != null)
    	  buf = cipher_.encrypt(buf);
  	}
  	
  	return buf;
  }
  
  private void writeBody(OutputStream os, byte[] buf) throws IOException
  {
    if (buf != null)
    {
    	int count  = buf.length;
    	int indx   = 0;
    	
    	while (indx < count)
    	{
    		os.write(buf, indx, (indx + 512) > count ? count - indx
    		                                         : 512);
    		indx += (indx + 512) > count ? count - indx
    		                             : 512;
				os.flush();
				//System.out.println("Write content " + indx);
    	}
    }
    	
    //  body_.writeTo(os);

  }
  
  // Request authorisation details for the remote server
  private void handleAuthorisation(int    responseCode,
                                   Map    headers,
                                   byte[] buf) throws AnyException,
                                                      IOException
  {
    if (auth_ == null)
    {
      StringI authHeader;

      if (responseCode == HttpURLConnection.HTTP_PROXY_AUTH)
      {
        authHeader  = (StringI)headers.get(proxyAuthenticate__);
        authHeader_ = proxyAuthorize__;
      }
      else
      {
        authHeader  = (StringI)headers.get(wwwAuthenticate__);
        authHeader_ = wwwAuthorize__;
      }

      StringTokenizer st = new StringTokenizer(authHeader.getValue());

      scheme_ = st.nextToken();
      String realm  = null;
      if (st.hasMoreTokens())
      {
        realm = st.nextToken(); // not sure what to do with this
      }

      auth_ = Authenticator.requestPasswordAuthentication
                             (null,
                              port_,
                              "HTTP",
                              "Authorization required for " + url_,
                              scheme_);
      doHttpRequest();
      doContentLength(buf);
    }
    else
      throw new AnyException("Proxy Authentication Failure");
  }

  private void initOutputStreams(String responseCode) throws AnyException
  {
    if (header_ != null)
      return;
      
    if (httpCommand_ == GET) // client side
      throw new UnsupportedOperationException("Tunnel is read-only");

    // we will do some output
    initHeaderStream();
    body_   = new ExposedByteArrayOutputStream(1024);
    try
    {
	    oos_    = new ReplacingOutputStream(body_,
	                                        Globals.channelOutputReplacements__);
    }
    catch (IOException e)
    {
    	AnyException.throwExternalException(e);
    }
    
    initSerializer();
    
    doHttpRequest(responseCode);
    
    // If we are a server then write the headers to the
    // the socket now and reset the stream for the remaining
    // content length.  This will force a socket exception if
    // the client hasn't yet got round to sending us a
    // new GET request
    if (serverOutput_ && socket_ != null)
    {
    	try
    	{
	    	OutputStream os = socket_.getOutputStream();
	    	//System.out.println(header_);
		    header_.writeTo(os);
		    os.flush();
		    header_.reset();
    	}
	    catch (IOException e)
	    {
	    	AnyException.throwExternalException(e);
	    }
    }
  }
  
  private void initInputStreams(ResolvingInputStream ois,
                                String               responseCode) throws AnyException
  {
    if (header_ != null)
      return;
    
    // Required to send response, even if its only an OK header
    initHeaderStream();
    
    serializer_ = new Serialize(ois);

    doHttpRequest(responseCode);
  }
  
  private void initHeaderStream()
  {
    header_ = new ByteArrayOutputStream(1024);
  }
  
  private void initTunnel() throws AnyException
  {
  	if (socket_ != null)
  	  return;
  	  
  	try
  	{
			if (usingProxy_)
			{
	      //System.out.println ("Connecting to " + proxyHost_ + ":" + proxyPort_);
			  socket_ = new java.net.Socket(proxyHost_, proxyPort_);
	    }
			else
			{
			  socket_ = new java.net.Socket(url_.getHost(), port_);
	    }
		  socket_.setTcpNoDelay(true);
  	}
  	catch(IOException e)
  	{
      AnyException.throwExternalException(e);
  	}
  }
  
  private void initSerializer()
  {
    if (serializer_ == null)
      serializer_ = new Serialize(oos_);
  }
  
	private int getResponseCode(String statusLine)
	{
		int index = 0;
		//responseMessage_ = "";
		String responseMessage;
		int    responseCode;
		try
		{
			index = statusLine.indexOf(' ');
			while(statusLine.charAt(index) == ' ')
				index++;
				
			responseCode    = Integer.parseInt(statusLine.substring(index, index + 3));
			responseMessage = statusLine.substring(index + 4).trim();
			return responseCode;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			//System.out.println ("index " + index);
			//System.out.println ("line " + statusLine);
			return -1;
		}
	}
	
	private ResolvingInputStream bufferInput(InputStream is,
                                           int         contentLength) throws IOException
	{
    int bytesRead = 0;
    byte[] buf = new byte[contentLength];
    int thisRead = 0;

    while (bytesRead < contentLength)
    {
    	thisRead = is.read(buf,
                           bytesRead,
                           ((bytesRead + 512) < contentLength)
                                   ? 512
                                   : contentLength - bytesRead);

      if (thisRead > 0)
        bytesRead += thisRead;
        
      //System.out.println("Read Content  " + bytesRead);
    }
    
    // If we are a client-side input tunnel then we close
    // the socket once all the input has been read.  This
    // acts as a signal to the server that we are done
    // so the server can close its end.  This appears
    // to be necessary as closing the server end can cause
    // problems for the client while reading the content.
    if (isClientInput())
      closeSocket();
      
    bufferingStream_ = new ByteArrayInputStream
                            ((cipher_ == null) ? buf
                                               : cipher_.decrypt(buf));

    return new ResolvingInputStream(bufferingStream_,
                                    Globals.channelInputReplacements__);
	}

	// Check if this tunnel can do input on the client side
  private boolean isClientInput()
  {
    return ((httpCommand_ == POST && sessionId_ == null) ||
            (httpCommand_ == GET  && sessionId_ != null));
  }
  
  private void setupHttpCommand()
  {
    if (httpCommand_ != null)
    {
    	String path = (cipher_ == null) ? inq__.toString()
                                      : inqs__.toString();
    	  
      httpCommandLine_ = httpCommand_ +
                         "http://" +
                         url_.getHost() +
                         ((port_ != defaultPort__) ? (":" + port_)
                                                  : ("")) +
                         path +
                         ((getSessionId() != null)
                           ? ("?" + id__.toString() + "=" + getSessionId())
                           : "") +
                         " HTTP/" + protocolLevel_ + "\r\n";
    }
	}
	
 	private void initProxy()
	{
		//String proxySet;
		
		//proxySet = System.getProperty(proxySet__);
		//if (proxySet == null)
		  //proxySet = System.getProperty(httpProxySet__);
		
		//System.out.println ("check proxy 3");
		Map m = NetUtil.getProxyServer();
		//System.out.println("check proxy 3.1 " + m);
		if (NetUtil.useProxy(m))
		{
      //System.out.println ("check proxy 4");
			usingProxy_ = true;
      proxyHost_ = NetUtil.getProxyHost(m);
      proxyPort_ = NetUtil.getProxyPort(m);

      // Se the system properties if we are using a proxy, so that
      // ordinary http:// references work for things like icons.
      //System.getProperties().put(proxySet__, "true");
      //System.getProperties().put(proxyHost__, proxyHost_);
      //System.getProperties().put(proxyPort__, String.valueOf(proxyPort_));

      System.setProperty(proxySet__,     "true");
      System.setProperty(httpProxySet__, "true");
      System.setProperty(httpProxyHost__, proxyHost_);
      System.setProperty(httpProxyPort__, String.valueOf(proxyPort_)); 		}
	}

	private class ExposedByteArrayOutputStream extends ByteArrayOutputStream
	{
		private ExposedByteArrayOutputStream()
		{
			super();
		}
		
		private ExposedByteArrayOutputStream(int initSize)
		{
			super(initSize);
		}
		
		private byte[] getBuf()
		{
			return buf;
		}
	}
}
