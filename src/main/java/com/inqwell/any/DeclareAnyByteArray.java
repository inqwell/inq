/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/DeclareAnyByteArray.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

public class DeclareAnyByteArray extends    Declare
                                 implements Cloneable
{
  private static final long serialVersionUID = 1L;
  
  private Array  compositeMembers_;
  private Any    byteinit_;
  
  public DeclareAnyByteArray(Locate at, Any var, Any byteinit, Array compositeMembers)
  {
    super(at, var);
    byteinit_         = byteinit;
    compositeMembers_ = compositeMembers;
  }
  
  public Any exec (Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    // We know its an array from the parser
    AnyByteArray var = (AnyByteArray)evaluateVar(a, t);
    
    if (byteinit_ != null)
    {
      // Evaluate the expression that is used to dimension the array
      IntI dim = (IntI)EvalExpr.evalFunc(t,
                                         a,
                                         byteinit_.cloneAny());
      if (dim == null)
        nullOperand(byteinit_);
      
      var.setValue(new byte[dim.getValue()]);
    }
    
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
  
  private void processCompositeMembers(Array        compositeMembers,
                                       AnyByteArray a,
                                       Any          root) throws AnyException
  {
    
    if (compositeMembers != null)
    {
      Transaction t = getTransaction();
      AnyShort    b = new AnyShort();
      byte[] byteArray = a.getValue();
      if (byteArray == null)
        byteArray = new byte[compositeMembers.entries()];
      
      for (int i = 0; i < compositeMembers.entries(); i++)
      {
        Any m = EvalExpr.evalFunc(t,
                                  root,
                                  compositeMembers.get(i).cloneAny());

        m = AbstractAny.ripSafe(m, t);
        m = t.readProperty(m);

        if (m != null)
        {
          b.copyFrom(m);
          byteArray[i] = (byte)b.getValue();
        }
      }
      a.setValue(byteArray);
    }
  }
}
