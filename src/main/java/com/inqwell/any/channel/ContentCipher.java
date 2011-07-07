/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/ContentCipher.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.AnyException;
import com.inqwell.any.ContainedException;
import com.inqwell.any.RuntimeContainedException;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import com.sun.crypto.provider.SunJCE;
import java.io.IOException;

/**
 * A class that encrypts or decrypts the Http content.
 *
 * This class uses a pass-phrase based cipher engine (PKCS #5) that makes
 * use of MD5 and 56bit (or more) DES algorithms with CBC (Cipher Block 
 * Chaining Mode) and PKCS5Padding.
 *
 * @author Tim Holbrook
 */
public final class ContentCipher extends AbstractAny
{

	/////////////////////// Data //////////////////////////

	// Static data
	private static PBEParameterSpec paramSpec = null;
	private static SecretKeyFactory keyFac    = null;

	// Some random 8 byte seed
	private static final byte[] salt = {
				   (byte)0x3B, (byte)0x26, (byte)0xAC, (byte)0xE7,
				   (byte)0xEF, (byte)0x2C, (byte)0x7C, (byte)0x14};

  private static final String PASS_PHRASE = 
        "ContentCipher Created: Mar-2002 E8gf721723khGsad8KJKJS87787Vvhgfs";

  // Make it a bit harder to figure out the default pass-phrase!
  private static final String passPhrase = 
        new StringBuffer(PASS_PHRASE).reverse().toString();

  private static final String CIPHER_MODE = "PBEWithMD5AndDES";

	// Instance data
	private SecretKey key      = null;
	private Cipher    enCipher = null;
	private Cipher    deCipher = null;

	static
	{
		// Initialisation (first JVM use) of the cipher 
		// engine can take 1-2 secs on a PIII-500 due to
		// JCE seeding it's internal SecureRandom class.		
		try
		{
			ContentCipher tmp = new ContentCipher();
			byte[] x = tmp.encrypt("Hi!".getBytes());
		}
		catch(Exception e)
		{
			System.err.println("[ContentCipher/static-init] exception: " + e);
		}
	}

  public static ContentCipher makeCipher() throws RuntimeContainedException
  {
  	ContentCipher cipher = null;
    try
    {
      cipher = new ContentCipher();
    }
    catch(IOException e)
    {
      throw new RuntimeContainedException(e);
    }
    return cipher;
  }
  
	///////////////////////////////////////////////////////

    /**
     * Creates a ContentCipher object.
	 */
	public ContentCipher() throws IOException {

		try {
	        if (keyFac == null)
	            initKeyFactory();
	        
	        // Generate the secret key. Add a 'spike' to the pass-phrase
	        // to help prevent decryption via plain vanilla JCE should 
	        // someone obtain the passed-in passPhrase.
	        key = keyFac.generateSecret(
	                new PBEKeySpec((passPhrase + "h05ErU421y").toCharArray()));    	
		}
		catch(Exception e) {
			throw new IOException("[ContentCipher] " + e);
		}
	}
	
	/**
     * Decrypts the supplied byte array.
	 * @param b  the data to decrypt. May not be null.
	 * @return  the decrypted byte array.
     * @exception NullPointerException  if <tt>b</tt> is null.
	 * @exception IOException  the cipher engine failed to decrypt the 
     * byte data for some reason.
	 */
	public byte[] decrypt (byte[] b) throws IOException {

        return decrypt(b, 0, b.length);
	}

	/**
     * Decrypts the supplied byte array.
	 * @param b  the data to decrypt.  May not be null.
     * @param off  the start offset of the data from within <tt>b</tt>.
     * @param len  the maximum number of bytes read from <tt>b</tt>.
     * @exception NullPointerException  if <tt>b</tt> is null.
     * @exception IndexOutOfBoundsException if the <tt>off</tt> and 
     * <tt>len</tt> values are out of bounds.
	 * @exception IOException  the cipher engine failed to decrypt the 
     * byte data for some reason.
	 * @return  the decrypted byte array.
	 */
	public byte[] decrypt (byte[] b, int off, int len) throws IOException {
        
        // Validate args
        if (b == null) {
            throw new NullPointerException();
        } 
        else if ((off < 0) || (off > b.length) || (len < 0) ||
               ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        
        try {
	        return getDeCipher().doFinal(b, off, len);
		}
		catch(Exception e) {
			throw new IOException("[decrypt] " + e);
		}
	}

    /**
     * Encrypts the supplied byte array.
	 * @param b  the byte array to encrypt. May not be null.
     * @exception NullPointerException  if <tt>b</tt> is null.
	 * @exception IOException  the cipher engine failed to encrypt the 
     * byte data for some reason.
	 * @return  the encrypted byte array.
	 */
	public byte[] encrypt (byte[] b) throws IOException {

        return encrypt(b, 0, b.length);
	}

    /**
     * Encrypts the supplied byte array.
	 * @param b  the byte array to encrypt. May not be null.
     * @param off  the start offset of the data from within <tt>b</tt>.
     * @param len  the maximum number of bytes read from <tt>b</tt>.
     * @exception NullPointerException  if <tt>b</tt> is null.
     * @exception IndexOutOfBoundsException if the <tt>off</tt> and 
     * <tt>len</tt> values are out of bounds.
	 * @exception IOException  the cipher engine failed to encrypt the 
     * byte data for some reason.
	 * @return  the encrypted byte array.
	 */
	public byte[] encrypt (byte[] b, int off, int len) throws IOException {

		try {
	        // Validate args
	        if (b == null) {
	            throw new NullPointerException();
	        } 
	        else if ((off < 0) || (off > b.length) || (len < 0) ||
	               ((off + len) > b.length) || ((off + len) < 0)) {
	            throw new IndexOutOfBoundsException();
	        }
	
	        return getEnCipher().doFinal(b, off, len);
		}
		catch(Exception e) {
			throw new IOException("[encrypt] " + e);
		}
	}

    /**
     * A one-time initialisation during the course of the current JVM runtime.
     */
    private static synchronized void initKeyFactory() {

        if (keyFac == null) { // Double-check pattern
            try {
                // Dynamically assign the security provider.
                Security.addProvider(new com.sun.crypto.provider.SunJCE());

                // One time initialisation.
                paramSpec = new PBEParameterSpec(salt, 12);
                keyFac    = SecretKeyFactory.getInstance(CIPHER_MODE);
            }
            catch (Exception e) {
                System.err.println("[ContentCipher/initKeyFactory] " +
                        "Java/JCE init exception: " + e);
            }
        }
    }

	/**
	 * @return the decryption cipher engine.
	 * @exception Exception
	 */
	private Cipher getDeCipher () throws Exception {

        if (deCipher == null)
            initDeCipher();

        return deCipher;
	}

    /**
     * Initialises the Decryption Cipher Engine.
	 * @exception Exception
     */
    private synchronized void initDeCipher() throws Exception {

        if (deCipher == null) { // Double-check pattern

            deCipher = Cipher.getInstance(CIPHER_MODE);

            // Initialise cipher with the key
            deCipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        }
    }

    /**
	* @return  The encryption cipher.
	* @exception Exception
	*/
	private Cipher getEnCipher () throws Exception {

        if (enCipher == null)
            initEnCipher();

        return enCipher;
	}

    /**
     * Initialises the Encryption Cipher Engine.
	 * @exception Exception
     */
    private synchronized void initEnCipher() throws Exception {

        if (enCipher == null) { // Double-check pattern

            enCipher  = Cipher.getInstance(CIPHER_MODE);

            // Initialise cipher with the key
            enCipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
        }
    }
}
