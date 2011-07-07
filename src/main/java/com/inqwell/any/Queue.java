/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * Interface for Queue composites.  Such composites can operate as LIFOs
 * or FIFOs.
 */

public interface Queue extends Array
{
  public void addFirst (Any a);
  public void addLast  (Any a);
  
  public Any getFirst ();
  public Any getLast  ();

  public Any  removeFirst();
  public Any  removeLast();

}
