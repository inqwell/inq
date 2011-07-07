/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyComparator.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Compare <code>Composite</code> children so that they can be ordered
 * according to arbitrary elements under each child node.
 * <p>
 * In the Any framework there is a need for only one comparator implementation.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.5 $
 */
public class AnyComparator extends    AbstractAny
													 implements OrderComparator,
																			Cloneable
{
  private static final long serialVersionUID = 1L;

	// An array of expressions which are applied to the items beneath
	// each Any passed to compare() being compared.
	private Array       orderingItems_;

	// If non-null then nodes compared will be the Map's values
	// assuming the given comparison Anys to be its keys.
	private Map         toOrder_;

	private Transaction t_ = Transaction.NULL_TRANSACTION;

	private EvalExpr    lessThan_;
	private EvalExpr    greaterThan_;
	
	private int     orderMode_  = Map.I_KEYS;
	private boolean descending_ = false;
  
  private int     nullMode_   = OrderComparator.NULL_HIGH;

	public AnyComparator(Array orderingItems, Map toOrder)
	{
		orderingItems_ = orderingItems;
		toOrder_       = toOrder;
		init();
	}

	public AnyComparator(Array orderingItems)
	{
		this(orderingItems, null);
	}

	public AnyComparator()
	{
		this(null, null);
	}

	public int compare (Object o1, Object o2)
	{
		Any a1 = (Any)o1;
		Any a2 = (Any)o2;
		return compare (a1, a2, orderingItems_);
	}

	public int compare (Any a1, Any a2)
	{
		return compare (a1, a2, orderingItems_);
	}

	public int compare (Any a1, Any a2, Array orderingItems)
	{
	  if (orderingItems == null)
	    return 0;

		if (toOrder_ != null && orderMode_ == Map.I_KEYS)
		{
			a1 = toOrder_.get(a1);
			a2 = toOrder_.get(a2);
		}
		
		Transaction t = t_;
		if (t_ == Transaction.NULL_TRANSACTION)
		  t = Globals.getProcessForCurrentThread().getTransaction();

		int num = orderingItems.entries();
		
		Any loop    = t.getLoop();
		Any context = t.getContext();
		// Note - LocateNode cannot accept a context of null, which
		// may be the case when processing received node events
		// from the server. Really, this should not be a problem
		// because we could (as here) be referencing $loop (or
		// some other prefix) but LocateNode starts off assuming
		// $this. TODO: LocateNode will be rewritten (as part of
		// the reentrancy initiative).
		if (context == null)
		  context = t.getProcess().getRoot();

		try
		{
			for (int i = 0; i < num; i++)
			{
				Any expr = orderingItems.get(i);
				
				t.setLoop(a1);

        // Apply the current ordering expression to each node...
    		Any l1 = EvalExpr.evalFunc(t,
                                   context,
                                   expr);
        
        if (l1 == null)
          throw new AnyException("sort did not resolve " + expr + " at node " + a1);

        t.setLoop(a2);

        // Apply the current ordering expression to each node...
    		Any l2 = EvalExpr.evalFunc(t,
    		                           context,
                                   expr);

        if (l2 == null)
          throw new AnyException("sort did not resolve " + expr + " at node " + a2);

        BooleanI b;

        lessThan_.setTransaction(t);
        lessThan_.setOp1(l1);
        lessThan_.setOp2(l2);
        b = (BooleanI)lessThan_.exec(null);  // already resolved, no root reqd.
        if (b.getValue())
          return -1 * (descending_ ? -1 : 1);

        greaterThan_.setTransaction(t);
        greaterThan_.setOp1(l1);
        greaterThan_.setOp2(l2);
        b = (BooleanI)greaterThan_.exec(null);  // already resolved, no root reqd.
        if (b.getValue())
          return 1 * (descending_ ? -1 :1);
			}

			return 0;
		}

		// We have to do this as the contract with java.util.Compare has no
		// explicit throws clause
		catch (AnyException e)
		{
			throw new RuntimeContainedException(e);
		}
		finally
		{
		  t.setLoop(loop);
		}
	}

	public void setOrderBy(Array orderBy)
	{
		orderingItems_ = orderBy;
	}

	public void setOrderMode(int orderMode)
	{
		orderMode_ = orderMode;
	}

	public Array getOrderBy()
	{
	  return orderingItems_;
	}
	
	public void setToOrder(Map toOrder)
	{
		toOrder_ = toOrder;
	}

	public void setDescending(boolean isDescending)
	{
		descending_ = isDescending;
	}
	
  public void setIgnoreCase(boolean ignore)
  {
    if (ignore)
    {
      // Special operators
      lessThan_    = new EvalExpr(null, null, new IgnoreCaseLessThan());
      greaterThan_ = new EvalExpr(null, null, new IgnoreCaseGreaterThan());
    }
    else
    {
      init();
    }
  }
  
  public void setNullMode(int nullMode)
  {
    nullMode_ = nullMode;
  }
  
	public boolean isDescending()
	{
		return descending_;
	}
	
	public void setTransaction(Transaction t)
	{
	  t_ = t;
	}

  public Object clone () throws CloneNotSupportedException
  {
    AnyComparator c = (AnyComparator)super.clone();

		c.init();

		c.toOrder_ = null;
		c.t_       = Transaction.NULL_TRANSACTION;
		c.orderingItems_ = (Array)AbstractAny.cloneOrNull(orderingItems_);

    return c;
  }

  private void init()
  {
    lessThan_    = new EvalExpr(null, null, new StringLessThan());
    greaterThan_ = new EvalExpr(null, null, new StringGreaterThan());
  }
  
  private class StringLessThan extends LessThan
  {
    private static final long serialVersionUID = 1L;

    public void visitAnyObject (ObjectI o)
    {
      AnyCollationKey target = (AnyCollationKey)op2_;
    	AnyCollationKey k      = (AnyCollationKey)o;
      result_ = new AnyBoolean (k.compare(target) < 0);
    }

    // Modified handling of the null constant.
    protected Any handleAnyNull(Any op1, Any op2)
    {
      Any iNull = AnyNull.instance();

      if ((op1 != null && op1 == iNull) &&
          (op2 != null && op2 != iNull))
      {
        // Left operand is null, right operand isn't
        if (nullMode_ == OrderComparator.NULL_HIGH)
          return AnyBoolean.FALSE;
        else
          return AnyBoolean.TRUE;
      }
      else if ((op1 != null && op1 == iNull) &&
               (op2 != null && op2 != iNull))
      {
        // Right operand is null, left operand isn't
        if (nullMode_ == OrderComparator.NULL_HIGH)
          return AnyBoolean.TRUE;
        else
          return AnyBoolean.FALSE;
      }
      else if ((op1 != null && op1 == iNull) ||
               (op2 != null && op2 == iNull))
      {
        // They are both null
        return AnyBoolean.FALSE;
      }

      // Normal behaviour
      return null;
    }
  }
  
  private class StringGreaterThan extends GreaterThan
  {
    private static final long serialVersionUID = 1L;

    public void visitAnyObject (ObjectI o)
    {
      AnyCollationKey target = (AnyCollationKey)op2_;
    	AnyCollationKey k      = (AnyCollationKey)o;
      result_ = new AnyBoolean (k.compare(target) > 0);
    }

    // Modified handling of the null constant.
    protected Any handleAnyNull(Any op1, Any op2)
    {
      Any iNull = AnyNull.instance();

      if ((op1 != null && op1 == iNull) &&
          (op2 != null && op2 != iNull))
      {
        // Left operand is null, right operand isn't
        if (nullMode_ == OrderComparator.NULL_HIGH)
          return AnyBoolean.TRUE;
        else
          return AnyBoolean.FALSE;
      }
      else if ((op1 != null && op1 == iNull) &&
               (op2 != null && op2 != iNull))
      {
        // Right operand is null, left operand isn't
        if (nullMode_ == OrderComparator.NULL_HIGH)
          return AnyBoolean.FALSE;
        else
          return AnyBoolean.TRUE;
      }
      else if ((op1 != null && op1 == iNull) ||
               (op2 != null && op2 == iNull))
      {
        // They are both null
        return AnyBoolean.FALSE;
      }

      // Normal behaviour
      return null;
    }
  }
  
  private class IgnoreCaseLessThan extends StringLessThan
  {
    private static final long serialVersionUID = 1L;

    public void visitAnyString (StringI s)
    {
      StringI op2 = (StringI)op2_;
      result_ = new AnyBoolean (s.getValue().compareToIgnoreCase(op2.getValue()) < 0);
    }

    public void visitAnyObject (ObjectI o)
    {
      throw new IllegalArgumentException("Cannot use collators with blanket ignorecase");
    }
  }
  
  private class IgnoreCaseGreaterThan extends StringGreaterThan
  {
    private static final long serialVersionUID = 1L;

    public void visitAnyString (StringI s)
    {
      StringI op2 = (StringI)op2_;
      result_ = new AnyBoolean (s.getValue().compareToIgnoreCase(op2.getValue()) > 0);
    }

    public void visitAnyObject (ObjectI o)
    {
      throw new IllegalArgumentException("Cannot use collators with blanket ignorecase");
    }
  }
}
