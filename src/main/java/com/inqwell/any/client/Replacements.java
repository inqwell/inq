/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/Replacements.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;
import java.util.HashMap;
import com.inqwell.any.Process;
import com.inqwell.any.identity.AnyMapEgDecor;
import com.inqwell.any.io.AbstractStreamFunc;
import com.inqwell.any.io.ReplacingStream;
import com.inqwell.any.tools.AnyClient;
import java.io.InvalidObjectException;

/**
 * Just sets up the replacement maps for client-side object
 * serialization.
 */
public class Replacements
{      
	static public java.util.Map clientToServer__         = new HashMap();
	static public java.util.Map clientToNativeStream__   = new HashMap();
	static public java.util.Map clientFromServer__       = new HashMap();
	static public java.util.Map clientFromNativeStream__ = new HashMap();

	// the replacement/resolving maps for xmlStreams
	static public java.util.Map clientFromXMLStream__ = new HashMap();
	static public java.util.Map clientToXMLStream__ = new HashMap();

	static
	{
	
		clientToServer__.put(ClientDescriptor.class,
												 new ClientDescriptorClientToServer());
												 
		//clientToServer__.put(ClientObject.class,
												 //new ClientObjectToServer());

		clientToServer__.put(AnyMapEgDecor.class,
												 new AnyMapEgDecorToAnywhere());

    // See InstanceHierarchyMap class below.
		//clientToServer__.put(InstanceHierarchyMap.class,
		//										 new ClientObjectToServer());
												 
		clientToServer__.put(UserProcess.class,
												 new ProcessClientToAny());
												 
		clientToServer__.put(AnyClient.ClientUserProcess.class,
												 new ProcessClientToAny());
                         
                         
												 
		clientToNativeStream__.put(ClientDescriptor.class,
															 new AbstractDescriptorToNativeStream());
	
		clientToNativeStream__.put(NativeDescriptor.class,
															 new AbstractDescriptorToNativeStream());
	
		clientToNativeStream__.put(ClientObject.class,
															 new ClientObjectToNativeStream());
                               
		clientToXMLStream__.put(ClientDescriptor.class,
														new AbstractDescriptorToXMLStream());

		clientToXMLStream__.put(NativeDescriptor.class,
														new AbstractDescriptorToXMLStream());

		clientToXMLStream__.put(AnyMapEgDecor.class,
													  new AnyMapEgDecorToAnywhere());


		clientFromServer__.put(ClientDescriptor.class,
													 new ClientDescriptorFromServer());
		clientFromServer__.put(NativeDescriptor.class,
													 new NativeDescriptorFromServer());
    clientFromServer__.put(ProxyDescriptor.class,
                           new ProxyDescriptorFromAnywhere());
		clientFromServer__.put(SerializedProcess.class,
													 new ProcessFromServer());

		clientFromNativeStream__.put(ProxyDescriptor.class,
																 new ProxyDescriptorFromAnywhere());

		clientFromXMLStream__.put(ProxyDescriptor.class,
				 											new ProxyDescriptorFromAnywhere());

		
	}

	
	static private class ClientDescriptorClientToServer extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			ClientDescriptor d = (ClientDescriptor)a;
			Descriptor pd = d.getProxy();

			return pd;
		}
	}
	
	static private class AbstractDescriptorToNativeStream extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			AbstractDescriptor d = (AbstractDescriptor)a;
			Descriptor pd = d.getProxy();

			return pd;
		}
	}

	/**
	 * Convert AbstractDescriptor to XML Stream via a ProxyDescriptor
	 * Implememtation is exactly same as ClientDescriptorClientToServer and AbstractDescriptorToNativeStream
	 * but we create a special handling like this, just in case.
	 * @author bharat
	 *
	 */
	static private class AbstractDescriptorToXMLStream extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			AbstractDescriptor d = (AbstractDescriptor)a;
			Descriptor pd = d.getProxy();

			return pd;
		}
	}

	static private class ClientObjectToNativeStream extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			// This function has the effect of writing the object
			// as is to the stream, with the exception that the
			// transaction flag is reset.  We can't just reset
			// in the current object as we don't want to affect
			// the in-memory objects, so we take a copy.
			Map mo = (Map)a;
			Map m = new ClientObject();

			Iter i = mo.createKeysIterator();
			while (i.hasNext())
			{
				Any k = i.next();
				m.add(k, mo.get(k));
			}

			// We can put our descriptor in as this will also be replaced
			// by the serialization process
			m.setDescriptor(mo.getDescriptor());

			// and we are NOT transctional
			m.setTransactional(false);
			return m;
		}
	}	
  
	static private class AnyMapEgDecorToAnywhere extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
      AnyMapEgDecor m = (AnyMapEgDecor)a;
      
      // Strip off the decoration so the object can be used directly
      // for creation (when sending to server).  Apart from anything else,
      // the txn flag is
      // reset on the receiving side anyway (to avoid the need to
      // create additional objects here) and it doesn't really make
      // sense to have a identity-decorated, non-transactional
      // object.
      return m.getInstance();
		}
	}
  
	static private class ClientObjectToServer extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			// This function has the effect of writing the object
			// as is to the stream, with the exception that the
			// transaction flag is reset.  We can't just reset
			// in the current object as we don't want to affect
			// the in-memory objects, so we take a copy.
			Map mo = (Map)a;
			Map m = new ClientObject();
			Iter i = mo.createKeysIterator();
			while (i.hasNext())
			{
				Any k = i.next();
				m.add(k, mo.get(k));
			}

			// We can put our descriptor in as this will also be replaced
			// by the serialization process
			m.setDescriptor(mo.getDescriptor());
			
			// put in any unique key
			m.setUniqueKey(mo.getUniqueKey());

			// and we are NOT transctional
			m.setTransactional(false);
			return m;
		}
	}

  // Not currently used.  Intended to turn off the txn flag when
  // sending to the server but we can save on object creation if
  // we do it on the receiving side.
	static private class InstanceHierarchyMapToServer extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			// This function has the effect of writing the object
			// as is to the stream, with the exception that the
			// transaction flag is reset.  We can't just reset
			// in the current object as we don't want to affect
			// the in-memory objects, so we take a copy.
			Map mo = (Map)a;
			Map m = new InstanceHierarchyMap();
			Iter i = mo.createKeysIterator();
			while (i.hasNext())
			{
				Any k = i.next();
				m.add(k, mo.get(k));
			}

			// We can put our descriptor in as this will also be replaced
			// by the serialization process
			m.setDescriptor(mo.getDescriptor());
			
			// put in any unique key
			m.setUniqueKey(mo.getUniqueKey());

			// and we are NOT transctional
			m.setTransactional(false);
			return m;
		}
	}

	static private class ProcessClientToAny extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			Process p  = (Process)a;
			Process cp = new SerializedProcess(p.getCatalogPath());
			
			// Don't bother to write the child processes to the stream.
			// If this process is present in the destination it will
			// be resolved to that, and the children are already there.
			return cp;
		}
	}

	static private class ClientDescriptorFromServer extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			ClientDescriptor cd = (ClientDescriptor)a;
			// Put this object in the system catalog.  We don't mind
			// overwriting what's already there as this means that
			// every time a descriptor is serialized from the server
			// we update our catalog.
      cd.setProxy();
			BuildNodeMap b = new BuildNodeMap();
			try
			{
				b.build(cd.getFQName().toString(),
								cd,
								Catalog.instance().getCatalog());
			}
			catch(AnyException e)
			{
				e.printStackTrace();
			}
			return cd;
		}
	}
	
	static private class NativeDescriptorFromServer extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			NativeDescriptor nd = (NativeDescriptor)a;
			// Put this object in the system catalog.  We don't mind
			// overwriting what's already there as this means that
			// every time a descriptor is serialized from the server
			// we update our catalog.
      nd.setProxy();
			BuildNodeMap b = new BuildNodeMap();
			try
			{
				b.build(nd.getFQName().toString(),
								nd,
								Catalog.instance().getCatalog());
			}
			catch(AnyException e)
			{
				e.printStackTrace();
			}
			return nd;
		}
	}
	
	static private class ProcessFromServer extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			Process p = (Process)a;
			
			ProxyProcess cp = new ProxyProcess(p.getCatalogPath());

			Iter i = p.createKeysIterator();
			while (i.hasNext())
			{
				Any k = i.next();
				Any v = p.get(k);
				cp.add(k, v);
			}
      //System.out.println("ProxyProcess: " + cp + " at " + cp.getCatalogPath());

			return cp;
		}
	}
	
	static private class SerializedProcessFromServer extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			Process p  = (Process)a;
			
			// By replacing with a ProxyService, which is based on an
			// InstanceHierarchyMap, we build a structure that is
			// event propagatory in the client.
			Process cp = new ProxyProcess(p.getCatalogPath());
			
			Iter i = p.createKeysIterator();
			while (i.hasNext())
			{
				Any k = i.next();
				cp.add(k, p.get(k));
			}

			return cp;
		}
	}
	
	static private class ProxyDescriptorFromAnywhere extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			ProxyDescriptor pd = (ProxyDescriptor)a;
			// Find the object in the local catalog and return that.  If none
			// then tolerate the degenerate descriptor.
			LocateNode ln = new LocateNode(pd.getFQName());

			Descriptor d = null;

			try
			{
				d = (Descriptor)EvalExpr.evalFunc(Transaction.NULL_TRANSACTION,
																					Catalog.instance().getCatalog(),
																					ln,
																					Descriptor.class);
			}
			catch(AnyException e)
			{
				throw new RuntimeContainedException(new InvalidObjectException
																							 (e.getMessage()));
			}

			if (d == null)
			{
				d = Descriptor.degenerateDescriptor__;
			}

			return d;
		}
	}

}

