/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/AnyFormat.java $
 * $Author: sanderst $
 * $Revision: 1.12 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.inqwell.any.beans.ClassMap;
import com.inqwell.any.identity.AnyMapDecor;

/**
 * This class acts as a container for the various standard Java concrete
 * formatting classes, as well as itself being a formatter.  This allows us
 * to extend the standard formatting capabilities of the java.text package
 * to Any implementations.
 * <p>
 * The control of formatting through the use of patterns as documented in
 * the JDK for java.text.DecimalFormat and java.text.SimpleDateFormat
 * is supported with the addition of field widths and justification.
 * Any valid JDK format
 * pattern can be prefixed with one or more <code>&lt;</code>
 * or <code>&gt;</code> characters.  The
 * number of characters defines the field width.  <code>&lt;</code>
 * means left justify
 * and <code>&gt;</code> means right justify. Width control
 * works in this way by placing
 * the formatted value in a string padded as appropriate to the
 * chosen justification. This facility is suitable when
 * sending the formatted text to a fixed-width output medium.  It is
 * not generally appropriate to rendering data to a GUI component.
 * The padding character defaults to a space but can be specified and
 * is taken as the character prior to the <code>&lt;</code>
 * or <code>&gt;</code> justification/width characters. For example
 * <pre><code>
 *   0&gt;&gt;&gt;&gt;&gt;&gt;&gt;&gt;#,###.00
 * </code></pre>
 * given the number <code>123.456</code> would generated the formatted
 * result <code>00123.46</code>
 * <p>
 * In addition to widths, formatting directives are supported. These
 * directives apply some additional functionality to the formatting
 * operation. A formatting directive has the syntax <code>name=value</code>.
 * There may be any number of these, each separated with the <code>"@"</code>
 * character and the directives as a whole must appear as a contiguous
 * string, itself bounded <code>"@"</code> characters. Currently, the
 * only supported directive is the <code>zone</code> directive. This
 * directive controls the <code>TimeZone</code> used to
 * format <code><DateI</code> instances.  If absent,
 * an <code><DateI</code> is formatted according to the default time
 * zone for the JVM. Otherwise, the specified time zone is used.  As an
 * example:
 * <code><pre>
 *
 *   "dd MMM yyyy HH:mm@zone='America/New_York'@"
 *
 * </pre></code>
 * will always format the date/time against the New York time zone.
 * <p>
 * All directive values are either quoted strings (as above) or, if not
 * quoted, taken as node paths.  These paths can be resolved against
 * a given root when <code>resolveDirectives</code> is called. The
 * result must be a value suitable for the directive. In the case of
 * <code>zone</code> this means a string whose value is a valid time
 * zone ID.
 * <p>
 * There are convenience methods to make the appropriate format object
 * for the type of Any.  Note that while a formatter for DateI will
 * not do for formatting AnyInt, any kind of formatter can be passed an
 * AnyString and the width and justification settings will be honoured.
 * This is useful for formatting column headings in report generators
 * with the same attributes as the column data, regardless of the type
 * of that data.
 */

public class AnyFormat extends    Format
                       implements Map,
																	Cloneable
{
  /**
   * Output will not be justified
   */
  public static final int NONE  = 0;

  /**
   * Output will be left justified
   */
  public static final int LEFT  = 1;

  /**
   * Output will be right justified
   */
  public static final int RIGHT = 2;

  protected Format     _formatter;
  private   FormatAny  _formatAny;
  private   CanFormat  _canFormat;
  private   ParseAny   _parseAny;
  private   int        _fieldWidth;
  private   int        _justification;
  private   boolean    _doingJustification = true;
  private   boolean    _constrain = false;
  private   char       _padChar = ' ';
  private   boolean    _trailingZeros = true;
  private   Date       _date;
  
  private   DefaultPropertyAccessMap propertyMap_;
  
  // Maps AnyObject(class) to a string that will be placed
  // fore and aft the string produced by the underlying
  // formatter for that class.  Only really useful for applications
  // such as formattng an SQL string where we don't want to be
  // bothered with the syntax of the SQL.  If the value being
  // fornatted is Inq null (isNull() == true) then these
  // delimiters are not used.
  private   Map        _formatDelimiters;
  
  // Class hierarchical sensitive map for specifying
  // the strings to be used when value for formatting is null.
  // Default is the empty string for all value types.
  private   ClassMap   _nullStrings;
  
  private   Map        _directives;
  // True if directives contain expressions, false otherwise
  private   boolean    _mustResolveDirectives = false;
  // Only set to true if _mustResolveDirectives is false, after
  // any directives have been processed for this instance
  private   boolean    _doneDirectives        = false;

  public  static String defaultNumberFormat__ = new String ("#.######");
  public  static String defaultDateFormat__   = new String ("dd/MM/yyyy");
  private static MakeFormat _makeFormat = new MakeFormat();
  
  private static ClassMap defaultNullStrings__;

  private static String zone__ = "zone";

  private static DecimalFormatSymbols decSym__;
  
  static
  {
    defaultNullStrings__ = new ClassMap();
    defaultNullStrings__.add(Any.class, AnyString.EMPTY);
    
    decSym__ = new DecimalFormatSymbols(Locale.getDefault());
  }

  /**
   * Make a formatter.  Makes a formatter suitable for formatting
   * objects of the given Any <code>a</code> with the given
   * pattern.  The pattern must be compatible with the type of
   * object that will be formatted.  If there are format directives
   * embedded in the pattern then the 'clean' format string will
   * be put into <code>cleanPattern</code>.  Otherwise it is set
   * to null
   */
  public static AnyFormat makeFormat (Any a, String pattern, StringI cleanPattern)
  {
    if (cleanPattern != null)
      cleanPattern.setNull();
      
    // Just for Inq null
    if (pattern != null && pattern.length() == 0)
      pattern = null;

		AnyFormat f = _makeFormat.makeFormat(a, pattern, cleanPattern);
    return f;
  }

  public static AnyFormat makeFormat (Any a, String pattern)
  {
    return makeFormat(a, pattern, null);
  }
  
  public static AnyFormat makeFormat (Any a)
  {
    return makeFormat(a, null, null);
  }

  public static String setDefaultNumberFormat(String fmt)
  {
    String old = defaultNumberFormat__;
    defaultNumberFormat__ = fmt;
    return old;
  }

  public static String setDefaultDateFormat(String fmt)
  {
    String old = defaultDateFormat__;
    defaultDateFormat__ = fmt;
    return old;
  }

  public AnyFormat (Format formatter)
  {
    _formatter     = formatter;
    _fieldWidth    = 0;
    _justification = AnyFormat.NONE;
    _padChar       = ' ';
    init();
  }

  public AnyFormat (Format formatter, int fieldWidth, int justification, char padChar)
  {
    _formatter     = formatter;
    _fieldWidth    = fieldWidth;
    _justification = justification;
    _padChar       = padChar;
    init();
  }

  public AnyFormat (Format formatter, int fieldWidth, int justification, char padChar, Date d)
  {
    _formatter     = formatter;
    _fieldWidth    = fieldWidth;
    _justification = justification;
    _padChar       = padChar;
    _date          = d;
    init();
  }

  public AnyFormat (int fieldWidth, int justification, char padChar) // Only for Strings
  {
    _formatter     = null;
    _fieldWidth    = fieldWidth;
    _justification = justification;
    _padChar       = padChar;
    init();
  }

  AnyFormat ()
  {
    _formatter = null; // Only for strings
    _padChar   = ' ';
    init();
  }

  // java.text.Format implements Cloneable so we must also and we have
  // special needs in this regard
  public Object clone ()
  {
    AnyFormat newObject = (AnyFormat)super.clone();

    newObject._formatter = (Format)_formatter.clone();
    newObject.init();

    return newObject;
  }

  public StringBuffer format(Object        obj,
                             StringBuffer  toAppendTo,
                             FieldPosition pos)
  {
    try
    {
      if (_fieldWidth > 0 && _doingJustification)
      {
        _doingJustification = false;

        String formatted = format (obj);

        if (formatted.length() <= _fieldWidth)
          formatted = pad (formatted,
                           _fieldWidth - formatted.length());

        if ((_constrain) &&
            (_fieldWidth != 0) &&
            (formatted.length() > _fieldWidth))
        {
          formatted = formatted.substring(0, _fieldWidth);
        }

        // Now honour original formatting arguments using MessageFormat
        java.text.MessageFormat f = new java.text.MessageFormat("{0}");
        Object[] objs = {formatted};
        return f.format (objs, toAppendTo, pos);
      }
      else
      {
        if (obj instanceof Any)
        {
          return _formatAny.formatAny ((Any)obj, toAppendTo, pos);
        }
        else if (obj == null)
        {
          return new StringBuffer(""); 
        }
        else
        {
          // default to original behaviour
          return _formatter.format (obj, toAppendTo, pos);
        }
      }
    }
    catch (RuntimeException e)
    {
      throw e;
    }
    finally
    {
      _doingJustification = true;
    }
  }

  public Object parseObject (String source, ParsePosition pos)
  {
    return _formatter.parseObject (source, pos);
  }

	/**
	 * Behave as for standard JDK <code>parseObject</code> but then go
	 * on to assign the value to the given Any.  If this formatter
	 * was not created with an Any of the same type then this operation
	 * may fail with a run time exception.
	 */
	public void parseAny (String source, ParsePosition pos, Any a, boolean silent)
	{
		Object o = null;

		// if we were created  for formatting strings then there's no
		// underlying format object
		if (_formatter != null)
		{
      try
      {
        if (pos == null)
          o = _formatter.parseObject(source);
        else
          o = _formatter.parseObject(source, pos);
      }
      catch (ParseException e)
      {
        if (!silent)
          throw new RuntimeContainedException(e);
      }
		}

		if (_parseAny == null)
			_parseAny = new ParseAny();

		_parseAny.parseAny(a, o, source);
	}

	/**
	 * Convenience function.
	 */
	public void parseAny (String source, Any a, boolean silent)
	{
		parseAny (source, null, a, silent);
	}

  public boolean setConstrainToWidth (boolean constrain)
  {
    boolean old = _constrain;
    _constrain  = constrain;
    return old;
  }
  
  public void setPadChar(char padChar)
  {
    _padChar = padChar;
  }
  
  public void setFieldWidth(int fieldWidth)
  {
    _fieldWidth = (fieldWidth < 0) ? 0 : fieldWidth;
  }

  public void setDirectives(Map directives)
  {
    _directives = directives;
    if (_directives == null)
      _doneDirectives = true;
  }
  
  public void setMustResolveDirectives(boolean mustResolveDirectives)
  {
    _mustResolveDirectives = mustResolveDirectives;
  }
  
  public void resolveDirectives(Any a, Transaction t) throws AnyException
  {
    if (_directives == null)
    {
      _doneDirectives = true;
      return;
    }
    
    if (_mustResolveDirectives || !_doneDirectives)
    {
      Map resolvedDirectives = _directives;
      if (_mustResolveDirectives)
        resolvedDirectives = (Map)_directives.cloneAny();

      Iter i = _directives.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        Any d = resolvedDirectives.get(k);
        
        if (_mustResolveDirectives)
          d = EvalExpr.evalFunc(t,
                                a,
                                d);
        
        processDirective(k, d);
      }
      
      if (!_mustResolveDirectives)
        _doneDirectives = true;
    }
    
    // The only directive currently supported is @zone=a.b.c@
    // or @zone='Europe/London'@.  This can be obeyed now, before
    // we do the formatting.  If this ceases to be the case
    // then we would have to make the resolved directives a
    // member and clear after use.
  }
  
  public boolean canFormat(Any a)
  {
    if (a == null)
      return true;

    if (_canFormat == null)
      _canFormat = new CanFormat();
    
    return _canFormat.canFormat(a);
  }
  
  public void setDelimitersMap(Map delims)
  {
    _formatDelimiters = delims;
  }
  
  public void setNullStrings(Map m)
  {
    _nullStrings = (ClassMap)defaultNullStrings__.cloneAny();
    Iter i = m.createKeysIterator();
    while (i.hasNext())
    {
      Any k = i.next();
      _nullStrings.add((ObjectI)k, m.get(k));
    }
    //System.out.println("NullStrings: " + _nullStrings);
  }
  
  /*******************************************************************/
  /* Property Access Methods                                         */
  /*******************************************************************/

  public void setGroupingUsed(boolean used)
  {
    if (_formatter instanceof NumberFormat)
      ((NumberFormat)_formatter).setGroupingUsed(used);
  }
  
  public boolean isGroupingUsed()
  {
    if (_formatter instanceof NumberFormat)
      return ((NumberFormat)_formatter).isGroupingUsed();
    else
      return false;
  }
  
  public boolean hasGrouping()
  {
    return (_formatter instanceof NumberFormat);
  }
  
  public char getDecimalSeparator()
  {
    if (_formatter instanceof DecimalFormat)
      return ((DecimalFormat)_formatter).getDecimalFormatSymbols().getDecimalSeparator();
    else
    {
      throw new IllegalStateException("Not a DecimalFormat");
    }
  }
  
  public void setDecimalSeparator(char separator)
  {
    if (_formatter instanceof DecimalFormat)
      ((DecimalFormat)_formatter).getDecimalFormatSymbols().setDecimalSeparator(separator);
    else
    {
      throw new IllegalStateException("Not a DecimalFormat");
    }
  }
  
  public char getGroupingSeparator()
  {
    if (_formatter instanceof DecimalFormat)
      return ((DecimalFormat)_formatter).getDecimalFormatSymbols().getGroupingSeparator();
    else
    {
      throw new IllegalStateException("Not a DecimalFormat");
    }
  }
  
  public void setGroupingSeparator(char separator)
  {
    if (_formatter instanceof DecimalFormat)
      ((DecimalFormat)_formatter).getDecimalFormatSymbols().setGroupingSeparator(separator);
    else
    {
      throw new IllegalStateException("Not a DecimalFormat");
    }
  }
  
  public String getCurrencySymbol()
  {
    if (_formatter instanceof DecimalFormat)
      return ((DecimalFormat)_formatter).getDecimalFormatSymbols().getCurrencySymbol();
    else
    {
      throw new IllegalStateException("Not a DecimalFormat");
    }
  }
  
  public void setCurrencySymbol(String currency)
  {
    if (_formatter instanceof DecimalFormat)
      ((DecimalFormat)_formatter).getDecimalFormatSymbols().setCurrencySymbol(currency);
    else
    {
      throw new IllegalStateException("Not a DecimalFormat");
    }
  }
  
  public String getNegativePrefix()
  {
    if (_formatter instanceof DecimalFormat)
      return ((DecimalFormat)_formatter).getNegativePrefix();
    else
    {
      throw new IllegalStateException("Not a DecimalFormat");
    }
  }
  
  public void setNegativePrefix(String prefix)
  {
    if (_formatter instanceof DecimalFormat)
      ((DecimalFormat)_formatter).setNegativePrefix(prefix);
    else
    {
      throw new IllegalStateException("Not a DecimalFormat");
    }
  }
  
  public String getNegativeSuffix()
  {
    if (_formatter instanceof DecimalFormat)
      return ((DecimalFormat)_formatter).getNegativeSuffix();
    else
    {
      throw new IllegalStateException("Not a DecimalFormat");
    }
  }
  
  public void setNegativeSuffix(String suffix)
  {
    if (_formatter instanceof DecimalFormat)
      ((DecimalFormat)_formatter).setNegativeSuffix(suffix);
    else
    {
      throw new IllegalStateException("Not a DecimalFormat");
    }
  }
  
  public void setTrailingZeros(boolean trailingZeros)
  {
    _trailingZeros = trailingZeros;
  }
  
  public AnyTimeZone getTimeZone()
  {
    if (_formatter instanceof DateFormat)
    {
      return new AnyTimeZone(((DateFormat)_formatter).getTimeZone());
    }
    return null;
  }
  
  public void setTimeZone(Any tz)
  {
    if (!(_formatter instanceof DateFormat))
      throw new IllegalStateException("Not a DateFormat");
    
    DateFormat f = (DateFormat)_formatter;
    
    if (tz instanceof AnyTimeZone)
    {
      AnyTimeZone atz = (AnyTimeZone)tz;
      f.setTimeZone(atz.getTimeZone());
    }
    else if (tz instanceof StringI)
    {
      f.setTimeZone(TimeZone.getTimeZone(tz.toString()));
    }
  }
  
  public void setCurrency(Any ccy)
  {
    if (!(_formatter instanceof NumberFormat))
      throw new IllegalStateException("Not a NumberFormat");
    
    NumberFormat f = (NumberFormat)_formatter;
    
    if (ccy instanceof AnyCurrency)
    {
      AnyCurrency accy = (AnyCurrency)ccy;
      f.setCurrency(accy.getCurrency());
    }
    else if (ccy instanceof StringI)
    {
      f.setCurrency(Currency.getInstance(ccy.toString()));
    }
  }
  
  public Any getCurrency()
  {
    if (_formatter instanceof NumberFormat)
    {
      return new AnyCurrency(((NumberFormat)_formatter).getCurrency());
    }
    return null;
  }
  
  public boolean isLenient()
  {
    if (!(_formatter instanceof DateFormat))
      throw new IllegalStateException("Not a DateFormat");

    DateFormat f = (DateFormat)_formatter;
    
    return f.isLenient();
  }
    
  public void setLenient(boolean lenient)
  {
    if (!(_formatter instanceof DateFormat))
      throw new IllegalStateException("Not a DateFormat");

    DateFormat f = (DateFormat)_formatter;
    
    f.setLenient(lenient);
  }
    
  /*******************************************************************/
  /* End Property Access Methods                                         */
  /*******************************************************************/

  public String toString()
  {
    return (_formatter != null) ? _formatter.toString() : "null";
  }
  
  protected void processDirective(Any dName, Any dValue)
  {
    if (dName.toString().equals(zone__))
    {
      // value is a string that is a timezone to put into this
      // formatter (must therefore be a DateFormat)
      TimeZone tz = TimeZone.getTimeZone(dValue.toString());
      ((DateFormat)_formatter).setTimeZone(tz);
    }
    // add more else clauses here
  }
  
  // formatting functions for exposed Any underlying values

  protected void formatByte (byte          b,
                             StringBuffer  toAppendTo,
                             FieldPosition pos)
  {
    ((NumberFormat)_formatter).format (b, toAppendTo, pos);
  }
  protected void formatInt (int           i,
                            StringBuffer  toAppendTo,
                            FieldPosition pos)
  {
    ((NumberFormat)_formatter).format (i, toAppendTo, pos);
  }

  protected void formatShort (short         s,
                              StringBuffer  toAppendTo,
                              FieldPosition pos)
  {
    ((NumberFormat)_formatter).format (s, toAppendTo, pos);
  }

  protected void formatLong (long          l,
                             StringBuffer  toAppendTo,
                             FieldPosition pos)
  {
    ((NumberFormat)_formatter).format (l, toAppendTo, pos);
  }

  protected void formatFloat (float         f,
                              StringBuffer  toAppendTo,
                              FieldPosition pos)
  {
    ((NumberFormat)_formatter).format (f, toAppendTo, pos);
  }

  protected void formatDouble (double        d,
                               StringBuffer  toAppendTo,
                               FieldPosition pos)
  {
    ((NumberFormat)_formatter).format (d, toAppendTo, pos);

    if (!_trailingZeros)
    {
      trimTrailingZeros(toAppendTo, pos);
    }
  }

  protected void formatDecimal (Decimal       d,
                                StringBuffer  toAppendTo,
                                FieldPosition pos)
  {
    if (_formatter instanceof NumberFormat)
      formatDouble(d.doubleValue(), toAppendTo, pos);
    else
    {
      toAppendTo.append(d.toString());
      if (!_trailingZeros)
      {
        trimTrailingZeros(toAppendTo, pos);
      }
    }
  }

  protected void formatMap(Map           m,
                           StringBuffer  toAppendTo,
                           FieldPosition pos)
  {
	  _formatter.format(m, toAppendTo, pos);
	}

  protected void formatArray(Array         a,
	                           StringBuffer  toAppendTo,
	                           FieldPosition pos)
  {
	  ((ArrayMessageFormat)_formatter).format(a, toAppendTo, pos);
	}

  protected void formatString (StringI       as,
                               StringBuffer  toAppendTo,
                               FieldPosition pos)
  {
    String s = as.getValue();
    
    String delim = null;
    if (_formatDelimiters != null && _formatDelimiters.contains(AnyString.class__))
      delim = _formatDelimiters.get(AnyString.class__).toString();
      
    if (delim != null)
      toAppendTo.append(delim);

    if (_formatter instanceof java.text.MessageFormat)
    {
      Object[] objs = {s};
      _formatter.format (objs, toAppendTo, pos);
    }
    else
    {
	    toAppendTo.append(s);
	  }
    if (delim != null)
      toAppendTo.append(delim);
  }

  protected void formatDate (Date          d,
                             StringBuffer  toAppendTo,
                             FieldPosition pos)
  {
    // Seriously yucky and possibly only temporary - we've been given
    // a date but are wrapping a numeric formatter.  Format the date's
    // milliseconds since epoch value.
    if (!(_formatter instanceof DateFormat))
    {
      formatLong(d.getTime(), toAppendTo, pos);
      return;
    }
    
    String delim = null;
    if (_formatDelimiters != null && _formatDelimiters.contains(AnyDate.class__))
      delim = _formatDelimiters.get(AnyDate.class__).toString();
      
    if (delim != null)
      toAppendTo.append(delim);

    ((DateFormat)_formatter).format (d, toAppendTo, pos);

    if (delim != null)
      toAppendTo.append(delim);
  }

  private void trimTrailingZeros(StringBuffer  toAppendTo,
                                 FieldPosition pos)
  {
    int deleteFrom = -1;
    char decSep = decSym__.getDecimalSeparator();
    int indx = toAppendTo.length();
    while (--indx >= 0 && deleteFrom == -1)
      if (toAppendTo.charAt(indx) == decSep)
        deleteFrom = indx;
    
    if (deleteFrom >= 0)
    {
      int len = toAppendTo.length();
      indx = deleteFrom + 1;
      while (indx < len)
      {
        if (toAppendTo.charAt(indx) != '0')
          deleteFrom = ++indx;
        else
          ++indx;
      }
      toAppendTo.delete(deleteFrom, len);
      // Do I need to adjust pos in any way?
    }
  }
  
  private String pad (String s, int numChars)
  {
    StringBuffer b = new StringBuffer();

    for (int i = 0; i < numChars; i++)
      b.append(_padChar);

    if (_justification == AnyFormat.RIGHT)
    {
      b.append(s);
    }
    else
    {
      b.insert(0, s);
    }
    return new String(b);
  }

  private void init()
  {
    _formatAny   = new FormatAny ();
    _parseAny    = null;
    _nullStrings = defaultNullStrings__;
  }

  private void appendString(String s, StringBuffer b)
  {
    b.append(s);
  }
  
  public Iter createIterator () {return DegenerateIter.i__;}

  public void accept (Visitor v)
  {
    v.visitMap(this);
  }

  public Any copyFrom (Any a)
  {
    throw new IllegalArgumentException ("copyFrom() not supported");
  }

  public Any buildNew (Any a)
  {
    throw new IllegalArgumentException ("buildNew() not supported");
  }

  public boolean like (Any a)
  {
    return false;
  }

  public boolean isTransactional()
  {
		return false;
  }

  public boolean isConst()
  {
    return false;
  }
    
  public Any bestowConstness()
  {
    return this;
  }
  
  public final Any cloneAny ()
  {
    Any a = null;

    try
    {
      a = (Any)clone();
    }
    catch (Exception e)
    {
      throw (new IllegalArgumentException ("cloneAny exception: " +
                                           getClass().getName()));
    }
    return a;
  }

  public void add(Any key, Any value)
  {
    throw new UnsupportedOperationException();
  }

  public void add(StringI keyAndValue)
  {
    throw new UnsupportedOperationException();
  }

  public Map bestowIdentity()
  {
    return new AnyMapDecor(this);
  }

  public boolean contains(Any key)
  {
    if (key.equals(PropertyAccessMap.properties__))
      return true;

    return false;
  }

  public boolean containsValue(Any value)
  {
    throw new UnsupportedOperationException();
  }

  public Iter createKeysIterator()
  {
    throw new UnsupportedOperationException();
  }

  public Iter createConcurrentSafeKeysIterator()
  {
    throw new UnsupportedOperationException();
  }
  
  public Any get(Any key)
  {
    if (propertyMap_ == null)
      propertyMap_ = new FormatPropertyAccess();

    return propertyMap_.get(key);
  }

  public Descriptor getDescriptor()
  {
    return Descriptor.degenerateDescriptor__;
  }

  public Any getIfContains(Any key)
  {
    if (propertyMap_ == null)
      propertyMap_ = new FormatPropertyAccess();

    return propertyMap_.getIfContains(key);
  }

  public java.util.Map getMap()
  {
    throw new UnsupportedOperationException();
  }

  public short getPrivilegeLevel(Any access, Any key)
  {
    if (propertyMap_ == null)
      propertyMap_ = new FormatPropertyAccess();

    return propertyMap_.getPrivilegeLevel(access, key);
  }

  public Object getPropertyBean()
  {
    throw new UnsupportedOperationException();
  }

  public Any getUniqueKey()
  {
    if (propertyMap_ == null)
      propertyMap_ = new FormatPropertyAccess();

    return propertyMap_.getUniqueKey();
  }

  public boolean hasKeys(Array keys)
  {
    throw new UnsupportedOperationException();
  }

  public Array keys()
  {
    throw new UnsupportedOperationException();
  }

  public Any getMapKey(Any key)
  {
    throw new UnsupportedOperationException();
  }
  
  public Any remove(Any key)
  {
    throw new UnsupportedOperationException();
  }

  public void replaceItem(Any key, Any item)
  {
    throw new UnsupportedOperationException();
  }

  public void replaceValue(Any key, Any value)
  {
    throw new UnsupportedOperationException();
  }

  public void setContext(Any context)
  {
    throw new UnsupportedOperationException();
  }

  public void setDescriptor(Descriptor d)
  {
    throw new UnsupportedOperationException();
  }

  public void setAux(Any aux)
  {
    throw new UnsupportedOperationException();
  }
  
  public Any getAux()
  {
    throw new UnsupportedOperationException();
  }
  
  public void setPrivilegeLevels(Map levels, Any key, boolean merge)
  {
    if (propertyMap_ == null)
      propertyMap_ = new FormatPropertyAccess();

    propertyMap_.setPrivilegeLevels(levels, key, merge);
  }

  public void setPropertyBean(Object bean)
  {
    throw new UnsupportedOperationException();
  }

  public void setTransactional(boolean isTransactional)
  {
    throw new UnsupportedOperationException();
  }

  public void setUniqueKey(Any keyVal)
  {
    if (propertyMap_ == null)
      propertyMap_ = new FormatPropertyAccess();

    propertyMap_.setUniqueKey(keyVal);
  }

  public Map shallowCopy()
  {
    throw new UnsupportedOperationException();
  }

  public Composite shallowCopyOf()
  {
    throw new UnsupportedOperationException();
  }

  public void add(Any element)
  {
    throw new UnsupportedOperationException();
  }

  public boolean containsAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  public boolean containsAny(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  public void empty()
  {
    throw new UnsupportedOperationException();
  }

  public int entries()
  {
    return 1;
  }

  public boolean equals(Any a)
  {
    return this == a;
  }

  public Any getNameInParent()
  {
    throw new UnsupportedOperationException();
  }

  public Any getNodeSet()
  {
    return null;
  }

  public Composite getParentAny()
  {
    throw new UnsupportedOperationException();
  }

  public Any getPath()
  {
    throw new UnsupportedOperationException();
  }

  public Process getProcess()
  {
    return null;
  }

  public boolean hasIdentity()
  {
    return false;
  }

  public int identity()
  {
    return System.identityHashCode(this);
  }

  public boolean isDeleteMarked(Any id)
  {
    throw new UnsupportedOperationException();
  }

  public boolean isEmpty()
  {
    return false;
  }

  public boolean isParentable()
  {
    return false;
  }

  public void markForDelete(Any id)
  {
    throw new UnsupportedOperationException();
  }

  public void removeAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  public void removeInParent()
  {
  }

  public void retainAll(Composite c)
  {
    throw new UnsupportedOperationException();
  }

  public void setNodeSet(Any nodeSet)
  {
    throw new UnsupportedOperationException();
  }

  public void setParent(Composite parent)
  {
  }
  
  private class FormatPropertyAccess extends DefaultPropertyAccessMap
  {
    protected Object getPropertyOwner(Any property)
    {
      return AnyFormat.this;
    }
    
  }

  /**
   * Convenience class to make a formatter object suitable for the given
   * Any.
   */
  static private class MakeFormat extends AbstractVisitor
  {
    private AnyFormat _formatter;
    private String    _pattern;
    private StringI   _cleanPattern;
    private int       _fieldWidth;
    private int       _justification;
    private char      _padChar;
    
    synchronized AnyFormat makeFormat (Any a, String pattern, StringI cleanPattern)
    {
      _cleanPattern = cleanPattern;
      
      int patternIndex = -1;
  
      _justification = AnyFormat.NONE;
      _padChar       = ' ';
  
      if (pattern != null)
      {
        if ((patternIndex = pattern.lastIndexOf('<')) >= 0)
        {
          _justification = AnyFormat.LEFT;
        }
        else if ((patternIndex = pattern.lastIndexOf('>')) >= 0)
        {
          _justification = AnyFormat.RIGHT;
        }
      }
  
      if (patternIndex < 0)
      {
        _pattern = pattern;
        _fieldWidth = 0;   // means as is
      }
      else
      {
        _pattern = pattern.substring(patternIndex + 1);
        
        // Check if there is a specified padding character
        char padChar = pattern.charAt(0);
        if (padChar != '>' &&
            padChar != '<')
        {
          // There is - decrement the pattern index to take account
          patternIndex--;
          _padChar = padChar;
        }
          
        _fieldWidth = patternIndex + 1;
      }
  
      a.accept(this);
      _cleanPattern = null;
      AnyFormat f = _formatter;
      _formatter = null;
      return f;
    }
  
    public void visitAnyByte (ByteI b)
    {
      _formatter = decimalFormat ();
    }
  
    public void visitAnyInt (IntI i)
    {
      _formatter = decimalFormat ();
    }
  
    public void visitAnyShort (ShortI s)
    {
      _formatter = decimalFormat ();
    }
  
    public void visitAnyLong (LongI l)
    {
      _formatter = decimalFormat ();
    }
  
    public void visitAnyFloat (FloatI f)
    {
      _formatter = decimalFormat ();
    }
  
    public void visitAnyDouble (DoubleI d)
    {
      _formatter = decimalFormat ();
    }
  
    public void visitAnyDate (DateI d)
    {
      if (_pattern != null)
      {
        Map directives = parseDateDirectives(_pattern);
        boolean mustResolve = mustResolveDirectives(directives);
        
        // If the pattern is a '#' character then create a
        // NumberFormat to format the date as a number
        // since the epoch.  See also parse function.
        if (_pattern.charAt(0) == '#')
          _formatter = decimalFormat();
        else
          _formatter = new AnyFormat (new SimpleDateFormat (_pattern),
                                      _fieldWidth,
                                      _justification,
                                      _padChar,
                                      new Date());
                                    
        _formatter.setDirectives(directives);
        _formatter.setMustResolveDirectives(mustResolve);
        if (_cleanPattern != null)
          _cleanPattern.setValue(_pattern);
      }
      else
        _formatter = new AnyFormat
                           (new SimpleDateFormat (AnyFormat.defaultDateFormat__),
                            _fieldWidth,
                            _justification,
                            _padChar,
                            new Date());
    }
  
    public void visitDecimal(Decimal d)
    {
      if (_pattern == null)
        _pattern = AnyFormat.defaultNumberFormat__;
  
      // If a pattern is specified then honour it but limit the
      // decimal part to the scale of the decimal.
      int dp = _pattern.indexOf('.');
      if (dp >= 0)
      {
        int scale = d.scale();
        dp        = _pattern.length() - dp - 1;
        if (dp > scale)
          _pattern = _pattern.substring(0, _pattern.length() - dp + scale);
      }
      _formatter = decimalFormat ();
    }
    
    public void visitAnyString (StringI s)
    {
      // For strings use a java.text.MessageFormat
      // so we can honour any spaces, special chars etc
      int  index   = -1;
      char replace = 'x';
  
      if (_pattern != null)
      {
        index = _pattern.indexOf("{x}");
        if (index < 0)
        {
          index = _pattern.indexOf("{X}");
          replace = 'X';
        }
      }
  
      if (index >= 0)
      {
        _formatter = new AnyFormat(new java.text.MessageFormat(_pattern.replace(replace, '0')),
                                   _fieldWidth,
                                   _justification,
                                   _padChar);
      }
      else
      {
        _formatter = new AnyFormat(new java.text.MessageFormat("{0}"),
                                   _fieldWidth,
                                   _justification,
                                   _padChar);
      }
    }
  
    public void visitAnyObject (ObjectI o)
    {
      this.visitAnyString(null);
    }
    
    public void visitUnknown (Any o)
    {
      this.visitAnyString(null);
    }
    
    public void visitMap (Map m)
    {
      // For maps use a com.inqwell.any.AnyMessageFormat
      // so we can support complex format aggregations
      String pattern = (_pattern != null) ? _pattern : "";
      _formatter = new AnyFormat(new AnyMessageFormat(pattern),
                                 _fieldWidth,
                                 _justification,
                                 _padChar);
    }
  
    public void visitArray (Array a)
    {
      // For maps use a com.inqwell.any.ArrayMessageFormat
      // so we can support complex format aggregations
      _formatter = new AnyFormat(new ArrayMessageFormat(_pattern),
                                 _fieldWidth,
                                 _justification,
                                 _padChar);
    }
  
    /**
  	 * Override base.  If the Any is not formattable (i.e. its something like
  	 * an icon) then just ignore
  	 */
    protected void unsupportedOperation (Any o)
    {
  		_formatter = null;
    }
  
    private AnyFormat decimalFormat()
    {
  		DecimalFormat df;
  
      if (_pattern != null)
        df = new DecimalFormat(_pattern);
      else
        df = new DecimalFormat(AnyFormat.defaultNumberFormat__);
  
  		df.setParseIntegerOnly(false);
  
  		return new AnyFormat(df, _fieldWidth, _justification, _padChar);
    }
    
    // Process any formatting directives and place them in a
    // map whose keys are the directive names and values the
    // directive values.  Return the map or null if no directives
    // are present in the format pattern.
    // Also fix up the _pattern by removing the directives
    // after processing, so that it is acceptable to the underlying
    // java formatters.
    private Map parseDateDirectives(String pattern)
    {
    	Map directives = null;
    	int startIndx  = -1;
    	int endIndx    = -1;
    	int thisStart  = 0;
  
      // locate start and end directive delimiters
    	if ((startIndx = pattern.indexOf('@')) >= 0)
    	{
        directives = AbstractComposite.simpleMap();
        
        thisStart = pattern.indexOf('@', thisStart);
        
        while (thisStart < pattern.length())
        {
          if ((endIndx = pattern.indexOf('@', thisStart+1)) < 0)
          {
            endIndx = thisStart;
            break;
          }
  
          // get the directive string excluding delimiters
          String directive = pattern.substring(thisStart+1, endIndx);
          if (directive.length() != 0)
            directives.add(new ConstString(directive));
  
          thisStart = endIndx;
        }
        
        // Parsed directives are now in the map, remove them from
        // the format string.
        _pattern = pattern.substring(0, startIndx) + pattern.substring(endIndx+1);
    	}
    	
    	return directives;
    }
    
    // Determine up-front whether directives, when used, will require
    // resolving (because they are expressions) or are just literals.
    // Optimises formatter when it is used.
    private boolean mustResolveDirectives(Map directives)
    {
      boolean mustResolve = false;
      if (directives != null)
      {
        Iter i = directives.keys().createIterator();
        while (i.hasNext())
        {
          Any dName = i.next();
          
          Any dValue = directives.get(dName);
          
          // Check if directive is a string literal or a node path.
          // If its a path then replace the element with a LocateNode
          // function
          int startIndx = -1;
          String dStr = dValue.toString();
          if ((startIndx = dStr.indexOf('\'')) < 0)
          {
            LocateNode l = new LocateNode(dStr);
            directives.replaceItem(dName, l);
            mustResolve = true;
          }
          else
          {
            int endIndx = dStr.indexOf('\'', startIndx+1);
            ConstString l = new ConstString(dStr.substring(startIndx+1, endIndx));
            directives.replaceItem(dName, l);
          }
        }
      }
      return mustResolve;
    }
  }

  // Determine what kind of Any we are formatting
  private class FormatAny extends AbstractVisitor
  {
    private StringBuffer  _toAppendTo;
    private FieldPosition _pos;

    public StringBuffer formatAny (Any           a,
                                   StringBuffer  toAppendTo,
                                   FieldPosition pos)
    {
      _toAppendTo = toAppendTo;
      _pos        = pos;

      a.accept (this);

      return _toAppendTo;
    }

    // toString may be used for Booleans

    public void visitAnyByte (ByteI b)
    {
      if (!b.isNull())
        AnyFormat.this.formatByte (b.getValue(), _toAppendTo, _pos);
      else
        appendString(_nullStrings.get(AnyByte.class__).toString(),
                     _toAppendTo);
    }

    // toString may be used for Chars

    public void visitAnyInt (IntI i)
    {
      if (!i.isNull())
        AnyFormat.this.formatInt (i.getValue(), _toAppendTo, _pos);
      else
        appendString(_nullStrings.get(AnyInt.class__).toString(),
                     _toAppendTo);
    }

    public void visitAnyShort (ShortI s)
    {
      if (!s.isNull())
        AnyFormat.this.formatShort (s.getValue(), _toAppendTo, _pos);
      else
        appendString(_nullStrings.get(AnyShort.class__).toString(),
                     _toAppendTo);
    }

    public void visitAnyLong (LongI l)
    {
      if (!l.isNull())
        AnyFormat.this.formatLong (l.getValue(), _toAppendTo, _pos);
      else
        appendString(_nullStrings.get(AnyLong.class__).toString(),
                     _toAppendTo);
    }

    public void visitAnyFloat (FloatI f)
    {
      if (!f.isNull())
        AnyFormat.this.formatFloat (f.getValue(), _toAppendTo, _pos);
      else
        appendString(_nullStrings.get(AnyFloat.class__).toString(),
                     _toAppendTo);
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (!d.isNull())
        AnyFormat.this.formatDouble (d.getValue(), _toAppendTo, _pos);
      else
        appendString(_nullStrings.get(AnyDouble.class__).toString(),
                     _toAppendTo);
    }

    public void visitDecimal (Decimal d)
    {
      if (!d.isNull())
        AnyFormat.this.formatDecimal (d, _toAppendTo, _pos);
      else
        appendString(_nullStrings.get(Decimal.class__).toString(),
                     _toAppendTo);
    }

    // Strings use toString inside MessageFormat but are acceptable here
    //anyway

    public void visitAnyString (StringI s)
    {
      if (!s.isNull())
        AnyFormat.this.formatString (s, _toAppendTo, _pos);
      else
        appendString(_nullStrings.get(AnyString.class__).toString(),
                     _toAppendTo);
    }

    public void visitAnyBoolean (BooleanI b)
    {
      _toAppendTo.append(b.toString());
    }

    public void visitAnyObject (ObjectI o)
    {
      if (o.getValue() != null)
        _toAppendTo.append(o.toString());
      else
        appendString(_nullStrings.get(AnyString.class__).toString(),
                     _toAppendTo);
    }

    public void visitUnknown (Any o)
    {
      appendString(_nullStrings.get(AnyString.class__).toString(),
                   _toAppendTo);
    }

    public void visitAnyDate (DateI d)
    {
      if (!d.isNull())
      {
        if (_date == null)
          _date = new Date();
        
        _date.setTime(d.getTime());
        AnyFormat.this.formatDate (_date, _toAppendTo, _pos);
      }
      else
        appendString(_nullStrings.get(AnyDate.class__).toString(),
                     _toAppendTo);
    }

    public void visitMap (Map m)
    {
      AnyFormat.this.formatMap (m, _toAppendTo, _pos);
    }

    public void visitArray (Array a)
    {
      AnyFormat.this.formatArray (a, _toAppendTo, _pos);
    }
  }

  // Determine what kind of Any we are parsing
  private class ParseAny extends AbstractVisitor
  {
    private Object    _valueParsed;
    private AnyDouble _double = new AnyDouble();
    private AnyLong   _long   = new AnyLong();
    private String    _source;

    /**
     * Extract value parsed into <code>o</code> and place into Any <code>a</code>.
     * We assume the type of <code>o</code> from the type of <code>a</code>.
     */
    public void parseAny (Any a, Object o, String source)
    {
      _valueParsed = o;
      _source      = source;

      a.accept (this);
    }

    // toString may be used for Booleans

    public void visitAnyByte (ByteI b)
    {
      parseNumber(b);
    }

    // toString may be used for Chars

    public void visitAnyInt (IntI i)
    {
      parseNumber(i);
    }

    public void visitAnyShort (ShortI s)
    {
      parseNumber(s);
    }

    public void visitAnyLong (LongI l)
    {
      parseNumber(l);
    }

    public void visitAnyFloat (FloatI f)
    {
      parseNumber(f);
    }

    public void visitAnyDouble (DoubleI d)
    {
      parseNumber(d);
    }

    public void visitDecimal (Decimal d)
    {
      if (_valueParsed == null)
      {
        d.setNull();
        return;
      }
//      System.out.println("Value Parsed " + _valueParsed);
//      System.out.println("Class " + _valueParsed.getClass());
//      System.out.println("Source " + _source);
      
      // If we were a double formatter (because we wanted group
      // separators in the formatted text) then there will be
      // something in _valueParsed, however it will be a Java
      // Double, which we cannot safely pass to a decimal (in any
      // case, the magnitude of a double is finite, so this could
      // break anyway).  The only safe way to do this is to
      // strip any grouping separators and copy from the resulting
      // string
      //String s = _source;
      String s = _valueParsed.toString();
      if (_formatter instanceof DecimalFormat)
      {
        char gs = 0;
        int bdx = 0;
        int len = _source.length();
        gs = ((DecimalFormat)_formatter).getDecimalFormatSymbols().getGroupingSeparator();
        //System.out.println("gs is " + gs);
        //System.out.println("source is " + ss);
        int idx = _source.indexOf(gs);
        StringBuffer sb = null;
        while (idx >= 0 && bdx < len)
        {
          if (sb == null)
            sb = new StringBuffer();
          sb.append(_source.substring(bdx, idx));
          bdx = idx + 1;
          if (bdx != len)  // when ends in a group separator (well, you never know!)
            idx = _source.indexOf(gs, bdx);
        }
        // Add anything remaining
        if (sb != null && bdx != len)
          sb.append(_source.substring(bdx));
        
        if (sb != null)
          s = sb.toString();
      }
      //System.out.println("Parsing decimal from " + s);
      if (s.length() == 0)
        d.setNull();
      else
        d.fromString(s);
    }

    // Strings just get copied from the original parsing source
    // (current assumption here is that the source does not contain
    // more than we want, since we don't honour position argument)
    public void visitAnyString (StringI s)
    {
      // TODO: should zero length be inq null? Say yes
      if (_source.length() == 0)
        s.setNull();
      else
        s.setValue(_source);
    }

    public void visitAnyObject(ObjectI o)
    {
      // Hmmm, assume a string will do. May be its a blob?
      o.setValue(_source);
    }
    
    public void visitAnyDate (DateI d)
    {
      parseDate(d);
    }

    private void parseNumber(Value a)
    {
      if (_valueParsed == null)
      {
        a.setNull();
        return;
      }
      
      // Object must be a Long or a Double, according to JDK docs
      if (_valueParsed instanceof Long)
      {
        Long l = (Long)_valueParsed;
        _long.setValue (l.longValue());
        a.copyFrom (_long);
      }
      else if (_valueParsed instanceof Double)
      {
        Double d = (Double)_valueParsed;
        _double.setValue (d.doubleValue());
        a.copyFrom (_double);
      }
      else
      {
        throw new IllegalArgumentException("Unknown type from parseObject");
      }
    }

    private void parseDate(DateI d)
    {
      if (_valueParsed == null)
      {
        d.setNull();
        return;
      }
      
      if (_valueParsed instanceof java.util.Date)
      {
        java.util.Date jDate = (java.util.Date)_valueParsed;
        d.setValue (jDate);
      }
      else if (_valueParsed instanceof Long)
      {
        // Its milliseconds since epoch
        Long l = (Long)_valueParsed;
        d.setTime (l.longValue());
      }
      else
      {
        throw new IllegalArgumentException("Unknown type from parseObject");
      }
    }
  }

  // Determine whether our current formatter is capable of
  // formatting the given Any
  private class CanFormat extends AbstractVisitor
  {
    private boolean canFormat_;

    public boolean canFormat(Any a)
    {
      a.accept(this);
      return canFormat_;
    }
    
    public void visitAnyByte (ByteI b)
    {
      canFormat_ = (_formatter instanceof DecimalFormat);
    }

    // toString may be used for Chars

    public void visitAnyInt (IntI i)
    {
      canFormat_ = (_formatter instanceof DecimalFormat);
    }

    public void visitAnyShort (ShortI s)
    {
      canFormat_ = (_formatter instanceof DecimalFormat);
    }

    public void visitAnyLong (LongI l)
    {
      canFormat_ = (_formatter instanceof DecimalFormat);
    }

    public void visitAnyFloat (FloatI f)
    {
      canFormat_ = (_formatter instanceof DecimalFormat);
    }

    public void visitAnyDouble (DoubleI d)
    {
      canFormat_ = (_formatter instanceof DecimalFormat);
    }

    // Strings just get copied from the original parsing source
    // (current assumption here is that the source does not contain
    // more than we want, since we don't honour position argument)
    public void visitAnyString (StringI s)
    {
      canFormat_ = true;
    }

    // Likewise for Decimals, which will be formatted with toString()
    public void visitDecimal (Decimal d)
    {
      canFormat_ = true;
    }
    
    // Likewise for AnyObjects, which will be formatted with toString()
    public void visitAnyObject (ObjectI o)
    {
      canFormat_ = true;
    }

    // For unknowns, which will be formatted with toString()
    public void visitUnknown (Any o)
    {
      canFormat_ = true;
    }

    // ...and booleans as well.
    public void visitAnyBoolean (BooleanI b)
    {
      canFormat_ = true;
    }

    public void visitAnyDate (DateI d)
    {
      canFormat_ = (_formatter instanceof DateFormat);
    }

    public void visitMap (Map m)
    {
      canFormat_ = (_formatter instanceof AnyMessageFormat);
    }

    public void visitArray (Array a)
    {
      canFormat_ = (_formatter instanceof ArrayMessageFormat);
    }
  }
}
