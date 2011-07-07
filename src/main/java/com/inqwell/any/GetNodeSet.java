/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/GetNodeSet.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Gets the node set attribute of the specified Map.
 * <p>
 * Returns the given root node.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class GetNodeSet extends    AbstractFunc
                          implements Cloneable
{
  private Any node_;

  /**
   * 
   */
  public GetNodeSet(Any node)
  {
    node_    = node;
  }

  public Any exec(Any a) throws AnyException
  {
    Map node    = (Map)EvalExpr.evalFunc(getTransaction(),
                                         a,
                                         node_,
                                         Map.class);
    if (node == null)
      nullOperand(node_);

    return node.getNodeSet();
  }
  
  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(node_);
    return a.createIterator();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    GetNodeSet g = (GetNodeSet)super.clone();
    
    g.node_    = node_.cloneAny();        
    
    return g;
  }
}
