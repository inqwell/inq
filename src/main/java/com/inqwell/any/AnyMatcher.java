/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyMatcher.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.util.regex.*;

/**
 * Encapsulates the functionality provided by
 * the <code>java.util.regex</code> package.
 * <p>
 * This class makes all of its functionality available
 * via bean properties - these can therefore be accessed via
 * script constructs like
 * <pre><code>
 *    regex re;
 *    re.properties.pattern = "([A-Z][A-Z][0-9A-Z]+)\\s*,\\s*([0-9]+)";
 *    re.properties.sequence = "US88579Y1010 , 12345";
 *    boolean b = re.properties.find;
 *    if (b)
 *    {
 *      array grps;
 *      grps = re.properties.groups;
 *      foreach(grps)
 *        writeln($catalog.system.out, "|" + $this + "|");
 *    }
 *    else
 *      writeln($catalog.system.out, "No match");
 * </code></pre>
 * where the Inq type to declare a regular expression instance
 * is <code>regex</code>.
 * <p>
 * The properties are as follows:
 * <ul>
 * <li><b>pattern</b>: Type <code>string</code>.
 * Establish the regular expression pattern, for
 * example <code>"[A-Za-z]+[0-9A-Za-z]*"</code>.  This property
 * is write-only.
 * </li>
 * <li><b>sequence</b>: Type <code>string</code>.
 * Establish the character sequence to be
 * processed by the regular expression. This property can only be
 * set after the <code>pattern</code> has been established. If the
 * supplied sequence is null the matcher is reset using the
 * prevailing sequence.  This property is write-only.
 * <li><b>find</b>: Read type <code>boolean</code>, write
 * type <code>int</code>s.
 * On read, determine whether there is another match to
 * be found in the current sequence by the current pattern.
 * Returns <code>true</code> if a match
 * is found, <code>false</code> otherwise. On write, reset the
 * matcher and search for a match from the given starting point.
 * Whether a match can be found is determined by the next read
 * operation.
 * <li><b>end</b>: Type <code>int</code>. Returns the index
 * of the last character matched, plus one.
 * <li><b>group</b>: Type <code>string</code>. Returns the
 * input subsequence matched by the previous match (initiated
 * by find). Note that some patterns, for example a*, match
 * the empty string. This method will return the empty string
 * when the pattern successfully matches the empty string
 * in the input. 
 * <li><b>groupCount</b>: Type <code>int</code>. Returns the
 * number of capturing groups in this matcher's pattern.
 * Group zero denotes the entire pattern by convention. It
 * is not included in this count. 
 * <li><b>groups</b>: Type <code>array</code>. Returns the
 * input subsequences captured by groups specified by
 * this regex. Capturing groups are indexed from left to right,
 * starting at one. Group zero denotes the entire pattern, so
 * the array contains <code>groupCount + 1</code> members.
 * If the match was successful but a given group specified
 * failed to match any part of the input sequence, then null
 * is returned for that array item. Note that some groups, for
 * example (a*), match the empty string. An item will
 * be the empty string when such a group successfully matches
 * the emtpy string in the input. 
 */
public class AnyMatcher extends    PropertyAccessMap
                        implements Cloneable
{
  private Matcher  m_;
  private Pattern  p_;
  private BooleanI b_ = new AnyBoolean();

  private Map      propertyMap_;

	public AnyMatcher()
	{
	}

	public AnyMatcher(Any pattern, StringI sequence)
	{
    this.setPattern(pattern);
    this.setSequence(sequence);
	}

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
			if (a instanceof StringI)
			{
				StringI s = (StringI)a;
				this.setPattern(s);
			}
      else
        throw new IllegalArgumentException("Must copy from a string (to set pattern)");
		}
    return this;
  }

  /**
   * Override.  If the key is <code>"properties"</code> then (make and)
   * return a property binding object.
   */
  public Any get (Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      handleNotExist(key); // throws
      return null;
    }
  }

  public Any getIfContains(Any key)
  {
    if (properties__.equals(key))
    {
      if (propertyMap_ == null)
      {
        propertyMap_ = makePropertyMap();
      }

      return propertyMap_;
    }
    else
    {
      return null;
    }
  }

  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;

    return false;
  }

  /**
   * Check whether there is a further match in the current
   * matcher. The return value must be treated as read-only.
   */
  public Any getFind()
  {
    checkInitialised();
    b_.setValue(m_.find());
    return b_;
  }
  
  public boolean find()
  {
  	checkInitialised();
  	return m_.find();
  }

  /**
   * Rewind the matcher to the given index position and search for
   * a match from there.  Note that, unlike the corresponding get
   * function that returns a boolean, this function accepts an
   * integer to set the position.  As a setProperty function
   * it  can't return anything so after this function has been
   * called the next call to getFind() will return whether a
   * match was found.
   */
  public void setFind(Any i)
  {
    checkInitialised();
    IntI ai = (IntI)i;
    b_.setValue(m_.find(ai.getValue()));
  }

  /**
   * Returns the string last matched, can be empty. The return value
   * can be treated as read/write.
   */
  public Any getGroup()
  {
    checkInitialised();
    return new AnyString(m_.group());
  }

  /**
   * Returns an array containing the strings of the
   * captured groups.
   */
  public Any getGroups()
  {
    checkInitialised();

    int i = m_.groupCount();

    Array ret = null;

    if (i == 0)
      ret = AbstractComposite.array(1);
    else
      ret = AbstractComposite.array(i+1);

    for (int j = 0; j <= i; j++) // grp 0 is the whole string
    {
      String s = m_.group(j);
      ret.add(new AnyString(s));
    }
    return ret;
  }

  public Any getGroupCount()
  {
    checkInitialised();

    return new ConstInt(m_.groupCount());
  }
  
  /**
   * Sets the given pattern into the matcher and discards any
   * previous matcher state.
   */
  public void setPattern(Any pattern)
  {
    p_ = Pattern.compile(pattern.toString());
    m_ = null;
  }

  /**
   * Establish the given sequence in the matcher.  If the matcher
   * does not yet exist (because the pattern has just been set)
   * then it is created.  If the matcher already exists then the
   * sequence is set with the current matcher (and therefore
   * pattern).  If the the sequence is null then the matcher
   * must exist and is reset.
   */
  public void setSequence(StringI sequence)
  {
    if (sequence == null || sequence.isNull())
    {
      checkInitialised();
      m_.reset();
    }
    else
    {
      if (p_ == null)
        throw new AnyRuntimeException("Initialise pattern before sequence");

      if (m_ == null)
        m_ = p_.matcher(sequence.toString());
      else
        m_.reset(sequence.toString());
    }
  }

  public Any getStart()
  {
    checkInitialised();
    return new ConstInt(m_.start());
  }

  public Any getEnd()
  {
    checkInitialised();
    return new ConstInt(m_.end());
  }

  public Any replaceAll(Any replWith)
  {
    checkInitialised();
    return new AnyString(m_.replaceAll(replWith.toString()));
  }
  
  public Any replaceFirst(Any replWith)
  {
    checkInitialised();
    return new AnyString(m_.replaceFirst(replWith.toString()));
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    AnyMatcher m = (AnyMatcher)super.clone();

    if (p_ != null)
      m.p_ = Pattern.compile(p_.pattern());

    // We can't get the sequence so set m_ to null
    m.m_ = null;

    // Clear the properties map
    m.propertyMap_ = null;

    return m;
  }

  public boolean isEmpty() { return false; }

	public Iter createIterator () {return DegenerateIter.i__;}

  protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}
	
	Pattern pattern()
	{
		return p_;
	}

  private void checkInitialised()
  {
    if (m_ == null)
      throw new AnyRuntimeException("matcher not initialised");
  }
}
