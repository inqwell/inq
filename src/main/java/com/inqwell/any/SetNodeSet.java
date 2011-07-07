/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SetNodeSet.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Sets the node set attribute of the specified Map.
 * <p>
 * Returns the given root node.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class SetNodeSet extends    AbstractFunc
                          implements Cloneable
{
  private Any node_;
  private Any nodeSet_;

  /**
   * 
   */
  public SetNodeSet(Any node, Any nodeSet)
  {
    node_    = node;
    nodeSet_ = nodeSet;
  }

  public Any exec(Any a) throws AnyException
  {
    Any nodeSet = EvalExpr.evalFunc(getTransaction(), a, nodeSet_);
    
    if (nodeSet == null)
      nullOperand(nodeSet_);
  
    Map node    = (Map)EvalExpr.evalFunc(getTransaction(),
                                         a,
                                         node_,
                                         Map.class);
    if (node == null)
      nullOperand(node_);

    if (nodeSet instanceof Descriptor)
    {
      nodeSet = ((Descriptor)nodeSet).getFQName();
    }
    else if(nodeSet == AnyNull.instance())
    {
      nodeSet = null;
    }

    node.setNodeSet(nodeSet);
        
    return node;
  }
  
  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(node_);
    a.add(nodeSet_);
    return a.createIterator();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    SetNodeSet s = (SetNodeSet)super.clone();
    
    s.node_    = node_.cloneAny();        
    s.nodeSet_ = nodeSet_.cloneAny();        
    
    return s;
  }
}
