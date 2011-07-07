/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/StringIndex.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

/**
 * Return index in first operand of the second as an AnyInt. Returns -1
 * if the second operand is not contained within the first.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class StringIndex extends    AbstractFunc
                         implements Cloneable
{

	private Any container_;
	private Any contained_;
	private Any fromIndex_;

	public StringIndex(Any container, Any contained, Any fromIndex)
	{
		container_ = container;
		contained_ = contained;
		fromIndex_ = fromIndex;
	}

	public Any exec(Any a) throws AnyException
	{
		StringI container = (StringI)EvalExpr.evalFunc(getTransaction(),
                                                   a,
                                                   container_,
                                                   StringI.class);

		Any     contained = EvalExpr.evalFunc(getTransaction(),
                                          a,
                                          contained_);

		Any     fromIndex = EvalExpr.evalFunc(getTransaction(),
                                          a,
                                          fromIndex_);

    AnyInt ret = new AnyInt(-1);
    
    if (container == null)
      nullOperand(container_);
      
    if (contained == null)
      nullOperand(contained_);
    
    if (fromIndex == null && fromIndex_ != null)
      nullOperand(fromIndex_);
    
    if (container.isNull())
      return ret;
    
    ResolveSub rs = null;
    
    if (fromIndex != null)
    {
      AnyInt idx = new AnyInt();
      idx.copyFrom(fromIndex);
      if (!idx.isNull())
      {
        rs  = new ResolveSub(container, contained);
        ret.setValue(rs.doIndexOf(idx.getValue()));
      }
    }
    else
    {
      rs  = new ResolveSub(container, contained);
      ret.setValue(rs.doIndexOf(0));
    }

		return ret;
	}

  public Iter createIterator ()
  {
  	Array a = AbstractComposite.array();
  	a.add(container_);
		a.add(contained_);
    if (fromIndex_ != null)
      a.add(fromIndex_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
		StringIndex c = (StringIndex)super.clone();
		c.container_ = container_.cloneAny();
		c.contained_ = contained_.cloneAny();
		c.fromIndex_ = AbstractAny.cloneOrNull(fromIndex_);
		return c;
  }
  
  private class ResolveSub extends AbstractVisitor
  {
    private StringI container_;
    private Any     contained_;
    private int     fromIndex_;

    private ResolveSub(StringI container, Any contained)
    {
      container_ = container;
      contained_ = contained;
    }
    
    private int doIndexOf(int fromIndex)
    {
      fromIndex_ = fromIndex;
      contained_.accept(this);
      return fromIndex_;
    }
    
    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        fromIndex_ = -1;
      else
        fromIndex_ = container_.getValue().indexOf(c.getValue(), fromIndex_);
    }

    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        fromIndex_ = -1;
      else
        fromIndex_ = container_.getValue().indexOf(s.getValue(), fromIndex_);
    }
  }
}
