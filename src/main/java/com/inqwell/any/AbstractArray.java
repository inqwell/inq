/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.util.Iterator;
import java.util.ArrayList;

/**
 * AbstractArray supports access to elements by index.
 */
public abstract class AbstractArray extends    AbstractComposite
                                    implements Array,
                                               Vectored,
																							 Orderable,
                                               Cloneable
{
  private ArrayList value_;

  private AnyInt index_ = new AnyInt();
  
  public AbstractArray () { value_ = new ArrayList (); }
  public AbstractArray (int initialCapacity)
    { value_ = new ArrayList (initialCapacity); }

  public String toString()
  {
    return value_.toString();
  }

  public int hashCode()
  {
    return value_.hashCode();
  }

  public boolean equals(Any a)
  {
    if (a == this)
      return true;

  	if (AnyAlwaysEquals.isAlwaysEquals(a))
		  return true;
	
    if (a == null)
      return false;
      
    if (!(a instanceof Array))
      return false;
      
    Array ar = (Array)a;
    
    return getList().equals(ar.getList());
  }

  public Object clone() throws CloneNotSupportedException
  {
    AbstractArray a = (AbstractArray)super.clone();

    // Make a new ArrayList - does a shallow copy only
    ArrayList newValue = (ArrayList)value_.clone();
    a.value_ = newValue;

    // Iterate and clone elements - this should be the most efficient
    // implementation
    int i;
    for (i = 0; i < a.entries(); i++)
    {
    	Any aa = this.get(i);
    	if (aa != null)
    	  aa = aa.cloneAny();
      a.replaceItem(i, aa);
    }
    
    a.index_ = new AnyInt();
    
    return a;
  }

  public Array shallowCopy()
  {
    Array newArray = (Array)buildNew(null);
    
    Iter i = createIterator();
    while (i.hasNext())
    {
      newArray.add (i.next());
    }
    return newArray;
  }
  
  public Composite shallowCopyOf()
  {
    return shallowCopy();
  }


  public void removeByVector (int at) { remove(at); }
  public void removeByVector (Any at) { remove(at); }
  
  public Any getByVector (int at) { return get(at); }
  public Any getByVector (Any at) { return get(at); }
  
  public Any getKeyOfVector(int at)
  {
    throw new UnsupportedOperationException();
  }

  public Any getKeyOfVector(Any at)
  {
    throw new UnsupportedOperationException();
  }
  
  public void addByVector(Any value) { add(value); }
  
  public void addByVector(int at, Any value) { add(at, value); }
  
  public void addByVector(int at, Any key, Any value) { add(at, value); }
  
  public Array initOrderBacking()
  {
    return this;
  }
  
  public void setSparse(boolean isSparse)
  { 
    throw new UnsupportedOperationException("AbstractArray.setSparse()");
  }
  
  public int entries() { return value_.size(); }

  public Iter createIterator () {return new AnyIter();}

  public Iter createReverseIterator () {return new AnyReverseIter();}

  public void accept (Visitor v)
  {
    v.visitArray(this);
  }

  /**
   * Replace the contents of this <code>Array</code> with
   * the elements of the given composite.  If the argument is
   * not a composite or has no child elements
   * this <code>Array</code> will be emptied.
   */
  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
      this.empty();

      Iter i = a.createIterator();

      while (i.hasNext())
      {
        Any child = i.next();
        this.add(child);
      }
    }
    return this;
  }

  public void add (Any element)
  {
  	beforeAdd(-1, element);
    value_.add (element);
  	afterAdd(-1, element);
  }

  public void add (int at, Any element)
  {
  	beforeAdd(at, element);
    value_.add (at, element);
  	afterAdd(at, element);
  }
  
  public void add (Any at, Any element)
  {
  	index_.copyFrom(at);
    add(index_.getValue(), element);
  }
  
  public void addAll(Composite c)
  {
    if (c != null && c.entries() > 0)
    {
      Iter i = c.createIterator();
      while (i.hasNext())
      {
        Any a = i.next();
        this.add(a);
      }
    }
  }

  public void replaceItem (int at, Any item)
  {
  	replacing(at, item);
    value_.set (at, item);
  }

  public void replaceItem (Any at, Any item)
  {
  	index_.copyFrom(at);
    replaceItem (index_.getValue(), item);
  }

  public void replaceValue (int at, Any value)
  {
    Any a = this.get (at);
    a.copyFrom(value);
  }

  public void replaceValue (Any at, Any value)
  {
  	index_.copyFrom(at);
    replaceValue (index_.getValue(), value);
  }

  public Any remove (int at)
  {
  	beforeRemove(at);
    Any a = (Any)value_.remove (at);
  	afterRemove(at);
  	return a;
  }

  public Any remove (Any at)
  {
  	index_.copyFrom(at);
		return remove (index_.getValue());
  }

  public int indexOf(Any a)
  {
    return value_.indexOf(a);
  }

  public void removeAll(Composite c)
  {
    Iter i = c.createIterator();
    int  indx;
    
    while (i.hasNext())
    {
    	Any a = i.next();
    	if ((indx = this.indexOf(a)) >= 0)
    	  this.remove(indx);
    }
  }

  /**
   * Returns the element at the specified position in this Array
   * @exception IndexOutOfBoundsException - index is out of range
   *    (index &lt 0 || index &ge entries())
   */
  public Any get (int at)
  {
    return (Any)value_.get(at);
  }

  public Any get (Any at)
  {
  	index_.copyFrom(at);
    return get(index_.getValue());
  }
  
  public void reverse()
  {
    if (this.entries() == 0)
      return;
    
    int i = 0;
    int j = this.entries() - 1;
    
    while (i < j)
    {
      // Swap i and j;
      Any last = this.remove(j);
      Any first = this.remove(i);
      this.add(i, last);
      this.add(j, first);
      i++;
      j--;
    }
  }

  public boolean contains (Any a)
  {
    return value_.contains(a);
  }
  
  public boolean containsValue (Any value)
  {
    return this.contains(value);
  }

  public void empty()
  {
		emptying();
    value_.clear();
  }
  
  public boolean isEmpty()
  {
    return value_.isEmpty();
  }

  public void sort (Array orderBy)
  {
		AbstractComposite.sortOrderable(this, orderBy);
	}
	
  public void sort (OrderComparator c)
  {
		AbstractComposite.sortOrderable(this, c);
  }

  public void sort (Array orderBy, OrderComparator c)
  {
		AbstractComposite.sortOrderable(this, orderBy, c);
  }
  
  public OrderComparator getOrderComparator()
  {
	  throw new UnsupportedOperationException("AbstractArray.getOrderComparator()");
	}
	
  public int reorder(Any a)
  {
	  throw new UnsupportedOperationException("AbstractArray.reorder()");
	}
	
  public Object[] toArray()
  {
    Object[] o = new Object[entries()];

    int i;

    for (i = 0; i < entries(); i++)
    {
      o[i] = value_.get(i);
    }

    return o;
  }

  /**
   * Expose the underlying java List implementation - required for access
   * to other Java core API classes.
   */
  public java.util.List getList ()
  {
		return value_;
  }

  public Array getArray()
  {
    return this;
  }
  
	public Array bestowIdentity()
	{
		return null;
		//tbd
	}

  // In the protected interface for collection implementation
  // we define the position argument.  If it is not available
  // from the calling method then -1 is passed.
	protected abstract void beforeAdd(int at, Any item);
	protected abstract void afterAdd(int at, Any item);
	protected abstract void beforeRemove(int at);
	protected abstract void afterRemove(int at);
	protected abstract void emptying();
	
	/**
	 * Called while replacing the entry at <code>at</code> with the
	 * given <code>item</code>.
	 */
	protected abstract void replacing(int at, Any item);
	
  // Iterator is implemented as an inner class
  private class AnyIter extends AbstractIter implements Iter
  {
    public AnyIter()
    {
      setIterRoot(AbstractArray.this);
    }
    
    private Iterator _i = value_.listIterator();

    public boolean hasNext()
    {
      return _i.hasNext();
    }

    public Any next()
    {
      return (Any)_i.next();
    }
    
    public void remove()
    {
      _i.remove();
    }

  }

  // Iterator is implemented as an inner class
  private class AnyReverseIter extends AbstractIter implements Iter
  {
  	private int	index_;
  	
  	public AnyReverseIter()
  	{
      setIterRoot(AnyReverseIter.this);
  		index_ = (entries() - 1);
  	}
  	
    public boolean hasNext()
    {
      return (index_ >= 0);
    }

    public Any next()
    {
      return get(index_--);
    }
    
    public void remove()
    {
      AbstractArray.this.remove(index_ + 1);
      index_--;
    }
  }

}
