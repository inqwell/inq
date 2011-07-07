/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/util/CommandArgs.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.util;

import java.util.StringTokenizer;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractMap;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.ConstString;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.StringI;

/**
 * <i>Perform command line argument parsing according to
 * the <b>-flag arg</b> style of command line</i>
 * @author $Author: sanderst $
 * @version $Revision: 1.7 $
 */ 
public class CommandArgs extends AbstractMap
{
  public static Any commandLine__      = AbstractValue.flyweightString("argsMap");
  public static Any shebang__          = AbstractValue.flyweightString("shebang");
  public static String minusShebang__  = "-shebang";
  public static String minusIn__       = "-in";
  
  private String args_[];   // From main (String args[])
  
  AnyString s_ = new AnyString();
 
 /**
  * Creates a command args object with the to parse the arguments in the
  * given string array.  Typically called from main(String args[])
  */
  public CommandArgs(String args[])
  {
    args_ = args;
  }
 
  /**
   * Look for the given command line boolean flag.  If found  return true.
   * Otherwise, return false.
   */
  public boolean arg(String option)
  {
    return findArg(option, 0) != -1;
  }
  
  /**
   * Look for the given command line flag expecting an argument value
   * If found place the result in val and return true.  Otherwise, place
   * defVal in val and return false.
   */
  public boolean arg(String option,
                     Any    val,
                     Any    defVal)
  {
    int i = findArg(option, 0);
    if (i == -1)
    {
      if (defVal != null)
        val.copyFrom(defVal);
      return false;
    }
    else
    {
      val.copyFrom(getArg(option, i));
      return true;
    }
  }
  
  /**
   * Look for the given command line flag expecting an argument value
   * If found place the result in val and return true.  Otherwise, place
   * defVal in val and return false.
   */
  public boolean arg(String option,
                     Any    val,
                     String defVal)
  {
    int i = findArg(option, 0);
    if (i == -1)
    {
      if (defVal != null)
      {
        s_.setValue(defVal);
        val.copyFrom(s_);
      }
      return false;
    }
    else
    {
      val.copyFrom(getArg(option, i));
      return true;
    }
  }
  
  public boolean arg(String option,
                     Any    val)
	{
		return arg(option, val, (String)null);
	}
	
  /**
   * Look for the nth occurrence of the given command line flag expecting a
   * list of argument values.  Occurrences are numbered from zero.
   * If found place the list as items of type val in array a and return true.
   * Otherwise return false.
   */
  public boolean arg(String option,
                     Array  a,
                     Any    val,
                     int    skip)
  {
    a.empty();
    int i = findArg(option, skip);

    if (i == -1)
      return false;
    
    ConstString thisArg;
    
    if (option.length() != args_[i].length())
    {
      thisArg =  new ConstString (args_[i].substring(option.length()));
      a.add (val.copyFrom(thisArg).cloneAny());
    }
    
    while (++i < args_.length)
    {
      if (args_[i].substring(0, 1).equals("-"))
        break;
      thisArg = new ConstString (args_[i]);
      a.add (val.copyFrom(thisArg).cloneAny());
    }
    return true;
  }
  
  /**
   * Look for the given command line flag expecting a list of argument values.
   * If found place the list as items of type val in array a and return true.
   * Otherwise return false.
   */
  public boolean arg(String option,
                     Array  a,
                     Any    val)
  {
    return arg(option, a, val, 0);
  }

  /**
   * Convert the command line to a map.  The command line is
   * assumed to conform to the syntax <code>-f arg</code>.
   * Because we are not looking for a specific argument
   * the syntax <code>-farg</code> has limited supported as we
   * cannot tell if this is an argument name (whose value
   * would be boolean <code>true</code>) or the
   * argument <code>f</code> whose value is <code>arg</code>.
   * The former possibility is assumed.
   * <p>
   * Each arg value is put in a map with the key of the argument
   * name and the value as a string.  If an argument
   * is repeated then the
   * last one found is placed in the map.  If the argument
   * value is not a single token then only the first one
   * specified will be available.
   */
  public Map toMap()
  {
    Map args = AbstractComposite.simpleMap();
    int i = 0;
    
    while (i < args_.length)
    {
      String thisArg = args_[i];
      
      if (thisArg.startsWith("-"))
      {
        if (thisArg.contains("="))
        {
          // arg=value format
          // tokenise, strip any leading "-" and establish value
          StringTokenizer s = new StringTokenizer(thisArg, "=");
          String argName = s.nextToken();
          String argVal  = s.nextToken();
          int idx = argName.lastIndexOf('-');
          argName = argName.substring(idx+1);
          args.replaceItem(new ConstString(argName), new AnyString(argVal));
        }
        else if (thisArg.substring(0, 1).equals("-"))
        {
          // Found an argument flag.  If its the last thing on the command
          // line (combined value -farg) or the next token begins with a
          // "-" then assume -farg.  If its just -f then assume boolean
          // true.
          if (i == args_.length - 1)
          {
            // last one on the cmd line
            inMap(args, thisArg, new AnyBoolean(true));
          }
          else
          {
            //System.out.println("NOT LAST " + thisArg);
            String nextArg = args_[i+1];
            if (nextArg.startsWith("-"))
            {
              // next arg is new -farg or -f arg.  Assume this one
              // is -farg
              // because the next arg is also a flag assume
              // this arg is name whose boolean value is true
              inMap(args, thisArg, new AnyBoolean(true));
            }
            else
            {
              // next arg is value and this arg is name
              inMap(args, thisArg, new AnyString(nextArg));
              i++;
            }
          }
        }
      }
      i++;
    }
    return args;
  }
  
  private void inMap(Map args, String argName, Any argVal)
  {
    args.replaceItem(new ConstString(argName.substring(1)), argVal);
  }
    
  private int findArg (String option, int skip)
  {
    for (int i = 0; i < args_.length; i++)
    {
      try
      {
        if ((args_[i].substring(0, option.length()).equals(option)) &&
            (skip-- == 0))
          return i;
      }
      catch (Exception e){}
    }
    return -1;
  }
  
  private StringI getArg(String option, int i)
  {    
    // Support -farg and -f arg
    if (option.length() == args_[i].length())
    {
      s_.setValue(args_[i + 1]);
      return s_;
    }
    else
    {
      s_.setValue(args_[i].substring(option.length()));
      return s_;
    }
  }
  
  // Return the "child" as an array for scripted access to multi-valued arguments
  public Any getIfContains(Any key)
  {
    AnyString s = new AnyString();
    Array ret = AbstractComposite.array();
    if (arg("-" + key.toString(), ret, s))
      return ret;
    
    return null;
  }
  
  public Any get(Any key)
  {
    Any ret = getIfContains(key);
    if (ret == null)
      handleNotExist(key);
    
    return ret;
  }

  protected void afterAdd(Any key, Any value)
  {
  }

  protected void afterRemove(Any key, Any value)
  {
  }

  protected boolean beforeAdd(Any key, Any value)
  {
    return false;
  }

  protected void beforeRemove(Any key)
  {
  }

  protected void emptying()
  {
  }

  public boolean isEmpty()
  {
    return false;
  }

  public Iter createIterator()
  {
    return DegenerateIter.i__;
  }
}
