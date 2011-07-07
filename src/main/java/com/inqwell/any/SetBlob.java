/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SetBlob.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-07 16:53:31 $
 */
package com.inqwell.any;

/**
 * Sets the blob value to the specified Any.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class SetBlob extends    AbstractFunc
                     implements Cloneable
{
	
	private Locate blob_;
	private Any    val_;
	
	public SetBlob(Locate blob, Any val)
	{
		blob_ = blob;
    val_  = val;
	}
	
	public Any exec(Any a) throws AnyException
	{
		AnyBlob blob = (AnyBlob)EvalExpr.evalFunc(getTransaction(),
                                              a,
                                              blob_,
                                              AnyBlob.class);
                                       
		Any val = EvalExpr.evalFunc(getTransaction(),
                                a,
                                val_);

    if (blob == null)
      throw new AnyException("Did not resolve blob operand " + blob_);
    
    if (val == null)
      throw new AnyException("Did not resolve value operand " + val_ + " Use setnull instead");

    // What we do has to be done under transaction control so leave
    // it till then if appropriate
    Map m = blob_.getMapParent();
    if (m.isTransactional())
    {
      val_ = val;
      return m;
    }

    getTransaction().checkPrivilege(AbstractMap.P_WRITE, m, blob_.getPath());
    blob = (AnyBlob)m.get(blob_.getPath());
    blob.setValue(val);
    return blob;
	}
	
  public Object clone () throws CloneNotSupportedException
  {
		SetBlob s = (SetBlob)super.clone();
		s.blob_ = (Locate)blob_.cloneAny();
		s.val_  =  val_.cloneAny();
		return s;
  }

  /**
   * The <code>SetBlob</code> function is a mutating operation
   * on the target instance.  If the
   * <code>SetBlob</code> is used and the target value is held
   * within an Inq transactional container then that container
   * will be entered into the transaction.
   */
  public Any doTransactionHandling (Any root, Any a) throws AnyException
  {
    Map m = (Map)a;
    
    AnyBlob b; 
    
    // Check write PRIVILEGE
    Transaction t = getTransaction();
    t.copyOnWrite(m);
    b = (AnyBlob)blob_.doTransactionHandling(root, m);
    if (b.getValue() != val_)
    {
      b.setValue(val_);
      t.fieldChanging(m, blob_, null);
    }
	  return b;
  }

}
