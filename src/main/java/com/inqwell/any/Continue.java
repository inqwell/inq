/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Continue.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $ 
 * $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see 
 */

package com.inqwell.any;

/**
 * Terminates the <i>current iteration</i> of a loop operation by
 * throwing a <code>ContinueException</code>.
 * <p>
 * This function effects a change of control flow to prematurely
 * terminate a loop iteration.
 */
public class Continue extends    AbstractFunc
											implements Cloneable
{
	/**
	 * 
	 */
  public Continue()
  {
  }

  public Any exec(Any a) throws AnyException
  {
		throw new ContinueException();
  }
  
  public Object clone () throws CloneNotSupportedException
  {
		return super.clone();
  }
}
