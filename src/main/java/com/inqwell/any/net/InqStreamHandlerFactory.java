/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/InqStreamHandlerFactory.java $
 * $Author: sanderst $
 * $Revision: 1.8 $
 * $Date: 2011-04-09 18:16:21 $
 */
 

package com.inqwell.any.net;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import com.inqwell.any.jms.MqStreamHandler;
import com.inqwell.any.print.PrintServiceStreamHandler;

/**
 * Implement the <code>URLStreamHandlerFactory</code> interface
 * for <code>speakinq://</code> style URLs and for the various
 * other protocols that Inq supports.
 * <p>
 * URLs of the style <code>speakinq://host:port</code> and others
 * are handled by this derivation of <code>java.net.URLStreamHandler</code>.
 */
public class InqStreamHandlerFactory implements URLStreamHandlerFactory
{
  private static URLStreamHandlerFactory factory__;
  
  private URLStreamHandlerFactory delegate_;
  
  /**
   * Install the Inq stream handler factory. This factory
   * returns {@link URLStreamHandler} implementations for
   * all the protocol types supported by the Inq language.
   * <p/>
   * When installing this factory efforts are made to
   * verify whether another factory has already been
   * put in place. If so, this factory takes priority
   * and any existing one is called for unknown protocols.
   */
  public static void install()
  {
    synchronized(InqStreamHandlerFactory.class)
    {
      if (factory__ == null)
      try
      {
        for (final Field field : URL.class.getDeclaredFields())
        {
          if ("factory".equalsIgnoreCase(field.getName()))
          {
            field.setAccessible(true);
            field.set(null,
                      factory__ = new InqStreamHandlerFactory((URLStreamHandlerFactory)field.get(null)));
          }
        }
      }
      catch (Throwable e)
      {
        // In the face of security exceptions just try to install
        // directly.
        URL.setURLStreamHandlerFactory(factory__ = new InqStreamHandlerFactory(null));
      }
    }
  }
  
//  public static URLStreamHandler getStreamHandler(String protocol)
//  {
//    return factory__.createURLStreamHandler(protocol);
//  }
  
  public static boolean isKnownProtocol(String protocol, String url)
  {
    // Check if its one of ours. This is repetition but just to avoid
    // creating a stream handler
    if (protocol.equals("speakinq"))
      return true;
    else if (protocol.equals("speakinqs"))
      return true;
    else if (protocol.equals("httpinq"))
      return true;
    else if (protocol.equals("httpinqs"))
      return true;
    else if (protocol.equals("printer"))
      return true;
    else if (protocol.equals("socket"))
      return true;
    else if (protocol.equals("bytearray"))
      return true;
    else if (protocol.equals("string"))
      return true;
    else if (protocol.equals("classpath") || protocol.equals("cp"))
      return true;
    else if (protocol.equals("stdin"))
      return true;
    else if (protocol.equals("stream"))
      return true;
    else if (protocol.equals("pipe"))
      return true;
    else if (protocol.equals("mq"))
      return true;
    
    // try creating a url
    try
    {
      new URL(url);
    }
    catch(MalformedURLException e)
    {
      return false;
    }
    return true;
  }
  
  public InqStreamHandlerFactory(URLStreamHandlerFactory delegate)
  {
    delegate_ = delegate;
  }
  
	public URLStreamHandler createURLStreamHandler(String protocol)
	{
		URLStreamHandler ret = null;
		
		if (protocol.equals("speakinq"))
			ret = new SpeakInqStreamHandler();
		else if (protocol.equals("speakinqs"))
			ret = new SpeakInqsStreamHandler();
	  else if (protocol.equals("httpinq"))
	    ret = new HttpInqStreamHandler();
	  else if (protocol.equals("httpinqs"))
	    ret = new HttpInqsStreamHandler();
	  else if (protocol.equals("printer"))
	    ret = new PrintServiceStreamHandler();
	  else if (protocol.equals("socket"))
	    ret = new PlainSocketStreamHandler();
	  else if (protocol.equals("bytearray"))
	    ret = new ByteArrayStreamHandler();
	  else if (protocol.equals("string"))
	    ret = new StringStreamHandler();
	  else if (protocol.equals("classpath") || protocol.equals("cp"))
	    ret = new ClassPathStreamHandler();
	  else if (protocol.equals("stdin"))
	    ret = new StdinStreamHandler();
	  else if (protocol.equals("stream"))
	    ret = new StreamStreamHandler();
	  else if (protocol.equals("pipe"))
	    ret = new PipedStreamHandler();
    else if (protocol.equals("mq"))
      ret = new MqStreamHandler();
		
		// If the protocol is not supported try any delegate.
		// Note - we may want to make the order in which the
		// factories are used configurable.
		if (ret == null && delegate_ != null)
		  ret = delegate_.createURLStreamHandler(protocol);
		  
		return ret;
	}
}
