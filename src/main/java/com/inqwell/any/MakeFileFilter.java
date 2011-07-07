/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/MakeFileFilter.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * Make an AnyFileFilter evaluating the argument expressions that it
 * requires. This method of creating AnyFileFilter objects is used by
 * the parser so that the arguments can be expressions in Inq script.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @deprecated No longer used
 */
public class MakeFileFilter extends    AbstractFunc
                            implements Cloneable
{
	private Any description_;
	private Any filterFunc_;
	private Any acceptDirs_;

  public MakeFileFilter(Any description,
                        Any filterFunc,
                        Any acceptDirs)
	{
		description_ = description;
		filterFunc_  = filterFunc;
		acceptDirs_  = acceptDirs;
	}
	
	public Any exec(Any a) throws AnyException
	{
    // The parser sees to it that there is always a description
		Any description = EvalExpr.evalFunc(getTransaction(),
                                        a,
                                        description_);
    
//    if (description == null)
//      description = AnyFileFilter.allFiles__;
    
    // Slightly new teritory - could be a call statement directly
    // or a FuncHolder that contains a call statement ?  Well not
    // for now.  No precedent so demand a FuncHolder. Hmmm see also
    // the parser code...
		AnyFuncHolder.FuncHolder filterFunc = (AnyFuncHolder.FuncHolder)EvalExpr.evalFunc
                                                (getTransaction(),
                                                 a,
                                                 filterFunc_,
																								 AnyFuncHolder.FuncHolder.class);

    BooleanI acceptDirs = (BooleanI)EvalExpr.evalFunc
                                                (getTransaction(),
                                                 a,
                                                 acceptDirs_,
																								 BooleanI.class);
    if (acceptDirs == null)
      acceptDirs = AnyBoolean.TRUE;

    return null;// new AnyFileFilter(description, filterFunc, acceptDirs);
	}

  public Object clone () throws CloneNotSupportedException
  {
    MakeFileFilter m = (MakeFileFilter)super.clone();
    
//    if (description_ != AnyFileFilter.allFiles__)
//      m.description_   = description_.cloneAny();
    
    m.filterFunc_ = filterFunc_.cloneAny();
    m.acceptDirs_ = acceptDirs_.cloneAny();
    
    return m;
  }
}
