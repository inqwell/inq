/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/ClientKey.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;

import java.io.ObjectStreamException;

/**
 * A <code>KeyDef</code> implementation instances of which
 * get created by the serialization of server-side implementations
 * when the destination is a simple client.
 */
public class ClientKey extends    AbstractKeyDef
											 implements KeyDef
{
  public ClientKey(Any        name,
									 Descriptor descriptor,
									 Array      fields,
                   Map        proto,
									 boolean    isUnique,
									 boolean    isNative,
									 boolean    isForeign,
                   int        maxCount)
  {
		super (name, descriptor, fields, proto, isUnique, isNative, isForeign, maxCount);
  }
  
  public Any read (Map keyVal, int maxCount) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " read"));
  }
  
  public boolean isPrimary()
  {
    return false;
  }
  
	public Map makeKeyVal (Map instanceVal)
	{
    Map m = super.makeKeyVal(instanceVal);
    Map ret = AbstractComposite.managedMap();
    ret.copyFrom(m);
		ret.setTransactional(true);
    return ret;
	}

	public Map getKeyProto()
	{
    Map m = super.getKeyProto();
    Map ret = AbstractComposite.managedMap();
    ret.copyFrom(m);
		ret.setTransactional(true);
    return ret;
	}
  
  public void write (Map instanceVal, Transaction t) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " write"));
  }
  
  public void write (Map keyVal, Map instanceVal, Transaction t) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " write"));
  }
  
  public void delete (Map instanceVal, Transaction t) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " delete"));
  }
  
  public void delete (Map keyVal, Map instanceVal, Transaction t) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " delete"));
  }
  
	public void resync (Transaction t, Map m) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " resync(t, m)"));
  }

	public void manage (Map instanceVal)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " manage"));
  }
  
	public void unmanage (Map instanceVal)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " unmanage"));
  }
  
  public Any getFromPrimary(Any key)
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " getFromPrimary"));
  }
}
