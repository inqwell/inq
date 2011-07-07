/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * A function that has a name. Such functions can be referenced using their
 * name, rather than only being constructed in function networks.
 * 
 * @author Tom
 */
public interface NamedFunc extends Func
{
  /**
   *  Set the name of this function
   * @param fqName
   */
  public void setFQName(Any fqName);

}
