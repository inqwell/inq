/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/AbstractStack.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.util.Iterator;

/**
 * AbstractStack is a general collection class mapping hashing Anys into
 * a unique set.  It is an abstract class providing an in-memory
 * Stack implementation acting as a basis for concrete imnplementations.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public abstract class AbstractStack extends    AbstractComposite
																	  implements Stack,
																						   Cloneable
{
  private java.util.Stack value_;

  public AbstractStack() {value_ = new java.util.Stack();}
  
  /**
	 * Construct using a ready formed HashSet. But to make sure this contains
	 * only Any type objects - constrain this constructor to be used by our
	 * sub classes only
	 */
  protected AbstractStack(java.util.Stack stack) { value_ = (java.util.Stack)stack.clone(); }

  public String toString()
  {
    return value_.toString();
  }

  public int hashCode()
  {
    return value_.hashCode();
  }

  /**
   * Stack equality.  Argument must be an instanceof Stack and contain the same
   * number of entries and all elements must be contained within this and
   * all elements must test true for equality
   */
  public boolean equals(Any a)
  {
    if (a == this)
      return true;

    if (!(a instanceof Stack))
      return false;
      
    Stack s = (Stack)a;
    
    return getStack().equals(s.getStack());
  }
  
  /**
   * returns true if arg implements Stack and has same number of
   * elements as this and all elements also contained in this
   */
  public boolean like(Any a)
  {
    if (a == this)
      return true;

    if (!(a instanceof Stack))
      return false;
      
    Stack s = (Stack)a;
    
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
    AbstractStack s = (AbstractStack)super.clone();

    s.value_ = new java.util.Stack();

    // Iterate and clone elements
    Iter i = this.createIterator();
    while (i.hasNext())
    {
      Any a = i.next();
			s.push (a.cloneAny());
    }
    return s;
  }

  public Stack shallowCopy()
  {
    AbstractStack newSet = (AbstractStack)buildNew(null);
    newSet.value_ = (java.util.Stack)value_.clone();  // shallow copy of underlying HashSet
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
    v.visitUnknown(this);
	}

  /**
   * Argument must be a Stack.  Implemented as a deep copy of the
   * elements in argument to this.  Original contents of this are
   * removed.
   */
  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
      if (!(a instanceof Stack))
        throw new IllegalArgumentException ();

      Stack from = (Stack)a;

      this.empty();

      Iter i = from.createIterator();
      while (i.hasNext())
      {
        Any any = i.next();
        this.push (any.cloneAny());
      }
    }
    return this;
  }

  public void push (Any a)
  {
		beforePush(a);
    value_.push(a);
		afterPush(a);
  }

  public Any pop ()
  {
    Any ret = (Any)value_.pop();
		return ret;
  }

  public Any peek ()
  {
    Any ret = (Any)value_.peek();
		return ret;
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
  
  public java.util.Stack getStack ()
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
	
	protected abstract void beforePush(Any a);
	protected abstract void afterPush(Any a);
	protected abstract void emptying();
	
	public class AnyIter extends AbstractIter implements Iter
	{
    public AnyIter()
    {
      setIterRoot(AbstractStack.this);
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

