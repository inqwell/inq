/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

/**
 * AnyValue defines the interface for single value data classes
 * such as Float, String etc.
 */
public interface Value extends Any
{
  /**
   * Relative rankings of the precision of each scalar value type.  The higher
   * the ranking, the greater the precision.  RANK_CONVERT means that conversion
   * should take place i.e. StringI would have a precision of RANK_CONVERT
   * @see com.inqwell.any.BinaryOpVisitor
   */
  public static final int RANK_UNKNOWN = -2;
  public static final int RANK_DATE = -1;
  public static final int RANK_CONVERT = 0;
  public static final int RANK_BOOLEAN = 1;
  public static final int RANK_BYTE    = 2;
  public static final int RANK_CHAR    = 3;
  public static final int RANK_SHORT   = 4;
  public static final int RANK_INTEGER = 5;
  public static final int RANK_LONG    = 6;
  public static final int RANK_FLOAT   = 7;
  public static final int RANK_DOUBLE  = 8;
  public static final int RANK_DECIMAL = 10;
  public static final int RANK_ASIS    = 1000; // must be highest

  public boolean equals (Any a);
  
  public boolean isNull();
  public void setNull();
}

