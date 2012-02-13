/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.text.ChoiceFormat;
import java.text.FieldPosition;
import java.text.Format;

/**
 * This is a wrapper around the standard java.text.MessageFormat class
 * to support strings parameterised by an array of Any values.
 * The original functionality
 * is unchanged except that the appropriate methods
 * from <code>java.text.MessageFormat</code> are provided to ensure
 * that instances of AnyFormat are installed where appropriate.
 */

public class ArrayMessageFormat extends java.text.MessageFormat
{
  private boolean usesChoice_;
  
  public ArrayMessageFormat (String    pattern) { super(pattern); }
  public ArrayMessageFormat (ConstString pattern) { super(pattern.getValue()); }

  public static String format (String pattern, Array objs)
  {
    ArrayMessageFormat f = new ArrayMessageFormat (pattern);
    return f.format (objs);
  }

  public static String format (StringI pattern, Array objs)
  {
    return ArrayMessageFormat.format (pattern.getValue(), objs);
  }

  // Can't override base class method because the damn
  // thing is final!  Provide a specific one instead
  public StringBuffer format(Array source, StringBuffer result,
                                   FieldPosition ignore)
  {
    Object[] objs = source.toArray();
    StringBuffer ret = super.format(objs,result,ignore);
    
    // If there were any ChoiceFormats in the pattern then at this
    // stage all they have done is make the choice. If the chosen
    // alternative itself contains a format pattern then this is
    // yet to be evaluated. Check the current result and format
    // again if necessary
    if (usesChoice_ && ret.indexOf("{") >= 0)
    {
      ArrayMessageFormat f = new ArrayMessageFormat(ret.toString());
      ret = f.format(objs, new StringBuffer(), ignore);
    }
    return ret;
  }

  public void applyPattern (String pattern)
  {
    super.applyPattern (pattern);

    // Now wrap any formatters the base class created in AnyFormat objects!

    Format[] formats = super.getFormats();

    int i;

    for (i = 0; i < formats.length; i++)
    {
      if (formats[i] != null)
      {
        super.setFormat (i, wrapFormat(formats[i]));
      }
    }
  }

  public void setFormat (int i, Format f)
  {
    if (!(f instanceof AnyFormat))
    {
      super.setFormat (i, wrapFormat (f));
    }
  }

  public void setFormats (Format[] newFormats)
  {
    // We don't assume that super.setFormats calls setFormat - sadly, a
    // look at the source says it doesn't!

    int i;

    for (i = 0; i < newFormats.length; i++)
    {
      newFormats[i] = wrapFormat(newFormats[i]);
    }
    super.setFormats(newFormats);
  }

  private Format wrapFormat (Format f)
  {
    // be sure not to double-wrap
    if (!(f instanceof AnyFormat))
    {
      if (f instanceof ChoiceFormat)
        usesChoice_ = true;
      
      f = new AnyFormat (f);
    }
    return f;
  }
}
