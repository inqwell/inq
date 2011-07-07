/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/GetUniqueKey.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Returns the unique key of the
 * single <code>Map</code> operand.
 * <p>
 * Returns the given root node.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GetUniqueKey extends    AbstractFunc
									        implements Cloneable
{
  private Any node_;

	/**
	 * 
	 */
  public GetUniqueKey(Any node)
  {
    node_    = node;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    Map node  = (Map)EvalExpr.evalFunc(t,
																			 a,
																			 node_,
																			 Map.class);
    
    if (node == null)
      nullOperand(node_);
    
    // When map is in the transaction get the public copy. Only this has
    // the unique key (is that right or should it have been copied across?)
    if (t.getResolving() == Transaction.R_MAP)
      node = t.getLastTMap();

    Any ret = node.getUniqueKey();
    
    if (ret == null)
      throw new AnyException("Unique key is not initialised");
    
    return ret;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(node_);
 	  return a.createIterator();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    GetUniqueKey g = (GetUniqueKey)super.clone();
    
    g.node_   = node_.cloneAny();        
    
    return g;
  }
}
