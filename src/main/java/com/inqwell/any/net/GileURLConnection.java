/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.RuntimeContainedException;

/**
 * Implement the <code>URLConnection</code> sub-class
 * for <code>gile://</code> style URLs.
 * <p>
 * URLs of the style <code>gile:///</code>
 * are handled by this derivation of <code>java.net.URLConnection</code>.
 * http://www.macs.hw.ac.uk/~ml355/lore/pkencryption.htm
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

	static private class FileDecryption
	{

		public static final int	AES_Key_Size = 128;

		/**
		 * String to hold name of the public key file.
		 */
		public static final String PUBLIC_KEY_FILE	= System.getProperty("inq_rsa.pub");

		/**
		 * String to hold name of the AES key file.
		 */
		public static final String AES_KEY_FILE	= PUBLIC_KEY_FILE + ".aes"; // System.getProperty("inq.aes");

		private Cipher									pkCipher, aesCipher;
		private byte[]									aesKey;
		private SecretKeySpec						aeskeySpec;

		/**
		 * Constructor: creates ciphers
		 */
		public FileDecryption()
		{
			if (PUBLIC_KEY_FILE == null || AES_KEY_FILE == null)
				throw new AnyRuntimeException("No public key specified");
			
			try
			{
				// create RSA public key cipher
  			pkCipher = Cipher.getInstance("RSA");
  			// create AES shared key cipher
  			aesCipher = Cipher.getInstance("AES");
			}
			catch (GeneralSecurityException e)
			{
				throw new RuntimeContainedException(e);
			}
		}

		/**
		 * Decrypts an AES key from a file using an RSA public key
		 */
		public void loadKey(File in, File publicKeyFile)
				throws GeneralSecurityException, IOException
		{
			// read private key to be used to decrypt the AES key
			byte[] encodedKey = new byte[(int) publicKeyFile.length()];
			FileInputStream fis = new FileInputStream(publicKeyFile);
			fis.read(encodedKey);
			fis.close();

			// create public key
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey pk = kf.generatePublic(publicKeySpec);

			// read AES key
			pkCipher.init(Cipher.DECRYPT_MODE, pk);
			aesKey = new byte[AES_Key_Size / 8];
			CipherInputStream is = new CipherInputStream(new FileInputStream(in),
					pkCipher);
			is.read(aesKey);
			aeskeySpec = new SecretKeySpec(aesKey, "AES");
		}

		/**
		 * Given a stream, returns a stream that will decrypt the input source.
		 */
		public InputStream decrypt(InputStream cipherTextStream) throws IOException
		{
			try
			{
  			loadKey(new File(AES_KEY_FILE), new File(PUBLIC_KEY_FILE));
  			
  			aesCipher.init(Cipher.DECRYPT_MODE, aeskeySpec);
  
  			CipherInputStream is = new CipherInputStream(cipherTextStream, aesCipher);
  			
  			return is;
			}
			catch (InvalidKeyException e)
			{
				throw new RuntimeContainedException(e);
			}
			catch (GeneralSecurityException e)
			{
				throw new RuntimeContainedException(e);
			}
		}

		/**
		 * Encrypts and then copies the contents of a given file.
		 */
		public OutputStream encrypt(OutputStream plainTextStream) throws IOException
		{
			try
			{
  			loadKey(new File(AES_KEY_FILE), new File(PUBLIC_KEY_FILE));
  
  			aesCipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
  
  			CipherOutputStream os = new CipherOutputStream(plainTextStream, aesCipher);
  			
  			return os;
			}
			catch (InvalidKeyException e)
			{
				throw new RuntimeContainedException(e);
			}
			catch (GeneralSecurityException e)
			{
				throw new RuntimeContainedException(e);
			}
		}

	}
}
