/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SetNull.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-07 16:53:31 $
 */
package com.inqwell.any;

/**
 * SetNull the specified node of all its child nodes.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class SetNull extends    AbstractFunc
                    implements Cloneable
{
	
	private Locate any_;
	
	public SetNull(Locate any)
	{
		any_ = any;
	}
	
	public Any exec(Any a) throws AnyException
	{
		Value v = (Value)EvalExpr.evalFunc(getTransaction(),
																			 a,
                                       any_,
                                       Value.class);
    
    if (v == null)
      nullOperand(any_);
                                       
    // What we do has to be done under transaction control so leave
    // it till then if appropriate
    Map m = any_.getMapParent();
    if (m == null)
      nullOperand(any_);
    
    if (m.isTransactional())
      return m;

    getTransaction().checkPrivilege(AbstractMap.P_WRITE, m, any_.getPath());
    v = (Value)m.get(any_.getPath());
    v.setNull();
    return v;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		SetNull s = (SetNull)super.clone();
		s.any_ = (Locate)any_.cloneAny();
		return s;
  }

  /**
   * The <code>SetNull</code> function is a mutating operation
   * on the target instance.  If the
   * <code>SetNull</code> is used and the target value is held
   * within an Inq transactional container then that container
   * will be entered into the transaction.
   */
  public Any doTransactionHandling (Any root, Any a) throws AnyException
  {
    Map m = (Map)a;
    
    Value v; 
    
    // Check write PRIVILEGE
    Transaction t = getTransaction();
    t.copyOnWrite(m);
    v = (Value)any_.doTransactionHandling(root, m);
    //if (!v.isNull())  Otherwise txn maps are inconsistent. TODO may be
    //{
      v.setNull();
      t.fieldChanging(m, any_, null);
    //}
	  return v;
  }

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(any_);
  	return a.createIterator();
  }
}
