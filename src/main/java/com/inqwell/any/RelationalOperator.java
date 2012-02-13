/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Equals.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Provides a default implementation for 
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */

public abstract class RelationalOperator extends OperatorVisitor
{
  protected BooleanI getBooleanResult(boolean b)
  {
    return b ? AnyBoolean.TRUE : AnyBoolean.FALSE;
  }
}
