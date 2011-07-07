/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/DeclareSet.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

public class DeclareSet extends    Declare
                        implements Cloneable
{
  private static final long serialVersionUID = 1L;
  
  private Array  compositeMembers_;
  
  public DeclareSet(Locate at, Any var, Array compositeMembers)
  {
    super(at, var);
    compositeMembers_ = compositeMembers;
  }
  
  public Any exec (Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    // We know its a set from the parser
    Set var = (Set)evaluateVar(a, t);
    
    if (compositeMembers_ != null)
      processCompositeMembers(compositeMembers_, var, a);

    placeVar(var, a, t);

    return var;
  }

  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(at_);
    a.add(var_);
    if (compositeMembers_ != null)
      a.add(compositeMembers_);
    return a.createIterator();
  }
  
  // There is no need for a clone function. The compositeMembers_ *array*
  // is readonly and we clone each entry as we go along.
  
  private void processCompositeMembers(Array compositeMembers,
                                       Set   s,
                                       Any   root) throws AnyException
  {
    if (compositeMembers != null)
    {
      Transaction t = getTransaction();
      
      for (int i = 0; i < compositeMembers.entries(); i++)
      {
        Any m = EvalExpr.evalFunc(t,
                                  root,
                                  compositeMembers.get(i).cloneAny());

        m = AbstractAny.ripSafe(m, getTransaction());
        m = t.readProperty(m);

        if (m != null && !s.contains(m))
        {
          s.add(m);
        }
      }
    }
  }
}
