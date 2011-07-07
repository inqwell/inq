/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Expression.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Defines the interface for function implementations which are expression
 * elements.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */ 
public interface Expression extends Func
{
	/**
	 * Resolve the expression's operands against the given root
	 * prior to executing it.
	 */
  public void resolveOperands(Any root) throws AnyException;
}
