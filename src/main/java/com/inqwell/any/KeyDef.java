/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/KeyDef.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import com.inqwell.any.io.PhysicalIO;

public interface KeyDef extends Any
{
  public static Any key__         = new ConstString("__key");
  public static Any defaultKey__  = AbstractValue.flyweightString("unique");
  public static Any primaryKey__  = AbstractValue.flyweightString("pkey");

	/**
	 * Read from configured persistent storage using the supplied
	 * key value.  If multiple instances are returned then the
	 * return type will be an Array.  Otherwise it will be a Map.
	 * @return Any representing the read results
	 * @throws AnyIOException if an IO error occurs
	 */
  public Any read (Map keyVal, int maxCount) throws AnyException;
	
	/**
	 * Write the given instance to persistent storage.  Optional method
	 * and generally only implemented on the key designated as primary.
	 */
  public void write (Map instanceVal, Transaction t) throws AnyException;
  
	/**
	 * Write the given instance to persistent storage.  Optional method
	 * and generally only implemented on the key designated as primary.
	 * <p>
	 * This method offers the implementation the optimisation of not
	 * having to create the key value from the instance where the caller
	 * already has the key value.  It might also be the case that a key
	 * value is required but is not carried in the instance as fields.
	 */
  public void write (Map keyVal, Map instanceVal, Transaction t) throws AnyException;

	/**
	 * Delete the given instance to persistent storage.  Optional method
	 * and generally only implemented on the key designated as primary.
	 */
  public void delete (Map instanceVal, Transaction t) throws AnyException;
  
	/**
	 * Delete the given instance to persistent storage.  Optional method
	 * and generally only implemented on the key designated as primary.
	 * <p>
	 * This method offers the implementation the optimisation of not
	 * having to create the key value from the instance where the caller
	 * already has the key value.  It might also be the case that a key
	 * value is required but is not carried in the instance as fields.
	 */
  public void delete (Map keyVal, Map instanceVal, Transaction t) throws AnyException;

	/**
	 * Makes a map representing this key definition from the given
	 * instance.  The returned map can contain additional <i>system</i> fields
	 * to ensure that key values are unique for the purposes of locking
	 */
	public Map makeKeyVal (Map instanceVal);

  public boolean checkExists(Any instanceVal, Transaction t);
  
	/**
	 * Returns a read/write prototype of the Map representing a suitable
	 * key value for this definition.  The map will not contain
	 * any <i>system</i> fields since these will be added when the map
	 * is actually used.
	 */
	public Map getKeyProto();
	
	/**
	 * Declare a newly created instance to this key definition.  The instance must
	 * have identity semantics.  The implementation
	 * may perform any desired operation on this event, such as entering the
	 * object into its cache.
	 */
	public void manage (Map instanceVal);
	
	public void unmanage (Map instanceVal);
	
	public void resync (Transaction t, Map m) throws AnyException;

	public void expire(Transaction t) throws AnyException;
	public void destroy();
  
  /**
   * Purge the cache of this key.  Will be a no-op for implementations
   * that do not operate a cache.
   */
  public void purge();

  /**
   * Return the instance cached against the given key
   * or <code>null</code> if none. Only supported for
   * unique keys
   */
  public Any getFromPrimary(Any key);

	/**
	 * Check if the given Map contains keys such that it could be
	 * used  by this definition, i.e. it contains the fields defined
	 * for this key.  Within the given Map <code>keyVal</code> the
	 * keys must be the same set or a superset of the fields defined
	 * for this Key Definition.  System fields used for key disambiguity
	 * between key instances of different run-time object types are
	 * not relevant.
	 * @return the number of fields for this key if satisfied, zero
	 * otherwise.  Allows callers to determine the best match.
	 */
  public int satisfies(Map keyVal);
  
//  public void addField (StringI field);
//  public void addField (String field);
//  public Any getName ();
//  public void clear  ();
  
  // Query section

  /**
	 * Return the list of fields native to this typedef defining this
   * key.  If the overall list of fields also contains references
   * they are not held in this list.
   */
  public Array getFields ();
  
  /**
   * Return the list of all fields that comprise this key.  This
   * list contains both the native and referenced fields, by their
   * alias if specified, in the order they were declared in the
   * key definition.
   */
  public Array getAllFields ();
  
  /**
   * Add a name to the list returned by getAllFields.  Must be unique.
   */
  public void addKeyFieldName(Any name);
  
  public Any getName ();
  
  public Descriptor getDescriptor();

  /**
   * Return the default maximum number of instances this key will retrieve.
   * Only a non-unique key can specify the <code>maxCount</code>.
   * @return the maximum number of instances the key will return or
   * zero if no maximum is specified.
   */
  public int getMaxCount();

  /**
	 * Return <code>true</code> if this key is native.  Only a native
	 * key can be applied to yield instances of the parent Map definition.
	 */
  public boolean isNative  ();
  
  /**
	 * Return <code>true</code> if this key is foreign.  A foreign
	 * key can be used as a native key in another Descriptor.
	 */
  public boolean isForeign ();
  
  /**
	 * Returns <code>true</code> if this key will return at most one
	 * instance when applied.  If, due to mis-configuration or mismatch
	 * with external storage this turns out not to be the case,
	 * an <code>UnexpectedCardinalityException</code> results.
	 */
  public boolean isUnique  ();
  
  public boolean isPersistent();

  public boolean isVolatile();
  
  public boolean shouldCache();

  public boolean isPrimary();

  public void setDescriptor (Descriptor d);

  public void buildProto();

	public void addResolvedReference(Any key, Any value);
  
  public void addInitialiser(Any key, Any value);

  public void setStaticticsVariables(IntI loaded, FloatI hitRate);

	public void setIO(PhysicalIO io, Descriptor d);

	public void setAuxInfo(Any a);
	
	public Any getAuxInfo();
	
  //public boolean isUpdate  ();
  //public boolean isValid();
  
	// Setup section
//  public void setFields (Array fields);
//  public void setNative  (boolean isNative);
//  public void setForeign (boolean isForeign);
//  public void setUnique  (boolean isUnique);
//  public void setUpdate  (boolean isUpdate);
//  public void setName    (Any name);
}
