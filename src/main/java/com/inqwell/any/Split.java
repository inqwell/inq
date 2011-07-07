/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Split.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.util.regex.Pattern;

/**
 * Split the first operand around occurrances of the second, regex,
 * operand.  Returns an array of strings in order of occurance of
 * the regex split delimiter.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Split extends    AbstractFunc
                   implements Cloneable
{
	private Any toSplit_;
  private Any regex_;
  private Any limit_;

	public Split(Any toSplit, Any regex)
	{
    this(toSplit, regex, null);
	}

	public Split(Any toSplit, Any regex, Any limit)
	{
		toSplit_ = toSplit;
		regex_   = regex;
		limit_   = limit;
	}

	public Any exec(Any a) throws AnyException
	{
		StringI toSplit    = (StringI)EvalExpr.evalFunc
                                           (getTransaction(),
                                            a,
                                            toSplit_,
                                            StringI.class);

		StringI regex    = (StringI)EvalExpr.evalFunc
                                           (getTransaction(),
                                            a,
                                            regex_,
                                            StringI.class);

		Any limit          =            EvalExpr.evalFunc
                                           (getTransaction(),
                                            a,
                                            limit_);

    if (toSplit == null)
      throw new AnyException("Could not resolve string " + toSplit_);

    if (toSplit == null)
      throw new AnyException("Could not resolve regex " + regex_);
    
    int pLimit = 0;
    if (limit != null)
    {
      AnyInt i = new AnyInt();
      i.copyFrom(limit);
      pLimit = i.getValue();
    }

    Pattern p = Pattern.compile(regex.getValue());
    
    String[] s = p.split(toSplit.getValue(), pLimit);
    
    Array ret = AbstractComposite.array(s.length);
    for (int i = 0; i < s.length; i++)
      ret.add(new AnyString(s[i]));
      
    return ret;
	}

  public Object clone () throws CloneNotSupportedException
  {
    Split s = (Split)super.clone();

    s.toSplit_   = toSplit_.cloneAny();
    s.regex_     = regex_.cloneAny();

    return s;
  }

}
