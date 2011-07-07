/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Func.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;


/**
 * The function call interface.  It is placed in the Any
 * hierarchy to allow it to be collected and visited like other Any
 * deriveds.
 */
public interface Func extends Any
{
	/**
	 * Execute the function with the specified argument.  In many cases
	 * this argument will be a node in an <code>Any</code> structure
	 * which contains the necessary elements required by the function.
	 */
  public Any exec (Any a) throws AnyException;
  
  public Any execFunc (Any a) throws AnyException;
  
  public void setTransaction(Transaction t);
  public Transaction getTransaction();

  public Any doTransactionHandling (Any root, Any a) throws AnyException;
  
  public Map buildArgs();
  public Map buildArgs(Map m);
  
  public Any getParam();
  
  public void setParam(Any a);
  
  public void setLineNumber(int line);
  public void setColumn(int col);
  public int  getLineNumber();
  public int  getColumn();

  /**
   * Return the name of this function.
   */
  public Any  getFQName();
  
  /**
   * Get the URL this function was parsed from. Optional operation.
   * @return null if not supported, the URL otherwise.
   */
  public Any getBaseURL();
}
