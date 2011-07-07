/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/NodesOf.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;


/**
 * Return a vector of nodes or indices that are represented by the given
 * path when applied to a specified starting node. 
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class NodesOf extends    AbstractFunc
                     implements Cloneable
{
  private static final long serialVersionUID = 1L;
  
  private Any     container_;
  private Any     path_;
  private boolean indices_;
  
  public NodesOf(Any container, Any path)
  {
    container_ = container;
    path_      = path;
  }

  public Any exec(Any a) throws AnyException
  {
    Map container = (Map)EvalExpr.evalFunc(getTransaction(),
                                           a,
                                           container_,
                                           Map.class);
    
    if (container == null)
      nullOperand(container_);

    NodeSpecification ns = (NodeSpecification)EvalExpr.evalFunc
                                     (getTransaction(),
                                      a,
                                      path_,
                                      NodeSpecification.class);

    if (ns == null)
      nullOperand(path_);
    
    final Array ret = AbstractComposite.array();
    
    // Handle $this. A little messy but needed to be consistent
    // with hard prefices.
    if (!NodeSpecification.prefices__.contains(ns.getFirst()))
      ret.add(a);
    
    // My god - I've used an anonymous inner class. Those day jobs are
    // really taking their toll...
    LocateNode ln = new LocateNode(ns)
    {
      protected void nodeFound(Any pathItem, Any node, Any parent)
      {
        // We get called the first time the path breaks
        if (node == null)
          return;
        
        if (indices_)
        {
          if (!(parent instanceof Vectored))
            throw new AnyRuntimeException("Not a vector");
          
          Vectored v = (Vectored)parent;
          if (parent instanceof Map)
            node = new ConstInt(v.indexOf(pathItem));
          else
            node = new ConstInt(v.indexOf(node));
        }
        ret.add(node);
      }
    };
    ln.execFunc(container);
    
    return ret;
  }

  public void setIndices(boolean indices)
  {
    indices_ = indices;
  }
  
  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(container_);
    a.add(path_);
    return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    NodesOf n = (NodesOf)super.clone();
    n.container_ = container_.cloneAny();
    n.path_      = path_.cloneAny();
    return n;
  }
}
