/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * A synchronizing wrapper for any class implementing the Queue interface.
 * Merely a decorator adding synchronization for those methods which change
 * the structure of the underlying collection.
 * <p>
 * <b>Note:</b> Iteration over the underlying queue must take place within
 * a synchronized block on 'this' to be thread safe.
 */
class SynchronizedQueue implements Queue,
                                   Cloneable
{
  private Queue q_;
  
  public SynchronizedQueue (Queue q)
  {
    q_ = q;
  }
  
  public Iter createIterator ()
  {
  	return new AnyIter();
  }

  public synchronized void reverse()
  {
    q_.reverse();
  }
  
  public Iter createReverseIterator () {return new AnyReverseIter();}
  
  // Not structurally altering so no need to synchronize
  public void accept (Visitor v)
  {
		// Must implement rather than forwarding to preserve
		// encapsulation of q_ and thus synchronization through
		// this wrapper.
    v.visitArray(this);
  }
  
  public synchronized Any copyFrom (Any a)
  {
    return q_.copyFrom(a);
  }

  public Any buildNew (Any a)
  {
    return q_.buildNew(a);
  }
  
  public synchronized Object clone () throws CloneNotSupportedException
  {
    SynchronizedQueue q = (SynchronizedQueue)super.clone();
    q.q_ = (Queue)q_.cloneAny();
    return q;
  }

  public synchronized Array shallowCopy()
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

  public int identity()
  {
		return q_.identity();
	}
  
  public boolean hasIdentity()
	{
		return false;
	}

  public Array bestowIdentity()
	{
    throw new IllegalArgumentException ("bestowIdentity() not supported");
	}

  public synchronized int hashCode()
  {
		return q_.hashCode();
	}
	
  public boolean isTransactional()
  {
		return q_.isTransactional();
  }

  public boolean isConst()
  {
		return q_.isConst();
  }
    
  public Any bestowConstness()
  {
    return this;
  }
  
  public synchronized int entries()
  {
    return q_.entries();
  }

  public synchronized boolean equals (Any a)
  {
		synchronized (a)
		{
			return q_.equals(a);
		}
  }

  public synchronized boolean contains (Any a)
  {
    return q_.contains(a);
  }
  
  public synchronized boolean containsAll (Composite c)
  {
    return q_.containsAll(c);
  }
  
  public synchronized boolean containsAny (Composite c)
  {
    return q_.containsAny(c);
  }

  public synchronized void removeAll(Composite c)
  {
  	q_.removeAll(c);
  }

  public synchronized void retainAll(Composite c)
  {
  	q_.retainAll(c);
  }
  
  public synchronized void empty()
  {
    q_.empty();
  }
  
  public synchronized boolean isEmpty()
  {
    return q_.isEmpty();
  }

  public synchronized void add (Any element)
  {
    q_.add(element);
  }

  public synchronized void add (int at, Any element)
  {
    q_.add (at, element);
  }
  
  public synchronized void addAll (Composite c)
  {
    q_.addAll(c);
  }
  
  public synchronized void add (Any at, Any element)
  {
    q_.add(at, element);
  }
  
  public synchronized void replaceItem (int at, Any item)
  {
    q_.replaceItem(at, item);
  }
  
  public synchronized void replaceItem (Any at, Any item)
  {
    q_.replaceItem(at, item);
  }
  

  public synchronized void replaceValue (int at, Any value)
  {
    q_.replaceValue(at, value);
  }
  
  public synchronized void replaceValue (Any at, Any value)
  {
    q_.replaceValue(at, value);
  }
  

  public synchronized Any remove (int at)
  {
    return q_.remove(at);
  }

  public synchronized int indexOf(Any a)
  {
    return q_.indexOf(a);
  }

  public synchronized Any get (int at)
  {
    return q_.get(at);
  }
  
  public synchronized Any get (Any at)
  {
    return q_.get(at);
  }
  
  public synchronized Object[] toArray()
  {
    return q_.toArray();
  }

  public synchronized java.util.List getList()
  {
    return q_.getList();
  }

  public synchronized void addFirst (Any a)
  {
    q_.addFirst(a);
  }
  
  public synchronized void addLast  (Any a)
  {
    q_.addLast(a);
  }
  
  public synchronized Any getFirst ()
  {
    return q_.getFirst();
  }
  
  public synchronized Any getLast  ()
  {
    return q_.getLast();
  }
  
  public synchronized Any  removeFirst()
  {
    return q_.removeFirst();
  }
  
  public synchronized Any  removeLast()
  {
    return q_.removeLast();
  }

  // Iterator is implemented as an inner class
  private class AnyIter extends AbstractIter implements Iter
  {
  	private int	index_;
  	
  	public AnyIter()
  	{
  	  setIterRoot(SynchronizedQueue.this);
  		index_ = 0;
  	}
  	
    public boolean hasNext()
    {
      return (index_ < entries());
    }

    public Any next()
    {
      return get(index_++);
    }

    public void remove()
    {
      SynchronizedQueue.this.remove(index_ - 1);
      index_--;
    }
  }

  public synchronized Composite getParentAny()
  {
    return q_.getParentAny();
	}
  
  public synchronized Process getProcess()
  {
    return q_.getProcess();
  }
  
  public synchronized Any getNameInParent()
  {
    return q_.getNameInParent();
  }
  
  public Any getPath()
  {
    return q_.getPath();
  }
  
  public synchronized boolean isParentable()
  {
    return q_.isParentable();
  }
  
  public synchronized void setParent(Composite parent)
  {
	  q_.setParent(parent);
  }

  public Any getNodeSet()
  {
		return q_.getNodeSet();
  }
    
  public void setNodeSet(Any nodeSet)
  {
		q_.setNodeSet(nodeSet);
  }
  
  public Any remove(Any id)
  {
		return q_.remove(id);
	}
  
  public synchronized void markForDelete(Any key)
  {
    q_.markForDelete(key);
  }

  public boolean isDeleteMarked(Any id)
  {
    return q_.isDeleteMarked(id);
  }
  
  public void removeInParent()
  {
		q_.removeInParent();
	}

  // Iterator is implemented as an inner class
  private class AnyReverseIter extends AbstractIter implements Iter
  {
  	private int	index_;
  	
  	public AnyReverseIter()
  	{
      setIterRoot(AnyReverseIter.this);
  		index_ = (q_.entries() - 1);
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
      SynchronizedQueue.this.remove(index_ + 1);
      index_--;
    }
  }
}
