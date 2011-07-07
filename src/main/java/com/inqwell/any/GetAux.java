/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/GetAux.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Returns the unique key of the
 * single <code>Map</code> operand.
 * <p>
 * Returns the given root node.
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @deprecated to be removed
 */
public class GetAux extends    AbstractFunc
                          implements Cloneable
{
  private Any node_;

  /**
   * 
   */
  public GetAux(Any node)
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
    
    Any ret = node.getAux();
    
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
    GetAux g = (GetAux)super.clone();
    
    g.node_   = node_.cloneAny();        
    
    return g;
  }
}
