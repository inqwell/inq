/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.util.StringTokenizer;

/**
 * Give the standard StringTokenizer class an AnyString interface while
 * maintaining the original.
 */
public class AnyStringTokenizer extends StringTokenizer
{
  public AnyStringTokenizer(StringI str, String delim, boolean returnTokens)
  {
    super (str.getValue(), delim, returnTokens);
  }

  public AnyStringTokenizer(StringI str, String delim)
  {
    super (str.getValue(), delim);
  }
  
  public AnyStringTokenizer(StringI str)
  {
    super (str.getValue());
  }
 
  public AnyStringTokenizer(String str, String delim, boolean returnTokens)
  {
    super (str, delim, returnTokens);
  }

  public AnyStringTokenizer(String str, String delim)
  {
    super (str, delim);
  }
  
  public AnyStringTokenizer(String str)
  {
    super (str);
  }
 
  public StringI nextToken(StringI target)
  {
    target.setValue (super.nextToken());
    return target;
  }
  
  public StringI nextToken(StringI target, String delim)
  {
    target.setValue (super.nextToken(delim));
    return target;
  }
}
