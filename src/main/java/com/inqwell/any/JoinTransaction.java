/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/JoinTransaction.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-07 16:53:31 $
 */
package com.inqwell.any;

/**
 * If its container is transactional join the container and field into
 * the current transaction.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class JoinTransaction extends    AbstractFunc
                             implements Cloneable
{
	
  private Locate any_;
  private Any    source_;
	
	public Any exec(Any a) throws AnyException
	{
		Value v = (Value)EvalExpr.evalFunc(getTransaction(),
																			 a,
                                       any_,
                                       Value.class);
                                       
    // What we do has to be done under transaction control so leave
    // it till then if appropriate
    Map m = any_.getMapParent();
    if (m.isTransactional())
      return m;

    v = (Value)m.get(any_.getPath());

    return v;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		JoinTransaction j = (JoinTransaction)super.clone();
		j.any_ = (Locate)any_.cloneAny();
		return j;
  }

  /**
   * If the target value is held
   * within an Inq transactional container then that container
   * will be entered into the transaction.
   */
  public Any doTransactionHandling (Any root, Any a) throws AnyException
  {
    Map m = (Map)a;
    
    Transaction t = getTransaction();
    t.copyOnWrite(m);
    a = any_.doTransactionHandling(root, m);
    if (a != null)
    {
      t.fieldChanging(m, any_, source_);
    }
	  return a;
  }

  public void setAny(Locate a, Any source)
  {
    any_    = a;
    source_ = source;
  }
  
  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(any_);
  	return a.createIterator();
  }
}
