/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.util.ListIterator;
import java.util.LinkedList;

/**
 * Concrete Queue composite.  Such composites can operate as LIFOs
 * or FIFOs.  The access methods by index are relatively inefficient as
 * compared to arrays since the implementation is a LinkedList
 */

public class AnyQueue extends    AbstractComposite
                      implements Queue,
                                 Vectored,
																 Orderable,
                                 Cloneable
{
  private LinkedList _value;
  private AnyInt index_ = new AnyInt();

  public AnyQueue () { _value = new LinkedList (); }

  public String toString()
  {
    return _value.toString();
  }

  public int hashCode()
  {
    return _value.hashCode();
  }

  public boolean equals(Any a)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

    if (a == this)
      return true;

    if (!(a instanceof Queue))
      return false;
      
    Queue q = (Queue)a;
    
    return getList().equals(q.getList());
  }

  public Object clone() throws CloneNotSupportedException
  {
    AnyQueue q = (AnyQueue)super.clone();

    // Make a new LinkedList.  The clone method of our backing linked
    // list does a shallow copy only.  Since we have to traverse the
    // list in 'this' and replace the elements in the clone its more
    // efficient to start with an empty list.
    LinkedList newValue = new LinkedList ();
    q._value = newValue;

    Iter i = createIterator();
    while (i.hasNext())
    {
      Any a = i.next();
      q.addToken(a.cloneAny());
    }
    return q;
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

  public int entries() { return _value.size(); }

  public Iter createIterator () {return new AnyIter();}

  public Iter createReverseIterator () {return new AnyReverseIter();}

  public void accept (Visitor v)
  {
    // Leave this as an array for now as Queue extends Array
    v.visitArray(this);
  }

  public Any copyFrom (Any a)
  {
    return null; /* tbd */
  }

  public void add (Any element)
  {
    addToken(element);
  }

  public void add (int at, Any element)
  {
    _value.add (at, element);
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
    _value.set (at, item);
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

  public void reverse()
  {
    throw new UnsupportedOperationException();
  }
  
  public Any remove (Any at)
  {
  	index_.copyFrom(at);
    return remove (index_.getValue());
  }

  public Any remove (int at)
  {
    Any a = (Any)_value.remove (at);
    return a;
  }

  public int indexOf(Any a)
  {
    return _value.indexOf(a);
  }

  /**
   * Returns the element at the specified position in this Array
   * @exception IndexOutOfBoundsException - index is out of range
   *    (index &lt 0 || index &ge entries())
   */
  public Any get (int at)
  {
    return (Any)_value.get(at);
  }

  public Any get (Any at)
  {
  	index_.copyFrom(at);
    return (Any)get(index_.getValue());
  }

  public boolean contains (Any a)
  {
    return _value.contains(a);
  }
  
  public void empty()
  {
    _value.clear();
  }
  
  public boolean isEmpty()
  {
    return _value.isEmpty();
  }
  
  public void addFirst (Any a)
  {
    _value.addFirst(a);
  }
  
  public void addLast  (Any a)
  {
    add (a);
  }
  
  public Any getFirst ()
  {
    return (Any)_value.getFirst();
  }
  
  public Any getLast  ()
  {
    return (Any)_value.getLast();
  }
  
  public Any removeFirst()
  {
    return (Any)_value.removeFirst();
  }

  public Any removeLast()
  {
    return (Any)_value.removeLast();
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
	  throw new UnsupportedOperationException("AnyQueue.getOrderComparator()");
	}
	
  public int reorder(Any a)
  {
	  throw new UnsupportedOperationException("AnyQueue.reorder()");
	}
	
  public Object[] toArray()
  {
    Object[] o = new Object[entries()];

    int i;

    for (i = 0; i < entries(); i++)
    {
      o[i] = _value.get(i);
    }

    return o;
  }

	public Array bestowIdentity()
	{
		return null;
		//tbd
	}

  /**
   * Expose the underlying java List implementation - required for access
   * to other Java core API classes.
   */
  public java.util.List getList ()
  {
		return _value;
  }

  public Array getArray()
  {
    return this;
  }
  
  protected void addToken(Any token)
  {
    _value.add (token);
  }

  // Iterator is implemented as an inner class
  private class AnyIter extends AbstractIter implements Iter
  {
    public AnyIter()
    {
      setIterRoot(AnyQueue.this);
    }
    
    private ListIterator _i = _value.listIterator();

    public boolean hasNext()
    {
      return _i.hasNext();
    }

    public Any next()
    {
      return (Any)_i.next();
    }
    
    public Any previous()
    {
      return (Any)_i.previous();
    }
    
    public void remove()
    {
      _i.remove();
    }
    
    public void add(Any a)
    {
      _i.add(a);
    }
  }

  // Iterator is implemented as an inner class
  private class AnyReverseIter extends AbstractIter implements Iter
  {
  	private int	index_;
  	
  	public AnyReverseIter()
  	{
      setIterRoot(AnyReverseIter.this);
  		index_ = (_value.size() - 1);
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
      AnyQueue.this.remove(index_ + 1);
      index_--;
    }
  }

  public void addByVector(Any value)
  {
    this.add(value);
    
  }

  public void addByVector(int at, Any value)
  {
    this.add(at, value);
    
  }

  public void addByVector(int at, Any key, Any value)
  {
    throw new UnsupportedOperationException();
  }

  public Array initOrderBacking()
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean containsValue(Any value)
  {
    return this.indexOf(value) >= 0;
  }

  public Any getByVector(int at)
  {
    return this.get(at);
  }

  public Any getByVector(Any at)
  {
    return this.get(at);
  }

  public void removeByVector(int at)
  {
    this.remove(at);
  }

  public Any getKeyOfVector(int at)
  {
    throw new UnsupportedOperationException();
  }

  public Any getKeyOfVector(Any at)
  {
    throw new UnsupportedOperationException();
  }
  
  public void removeByVector(Any at)
  {
    this.remove(at);
  }

  public void setSparse(boolean isSparse)
  {
    throw new UnsupportedOperationException();
  }
}
