/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import com.inqwell.any.identity.AnyMapDecor;

/**
 * Provides a decorator to ensure that the <code>hashCode()</code>
 * and <code>equals()</code> methods apply such that different instances
 * containing the same underlying Any yield the same result.
 */
public final class AnyMapConstDecor implements Map,
                                               Cloneable
{
  private Map instance_;
  
  /**
   * Wrap a Map to bestow identity semantics.  See also Identity.bestowIdentity().
   * Only these functions can be used to set up identity semantics, hence this
   * constructor is restricted to package access.
   */
  public AnyMapConstDecor (Map m)
  {
    if (!(m instanceof Map))
      throw new IllegalArgumentException ();
    instance_ = m;
  }
  
  private AnyMapConstDecor (Any a)
  {
    if (!(a instanceof Map))
      throw new IllegalArgumentException ();
    instance_ = (Map)a;
  }
  
  public Iter createIterator ()
  {
    return new ConstIter();
  }

  public void accept (Visitor v)
  {
    v.visitMap(this);
  }

  public Any copyFrom (Any a)
  {
    constViolation();

    return this;
  }


  public Any buildNew (Any a)
  {
    return new AnyMapConstDecor(instance_.buildNew(a));
  }

  public Any cloneAny ()
  {
    Any a = null;
    
    a = instance_.cloneAny();
    
    return new AnyMapConstDecor(a);
  }
  
  public boolean isTransactional()
  {
    return instance_.isTransactional();
  }

  public boolean isConst()
  {
    return true;
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
    return false;
  }

  public Map bestowIdentity()
  {
    return new AnyMapDecor (this);
  }

  public int hashCode() { return instance_.hashCode(); }
  
  public boolean equals(Object o)
  {
    if (o instanceof AnyMapConstDecor)
    {
      AnyMapConstDecor hd = (AnyMapConstDecor)o;
      return (instance_.equals(hd.instance_));
    }
    else if (o instanceof Map)
    {
      return instance_.equals(o);
    }
    return false;
  }

  public boolean equals(Any a)
  {
    if (a instanceof AnyMapConstDecor)
    {
      AnyMapConstDecor hd = (AnyMapConstDecor)a;
      return (instance_.equals(hd.instance_));
    }
    else if (a instanceof Map)
    {
      return instance_.equals(a);
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
  
  public Any getMapKey(Any key)
  {
    return instance_.getMapKey(key);
  }
  
  public void empty()
  {
    constViolation();
  }

  public boolean isEmpty() { return instance_.isEmpty(); }
  
  public void add (Any key, Any value)
  {
    constViolation();
  }

  public void add (StringI keyAndValue)
  {
    constViolation();
  }

  public void add (Any element)
  {
    constViolation();
  }

  public Iter createKeysIterator () {return instance_.createKeysIterator(); }

  public Iter createConcurrentSafeKeysIterator()
  {
    throw new UnsupportedOperationException();
  }
  
  public Any get (Any key)
  {
    Any a = instance_.get(key);
    return a.bestowConstness();
  }

  public Any getIfContains (Any key)
  {
    Any a = instance_.getIfContains(key);
    if (a != null)
      a = a.bestowConstness();
    
    return a;
  }

  public java.util.Map getMap ()
  {
    // Internal use only (i.e. not from script) so OK, sort of!
    return instance_.getMap();
  }

  public Any remove (Any key)
  {
    constViolation();
    return null;
  }

  public void replaceItem (Any key, Any item)
  {
    constViolation();
  }

  public void replaceValue (Any key, Any item)
  {
    constViolation();
  }

  public Map shallowCopy()
  {
    return new AnyMapConstDecor (instance_.shallowCopy());
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
  
  public Any getPath(Any to)
  {
    return instance_.getPath(to);
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
    // Guess this is OK?
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
    // TODO is this OK (and other decorators)?
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
    constViolation();
  }

  public void retainAll(Composite c)
  {
    constViolation();
  }

  private void constViolation()
  {
    throw new AnyRuntimeException("Map is Const");
  }
  
  private class ConstIter extends AbstractIter
  {
    private Iter i_;
    
    private ConstIter()
    {
      setIterRoot(ConstIter.this);
      i_ = instance_.createIterator();
    }
    
    public boolean hasNext()
    {
      return i_.hasNext();
    }
    
    public Any next()
    {
      Any a = i_.next();
      return a.bestowConstness();
    }
    
    public void remove()
    {
      constViolation();
    }
    
    public Any previous()
    {
      Any a = i_.previous();
      return a.bestowConstness();
    }
    
    public void add(Any a)
    {
      constViolation();
    }
  }
}
