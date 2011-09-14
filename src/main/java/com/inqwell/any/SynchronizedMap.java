/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * A synchronizing wrapper for any class implementing the Map interface.
 * Merely a decorator adding synchronization for those methods which change
 * the structure of the underlying collection.
 * <p>
 * <b>Note:</b> Iteration over the underlying queue must take place within
 * a synchronized block on 'this' to be thread safe.
 */
public class SynchronizedMap implements Map,
																        Cloneable
{
  private Map m_;
  
  public SynchronizedMap (Map m)
  {
    m_ = m;
  }
  
  /**
	 * The <code>createIterator</code> and <code>createKeysIterator</code> methods,
	 * in fact, pierce the encapsulation of the contained <code>Map</code> object
	 * in their current implementation, in that methods invoked within the
	 * iterator instance operate on the contained Map.  Such invocations are
	 * therefore bypassing their synchronized equivalents.  This is, however,
	 * acceptable insofar as iteration over a Map should always be done within
	 * a synchronized block
	 */
  public synchronized Iter createIterator ()
  {
  	return m_.createIterator();
  }

  public synchronized Iter createKeysIterator ()
  {
  	return m_.createKeysIterator();
  }

  public synchronized Iter createConcurrentSafeKeysIterator()
  {
    return m_.createConcurrentSafeKeysIterator();
  }
  
  public synchronized void accept (Visitor v)
  {
    v.visitMap(this);
  }
  
  public synchronized Any copyFrom (Any a)
  {
    return m_.copyFrom(a);
  }

  public synchronized Any buildNew (Any a)
  {
    return m_.buildNew(a);
  }
  
  public synchronized Object clone () throws CloneNotSupportedException
  {
    SynchronizedMap m = (SynchronizedMap)super.clone();
    m.m_ = (Map)m_.cloneAny();
    return m;
  }

  public synchronized Map shallowCopy()
  {
    Map newMap = (Map)buildNew(null);
    
    Iter i = createKeysIterator();
    while (i.hasNext())
    {
      Any key = i.next();
      newMap.add (key, get(key));
    }
    return newMap;
  }

  public synchronized Composite shallowCopyOf()
  {
    return shallowCopy();
  }

  public synchronized final Any cloneAny ()
  {
    Any a = null;

    try
    {
      a = (Any)clone();
    }
    catch (CloneNotSupportedException e)
    {
      throw (new IllegalArgumentException ("cloneAny exception: " +
                                           getClass().getName()));
    }
    return a;
  }

  public synchronized int identity()
  {
		return super.hashCode();
	}
  
  public synchronized boolean hasIdentity()
	{
		return false;
	}

  public synchronized Map bestowIdentity()
	{
    throw new IllegalArgumentException ("bestowIdentity() not supported");
	}

  public synchronized int hashCode()
  {
		return m_.hashCode();
	}
	
  public synchronized boolean isTransactional()
  {
		return m_.isTransactional();
  }

  public synchronized boolean isConst()
  {
		return m_.isConst();
  }
    
  public synchronized Any bestowConstness()
  {
    return this;
  }
  
  public synchronized void setTransactional(boolean isTransactional)
  {
		m_.setTransactional(isTransactional);
  }
  
  public synchronized int entries()
  {
    return m_.entries();
  }

  public synchronized boolean equals (Any a)
  {
		synchronized (a)
		{
			return m_.equals(a);
		}
  }

  public synchronized boolean contains (Any a)
  {
    return m_.contains(a);
  }
  
  public synchronized boolean containsValue (Any value)
  {
    return m_.containsValue(value);
  }
 
  public synchronized boolean containsAll (Composite c)
  {
    return m_.containsAll(c);
  }
  
  public synchronized boolean containsAny (Composite c)
  {
    return m_.containsAny(c);
  }

  public synchronized void removeAll(Composite c)
  {
  	m_.removeAll(c);
  }

  public synchronized void retainAll(Composite c)
  {
  	m_.retainAll(c);
  }
  
  public synchronized boolean hasKeys (Array keys)
  {
    return m_.hasKeys(keys);
  }
  
  public synchronized Array keys () {return m_.keys(); }
  
  public synchronized Any getMapKey(Any key) { return m_.getMapKey(key); }

  public synchronized void empty()
  {
    m_.empty();
  }
  
  public synchronized boolean isEmpty()
  {
    return m_.isEmpty();
  }

  public synchronized void add (Any key, Any value)
  {
		m_.add (key, value);
  }
  
  public synchronized void add (StringI keyAndValue)
  {
		m_.add (keyAndValue);
  }

  public synchronized void add (Any element)
  {
		m_.add (element);
  }

  public synchronized void replaceItem (Any key, Any item)
  {
    m_.replaceItem(key, item);
  }
  
  public synchronized void replaceValue (Any key, Any value)
  {
    m_.replaceValue(key, value);
  }
  
  public synchronized Any remove (Any key)
  {
    return m_.remove(key);
  }

  public synchronized void markForDelete (Any key)
  {
    m_.markForDelete(key);
  }

  public synchronized boolean isDeleteMarked(Any id)
  {
    return m_.isDeleteMarked(id);
  }
  
  public synchronized Any get (Any key)
  {
    return m_.get(key);
  }
  
  public synchronized Any getIfContains (Any key)
  {
    return m_.getIfContains(key);
  }

  public synchronized java.util.Map getMap ()
  {
    return m_.getMap();
  }

  public synchronized Descriptor getDescriptor()
  {
		return m_.getDescriptor();
  }

  public synchronized void setDescriptor(Descriptor d)
  {
	  m_.setDescriptor(d);
  }

  public synchronized Composite getParentAny()
  {
    return m_.getParentAny();
	}
  
  public synchronized Process getProcess()
  {
    return m_.getProcess();
  }
  
  public synchronized Any getNameInParent()
  {
    return m_.getNameInParent();
  }
  
  public synchronized Any getPath(Any to)
  {
    return m_.getPath(to);
  }
  
  public synchronized boolean isParentable()
  {
    return m_.isParentable();
	}
  
  public synchronized void setParent(Composite parent)
  {
	  m_.setParent(parent);
  }
  
  public synchronized void setContext(Any context)
  {
	  m_.setContext(context);
  }
  
  public void setAux(Any aux)
  {
    m_.setAux(aux);
  }
  
  public Any getAux()
  {
    return m_.getAux();
  }

  public synchronized Any getUniqueKey()
  {
    return m_.getUniqueKey();
  }
  
  public synchronized void setUniqueKey(Any keyVal)
  {
		m_.setUniqueKey(keyVal);
  }
  
  public short getPrivilegeLevel(Any access, Any key)
  {
    return m_.getPrivilegeLevel(access, key);
  }
  
  public synchronized void setPrivilegeLevels(Map levels, Any key, boolean merge)
  {
    m_.setPrivilegeLevels(levels, key, merge);
  }
  
  public synchronized Any getNodeSet()
  {
    return m_.getNodeSet();
  }
  
  public synchronized void setNodeSet(Any nodeSet)
  {
	  m_.setNodeSet(nodeSet);
  }

  public synchronized void setPropertyBean(Object bean)
  {
    m_.setPropertyBean(bean);
  }

  public synchronized Object getPropertyBean()
  {
    return m_.getPropertyBean();
  }
  
  public synchronized void removeInParent()
  {
		m_.removeInParent();
	}

}
