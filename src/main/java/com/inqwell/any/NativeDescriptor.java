/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/NativeDescriptor.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import com.inqwell.any.io.AnyIOException;

/**
 * A descriptor whose purpose is to define a single native type
 * with optional label, width formatting specification as for
 * managed object fields.
 */
public final class NativeDescriptor extends    AbstractDescriptor
                                    implements Descriptor
{
	public NativeDescriptor (Any name,
                           Any fQName,
                           Any inqPackage)
  {
    super(name, name, fQName, inqPackage);
  }
  
	public KeyDef getKey(Any keyName)
	{
    throw new UnsupportedOperationException();
	}

  public Map getUniqueKeys()
  {
    throw new UnsupportedOperationException();
	}
	
  public Map getAllKeys()
  {
		return null;
	}
  
  public Map getListenerData()
  {
    throw new UnsupportedOperationException();
  }
	
  public Any newInstance()
  {
    Any ret = getProto().get(getName());

		return ret.cloneAny();
	}
	
	/**
	 * Config setup method
	 */
	public void setDescriptorsInKeys()
	{
	}
	
  public String getFormat(Any key)
  {
    return super.getFormat(getName());
  }

  public Any getTitle(Any key)
  {
    return super.getTitle(getName());
  }

  public int getWidth(Any key)
  {
    return super.getWidth(getName());
  }
  
  public boolean isEnum(Any key)
  {
		//System.out.println ("isEnum() enums are: " + enums_);
    return super.isEnum(getName());
  }
  
  public boolean isResolved(Descriptor d)
  {
    return true;
  }
  
  public Set reportUnresolved()
  {
    return null;
  }
  
  public String getRenderer(Any key)
  {
    Any a = getProto().get(getName());
  	if (a instanceof BooleanI)
  	  return Descriptor.checkbox__;
  	else
		  return Descriptor.label__;
  }
  
  public String getEditor(Any key)
  {
		if (isEnum(key))
			return Descriptor.combobox__;
		else
		{
	  	Any a = getProto().get(getName());
	  	if (a instanceof BooleanI)
	  	  return Descriptor.checkbox__;
	  	else
			  return Descriptor.textfield__;
	  }
  }
  
  public void addDataFieldReference(Any field, Any fQName, Any alias, Any initOverride)
  {
		throw new UnsupportedOperationException();
  }
  
  public int resolveReferences(Descriptor d)
  {
  	return RESOLVE_NOTHING;
  }
  
  public void resetResolved(Descriptor d)
  {
  }
  
  /* ------------- */

  public Any read(Process p, Map keyVal, int maxCount) throws AnyIOException
  {
		throw new UnsupportedOperationException();
  }

	public Any  read   (Process p, Any keyName, Map keyVal, int maxCount) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

	public Any  read   (Process p, KeyDef keyDef, Map keyVal, int maxCount) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

  public void write(Process p, Map m) throws AnyException
  {
		throw new UnsupportedOperationException();
  }
	
  public void write(Process p, Map k, Map o) throws AnyException
  {
		throw new UnsupportedOperationException();
  }
	public Map manage(Process p, Map m)
  {
		throw new UnsupportedOperationException();
  }

	public void resync (Process p, Map m) throws AnyException
  {
		throw new UnsupportedOperationException();
  }

	public void unmanage(Process p, Map m)
  {
		throw new UnsupportedOperationException();
  }

	public void delete(Process p, Map m) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

	public void delete(Process p, Map k, Map o) throws AnyException
	{
		throw new UnsupportedOperationException();
	}
	public void construct(Map m, Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

	public void mutate(Map newVal,
                     Map oldVal,
                     Map context,
                     Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

	public void destroy(Map m, Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

  public void expire(Map m, Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

  public Map getUniqueKey(Map m)
  {
		throw new UnsupportedOperationException();
  }

	public KeyDef getPrimaryKey()
  {
		throw new UnsupportedOperationException();
  }

  public void addDataField(Any key, Any field)
  {
  	if (getProto().entries() != 0)
  	  throw new AnyRuntimeException("Native descriptors only have 1 field");
  	getProto().add(getName(), field);
  }
}
