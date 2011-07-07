/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/Replacements.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.server;

import com.inqwell.any.*;

import java.util.HashMap;
import com.inqwell.any.Process;
import com.inqwell.any.client.ClientDescriptor;
import com.inqwell.any.client.ClientKey;
import com.inqwell.any.io.AbstractStreamFunc;
import com.inqwell.any.io.ReplacingStream;
import com.inqwell.any.io.inq.RemoteDescriptor;
import com.inqwell.any.io.inq.AsIsDescriptorDecor;
import com.inqwell.any.io.inq.ClientMonitor;
import com.inqwell.any.io.inq.ServerMonitor;
import com.inqwell.any.io.inq.IoProcess;
import com.inqwell.any.io.inq.LockerProxy;
import com.inqwell.any.identity.AnyMapEgDecor;
import com.inqwell.any.beans.ClassMap;
import com.inqwell.any.channel.InputChannel;
import java.io.InvalidObjectException;
import java.io.Serializable;

/**
 * Just sets up the replacement maps for server-side object
 * serialization.
 */
public class Replacements
{      
	static public java.util.Map serverToClient__         = new HashMap();
	static public java.util.Map serverToNativeStream__   = new HashMap();

	static public java.util.Map serverIOToServer__       = new HashMap();
	
	static public java.util.Map serverFromClient__       = new HashMap();
	static public java.util.Map serverFromNativeStream__ = new HashMap();

	// the replacement/resolving maps for xmlStreams
	static public java.util.Map serverFromXMLStream__    = new HashMap();
	static public java.util.Map serverToXMLStream__      = new HashMap();

	static
	{
    // Inq server to single-user client
		serverToClient__.put(BOTDescriptor.class,
												 new BOTDescriptorServerToClient());
		serverToClient__.put(RemoteDescriptor.class,
												 new RemoteDescriptorServerToClient());
		serverToClient__.put(CachingKey.class,
												 new CachingKeyServerToClient());
		serverToClient__.put(ManagedObject.class,
												 new ManagedObjectServerToClient());
		serverToClient__.put(SimpleKey.class,
												 new SimpleKeyServerToClient());
		serverToClient__.put(UserProcess.class,
												 new ProcessServerToAny());
		serverToClient__.put(ClientMonitor.class,
												 new ProcessServerToAny());
		serverToClient__.put(ServerMonitor.class,
												 new ProcessServerToAny());
		serverToClient__.put(ServerListener.class,
												 new ProcessServerToAny());
		serverToClient__.put(DeadlockScanner.class,
												 new ProcessServerToAny());
                         
    
    serverIOToServer__.put(AnyMapEgDecor.class,
                          new AnyMapEgDecorIOToServer());
    serverIOToServer__.put(ManagedObject.class,
                          new ManagedObjectIOToServer());
    serverIOToServer__.put(BOTDescriptor.class,
                          new BOTDescriptorIOToServer());
    serverIOToServer__.put(AsIsDescriptorDecor.class,
                          new AsIsDescriptorDecorIOToServer());
    /*
    // This would be for multiple-hop remoteness!
    serverIOToServer__.put(RemoteDescriptor.class,
                          new RemoteDescriptorIOToServer());
    */
        
    
    // Inq server to native object stream
		serverToNativeStream__.put(BOTDescriptor.class,
															 new AbstractDescriptorToNativeStream());
		serverToNativeStream__.put(ClientDescriptor.class,
															 new AbstractDescriptorToNativeStream());
		serverToNativeStream__.put(NativeDescriptor.class,
															 new AbstractDescriptorToNativeStream());
		serverToNativeStream__.put(ManagedObject.class,
															 new ManagedObjectToNativeStream());
		serverToNativeStream__.put(ClientMonitor.class,
															 new ProcessServerToAny());
		serverToNativeStream__.put(ServerMonitor.class,
															 new ProcessServerToAny());
		serverToNativeStream__.put(IoProcess.class,
															 new ProcessServerToAny());
		serverToNativeStream__.put(ServerListener.class,
															 new ProcessServerToAny());
		serverToNativeStream__.put(DeadlockScanner.class,
															 new ProcessServerToAny());

    // Inq server to XML stream
		serverToXMLStream__.put(BOTDescriptor.class,
												 		new AbstractDescriptorToXMLStream());
		serverToXMLStream__.put(ClientDescriptor.class,
				 										new AbstractDescriptorToXMLStream());
		serverToXMLStream__.put(NativeDescriptor.class,
				 										new AbstractDescriptorToXMLStream());
		serverToXMLStream__.put(AnyMapEgDecor.class,
														new AnyMapEgDecorToXMLStream());
		serverToXMLStream__.put(ManagedObject.class,
														new ManagedObjectToXMLStream());

		

		// Inq server from single-user client
		serverFromClient__.put(ProxyDescriptor.class,
													 new ProxyDescriptorFromClient());
		serverFromClient__.put(SerializedProcess.class,
													 new ProcessFromClient());
		serverFromClient__.put(LockerProxy.class,
													 new ProcessFromClient());
		serverFromClient__.put(ProxyProcess.class,
													 new ProcessFromClient());
		serverFromClient__.put(InstanceHierarchyMap.class,
													 new InstanceHierarchyMapFromClient());
		serverFromClient__.put(ClientObject.class,
													 new ClientObjectFromClient());

    // Inq server from native object stream
		serverFromNativeStream__.put(ProxyDescriptor.class,
																 new ProxyDescriptorFromClient());  // as above
    // Inq server from XML stream
		serverFromXMLStream__.put(ProxyDescriptor.class,
															new ProxyDescriptorFromClient());

	}

	
	static private class BOTDescriptorServerToClient extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			BOTDescriptor d = (BOTDescriptor)a;
      Any ret;
      
      if (s.getReplacementInfo(d) == BOTDescriptor.REPLACE)
      {
  			ClientDescriptor cd = new ClientDescriptor(d.getName(),
  																								 d.getDefaultAlias(),
  																								 d.getFQName(),
  																								 d.getProto(),
  																								 d.getAllKeys(),
  																								 d.getFormatStrings(),
  																								 d.getTitleStrings(),
  																								 d.getWidths(),
  																								 d.getEnums(),
  																								 d.getEnumSymbols(),
                                                   d.getPrivileges());
        
        //s.setReplacementInfo(d, BOTDescriptor.PROXY);
  			ret = cd;
      }
      else
        ret = d.getProxy();
      
			return ret;
		}
	}
	
	static private class RemoteDescriptorServerToClient extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			RemoteDescriptor rd = (RemoteDescriptor)a;
			BOTDescriptor    bd = (BOTDescriptor)rd.getDescriptor();
			
      Descriptor cd = null;
			
			if (bd == null)
			{
        cd = Descriptor.degenerateDescriptor__;
			}
			else
			{
        cd = new ClientDescriptor(bd.getName(),
                                  bd.getDefaultAlias(),
                                  bd.getFQName(),
                                  bd.getProto(),
                                  bd.getAllKeys(),
                                  bd.getFormatStrings(),
                                  bd.getTitleStrings(),
                                  bd.getWidths(),
                                  bd.getEnums(),
                                  bd.getEnumSymbols(),
                                  bd.getPrivileges());
			}
			


			return cd;
		}
	}
	
	static private class CachingKeyServerToClient extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			CachingKey k = (CachingKey)a;
			ClientKey ck = new ClientKey(k.getName(),
																	 k.getDescriptor(),
																	 k.getFields(),
                                   k.getKeyProto(),
																	 k.isUnique(),
																	 k.isNative(),
																	 k.isForeign(),
                                   k.getMaxCount());

			return ck;
		}
	}
	
	static private class ManagedObjectServerToClient extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
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

			// and we are transctional
			m.setTransactional(a.isTransactional());
			//m.setTransactional(true);
			return m;
		}
	}
	
	static private class SimpleKeyServerToClient extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			SimpleKey k = (SimpleKey)a;
			ClientKey ck = new ClientKey(k.getName(),
																	 k.getDescriptor(),
																	 k.getFields(),
                                   k.getKeyProto(),
																	 k.isUnique(),
																	 k.isNative(),
																	 k.isForeign(),
                                   k.getMaxCount());

			return ck;
		}
	}
	
	// This class is listed against all server-side concrete
	// classes implementing Process.  Can't list against
	// a single interface as this would cause object replacement
	// to recurse.
	static private class ProcessServerToAny extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			Process p  = (Process)a;
			Process cp = new SerializedProcess(p.getCatalogPath());
			
			// All current Process implementations are also Maps
			// and we put in the children
			Iter i = p.createKeysIterator();
			while (i.hasNext())
			{
				Any k = i.next();
				Any v = p.get(k);
				if (v instanceof InputChannel)
          continue;
        if (!(v instanceof Serializable))
          continue;
				cp.add(k, v);
			}
      //System.out.println("SerializedProcess: " + cp);
			return cp;
		}
	}
	
	// ---- Replacements for i/o connection to a client server from a
	//      hosting server
	
	// This one only gets used if the object in question is not already
	// decorated by an identity wrapper.
	static private class ManagedObjectIOToServer extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			Map mo = (Map)a;
			Map m = AbstractComposite.simpleMap();

			Iter i = mo.createKeysIterator();
			while (i.hasNext())
			{
				Any k = i.next();
				m.add(k, mo.get(k));
			}

			return m;
		}
	}
	
	// This one effectively strips off the identity wrapper, allowing
	// the i/o client server to control this issue.
	static private class AnyMapEgDecorIOToServer extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			Map mo = (Map)a;
			Map m = AbstractComposite.simpleMap();

			Iter i = mo.createKeysIterator();
			while (i.hasNext())
			{
				Any k = i.next();
				m.add(k, mo.get(k));
			}

			return m;
		}
	}

  // Note this does for i/o and monitor process connections but we
  // vary the replacement. See IoProcess.java.fetchDescriptor() where
  // we want to send the real descriptor.
	static private class BOTDescriptorIOToServer extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
      BOTDescriptor bd  = (BOTDescriptor)a;
      Descriptor    ret = bd;
      if (s.getReplacementInfo(bd) != BOTDescriptor.THIS)
        ret = new ProxyDescriptor(bd.getFQName());

			return ret;
		}
	}
	
	static private class AsIsDescriptorDecorIOToServer extends AbstractStreamFunc
  {
		public Any exec(Any a, ReplacingStream s)
		{
      // Set the stream up NOT to replace the object.  See above
      // and AsIsDescriptorDecor.writeObject()
      s.setReplacementInfo(BOTDescriptor.THIS, null);
      return a;
		}
  }
  
	// ---- END Replacements for i/o connection to a client server.
		
	static private class AbstractDescriptorToNativeStream extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			AbstractDescriptor d = (AbstractDescriptor)a;
      //ProxyDescriptor pd = new ProxyDescriptor(d.getFQName());
      Descriptor pd = d.getProxy();

			return pd;
		}
	}
	
	static private class ManagedObjectToNativeStream extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
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

	static private class AnyMapEgDecorToXMLStream extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
      AnyMapEgDecor m = (AnyMapEgDecor)a;
      
      // Strip off the decoration and return the internal instance.
      return m.getInstance();
		}
	}

	static private class ManagedObjectToXMLStream extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
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
	
	static private class ProxyDescriptorFromClient extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			ProxyDescriptor pd = (ProxyDescriptor)a;
			// Find the object in the local catalog and return that.
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
				throw new RuntimeContainedException(new InvalidObjectException
						("Couldn't resolve descriptor " + pd.getFQName()));

			return d;
		}
	}

	static private class ProcessFromClient extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			Process cp = (Process)a;
			
			// Find the object in the local process list and return that.
			LocateNode ln = new LocateNode(cp.getCatalogPath());

			Process p = null;

			try
			{
				p = (Process)EvalExpr.evalFunc(Transaction.NULL_TRANSACTION,
                                       Catalog.instance().getCatalog(),
                                       ln,
                                       Process.class);
			}
			catch(AnyException e)
			{
				throw new RuntimeContainedException(new InvalidObjectException
																							 (e.getMessage()));
			}

			if (p == null)
				throw new RuntimeContainedException(new InvalidObjectException
						("Couldn't resolve process " + cp.getCatalogPath()));

			return p;
		}
	}

	static private class InstanceHierarchyMapFromClient extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			Map m = (Map)a;
			
      m.setTransactional(false);
      
			return m;
		}
	}

	static private class ClientObjectFromClient extends AbstractStreamFunc
	{
		public Any exec(Any a, ReplacingStream s)
		{
			Map m = (Map)a;
			
      m.setTransactional(false);
      
			return m;
		}
	}

}

