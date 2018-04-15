/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/IndexOf.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
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
public class IndexOf extends    AbstractFunc
									   implements Cloneable
{
  static private Any of__    = AbstractValue.flyweightString("of");
  static private Any other__ = AbstractValue.flyweightString("other");

	private Any container_;
	private Any contained_;

	private Any matchFunc_;

	public IndexOf(Any container, Any contained, Any matchFunc)
	{
		container_ = container;
    contained_ = contained;
    matchFunc_ = matchFunc;
	}

	public Any exec(Any a) throws AnyException
	{
	  Transaction t = getTransaction();

		Vectored container = (Vectored)EvalExpr.evalFunc(t,
																											 a,
																											 container_,
																											 Vectored.class);

    if (container == null)
      nullOperand(container_);

    Any contained = EvalExpr.evalFunc(t,
																		  a,
																		  contained_);

    if (contained == null)
      nullOperand(contained_);

    AnyFuncHolder.FuncHolder matchFunc =
            (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc(t,
                                                        a,
                                                        matchFunc_,
                                                        AnyFuncHolder.FuncHolder.class);

    if (matchFunc_ != null && matchFunc == null)
      nullOperand(matchFunc_);

    AnyInt ret = new AnyInt(-1);

    if (matchFunc == null)
		  ret.setValue(container.indexOf(contained));
    else
    {
      Any outerLoop = t.getLoop();
      Map args = AbstractComposite.simpleMap();
      args.add(of__, contained);

      try
      {
        Iter i = container.createIterator();
        int idx = 0;
        while (i.hasNext())
        {
          Any item = i.next();
          t.setLoop(item);
          args.replaceItem(other__, item);

          BooleanI b = (BooleanI)matchFunc.doFunc(t, args, a);
          if (b.getValue())
          {
            ret.setValue(idx);
            break;
          }
          idx++;
        }
      }
      finally
      {
        t.setLoop(outerLoop);
      }
    }

		return ret;
	}

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(container_);
		a.add(contained_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
		IndexOf c = (IndexOf)super.clone();
		c.container_ = container_.cloneAny();
    c.contained_ = contained_.cloneAny();
    c.matchFunc_ = AbstractAny.cloneOrNull(matchFunc_);
		return c;
  }
}
