/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/SpeakInqsURLConnection.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */


package com.inqwell.any.net;

import java.net.URL;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.security.KeyStoreException;
import com.inqwell.any.Any;
import com.inqwell.any.Map;
import com.inqwell.any.Globals;
import com.inqwell.any.BooleanI;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.StartProcessEvent;
import com.inqwell.any.EventConstants;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.io.ResolvingInputStream;
import com.inqwell.any.io.ReplacingOutputStream;
import com.inqwell.any.channel.ChannelDriver;
import com.inqwell.any.channel.Serialize;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.Iterator;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

/**
 * Implement the <code>URLConnection</code> sub-class
 * for <code>speakinq://</code> style URLs.
 * <p>
 * URLs of the style <code>speakinq://host:port</code> are handled by
 * this derivation of <code>java.net.URLConnection</code>.  Such
 * a URL can be used to support interfacing between
 * an <code>&lt;inq&gt;</code><sup><font size=-2>TM</font></sup> environment
 * and external systems.
 */
public class SpeakInqsURLConnection extends InqURLConnection
{
  // Password for own keystore
  private static final char[] keyStorePassword__ = new String("inqwell").toCharArray();

  // Path to own keystore. Store it into the home directory to
  // avoid permission problems.
  private static final String keyStorePath__ =
        System.getProperty("user.home") +
        System.getProperty("file.separator") +
                           ".inqkeystore";

	private Socket        socket_;

	private ChannelDriver channelDriver_;

	private int           port_;

  // If set is used to authenticate an incoming server
  // certificate that is not in the local KeyStore.
  // If the externalised certificate chain received
  // matches this one then it will be trusted and
  // written to the local KeyStore. It is then
  // cleared.
  // Also used to pass out the current certificate
  // chain we've chosen not to trust yet.
	private Any           cert_;

	public SpeakInqsURLConnection(URL url, int port)
	{
		super(url);
		port_ = port;
	}

	public InputStream getInputStream() throws IOException
	{
		return socket_.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException
	{
		return socket_.getOutputStream();
	}

  public void setTrusted(Any toTrust)
  {
    cert_ = toTrust;
  }

  public Any getTrusted()
  {
    Any ret = cert_;
    cert_ = null;
    return ret;
  }

  public Socket getSocket()
  {
    return socket_;
  }
  
	public void connect() throws IOException
	{
    if (!this.connected)
    {

      /*
      SSLSocketFactory sslFact =
        (SSLSocketFactory)SSLSocketFactory.getDefault();
      //SSLSocket s =
        //(SSLSocket)sslFact.createSocket(getURL().getHost(), port_);
      socket_ = sslFact.createSocket(getURL().getHost(), port_);

      //((SSLSocket)socket_).addHandshakeCompletedListener(new ClientHandShake());
      */

      SSLSocketFactory factory = null;
      try
      {
        KeyManager[]     km      = null;
        TrustManager[]   tm      = {new StoreCertTrustManager()};
        SSLContext sslContext    = SSLContext.getInstance("SSL","SunJSSE");
        sslContext.init(null, tm, new java.security.SecureRandom());
        factory = sslContext.getSocketFactory();
      }
      catch(Exception e)
      {
        throw new RuntimeContainedException(e);
      }

      socket_ = factory.createSocket(getURL().getHost(), port_);

      socket_.setTcpNoDelay(true);
      socket_.setKeepAlive(true);
      socket_.setTrafficClass(0x1c);
      this.connected = true;

      // if there is a session id then we are resuming a broken
      // connection, so send the session resume event.
      // TODO - is this ever used? Check and may be remove....
      if (getSessionId() != null)
      {
	      try
	      {
			    getWriteChannelDriver().write
			        (new StartProcessEvent(EventConstants.SESSION_RESUME,
			                               getSessionId()));
	      }
	      catch(AnyException e)
	      {
	      	throw new RuntimeContainedException(e);
	      }
      }
    }
	}

	public void disconnect() throws AnyException
	{
    if (!this.connected)
      return;

		this.connected = false;

    try
    {
      InputStream  is = socket_.getInputStream();
      OutputStream os = socket_.getOutputStream();
      is.close();
      os.close();
      socket_.close();
    }
    catch (Exception e)
    {
      AnyException.throwExternalException(e);
    }
	}

	public ChannelDriver getReadChannelDriver() throws AnyException
	{
    if (channelDriver_ != null)
      return channelDriver_;

    try
    {
      connect();

      ResolvingInputStream  ois = null;
      ReplacingOutputStream oos = null;

      // Create the o/p stream first to avoid deadlock
      // across the socket while the stream header is
      // written/read.
      try
      {
        //oos = new ReplacingOutputStream(getOutputStream(),
        //oos = new ReplacingOutputStream(new GZIPOutputStream(getOutputStream()),
        int bufsiz = 2 * socket_.getSendBufferSize();
        if (bufsiz < 0)
          bufsiz = 8192;
        oos = new ReplacingOutputStream(new ReplacingOutputStream.SupportingOutputStream(getOutputStream(), bufsiz),
        //oos = new ReplacingOutputStream(new BufferedOutputStream(getOutputStream(), bufsiz),
                                        Globals.channelOutputReplacements__);

        // Make sure the serialization header goes in spite of buffering
        oos.flush();
      }
      catch (Exception ooe)
      {
        disconnect();
        AnyException.throwExternalException (ooe);
      }

      try
      {
        //ois = new ResolvingInputStream(getInputStream(),
        //ois = new ResolvingInputStream(new GZIPInputStream(getInputStream()),
        int bufsiz = 2 * socket_.getReceiveBufferSize();
        ois = new ResolvingInputStream(new ResolvingInputStream.SupportingInputStream(getInputStream(), bufsiz),
                                       oos,
        //ois = new ResolvingInputStream(new BufferedInputStream(getInputStream(), bufsiz),
                                       Globals.channelInputReplacements__);
        //oos.setCompressed(true);
        //ois.readAny();  // consume compress response
      }
      catch (Exception oie)
      {
        disconnect();
        oos.close();
        AnyException.throwExternalException (oie);
      }

      channelDriver_ = new Serialize(ois, oos);

      return channelDriver_;
    }
    catch (Exception e)
    {
      AnyException.throwExternalException (e);
      return null;
    }
  }

	public ChannelDriver getWriteChannelDriver() throws AnyException
	{
    return getReadChannelDriver();
  }

	public boolean isUnreliable()
	{
		return false;
	}

	public boolean isPermanent()
	{
		return true;
	}

  private class StoreCertTrustManager implements X509TrustManager
  {
    // The trustmanager instance to delegate to for default behaviour
    private TrustManager tm_ = null;

    // The stream for reading from the keystore.
    FileInputStream keyStoreIStream = null;

    // The local KeyStore. This is loaded lazily on first use.
    private KeyStore keyStore_ = null;

    /**
     * Creates a TrustManager which first checks the default
     * behaviour of the X509TrustManager.
     * If the default behaviour throws a CertificateException ask
     * check if the certificate has been trusted in the local
     * KeyStore.
     * @throws AnyRuntimeException: If SSL - initialization failed.
     */
    private StoreCertTrustManager()
    {
      // Fetch the TrustManagerFactory. We use the SunJSSE provider
      // for this purpose.
      try
      {
        TrustManagerFactory tmf =
          TrustManagerFactory.getInstance("SunX509", "SunJSSE");

        tmf.init((KeyStore)null);

        //System.out.println("Number of TrustManagers is: " + tmf.getTrustManagers().length);
        tm_ = tmf.getTrustManagers()[0];
      }
      catch(GeneralSecurityException gex)
      {
        throw new RuntimeContainedException(gex);
      }

      /* Something failed we could not get a TrustManager instance.*/
      if(tm_ == null)
      {
        throw new AnyRuntimeException
          ("Could not get default TrustManager instance");
      }
    }

    /**
     * Authenticates a client certificate. This behaviour is not
     * modified - only implement the default behaviour.
     *
     * @param chain In: The certificate chain to be authenticated.
     * @param authType In: The key exchange algorithm.
     */
    public void checkClientTrusted(X509Certificate[] chain,
                                   String            authType) throws CertificateException
    {
      ((X509TrustManager)tm_).checkClientTrusted(chain, authType);
    }

    /**
     * Authenticates a server certificate. If the given certificate is untrusted ask
     * the user whether to proceed or not.
     *
     * @param chain In: The certificate chain to be authenticated.
     * @param authType In: The key exchange algorithm.
     */
    public void checkServerTrusted(X509Certificate[] chain,
                                   String            authType) throws CertificateException
    {
      /* Output the certifcate chain for debugging purposes */
      //System.out.println("got X509 certificate from server:");
//      for(int i = 0; i < chain.length; i++)
//      {
//        System.out.println("chain[" + i + "]: " + chain[i].getIssuerDN().getName());
//      }

      try
      {
        /* First try the default behaviour. */
        ((X509TrustManager)tm_).checkServerTrusted(chain, authType);
      }
      catch(CertificateException ce)
      {
        // If the default TrustManager throws then the certificate
        // is not trusted by it (likely not in cacerts or whatever
        // the TrustStore property has been set to.  See if we can
        // check the Inq KeyStore.  Note - if the certificate was
        // invalid because it expired, there is a possibility that
        // it gets copied into the local KeyStore if it is
        // accepted permanently.
//        System.out.println("in checkServerTrusted: authType: " +
//                           authType +
//                           ", got certificate exception: " +
//                           ce.getMessage());

        //ce.printStackTrace();

        // If the chain is flakey throw a runtime exception
        if(chain        == null ||
           chain.length == 0)
        {
          throw new AnyRuntimeException("Invalid certificate chain");
        }

        // If the keystore trusts this chain then all OK
        KeyStore keyStore = readLocalKeyStore(keyStorePath__);

        boolean trusted = true;
        try
        {
          for (int i = 0; i < chain.length; i++)
          {
            X509Certificate c = chain[i];
            String issuerDN = c.getIssuerDN().toString();

//            System.out.println("Checking " + issuerDN + " " +
//                               keyStore.isCertificateEntry(c.getIssuerDN().toString()) +
//                               " in keystore");

            // If its not in the local KeyStore don't trust it
            if (!keyStore.isCertificateEntry(issuerDN))
            {
              trusted = false;
              break;
            }

            // If its in the KeyStore but has expired since being
            // written there then remove it and don't trust it.
            // If subsequently accepted then its entry date into
            // the KeyStore will be later than the expiration date
            // and it will remain there until removed externally
            // say using keytool.  Clean up all expired certificates
            // in this way.
            Date now = new Date();
            if (c.getNotAfter().compareTo(now) < 0 &&
                keyStore.getCreationDate(issuerDN).compareTo(c.getNotAfter()) < 0)
            {
              trusted = false;
              keyStore.deleteEntry(issuerDN);
              writeLocalKeyStore(null, keyStore, keyStorePath__);
            }
          }
        }
        catch(KeyStoreException kse)
        {
          // We don't expect any errors here because we should, at worst,
          // have an empty KeyStore. However, if we get this exception
          // treat it as if the certificate chain was untrusted. Then, if
          // we subsequently authorise it and re-write the KeyStore, may be
          // things are left in a good state.
          throw new UntrustedCertificateException(chain,
                                                  kse,
                                                  "Untrusted because of KeyStore error");
        }

        if (!trusted)
        {
          // [Part of] the chain was not found in the local KeyStore.
          // Last chance is that there is a cert_ entry. This would have
          // been set if the connection attempt is being made a second time
          // after the user has accepted the certificate from the
          // original attempt.
          if (cert_ == null)
          {
            // Store the current chain so that a client can use it (for
            // example so that a user can manually accept it).
            cert_ = UntrustedCertificateException.externaliseX509Chain(chain);
            throw new UntrustedCertificateException(chain);
          }

          Any toTrust = cert_;
          cert_    = null;

          boolean trustAlways = trustAlways(toTrust);

          Any extChain = UntrustedCertificateException.externaliseX509Chain(chain);
          if (!extChain.equals(toTrust))
          {
            System.out.println(extChain);
            throw new AnyRuntimeException("Unexpected certificate chain", extChain);
          }

          // We chose to trust it - write to local KeyStore if permanent
          if (trustAlways)
          {
            writeLocalKeyStore(chain, keyStore, keyStorePath__);
            //System.out.println("Keystore saved in " + keyStorePath__);
          }
        }
      }
    }

    // Rewrite the given KeyStore to the specified path, adding
    // the chain if supplied.
    private void writeLocalKeyStore(X509Certificate[] chain,
                                    KeyStore          keyStore,
                                    String            keyStorePath)
    {
      // Add Chain to the keyStore.
      try
      {
        if (chain != null)
        {
	        for (int i = 0; i < chain.length; i++)
          {
            keyStore.setCertificateEntry(chain[i].getIssuerDN().toString(), chain[i]);
          }
        }

        // Save the KeyStore to the file.
        FileOutputStream os = new FileOutputStream(keyStorePath);
        keyStore.store(os, keyStorePassword__);
        os.close();

        //System.out.println("Keystore saved in " + keyStorePath);
      }
      catch(Exception e)
      {
        // Anything going wrong during writing is treated as an unrecoverable
        // error
        throw new RuntimeContainedException(e);
      }
    }

    // Returns the KeyStore containing the locally trusted keys or an
    // empty KeyStore if the KeyStore file cannot be found.
    private KeyStore readLocalKeyStore(String keyStorePath)
    {
      if (keyStore_ != null)
        return keyStore_;

      // Create the keystore. If the stream is null the KeyStore
      // will be empty.

      KeyStore keyStore = null;

      try
      {
        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      }
      catch(GeneralSecurityException gex)
      {
        // This is an unrecoverable error so convert it to a
        // RuntimeException
        throw new RuntimeContainedException(gex);
      }

      FileInputStream is = null;

      try
      {
        is = new FileInputStream(keyStorePath);
      }
      catch(IOException ioe)
      {
        // If the path does not exist then a null stream means
        // the keystore is initialized empty. If an untrusted
        // certificate chain is trusted by the user, then it will be
        // saved in the file pointed to by keyStorePath_.
      }

      try
      {
        keyStore.load(is, keyStorePassword__);
      }
      catch(Exception e)
      {
        // We have an empty KeyStore already. If for any reason
        // we can't load it then return it anyway and hopefully
        // we'll be able to write out a new one later.
      }
      finally
      {
        try
        {
          if (is != null)
            is.close();
        }
        catch(Exception e) {}
      }

      keyStore_ = keyStore;

      return keyStore;
    }

    private boolean trustAlways(Any toTrust)
    {
      boolean ret = false;
      Map m = (Map)toTrust;
      if (m.contains(AnyCertificate.kPermanent__))
      {
        BooleanI b = (BooleanI)m.remove(AnyCertificate.kPermanent__);
        ret = b.getValue();
      }
      return ret;
    }

    private boolean checkValidity(X509Certificate[] chain)
    {
      try
      {
        for (int i = 0; i < chain.length; i++)
        {
          chain[0].checkValidity();
        }
      }
      catch(CertificateException ce)
      {
        return false;
      }
      return true;
    }

    /**
     * Merges the system wide accepted issuers and the own ones and
     * returns them.
     *
     * TS: When used by clients to authenticate servers at least, this
     * method is not called.
     *
     * @return: Array of X509 certificates of the accepted issuers.
     */
    public X509Certificate[] getAcceptedIssuers()
    {
      //System.out.println("In getAcceptedIssuers()");
      X509Certificate[] cf     = ((X509TrustManager)tm_).getAcceptedIssuers();
      X509Certificate[] allCfs = cf;
      //System.out.println("Number of accepted issuers is " + allCfs.length);

      KeyStore keyStore = readLocalKeyStore(keyStorePath__);

      try
      {
        if (keyStore.size() != 0)
        {
          Enumeration ownCerts = keyStore.aliases();
          Vector certsVect = new Vector();
          while(ownCerts.hasMoreElements())
          {
            Object cert = ownCerts.nextElement();
            certsVect.add(keyStore.getCertificate(cert.toString()));
          }

          int newLength = cf.length + certsVect.size();

          allCfs = new X509Certificate[newLength];

          Iterator it=certsVect.iterator();

          for(int i = 0; i < newLength; i++)
          {
            if(i < cf.length)
            {
              allCfs[i] = cf[i];
            }
            else
            {
              allCfs[i] = (X509Certificate)it.next();
            }
          }
        }
      }
      catch(KeyStoreException e)
      {
        throw new RuntimeContainedException(e);
      }

      for(int i = 0; i < allCfs.length; i++)
      {
        System.out.println("allCfs[" + i + "]: " + allCfs[i].getIssuerDN());
      }
      return allCfs;
    }
  }

  private class ClientHandShake implements HandshakeCompletedListener
  {
    public void handshakeCompleted(HandshakeCompletedEvent event)
    {
      System.out.println("HANDSHAKE COMPLETED " + event);
    }
  }
}
