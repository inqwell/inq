/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
 

package com.inqwell.any.jms;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Implement the <code>URLStreamHandler</code> sub-class
 * for <code>mq://</code> style URLs.
 * <p>
 * URLs of the style <code>mq://</code> are handled by
 * this derivation of <code>java.net.URLStreamHandler</code>.  Such
 * a URL can be used to open streams to bytes and stream messages.
 */
public class MqStreamHandler extends URLStreamHandler
{
  public URLConnection openConnection(URL u) throws IOException
  {
    return new MqURLConnection(u);
  }

  /**
   * Not relevant.
   */
  public int getDefaultPort()
  {
    return -1;
  }
}
