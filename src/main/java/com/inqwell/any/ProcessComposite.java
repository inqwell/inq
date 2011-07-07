/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * Implements the exec method on the assumption that the Map _process contains
 * values which are function objects against keys which are also present in
 * the argument, which is thus assumed to be a map also.
 *
 * Derived classes will typically add their function objects to _process with
 * the appropriate keys.  A neat way to implement the function objects is to
 * create anonymous inner classes which call member functions.
 */
public class ProcessComposite extends AbstractFunc
{
  protected Map _process = AbstractComposite.map();  

  public ProcessComposite ()
  {
  }

  /**
   * The argument must be a Map with keys the same as those in _process.  The
   * functions contained in _process are executed passing the corresponding
   * value.
   * @return the original argument.  If this is not required implement exec in
   * derived class as super.exec(a); return something;
   */
  public Any exec (Any a) throws AnyException
  {
    Iter i = _process.createKeysIterator();
    while (i.hasNext())
    {
      Any any = i.next();
      ((Func)_process.get(any)).exec (((Map)a).get(any));
    }
    return a;
  }
}

