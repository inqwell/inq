/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/UntrustedCertificateException.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.net;

import com.inqwell.any.Any;
import com.inqwell.any.Map;
import com.inqwell.any.Vectored;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.RuntimeContainedException;

import java.security.cert.CertificateException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * An exception that can be thrown by implementations
 * of javax.net.ssl.X509TrustManager to indicate that
 * a certificate chain is untrusted. The chain is
 * carried in the exception and can be externalised
 * as an Inq structure.
 * <p>
 * An optional underlying exception can also be carried,
 * if desired.
 * <p>
 * Note: This is an unchecked exception. It can be used
 * to pass control and data out of an X509TrustManager to
 * a point where the checked exception declared by that
 * interface, CertificateException, no longer appears (yet
 * will pass through even if not caught, presumably by
 * a similar exception tunneling technique).
 */
public class UntrustedCertificateException extends CertificateException
{
  private final static Any cert__ = new ConstString("certificate");

  private Certificate[] certChain_;
  private Throwable     underlying_;

  // In fact, even if a certificate chain is trusted it could
  // have expired. If we throw in this case the trusted flag is
  // true.
  private boolean trusted_ = false;

  public UntrustedCertificateException(Certificate[] certChain)
  {
    certChain_ = certChain;
  }

  public UntrustedCertificateException(Certificate[] certChain,
                                       boolean       trusted)
  {
    certChain_ = certChain;
    trusted_ = trusted;
  }

  public UntrustedCertificateException(Certificate[] certChain,
                                       Throwable     underlying)
  {
    underlying_ = underlying;
    certChain_  = certChain;
  }

  public UntrustedCertificateException(Certificate[] certChain,
                                       Throwable     underlying,
                                       String msg)
  {
    super(msg);
    underlying_ = underlying;
    certChain_ = certChain;
  }

  public Throwable getThrowable()
  {
    return underlying_;
  }

  public Any externaliseX509Chain()
  {
    return externaliseX509Chain(certChain_);
  }

  public boolean getTrusted()
  {
    return trusted_;
  }

  static public Any externaliseX509Chain(Certificate[] certChain)
  {
    Map ret = AbstractComposite.orderedMap();

    for(int i = 0; i < certChain.length; i++)
    {
      X509Certificate cert = (X509Certificate)certChain[i];
      Map m = AbstractComposite.simpleMap();
      m.add(cert__, new AnyCertificate(cert));
      ret.add(new ConstInt(i), m);
    }

    return ret;
  }
}

