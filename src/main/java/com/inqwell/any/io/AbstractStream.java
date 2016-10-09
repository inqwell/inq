/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/AbstractStream.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-05-03 14:35:51 $
 */


package com.inqwell.any.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyURL;
import com.inqwell.any.ConstString;
import com.inqwell.any.ContainedException;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.PropertyAccessMap;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.io.AbstractStream.FileDecryption;
import com.inqwell.any.net.GileURLConnection;
import com.inqwell.any.net.PipedURLConnection;
import com.inqwell.any.net.PlainSocketURLConnection;
import com.inqwell.any.net.StreamURLConnection;
import com.inqwell.any.net.StringURLConnection;

/**
 * Provides utility functionality for sub-classes which implement
 * the <code>PhysicalIO</code> interface for I/O to streams.
 */

public abstract class AbstractStream extends    PropertyAccessMap
																		 implements PhysicalIO,
																								Cloneable
{
	protected transient InputStream  istream_;
	protected transient OutputStream ostream_;

  private   AnyURL        openedURL_;
  
  private   URLConnection urlc_;

  private transient Map  propertyMap_;
  
  private boolean isCipher_;

  private static    Any  tiedStream__ = new ConstString("tiedStream__");

	public void setAuxInfo (Any a, Any subs) {}

  public Object clone() throws CloneNotSupportedException
  {
		AbstractStream s = (AbstractStream)super.clone();
		s.istream_     = null;
		s.ostream_     = null;
    s.openedURL_   = null;
    s.propertyMap_ = null;
		return s;
  }

	public void close()
	{
		// If open for both input and output then we are a pipe://
    // In this case just close the write side, on the assumption that
    // the writer closes the pipe and the reader will do so some time
    // later when it sees eof.
    boolean isPipe = ostream_ != null && istream_ != null;
    
		if (ostream_ != null)
    {
      boolean closed = true;
		  try
		  {
        closed = doCloseWrite();
      }
      finally
      {
        propertyMap_ = null;
        if (closed)
        {
          try
          {
    			  ostream_.close();
          }
          catch(Exception e)
          {
            throw new RuntimeContainedException(e);
          }
          finally
          {
            ostream_ = null;
          }
        }
      }
    }

		if (istream_ != null && !isPipe)
    {
      boolean closed = true;
      try
      {
        closed = doCloseRead();
      }
      finally
      {
        if (closed)
        {
          try
          {
            istream_.close();
          }
          catch(Exception e)
          {
            throw new RuntimeContainedException(e);
          }
          finally
          {
            istream_ = null;
          }
        }
      }
    }

		if (propertyMap_ != null)
    {
      AbstractStream s = (AbstractStream)propertyMap_.get(tiedStream__);
      if (s != null)
        s.close();
    }
	}
	
	public void flush()
	{
    if (propertyMap_ != null)
    {
      AbstractStream s = (AbstractStream)propertyMap_.get(tiedStream__);
      if (s != null)
        s.flush();
    }
    doFlush();
	}

  public void setStreams(InputStream is, OutputStream os) throws AnyException
  {
    istream_ = is;
    ostream_ = os;
    acceptStreams();
  }

  public void setTiedStream(Any s)
  {
    // We don't want the overhead of storing a stream in
    // a class as commonly used as this one, so store it
    // in the property map instead.
    if (propertyMap_ == null)
      propertyMap_ = makePropertyMap();
    
    if (AnyNull.isNull(s))
    {
      propertyMap_.replaceItem(tiedStream__, null);
    }
    else
    {
      propertyMap_.replaceItem(tiedStream__, s);
    }
  }
  
  public AnyURL getURL()
  {
    return openedURL_;
  }

  public InputStream getUnderlyingInputStream()
  {
    return istream_;
  }

  public OutputStream getUnderlyingOutputStream()
  {
    return ostream_;
  }
  
  public boolean isOpenWrite()
  {
    return ostream_ != null;
  }
  
  public boolean isOpenRead()
  {
    return istream_ != null;
  }
  
  /**
   * Override.  If the key is <code>"properties"</code> then
   * return true.
   */
  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;
      
    return false;
  }

  /**
   * Override.  If the key is <code>"properties"</code> then (make and)
   * return a property binding object.
   */
  public Any get (Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }
        
      return propertyMap_;
    }
    else
    {
      handleNotExist(key); // throws
      return null;
    }
  }

  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      return null;
    }
  }

  public Any copyFrom (Any a)
  {
    // Do any derived processing first - if that fails there's no
    // point in us doing anything and we know cast must succeed.
    doCopyFrom(a);

    //AbstractStream as = (AbstractStream)a;
    this.istream_   = null;
    this.ostream_   = null;
    this.openedURL_ = null;

    return this;
  }
  
  // Properties
  
  public Any getAvailable()
  {
    if (isOpenRead())
    {
      try
      {
        return new AnyInt(istream_.available());
      }
      catch (IOException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
    else
    {
      notOpenedRead();
      return null;
    }
  }
  
  public void setSoTimeout(int timeout)
  {
    if (isOpenRead())
    {
      if (urlc_ instanceof PlainSocketURLConnection)
      {
        ((PlainSocketURLConnection)urlc_).setSoTimeout(timeout);
      }
      else
        throw new AnyRuntimeException("Not a socket");
    }
    else
      notOpenedRead();
  }
  
  public void setCipherText(boolean encrypt)
  {
  	isCipher_ = encrypt;
  }

  protected void doCopyFrom(Any a)
  {
  	// Can we ever copy a stream? What does it mean?
  	throw new IllegalArgumentException("Cannot copy streams");
  }

	protected void finalize() throws Throwable
	{
		// If we get finalized then do our best close to release resources
		close();
	}
  
  protected void attachStreams(AbstractStream stream) throws AnyException
  {
    setStreams(stream.istream_, stream.ostream_);
  }

  protected abstract void acceptStreams() throws AnyException;
  
  protected abstract boolean doCloseRead();

  protected abstract boolean doCloseWrite();
  
  protected abstract void doFlush();

  protected void notOpenedWrite()
  {
    throw new UnsupportedOperationException("Not opened for writing");
  }

  protected void notOpenedRead()
  {
    throw new UnsupportedOperationException("Not opened for reading");
  }

  /**
   * Make streams to the underlying source represented by the URL.
   * Where supported, streams will be created according to the
   * supplied mode.
   * <p>
   * The streams created only make the connection to the underlying
   * source/sink and are not characterised with any particular
   * format, such as native serialised form or text line-based.
   * That function is performed by derived implementations
   * of <code>acceptStreams</code>, which can be called after
   * this method has executed successfully.
   * <p>
   * If the supplied URL, <code>toOpen</code>, is relative then
   * it is based on the file:// protocol, assuming the user's
   * current working directory.
   */
	protected final boolean makeStreams(Process p, Any toOpen, IntI mode) throws AnyException
	{
    String cwd = System.getProperties().getProperty("user.dir");
    String fs  = System.getProperties().getProperty("file.separator");

    AnyURL    baseUrl = new AnyURL("file:///" + cwd + fs + "dummy");
    AnyURL    openUrl = new AnyURL(toOpen);

		boolean ret = true;

		try
		{
  		URL url = openUrl.getURL(baseUrl);

		  // URL looks OK - try to process it

			URLConnection uc = url.openConnection();

      openedURL_ = openUrl;
      urlc_      = uc;

			if ((mode.equals(PhysicalIO.write__)) ||
					(mode.equals(PhysicalIO.append__)))
			{
				// Not every type of URL can support append (or is it meaningful to
				// do so). Currently, only files support append and because we
				// cannot get at the underlying stream handler class we undertake
				// to create the output stream in this case
				String protocol = url.getProtocol();
				if (protocol.equals("file:") && mode.equals(PhysicalIO.append__))
				{
          String filePath = url.getPath();
          ostream_ = new FileOutputStream(filePath, true);
				}
				else
				{
          OutputStream ostream = getOutputStream(uc, mode);
          close();
          ostream_ = ostream;
          
          // If its a piped stream then we must get the input one at
          // the same time.
          if ((uc instanceof PipedURLConnection) ||
              (uc instanceof PlainSocketURLConnection) ||
              (uc instanceof StreamURLConnection))
          {
            istream_ = getInputStream(uc);

            if (isCipher_)
            {
              FileDecryption fd = new FileDecryption();
              istream_ = fd.decrypt(istream_);
            }
            
            if (uc instanceof StreamURLConnection)
            {
              this.setTiedStream(((StreamURLConnection)uc).getTiedStream());
            }
          }
        }
        
        if (isCipher_)
        {
          FileDecryption fd = new FileDecryption();
          ostream_ = fd.encrypt(ostream_);
        }
			}
			else
			{
				InputStream istream = getInputStream(uc);
        close();
        istream_ = istream;
        
        if (isCipher_)
        {
          FileDecryption fd = new FileDecryption();
          istream_ = fd.decrypt(istream_);
        }

        // If its a piped or socket stream then get the input one at
        // the same time.
        if ((uc instanceof PipedURLConnection) ||
            (uc instanceof PlainSocketURLConnection) ||
            (uc instanceof StreamURLConnection))
        {
          ostream_ = getOutputStream(uc, mode);
          
          if (isCipher_)
          {
            FileDecryption fd = new FileDecryption();
            ostream_ = fd.encrypt(ostream_);
          }
          
          if (uc instanceof StreamURLConnection)
          {
            this.setTiedStream(((StreamURLConnection)uc).getTiedStream());
          }
        }
			}
		}
		catch (MalformedURLException mue)
		{
			throw new ContainedException(mue);
		}
		catch (IOException ioex)
		{
		  openedURL_ = null;
		  urlc_      = null;
		  
			ret = false;
		}
		return ret;
	}

	protected boolean isPipe()
	{
    if ((urlc_ instanceof PipedURLConnection) ||
        (urlc_ instanceof PlainSocketURLConnection))
      return true;
    
    if (propertyMap_ != null)
    {
      AbstractStream s = (AbstractStream)propertyMap_.get(tiedStream__);
      if (s != null)
        return s.isPipe();
    }
    
    return false;
	}
	
  public Iter createIterator () { return DegenerateIter.i__; }

  public boolean isEmpty() { return false; }

  /**
   *  Make sure there is a filter__ child, because of PropertyMap.contains()
   *  always returning true.
   */
  protected Map makePropertyMap()
  {
    Map m = super.makePropertyMap();
    m.add(tiedStream__, null);
    return m;
  }
  
  protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}

	private OutputStream getOutputStream(URLConnection uc,
																			 IntI          mode) throws AnyException
	{
		OutputStream ret = null;

		try
		{
		  if (uc instanceof StringURLConnection)
		  {
		    StringURLConnection struc = (StringURLConnection)uc;
		    struc.setAppend(mode.equals(PhysicalIO.append__));
		    ret = struc.getOutputStream();
	    }
	    else
			  ret = uc.getOutputStream();
		}
		catch (UnknownServiceException usex)
		{
			// In fact, the file: protocol doesn't support output streams
			// so we have to handle this case ourselves.
			ret = getFileOutputStream(uc, mode);
		}
		catch (Exception e)
		{
			throw new ContainedException(e);
		}

		return ret;
	}

	private OutputStream getFileOutputStream(URLConnection uc,
																					 IntI          mode) throws AnyException
	{
		OutputStream ret = null;

		String protocol = uc.getURL().getProtocol();

		//System.out.println ("AbstractStream.getFileOutputStream() protocol: " + protocol);

		if (!protocol.equals("file"))
		{
			throw new AnyIOException("Unsupported protocol for output");
		}

		String path = uc.getURL().getPath();

		try
		{
			ret = new FileOutputStream(path, mode.equals(PhysicalIO.append__));
			if (uc instanceof GileURLConnection)
				ret = ((GileURLConnection)uc).getEncryptingOutputStream(ret);
		}
		catch(Exception e)
		{
			throw new ContainedException(e);
		}

		return ret;
	}

	private InputStream getInputStream(URLConnection uc) throws IOException
	{
    return uc.getInputStream();
	}

	static public class FileDecryption
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
			is.close();
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
