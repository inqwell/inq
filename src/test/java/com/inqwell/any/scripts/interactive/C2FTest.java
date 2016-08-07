package com.inqwell.any.scripts.interactive;

import org.junit.Ignore;
import org.junit.Test;

import com.inqwell.any.parser.InteractiveTestSupport;

@Ignore
public class C2FTest extends InteractiveTestSupport
{
  @Test
  public void test()
  {
  	String args[] = { "-in", "src/main/examples/gui/C2F.inq"}; 
  	run(args);
  }
  
  @Test
  public void test2()
  {
  	String args[] = { "-in", "src/main/examples/gui/datechooser.inq"}; 
  	run(args);
  }
}
