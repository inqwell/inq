/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/AbstractSet.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.util.Iterator;
import java.util.HashSet;

/**
 * A Set that can only accept an underlying <code>java.util.Set</code>
 * to supply its contents.  The <code>java.util.Set</code> can only
 * be supplied on construction and itself is immutable through this
 * class, although object removal via the iterator is possible.
 * <p>
 * The purpose of this class is to provide a <code>Set</code>
 * interface (that is in the Any sense) to <code>java.util.Set</code>s
 * that are returned by methods on other Java classes (for
 * example, <code>Map.ketSet()</code>).
 * <p>
 * <strong>Note:</strong> The underlying <code>java.util.Set</code>
 * must only contain <code>Any</code>s or ClassCastExceptions will
 * likely be thrown at some point.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class AbstractSet extends    AbstractComposite
                         implements Set
{
  private java.util.Set value_;

  /**
	 * Construct using a ready formed HashSet. But to make sure this contains
	 * only Any type objects - constrain this constructor to be used by our
	 * sub classes only
	 */
  AbstractSet (java.util.Set set)
  {
		value_ = set;
	}

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

  public Set shallowCopy()
  {
    throw new UnsupportedOperationException();
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
   * Argument must be a Set.  Implemented as a deep copy of the
   * elements in argument to this.  Original contents of this are
   * removed.
   */
  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
      if (!(a instanceof Set))
        throw new IllegalArgumentException ();

      Set from = (Set)a;

      this.empty();

      Iter i = from.createIterator();
      while (i.hasNext())
      {
        Any any = i.next();
        this.add (any.cloneAny());
      }
    }
    return this;
  }

  public void add (Any a)
  {
    throw new UnsupportedOperationException();
  }

  public void addAll (Composite c, boolean excludeDuplicates)
  {
    throw new UnsupportedOperationException();
	}
	
  public Any remove (Any a)
  {
    throw new UnsupportedOperationException();
  }

  public void empty()
  {
    throw new UnsupportedOperationException();
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
	
	public class AnyIter extends AbstractIter implements Iter
	{
    public AnyIter()
    {
      setIterRoot(AbstractSet.this);
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

