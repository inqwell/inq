/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Enumeration;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyString;
import com.inqwell.any.ConstString;
import com.inqwell.any.Map;
import com.inqwell.any.SystemProperties;

/**
 * Set of general utility functions
 *
 */
public class Util
{
  static Map _properties = null;

  public static Any java_version       = AbstractValue.flyweightString("java.version");
  public static Any java_vendor        = AbstractValue.flyweightString("java.vendor");
  public static Any java_vendor_url    = AbstractValue.flyweightString("java.vendor.url");
  public static Any java_home          = AbstractValue.flyweightString("java.home");
  public static Any java_class_version = AbstractValue.flyweightString("java.class.version");
  public static Any java_class_path    = AbstractValue.flyweightString("java.class.path");
  public static Any os_name            = AbstractValue.flyweightString("os.name");
  public static Any os_arch            = AbstractValue.flyweightString("os.arch");
  public static Any os_version         = AbstractValue.flyweightString("os.version");
  public static Any file_separator     = AbstractValue.flyweightString("file.separator");
  public static Any path_separator     = AbstractValue.flyweightString("path.separator");
  public static Any line_separator     = AbstractValue.flyweightString("line.separator");
  public static Any user_name          = AbstractValue.flyweightString("user.name");
  public static Any user_home          = AbstractValue.flyweightString("user.home");
  public static Any user_dir           = AbstractValue.flyweightString("user.dir");
    
  static
  {
    loadProperties();
  }
  
  public static String readLine(InputStream is) throws IOException
  {
		int    byteRead;
		int    offset    = 0;
		int    priorByte = -1;
		String ret       = null;
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream(80);
		
		while(true)
		{
			byteRead = is.read();
			
			if (byteRead < 0)
			{
				bos.reset();
				bos.close();
				throw new IOException ("readline: Premature EOF");
			}
			
			//if (byteRead == '\n' && priorByte == '\r')
			if (byteRead == '\n')
			{
				ret = bos.toString();
				bos.reset();
				bos.close();
			  return ret;
			}
			
			if (byteRead != '\r')
			  bos.write(byteRead);
			
			priorByte = byteRead;
		}
  }
  
  /**
   * Encode a string to the json standard, escaping any special json
   * characters.
   */
  public static String escapeJSON(String s)
  {
    if (s == null)
      return null;

    StringBuffer sb = new StringBuffer();
    
    escape(s, sb);
    
    return sb.toString();
  }

  private static void escape(String s, StringBuffer sb)
  {
    for (int i = 0; i < s.length(); i++)
    {
      char ch = s.charAt(i);
     
      switch (ch)
      {
        case '"':
          sb.append("\\\"");  // quote
          break;
          
        case '\\':
          sb.append("\\\\");  // rev solidus
          break;
          
        case '\b':
          sb.append("\\b");   // backspace
          break;
          
        case '\f':
          sb.append("\\f");   // form feed
          break;
          
        case '\n':
          sb.append("\\n");   // newline
          break;
          
        case '\r':
          sb.append("\\r");   // cr
          break;
          
        case '\t':
          sb.append("\\t");   // tab
          break;
          
        case '/':
          sb.append("\\/");   // solidus
          break;
          
        default:
          // Non-printable, non-ascii
          if ((ch >= '\u0000' && ch <= '\u001F') ||
              (ch >= '\u007F' && ch <= '\u009F') ||
              (ch >= '\u2000' && ch <= '\u20FF'))
          {
            String ss = Integer.toHexString(ch);
            sb.append("\\u");
            for (int k = 0; k < 4 - ss.length(); k++)
            {
              sb.append('0');
            }
            sb.append(ss.toUpperCase());
          }
          else
          {
            sb.append(ch);
          }
      }
    }
  }

  public static String escapeXML(String s)
  {
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(s);
    char ch = iterator.current();
    while (ch != CharacterIterator.DONE)
    {
      if (ch == '<')
      {
        result.append("&lt;");
      }
      else if (ch == '>')
      {
        result.append("&gt;");
      }
      else if (ch == '\"')
      {
        result.append("&quot;");
      }
      else if (ch == '\'')
      {
        result.append("&#039;");
      }
      else if (ch == '&')
      {
        result.append("&amp;");
      }
      else if ((ch >= '\u0000' && ch <= '\u001F') ||
               (ch >= '\u007F' && ch <= '\u009F') ||
               (ch >= '\u2000' && ch <= '\u20FF'))
      {
        addCharEntity(ch, result);
      }
      else
      {
        result.append(ch);
      }
      ch = iterator.next();
    }
    return result.toString();
  }

  private static void addCharEntity(int i, StringBuilder sb)
  {
    String padding = "";
    if (i <= 9)
    {
      padding = "00";
    }
    else if (i <= 99)
    {
      padding = "0";
    }
    else
    {
      // no prefix
    }
    String number = padding + String.valueOf(i);
    sb.append("&#" + number + ";");
  }

  /**
   * Return the specified property or null if no such property exists
   */
  public static Any getProperty (Any a)
  {
    if (!_properties.contains(a))
      return null;
    else
      return _properties.get(a);
  }
  
  public static String lineSeparator ()
  {
    return getProperty (line_separator).toString();
  }
  
  static void loadProperties () throws SecurityException
  {
    _properties = AbstractComposite.map();
    
    java.util.Properties properties = System.getProperties();
    
    Enumeration e = properties.propertyNames();
    
    while (e.hasMoreElements())
    {
      String propName  = (String)e.nextElement();
      String propValue = (String)properties.getProperty(propName);
      if (propValue == null)
        _properties.add (new ConstString (propName), AnyString.EMPTY);
      else
        _properties.add (new ConstString (propName), new ConstString(propValue));
    }
    
    // Try to add the name of localhost
    try
    {
      _properties.add(SystemProperties.localhostname, new ConstString(java.net.InetAddress.getLocalHost().getHostName()));
    }
    catch(Exception ex)
    {
      _properties.add(SystemProperties.localhostname, new ConstString("unknown"));
    }
  }
}
