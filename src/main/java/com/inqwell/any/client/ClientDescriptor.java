/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/ClientDescriptor.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.AbstractDescriptor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.Descriptor;
import com.inqwell.any.Globals;
import com.inqwell.any.KeyDef;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.Transaction;
import com.inqwell.any.io.AnyIOException;

/**
 * A <code>Descriptor</code> implementation instances of which
 * get created by the serialization of server-side implementations
 * when the destination is a simple client.
 */
public final class ClientDescriptor extends    AbstractDescriptor
                                    implements Descriptor,
                                               Cloneable
{
	public ClientDescriptor (Any name,
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
		super(name, alias, fQName, proto, keys, formatStrings, titleStrings, widths, enums, enumSymbols, privileges);
	}

	public void construct(Map m, Transaction t) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " construct"));
	}

	public void mutate(Map newVal,
                     Map oldVal,
                     Map context,
                     Transaction t) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " mutate"));
	}

	public void destroy(Map m, Transaction t) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " destroy"));
	}

  public void expire(Map m, Transaction t) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " destroy"));
	}

  public Map getUniqueKey(Map m)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " getUniqueKey"));
  }

	public KeyDef getPrimaryKey()
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " getPrimaryKey"));
  }

  public Any read(Process p, Map keyVal, int maxCount) throws AnyIOException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " read(keyVal)"));
  }

	public Any  read   (Process p, Any keyName, Map keyVal, int maxCount) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " read(keyName, keyVal)"));
	}

	public Any  read   (Process p, KeyDef keyDef, Map keyVal, int maxCount) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " read(keyDef, keyVal)"));
	}

  public void write(Process p, Map m) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " write(m)"));
  }
	
  public void write(Process p, Map k, Map o) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " write(k, o)"));
  }
	
  public boolean isResolved(Descriptor d)
  {
    return true;
  }
  
  public Any newInstance()
  {
		Map m = (Map)super.newInstance();
    m = m.bestowIdentity();
		m.setTransactional(true);
		return m;
	}
	
	public Map manage(Process p, Map m)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " manage(m)"));
  }

	public void resync (Process p, Map m) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " resync(p, m)"));
  }

	public void unmanage(Process p, Map m)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " manage(m)"));
  }

	public void delete(Process p, Map m) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " delete(m)"));
	}

	public void delete(Process p, Map k, Map o) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " delete(k, o)"));
	}
	
  public void addDataField(Any key, Any field)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " addDataField()"));
	}

  public void addEnumValue(Any key, Any symbol, Any value, Any extValue)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " addEnumValue()"));
	}

  public void setFormat(Any key, Any formatString)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " setFormat()"));
  }
  
  public void setNotifyOld(Descriptor d)
  {
    Process p = Globals.getProcessForThread(Thread.currentThread());
    Transaction t = p.getTransaction();
    t.setNotifyOld(d, true);
  }
  
  public void setForgetOld(Descriptor d)
  {
    Process p = Globals.getProcessForThread(Thread.currentThread());
    Transaction t = p.getTransaction();
    t.setNotifyOld(d, false);
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    return this;
  }
	
//	protected Object writeReplace() throws ObjectStreamException
//	{
//		if (toClient_)
//		{
//			toClient_ = false;
//			return this;
//		}
//		
//		ProxyDescriptor pd = new ProxyDescriptor(getFQName());
//		
//		return pd;
//	}
	
  
// Note: the resolving stream does this
//	protected Object readResolve() throws ObjectStreamException
//	{
//		// Put this object in the system catalog.  We don't mind
//		// overwriting what's already there as this means that
//		// every time a descriptor is serialized from the server
//		// we update our catalog.
//    setProxy();
//		BuildNodeMap b = new BuildNodeMap();
//		try
//		{
//			b.build(getFQName().toString(),
//							this,
//							Catalog.instance().getCatalog());
//		}
//		catch(AnyException e)
//		{
//			e.printStackTrace();
//		}
//		return this;
//	}
}
