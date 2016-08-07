/**
 * Copyright (C) 2012 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.scripts.interactive;

import junit.framework.Assert;

import org.junit.Test;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.Array;
import com.inqwell.any.ConstBigDecimal;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstLong;
import com.inqwell.any.parser.InteractiveTestSupport;

/**
 * Various calculation-type regressions
 * 
 * @author tom
 *
 */
public class MathsTest extends InteractiveTestSupport
{
  /**
   * Run the euler1 regression script
   */
	@Test
  public void euler1()
  {
  	String args[] = { "-in", "src/main/examples/maths/euler1.inq" };
  	Any result = run(args);
  	Assert.assertEquals(new ConstInt(233168), result);
  }
  
  /**
   * Run the euler2 regression script
   */
	@Test
  public void euler2()
  {
  	String args[] = { "-in", "src/main/examples/maths/euler2.inq" };
  	Any result = run(args);
  	Array a = AbstractComposite.array();
  	a.add(new ConstInt(4613732));
  	a.add(new ConstLong(4613732));
  	
  	Assert.assertEquals(a, result);
  }
  
  /**
   * Calculates 12! using the regression script
   */
	@Test
  public void factorial()
  {
  	String args[] = { "-in", "src/main/examples/maths/factorial.inq", "12" };
  	Any result = run(args);
  	Assert.assertEquals(new ConstBigDecimal("479001600", 0), result);
  }
  
  /**
   * Determines the smallest number divisible by the integers 1 to 10
   */
	@Test
  public void jakemaths()
  {
  	String args[] = { "-in", "src/main/examples/maths/jakemaths.inq" };
  	Any result = run(args);
  	Assert.assertEquals(new ConstInt(2520), result);
  }
  
  /**
   * Uses the inq sum() function. While not likely in general use, the
   * highest precision of those values encountered in the summation is
   * that of the result.
   */
	@Test
  public void sum()
  {
  	String args[] = { "-in", "src/main/examples/misc/sum.inq" };
  	Any result = run(args);
  	Assert.assertEquals(new ConstBigDecimal(10), result);
  }
}
