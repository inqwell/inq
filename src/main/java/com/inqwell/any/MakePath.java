/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/MakePath.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * Make a NodeSpecification evaluating any indirections.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class MakePath extends    AbstractFunc
                      implements Cloneable
{
	private Any    path_;
  
  public MakePath(Any path)
	{
    path_ = path;
	}
	
	public Any exec(Any a) throws AnyException
	{
    NodeSpecification path =
        (NodeSpecification)EvalExpr.evalFunc(getTransaction(),
                                             a,
                                             path_,
                                             NodeSpecification.class);
  
    path = path.resolveStart(a, getTransaction());

    return path;
  }

  public Object clone () throws CloneNotSupportedException
  {
    MakePath m = (MakePath)super.clone();
    
    // The parser ensures that the operand is always a NodeSpecification.
    // If it contains indirections then the resolving code creates a new
    // one and clones any functions as required.
    
    return m;
  }
}
