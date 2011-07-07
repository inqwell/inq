/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyURL.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-04-09 18:17:49 $
 */

package com.inqwell.any;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;

import com.inqwell.any.net.InqStreamHandlerFactory;
import com.inqwell.any.net.NetUtil;

public class AnyURL extends    AnyObject
										implements Cloneable
{
	public static AnyURL null__ = new AnyURL((URL)null);

	public static Any baseURLKey__ = new ConstString("__baseURL");
	
	private static AnyURL cwd__ = null;

  // We sometimes keep the original string, for example
  // if the string was a relative URL and the URL proper
  // can't be contructed yet.
  private String urlString_;
  
  public static AnyURL getCwd()
  {
    if (cwd__ == null)
    {
      synchronized(AnyURL.class)
      {
        if (cwd__ == null)
        {
          String cwd = System.getProperties().getProperty("user.dir");
          String fs  = System.getProperties().getProperty("file.separator");
          cwd__ = new AnyURL("file:///" + cwd + fs + "dummy");
        }
      }
    }
    return cwd__;
  }

	/**
	 * Construct to wrap a pre-loaded URL
	 */
	public AnyURL(URL u)
	{
		super(u);
	}

	/**
	 * Construct around the possibility that <code>source</code> is
	 * either a URL string
	 */
	public AnyURL(Any source)
	{
		processURL(source.toString());
	}

	public AnyURL(String source)
	{
		processURL(source);
	}

	public AnyURL() {}

  /**
   * Get the URL we represent.
   * @return The underlying URL or null if this object
   * does not represent a valid URL
   */
	public URL getURL()
	{
		return (URL)getValue();
	}

  /**
   * Get the URL we represent. If we represent an absolute
   * URL then the <code>baseURL</code> argument is ignored
   * and the absolute URL is returned. If we are a relative
   * URL string then the base is used in an attempt to
   * build an absolute URL
   * @return The underlying [base]:URL
   */
	public URL getURL(Any baseURL)
	{
	  URL ret = getURL();

	  if (ret != null && !isRelative())
	    return ret;

    // Try relative URL construction.  Argument must be
    // an AnyURL and represent a valid absolute URL
	  AnyURL bu = (AnyURL)baseURL;
	  
	  // If the base itself is relative then its no good. Just resolve
	  // this w.r.t the cwd
	  if (bu.isRelative())
	  {
	    this.resolveToCwd();
	    return this.getURL();
	  }
	  
    URL base = bu.getURL();
    
    String s = this.toString();
    
    // For windows paths that begin like C:\... it is a
    // problem for Java to use these as a relative spec
    // as it confuses the C: for a protocol. Check by seeing
    // if any protocol is good. If not prefix with file:/
    // and hope for the best.
    String protocol = getProtocol(s);
    if (protocol != null &&
        !InqStreamHandlerFactory.isKnownProtocol(protocol, s))
      s = "file:/" + s;
    
    try
    {
      ret = new URL(base, s);
    }
    catch (MalformedURLException e)
    {
	    throw new RuntimeContainedException(e);
	  }
    
		return ret;
	}
  
  public void setURL(String u)
  {
    urlString_ = null;
    processURL(u);
  }
  
  public void setURL(URL base, String u)
  {
    try
    {
      setValue(new URL(base, u));
    }
    catch (MalformedURLException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
			if ((a instanceof AnyURL))
      {
        AnyURL u = (AnyURL)a;
        this.setValue(u.getValue());
        this.urlString_ = u.urlString_;
      }
      else
      {
        setValue(null);
        processURL(a.toString());
      }
		}
    return this;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
  
  /**
   * Returns the last path component of the file name part of
   * this URL
   */
  public String getLastPath()
  {
    URL u = getURL();
    if (u != null)
    {
      return lastPath(u.getPath());
    }
    else if (urlString_ != null)
    {
      return lastPath(urlString_);
    }
    else
      return "unknown";
  }

  public boolean isRelative()
  {
    // We are a relative URL if we weren't able to convert the
    // source string into a real URL, stored in the base class.
    boolean ret = this.getValue() == null;
    
    if (!ret)
    {
      // We might still be a relative URL if the
      // path component does not begin with "/" etc
      URL u = this.getURL();
      
      String p = u.getProtocol();
      // Jar is always absolute and path is "compromised"
      if ("jar".equals(p))
        ret = false;
      else
      {
        p = u.getPath();
        if (!(p.startsWith(System.getProperty("file.separator")) ||
            p.startsWith("/")))
          ret = true;
      }
    }
    
    return ret;
  }
  
  public URL resolveToCwd()
  {
    if (this.isRelative())
      this.setURL(getCwd().getURL(), this.toString());

    return getURL();
  }
  
  /**
   * Return a Map with key/value pairs according to this URL's query
   * component: <code>....?arg1=val1&arg2=val2</code>
   * <p/>
   * If an argument has no value in the query string then its value
   * in the Map is boolean <code>true</code>. 
   * @return A Map containing the query arguments/values. If no
   * query component is present null is returned.
   */
  public Map getQueryArgs()
  {
    Map ret = null;
    URL u = getURL();
    if (u != null)
      ret = NetUtil.parseURLQuery(getURL().getQuery(), null);
    else
    {
      if (urlString_ != null)
      {
        int i = urlString_.indexOf("?");
        if (i >= 0)
          ret = NetUtil.parseURLQuery(urlString_.substring(i + 1), null);
      }
    }
    
    return ret;
  }
  
  public String toString()
  {
    if (urlString_ != null)
      return urlString_;

    return super.toString();
  }

	private void processURL(String s)
	{
		try
		{
  		URL u = new URL(s);

      // OK - the URL string is valid so it must be
      // an absolute URL
		  setValue(u);
		}

		catch (MalformedURLException e)
		{
			// Its not a valid URL so we assume its a
			// relative one.  We've got no base at this point
			// so just remember the string;
			urlString_ = s;
		}
	}
  
  private String lastPath(String path)
  {
    int idx = path.lastIndexOf('/');
    
    if (idx >= 0)
    {
      if (idx == (path.length() - 1))
        path = path.substring(0, idx); // just a trailing '/'
      else
        path = path.substring(idx+1);
    }
    return path;
  }
  
  private String getProtocol(String s)
  {
    String ret = null;
    int i;
    if ((i = s.indexOf(':')) > 0)
      ret = s.substring(0, i);
    
    return ret;
  }

  public static URL fixJarURL(URL url)
  {
    if (url == null)
      return null;
    
    // final String method = _module + ".fixJarURL";
    String originalURLProtocol = url.getProtocol();
    // if (log.isDebugEnabled()) { log.debug(method + " examining '" + originalURLProtocol + "' protocol url: " + url); }
    if ("jar".equalsIgnoreCase(originalURLProtocol) == false)
    {
      // if (log.isDebugEnabled()) { log.debug(method + " skipping fix: URL is not 'jar' protocol: " + url); }
      return url;
    }
 
    // if (log.isDebugEnabled()) { log.debug(method + " URL is jar protocol, continuing"); }
    String originalURLString = url.toString();
    // if (log.isDebugEnabled()) { log.debug(method + " using originalURLString: " + originalURLString); }
    int bangSlashIndex = originalURLString.indexOf("!/");
    if (bangSlashIndex > -1)
    {
      // if (log.isDebugEnabled()) { log.debug(method + " skipping fix: originalURLString already has bang-slash: " + originalURLString); }
      return url;
    }
 
    // if (log.isDebugEnabled()) { log.debug(method + " originalURLString needs fixing (it has no bang-slash)"); }
    String originalURLPath = url.getPath();
    // if (log.isDebugEnabled()) { log.debug(method + " using originalURLPath: " + originalURLPath); }
 
    URLConnection urlConnection;
    try
    {
      urlConnection = url.openConnection();
      if (urlConnection == null)
      {
        throw new IOException("urlConnection is null");
      }
    }
    catch (IOException e)
    {
      // if (log.isDebugEnabled()) { log.debug(method + " skipping fix: openConnection() exception", e); }
      return url;
    }
    // if (log.isDebugEnabled()) { log.debug(method + " using urlConnection: " + urlConnection); }
 
    Permission urlConnectionPermission;
    try
    {
      urlConnectionPermission = urlConnection.getPermission();
      if (urlConnectionPermission == null)
      {
        throw new IOException("urlConnectionPermission is null");
      }
    }
    catch (IOException e)
    {
      // if (log.isDebugEnabled()) { log.debug(method + " skipping fix: getPermission() exception", e); }
      return url;
    }
    // if (log.isDebugEnabled()) { log.debug(method + " using urlConnectionPermission: " + urlConnectionPermission); }
 
    String urlConnectionPermissionName = urlConnectionPermission.getName();
    if (urlConnectionPermissionName == null)
    {
      // if (log.isDebugEnabled()) { log.debug(method + " skipping fix: urlConnectionPermissionName is null"); }
      return url;
    }
 
    // if (log.isDebugEnabled()) { log.debug(method + " using urlConnectionPermissionName: " + urlConnectionPermissionName); }
 
    File file = new File(urlConnectionPermissionName);
    if (file.exists() == false)
    {
      // if (log.isDebugEnabled()) { log.debug(method + " skipping fix: file does not exist: " + file); }
      return url;
    }
    // if (log.isDebugEnabled()) { log.debug(method + " using file: " + file); }
 
    String newURLStr;
    try
    {
      newURLStr = "jar:" + file.toURL().toExternalForm() + "!/" + originalURLPath;
    }
    catch (MalformedURLException e)
 
    {
      // if (log.isDebugEnabled()) { log.debug(method + " skipping fix: exception creating newURLStr", e); }
      return url;
    }
    // if (log.isDebugEnabled()) { log.debug(method + " using newURLStr: " + newURLStr); }
 
    try
    {
      url = new URL(newURLStr);
    }
    catch (MalformedURLException e)
    {
      // if (log.isDebugEnabled()) { log.debug(method + " skipping fix: exception creating new URL", e); }
      return url;
    }
 
    return url;
  }
}
