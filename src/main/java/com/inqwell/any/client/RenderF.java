/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/RenderF.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:21 $
 */
package com.inqwell.any.client;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFormat;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.ConstString;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Iter;
import com.inqwell.any.Locate;
import com.inqwell.any.Map;


/**
 * A <i>printf</i> style operation using format strings as
 * specified by {@link java.text.MessageFormat MessageFormat}. If the
 * typedef/field can be determined then any formatting available
 * will be used, otherwise the place-holder in the string to be
 * formatted may specify the formatting.
 * <p>
 * Returns a <code>StringI</code> result.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.4 $
 */
public class RenderF extends    AbstractFunc
                    implements Cloneable
{
  private Array      values_;
  private Any        format_;

  public RenderF(Any format, Array values)
  {
    values_ = values;
    format_ = format;
  }
  
  public Any exec(Any a) throws AnyException
  {
    Any format  = EvalExpr.evalFunc(getTransaction(),
                                      a,
                                      format_);
    
    if (format == null || format.toString().length() == 0)
      throw new AnyException("Format string cannot be null or zero length");
    
    // Parser always constructs an array. We have to resolve
    // any expressions in its content
    int j = values_.entries();
    Array ar = AbstractComposite.array(j);
    for (int i = 0; i < j; i++)
    {
      Any expr = values_.get(i);
      
      Any aa = EvalExpr.evalFunc(getTransaction(),
                                 a,
                                 expr);
      
      // Silently ignore anything that doesn't resolve
      if (aa == null)
        ar.add(AnyString.EMPTY);
      else
        ar.add(formatMeta(aa, expr));
    }
    
    AnyFormat af = AnyFormat.makeFormat(ar, format.toString());
    
    return new AnyString(af.format(ar));
  }

  public Iter createIterator ()
  {
    // Just the expression
    Array a = AbstractComposite.array();
    a.add(values_);
    a.add(format_);
    return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    RenderF r = (RenderF)super.clone();
    r.values_ = (Array)values_.cloneAny();
    r.format_ = format_.cloneAny();
    return r;
  }

  private Any formatMeta(Any value, Any expr)
  {
    if (expr instanceof Locate)
    {
      Locate l = (Locate)expr;
      Map m = l.getMapParent();
      if (m != null)
      {
        Descriptor d = m.getDescriptor();
        if (d != Descriptor.degenerateDescriptor__)
        {
          AnyFormat formatter = AnyFormat.makeFormat(value, d.getFormat(l.getPath()));
          if (formatter != null)
            value = new ConstString(formatter.format(value));
        }
      }
    }
    
    return value;
  }
}
