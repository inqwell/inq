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

/**
 * The Inq Version Number
 * 
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class Version extends    AbstractFunc
                     implements Cloneable
{
  public static Version version__;
  
  public static Version getVersion()
  {
    synchronized(Version.class)
    {
      if (version__ == null)
        version__ = new Version();
      
      return version__;
    }
  }
  
  public Any version_ = AbstractValue.flyweightString("1.1");
  
  private Version() {}
  
  public Any exec(Any a) throws AnyException
  {
    return version_;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    return this;
  }
}
