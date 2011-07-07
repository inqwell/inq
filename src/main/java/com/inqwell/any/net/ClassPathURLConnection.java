/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/ClassPathURLConnection.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
 

package com.inqwell.any.net;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.IOException;

import com.inqwell.any.AnyURL;

/**
 * Implement the <code>URLConnection</code> sub-class
 * for <code>socket://</code> style URLs.
 * <p>
 * URLs of the style <code>classpath:///</code> are handled by
 * this derivation of <code>java.net.URLConnection</code>.
 */
public class ClassPathURLConnection extends URLConnection
{
	private URLConnection   connection_;
	
	public ClassPathURLConnection(URL url)
	{
		super(url);
	}

	public InputStream getInputStream() throws IOException
	{
    connect();
    
    if (connection_ == null)
      return null;
      
		return connection_.getInputStream();
	}
	
	public OutputStream getOutputStream() throws IOException
	{
    connect();

    if (connection_ == null)
      return null;
      
		return connection_.getOutputStream();
	}
	
	public void connect() throws IOException
	{
    if (!this.connected)
    {
      ClassLoader cl = getClass().getClassLoader();
      String path = getURL().getPath();
      
      // if path starts with / then remove it
      if (path.startsWith("/"))
        path = path.substring(1);
        
      URL u = cl.getResource(path);
      u = AnyURL.fixJarURL(u);
//System.out.println("Connecting " + getURL().getPath());
//System.out.println("ClassLoader " + cl);
//System.out.println("URL " + u);
      if (u != null)
        connection_ = u.openConnection();
      this.connected = true;
    }
	}
}
