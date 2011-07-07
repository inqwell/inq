/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/Catalog.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import com.inqwell.any.util.CommandArgs;

/**
 * Catalog 
 */ 
public final class Catalog
{
	private static Catalog theCatalog__ = null;	
	private Map catalog_;
		
	public static Catalog instance()
	{
		if (theCatalog__ == null)
		{
			synchronized (Catalog.class)
			{
				if (theCatalog__ == null)
					theCatalog__ = new Catalog();
			}
		}
		return theCatalog__;
	}

	public static Any catalog (Any a, StringI s, Transaction t) throws AnyException
	{
		//System.out.println ("Catalog.catalog(): inside catalog(" + s + ")");
		Any a1 = Catalog.instance().catalog(a, new NodeSpecification(s), t);
		//System.out.println ("Catalog.catalog(): leaving catalog returning " + a1);
		return a1;
  }
//	
//	public static Any lookup (AnyString s) throws AnyException
//	{
//		//System.out.println ("Catalog.catalog(): inside lookup(" + s + ")");
//		Any a = Catalog.instance().lookup(new NodeSpecification(s));
//		//System.out.println ("Catalog.catalog(): returning " + a);
//		return a;
//	}
//
//	public static Any lookupClone (AnyString s) throws AnyException
//	{
//		Any a = Catalog.instance().lookup(new NodeSpecification(s));
//		return a.cloneAny();
//	}
//
	public static Any catalog (Any a, String s, Transaction t) throws AnyException
	{
	  return Catalog.catalog(a, new ConstString(s), t);
	}
//	
//	public static Any lookup (String s) throws AnyException
//	{
//	  return Catalog.lookup(new AnyString(s));
//	}
//		
//	public static Any lookupClone (String s) throws AnyException
//	{
//    return Catalog.lookupClone(new AnyString(s));
//  }
  

	private Catalog ()
	{
		catalog_ = AbstractComposite.managedMap();
	}
			
	public Map getCatalog ()
	{
		return catalog_;
	}
	
	// Add the given Any to the system catalog and return what was given
  // add transaction for PRIVILEGE
	public Any catalog (Any a, NodeSpecification s, Transaction t) throws AnyException
	{
		BuildNodeMap b = new BuildNodeMap(s, a, (Map)catalog_.buildNew(null));
		b.setTransaction(t);
    synchronized(Catalog.class)
    {
      b.exec (catalog_);
    }
		return a;
	}
  
  public Map getCommandArgs()
  {
    Map argsMap = (Map)getCatalog().get(CommandArgs.commandLine__);
    return argsMap;
  }
  
//	
//	public synchronized Any lookup (NodeSpecification s) throws AnyException
//	{
//		LocateNode l = new LocateNode(s);
//		Any a = l.exec (catalog_);
//		return a;
//	}
}
