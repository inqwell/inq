/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyPrintable.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.awt.print.Printable;
import java.awt.print.Paper;

/**
 * Allow an Any to provide a java.awt.print.Printable object.
 */
public interface AnyPrintable extends Any
{
  public Any getNumberOfPages();

  /**
   * Return the java.awt.print.Printable capable of rendering this
   * object.  Unfortunately...
   */  
  public Printable getPrintable();
}
