/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/SimpleStack.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class SimpleStack extends    AbstractStack
											   implements Stack,
																	  Cloneable
{
  public SimpleStack () { super(); }
	protected void beforePush(Any item) {}
	protected void afterPush(Any item) {}
	protected void emptying() {}
}
