/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.inqwell.any.identity.AnyMapDecor;

/**
 * A subclass of {@link java.util.logging.LogManager} so logging relates to Inq
 * scripts.
 * <p>
 * If, via configuration of the Java logging system, the LogManager is
 * replaced with {@link AnyLogManager} then instances of this class
 * will be created as the logging system is initialised from its properties.
 */
public class AnyLogManager extends LogManager implements Map, Cloneable
{
//public static IntI LOG_ALL     = (IntI)AbstractValue.flyweightConst(new ConstInt(Level.ALL.intValue()+1));
//public static IntI LOG_CONFIG  = (IntI)AbstractValue.flyweightConst(new ConstInt(Level.CONFIG.intValue()));
//public static IntI LOG_FINE    = (IntI)AbstractValue.flyweightConst(new ConstInt(Level.FINE.intValue()));
//public static IntI LOG_FINER   = (IntI)AbstractValue.flyweightConst(new ConstInt(Level.FINER.intValue()));
//public static IntI LOG_FINEST  = (IntI)AbstractValue.flyweightConst(new ConstInt(Level.FINEST.intValue()));
//public static IntI LOG_INFO    = (IntI)AbstractValue.flyweightConst(new ConstInt(Level.INFO.intValue()));
//public static IntI LOG_OFF     = (IntI)AbstractValue.flyweightConst(new ConstInt(Level.OFF.intValue()));
//public static IntI LOG_SEVERE  = (IntI)AbstractValue.flyweightConst(new ConstInt(Level.SEVERE.intValue()));
//public static IntI LOG_WARNING = (IntI)AbstractValue.flyweightConst(new ConstInt(Level.WARNING.intValue()));

  // Slightly hacky but makes toString look like the symbol.
  public static IntI LOG_ALL     = new ConstInt(Level.ALL.intValue()+1)   { public String toString() { return "ALL"; } };
  public static IntI LOG_CONFIG  = new ConstInt(Level.CONFIG.intValue())  { public String toString() { return "CONFIG"; } };
  public static IntI LOG_FINE    = new ConstInt(Level.FINE.intValue())    { public String toString() { return "FINE"; } };
  public static IntI LOG_FINER   = new ConstInt(Level.FINER.intValue())   { public String toString() { return "FINER"; } };
  public static IntI LOG_FINEST  = new ConstInt(Level.FINEST.intValue())  { public String toString() { return "FINEST"; } };
  public static IntI LOG_INFO    = new ConstInt(Level.INFO.intValue())    { public String toString() { return "INFO"; } };
  public static IntI LOG_OFF     = new ConstInt(Level.OFF.intValue())     { public String toString() { return "OFF"; } };
  public static IntI LOG_SEVERE  = new ConstInt(Level.SEVERE.intValue())  { public String toString() { return "SEVERE"; } };
  public static IntI LOG_WARNING = new ConstInt(Level.WARNING.intValue()) { public String toString() { return "WARNING"; } };

//  public static Any LOG_ALL     = AbstractValue.flyweightString("ALL");
//  public static Any LOG_CONFIG  = AbstractValue.flyweightString("CONFIG");
//  public static Any LOG_FINE    = AbstractValue.flyweightString("FINE");
//  public static Any LOG_FINER   = AbstractValue.flyweightString("FINER");
//  public static Any LOG_FINEST  = AbstractValue.flyweightString("FINEST");
//  public static Any LOG_INFO    = AbstractValue.flyweightString("INFO");
//  public static Any LOG_OFF     = AbstractValue.flyweightString("OFF");
//  public static Any LOG_SEVERE  = AbstractValue.flyweightString("SEVERE");
//  public static Any LOG_WARNING = AbstractValue.flyweightString("WARNING");
  
  private   DefaultPropertyAccessMap propertyMap_;
  
  private   String                   lastName_;
  
  // Hold loggers as strong references until they are first
  // requested via getAnyLogger(). The JVM can GC a logger held
  // within java.util.logging.LogManager before it is even used
  // because it holds them as weak references. They would get
  // re-created when using Logger.getLogger() but surely this
  // is not the intention - code would be expected to hold a
  // reference to 
  //private   HashMap<String, Logger>  loggers_ = new HashMap<String, Logger>();
  
  public static Level toLevel(Any l)
  {
    if (l.equals(LOG_ALL))
      return Level.ALL;
    else if (l.equals(LOG_CONFIG))
      return Level.CONFIG;
    else if (l.equals(LOG_FINE))
      return Level.FINE;
    else if (l.equals(LOG_FINER))
      return Level.FINER;
    else if (l.equals(LOG_FINEST))
      return Level.FINEST;
    else if (l.equals(LOG_INFO))
      return Level.INFO;
    else if (l.equals(LOG_OFF))
      return Level.OFF;
    else if (l.equals(LOG_SEVERE))
      return Level.SEVERE;
    else if (l.equals(LOG_WARNING))
      return Level.WARNING;
    else
      throw new IllegalArgumentException("Unknown level " + l);
  }
  
  public static Any toAny(Level l)
  {
    if (l.equals(Level.ALL))
      return LOG_ALL;
    else if (l.equals(Level.CONFIG))
      return LOG_CONFIG;
    else if (l.equals(Level.FINE))
      return LOG_FINE;
    else if (l.equals(Level.FINER))
      return LOG_FINER;
    else if (l.equals(Level.FINEST))
      return LOG_FINEST;
    else if (l.equals(Level.INFO))
      return LOG_INFO;
    else if (l.equals(Level.OFF))
      return LOG_OFF;
    else if (l.equals(Level.SEVERE))
      return LOG_SEVERE;
    else if (l.equals(Level.WARNING))
      return LOG_WARNING;
    else
      throw new IllegalArgumentException("Unknown level " + l);
  }

  public AnyLogManager()
  {
  }

  /**
   * Returns a {@link AnyLogger} instance.
   * <p>
   * This method override does not quite honour the base class contract in
   * that it will always return a logger for the given name, creating
   * an {@link AnyLogger} if one does not already exist. This is to
   * ensure that AnyLoggers are always used (see JDK
   * source LogManager.demandLogger - java.util.logging does not cover
   * itself in glory when it comes to extending the various classes). 
   */
  public synchronized Logger getLogger(String name)
  {
    lastName_ = name;
    
    Logger l = super.getLogger(name);
    if (l == null)
    {
      l = new AnyLogger(name, null);
      //loggers_.put(name, l);
      this.addLogger(l);
    }
    
    lastName_ = null;
    
    return l;
  }
  
  /**
   * A slightly messy override as being the only (easy) way we can enrich the
   * property configuration of log handlers. Many of a handler's properties
   * can only be configured at the class level. For example, you cannot
   * specify the pattern for the FileHandler of a specific logger.
   * To get round this we observe that the getLogger(String name) method,
   * also overridden in this class, is above us in the call stack and we
   * remember the name of the last logger that was requested. When
   * looking for properties we check for various well-known names
   * such as <code>java.util.logging.FileHandler.<b>pattern</b></code> and
   * prepend the logger name to this.
   */
  public String getProperty(String name)
  {
    String s = loggerPropertyName(name);
    if (s != name)
    {
      s = super.getProperty(s);
      if (s == null)
        s = super.getProperty(name);
    }
    else
      s = super.getProperty(name);
    
    if (s != null)
      s = expandPropValue(s);
    
    return s;
  }
  
  /*
  // Properties
  public Map getLoggers()
  {
    Map ret = AbstractComposite.simpleMap();
    
    return ret;
  }
  */

//  public AnyLogger getAnyLogger(String name)
//  {
//    System.out.println("getting " + name);
//    
//    AnyLogger ret = null;
//    
//    // Use super see AnyLogManager.getLogger(String) for further details.
//    Logger l = super.getLogger(name);
//    if (l instanceof AnyLogger)
//      ret = (AnyLogger)l;
//if (l != null)
//  System.out.println(l.getClass().toString());
//    return ret;
//  }
  
  // If the property name has a well-known suffix return the
  // current logger name (given by lastName_) with this suffix
  // otherwise return the string unchanged,
  private String loggerPropertyName(String name)
  {
    // Logger initialisation may be processing global handlers
    if (lastName_ == null)
      return name;
    
    // Logger initialisation may be processing parent handlers
    if (!name.startsWith("java.util.logging"))
      return name;
    
    if (name.endsWith(".pattern"))
      return lastName_ + ".pattern";
    else if (name.endsWith(".limit"))
      return  lastName_ + ".limit";
    else if (name.endsWith(".count"))
      return  lastName_ + ".count";
    else if (name.endsWith(".append"))
      return  lastName_ + ".append";
    else if (name.endsWith(".formatter"))
      return lastName_ + ".formatter";
    else if (name.endsWith(".encoding"))
      return lastName_ + ".encoding";
    else if (name.endsWith(".filter"))
      return lastName_ + ".filter";
    else if (name.endsWith(".port"))
      return lastName_ + ".port";
    else if (name.endsWith(".host"))
      return lastName_ + ".host";
   else
      return name;
  }

  // Substitute strings of the form ${foo} with the value of the
  // system property foo. If the property is undefined no substitution
  // takes place. Substitution is not recursive nor is it nested.
  private String expandPropValue(String value)
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
          // Try the logger properties first
          String propVal = getProperty(propName);
          // Then if not defined there go to the System properties
          if (propVal == null)
            propVal = System.getProperty(propName);
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
      propertyMap_ = new LogManagerPropertyAccess();

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
      propertyMap_ = new LogManagerPropertyAccess();

    return propertyMap_.getIfContains(key);
  }

  public java.util.Map getMap()
  {
    throw new UnsupportedOperationException();
  }

  public short getPrivilegeLevel(Any access, Any key)
  {
    if (propertyMap_ == null)
      propertyMap_ = new LogManagerPropertyAccess();

    return propertyMap_.getPrivilegeLevel(access, key);
  }

  public Object getPropertyBean()
  {
    throw new UnsupportedOperationException();
  }

  public Any getUniqueKey()
  {
    if (propertyMap_ == null)
      propertyMap_ = new LogManagerPropertyAccess();

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
      propertyMap_ = new LogManagerPropertyAccess();

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
    if (propertyMap_ == null)
      propertyMap_ = new LogManagerPropertyAccess();

    propertyMap_.setUniqueKey(keyVal);
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

  public Any getPath(Any to)
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
    throw new UnsupportedOperationException();
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
    throw new IllegalArgumentException ("buildNew() not supported");
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

  public Iter createIterator () {return DegenerateIter.i__;}

  public boolean isConst()
  {
    return false;
  }

  public boolean isTransactional()
  {
    return false;
  }
  
  // End Map stuff

  private class LogManagerPropertyAccess extends DefaultPropertyAccessMap
  {
    protected Object getPropertyOwner(Any property)
    {
      return AnyLogManager.this;
    }
    
  }
}
