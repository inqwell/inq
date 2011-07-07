/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any;

/*
 * $Archive: /src/com/inqwell/any/DeclareAnonymous.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
public class DeclareAnonymous extends Declare implements Cloneable
{
  private static final long serialVersionUID = 1L;

  public DeclareAnonymous(Locate at, Any var)
  {
    super(at, var);
  }

  public Any exec (Any a) throws AnyException
  {
    Transaction t = getTransaction();
    
    Any var = evaluateVar(a, t);
    var = t.readProperty(var);
    
    placeVar(var, a, t);
    
    return var;
  }

}
