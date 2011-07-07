/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/OperatorVisitor.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Base class for operations on values.  Derived classes must implement the
 * appropriate visitAny... methods.
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public abstract class OperatorVisitor extends    AbstractVisitor
                                      implements Cloneable
{
  protected Any         op1_;
  protected Any         op2_;
  protected Any         result_;
  protected Any         param_;
  protected RankVisitor rankV_;

  public OperatorVisitor()
  {
    init();
  }
  
  /**
   * Perform the operation defined by the specific derived class instance on
   * the given Anys and return the result.  The result will be the same or
   * greater precision than the highest precision of the two operands.  Argument
   * op2 may be null in the case of unary operators, but this is more relevant
   * to the derived classes which implement those operators.
   */
  public Any doOperation (Any op1, Any op2) throws AnyException
  {
    samePrecision (op1, op2);

    result_ = handleNullOperands(op1_, op2_, op1, op2);
    
    // If we get here then the operands resolved to the satisfaction
    // of the specific operation we were actually performing, but
    // one or both could still be AnyNull.  Handle this separately.
    result_ = handleAnyNull(op1_, op2_);

    if (result_ == null)
      op1_.accept(this);

    return result_;
  }
 
  /**
   * Unary version of doOperation(op1, op2)
   */
  public Any doOperation (Any op1) throws AnyException
  {
    return doOperation(op1, null);
  }
 
  /**
   * Establishes the Any which will be passed to exec() in the case where
   * an operand is an implementation of com.inqwell.any.Func.  In this case, the
   * default action provided by this class is to exec the function passing
   * the supplied Any.
   */
  public void setParam(Any a)
  {
    param_ = a;
  }
  
  public Any getOp1()
  {
    return op1_;
  }
  
  public Any getOp2()
  {
    return op2_;
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    OperatorVisitor v = (OperatorVisitor)super.clone();
    v.init();
    return v;
  }
  
  /**
   * Determine the rank of the precision of the supplied Any.  If the Any
   * is a function then it is evaluated and the ranking operation is applied
   * to the function's return value.  This process is recursive.
   * @param a the Any to rank
   * @return rank of supplied any or function result.
   * @throws IllegalArgumentException if a or exec(a) is not a rankable scalar
   * type.
   */
  protected int rank(Any a)
  {
    // determine and return the rank of the argument
    rankV_.setAny (a);
    rankV_.setTransaction(getTransaction());
    a.accept (rankV_);
    return rankV_.rank();
  }
  
  protected abstract Any handleNullOperands(Any res1,
                                            Any res2,
                                            Any op1,
                                            Any op2) throws AnyException;
  
  protected Any handleAnyNull(Any op1, Any op2)
  {
    Any iNull = AnyNull.instance();
    // Handle unary and binary cases
    if ((op1 != null && op1 == iNull) ||
        (op2 != null && op2 == iNull))
    {
      return AnyNull.instance();
    }
    
    return null;
  }
  
  /**
   * Handle field ripping by operators. This method is passed a
   * resolved operand and the default implementation simply
   * returns that operand. Derived classes can override if
   * they have field ripping issues, such as max(x,y)
   * and min(x,y).
   */
  protected Any rippedField(Any a)
  {
    return a;
  }
  
  /**
   * Ensures that the operands are of the same scalar type and chooses the
   * type of the highest precison of the operands to represent them.  This will
   * also be the type of the result in most cases, but the result could
   * be a type of a greater precision.
   * <p>
   * Note that this method implicitly assumes that the result of the operation
   * implemented in derived classes will not be stored in one of the operands.
   * This is because the interface with derived classes is via the protected
   * members op1_ and op2_.  This method initialises these members, either with
   * the original operands, or with a conversion to a higher precision.  It
   * should not be assumed that either has anything more than temporary status.
   */
  protected void samePrecision (Any op1, Any op2)
  {
    op1_ = op1;
    op2_ = op2;
    
    // Evaluate the first operand
    int rank1 = rank (op1_);
    op1_ = rankV_.getAny();
    op1_ = rippedField(op1_);
    getTransaction().resetResolving();
    
    int rank2 = -1;
    
    if (op2 != null)
    {
      rank2 = rank(op2_);
      op2_   = rankV_.getAny();
      op2_ = rippedField(op2_);
      getTransaction().resetResolving();
    }
    
    Any iNull = AnyNull.instance();
    
    if ((op1 == op2)  ||                        // objects are the same
        (op2 == null) ||                        // operator is unary
        (op1_ == null) ||                       // one or other of the...
        (op2_ == null) ||                       // ...operands was not resolved
        (op1_ == iNull) ||                       // one or other of the...
        (op2_ == iNull) ||                       // ...operands was null
        (op1_.getClass() == op2_.getClass()) || // operands are already the same
        (rank1 == rank2) ||                     // same rank
        //(rank1 == Value.RANK_UNKNOWN) ||        // Let unknowns be handled...
        //(rank2 == Value.RANK_UNKNOWN) ||        // ... by the other operand
        (rank1 == Value.RANK_ASIS) ||           // op1 always acceptable for op2
        (rank2 == Value.RANK_ASIS))             // op2 always acceptable for op1
      return;

    if (rank1 == Value.RANK_CONVERT)
      rank1 = rank2 + 1;   // Yucky and causes problems when concatenating strings with doubles - TBF

    // If op1 is a decimal then we only permit conversion from
    // the various integral types.
    if (rank1 == Value.RANK_DECIMAL && !(rank2 == Value.RANK_BYTE ||
                                         rank2 == Value.RANK_CHAR ||
                                         rank2 == Value.RANK_SHORT ||
                                         rank2 == Value.RANK_INTEGER ||
                                         rank2 == Value.RANK_LONG))
      throw new AnyRuntimeException("Cannot combine decimal with non-integral types");

    if (rank1 == Value.RANK_DATE &&
    		rank2 == Value.RANK_LONG)
    {
    	op2_ = new AnyDate(((LongI)op2_).getValue());
    }
    		
    if (rank1 < rank2)
      op1_ = op2_.buildNew(op1_);
    else
      op2_ = op1_.buildNew(op2_);
  }

  protected void notResolved(Any op1)
  {
    throw new AnyRuntimeException("Operand " + op1 + " could not be resolved");
  }

  protected void init()
  {
    rankV_ = new RankVisitor();
  }

  public void visitUnknown(Any o)
  {
    // Just for Sniff to check definers in (limited) set of subclasses!
    super.visitUnknown(o);
  }
  

  /**
   * Visitor class to implement ranking is coded as an inner class.  This
   * allows us to access the param_ member in case of evaluating funcs.
   */
  protected class RankVisitor extends AbstractVisitor
  {
    private static final long serialVersionUID = 1L;

    private int rank_ = -1;
    
    protected Any any_  = null;

    private Func lastFunc_ = null;

    public void setAny (Any a) { any_ = a; }
    
    public Any getAny () { return any_; }

    public int rank() { return rank_; }

    public void visitAnyByte (ByteI b)
    {
      if (b.isNull())
        any_ = AnyNull.instance();
      else
        any_  = b;
      rank_ = Value.RANK_BYTE;
    }

    public void visitAnyBoolean (BooleanI b)
    {
      any_  = b;
      rank_ = Value.RANK_BOOLEAN;
    }

    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        any_ = AnyNull.instance();
      else
        any_  = c;
      rank_ = Value.RANK_CHAR;
    }

    public void visitAnyInt (IntI i)
    {
      if (i.isNull())
        any_ = AnyNull.instance();
      else
        any_  = i;
      rank_ = Value.RANK_INTEGER;
    }

    public void visitAnyShort (ShortI s)
    {
      if (s.isNull())
        any_ = AnyNull.instance();
      else
        any_  = s;
      rank_ = Value.RANK_SHORT;
    }

    public void visitAnyLong (LongI l)
    {
      if (l.isNull())
        any_ = AnyNull.instance();
      else
        any_  = l;
      rank_ = Value.RANK_LONG;
    }

    public void visitAnyFloat (FloatI f)
    {
      if (f.isNull())
        any_ = AnyNull.instance();
      else
        any_  = f;
      rank_ = Value.RANK_FLOAT;
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (d.isNull())
        any_ = AnyNull.instance();
      else
        any_  = d;
      rank_ = Value.RANK_DOUBLE;
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        any_ = AnyNull.instance();
      else
        any_  = d;
      // Just defer to AnyBigDecimal
      rank_ = Value.RANK_DECIMAL;
    }

    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        any_ = AnyNull.instance();
      else
        any_  = s;
      rank_ = Value.RANK_CONVERT;
    }

		public void visitArray(Array a)
		{
      any_  = a;
      rank_ = Value.RANK_ASIS;
		}
  
    public void visitSet (Set s)
		{
      any_  = s;
      rank_ = Value.RANK_ASIS;
		}
  
    public void visitMap (Map m)
    {
			if (m.isTransactional())
			{
        Any any = null;
        
				// The protocol with the common Func implementation LocateNode is
				// to return the t-Map parent of the target, if it has one.  We
				// can then exec the func again to do the last part of the location
				// of the transaction's private instance.  The reason why we don't
				// just do all that in LocateNode and have done with it is that
				// we then have the opportunity to do anything else we want, such
				// as join in the transaction context. [QV Assign]
				try
				{
	        any = lastFunc_.doTransactionHandling(param_, m);
				}
				catch (AnyException e)
				{
					throw new RuntimeContainedException(e);
				}
				
				// Make sure if we see the same any twice we don't recurse
				// ad-infinitum.  This would otherwise happen if a
				// transactional map is the target node in the rank operation
				if (any_ == any)
				{
          rank_ = Value.RANK_UNKNOWN;
          return;
        }
        
        any_ = any;
        
				if (any != null)
				  any_.accept(this);
			}
			else
			{
        any_  = m;
				rank_ = Value.RANK_UNKNOWN;
			}
    }

    /**
     * If dealing with a function then we exec it and re-rank the result.
     * This could be deferred until a derived class of OperatorVisitor but
     * we really want both operands to be scalars by the time we get there.
     */
    public void visitFunc (Func f)
    {
      try
      {
        f.setTransaction(getTransaction());
        any_ = f.execFunc (param_);
        lastFunc_ = f;
  			if (any_ != null)
				  any_.accept(this);
      }
      catch (AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
  
		public void visitAnyDate(DateI d)
		{
      if (d.isNull())
      {
        any_ = AnyNull.instance();
        rank_ = Value.RANK_UNKNOWN;
      }
      else
      {
        any_  = d;
        rank_ = Value.RANK_DATE;
      }
		}
  
		public void visitAnyObject(ObjectI o)
		{
      Object oo = o.getValue();
      if (oo == null || AnyNull.isNull(oo))
        any_ = AnyNull.instance();
      else
        any_  = o;
      
      rank_ = Value.RANK_UNKNOWN;
		}
  
		public void visitUnknown(Any o)
		{
      any_  = o;
      if (AnyAlwaysEquals.isAlwaysEquals(o))
        rank_ = Value.RANK_ASIS;
      else
        rank_ = Value.RANK_UNKNOWN;
		}
  }
}
