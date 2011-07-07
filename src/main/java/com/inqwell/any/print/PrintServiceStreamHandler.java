/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/print/PrintServiceStreamHandler.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.print;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.io.IOException;
import java.net.Socket;

/**
 * Implement the <code>URLStreamHandler</code> sub-class
 * for <code>printer://</code> style URLs.
 * <p>
 * URLs of the
 * style <code>printer://host:[port]/file?arg1=val&arg2=val</code> are
 * handled by this derivation of <code>java.net.URLStreamHandler</code>.  Such
 * a URL can be used to send data to a printer.
 * <p>
 * The host, port and file parts of the URL are not presently used. The arguments
 * are used to specify the various print job attributes as follows:
 */
public class PrintServiceStreamHandler extends URLStreamHandler
{
	public URLConnection openConnection(URL u) throws IOException
	{
		return new PrintServiceURLConnection(u);
	}

	/**
	 * The default port for the printer service is arbitrarily 1024.
	 */
	public int getDefaultPort()
	{
		return 1024;
	}
}
