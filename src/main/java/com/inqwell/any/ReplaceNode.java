/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ReplaceNode.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * A pre-canned service for a client or a server which inserts a received
 * <code>Any</code> as a node in the receiving process's universe,
 * essentially wrapping up <code>AddTo</code>
 * <p>
 * *** DEFUNCT ***
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class ReplaceNode extends Service
{
  
  private static Any servicePath__ = new ConstString("system.services.ReplaceNode");
  private static Any node__   = new ConstString("node");
  private static Any path__ = new ConstString("path");

  public ReplaceNode() throws AnyException
  {
    init();
  }

  private void init() throws AnyException
  {
    addParam (node__.toString(), AbstractComposite.simpleMap());
    addParam (path__.toString(), new AnyString());
		setExpr(new AddTo(new LocateNode
																	 (NodeSpecification.stack__.toString() +
																		"." +
																		node__.toString()),
															new LocateNode
																	 (NodeSpecification.stack__.toString() +
																		"." +
																		path__.toString())));
																		
		Catalog.catalog(this, servicePath__.toString(), Transaction.NULL_TRANSACTION);
	}
}
