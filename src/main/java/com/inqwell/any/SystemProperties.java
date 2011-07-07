/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/SystemProperties.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * SystemProperties 
 */
public final class SystemProperties
{
	private static SystemProperties theSystemProperties__ = null;	
	private Map properties_;	
  
  public static Any localhostname      = AbstractValue.flyweightString("localhostname");
  
  /**
   * Load the properties from the stream and place them into the given Map
   * as a single level. So as not to clash with the Inq path form, the
   * period character <code>'.'</code> is converted into <code>'_'</code>. Thus
   * a Java property of <code>path.separator</code> becomes <code>path_separator</code>
   * @param is
   * @param m
   */
  public static void loadProperties(InputStream is, Map m)
  {
    try
    {
      Properties p = new Properties();
      p.load(is);
      convertToMap(p, m, true);
    }
    catch (IOException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public static void convertToMap(Properties p, Map m, boolean resolve)
  {
    Enumeration props   = p.propertyNames();
    
    while (props.hasMoreElements())
    {
      String pName = (String)props.nextElement();
      String pValue = p.getProperty(pName);
      if (resolve)
        pValue = expandPropValue(pValue);
      pName = pName.replace('.', '_');
      m.add(new ConstString(pName), new ConstString(pValue));
    }
  }
  
  private static String expandPropValue(String value)
  {
    int idx = 0;
    int endIdx = 0;
    
    while ((idx = value.indexOf("${", idx)) >= 0)
    {
      int startIdx = idx + 2;
      if ((endIdx = value.indexOf("}", startIdx)) > 0)
      {
        if (endIdx > startIdx)
        {
          String propName = value.substring(idx+2, endIdx);
          String propVal = System.getProperty(propName);
          if (propVal != null)
          {
            String oldChar = value.substring(idx, endIdx+1);
            value = value.replace(oldChar, propVal);
            endIdx = idx + propVal.length() - 1;
          }
        }
        idx = endIdx + 1;
      }
      else
        idx = startIdx;
    }
    return value;
  }
  
	public static SystemProperties instance()
	{
		if (theSystemProperties__ == null)
		{
			synchronized (SystemProperties.class)
			{
				if (theSystemProperties__ == null)
					theSystemProperties__ = new SystemProperties();
			}
		}
		return theSystemProperties__;
	}

	private SystemProperties ()
	{
		properties_ = AbstractComposite.managedMap();
		loadProperties();
	}
	
	public Map getSystemProperties ()
	{
		return properties_;
	}
	
	private void loadProperties()
	{
		Properties  sysProp = System.getProperties();
    convertToMap(sysProp, properties_, false);
    
    // Try to add the name of localhost
    try
    {
      properties_.add(localhostname, new ConstString(java.net.InetAddress.getLocalHost().getHostName()));
    }
    catch(Exception ex)
    {
      properties_.add(localhostname, new ConstString("unknown"));
    }
	}
}
