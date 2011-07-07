/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AbstractDescriptor.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Provide basic descriptor requirements.
 */
public abstract class AbstractDescriptor extends    PropertyAccessMap //AbstractAny
																				 implements Descriptor
{
  // The name of this item
  private Any   name_;

  // The alias of this item
  private Any   alias_;

  // The fully qualified name of this item
  private Any   fQName_;
  
  // A proxy instance for this to place in serialize streams
  private transient Descriptor proxy_;

  // The prototype from which all instances of items described by
  // this descriptor will be made.
  private Map    proto_;
  
	// Key names to KeyDef instances for all keys, unique and non-unique, foreign...
  private Map    keys_;
  
  // Our format strings
  private Map    formatStrings_;
  
  // Our title strings (often used in rendering)
  private Map    titleStrings_;
  
  // Our widths
  private Map    widths_;
  
  // A map whose fields are initialised to the equals constant
  private Map listenerData_;

  private Map    privileges_;
  
  // Our enumerated values.  A map of maps.  The first
  // level maps field name to a map, which in turn
  // maps internal values to external values
  private Map    enums_;
  
  // As above, but 2nd level maps are symbolic names
  // to internal values.
  private Map    enumSymbols_;
  
  private Any  package_;

  private transient Map   propertyMap_;
  
	protected AbstractDescriptor (Any name,
																Any alias,
																Any fQName,
                                Any inqPackage)
  {
    name_          = name;
    alias_         = alias;
    fQName_        = fQName;
    proxy_         = new ProxyDescriptor(fQName);
    package_       = inqPackage;
    
		init();
  }
  
	protected AbstractDescriptor (Any name,
																Any alias,
																Any fQName,
																Map proto,
																Map keys,
																Map formatStrings,
																Map titleStrings,
																Map widths,
																Map enums,
																Map enumSymbols,
                                Map privileges)
  {
    name_          = name;
    alias_         = alias;
    fQName_        = fQName;
    proto_         = proto;
    formatStrings_ = formatStrings;
    keys_          = keys;
    formatStrings_ = formatStrings;
    titleStrings_  = titleStrings;
    widths_        = widths;
    enums_         = enums;
    enumSymbols_   = enumSymbols;
    proxy_         = new ProxyDescriptor(fQName);
    setPrivileges(privileges);
  }

  public Map getProto()
  {
		return proto_;
	}
	
  public void setProto(Map proto)
  {
		//System.out.println ("inside setProto");
		proto_ = proto;
	}

  public Any getName() { return name_; }
  
  public void setName(Any name) { name = name_; }

	public Any getDefaultAlias() { return alias_; }
	
	public Any getFQName() { return fQName_; }
  
  public Descriptor getProxy()
  {
    return proxy_;
  }

  public Any getDataField(Any fieldName, boolean mustResolve)
	{
		Any f = getProto().getIfContains(fieldName);

    if (f == null && mustResolve)
      throw new AnyRuntimeException("No such field " + fieldName);
    
    return f;
	}

	public DomainAgent getDomainAgent()
	{
    return null;
  }

  public Descriptor getDescriptor()
  {
    // Satisfies the GetType's visitor requirements! 
    return this;
  }

  public void expire(Transaction t) throws AnyException
	{
	}

	public void destroy()
	{
	}

  public Any getPackage() { return package_; }
  
	public KeyDef getKey(Any keyName)
	{
		//System.out.println ("getKey: " + keyName + " in " + keys_);
		return (KeyDef)keys_.get(keyName);
	}

  public Map getUniqueKeys()
  {
		Map ret = AbstractComposite.map();
		
		Iter i = getAllKeys().createKeysIterator();
		while (i.hasNext())
		{
			Any k = i.next();
			KeyDef kd = (KeyDef)keys_.get(k);
			if (kd.isUnique() && kd.isNative())
				ret.add(k, kd);
		}
		return ret;
	}
	
  public Map getAllKeys()
  {
		return keys_;
	}
	
  public void joinForeign(Any fieldName, Transaction t)
  {
    throw new UnsupportedOperationException();
  }
  
  public Any newInstance()
  {
    if (!isResolved(null))
    {
      throw new AnyRuntimeException("Cannot perform new of unresolved type " +
                                    getFQName() + " " +
                                    reportUnresolved().toString());
      
    }
      
		Map m = (Map)getProto().cloneAny();
		m.setDescriptor(this);
		return m;
	}
	
  public void setFormat(Any key, Any formatString)
  {
		//System.out.println ("Inside setFormat " + key + "fmt " + formatString);
  	if (formatStrings_.contains(key))
  	  formatStrings_.replaceItem(key, formatString);
  	else
	  	formatStrings_.add(key, formatString);
  }
  
  public void setTitle(Any key, Any titleString)
  {
  	if (titleStrings_.contains(key))
  	  titleStrings_.replaceItem(key, titleString);
  	else
	  	titleStrings_.add(key, titleString);
  }

  public void setWidth(Any key, Any width)
  {
  	if (widths_.contains(key))
  	  widths_.replaceItem(key, width);
  	else
	  	widths_.add(key, width);
  }
  
	public void checkPrivilege(Process p, Any access, Any key)
  {
    if (p == null)
      throw new AnyRuntimeException("No process");
    
    // Always allow if not specified.
    short nodePrivilege = Process.MINIMUM_PRIVILEGE;
    
    if (privileges_ != null && privileges_.contains(key))
    {
      Map privileges = (Map)privileges_.get(key);
      if (privileges.contains(access))
      {
        ShortI s = (ShortI)privileges.get(access);
        nodePrivilege = s.getValue();
      }
    }

    short procPrivilege = p.getEffectivePrivilegeLevel();
    
    if (procPrivilege > nodePrivilege)
      throw new AnyRuntimeException("Insufficient privilege for requested operation");
  }
  
	public void checkPrivilege(Process p, Any access, Map keys)
	{
    Iter i = keys.createKeysIterator();
    while (i.hasNext())
      this.checkPrivilege(p, access, i.next());
	}
	
  public void setPrivilege(Any key, Map privileges)
  {
  	if (privileges_.contains(key))
  	  privileges_.replaceItem(key, privileges);
  	else
	  	privileges_.add(key, privileges);
  }

	/**
	 * Config setup method
	 */
	public void setDescriptorsInKeys()
	{
		Iter i = keys_.createIterator();
		while (i.hasNext())
		{
			KeyDef k = (KeyDef)i.next();
			k.setDescriptor(this);
			k.buildProto();
		}
	}
	
  public String getFormat(Any key)
  {
  	if (formatStrings_.contains(key))
		  return formatStrings_.get(key).toString();
		else
		  return null;
  }

  public AnyFormat getFormatter(Any field)
  {

    String str = this.getFormat(field);
    Any a = getDataField(field, true);
      
    AnyFormat f = AnyFormat.makeFormat(a, str, null);
    return f;
  }

  public Any getTitle(Any key)
  {
  	if (titleStrings_.contains(key))
		  return titleStrings_.get(key);
		else
		  return key;
  }

  public int getWidth(Any key)
  {
  	if (widths_.contains(key))
    {
		  return ((IntI)widths_.get(key)).getValue();
    }
		else
		  return 10;
  }
  
  public void addEnumValue(Any key, Any symbol, Any value, Any extValue)
  {
  	if (enums_.contains(key))
  	{
  		Map m = (Map)enums_.get(key);
  		m.add(value, extValue);
  		
  		m = (Map)enumSymbols_.get(key);
  		m.add(symbol, value);
  	}
  	else
  	{
  		Map m = AbstractComposite.orderedMap();
  		m.add(value, extValue);
  		enums_.add(key, m);
  		
  		m = AbstractComposite.orderedMap();
  		m.add(symbol, value);
  		enumSymbols_.add(key, m);
  	}
  	//System.out.println ("addEnumValue enums: " + enums_);
	}
	
  public boolean isEnum(Any key)
  {
		//System.out.println ("isEnum() enums are: " + enums_);
  	return enums_.contains(key);
  }
  
  public boolean isResolved(Descriptor d)
  {
		throw new UnsupportedOperationException();
  }
  
  public boolean isKeyField(Any f)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isUniqueKeyField(Any f)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isNonKeyField(Any f)
  {
    throw new UnsupportedOperationException();
  }
  
  public Set reportUnresolved()
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isDefunct()
  {
		throw new UnsupportedOperationException(getClass().toString() + "isDefunct ");
  }
  
  public String getRenderer(Any key)
  {
  	Any a = getProto().get(key);
  	if (a instanceof BooleanI)
  	  return Descriptor.checkbox__;
  	else
		  return Descriptor.label__;
  }
  
  public String getEditor(Any key)
  {
		if (enums_.entries() != 0)
			return Descriptor.combobox__;
		else
		{
	  	Any a = getProto().get(key);
	  	if (a instanceof BooleanI)
	  	  return Descriptor.checkbox__;
	  	else
			  return Descriptor.textfield__;
	  }
  }
  
  public Any buildNew (Any a)
  {
    // Descriptors cannot be built but we may encounter them
    // during the traversal of Clone.
    return this;
  }
  
  // All descriptors are effectively immutable, so make this a no-op
  // rather then throwing an exception.
  public Any copyFrom (Any a)
  {
    return this;
  }
  
  public void accept (Visitor v)
  {
    if (v instanceof Equals || v instanceof NotEquals)
      v.visitUnknown(this);
    else
      super.accept(v);
  }

  public boolean equals(Any o)
  {
    if (this == o)
      return true;
      
		if (AnyAlwaysEquals.isAlwaysEquals(o))
			return true;

		if (o instanceof Descriptor)
		{
      Descriptor d = (Descriptor)o;
			return d.getFQName().equals(fQName_);
		}
		else
		{
			return false;
		}
  }
  
  public int hashCode()
  {
		return fQName_.hashCode();
  }
  
  public String toString()
  {
		return fQName_.toString();
	}
  
  // Provide some methods giving to access to members to make it
  // easy to generate the serializable object
  // (see BOTDescriptor.writeReplace).  Note these methods are
	// potentially destructive but they are not available from the
	// Descriptor interface, which we use in the general case.
  public Map getFormatStrings() { return formatStrings_; }
  public Map getTitleStrings() { return titleStrings_; }
  public Map getWidths() { return widths_; }
  public Map getEnums() { return enums_; }
  public Map getEnumSymbols() { return enumSymbols_; }
//  public void setFormatStrings(Map fs) { formatStrings_ = fs; }
//  public void setTitleStrings(Map ts) { titleStrings_ = ts; }
//  public void setEnums(Map en) { enums_ = en; }
  
  public KeyDef locateRetrievalKey (Map keyVal) throws AnyException
  {
		throw new UnsupportedOperationException("locateRetrievalKey " + getClass().toString());
  }
  
  public KeyDef locateRelationshipKey (Descriptor from) throws AnyException
  {
		throw new UnsupportedOperationException("locateRelationshipKey " + getClass().toString());
  }

  public boolean isTransient(Any key)
  {
		throw new UnsupportedOperationException("isTransient " + getClass().toString());
  }
  
  public Any getFromPrimary(Any key)
  {
    throw new UnsupportedOperationException("getFromPrimary() " + getClass().toString());
  }

  public void addDataFieldReference(Any field,
                                    Any fQName,
                                    Any alias,
                                    Any initOverride,
                                    Any labelOverride,
                                    Any target)
  {
		throw new UnsupportedOperationException("addDataFieldReference " + getClass().toString());
  }
  
  public int resolveReferences(Descriptor d)
  {
		throw new UnsupportedOperationException("resolveReferences " + getClass().toString());
  }
  
  public void resetResolved(Descriptor d)
  {
		throw new UnsupportedOperationException("resetResolved " + getClass().toString());
  }
  
  public Map getListenerData()
  {
    if (listenerData_ == null)
      throw new IllegalStateException("No listener data has been set");
    
    return listenerData_;
  }
  
  // Called from parser
  public void setListenerData(Map listenerData)
  {
    listenerData_ = listenerData;
  }
  
  public Map getPrivileges()
  {
    return privileges_;
  }

  /**
   * Override.  If the key is <code>"properties"</code> then (make and)
   * return a property binding object.
   */
  public Any get (Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      handleNotExist(key); // throws
      return null;
    }
  }

  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      return null;
    }
  }

  public void setProxy()
  {
    // For use when received over the stream
    proxy_ = new ProxyDescriptor(fQName_);
  }
  
  protected final void setPrivileges(Map priv)
  {
    if (privileges_ != null)
      throw new IllegalArgumentException("Can't reset privileges");
    
    privileges_ = priv;
  }
  
	private void init()
	{
		formatStrings_ = AbstractComposite.simpleMap();
		enums_         = AbstractComposite.orderedMap();
		enumSymbols_   = AbstractComposite.orderedMap();
		titleStrings_  = AbstractComposite.simpleMap();
		keys_          = AbstractComposite.simpleMap();
		widths_        = AbstractComposite.simpleMap();
		privileges_    = AbstractComposite.simpleMap();
	}
}
