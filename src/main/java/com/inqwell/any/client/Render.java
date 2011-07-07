/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/Render.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.Any;
import com.inqwell.any.AbstractAny;
import com.inqwell.any.Array;
import com.inqwell.any.AnyString;
import com.inqwell.any.ConstString;
import com.inqwell.any.StringI;
import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AnyException;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.AnyFormat;
import com.inqwell.any.Descriptor;
import com.inqwell.any.Iter;
import java.text.Format;


/**
 * Perform the data rendering function supported by GUI nodes
 * as a func.  The enclosed <code>RenderInfo</code> is resolved
 * and any formatting it contains applied to return
 * an <code>StringI</code> result.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Render extends    AbstractFunc
									  implements Cloneable
{
  private Any        toRender_;
  private Any        format_;
  private Any        type_;
  private Any        field_;

	public Render(Any toRender, Any type, Any field, Any format)
  {
    toRender_ = toRender;
    type_     = type;
    field_    = field;
    format_   = format;
  }
  
	public Any exec(Any a) throws AnyException
	{
		Any toRender  = EvalExpr.evalFunc(getTransaction(),
                                      a,
                                      toRender_);
    if (toRender == null)
      return AnyString.EMPTY;

		Descriptor descriptor = (Descriptor)EvalExpr.evalFunc
																			(getTransaction(),
		                                   a,
		                                   type_,
		                                   Descriptor.class);

		Any field  = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   field_);

		Any format = EvalExpr.evalFunc(getTransaction(),
                                   a,
                                   format_);
    
    AnyFormat af;
    
    // If there is no explicit format then use the descriptor/field
    if (format == null)
    {
      if (descriptor == null || field == null)
        throw new AnyException("Must supply type/field or pattern");
      
      af = AnyFormat.makeFormat(toRender, descriptor.getFormat(field));
    }
    else
    {
      if ((format instanceof AnyFormat))
        af = (AnyFormat)format;
      else
      {
        af = AnyFormat.makeFormat(toRender, format.toString());
      }
    }
    
    af.resolveDirectives(a, getTransaction());
    
    return new ConstString(af.format(toRender));

	}

  public Iter createIterator ()
  {
    // Just the expression
  	Array a = AbstractComposite.array();
  	a.add(toRender_);
  	return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
		Render r = (Render)super.clone();
		r.toRender_ = toRender_.cloneAny();
		r.format_   = AbstractAny.cloneOrNull(format_);
		r.type_     = AbstractAny.cloneOrNull(type_);
		r.field_    = AbstractAny.cloneOrNull(field_);
		return r;
  }

}
