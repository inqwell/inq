/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.util.Collections;

/**
 * Provides default implementations for the methods defined
 * in interface Composite.
 */
public abstract class AbstractComposite implements Composite
{
  private Any     nodeSet_         = null;

  public boolean equals(Object o)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(o))
			return true;

    if (o instanceof Any)
      return equals ((Any)o);
    return false;
  }

  public int entries()
  {
		return 0;
  }

  public boolean equals(Any a)
  {
    return this == a;
  }

  public void empty() {}

  public boolean like (Any a)
  {
    return false;
  }

  public boolean isTransactional()
  {
		return false;
  }

  public boolean isConst()
  {
    return false;
  }
    
  public Any bestowConstness()
  {
    return this;
  }
  
  public final Any cloneAny ()
  {
    Any a = null;

    try
    {
      a = (Any)clone();
    }
    catch (CloneNotSupportedException e)
    {
      throw (new RuntimeContainedException (e));
    }
    return a;
  }

  public Any buildNew (Any a)
  {
    try
    {
      Any ret = (Any)getClass().newInstance();
      if (a != null)
        ret.copyFrom(a);
      
      return ret;
    }
    catch (InstantiationException e)
    {
      throw new RuntimeContainedException(e);
    }
    catch (IllegalAccessException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public void add (Any element)
  {
    throw new IllegalArgumentException ("add(element) not supported");
  }

  public void accept (Visitor v)
  {
    throw new IllegalArgumentException ("accept() not supported");
  }

  public Any copyFrom (Any a)
  {
    throw new IllegalArgumentException ("copyFrom() not supported");
  }

  public int identity()
  {
		return System.identityHashCode(this);
	}

  public boolean hasIdentity()
	{
		return false;
	}

  public Object clone() throws CloneNotSupportedException
  {
    AbstractComposite c = (AbstractComposite)super.clone();
    //c.nodeSet_          = null;
    return c;
  }

  public static Map   map()           { return new AnyPMap(); }
  public static Map   simpleMap()     { return new SimpleMap(); }
  public static Map   orderedMap()    { return new AnyOrderedMap(); }
  public static Map   managedMap()    { return new InstanceHierarchyMap(); }
  public static Map   weakMap()       { return new WeakMap(); }
  public static Map   eventIdMap()    { return new EventIdMap(); }
  public static Map   keyMap()        { return new KeyMap(); }
  public static Array array()         { return new AnyArray(); }
  public static Array array(int size) { return new AnyArray(size); }
  public static Set   set()           { return new SimpleSet(); }
  public static Set   set(int size)   { return new SimpleSet(size); }
  public static Set   fieldSet()      { return new FieldSet(); }
  public static Set   orderedSet()    { return new OrderedSet(); }
  public static Stack stack()         { return new SimpleStack(); }
  public static Stack callStack()     { return new CallStack(); }
  public static Queue queue()         { return new AnyQueue(); }
  public static Queue synchronizedQueue()
  {
    return new SynchronizedQueue(new AnyQueue());
  }

  public static void sortOrderable (Orderable toSort, Array orderBy)
  {
		AbstractComposite.sortOrderable(toSort, orderBy, null);
	}

  public static void sortOrderable (Orderable toSort, OrderComparator c)
  {
    if (c == null)
      return;
      
		Collections.sort(toSort.getList(), c);
	}

  public static void sortOrderable (Orderable       toSort,
																		Array           orderBy,
																		OrderComparator c)
  {
  	// if there's no comparator given then the sort operation
  	// is just to maintain subsequent insertion order
  	if (c == null)
  	  return;
 
    if (orderBy == null && c.getOrderBy() == null)
      return;

		if (c == null)
			c = new AnyComparator(orderBy);
		else
		{
			if (orderBy != null)
			  c.setOrderBy(orderBy);
	  }

		Collections.sort(toSort.getList(), c);
	}

  public static int findInsertionPosition(Vectored v, OrderComparator c, Any value)
  {
	  //System.out.println("findInsertionPosition value " + value);
	  if (c == null)
	    return -1;
	
	  int lowerBound = 0;
	  int upperBound = v.entries();
	
	  if (upperBound < 0)
	    return 0;
	  
	  while (lowerBound != upperBound)
	  {
      int mid = lowerBound + (upperBound - lowerBound) / 2;
      Any child = v.getByVector(mid);
      int comparison = c.compare(value, child);
      if (comparison < 0)
        upperBound = mid;
      else if (comparison > 0)
        lowerBound = (lowerBound == mid) ? upperBound : mid;
      else
      {
        lowerBound = mid;
        upperBound = mid;
      }
    }
    return lowerBound;
  }
	
  public Any getNodeSet()
  {
		return nodeSet_;
  }
    
  public void setNodeSet(Any nodeSet)
  {
		nodeSet_ = nodeSet;
  }
  
  /**
	 * Default implementation is not allowed
	 */
  public Composite getParentAny()
  {
		throw new UnsupportedOperationException("getParentAny()");
	}

  public Process getProcess()
  {
    return null;
  }

  public Any getNameInParent()
  {
		return null;
  }

  public Any getPath(Any to)
  {
		return null;
  }

  public boolean isParentable()
  {
		return false;
	}

  /**
	 * Default implementation is a no-operation
	 */
  public void setParent(Composite parent)
  {
  }

  public boolean containsAll (Composite c)
  {
		Iter i = null;
		if (c instanceof Map)
		  i = ((Map)c).createKeysIterator();
		else
		  i = c.createIterator();
		  
		while (i.hasNext())
		{
			Any a = i.next();
			if (!contains(a))
				return false;
		}
		return true;
  }

  public boolean containsAny (Composite c)
	{
    if (c == null)
      return false;
      
		Iter i = null;
		if (c instanceof Map)
		  i = ((Map)c).createKeysIterator();
		else
		  i = c.createIterator();

		while (i.hasNext())
		{
			Any a = i.next();
			if (contains(a))
				return true;
		}
		return false;
	}

  public void removeAll(Composite c)
  {
  	throw new UnsupportedOperationException();
  }

  public void retainAll(Composite c)
  {
  	throw new UnsupportedOperationException();
  }
  
  public Any remove(Any id)
  {
		throw new UnsupportedOperationException();
	}

  public void markForDelete(Any id)
  {
		throw new UnsupportedOperationException();
	}

  public boolean isDeleteMarked(Any key)
  {
    throw new UnsupportedOperationException();
  }
  
  public void removeInParent()
  {
		throw new UnsupportedOperationException();
	}
}
