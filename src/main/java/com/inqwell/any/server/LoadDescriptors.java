/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/server/LoadDescriptors.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.server;

import com.inqwell.any.*;

/**
 * A pre-canned service on the INQ server.
 * <p>
 * This service locates all <code>Descriptor</code> objects in
 * the system catalog and builds them into a <code>Set</code>
 * for return to a client.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class LoadDescriptors extends Service
{

  private static Any servicePath__ = new ConstString("system.server.services.LoadDescriptors");
  private static Any dummy__       = new ConstString("dummy__");

  public LoadDescriptors() throws AnyException
  {
    init();
  }

  private void init() throws AnyException
  {
		setExpr(new FindDescriptors());

		Catalog.catalog(this, servicePath__.toString(), Transaction.NULL_TRANSACTION);
		setBaseURL(new ConstString("internal://LoadDescriptors"));
		setFQName(new ConstString("system:LoadDescriptors"));
	}

	static class FindDescriptors extends    AbstractFunc
															 implements Cloneable
	{

		public FindDescriptors () {}

		/**
		 *
		 */
		public Any exec(Any a) throws AnyException
		{
			Set s = AbstractComposite.set();

			BreadthFirstIter i = new BreadthFirstIter(Catalog.instance().getCatalog());
      i.setCyclicSafe(true);

			while (i.hasNext())
			{
				Any any = i.next();

				if (any instanceof Descriptor)
				  s.add(any);
			}

			Map args = AbstractComposite.simpleMap();
			args.add(dummy__, s);
		  SendRequest sr = new SendRequest (new ConstString("system.services.NullService"),
													              args,
													              new LocateNode (ServerConstants.ROCHANNEL));

	    sr.setTransaction(getTransaction());
		  sr.setPropagateContext(false);
		  sr.exec(a);

			return s;
		}

		public Object clone() throws CloneNotSupportedException
		{
			return super.clone();
		}

	}
}


