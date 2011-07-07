/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/identity/AnyMapEgDecor.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.identity;

import com.inqwell.any.*;
import com.inqwell.any.Process;

/**
 * Provides a decorator to ensure that the <code>hashCode()</code>
 * and <code>equals()</code> methods apply such that different instances
 * containing the same underlying Any yield the same result.
 */
public final class AnyMapEgDecor implements Map,
																						EventGenerator,
																						HasIdentity,
																						Cloneable
{
	private Map instance_;
	private EventGenerator eg_;
	
	/**
	 * Wrap a Map to bestow identity semantics.  See also Identity.bestowIdentity().
	 * Only these functions can be used to set up identity semantics, hence this
	 * constructor is restricted to package access.
	 */
	public AnyMapEgDecor (Map m)
	{
    if (!(m instanceof EventGenerator))
      throw new IllegalArgumentException ();
		instance_ = m;
		eg_       = (EventGenerator)m;
	}
	
	private AnyMapEgDecor (Any a)
	{
    if (!(a instanceof Map))
      throw new IllegalArgumentException ();
    if (!(a instanceof EventGenerator))
      throw new IllegalArgumentException ();
		instance_ = (Map)a;
		eg_       = (EventGenerator)a;
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
    return new AnyMapEgDecor(instance_.buildNew(a));
  }

  public Any cloneAny ()
  {
    Any a = null;
    
    a = instance_.cloneAny();
    
    return new AnyMapEgDecor(a);
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
    if (o instanceof AnyMapEgDecor)
    {
			AnyMapEgDecor hd = (AnyMapEgDecor)o;
			return (instance_ == hd.instance_);
		}
    return false;
  }

  public boolean equals(Any a)
  {
    if (a instanceof AnyMapEgDecor)
    {
			AnyMapEgDecor hd = (AnyMapEgDecor)a;
			return (instance_ == hd.instance_);
		}
    return false;
  }

  public int entries() { return instance_.entries(); }

  public boolean containsAll (Composite c)
  {
    return instance_.containsAll(c);
  }
  
  public boolean containsAny (Composite c)
  {
    return instance_.containsAny(c);
  }

  public boolean contains (Any key) {return instance_.contains(key); }
  public boolean containsValue (Any value) {return instance_.containsValue(value); }

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
		return new AnyMapEgDecor (instance_.shallowCopy());
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

  public Any getUniqueKey()
  {
    return instance_.getUniqueKey();
  }
  
  public void setUniqueKey(Any keyVal)
  {
		instance_.setUniqueKey(keyVal);
  }
  
  public short getPrivilegeLevel(Any access, Any key)
  {
    return instance_.getPrivilegeLevel(access, key);
  }
  
  public void setPrivilegeLevels(Map levels, Any key, boolean merge)
  {
    instance_.setPrivilegeLevels(levels, key, merge);
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
  
  public void removeInParent()
  {
		instance_.removeInParent();
	}
	
  public void removeAll(Composite c)
  {
		instance_.removeAll(c);
  }

  public void retainAll(Composite c)
  {
		instance_.retainAll(c);
  }

  public void markForDelete(Any id)
  {
  	instance_.markForDelete(id);
  }
  
  public boolean isDeleteMarked(Any id)
  {
    return instance_.isDeleteMarked(id);
  }
  
  public void fireEvent (Event e) throws AnyException
  {
		eg_.fireEvent(e);
  }
  
  public void addEventListener (EventListener l, Any eventParam)
  {
		eg_.addEventListener(l, eventParam);
  }

  public void addEventListener (EventListener l)
  {
		eg_.addEventListener(l);
  }
  
  public void removeEventListener (EventListener l)
  {
		eg_.removeEventListener(l);
  }

  public void removeAllListeners ()
  {
		eg_.removeAllListeners();
  }

  public Array getGeneratedEventTypes()
  {
		return eg_.getGeneratedEventTypes();
  }
  
  public Event makeEvent(Any eventType)
  {
		Event e = eg_.makeEvent(eventType);
		if (e != null)
		{
			e.reset(this);
//			if (e.getContext() == eg_)
//				e.setContext(this);
		}

		return e;
	}
	
  public boolean raiseAgainstChildren(Event e)
  {
    return eg_.raiseAgainstChildren(e);
  }

  public String toString()
	{
		return "EG identity " + instance_.toString();
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
