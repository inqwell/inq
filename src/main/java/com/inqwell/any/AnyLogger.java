/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any;

import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.inqwell.any.Call.CallStackEntry;
import com.inqwell.any.client.AnyComponent;
import com.inqwell.any.identity.AnyMapDecor;

/**
 * A subclass of {@link java.util.logging.Logger} so logging relates to Inq
 * scripts.
 * <p>
 * If, via configuration of the Java logging system, the LogManager is
 * replaced with {@link AnyLogManager} then instances of this class
 * will be created as the logging system is initialised from its properties.
 */
public class AnyLogger extends Logger implements Map, Cloneable
{
  // Property access to this logger
  private DefaultPropertyAccessMap propertyMap_;
  
  public AnyLogger(String name, String resourceBundleName)
  {
    super(name, resourceBundleName);
  }

  /**
   * Intercepts logging to provide Inq script details.
   * <p>
   * There is no point in the JDK classes providing the class and method names
   * in the {@link LogRecord} since these will always be of the invocation
   * of LogMessage.exec(Any a). By arranging with {@link AnyLogManager} to log
   * via this class the function (or service) name, the URL from which it
   * was parsed and the current line number are placed into the LogRecord 
   */
  public void log(LogRecord record)
  {
    Transaction t = Globals.getProcessForCurrentThread().getTransaction();
    
    if (!t.getCallStack().isEmpty())
    {
      CallStackEntry se = (CallStackEntry)t.getCallStack().peek();
      StringBuffer sb = new StringBuffer(se.getSourceUrl().toString());
      sb.append(':');
      sb.append(t.getLineNumber());
      
      record.setSourceClassName(sb.toString());
      
      record.setSourceMethodName(se.getFQName().toString());
    }
    
    super.log(record);
  }
  
  // Properties
  
  public Array getLogHandlers()
  {
    Array ret = AbstractComposite.array();
    
    Handler[] handlers = getHandlers();
    
    if (handlers.length > 0)
    {
      for (int i = 0; i < handlers.length; i++)
        ret.add(new AnyLogHandler(handlers[i]));
    }
    
    return ret;
  }
  
  public void setLogFilter(Any f)
  {
    if (AnyNull.isNullInstance(f))
      setFilter(null);
    else
    {
      if (!(f instanceof AnyLogFilter))
        throw new IllegalArgumentException(f.getClass().toString() + " is not a AnyLogFilter");
      
      AnyLogFilter ff = (AnyLogFilter)f;
      setFilter(ff.getFilter());
    }
  }
  
  public Any getLogFilter()
  {
    Any ret = null;
    
    Filter f = getFilter();
    if (f != null)
      ret = new AnyLogFilter(f);
    
    return ret;
  }
  
  public void setLogLevel(Any l)
  {
    Level level = AnyLogManager.toLevel(l);
    setLevel(level);
  }
  
  public Any getLogLevel()
  {
    return AnyLogManager.toAny(getLevel());
  }
  
  public void setLogFunc(Any filterF)
  {
    if (AnyNull.isNullInstance(filterF))
      setFilter(null);
    else
    {
      Call filter = AnyComponent.verifyCall(filterF);
      setFilter(new AnyLogFilter.FuncFilter(filter));
    }
  }

  public Any getLogFunc()
  {
    Any ret = null;
    Filter f = getFilter();
    if (f instanceof AnyLogFilter.FuncFilter)
    {
      AnyLogFilter.FuncFilter ff = (AnyLogFilter.FuncFilter)f;
      ret = ff.getLogFunc();
    }
    return ret;
  }

  // Start Map stuff

  public void add(Any key, Any value)
  {
    throw new UnsupportedOperationException();
  }

  public void add(StringI keyAndValue)
  {
    throw new UnsupportedOperationException();
  }

  public Map bestowIdentity()
  {
    return new AnyMapDecor(this);
  }

  public boolean contains(Any key)
  {
    if (key.equals(PropertyAccessMap.properties__))
      return true;

    return false;
  }

  public boolean containsValue(Any value)
  {
    throw new UnsupportedOperationException();
  }

  public Iter createConcurrentSafeKeysIterator()
  {
    throw new UnsupportedOperationException();
  }

  public Iter createKeysIterator()
  {
    throw new UnsupportedOperationException();
  }

  public Any get(Any key)
  {
    if (propertyMap_ == null)
      propertyMap_ = new LoggerPropertyAccess();

    return propertyMap_.get(key);
  }

  public Any getAux()
  {
    throw new UnsupportedOperationException();
  }

  public Descriptor getDescriptor()
  {
    return Descriptor.degenerateDescriptor__;
  }

  public Any getIfContains(Any key)
  {
    if (propertyMap_ == null)
      propertyMap_ = new LoggerPropertyAccess();

    return propertyMap_.getIfContains(key);
  }

  public java.util.Map getMap()
  {
    throw new UnsupportedOperationException();
  }

  public short getPrivilegeLevel(Any access, Any key)
  {
    if (propertyMap_ == null)
      propertyMap_ = new LoggerPropertyAccess();

    return propertyMap_.getPrivilegeLevel(access, key);
  }

  public Object getPropertyBean()
  {
    throw new UnsupportedOperationException();
  }

  public Any getUniqueKey()
  {
    if (propertyMap_ == null)
      propertyMap_ = new LoggerPropertyAccess();

    return propertyMap_.getUniqueKey();
  }

  public boolean hasKeys(Array keys)
  {
    throw new UnsupportedOperationException();
  }

  public Array keys()
  {
    throw new UnsupportedOperationException();
  }

  public Any getMapKey(Any key)
  {
    throw new UnsupportedOperationException();
  }
  
  public Any remove(Any key)
  {
    throw new UnsupportedOperationException();
  }

  public void replaceItem(Any key, Any item)
  {
    throw new UnsupportedOperationException();
  }

  public void replaceValue(Any key, Any value)
  {
    throw new UnsupportedOperationException();
  }

  public void setAux(Any aux)
  {
    throw new UnsupportedOperationException();
  }

  public void setContext(Any context)
  {
    throw new UnsupportedOperationException();
  }

  public void setDescriptor(Descriptor d)
  {
    throw new UnsupportedOperationException();
  }

  public void setPrivilegeLevels(Map levels, Any key, boolean merge)
  {
    if (propertyMap_ == null)
      propertyMap_ = new LoggerPropertyAccess();

    propertyMap_.setPrivilegeLevels(levels, key, merge);
  }

  public void setPropertyBean(Object bean)
  {
    throw new UnsupportedOperationException();
  }

  public void setTransactional(boolean isTransactional)
  {
    throw new UnsupportedOperationException();
  }

  public void setUniqueKey(Any keyVal)
  {
    throw new UnsupportedOperationException();
  }

  public Map shallowCopy()
  {
    throw new UnsupportedOperationException();
  }

  public void add(Any element)
  {
    throw new UnsupportedOperationException();
  }

  public boolean containsAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  public boolean containsAny(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  public void empty()
  {
    throw new UnsupportedOperationException();
  }

  public int entries()
  {
    return 1;
  }

  public boolean equals(Any a)
  {
    return this == a;
  }

  public Any getNameInParent()
  {
    throw new UnsupportedOperationException();
  }

  public Composite getParentAny()
  {
    throw new UnsupportedOperationException();
  }

  public Any getNodeSet()
  {
    return null;
  }

  public Any getPath()
  {
    throw new UnsupportedOperationException();
  }

  public Process getProcess()
  {
    return null;
  }

  public boolean hasIdentity()
  {
    return false;
  }

  public int identity()
  {
    return System.identityHashCode(this);
  }

  public boolean isDeleteMarked(Any id)
  {
    throw new UnsupportedOperationException();
  }

  public boolean isEmpty()
  {
    return false;
  }

  public boolean isParentable()
  {
    return false;
  }

  public void markForDelete(Any id)
  {
    throw new UnsupportedOperationException();
  }

  public void removeAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  public void removeInParent()
  {
  }

  public void retainAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  public void setNodeSet(Any nodeSet)
  {
    throw new UnsupportedOperationException();
  }

  public void setParent(Composite parent)
  {
  }

  public Composite shallowCopyOf()
  {
    throw new UnsupportedOperationException();
  }

  public void accept(Visitor v)
  {
    v.visitMap(this);
  }

  public Any bestowConstness()
  {
    return this;
  }

  public Any buildNew(Any a)
  {
    throw new IllegalArgumentException ("");
  }

  public Any cloneAny()
  {
    Any a = null;

    try
    {
      a = (Any)clone();
    }
    catch (Exception e)
    {
      throw (new IllegalArgumentException ("cloneAny exception: " +
                                           getClass().getName()));
    }
    return a;
  }

  public Any copyFrom(Any a)
  {
    throw new IllegalArgumentException ("copyFrom() not supported");
  }

  public Iter createIterator ()
  {
    return DegenerateIter.i__;
  }

  public boolean isConst()
  {
    return false;
  }

  public boolean isTransactional()
  {
    return false;
  }
  
  // End Map stuff
  
  public String toString()
  {
    return getName();
  }

  private class LoggerPropertyAccess extends DefaultPropertyAccessMap
  {
    protected Object getPropertyOwner(Any property)
    {
      return AnyLogger.this;
    }
    
  }
}
