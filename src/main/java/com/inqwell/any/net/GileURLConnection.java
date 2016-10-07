/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.inqwell.any.io.AbstractStream;
import com.inqwell.any.io.AbstractStream.FileDecryption;

/**
 * Implement the <code>URLConnection</code> sub-class
 * for <code>gile://</code> style URLs.
 * <p>
 * URLs of the style <code>gile:///</code>
 * are handled by this derivation of <code>java.net.URLConnection</code>.
 * http://www.macs.hw.ac.uk/~ml355/lore/pkencryption.htm
 * <p>
 * This connection type tries to read files either as plain text
 * or cipher text depending on what is found. Hence it is really
 * for source ascii files that may, or may not, be encrypted.
 */
public class GileURLConnection extends URLConnection
{
	private URLConnection   connection_;
	
	public GileURLConnection(URL url)
	{
		super(url);
	}

	public InputStream getInputStream() throws IOException
	{
    connect();
    
    if (connection_ == null)
      return null;
    
    // Try to decrypt. If successful open the stream again. Else
    // open a plain one again.
    boolean encrypted = true;
    byte[] b = new byte[32];
    InputStream is = connection_.getInputStream();
    FileDecryption fd = new FileDecryption();
    is = fd.decrypt(is);
    try
    {
      is.read(b);
      is.close();
    }
    catch (Exception e)
    {
    	is.close();
    	encrypted = false;
    }
    finally
    {
    	// Whatever happened, we'll reopen anyway
    	this.connected = false;
    	connection_ = null;
    }

    // Even if the decryption didn't throw we might still have read cipher text.
    // Check if the sample bytes read are all ascii. Not sure there's another
    // way without explicitly knowing the file was encrypted.
    encrypted = isAscii(b);
    
    connect();
    is = connection_.getInputStream();
    if (encrypted)
      is = fd.decrypt(is);
    
		return is;
	}
	
	public OutputStream getOutputStream() throws IOException
	{
    connect();

    if (connection_ == null)
      return null;

    FileDecryption fd = new FileDecryption();

    // gile:// protocol always encrypts
		return fd.encrypt(connection_.getOutputStream());
	}
	
	public OutputStream getEncryptingOutputStream(OutputStream os) throws IOException
	{

    FileDecryption fd = new FileDecryption();

    // gile:// protocol always encrypts
		return fd.encrypt(os);
	}
	
	public void connect() throws IOException
	{
    if (!this.connected)
    {
      URL u = getURL();
      connection_ = u.openConnection();
      this.connected = true;
    }
	}
	
	private boolean isAscii(byte[] buf)
	{
		for (byte b : buf)
			if (b < 0 || b > 127) return false;
		return true;
	}
}
