/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyMath.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Misc math related stuff.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see com.inqwell.any.Any
 */
public class AnyMath extends AbstractAny
{
  private static Any PI = null;
  private static Any E  = null;
  
  static public Any getPi()
  {
    if (PI == null)
      PI = new ConstDouble(Math.PI);
    
    return PI;
  }
  
  static public Any getE()
  {
    if (E == null)
      E = new ConstDouble(Math.E);
    
    return E;
  }
}

