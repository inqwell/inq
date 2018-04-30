/**
 * Copyright (C) 2018 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/MinOutOf.java $
 * $Author: sanderst $
 */
package com.inqwell.any;

/**
 * Return index in first operand of the second as an AnyInt. Returns -1
 * if the second operand is not contained within the first.
 * <p>
 * The first operand must support vector access. If the optional
 * function argument is not provided the natural equality of contained
 * and the elements is used. If the function is provided it will be
 * passed two arguments: <code>"of"</code> is the second argument
 * and <code>"other"</code> is the child of container, so within this
 * function for example you could
 * say <code>of.TradeDate == other.Trade.TradeDate;</code>
 * </p>
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class MinOutOf extends    AbstractFunc
                      implements Cloneable
{
  private Any op1_;
  private Any op2_;

  private Array more_;
  private Any minFunc_;

  public MinOutOf(Any op1, Any op2, Array more)
  {
    op1_  = op1;
    op2_  = op2;
    more_ = more;
  }

  public Any exec(Any a) throws AnyException
  {
    Transaction t = getTransaction();

    Any op1 = EvalExpr.evalFunc(t,
            a,
            op1_);

    if (op1 == null)
      nullOperand(op1_);

    Any op2 = EvalExpr.evalFunc(t,
            a,
            op2_);

    if (op2 == null)
      nullOperand(op2_);

    OperatorVisitor min = new Min();
    min.setParam(a);
    min.setTransaction(t);

    Any ret = min.doOperation(op1, op2);

    if (more_ != null)
    {
      Array more = (Array)EvalExpr.evalFunc(t,
                                            a,
                                            more_,
                                            Array.class);

      if (more == null)
        nullOperand(more_);

      for (int i = 0; i < more.entries(); i++)
      {
        Any moreItem = EvalExpr.evalFunc(t,
                                         a,
                                         more.get(i));

        if (AnyNull.isNull(moreItem))
          continue;

        ret = min.doOperation(ret, moreItem);
      }
    }

    return ret;
  }

  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(op1_);
    a.add(op2_);
    if (more_ != null)
      a.add(more_);
    return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    MinOutOf m = (MinOutOf)super.clone();
    m.op1_ = op1_.cloneAny();
    m.op2_ = op2_.cloneAny();
    m.more_ = (Array)AbstractAny.cloneOrNull(more_);
    return m;
  }
}
