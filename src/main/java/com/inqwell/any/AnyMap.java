/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.util.Iterator;
import java.util.HashMap;
import com.inqwell.any.debug.DebugMap;

/**
 * AnyMap is a general collection class mapping keys of type Any to
 * values of type Any.  It is an abstract class providing an in-memory
 * map implementation acting as a basis for concrete implementations.
 */
public abstract class AnyMap extends    PropertyAccessMap
														 implements Map, Cloneable
{
  private java.util.Map value_;

  public AnyMap()
  {
    value_ = new HashMap();
  }

  /**
	 * Construct using a ready formed HashMap. But to make sure this contains
	 * only Any type objects - constrain this constructor to be used by our
	 * sub classes only
	 */
  protected AnyMap(HashMap map)
  {
    if (map != null)
      value_ = (HashMap)map.clone();
  }

  protected void debugDecorate()
  {
    value_ = DebugMap.decorate(value_);
  }

  public String toString()
  {
    // debug recursion System.out.println(value_.keySet().toString());
    //AbstractAny.stackTrace();
    return value_.toString();
  }

  public int hashCode()
  {
    return value_.hashCode();
  }

  /**
   * Map equality.  Argument must be an instanceof Map and contain the same
   * number of entries and all elements must be contained within this and
   * all elements must test true for equality
   */
  public boolean equals(Any a)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(a))
			return true;

    if (a == this)
      return true;

    if (!(a instanceof Map))
      return false;

    Map m = (Map)a;

    return getMap().equals(m.getMap());
  }

  public Object clone() throws CloneNotSupportedException
  {
    AnyMap m = (AnyMap)super.clone();

    if (value_ != null)
    {
      m.value_           = new HashMap(this.entries());


      // Iterate and clone elements
      Iter i = this.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        Any v = this.get(k);
        m.add (k, ((v != null) ? v.cloneAny() : null));
      }
    }

    return m;
  }

  public Map shallowCopy()
  {
    AnyMap newMap = (AnyMap)buildNew(null);
    newMap.value_ = (java.util.Map)((HashMap)value_).clone();  // shallow copy of underlying HashMap
    return newMap;
  }

  public int entries() { return value_.size(); }

  public Iter createIterator ()
  {
    if (value_ != null)
      return new AnyMapIter(Map.I_VALUES);
    else
      return DegenerateIter.i__;
  }

  /**
   * Argument must be a Map.  Implemented as a deep copy of the
   * elements in argument to this.  Original contents of this are
   * removed.
   */
  public Any copyFrom (Any a)
  {
    if (!(a instanceof Map))
      throw new IllegalArgumentException ("Cannot copy to a Map from " +
                                          ((a == null) ? "null"
                                                      : a.getClass().toString()));

    Map from = (Map)a;

    this.empty();

    Iter i = from.createKeysIterator();
    while (i.hasNext())
    {
      Any k = i.next();
      this.add (k, from.get(k).cloneAny());
    }
    return this;
  }

  public void add (Any key, Any value)
  {
		if (beforeAdd(key, value))
		{
      handleDuplicates(key);
	//		if (value == null)
	//			throw new IllegalArgumentException("Adding null value with key " + key);
	//		if (key == null)
	//			throw new IllegalArgumentException("Adding value with null key " + value);
	    value_.put (key, value);
			afterAdd(key, value);
	  }
  }

  public void add (StringI keyAndValue)
  {
		AnyStringTokenizer t =
			new AnyStringTokenizer(keyAndValue, "=");

		if (t.countTokens() == 2)
		{
			AnyString argValue = new AnyString();
			AnyString argName  = new AnyString();
			t.nextToken(argName);
			t.nextToken(argValue);
			add (argName, argValue);
		}
		else
		{
			throw (new IllegalArgumentException ("Cannot parse key and value: " +
																					 keyAndValue.toString()));
		}
	}

  public Any remove (Any key)
  {
		beforeRemove(key);
    Any v = (Any)value_.remove (key);
		afterRemove(key, v);
		return v;
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

  public void replaceItem (Any key, Any item)
  {
    if (contains(key))
			remove(key);
    add(key, item);
  }

  public void replaceValue (Any key, Any value)
  {
    Any a = get(key);
    a.copyFrom (value);
  }

  public Any get (Any key)
  {
		handleNotExist(key);
    Any a = (Any)value_.get(key);
    return a;
  }

  public java.util.Map getMap ()
  {
    return value_;
  }

  public Any getIfContains(Any key)
  {
    Any a = (Any)value_.get(key);
    return a;
  }

  /**
   * Determine if the map contains the given key
   * @return TRUE if a key is present, FALSE otherwise.
   */
  public boolean contains (Any key)
  {
    return value_.containsKey(key);
  }

  public boolean containsValue (Any value)
  {
    return value_.containsValue(value);
  }

  public void removeAll(Composite c)
  {
    Iter i = this.createKeysIterator();
    while (i.hasNext())
    {
    	Any k = i.next();
    	if (c.contains(k))
    	  i.remove();
    }
  }

  public void retainAll(Composite c)
  {
    // Iterate over this removing any not contained in c
    Iter i = this.createKeysIterator();
    while (i.hasNext())
    {
      Any k = i.next();
      if (!c.contains(k))
        i.remove();
    }
  }

  public boolean hasKeys (Array keys)
  {
		Iter i = keys.createIterator();
		Any key;

		while (i.hasNext())
		{
			key = i.next();
			if (!contains(key))
				return false;
		}
		return true;
  }

  public Array keys ()
  {
    // Hmmm, the javadoc in Map.java says that this Set will be
    // safe to iterate over in the face of modifying its backing
    // collection.  Recent mods have made this not so!  This is
    // therefore less efficient than just returning the underlying
    // Java map's entry set, but what else can we do?
    /*
		Set ret = new AbstractSet((java.util.Set)value_.keySet().clone());
		return ret;
    */
		Array ret = AbstractComposite.array();
		Iter i = createKeysIterator();

		while (i.hasNext())
		{
			ret.add(i.next());
		}

		return ret;
	}
  
  public Any getMapKey(Any key)
  {
    Iter i = createKeysIterator();

    while (i.hasNext())
    {
      Any a = i.next();
      if (a.equals(key))
        return a;
    }
    
    return null;
  }

  public Iter createKeysIterator ()
  {
    if (value_ != null)
      return new AnyMapIter(Map.I_KEYS);
    else
      return DegenerateIter.i__;
  }

	protected void handleDuplicates(Any key)
	{
    if (value_.containsKey (key))
    {
      throw new DuplicateChildException ("Adding key: " +
                                         key +
                                         " whose value is " +
                                         value_.get(key));
    }
	}

  protected java.util.Map setMap(HashMap m)
  {
    java.util.Map ret = value_;
    value_ = m;

    return ret;
  }

	protected class AnyMapIter extends AbstractIter implements Iter
	{
		protected java.util.Iterator	i_;
		protected int iterType_;
		protected Any current_;

		/**
		 * Must provide a suitable object which implements java.util.Map
		 * @see java.util.Map
		 */
		public AnyMapIter(int iterType)
		{
      setIterRoot(AnyMap.this);
      
			iterType_ = iterType;

			if (iterType == Map.I_KEYS)
			{
				i_ = AnyMap.this.value_.keySet().iterator();
			}
			else
			{
				i_ = AnyMap.this.value_.values().iterator();
			}
		}

		public boolean hasNext()
		{
			return i_.hasNext();
		}

		public Any next()
		{
			return (current_ = (Any)i_.next());
		}

	  public void remove()
	  {
	  	// This code 'goes underneath' the com.inqwell.any.Map
	  	// interface.  This is not good for the protected mode
	  	// interface of AbstractMap (before/afterRemove()).
	  	// This is messy but useful and we don't often call
	  	// Iter.remove() - if what we are removing is a Composite
	  	// then try Composite.getNameInParent() and call
	  	// before/afterRemove (which need the key).  Otherwise,
	  	// if we are a key iterator that's OK.  If not just do
	  	// the underneath removal as there's nothing else we can
	  	// do to ascertain the key.
	  	if (iterType_ == Map.I_KEYS)
	  	{
	  		Any v = AnyMap.this.get(current_);
	  		AnyMap.this.beforeRemove(current_);
	      i_.remove();
	  		AnyMap.this.afterRemove(current_, v);
	  	}
	  	else
	  	{
	  		if (current_ instanceof Composite)
	  		{
	  			Any k = ((Composite)current_).getNameInParent();
	  			//System.out.println("ITER REMOVAL " + k);
	  			//System.out.println("ITER REMOVAL " + current_);
		  		AnyMap.this.beforeRemove(k);
		      i_.remove();
		  		AnyMap.this.afterRemove(k, current_);
	  			//System.out.println("ITER REMOVAL " + k);
	  			//System.out.println("ITER REMOVAL " + current_);
	  		}
	  		else
	  		{
	  			// ooer, hope for the best!
		      i_.remove();
	  		}
	  	}
	  }

	}
}

