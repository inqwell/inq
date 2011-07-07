/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyOrderedMap.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

import java.util.Iterator;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * This implementation maintains the order that the items were
 * inserted and supports the <code>Orderable</code> interface.
 */
public class AnyOrderedMap extends    AnyMap
													 implements Map,
													            Vectored,
													            Orderable,
													            Cloneable
{
	private Array order_ = AbstractComposite.array();
	private AnyInt index_ = new AnyInt();
	
  private   transient OrderComparator comparator_;

	private boolean isSparse_ = false;
	
  protected void afterAdd (Any key, Any value)
  {
		//super.afterAdd(key, value);
    if (order_ != null)
      order_.add(key);
  }

  protected void beforeRemove (Any key)
  {
		//super.beforeRemove(key);
    if (order_ != null)
    {
      int idx = order_.indexOf(key);
      if (idx >= 0)
        order_.remove (idx);
    }
  }

	protected boolean beforeAdd(Any key, Any value)
  {
    return true;
  }

	protected void afterRemove(Any key, Any value) {}
  
  public void removeByVector (int at)
  {
    // Just for safety croak should this ever be the case
    if (order_ != null)
    {
      Any key = order_.get(at);
      this.remove(key);
    }
    else
      throw new IllegalStateException("Not currently ordered");
  }
  
  public void removeByVector (Any at)
  {
    if (order_ != null)
    {
      index_.copyFrom(at);
      this.removeByVector(index_.getValue());
    }
    else
      throw new IllegalStateException("Not currently ordered");
  }
  
  
  public void replaceItem (Any key, Any item)
  {
    Any v = null;
    if ((v = getIfContains(key)) != null)
    {
      getMap().put (key, item);
    }
    else
      add(key, item);
  }

  public int indexOf(Any a)
  {
    if (order_ != null)
    {
      return order_.indexOf(a);
    }
    throw new IllegalStateException("Not currently ordered");
  }
  
  public void reverse()
  {
    if (order_ != null)
      order_.reverse();
  }

  public Any getByVector (int at)
  {
    if (order_ != null)
    {
      Any key = order_.get(at);
      return this.get(key);
    }
    throw new IllegalStateException("Not currently ordered");
  }
  
  public Any getByVector (Any at)
  {
  	index_.copyFrom(at);
  	return this.getByVector(index_.getValue());
  }
  
  public Any getKeyOfVector(int at)
  {
    if (order_ != null)
      return order_.get(at);
    else
      throw new IllegalStateException("Not currently ordered");
  }

  public Any getKeyOfVector(Any at)
  {
    index_.copyFrom(at);
    return this.getKeyOfVector(index_.getValue());
  }

  public void addByVector(Any value)
  {
  	Any key = IdentityOf.identityOf(value);
  	add(key, value);
  }
  
  public void addByVector(int at, Any value)
  {
		addByVector(at, null, value);
  }
  
  public void addByVector(int at, Any key, Any value)
  {
    if (order_ == null)
      throw new IllegalStateException("Not currently ordered");

		if (at < 0)
			throw new ArrayIndexOutOfBoundsException(at);
		
		if (at > entries() && !isSparse_)
			throw new ArrayIndexOutOfBoundsException(at);
  	
  	if (at < entries())
  	{
			add((key == null ? IdentityOf.identityOf(value) : key), value);
			// Because of afterAdd() the key is in the ordering vector at the
			// end.  Move it to the desired place
			Any keyOf = order_.remove(entries() - 1);
			order_.add(at, keyOf);
  	}
  	else if (at == entries())
  	{
			// just adding at the end
			add((key == null ? IdentityOf.identityOf(value) : key), value);
		}
		else
		{
			// Put place-holders in to get the desired result
			int toAdd = at - entries();
			for (int i = 0; i < toAdd; i++)
			{
				Any anyNull = new AnyNull();
				add(IdentityOf.identityOf(anyNull), anyNull);
			}
			add((key == null ? IdentityOf.identityOf(value) : key), value);
		}
  }
  
  public Array initOrderBacking()
  {
    return order_;
  }
  
  public void setSparse(boolean isSparse)
  {
		isSparse_ = isSparse;
  }
  
  public void sort (Array orderBy)
  {
	  comparator_ = null;
		AbstractComposite.sortOrderable(this, orderBy);
	}
	
  public void sort (OrderComparator c)
  {
	  comparator_ = c;
		AbstractComposite.sortOrderable(this, c);
    if (comparator_ != null)
      comparator_.setTransaction(Transaction.NULL_TRANSACTION);
  }

  public void sort (Array orderBy, OrderComparator c)
  {
  	if (orderBy != null)
	    comparator_ = c;
  	else
  		comparator_ = null;
  	
		AbstractComposite.sortOrderable(this, orderBy, c);
		if (comparator_ != null)
		  comparator_.setTransaction(Transaction.NULL_TRANSACTION);
  }

  public OrderComparator getOrderComparator()
  {
    return comparator_;
	}
	
  public int reorder(Any a)
  {
	  // If we don't have any order backing there's nothing to do
	  if (order_ == null)
	    return -1;
	
	  if (comparator_ == null)
	    return -1;
	
	  // It should be the last item in the order backing! Do this
	  // as an optimisation, rather than using indexOf
	  int i = order_.entries();
	  a = order_.remove(i - 1);

		int insertionPosition = AbstractComposite.findInsertionPosition(this,
			                                                              comparator_,
			                                                              a);
		if (insertionPosition < 0)
		{
	    order_.add(a);
		}
		else
		{
	    order_.add(insertionPosition, a);
	  }
	  
	  return insertionPosition;
	}
	
  public java.util.List getList ()
  {
    if (order_ != null)
      return order_.getList();
    
    throw new IllegalStateException("Not in ordered state");
  }

  public Array getArray()
  {
    if (order_ != null)
      return order_;
    
    throw new IllegalStateException("Not in ordered state");
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    AnyOrderedMap m;
    
    m = (AnyOrderedMap)super.clone();

    if (order_ == null)
      m.order_ = null;
    else
      m.order_ = order_.shallowCopy();
    
    m.comparator_ = null;
    m.index_ = new AnyInt();

    return m;
  }

  public Map shallowCopy()
  {
    AnyOrderedMap newMap = (AnyOrderedMap)super.shallowCopy();
    newMap.order_ = (order_ != null) ? order_.shallowCopy() : null;
    newMap.index_ = new AnyInt();
    return newMap;
  }
  
	public Iter createKeysIterator()
	{
		if (order_ == null)
		  return super.createKeysIterator();
		else
		  return new AnyOrderedMapIter(Map.I_KEYS);
	}

	public Iter createConcurrentSafeKeysIterator()
	{
    Array a = order_.shallowCopy();
    
    return new AnyOrderedMapIter(a.createIterator(), Map.I_KEYS);
	}

	public Iter createIterator()
	{
		if (order_ == null)
		  return super.createIterator();
		else
		  return new AnyOrderedMapIter();
	}

  public Object[] toArray()
  {
  	if (order_ == null)
  	  throw new IllegalArgumentException("Not in ordered state");
  	  
    Object[] o = new Object[entries()];

    int i;

    for (i = 0; i < entries(); i++)
    {
      o[i] = this.get(order_.get(i));
    }

    return o;
  }

	public String toString()
	{
		return super.toString() + " " +
           ((order_ != null) ? order_.toString() : "null");
	}
	
	protected void emptying()
  {
		//super.emptying();
    if (order_ != null)
      order_.empty();
  }
  
	private class AnyOrderedMapIter extends AbstractIter implements Iter
	{
		private Iter i_;
		private Any  current_;
		
		private int mode_ = Map.I_VALUES;
		
		public AnyOrderedMapIter()
		{
      setIterRoot(AnyOrderedMap.this);
			i_ = AnyOrderedMap.this.order_.createIterator();
		}

		public AnyOrderedMapIter(int mode)
		{
      setIterRoot(AnyOrderedMap.this);
			i_ = AnyOrderedMap.this.order_.createIterator();
			mode_ = mode;
		}

		public AnyOrderedMapIter(Iter i, int mode)
		{
      setIterRoot(AnyOrderedMap.this);
			i_    = i;
			mode_ = mode;
		}

		public boolean hasNext()
		{
			return i_.hasNext();
		}

		public Any next()
		{
      current_ = i_.next();
      
      if (mode_ == Map.I_VALUES)
        return AnyOrderedMap.this.get(current_);
      else
        return current_;
		}

		public void remove()
		{
			i_.remove();
			AnyOrderedMap.this.remove(current_);
		}
	}
}

