/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Descriptor.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;

public interface Descriptor extends Any
{
  public static final Any field__   = AbstractValue.flyweightString("field");
  public static final Any typedef__ = AbstractValue.flyweightString("typedef");
  public static final Any nodeset__ = AbstractValue.flyweightString("nodeset");
  
	static final Any        descriptor__           = new ConstString("__descriptor");
	static final Descriptor degenerateDescriptor__ = new DegenerateDescriptor();
	
	static final String label__     = "label";
	static final String textfield__ = "textfield";
	static final String combobox__  = "combo-box";
	static final String checkbox__  = "check-box";
	
	static final Any new__          = new ConstString("new");
	static final Any old__          = new ConstString("old");
  
  static final Any construct__    = new ConstString("<construct>");
  static final Any join__         = new ConstString("<join>");
	static final Any mutate__       = new ConstString("<mutate>");
	static final Any destroy__      = new ConstString("<destroy>");
  
  static final Any fieldOrder__   = new ConstString("field-order");
  
  static final int RESOLVE_NOTHING   = 0;
  static final int RESOLVE_SOMETHING = 1;
  static final int RESOLVE_DONE      = 2;
  static final int RESOLVE_UNABLE    = 3;
  
	/**
	 * Read instance(s) from configured persistent storage.  Returned
	 * instances will be managed, that is they can be retrieved from
	 * their cache if present.  Otherwise instances will be read
	 * from the storage configured for this item, given identity
	 * semantics and entered into the unique cache and the cache for the
	 * key used for retrieval.
	 * <P>
	 * The key used will be the first one that is satisfied by the
	 * given <code>keyVal</code> according to <code>KeyDef.satisfies()</code>
	 * for all the keys configured for this descriptor.
	 * @param Process the process performing the read operation.  Processes
	 * performing reads via the same descriptor on the same key value
	 * may arbitrate to ensure that I/O is only performed by one process.
	 */
	public Any  read   (Process p, Map keyVal, int maxCount) throws AnyException;
  
	/**
	 * Read instance(s) from configured persistent storage.  Returned
	 * instances will be managed, that is they can be retrieved from
	 * their cache if present.  Otherwise instances will be read
	 * from the storage configured for this item, given identity
	 * semantics and entered into the unique cache and the cache for the
	 * key used for retrieval.
	 * <P>
	 * The key definition used will be that known by the the given name.
	 * The supplied value must satisfy that definition
	 * according to <code>KeyDef.satisfies()</code>.
	 * @param Process the process performing the read operation.  Processes
	 * performing reads via the same descriptor on the same key value
	 * may arbitrate to ensure that I/O is only performed by one process.
	 */
	public Any  read   (Process p, Any keyName, Map keyVal, int maxCount) throws AnyException;

	/**
	 * Read instance(s) from configured persistent storage.  Operates
	 * as for <code>read(process, keyName, keyVal).  Provided for
	 * when the caller already has the KeyDef instance directly
	 */
	public Any  read   (Process p, KeyDef keyDef, Map keyVal, int maxCount) throws AnyException;
	
  /**
   * Write the given instance to the configured persistent storage.
   */
	public void write  (Process p, Map m) throws AnyException;
  
  /**
   * Write the given instance to the configured persistent storage
   * where the primary unique key is already given.
	public void write  (Process p, Map k, Map o) throws AnyException;
   */
  
  /**
   * Delete the given instance from persistent storage
   */
	public void delete(Process p, Map m) throws AnyException;
  
  /**
   * Delete the given instance from persistent storage
   * where the primary unique key is already given.
   */
	public void delete(Process p, Map keyVal, Map instanceVal) throws AnyException;
  
  /**
   * The semantics of <code>manage()</code> are to accept an
   * unmanaged instance and manage it.  This means checking it
   * for uniqueness using the criteria defined for this item
   * and giving it identity semantics.
   * @return managed instance
   */
	public Map manage(Process p, Map o);
  
  /**
	 * Delete the given managed instance from the cache
	 */
	public void unmanage(Process p, Map m);
	
	/**
	 * Create a new, unmanaged, unconstructed instance of the item
	 */
  public Any newInstance();

  /**
   * Resync the managed object whose primary unique key is given
   * by <code>keyVal</code>.  This method is provided as a
   * method to keep a cached server environment synchronized
   * with changes made to external storage other than through
   * the server's transaction mechanism.  For example, if a
   * database backing BOT instances is altered externally
   * the server's cached instances (if any) will never be
   * updated to reflect these changes.  Such external
   * updates can be signaled with the BOT and unique key
   * value in question to realign the cache and with the external
   * storage.
   */
  public void resync(Process p, Map keyVal) throws AnyException;
  
	/**
   * Expire this typedef.
   */
	public void expire(Transaction t) throws AnyException;
	
  /**
   * Allow the typedef to tidy up any resources as it is being
   * removed from the system
   */
	public void destroy();
  
  /**
   * Perform any construction on new instances.  Will be called before
   * object is managed/written
   */
  public void construct(Map m, Transaction t) throws AnyException;

  public void join(Map m, Transaction t) throws AnyException;
  
  /**
   * Perform any mutation on existing instances.  Will be called
   * as late as possible for the transaction implementation.
   */
	public void mutate(Map newVal,
                     Map oldVal,
                     Map context,
                     Transaction t) throws AnyException;
  
  /**
   * Perform any destruction on existing instances.  Will be called when
   * object is first deleted in the transaction
   */
  public void destroy(Map m, Transaction t) throws AnyException;
  
  /**
   * Perform any expiry processing on existing instances.
   * In fact, a "call back"
   * when <code>Descriptor.expire(Transaction t)</code> is called.
   */
  public void expire(Map m, Transaction t) throws AnyException;
  
	public Any getName();
	
	public Any getDefaultAlias();
	
	public Any getFQName();
  
  public Any getPackage();
	
	public Map getEnums();
  public Map getEnumSymbols();

	/**
	 * Return the prototype <code>Map</code> implementation
	 * this <code>Descriptor</code> uses for new instances.  This
	 * is strictly read-only and is supplied as an efficiency
	 * measure in those cases where a client requires a reference
	 * copy of the instance
	 */
	public Map getProto();

	/**
	 * Return a read-write copy of the specified named data field.
	 */
	public Any getDataField(Any fieldName, boolean mustResolve);

  /**
	 * Generate a <code>Map</code> which represents the designated
	 * primary unique key for the given map.  Implementations must
	 * ensure that the map contains the
	 * key <code>"descriptor__"</code> which can be mapped to the
	 * descriptor itself.  This ensures that key values whose Map keys
	 * collide are unique between different run-time types.
	 */
  public Map getUniqueKey(Map m);
  
  /**
	 * Return a map of <code>KeyDef</code>s containing each
	 * unique <code>KeyDef</code> for this descriptor, including the 
	 * primary one.  Map keys are key names.
	 */
  public Map getUniqueKeys();

	public KeyDef getKey(Any keyName);
	public KeyDef getPrimaryKey();
	
	/**
   * Check if the required privilege permits the requested access.
   * The privilege information for the specified field is checked
   * to see if the specified access is allowed for the given process.
   * @throws AnyRuntimeException if the requested access is not
   * allowed.
   */
	public void checkPrivilege(Process p, Any access, Any key);

  /**
   * Check if the required privilege permits the requested access.
   * Checks the access for all keys of the specified Map.
   */
	public void checkPrivilege(Process p, Any access, Map keys);
	
	/**
   * Return the <code>DomainAgent</code> that provides access to
   * the necessary remote facilities for this Descriptor.
   * @return null if this Descriptor is locally hosted.
   */
	public DomainAgent getDomainAgent();
	
  /**
	 * Return a map of <code>KeyDef</code>s containing all
	 * <code>KeyDef</code> for this descriptor, including the 
	 * primary one.  Maps names to KeyDefs
	 */
  public Map getAllKeys();
  
  /**
   * Enter any keys of referring typedefs that are dependent
   * on the given field into the transaction.
   */
  public void joinForeign(Any fieldName, Transaction t);
  
  public KeyDef locateRetrievalKey (Map keyVal) throws AnyException;
  public KeyDef locateRelationshipKey (Descriptor from) throws AnyException;

  public Map getListenerData();

  
  public void setProto(Map proto);
  public void addDataField(Any key, Any field);
  
  /**
   * Add successive discrete values for the specified field
   */
  public void addEnumValue(Any key, Any symbol, Any value, Any extValue);

  public void setFormat(Any key, Any formatString);
  
  public void setTitle(Any key, Any titleString);
  
  public void setWidth(Any key, Any width);
  
  public void setPrivilege(Any key, Map privileges);

  public boolean isEnum(Any key);
  
  /**
   * Determine whether this has all its references to <code>d</code>
   * resolved or, if <code>d</code> is <code>null</code> that
   * this has all references resolved.
   */
  public boolean isResolved(Descriptor d);

  public Set reportUnresolved();
  
  public boolean isDefunct();
  
  public boolean isKeyField(Any f);
  
  public boolean isUniqueKeyField(Any f);
  
  public boolean isNonKeyField(Any f);

  public void setDescriptorsInKeys();

  public Any getFromPrimary(Any key);
  
  /**
	 * Returns a string representing a renderer for the specified
	 * field
	 */
  public String getRenderer(Any key);
  
  /**
	 * Returns a string representing an editor for the specified
	 * field
	 */
  public String getEditor(Any key);
  
  public String getFormat(Any key);

  public Any getTitle(Any key);

  public int getWidth(Any key);
  
  public boolean isTransient(Any key);

  public void addDataFieldReference(Any field,
                                    Any fQName,
                                    Any alias,
                                    Any initOverride,
                                    Any labelOverride,
                                    Any target);

  public int resolveReferences(Descriptor d);

  public void resetResolved(Descriptor d);
}
