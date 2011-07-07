/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ExecFuncHolder.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import com.inqwell.any.Call.CallStackEntry;

/**
 * Execute the function given by the AnyFuncHolder operand.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 */
public class ExecFuncHolder extends    AbstractFunc
														implements Cloneable
{
	private Any funcHolder_;
	private Any args_;
	
	public ExecFuncHolder()
	{
	}
	
	public ExecFuncHolder(Any funcHolder)
	{
		this(funcHolder, null);
	}
	
	public ExecFuncHolder(Any funcHolder, Any args)
	{
		funcHolder_ = funcHolder;
		args_       = args;
	}
	
	public Any exec(Any a) throws AnyException
	{
    Transaction t = getTransaction();
    
		AnyFuncHolder.FuncHolder funcHolder = (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc
																								(t,
																								 a,
																								 funcHolder_,
																								 AnyFuncHolder.FuncHolder.class);
    
//    if (funcHolder == null)
//      nullOperand(funcHolder_);

		Map args = (Map)EvalExpr.evalFunc(t,
																			a,
																			args_,
																			Map.class);

		// In fact, the implementation of the <inq> language is such that
		// args_ is already a Map, i.e. when the expression enclosing us
		// was cloned we got a unique map in args_ already, we didn't
		// just clone a LocateNode pointing to the args.  Each individual
		// argument may well be a LocateNode, so we must evaluate them, but
		// we can just add whatever is returned to the target args list
																			
		if (funcHolder != null && args != null)
		{
			Map targetArgs = AbstractComposite.simpleMap();
			
			Iter i = args.createKeysIterator();
			while (i.hasNext())
			{
				Any pn = i.next();
				Any arg = args.get(pn);
				arg = EvalExpr.evalFunc(t,
																a,
																arg);
        
				arg = AbstractAny.ripSafe(arg, t);
        targetArgs.add(pn, arg);
        /*
				if (arg instanceof Value)
				{
					targetArgs.add(pn, arg.cloneAny());
				}
				else
				{
					targetArgs.add(pn, arg);
				}
        */
			}
			args = targetArgs;
		}
		
		if (funcHolder != null)
    {
      if (!getTransaction().getCallStack().isEmpty())
      {
        CallStackEntry se = (CallStackEntry)getTransaction().getCallStack().peek();
        se.setLineNumber(getLineNumber());
      }
      Any ret = funcHolder.doFunc(getTransaction(), args, a);
      ret = AbstractAny.ripSafe(ret, t);
		  return ret;
    }
		else
		  return null;
//		Func f = funcHolder.getFunc();
//		Any ret = null;
//		if (f != null)
//		{
//			f = (Func)f.cloneAny();
//			f.setTransaction(getTransaction());
//			ret = f.exec(a);
//		}
//		
//		return ret;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		ExecFuncHolder f = (ExecFuncHolder)super.clone();
		f.funcHolder_ = AbstractAny.cloneOrNull(funcHolder_);
		f.args_       = AbstractAny.cloneOrNull(args_);
		return f;
  }
	
}
