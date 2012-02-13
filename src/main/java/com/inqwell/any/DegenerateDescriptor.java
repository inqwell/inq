/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/DegenerateDescriptor.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:19 $
 */
 
package com.inqwell.any;

//import com.inqwell.any.Process;
import com.inqwell.any.*;
import  com.inqwell.any.io.AnyIOException;

/**
 * A degenerate descriptor implementation for non-managed objects
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 * @see com.inqwell.any.Any
 */ 
public class DegenerateDescriptor extends    AbstractAny
																	implements Descriptor,
																						 Cloneable
{
	static Any degenerateDescriptor__ = new ConstString("degenerateDescriptor");
	/**
	 * We can implement this method, so it just returns a normal AnyMap
	 */
  public Any newInstance()
  {
		return AbstractComposite.map();
  }
  
	public void construct(Map m, Transaction t) throws AnyException
	{
	}

  public void join(Map m, Transaction t) throws AnyException
  {
  }

	public void mutate(Map newVal,
                     Map oldVal,
                     Map context,
                     Transaction t) throws AnyException
  {
  }

  public void destroy(Map m, Transaction t) throws AnyException
  {
  }

  public void expire(Map m, Transaction t) throws AnyException
  {
  }

  public Any getName()
  {
		return degenerateDescriptor__;
	}
	
	public Any getDefaultAlias()
	{
    throw new UnsupportedOperationException();
	}
	
	public Any getFQName()
	{
    throw new UnsupportedOperationException();
	}

  public Any getPackage()
  {
    throw new UnsupportedOperationException();
  }

  public Map getUniqueKey(Map m)
  {
    throw new UnsupportedOperationException();
  }

	public Map getProto()
	{
    throw new UnsupportedOperationException();
	}
	
	public Any getDataField(Any fieldName, boolean mustResolve)
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
  
	public KeyDef getKey(Any keyName)
	{
    throw new UnsupportedOperationException();
	}

  public void joinForeign(Any fieldName, Transaction t)
  {
    throw new UnsupportedOperationException();
  }
  
	public KeyDef getPrimaryKey()
	{
    throw new UnsupportedOperationException();
	}

	public Map getEnums()
	{
    throw new UnsupportedOperationException();
	}

  public Map getEnumSymbols()
  {
    throw new UnsupportedOperationException();
  }
  
	public DomainAgent getDomainAgent()
	{
    throw new UnsupportedOperationException();
  }

	public boolean isEnum(Any key)
	{
    throw new UnsupportedOperationException();
	}

  public boolean isResolved(Descriptor d)
  {
		throw new UnsupportedOperationException();
  }
  
  public Set reportUnresolved()
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isDefunct()
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

	public void unmanage(Process p, Map m)
  {
    throw new UnsupportedOperationException();
  }

	public void resync (Process p, Map m) throws AnyException
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
	
	public void expire(Transaction t) throws AnyException
	{
    throw new UnsupportedOperationException();
	}

	public void destroy()
	{
    throw new UnsupportedOperationException();
	}

  public Any getFromPrimary(Any key)
  {
    throw new UnsupportedOperationException();
  }

  public void setProto(Map proto)
  {
    throw new UnsupportedOperationException();
	}

  public void addDataField(Any key, Any field)
  {
    throw new UnsupportedOperationException();
	}

  public void addDataFieldReference(Any field,
                                    Any fQName,
                                    Any alias,
                                    Any initOverride,
                                    Any labelOverride,
                                    Any target)
  {
    throw new UnsupportedOperationException();
  }
  
  public void addEnumValue(Any key, Any symbol, Any value, Any extValue)
  {
    throw new UnsupportedOperationException();
	}

  public void setFormat(Any key, Any formatString)
  {
    throw new UnsupportedOperationException();
  }
  
  public void setTitle(Any key, Any formatString)
  {
    throw new UnsupportedOperationException();
  }

  public void setWidth(Any key, Any width)
  {
    throw new UnsupportedOperationException();
  }

  public void setPrivilege(Any key, Map privileges)
  {
    throw new UnsupportedOperationException();
  }

	public void checkPrivilege(Process p, Any access, Any key)
	{
    throw new UnsupportedOperationException();
	}

	public void checkPrivilege(Process p, Any access, Map keys)
	{
    throw new UnsupportedOperationException();
	}
	
  public String getRenderer(Any key)
  {
    throw new UnsupportedOperationException();
  }
  
  public String getEditor(Any key)
  {
    throw new UnsupportedOperationException();
  }
  
	public void setDescriptorsInKeys()
  {
    throw new UnsupportedOperationException();
  }
  
  public String getFormat(Any key)
  {
    throw new UnsupportedOperationException();
  }
  
  public Any getTitle(Any key)
  {
    throw new UnsupportedOperationException();
  }

  public int getWidth(Any key)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean isTransient(Any key)
  {
    throw new UnsupportedOperationException();
  }
  
  public boolean equals(Object o)
  {
		if (AnyAlwaysEquals.isAlwaysEquals(o))
			return true;
		
		if (o == this)
		  return true;

		return (o instanceof DegenerateDescriptor);
  }

  public int hashCode()
  {
  	return 10;
  }
  
  public KeyDef locateRetrievalKey (Map keyVal) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  public KeyDef locateRelationshipKey (Descriptor from) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  public int resolveReferences(Descriptor d)
  {
    throw new UnsupportedOperationException();
  }
  
  public void resetResolved(Descriptor d)
  {
    throw new UnsupportedOperationException();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    return this;
  }
}
