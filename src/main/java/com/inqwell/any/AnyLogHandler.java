/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.io.IOException;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;

import com.inqwell.any.client.AnyComponent;

public class AnyLogHandler extends    DefaultPropertyAccessMap
                           implements Cloneable
{
  private static Set wrapProps__;
  
  private Handler handler_;
  
  static
  {
    synchronized(AnyLogHandler.class)
    {
      if (wrapProps__ == null)
        wrapProps__ = AbstractComposite.set();
      
      wrapProps__.add(AbstractValue.flyweightString("logFilter"));
      wrapProps__.add(AbstractValue.flyweightString("logFunc"));
      wrapProps__.add(AbstractValue.flyweightString("logLevel"));
    }
  }
  
  public AnyLogHandler(Handler h)
  {
    handler_ = h;
  }
    
  public Handler getHandler()
  {
    return handler_;
  }
  
  public void accept (Visitor v)
  {
    // Override from AbstractMap or equals operator doesn't work.
    // See AnyFile.accept also
    if (v instanceof Equals || v instanceof Assign || v instanceof NotEquals)
      v.visitUnknown(this);
    else
      super.accept(v);
  }

  public Iter createKeysIterator()
  {
    return DegenerateIter.i__;
  }

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
      if (!(a instanceof AnyLogHandler))
        throw new IllegalArgumentException();
      
      AnyLogHandler i = (AnyLogHandler)a;
      this.handler_ = i.handler_;
    }
    return this;
  }
  
  public Object getPropertyBean()
  {
    return handler_;
  }

  /**
   * Get this handler's {@link java.util.logging.Filter} if it has one,
   * wrapped inside a {@link AnyFilter}. If there is no filter then
   * returns <code>null</code>.
   * @return
   */
  public Any getLogFilter()
  {
    Any ret = null;
    
    Filter f = handler_.getFilter();
    if (f != null)
      ret = new AnyLogFilter(f);
    
    return ret;
    
  }
  
  public void setLogFilter(Any f)
  {
    if (AnyNull.isNullInstance(f))
      handler_.setFilter(null);
    else
    {
      if (!(f instanceof AnyLogFilter))
        throw new IllegalArgumentException(f.getClass().toString() + " is not a AnyLogFilter");
      
      AnyLogFilter ff = (AnyLogFilter)f;
      handler_.setFilter(ff.getFilter());
    }
  }
  
  public void setLogFunc(Any filterF)
  {
    if (AnyNull.isNullInstance(filterF))
      handler_.setFilter(null);
    else
    {
      Call filter = AnyComponent.verifyCall(filterF);
      handler_.setFilter(new AnyLogFilter.FuncFilter(filter));
    }
  }

  public Any getLogFunc()
  {
    Any ret = null;
    Filter f = handler_.getFilter();
    if (f instanceof AnyLogFilter.FuncFilter)
    {
      AnyLogFilter.FuncFilter ff = (AnyLogFilter.FuncFilter)f;
      ret = ff.getLogFunc();
    }
    return ret;
  }

  public void setLogLevel(Any level)
  {
    Level l = AnyLogManager.toLevel(level);
    handler_.setLevel(l);
  }
  
  public Any getLogLevel()
  {
    Any l = AnyLogManager.toAny(handler_.getLevel());
    return l;
  }

  /**
   * Set this handler according to the specified properties.
   * This method resets the underlying handler, 
   * @param props
   */
  public void setLogHandler(Map props)
  {
    
  }
//  public void setLogStream(AbstractStream s)
//  {
//    if (handler_ instanceof StreamHandler)
//    {
//      OutputStream os = s.getUnderlyingOutputStream();
//      
//      if (os == null)
//        throw new AnyRuntimeException("stream is not open for output");
//      
//      //((StreamHandler)handler_).setO
//    }
//    else
//      throw new AnyRuntimeException("Not a StreamHandler");
//  }
  
  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }

  public boolean equals(Any a)
  {
    return (a instanceof AnyLogHandler) &&
         (((AnyLogHandler)a).handler_.equals(handler_));
  }
  
  protected Object getPropertyOwner(Any property)
  {
    if (wrapProps__.contains(property))
      return this;
    
    return handler_;
  }
  
  static private class FileHandler extends java.util.logging.FileHandler
  {

    public FileHandler() throws IOException, SecurityException
    {
      super();
      configure();
      // TODO Auto-generated constructor stub
    }
    
    protected void configure()
    {
    }

  }
}
