/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/net/AnyCertificate.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.net;

import com.inqwell.any.*;
import java.security.cert.X509Certificate;
import java.security.MessageDigest;

/**
 * A map representation of various attributes of a certificate.
 */
public class AnyCertificate extends SimpleMap
{
  static StringI kVersion__         = new ConstString("version");
  static StringI kSerialNumber__    = new ConstString("serialNumber");
  static StringI kSigAlg__          = new ConstString("sigAlg");
  static StringI kIssuer__          = new ConstString("issuer");
  static StringI kStartDate__       = new ConstString("startDate");
  static StringI kEndDate__         = new ConstString("endDate");
  static StringI kSignature__       = new ConstString("signature");
  static StringI kMD5Fingerprint__  = new ConstString("MD5Fingerprint");
  static StringI kSHA1Fingerprint__ = new ConstString("SHA1Fingerprint");

  static StringI unknown__          = new ConstString("Unknown");

  static public StringI kPermanent__ = new ConstString("permanent");

  /**
   * Fill the map from the attributes of an X509Certificate
   */
  public AnyCertificate(X509Certificate certificate)
  {
    decodeCert(certificate);
  }

  private void decodeCert(X509Certificate certificate)
  {
    this.add(kVersion__,
             new ConstInt(certificate.getVersion()));

    this.add(kSerialNumber__,
             new ConstString(byteArrayToHex(certificate.getSerialNumber().toByteArray())));

    this.add(kSigAlg__,
             new ConstString(certificate.getSigAlgName()));

    this.add(kIssuer__,
             new ConstString(certificate.getIssuerDN().getName()));

    this.add(kStartDate__,
             new ConstDate(certificate.getNotBefore()));

    this.add(kEndDate__,
             new ConstDate(certificate.getNotAfter()));

    this.add(kSignature__,
             new ConstString(byteArrayToHex(certificate.getSignature())));

    try
    {
      this.add(kMD5Fingerprint__,
               new ConstString(getFingerprint(certificate.getEncoded(), "MD5")));
    }
    catch(Exception fingerE)
    {
      this.add(kMD5Fingerprint__, unknown__);
    }

    try
    {
      this.add(kSHA1Fingerprint__,
               new ConstString(getFingerprint(certificate.getEncoded(), "SHA1")));
    }
    catch(Exception fingerE)
    {
      this.add(kSHA1Fingerprint__, unknown__);
    }
  }

  private String byteArrayToHex(byte[] byteData)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < byteData.length; i++)
    {
      if (i != 0)
        sb.append(":");

      int b = byteData[i] & 0xff;
      String hex = Integer.toHexString(b).toUpperCase();
      if (hex.length() == 1)
        sb.append("0");

      sb.append(hex);
    }
    return sb.toString();
  }

  private String getFingerprint(byte[] certificateBytes,
                                String algorithm) throws Exception
  {
    MessageDigest md = MessageDigest.getInstance(algorithm);
    md.update(certificateBytes);
    byte[] digest = md.digest();
    return byteArrayToHex(digest);
  }
}
