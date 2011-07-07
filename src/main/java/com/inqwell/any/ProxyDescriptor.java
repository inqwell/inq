/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ProxyDescriptor.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:19 $
 */
 
package com.inqwell.any;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;

import com.inqwell.any.io.AnyIOException;

/**
 * A proxy descriptor implementation for the purposes of
 * passing in and out of serialization streams.  Resolves
 * and returns itself against the local cataloged version,
 * which therefore must exist.
 * 
 * @author $Author: sanderst $
 * @version $Revision: 1.5 $
 * @see com.inqwell.any.Any
 */ 
public final class ProxyDescriptor extends    AbstractAny
                                   implements Descriptor,
                                              Cloneable
{
	private Any fQName_;

	public ProxyDescriptor(Any fQName)
	{
		fQName_ = fQName;
	}
	
	public ProxyDescriptor(String fromString)
	{
		this(AbstractValue.flyweightString(fromString));
	}
	
	/**
	 * We can implement this method, so it just returns a normal AnyMap
	 */
  public Any newInstance()
  {
		return AbstractComposite.map();
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

  public Any getName()
  {
    throw new UnsupportedOperationException();
	}
	
	public DomainAgent getDomainAgent()
	{
    throw new UnsupportedOperationException();
  }

	public Any getDefaultAlias()
	{
    throw new UnsupportedOperationException();
	}
	
	public Any getFQName()
	{
		return fQName_;
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
	
  public KeyDef locateRetrievalKey (Map keyVal) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  public KeyDef locateRelationshipKey (Descriptor from) throws AnyException
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
  
	public KeyDef getKey(Any keyName)
	{
    throw new UnsupportedOperationException();
	}

	public KeyDef getPrimaryKey()
	{
    throw new UnsupportedOperationException();
	}

  public Map getListenerData()
  {
    throw new UnsupportedOperationException();
  }
  
  public void joinForeign(Any fieldName, Transaction t)
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

	public void expire(Transaction t) throws AnyException
	{
    throw new UnsupportedOperationException();
	}

	public void destroy()
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

  public void addEnumValue(Any key, Any symbol, Any value, Any extValue)
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
  
  public void addDataFieldReference(Any field,
                                    Any fQName,
                                    Any alias,
                                    Any initOverride,
                                    Any labelOverride,
                                    Any target)
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
  
  public boolean equals(Any o)
  {
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
  
  public boolean isTransient(Any key)
  {
    throw new UnsupportedOperationException();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    return this;
  }

  public String toString()
  {
		return fQName_.toString();
	}
  
	protected Object readResolve() throws ObjectStreamException
	{
		// Find the object in the local catalog and return that.
		LocateNode ln = new LocateNode(getFQName());
		
		Descriptor d = null;
		
		try
		{
//			ln.resolveNodePath(Catalog.instance().getCatalog());
//			System.out.println ("ProxyDescriptor : " + Catalog.instance().getCatalog());
//			System.out.println ("ProxyDescriptor : " + ln);
			  d = (Descriptor)EvalExpr.evalFunc(Transaction.NULL_TRANSACTION,
																					Catalog.instance().getCatalog(),
																					ln,
																					Descriptor.class);
		}
		catch(AnyException e)
		{
			throw new InvalidObjectException(e.getMessage());
		}
		
		if (d == null)
			throw new InvalidObjectException("Couldn't resolve descriptor " +
																			 getFQName());
		
		return d;
	}
}
