/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * The Inq Version Number
 * 
 * <p>
 * @author tom
 */
public class Version extends    AbstractFunc
                     implements Cloneable
{
	private static Any version__;
	
	public static Any getVersion()
	{
		if (version__ != null)
			return version__;
		
    AnyURL u  = new AnyURL("cp:///inq/inq.properties");
    URL    u1 = u.getURL();
    InputStream s = null;
    try
    {
      URLConnection uc = u1.openConnection();
      Properties p = new Properties();
      p.load(s = uc.getInputStream());
      Object v = p.get("inq.version");
      if (v == null)
        throw new AnyRuntimeException("Unknown version");
      Object b = p.get("inq.build");
      if (b == null)
        throw new AnyRuntimeException("Unknown build");
      return new ConstString("Inq version " + v.toString() +
                             " (build " + b.toString() + ")");
    }
    catch(IOException e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      if (s != null)
      {
        try
        {
          s.close();
        }
        catch(Exception e) {}
      }
    }
	}
	
  public Any exec(Any a) throws AnyException
  {
  	return getVersion();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    return super.clone();
  }
}
