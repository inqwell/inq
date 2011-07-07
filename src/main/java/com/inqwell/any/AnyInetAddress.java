/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AnyInetAddress extends    DefaultPropertyAccessMap
                            implements Value,
                                       Cloneable
{
  public static AnyInetAddress null__ = new AnyInetAddress((InetAddress)null);

  private InetAddress inetAddr_;

  static Any getLocalHost()
  {
    try
    {
      return new AnyInetAddress(InetAddress.getLocalHost());
    }
    catch(UnknownHostException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public AnyInetAddress()
  {
    inetAddr_ = null;
  }

  /**
   */
  public AnyInetAddress(InetAddress i)
  {
    inetAddr_ = i;
  }

  public InetAddress getInetSocketAddress()
  {
    return inetAddr_;
  }

  public boolean isNull()
  {
    return inetAddr_ == null;
  }
  
  public void setNull()
  {
    inetAddr_ = null;
  }

  // These intended for property access
  
  public Any getHostName()
  {
    return new AnyString(inetAddr_. getHostName());
  }

  public Any getHostAddress()
  {
    return new AnyString(inetAddr_.getHostAddress());
  }
  
  public Any  getCanonicalHostName()
  {
    return new AnyString(inetAddr_.getCanonicalHostName());
  }
  
  // End properties
  
  public Object getPropertyBean()
  {
    return inetAddr_;
  }

  public void setPropertyBean(Object o)
  {
    inetAddr_ = (InetAddress)o;
  }

  public Any copyFrom (Any a)
  {
    if (a == null || AnyNull.isNullInstance(a))
    {
      setNull();
      return this;
    }
    
    if (a != this)
    {
      if (!(a instanceof AnyInetAddress))
        throw new IllegalArgumentException("AnyInetAddress.copyFrom()");

      AnyInetAddress i = (AnyInetAddress)a;
      inetAddr_ = i.inetAddr_;
    }
    return this;
  }

  public void accept (Visitor v)
  {
    // Override from AbstractMap or equals operator doesn't work.
    // Bit messy but oh well
    // Actually need a better way, must add a method to Visitor...
    if (v instanceof Equals || v instanceof Assign || v instanceof NotEquals)
      v.visitUnknown(this);
    else
      super.accept(v);
  }

  public Iter createKeysIterator()
  {
    return DegenerateIter.i__;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
  
  public boolean equals(Any a)
  {
    if (a instanceof AnyInetAddress)
    {
      AnyInetAddress i = (AnyInetAddress)a;
      
      if (i.inetAddr_ == null && inetAddr_ == null)
        return true;
      
      if (inetAddr_ != null)
        return inetAddr_.equals(i.inetAddr_);
      else
        return false;
    }
    
    return false;
  }

  public int hashCode()
  {
    if (inetAddr_ == null)
      return 0;
    
    return inetAddr_.hashCode();
  }
  
  public String toString()
  {
    if (inetAddr_ == null)
      return "inetaddress:null";
    else
      return inetAddr_.toString();
  }
}
