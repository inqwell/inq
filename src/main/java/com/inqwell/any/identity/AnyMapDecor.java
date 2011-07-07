/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/identity/AnyMapDecor.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.identity;

import com.inqwell.any.Any;
import com.inqwell.any.Array;
import com.inqwell.any.Composite;
import com.inqwell.any.Descriptor;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.StringI;
import com.inqwell.any.Visitor;

/**
 * Provides a decorator to ensure that the <code>hashCode()</code>
 * and <code>equals()</code> methods apply such that different instances
 * containing the same underlying Any yield the same result.
 */
public final class AnyMapDecor implements Map,
																					HasIdentity,
																					Cloneable
{
	private Map instance_;
	
	/**
	 * Wrap a Map to bestow identity semantics.  See also Identity.bestowIdentity().
	 * Only these functions can be used to set up identity semantics, hence this
	 * constructor is restricted to package access.
	 */
  public AnyMapDecor (Map m)
	{
    if (!(m instanceof Map))
      throw new IllegalArgumentException ();
		instance_ = m;
	}
	
	private AnyMapDecor (Any a)
	{
    if (!(a instanceof Map))
      throw new IllegalArgumentException ();
		instance_ = (Map)a;
	}
	
  public Iter createIterator () { return instance_.createIterator(); }

  public void accept (Visitor v)
  {
    v.visitMap(this);
  }

  public Any copyFrom (Any a)
  {
    if (a != this)
      instance_.copyFrom(a);

    return this;
  }


  public Any buildNew (Any a)
  {
    return new AnyMapDecor(instance_.buildNew(a));
  }

  public Any cloneAny ()
  {
    Any a = null;
    
    a = instance_.cloneAny();
    
    return new AnyMapDecor(a);
	}
  
  public boolean isTransactional()
  {
		return instance_.isTransactional();
  }

  public boolean isConst()
  {
    return instance_.isConst();
  }
    
  public Any bestowConstness()
  {
    return this;
  }
  
  public void setTransactional(boolean isTransactional)
  {
		instance_.setTransactional(isTransactional);
  }
  
  public int identity() { return instance_.identity(); }

  public boolean hasIdentity()
	{
		return true;
	}

  public Map bestowIdentity()
	{
    return this;
	}

	public int hashCode() { return instance_.identity(); }
	
  public boolean equals(Object o)
  {
    if (o instanceof AnyMapDecor)
    {
			AnyMapDecor hd = (AnyMapDecor)o;
			return (instance_ == hd.instance_);
		}
    return false;
  }

  public boolean equals(Any a)
  {
    if (a instanceof AnyMapDecor)
    {
			AnyMapDecor hd = (AnyMapDecor)a;
			return (instance_ == hd.instance_);
		}
    return false;
  }

  public int entries() { return instance_.entries(); }

  public boolean contains (Any key) {return instance_.contains(key); }
  public boolean containsValue (Any value) {return instance_.containsValue(value); }

  public boolean containsAll (Composite c)
  {
    return instance_.containsAll(c);
  }
  
  public boolean containsAny (Composite c)
  {
    return instance_.containsAny(c);
  }

  public boolean hasKeys (Array keys) {return instance_.hasKeys(keys); }

  public Array keys () {return instance_.keys(); }
  
  public Any getMapKey(Any key) { return instance_.getMapKey(key); }
  
  public void empty() { instance_.empty(); }

  public boolean isEmpty() { return instance_.isEmpty(); }
  
	public void add (Any key, Any value) { instance_.add (key, value); }

	public void add (StringI keyAndValue) { instance_.add (keyAndValue); }

  public void add (Any element) { instance_.add (element); }

  public Iter createKeysIterator () {return instance_.createKeysIterator(); }

  public Iter createConcurrentSafeKeysIterator() { return instance_.createConcurrentSafeKeysIterator(); }
  
  public Any get (Any key) { return instance_.get(key); }

  public Any getIfContains (Any key) { return instance_.getIfContains(key); }

  public java.util.Map getMap () { return instance_.getMap(); }

  public Any remove (Any key) { return instance_.remove(key); }

  public void replaceItem (Any key, Any item) { instance_.replaceItem(key, item); }

  public void replaceValue (Any key, Any item) { instance_.replaceValue(key, item); }

  public Map shallowCopy()
  {
		return new AnyMapDecor (instance_.shallowCopy());
	}

  public Composite shallowCopyOf()
  {
    return shallowCopy();
  }

  public Descriptor getDescriptor()
  {
		return instance_.getDescriptor();
  }

  public void setDescriptor(Descriptor d)
  {
		instance_.setDescriptor(d);
  }
  
  public Composite getParentAny()
  {
    return instance_.getParentAny();
	}
  
  public Process getProcess()
  {
    return instance_.getProcess();
  }
  
  public Any getNameInParent()
  {
    return instance_.getNameInParent();
  }
  
  public Any getPath()
  {
    return instance_.getPath();
  }
  
  public boolean isParentable()
  {
    return instance_.isParentable();
	}
  
  public void setParent(Composite parent)
  {
	  instance_.setParent(parent);
  }
  
  public short getPrivilegeLevel(Any access, Any key)
  {
    return instance_.getPrivilegeLevel(access, key);
  }
  
  public void setPrivilegeLevels(Map levels, Any key, boolean merge)
  {
    instance_.setPrivilegeLevels(levels, key, merge);
  }
  
  public Any getUniqueKey()
  {
    return instance_.getUniqueKey();
  }
  
  public void setUniqueKey(Any keyVal)
  {
		instance_.setUniqueKey(keyVal);
  }
  
  public Any getNodeSet()
  {
    return instance_.getNodeSet();
  }
  
  public void setNodeSet(Any nodeSet)
  {
	  instance_.setNodeSet(nodeSet);
  }

  public void setPropertyBean(Object bean)
  {
    instance_.setPropertyBean(bean);
  }

  public Object getPropertyBean()
  {
    return instance_.getPropertyBean();
  }
  
  public void setContext(Any context)
  {
	  instance_.setContext(context);
  }
  
  public void setAux(Any aux)
  {
    instance_.setAux(aux);
  }
  
  public Any getAux()
  {
    return instance_.getAux();
  }

  public void removeInParent()
  {
		instance_.removeInParent();
	}
	
  public void markForDelete(Any id)
  {
  	instance_.markForDelete(id);
  }
  
  public boolean isDeleteMarked(Any id)
  {
    return instance_.isDeleteMarked(id);
  }
  
  public void removeAll(Composite c)
  {
		instance_.removeAll(c);
  }

  public void retainAll(Composite c)
  {
		instance_.retainAll(c);
  }
  
  public String toString()
  {
    return "Identity " + instance_.toString();
  }
  
  /**
   * This method is public to provide access to the decorated object
   * for the purposes of serialisation only.  It should NOT be called
   * for any other use.
   */
  public Map getInstance()
  {
    return instance_;
  }
}
