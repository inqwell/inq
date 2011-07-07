/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyInetSocketAddress.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.net.InetSocketAddress;

public class AnyInetSocketAddress extends    DefaultPropertyAccessMap
                                  implements Value,
                                             Cloneable
{
  public static AnyInetSocketAddress null__ = new AnyInetSocketAddress((InetSocketAddress)null);

  private InetSocketAddress inetSocketAddr_;

  public AnyInetSocketAddress()
  {
    inetSocketAddr_ = null;
  }

  /**
   */
  public AnyInetSocketAddress(InetSocketAddress i)
  {
    inetSocketAddr_ = i;
  }

  public InetSocketAddress getInetSocketAddress()
  {
    return inetSocketAddr_;
  }

  public boolean isNull()
  {
    return inetSocketAddr_ == null;
  }
  
  public void setNull()
  {
    inetSocketAddr_ = null;
  }

  // These intended for property access
  
  public Any getHostName()
  {
    return new AnyString(inetSocketAddr_. getHostName());
  }

  public Any getHostAddress()
  {
    return new AnyString(inetSocketAddr_.getAddress().getHostAddress());
  }
  
  public Any getPort()
  {
    return new AnyInt(inetSocketAddr_.getPort());
  }
  
  public Any  getCanonicalHostName()
  {
    return new AnyString(inetSocketAddr_.getAddress().getCanonicalHostName());
  }
  
  // End properties
  
  public Object getPropertyBean()
  {
    return inetSocketAddr_;
  }

  public void setPropertyBean(Object o)
  {
    inetSocketAddr_ = (InetSocketAddress)o;
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
      if (!(a instanceof AnyInetSocketAddress))
        throw new IllegalArgumentException("AnyInetSocketAddress.copyFrom()");

      AnyInetSocketAddress i = (AnyInetSocketAddress)a;
      inetSocketAddr_ = i.inetSocketAddr_;
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
    if (a instanceof AnyInetSocketAddress)
    {
      AnyInetSocketAddress i = (AnyInetSocketAddress)a;
      
      if (i.inetSocketAddr_ == null && inetSocketAddr_ == null)
        return true;
      
      if (inetSocketAddr_ != null)
        return inetSocketAddr_.equals(i.inetSocketAddr_);
      else
        return false;
    }
    
    return false;
  }

  public int hashCode()
  {
    if (inetSocketAddr_ == null)
      return 0;
    
    return inetSocketAddr_.hashCode();
  }
  
  public String toString()
  {
    if (inetSocketAddr_ == null)
      return "inetaddress:null";
    else
      return inetSocketAddr_.toString();
  }
}
