/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.net;

import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;
import com.inqwell.any.AnyString;
import com.inqwell.any.ConstString;
import com.inqwell.any.IntI;
import com.inqwell.any.ConstInt;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.util.Util;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.io.IOException;
/**
 * Set of general utility functions
 *
 */
public class NetUtil
{
  // Property names
	private static final String httpProxyUser__    = "http.proxyUser";
	private static final String proxyUser__        = "proxyUser";
	private static final String httpProxyPasswd__  = "http.proxyPasswd";
	private static final String proxyPasswd__      = "proxyPasswd";

  private static final String    contentLength__ = "content-length";
  private static final Any       contentLengthAny__ = new ConstString(contentLength__);
  
  // Inq paths
  private static final Any iHttpUseProxy__    = new ConstString("httpUseProxy");
  private static final Any iHttpProxyServer__ = new ConstString("httpProxyServer");
  private static final Any iHttpProxyPort__   = new ConstString("httpProxyPort");
  private static final Any iHttpProxyUser__   = new ConstString("httpProxyUser");
  private static final Any iHttpProxyPwd__    = new ConstString("httpProxyPwd");

	/**
	 * Parse a line representing an HTTP header. HTTP header lines
	 * are of the form <code>Name: value</code>. The line is assumed
	 * to represent a complete header name and value, i.e. this method
	 * does not manage folded header values.
	 * <p>
	 * The header name and value are placed in the given map as
	 * a key-value pair. This method creates the values as an
	 * appropriate <code>Any</code> type, for example,
	 * <code>Content-length</code> is mapped to an <code>IntI</code>
	 * <p>
	 * If the line cannot be recognised as an HTTP header then the
	 * map is unchanged.
	 */
	public static void parseHttpHeader(String lineIn, Map headers)
	{
		int colonIndex = lineIn.indexOf(':');
		
		if (colonIndex > 0)
		{
			Any headerKey = new ConstString(lineIn.substring(0, colonIndex).toLowerCase());
			
			// Look for the first non-white space character after
			// the colon for the value.
			colonIndex++;
			
			char nonWhite;
			
			while (colonIndex < lineIn.length() &&
			       (((nonWhite = lineIn.charAt(colonIndex)) == ' ') ||
			        (nonWhite == '\t')))
			  colonIndex++;
			  
			String headerValue = lineIn.substring(colonIndex);
			Any headerAnyValue = null;
			if (headerKey.equals(contentLengthAny__))
			  headerAnyValue = new ConstInt(headerValue);
			else
			  headerAnyValue = new ConstString(headerValue);
			
			if (headers.contains(headerKey))
			{
        headers.replaceItem(headerKey, headerAnyValue);
			}
			else
			{
        headers.add(headerKey, headerAnyValue);
      }
		}
	}
	
	public static Map parseURLQuery(String queryStr, Map query)
	{
    if (queryStr == null)
      return query;
      
    if (query == null)
      query = AbstractComposite.simpleMap();
    
    StringTokenizer st = new StringTokenizer(queryStr, "&");
    
    while (st.hasMoreTokens())
    {
      String queryParam = st.nextToken();
      
      int equIndx = queryParam.indexOf('=');
      
      Any queryKey;
      Any queryVal;
      if (equIndx <= 0)
      {
        // No value - just put current token in the map
        // against boolean true
        queryKey = new ConstString(queryParam);
        queryVal = AnyBoolean.TRUE;
      }
      else
      {
        queryKey = new ConstString(queryParam.substring(0, equIndx));
        queryVal = ((equIndx + 1) > queryParam.length())
            ? AnyBoolean.TRUE
            : new ConstString(queryParam.substring(equIndx + 1));
      }

      if (query.contains(queryKey))
        query.replaceItem(queryKey, queryVal);
      else
        query.add(queryKey, queryVal);  
    }
    return query;
	}
	
	/**
	 * Read the headers form an http request and return them
	 * as a map relating the header to its value
	 */
  public static Map readHeaders(InputStream is) throws IOException
  {
		String lineIn;
    Map    headers = AbstractComposite.simpleMap();
    
		while ((lineIn = Util.readLine(is)).length() != 0)
		{
      //System.out.println ("Header: " + lineIn);
			NetUtil.parseHttpHeader(lineIn, headers);
		}
		return headers;
  }

	public static int getContentLength(Map headers)
	{
    if (headers.contains(contentLengthAny__))
    {
      IntI i = (IntI)headers.get(contentLengthAny__);

      return i.getValue();
    }
    else
    {
      return 0;
    }
	}
	
	public static Map getProxyServer()
	{
    Map ret = null;
    
    LocateNode l = new LocateNode("$catalog.proxyServer");
    try
    {
      ret = (Map)l.exec(l);
    }
    catch (AnyException e) {}
    
    return ret;
	}
	
	public static String getProxyHost(Map proxyServer)
	{
    if (proxyServer != null && proxyServer.contains(iHttpProxyServer__))
      return proxyServer.get(iHttpProxyServer__).toString();
    else
      return null;
  }
	
	public static int getProxyPort(Map proxyServer)
	{
    if (proxyServer.contains(iHttpProxyPort__))
      return Integer.parseInt(proxyServer.get(iHttpProxyPort__).toString());
    else
      return 0;
  }
	
	public static String getProxyUser(Map proxyServer)
	{
    if (proxyServer != null && proxyServer.contains(iHttpProxyUser__))
      return proxyServer.get(iHttpProxyUser__).toString();
    else
      return null;
  }
	
	public static String getProxyPassword(Map proxyServer)
	{
    if (proxyServer != null && proxyServer.contains(iHttpProxyPwd__))
      return proxyServer.get(iHttpProxyPwd__).toString();
    else
      return null;
  }
	
	public static boolean useProxy(Map proxyServer)
	{
    if (proxyServer != null && proxyServer.contains(iHttpUseProxy__))
    {
      AnyBoolean b = new AnyBoolean();
      b.copyFrom(proxyServer.get(iHttpUseProxy__));
      return b.getValue();
    }
    else
      return false;
  }
}
