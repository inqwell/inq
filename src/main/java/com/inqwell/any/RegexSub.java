/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/RegexSub.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

/**
 * Perform single or global substitution of a regular expression
 * match in a character sequence.
 * <p>
 * This function accepts two or three operands.  The
 * first operand is always the replacement string. If three
 * operands are supplied they are as:
 * <i>replacement,  replace-in, regex</i>.  The <i>regex</i>
 * operand may be a string or an AnyMatcher instance.  If
 * it is the latter the matcher is reset with the
 * string <i>replace-in</i>, otherwise a temporary matcher
 * is created.
 * <p>
 * If two operands are supplied then the second must be a
 * matcher, which is assumed to be initialised with a pattern
 * and sequence. This matcher will be reset prior to performing
 * the substitution but is not reset afterwards.
 * The return value is a string 
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class RegexSub extends    AbstractFunc
                   implements Cloneable
{
	private Any replWith_;
  private Any replIn_;
  private Any regex_;
  
  private boolean replaceAll_;

	public RegexSub(Any replWith, Any regex)
	{
    this(replWith, null, regex);
	}

	public RegexSub(Any replWith, Any replIn, Any regex)
	{
		replWith_ = replWith;
		replIn_   = replIn;
		regex_    = regex;
	}

	public Any exec(Any a) throws AnyException
	{
		StringI replWith = (StringI)EvalExpr.evalFunc
                                           (getTransaction(),
                                            a,
                                            replWith_,
                                            StringI.class);

    // Could be a StringI or AnyMatcher
		Any regex          = EvalExpr.evalFunc(getTransaction(),
                                           a,
                                           regex_);

    // Could be null if regex is a Matcher
		StringI replIn   =            (StringI)EvalExpr.evalFunc
                                           (getTransaction(),
                                            a,
                                            replIn_,
                                            StringI.class);

    if (replWith == null)
      nullOperand(replWith_);

    if (regex == null)
      nullOperand(regex_);
    
    boolean isMatcher = (regex instanceof AnyMatcher);
    
    if (replIn == null && !isMatcher)
      throw new AnyException("Replace-in is null and regex not a matcher");
    
    AnyMatcher m;
    if (isMatcher)
    {
      m = (AnyMatcher)regex;
      m.setSequence(replIn);  // null is OK - resets matcher
    }
    else
    {
      m = new AnyMatcher(regex, replIn);
    }

    return replaceAll_ ? m.replaceAll(replWith)
                       : m.replaceFirst(replWith);
	}

  public void setReplaceAll(boolean replaceAll)
  {
    replaceAll_ = replaceAll;
  }
  
  public Object clone () throws CloneNotSupportedException
  {
    RegexSub s = (RegexSub)super.clone();

    s.replWith_   = replWith_.cloneAny();
    s.regex_      = regex_.cloneAny();
    s.replIn_     = AbstractAny.cloneOrNull(replIn_);

    return s;
  }

}
