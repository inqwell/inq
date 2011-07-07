/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * Maps keys to values within the Any framework.  A Map cannot contain
 * duplicate keys but may contain duplicate values.  Keys must be immutable,
 * or at least not changed if handles are retained.
 */

public interface Map extends Composite
{

  public static final int I_KEYS   = 0;
  public static final int I_VALUES = 1;
  
  public static final short P_READ   = 1000;
  public static final short P_WRITE  = 1001;
  public static final short P_ADD    = 1002;
  public static final short P_REMOVE = 1003;

  /**
   * Add a key/value pair to the map.
   * @exception DuplicateChildException if the key already exists in the
   * collection.
   */
  public void add (Any key, Any value);
  
  /**
	 * Specific add method in which a single string holds the key and
	 * the value in the form <b>key=value</b>.
	 * @exception IllegalArgumentException if the argument is not parsable
	 * for key and value
   * @exception DuplicateChildException if the key already exists in the
   * collection.
	 */
  public void add (StringI keyAndValue);

  /**
   * Remove a key/value pair identified by the given key.
   */
  public Any remove (Any key);

  /**
   * Replace the object identified by the key with the given item
   * @exception FieldNotFoundException if the key does not exist in the
   * collection.
   */
  public void replaceItem (Any key, Any item);

  /**
   * Replace the value identified by the key with the given value
   * @exception FieldNotFoundException if the key does not exist in the
   * collection.
   */
  public void replaceValue (Any key, Any value);

  /**
   * Return the value identified by the given key
   * @exception FieldNotFoundException if the key does not exist in the
   * collection.
   */
  public Any get (Any key);

  /**
   * Return the value identified by the given key
   * @return The value or <code>null</code> if the key does not exist
   * in the collection.
   */
  public Any getIfContains(Any key);

  /**
   * Expose the underlying java Map implementation - required for access
   * to other Java core API classes.
   */
  public java.util.Map getMap ();
  
  /**
   * Like get() but just checks for the existence of the given key.
   * Overrides contains() in com.inqwell.any.Composite
   * @return true if this contains the given key; false otherwise
   */
  public boolean contains (Any key);

  /**
   * Determines whether there are one or more key mappings to
   * the given value within this map.
   * @return true if this contains the given value; false otherwise
   */
  public boolean containsValue (Any value);

	/**
	 * Return true if this map contains all the given keys, else
	 * return false
	 */
  public boolean hasKeys (Array keys);
  
  /**
	 * Return an <code>Array</code> view of this Map's keys.  Allows
	 * the keys of the map to then be iterated over while at the same
	 * time the map can be changed.  The key values themselves are
	 * not cloned, so don't change them!
	 */
  public Array keys ();
  
  /**
   * 
   * @param key
   * @return
   */
  public Any getMapKey(Any key);
  
  /**
   * Get the privilege level that is the minimum required for
   * the specified access to the child given by the key.
   * @return The privilege level
   */
  public short getPrivilegeLevel(Any access, Any key);
  
  public void setPrivilegeLevels(Map levels, Any key, boolean merge);
  
  /**
   * Return a shallow copy of self.  New Map object contains same object
   * references for keys and values as this.
   */
  public Map shallowCopy();

  public Object getPropertyBean();

  public void setPropertyBean(Object bean);

  /**
   * Return an iterator which will return each key.  Note that the
   * createIterator method defined in Any returns an iterator for the
   * values.
   */
  public Iter createKeysIterator ();
  
  public Iter createConcurrentSafeKeysIterator();
  
  public Descriptor getDescriptor();
  public void setDescriptor(Descriptor d);
  
  /**
	 * Return this map's unique key.  Map be implemented by any
	 * concrete class to provide a value by which this map may
	 * be placed in a parent map. 
	 */
  public Any getUniqueKey();
  
  public void setUniqueKey(Any keyVal);
  
  public void setTransactional(boolean isTransactional);
  
  public void setContext(Any context);

  /**
   * @deprecated to be removed
   */
  public void setAux(Any aux);

  /**
   * @deprecated to be removed
   */
  public Any getAux();

  /**
	 * Adds identity semantics, if supported, to this object.
	 * @return For intents and purposes, the original object but now with
	 * identity semantics.
	 */
  public Map bestowIdentity();
}
