/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SetUniqueKey.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Sets the second operand as the unique key of the
 * first <code>Map</code> operand.
 * <p>
 * Returns the given root node.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class SetUniqueKey extends    AbstractFunc
									        implements Cloneable
{
  private Any node_;
  private Any key_;

	/**
	 * 
	 */
  public SetUniqueKey(Any node, Any key)
  {
    node_    = node;
    key_     = key;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
		Any key   = EvalExpr.evalFunc(t, a, key_);
    
    if (key == null)
      nullOperand(key_);
	
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

    // Check if the object is transactional. Can't change unique key  in that
    // case. (Should this be true for client also really?)
    if (Globals.isServer() && node.isTransactional())
      throw new AnyException("Cannot set the unique key of a transactional instance");

    node.setUniqueKey(key);
    		
	  return a;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(node_);
  	a.add(key_);
  	return a.createIterator();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    SetUniqueKey s = (SetUniqueKey)super.clone();
    
    s.node_   = node_.cloneAny();        
    s.key_    = key_.cloneAny();        
    
    return s;
  }
}
