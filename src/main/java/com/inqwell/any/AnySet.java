/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/AnySet.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

import java.util.Iterator;
import java.util.HashSet;

/**
 * A general collection class hashing Anys into
 * a unique set.  It is an abstract class providing an in-memory
 * Set implementation acting as a basis for concrete imnplementations.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public abstract class AnySet extends    AbstractComposite
                             implements Set,
																				Cloneable
{
  private HashSet value_;

  public AnySet() {value_ = new HashSet();}
  
  public AnySet (int initialCapacity)
  {
		value_ = new HashSet(initialCapacity);
	}
	
  /**
	 * Construct using a ready formed HashSet. But to make sure this contains
	 * only Any type objects - constrain this constructor to be used by our
	 * sub classes only
	 */
  protected AnySet(HashSet set) { value_ = set; }

  public String toString()
  {
    return value_.toString();
  }

  public int hashCode()
  {
    return value_.hashCode();
  }

  /**
   * Set equality.  Argument must be an instanceof Set and contain the same
   * number of entries and all elements must be contained within this and
   * all elements must test true for equality
   */
  public boolean equals(Any a)
  {
    if (a == this)
      return true;

    if (!(a instanceof Set))
      return false;
      
    Set s = (Set)a;
    
    return getSet().equals(s.getSet());
  }
  
  /**
   * returns true if arg implements Set and has same number of
   * elements as this and all elements also contained in this
   */
  public boolean like(Any a)
  {
    if (a == this)
      return true;

    if (!(a instanceof Set))
      return false;
      
    Set s = (Set)a;
    
    if (s.entries() != entries())
      return false;

    Iter i = s.createIterator();
    
    while (i.hasNext())
    {
      a = i.next();
      if (!contains(a))
        return false;
    }
    return true;
  }

  public Object clone() throws CloneNotSupportedException
  {
    AnySet s = (AnySet)super.clone();

    s.value_ = new HashSet(this.entries());

    // Iterate and clone elements
    Iter i = this.createIterator();
    while (i.hasNext())
    {
      Any a = i.next();
			s.add (a.cloneAny());
    }
    return s;
  }

  public Set shallowCopy()
  {
    AnySet newSet = (AnySet)buildNew(null);
    newSet.value_ = (HashSet)value_.clone();  // shallow copy of underlying HashSet
    return newSet;
  }
  
  public Composite shallowCopyOf()
  {
    return shallowCopy();
  }

  public int entries() { return value_.size(); }

  public Iter createIterator () {return new AnyIter();}

  public void accept (Visitor v)
  {
    v.visitSet(this);
	}

  /**
   * Argument must be a Composite.  Implemented as a deep copy of the
   * elements in argument to this.  Original contents of this are
   * removed.
   */
  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
      this.empty();
      
      if (a instanceof Composite)
      {
        Iter i = a.createIterator();
        while (i.hasNext())
        {
          Any any = i.next();
          this.add (any.cloneAny());
        }
      }
      else
        this.add(a);
    }
    return this;
  }

  public void add (Any a)
  {
		handleDuplicates(a);
		beforeAdd(a);
    value_.add (a);
		afterAdd(a);
  }

  public void addAll (Composite c, boolean excludeDuplicates)
  {
		Iter i = c.createIterator();
		while (i.hasNext())
		{
			Any a = i.next();
			if (!excludeDuplicates || !contains(a))
				add(a);
		}
	}
	
  public Any remove (Any a)
  {
		beforeRemove(a);
    Any ret = new AnyBoolean(value_.remove (a));
		afterRemove(a);
		return ret;
  }

  public void removeAll(Composite c)
  {
    Iter i = c.createIterator();
    
    while (i.hasNext())
    {
      Any a = i.next();
      if (this.contains(a))
        this.remove(a);
    }
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
  
  public java.util.Set getSet ()
  {
    return value_;
  }

  /**
   * Determine if the set contains the given object
   * @return TRUE if a object is present, FALSE otherwise.
   */
  public boolean contains (Any a)
  {
    return value_.contains(a);
  }
	
	protected void handleDuplicates(Any a)
	{
    if (value_.contains(a))
    {
      throw new DuplicateChildException ("Adding object: " + a);
    }
	}
	
	protected abstract void beforeAdd(Any a);
	protected abstract void afterAdd(Any a);
	protected abstract void beforeRemove(Any a);
	protected abstract void afterRemove(Any a);
	protected abstract void emptying();
	
	public class AnyIter extends AbstractIter implements Iter
	{
    public AnyIter()
    {
      setIterRoot(AnySet.this);
    }
    
    private Iterator i_ = value_.iterator();

		public boolean hasNext()
		{
			return i_.hasNext();
		}

		public Any next()
		{
			return (Any)i_.next();
		}

		public void remove()
		{
			i_.remove();
		}

	}
}

