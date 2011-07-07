/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.myorg;

import com.inqwell.any.Any;
import com.inqwell.any.AbstractAny;

public class MyClass extends AbstractAny
{
  public static void stdout()
  {
    System.out.println ("Method Test");
  }

  public static void stdout(Any str)
  {
    System.out.println (str);
  }

  public static MyClass ctor()
  {
  	return new MyClass();
  }

  public MyClass() {}

  public void instanceMethod(Any a)
  {
    System.out.println ("Method Test " + a + " " + this);
  }

}

