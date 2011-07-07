/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;

import java.text.Format;
import java.text.FieldPosition;

/**
 * This is a version of the standard java.text.MessageFormat class
 * to support strings parameterised by Any values.  The original functionality
 * is unchanged except that the necessary methods are overridden to ensure
 * that instances of AnyFormat are installed where appropriate.
 * TBD - toPattern needs to be overridden should this be requiredas the
 * base class relies on the java.text concrete types to re-create the
 * pattern and accesses the private array of formatters to do it.
 */

public class AnyMessageFormat extends MessageFormat
{
  // See AnyFormat for an explanation of these
  private Map nullStrings_;
  private Map formatDelimiters_;
  
  public AnyMessageFormat (String    pattern) { super(pattern); }
  public AnyMessageFormat (StringI   pattern) { super(pattern.getValue()); }

  public static String format (String pattern, Map objs)
  {
    AnyMessageFormat f = new AnyMessageFormat (pattern);
    return f.format (objs);
  }

  public static String format (StringI pattern, Map objs)
  {
    return AnyMessageFormat.format (pattern.getValue(), objs);
  }

  // Overrides
  public StringBuffer format(Object source, StringBuffer result,
                                   FieldPosition ignore)
  {
    java.util.Map map = ((Map)source).getMap();
    return super.format(map,result,ignore);
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

  public void setNullStrings(Map m)
  {
    nullStrings_ = m;
  }
  
  public void setDelimitersMap(Map delims)
  {
    formatDelimiters_ = delims;
  }
  
  private Format wrapFormat (Format f)
  {
    AnyFormat af = null;
    
    // be sure not to double-wrap
    if (!(f instanceof AnyFormat))
    {
      af = new AnyFormat (f);
        
      f = af;
    }
    else
      af = (AnyFormat)f;

    if (nullStrings_ != null)
      af.setNullStrings(nullStrings_);

    if (formatDelimiters_ != null)
      af.setDelimitersMap(formatDelimiters_);

    return f;
  }
}
