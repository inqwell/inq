/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.util.Iterator;
import java.io.IOException;
import java.io.ObjectInputStream;
import com.inqwell.any.identity.AnyMapDecor;
import java.util.HashMap;

/**
 * AnyPMap is a general collection class mapping keys of type Any to
 * values of type Any.  It is supports a link to a single parent.  This
 * link, once established, cannot be overwritten, enforcing a single
 * parent at any one time.  If the object is removed from its current
 * container it can then be added to another one.
 */
public class AnyPMap extends    AnyMap
										 implements Map, Cloneable
{
	// We make this field transient for very good reasons.
	// If we serialize a structure starting from an arbitrary
	// node somewhere within a larger object graph we
	// don't want the entire graph to go with it.  In
	// the typical case this would result in the entire
	// universe of objects being serialized, including any
	// catalog entries under $root.config!!!! (assuming
	// parentable maps all the way from the serialization
	// object root to $root.  On the receiving side we re-fix
	// the parent link in the serialization api's
	// private void readObject(ObjectInputStream stream)
	//                     throws IOException, ClassNotFoundException;
	// method
  private transient Composite parent_ = null;

  public AnyPMap() {}
  
  protected AnyPMap(HashMap map)
  {
    super(map);
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    AnyPMap m = (AnyPMap)super.clone();
		m.parent_ = null;
    return m;
  }

  public Map shallowCopy()
  {
    AnyPMap newMap = (AnyPMap)super.shallowCopy();
    newMap.parent_ = null;
    return newMap;
  }
  
	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {establishParent(value);}
	protected void beforeRemove(Any key) {undoParent(get(key));}
	protected void afterRemove(Any key, Any value) {}
	
  protected void emptying()
  {
		Iter i = createIterator();
		while (i.hasNext())
		{
			Any v = i.next();
			undoParent(v);
		}
  }
  
  public Composite getParentAny()
  {
    return parent_;
	}

  public boolean isParentable()
  {
		return true;
	}

  public void setParent(Composite parent)
  {
		if ((parent_ == null) || (parent == null) || (parent_ == parent))
		  parent_ = parent;
		else
		{
			System.out.println ("setParent() : croaking");
			System.out.println ("setParent() : this : " + toString());
			System.out.println ("setParent() : parent : " + parent.toString());
			throw (new IllegalArgumentException("Duplicate parent"));
		}
  }
  
  public short getPrivilegeLevel(Any access, Any key)
  {
    if (this.definesPrivileges())
      return super.getPrivilegeLevel(access, key);
    
    // Rather ugly in this case and could we not define getParentAny to
    // return a Map ?
    Map m = (Map)this.getParentAny();
    
    if (m == null)
      return super.getPrivilegeLevel(access, key);
      
    return m.getPrivilegeLevel(access, key);
  }

//  public String toString()
//  {
//  	if (parent_ != null)
//  	  return super.toString() + " Parent: " + System.identityHashCode(parent_);
//  	else
//  	  return super.toString() + " Parent: null";
//  }
  
	protected void establishParent(Any value)
	{
		if (value instanceof Composite)
		{
			Composite c = (Composite)value;
			c.setParent(this);
		}
	}
	
	protected void undoParent(Any value)
	{
		if (value instanceof Composite)
		{
			Composite c = (Composite)value;
			c.setParent(null);
		}
	}
	
	private void readObject(ObjectInputStream instr)
																							throws IOException,
																										 ClassNotFoundException
	{
		instr.defaultReadObject();
		// once this method has returned the object graph
		// underneath us is complete, so we can traverse our
		// immediate children and fix up the parental links
		Iter i = createIterator();
		while (i.hasNext())
			establishParent(i.next());
	}
}

