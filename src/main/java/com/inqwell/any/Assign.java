/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Assign.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-05-07 16:53:31 $
 */
package com.inqwell.any;

/**
 * Assign one Any from another and return operand 1 as the result.
 * @author $Author: sanderst $
 * @version $Revision: 1.6 $
 * @see com.inqwell.any.Any
 */
public class Assign extends MutatingOperator
{
  public Any doOperation (Any op1, Any op2) throws AnyException
  {
		if (op1 == op2)
			return op1;

    // If src operand is null before we evaluate it then
    // croak now
		if (op2 == null)
      notResolved(op2);

    op1_ = op1;
    op2_ = op2;

		// It doesn't make sense to rank op1 - we must assign to the original
		// operand irrespective of the type of the source operand.  Also, the
		// ranking process is a read-only operation suitable for source operands.
		// Here we are writing, which requires special consideration if
		// transaction semantics [with all that implies, like thread safety
		// and so forth] are in effect.
		// By resolving op1 before op2 (which might also make a reference
		// to the same node as op1 does, for example a.b = a.b + 1) we
		// ensure that op1 is locked before we go on to evaluate op2.
		// In this way, expressions like those above are guaranteed
		// without additional explicit locking.
    op1_.accept(this);

    return result_;
    //return op1;
  }

  public void visitAnyBoolean (BooleanI b)
  {
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
    b.copyFrom(op2_);
    result_ = b;
  }

  public void visitAnyString (StringI s)
  {
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
    s.copyFrom (op2_);
    result_ = s;
  }

  public void visitAnyByte (ByteI b)
  {
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
    b.copyFrom (op2_);
    result_ = b;
  }

  public void visitAnyChar (CharI c)
  {
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
    c.copyFrom (op2_);
    result_ = c;
  }

  public void visitAnyInt (IntI i)
  {
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
    i.copyFrom (op2_);
    result_ = i;
  }

  public void visitAnyShort (ShortI s)
  {
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
    s.copyFrom (op2_);
    result_ = s;
  }

  public void visitAnyLong (LongI l)
  {
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
    l.copyFrom (op2_);
    result_ = l;
  }

  public void visitAnyFloat (FloatI f)
  {
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
    f.copyFrom (op2_);
    result_ = f;
  }

  public void visitAnyDouble (DoubleI d)
  {
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
    d.copyFrom (op2_);
    result_ = d;
  }

  public void visitDecimal (Decimal d)
  {
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
    d.copyFrom (op2_);
    result_ = d;
  }

  public void visitAnyDate (DateI d)
  {
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
    d.copyFrom (op2_);
    result_ = d;
  }

  public void visitMap (Map m)
  {
		if (lastMap_ != m && m.isTransactional())
    {
      transactionalMap(m);
	  }
	  else
	  {
			rank(op2_);
      Any a = rankV_.getAny();
      if (a == null)
      {
        lastMap_ = null;   // reset before we croak
        notResolved(op2_);
      }
      op2_ = a;
			
      if (lastMap_ != null)
      {
        // Original map was transactional
        // Need to check its a real map and not just one for the purposes of
        // property access. Such things can be values as well. Obviously, a
        // little messy and may be a method would be better
        if (!(op2_ instanceof Value) && (op2_ instanceof Map))
        {
          // Check write PRIVILEGE on lastMap_ for all fields in op2_
          Process p = getTransaction().getProcess();
          if (p == null)
          {
            lastMap_ = null;
            throw new AnyRuntimeException("No Process");
          }
          
          // Bit expensive?
          Map op2 = (Map)op2_;
          Transaction t = getTransaction();
          Iter i = op2.createKeysIterator();
          while (i.hasNext())
          {
            Any k = i.next();
            t.checkPrivilege(AbstractMap.P_WRITE, lastMap_, k);
          }
        }
        else
        {
          // Check write PRIVILEGE on locate map parent, key pathItem_
          getTransaction().checkPrivilege(AbstractMap.P_WRITE, lastMap_, lastFunc_.getPath());
        }
      }
      else
      {
        // Original map was not transactional. We are just copying a (hopefully)
        // map to it
        if (op2_ != null && lastFunc_ != null) // in case a declaration
        {
          // Check write PRIVILEGE on locate map parent, key pathItem_
          getTransaction().checkPrivilege(AbstractMap.P_WRITE, lastFunc_.getMapParent(), lastFunc_.getPath());
        }
      }
      
      if (op2_ != null)
        copyToMap(m, op2_);
      result_ = m;
        
      if (lastMap_ != null && (!(op2_ instanceof Value)) && (op2_ instanceof Map))
      {
        // we re-entered visitMap from an original transactional map.
        // Means we are doing a map assignment - assume all fields defined
        // in the source map are changing
        Transaction t = getTransaction();
        t.fieldChanging(lastMap_, op2_, null);
      }
		}
    lastMap_ = null;
    //result_ = m;
  }

  public void visitArray (Array a)
  {
		rank(op2_);
    Any aa = rankV_.getAny();
    if (aa == null)
      notResolved(op2_);
		op2_ = aa;
    a.copyFrom(op2_);
    result_ = a;
  }

  public void visitSet (Set s)
  {
		rank(op2_);
    Any aa = rankV_.getAny();
    if (aa == null)
      notResolved(op2_);
		op2_ = aa;
    s.copyFrom(op2_);
    result_ = s;
  }

	public void visitAnyObject(ObjectI o)
	{
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
		// just hope copyFrom will be OK for whatever op2_ is
    o.copyFrom(op2_);
    result_ = o;
	}

	public void visitUnknown(Any o)
	{
		rank(op2_);
    Any a = rankV_.getAny();
    if (a == null)
      notResolved(op2_);
		op2_ = a;
		// just hope copyFrom will be OK for whatever op2_ is
    o.copyFrom(op2_);
    result_ = o;
	}
	
	protected void copyToMap(Map m, Any a)
	{
	  m.copyFrom(a);
	}

  protected Any handleAnyNull(Any op1, Any op2)
  {
    return null;
  }
}
