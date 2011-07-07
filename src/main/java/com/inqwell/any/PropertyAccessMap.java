/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/PropertyAccessMap.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import com.inqwell.any.beans.PropertySet;

/**
 * Provide utilities for managing and binding Java Bean properties as
 * map keys.
 * <p>
 * Objects may support access to and configuration of their state via
 * the idiom of Java Beans properties.  Such properties are typically
 * accessed by methods like 
 * <pre><code>
 * public void setSomeProperty(int value);
 * public int getSomeProperty();
 * </code></pre>
 * Inq uses reflection to discover these properties and this class provides
 * support for derived classes to make these properties available as map
 * children and therefore available to Inq script as a whole.
 * <p>
 * While property support is potentially useful to a wide variety of
 * subclasses, this class does not impose any member variable overhead
 * and derived classes may not even be maps in the true sense. This
 * means that there will always be a certain amount of work required
 * whenever this class is extended and property support used:
 * <BL>
 * <LI>
 * <code>Map.get(Any key)</code> must be implemented to at least recognise
 * the key being used to represent the
 * properties (suggest <code>this.properties__</code>) and to then
 * call <code>this.makePropertyMap()</code> in the first case. The reference
 * returned should be used on subsequent access.
 * </LI>
 * <LI>
 * <code>Map.contains(Any key)</code> must be implemented to
 * return <code>true</code> for the key used to represent the properties.
 * </LI>
 * </BL>
 * Derived classes may extend <code>PropertySet.PropertyBinding</code> and
 * override <code>getPropertyOwner</code> and <code>makePropertyBinding</code>.
 */
public abstract class PropertyAccessMap extends AbstractMap
{
  static private   java.util.Map<Class, PropertySet> propertySetMap__;
  static public    Any properties__ = null;

  static
  {
    properties__       = AbstractValue.flyweightString("properties");
		propertySetMap__   = new java.util.HashMap<Class, PropertySet>();
  }

  public PropertyAccessMap()
  {
    super();
  }
  
  public Iter createIterator () { return DegenerateIter.i__; }
  
  public boolean isEmpty() { return false; }

  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;

    return false;
  }

  /**
   * Return the object that is the owner of the property.
   * The default implementation returns <code>this</code>
   * however, if a class acts as a wrapper for the target
   * object then it may return something else.
   */
  protected Object getPropertyOwner(Any property)
  {
    return this;
  }
  
  /**
   * Fetch the PropertySet for the object that owns the given
   * property.
   */
  protected final PropertySet getPropertySet(Any property)
  {
    Object o = getPropertyOwner(property);
    return getPropertySet(o);
  }
  
  /**
   * Fetch the PropertySet for the the given object.  PropertySets are
   * cached on a class basis and so are only retrieved the first time the
   * given object's class is seen.
   */
  protected final PropertySet getPropertySet(Object o)
  {
    synchronized(propertySetMap__)
    {
      PropertySet ret = null;
      
      if ((ret = propertySetMap__.get(o.getClass())) == null)
      {
        ret = new PropertySet(o);
        propertySetMap__.put(o.getClass(), ret);
      }      
      return ret;
    }
  }

  /**
   * Make a new property binding using the given property set, binding the
   * specified property to the specified object.  The property set must be that
   * returned by <code>getPropertySet(o)</code>.
   * <p>
   * The <code>PropertySet.PropertyBinding</code> class supports the invocation
   * of the set/get methods, however this class does not make any assumptions
   * about threading implications (such as GUI thread safety, for example).
   * Derived classes may override to return their own extension
   * of <code>PropertySet.PropertyBinding</code> where there are specific
   * issues to address in how the property methods are called.
   */
  protected PropertyBinding makePropertyBinding(PropertySet p, Object o, Any property)
  {
    return p.makePropertyBinding(o, property);
  }
  
  /**
   * Create a Map implementation that is suitable for holding property bindings.
   * Derived classes should call this method when the child key they are
   * using (typically "properties") to represent the bound properties is
   * applied. They should then return this reference in subsequent requests
   * for the same child.
   * <p>
   * The Map implementation assumes that all keys (that is property names)
   * exist and Map.contains(key) always returns true.  When a property is
   * requested a new property binding is made on demand by
   * calling <code>this.makePropertyBinding()</code>, which is then held in
   * the map for subsequent use.
   */
  protected Map makePropertyMap()
  {
    return new PropertyMap(this);
  }
  
	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}

  static public class PropertyMap extends SimpleMap
	{
    /**
		 * Just to satisfy the compiler etc. These are never serialised
		 */
		private static final long serialVersionUID = 1L;
		
		private PropertyAccessMap outer_;

		public PropertyMap(PropertyAccessMap outer)
		{
		  super();
		  outer_= outer;
		}
		
    /**
     *  In this implementation a key must be a beans property name for
     *  the method to succeed.
     */
		public Any get (Any key)
    {
      if (!super.contains(key))
      {
        PropertyBinding b = makePropertyBinding(key);
        this.add(key, b);
        return b;
      }
      else
      {
        return super.get(key);
      }
    }
    
    public Any getIfContains(Any key)
    {
      Any ret = super.getIfContains(key);
      
      if (ret == null)
      {
        PropertyBinding b = makePropertyBinding(key);
        this.add(key, b);
        ret = b;
      }
      return ret;
    }
    
    /**
     * Always returns true on the basis that the key will resolve to
     * a beans property when {@link #get(Any)} is called
     */
    public boolean contains(Any key)
    {
      return true;
    }
    
    protected PropertyBinding makePropertyBinding(Any key)
    {
      PropertySet p = outer_.getPropertySet(key);
      Object o = outer_.getPropertyOwner(key);
      return outer_.makePropertyBinding(p, o, key);
    }
    
    protected PropertyAccessMap getOuter()
    {
      return outer_;
    }
	}
}
