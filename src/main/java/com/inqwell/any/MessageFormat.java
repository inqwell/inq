/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * @(#)MessageFormat.java 1.30 98/05/20
 *
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * Portions copyright (c) 1996-1998 Sun Microsystems, Inc.
 * All Rights Reserved.
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies. Please refer to the file "copyright.html"
 * for further important copyright and licensing information.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */

package com.inqwell.any;

import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.text.DecimalFormat;
//import java.text.Utility;
import java.text.Format;
import java.text.ParsePosition;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ChoiceFormat;
import java.text.ParseException;
/**
 * <code>MessageFormat</code> provides a means to produce concatenated
 * messages in language-neutral way. Use this to construct messages
 * displayed for end users.
 *
 * <p>
 * <code>MessageFormat</code> takes a set of objects, formats them, then
 * inserts the formatted strings into the pattern at the appropriate places.
 *
 * <p>
 * <strong>Note:</strong>
 * <code>MessageFormat</code> differs from the other <code>Format</code>
 * classes in that you create a <code>MessageFormat</code> object with one
 * of its constructors (not with a <code>getInstance</code> style factory
 * method). The factory methods aren't necessary because <code>MessageFormat</code>
 * doesn't require any complex setup for a given locale. In fact,
 * <code>MessageFormat</code> doesn't implement any locale specific behavior
 * at all. It just needs to be set up on a sentence by sentence basis.
 *
 * <p>
 * Here are some examples of usage:
 * <blockquote>
 * <pre>
 * Object[] arguments = {
 *     new Integer(7),
 *     new Date(System.currentTimeMillis()),
 *     "a disturbance in the Force"
 * };
 *
 * String result = MessageFormat.format(
 *     "At {1,time} on {1,date}, there was {2} on planet {0,number,integer}.",
 *     arguments);
 *
 * <em>output</em>: At 12:30 PM on Jul 3, 2053, there was a disturbance
 *           in the Force on planet 7.
 *
 * </pre>
 * </blockquote>
 * Typically, the message format will come from resources, and the
 * arguments will be dynamically set at runtime.
 *
 * <p>
 * Example 2:
 * <blockquote>
 * <pre>
 * Object[] testArgs = {new Long(3), "MyDisk"};
 *
 * MessageFormat form = new MessageFormat(
 *     "The disk \"{1}\" contains {0} file(s).");
 *
 * System.out.println(form.format(testArgs));
 *
 * // output, with different testArgs
 * <em>output</em>: The disk "MyDisk" contains 0 file(s).
 * <em>output</em>: The disk "MyDisk" contains 1 file(s).
 * <em>output</em>: The disk "MyDisk" contains 1,273 file(s).
 * </pre>
 * </blockquote>
 *
 * <p>
 * The pattern is of the form:
 * <blockquote>
 * <pre>
 * messageFormatPattern := string ( "{" messageFormatElement "}" string )*
 *
 * messageFormatElement := argument { "," elementFormat }
 *
 * elementFormat := "time" { "," datetimeStyle }
 *                | "date" { "," datetimeStyle }
 *                | "number" { "," numberStyle }
 *                | "choice" { "," choiceStyle }
 *
 * datetimeStyle := "short"
 *                  | "medium"
 *                  | "long"
 *                  | "full"
 *                  | dateFormatPattern
 *
 * numberStyle := "currency"
 *               | "percent"
 *               | "integer"
 *               | numberFormatPattern
 *
 * choiceStyle := choiceFormatPattern
 * </pre>
 * </blockquote>
 * If there is no <code>elementFormat</code>,
 * then the argument must be a string, which is substituted. If there is
 * no <code>dateTimeStyle</code> or <code>numberStyle</code>, then the
 * default format is used (for example, <code>NumberFormat.getInstance</code>,
 * <code>DateFormat.getTimeInstance</code>, or <code>DateFormat.getInstance</code>).
 *
 * <p>
 * In strings, single quotes can be used to quote the "{"
 * (curly brace) if necessary. A real single quote is represented by ''.
 * Inside a <code>messageFormatElement</code>, quotes are <strong>not</strong>
 * removed. For example, {1,number,$'#',##} will produce a number format
 * with the pound-sign quoted, with a result such as: "$#31,45".
 *
 * <p>
 * If a pattern is used, then unquoted braces in the pattern, if any, must match:
 * that is, "ab {0} de" and "ab '}' de" are ok, but "ab {0'}' de" and "ab } de" are
 * not.
 *
 * <p>
 * The argument is a number from 0 to 9, which corresponds to the
 * arguments presented in an array to be formatted.
 *
 * <p>
 * It is ok to have unused arguments in the array.
 * With missing arguments or arguments that are not of the right class for
 * the specified format, a <code>ParseException</code> is thrown.
 * First, <code>format</code> checks to see if a <code>Format</code> object has been
 * specified for the argument with the <code>setFormats</code> method.
 * If so, then <code>format</code> uses that <code>Format</code> object to format the
 * argument. Otherwise, the argument is formatted based on the object's
 * type. If the argument is a <code>Number</code>, then <code>format</code>
 * uses <code>NumberFormat.getInstance</code> to format the argument; if the
 * argument is a <code>Date</code>, then <code>format</code> uses
 * <code>DateFormat.getDateTimeInstance</code> to format the argument.
 * Otherwise, it uses the <code>toString</code> method.
 *
 * <p>
 * For more sophisticated patterns, you can use a <code>ChoiceFormat</code> to get
 * output such as:
 * <blockquote>
 * <pre>
 * MessageFormat form = new MessageFormat("The disk \"{1}\" contains {0}.");
 * double[] filelimits = {0,1,2};
 * String[] filepart = {"no files","one file","{0,number} files"};
 * ChoiceFormat fileform = new ChoiceFormat(filelimits, filepart);
 * form.setFormat(1,fileform); // NOT zero, see below
 *
 * Object[] testArgs = {new Long(12373), "MyDisk"};
 *
 * System.out.println(form.format(testArgs));
 *
 * // output, with different testArgs
 * output: The disk "MyDisk" contains no files.
 * output: The disk "MyDisk" contains one file.
 * output: The disk "MyDisk" contains 1,273 files.
 * </pre>
 * </blockquote>
 * You can either do this programmatically, as in the above example,
 * or by using a pattern (see
 * {@link ChoiceFormat}
 * for more information) as in:
 * <blockquote>
 * <pre>
 * form.applyPattern(
 *    "There {0,choice,0#are no files|1#is one file|1#are {0,number,integer} files}.");
 * </pre>
 * </blockquote>
 * <p>
 * <strong>Note:</strong> As we see above, the string produced
 * by a <code>ChoiceFormat</code> in <code>MessageFormat</code> is treated specially;
 * occurances of '{' are used to indicated subformats, and cause recursion.
 * If you create both a <code>MessageFormat</code> and <code>ChoiceFormat</code>
 * programmatically (instead of using the string patterns), then be careful not to
 * produce a format that recurses on itself, which will cause an infinite loop.
 * <p>
 * <strong>Note:</strong> formats are numbered by order of
 * variable in the string.
 * This is <strong>not</strong> the same as the argument numbering!
 * For example: with "abc{2}def{3}ghi{0}...",
 * <ul>
 * <li>format0 affects the first variable {2}
 * <li>format1 affects the second variable {3}
 * <li>format2 affects the second variable {0}
 * <li>and so on.
 * </ul>
 * <p>
 * When a single argument is parsed more than once in the string, the last match
 * will be the final result of the parsing.  For example,
 * <pre>
 * MessageFormat mf = new MessageFormat("{0,number,#.##}, {0,number,#.#}");
 * Object[] objs = {new Double(3.1415)};
 * String result = mf.format( objs );
 * // result now equals "3.14, 3.1"
 * objs = null;
 * objs = mf.parse(result, new ParsePosition(0));
 * // objs now equals {new Double(3.1)}
 * </pre>
 * <p>
 * Likewise, parsing with a MessageFormat object using patterns containing
 * multiple occurances of the same argument would return the last match.  For
 * example,
 * <pre>
 * MessageFormat mf = new MessageFormat("{0}, {0}, {0}");
 * String forParsing = "x, y, z";
 * Object[] objs = mf.parse(forParsing, new ParsePosition(0));
 * // result now equals {new String("z")}
 * </pre>
 * <p>
 * You can use <code>setLocale</code> followed by <code>applyPattern</code>
 * (and then possibly <code>setFormat</code>) to re-initialize a
 * <code>MessageFormat</code> with a different locale.
 *
 * @see          java.util.Locale
 * @see          Format
 * @see          NumberFormat
 * @see          DecimalFormat
 * @see          ChoiceFormat
 * @version      1.15 29 Jan 1997
 * @author       Mark Davis
 */

public class MessageFormat extends Format {
    /**
     * Constructs with the specified pattern.
     * @see MessageFormat#applyPattern
     */
    public MessageFormat(String pattern) {
        applyPattern(pattern);
    }

    /**
     * Constructs with the specified pattern and formats for the
     * arguments in that pattern.
     */
    public void setLocale(Locale theLocale) {
        locale = theLocale;
    }

    /**
     * Gets the locale. This locale is used for fetching default number or date
     * format information.
     */
    public Locale getLocale() {
        return locale;
    }


    /**
     * Sets the pattern. See the class description.
     */

    public void applyPattern(String newPattern) {
						formatters.clear();
						argPositions.clear();
						argumentTokens.clear();
            StringBuffer[] segments = new StringBuffer[4];
            for (int i = 0; i < segments.length; ++i) {
                segments[i] = new StringBuffer();
            }
            int part = 0;
            int formatNumber = 0;
            boolean inQuote = false;
            int braceStack = 0;
            // maxOffset = -1;
            for (int i = 0; i < newPattern.length(); ++i) {
                char ch = newPattern.charAt(i);
                if (part == 0) {
                    if (ch == '\'') {
                        if (i + 1 < newPattern.length()
                            && newPattern.charAt(i+1) == '\'') {
                            segments[part].append(ch);  // handle doubles
                            ++i;
                        } else {
                            inQuote = !inQuote;
                        }
                    } else if (ch == '{' && !inQuote) {
                        part = 1;
                    } else {
                        segments[part].append(ch);
                    }
                } else  if (inQuote) {              // just copy quotes in parts
                    segments[part].append(ch);
                    if (ch == '\'') {
                        inQuote = false;
                    }
                } else {
                    switch (ch) {
                    case ',':
                        if (part < 3)
                            part += 1;
                        else
                            segments[part].append(ch);
                        break;
                    case '{':
                        ++braceStack;
                        segments[part].append(ch);
                        break;
                    case '}':
                        if (braceStack == 0) {
                            part = 0;
                            // When we see the closing brace various bits of the
                            // format pattern are in segments.
                            // i contains the number of bits of formatting info
                            // and formatNumber is the number of format strings we
                            // have come across so far...
                            makeFormat(i, formatNumber, segments);
                            formatNumber++;
                        } else {
                            --braceStack;
                            segments[part].append(ch);
                        }
                        break;
                    case '\'':
                        inQuote = true;
                        // fall through, so we keep quotes in other parts
                    default:
                        segments[part].append(ch);
                        break;
                    }
                }
            }
            if (braceStack == 0 && part != 0) {
                // maxOffset = -1;
                throw new IllegalArgumentException("Unmatched braces in the pattern.");
            }
            pattern = segments[0].toString();
    }


    /**
     * Gets the pattern. See the class description.
     */

    public String toPattern() {
        // later, make this more extensible
        int lastOffset = 0;
        StringBuffer result = new StringBuffer();
        for (int i = 0; i <= argPositions.size(); ++i) {
            copyAndFixQuotes(pattern, lastOffset, intAt(argPositions, i),result);
            lastOffset = intAt(argPositions, i);
            result.append('{');
            result.append(stringAt (argumentTokens,i));  // the object selector strings l to r
            Format f = (Format)formatters.get(i);
            if (f == null) {
                // do nothing, string format
            } else if (f instanceof DecimalFormat) {
                if (f.equals(NumberFormat.getInstance(locale))) {
                    result.append(",number");
                } else if (f.equals(NumberFormat.getCurrencyInstance(locale))) {
                    result.append(",number,currency");
                } else if (f.equals(NumberFormat.getPercentInstance(locale))) {
                    result.append(",number,percent");
                } else if (f.equals(getIntegerFormat(locale))) {
                    result.append(",number,integer");
                } else {
                    result.append(",number," +
                                  ((DecimalFormat)f).toPattern());
                }
            } else if (f instanceof SimpleDateFormat) {
                if (f.equals(DateFormat.getDateInstance(DateFormat.DEFAULT,locale))) {
                    result.append(",date");
                } else if (f.equals(DateFormat.getDateInstance(DateFormat.SHORT,locale))) {
                    result.append(",date,short");
                } else if (f.equals(DateFormat.getDateInstance(DateFormat.DEFAULT,locale))) {
                    result.append(",date,medium");
                } else if (f.equals(DateFormat.getDateInstance(DateFormat.LONG,locale))) {
                    result.append(",date,long");
                } else if (f.equals(DateFormat.getDateInstance(DateFormat.FULL,locale))) {
                    result.append(",date,full");
                } else if (f.equals(DateFormat.getTimeInstance(DateFormat.DEFAULT,locale))) {
                    result.append(",time");
                } else if (f.equals(DateFormat.getTimeInstance(DateFormat.SHORT,locale))) {
                    result.append(",time,short");
                } else if (f.equals(DateFormat.getTimeInstance(DateFormat.DEFAULT,locale))) {
                    result.append(",time,medium");
                } else if (f.equals(DateFormat.getTimeInstance(DateFormat.LONG,locale))) {
                    result.append(",time,long");
                } else if (f.equals(DateFormat.getTimeInstance(DateFormat.FULL,locale))) {
                    result.append(",time,full");
                } else {
                    result.append(",date,"
                                  + ((SimpleDateFormat)f).toPattern());
                }
            } else if (f instanceof ChoiceFormat) {
                result.append(",choice,"
                              + ((ChoiceFormat)f).toPattern());
            } else {
                //result.append(", unknown");
            }
            result.append('}');
        }
        copyAndFixQuotes(pattern, lastOffset, pattern.length(), result);
        return result.toString();
    }

    /**
     * Sets formats to use on parameters.
     * See the class description about format numbering.
     */
    public void setFormats(Format[] newFormats) {
      // Keep this interface even though the formatters are not held as
      // an array internally any more.
      formatters.clear();
      for (int i = 0; i < newFormats.length; i++)
        formatters.add (newFormats[i]);
    }

    /**
     * Set a format to be used on a variable in the pattern.
     * @param variable the zero-based number of the variable in the format.
     * This is <em>not</em> the argument number. If <code>variable</code>
     * is out of range, an <code>ArrayIndexOutOfBoundsException</code> is
     * thrown.
     * @param newFormat the format to use for the specified variable
     */
    public void setFormat(int variable, Format newFormat) {
        // No array bounds checking or automatic growing of the array but
        // neither did the original...   Intended to be used once the
        // format array has been established by applyPattern (and then
        // makeFormat) where the array can grow to its natural size.
        formatters.set (variable, newFormat);
        //formats[variable] = newFormat;
    }

    /**
     * Gets formats that were set with setFormats.
     * See the class description about format numbering.
     */
    public Format[] getFormats() {
        // maintain this interface
        Format[] currentFormatters = new Format[formatters.size()];
        return (Format[])formatters.toArray(currentFormatters);
    }

    /**
     * Returns pattern with formatted objects.  If source is null, the
     * original pattern is returned, if source contains null objects, the
     * formatted result will substitute each argument with the string "null".
     * @param source an array of objects to be formatted & substituted.
     * @param result where text is appended.
     * @param ignore no useful status is returned.
     */
    public final StringBuffer format(Map source, StringBuffer result,
                                     FieldPosition ignore)
    {
        return format(source,result,ignore, 0);
    }

    /**
     * Convenience routine.
     * Avoids explicit creation of MessageFormat,
     * but doesn't allow future optimizations.
     */
    public static String format(String pattern, Map arguments) {
            MessageFormat temp = new MessageFormat(pattern);
            return temp.format(arguments);
    }

    // Overrides
    public StringBuffer format(Object source, StringBuffer result,
                                     FieldPosition ignore)
    {
        return format((Map)source,result,ignore, 0);
    }

    /**
     * Parses the string.
     *
     * <p>Caveats: The parse may fail in a number of circumstances.
     * For example:
     * <ul>
     * <li>If one of the arguments does not occur in the pattern.
     * <li>If the format of an argument loses information, such as
     *     with a choice format where a large number formats to "many".
     * <li>Does not yet handle recursion (where
     *     the substituted strings contain {n} references.)
     * <li>Will not always find a match (or the correct match)
     *     if some part of the parse is ambiguous.
     *     For example, if the pattern "{1},{2}" is used with the
     *     string arguments {"a,b", "c"}, it will format as "a,b,c".
     *     When the result is parsed, it will return {"a", "b,c"}.
     * <li>If a single argument is parsed more than once in the string,
     *     then the later parse wins.
     * </ul>
     * When the parse fails, use ParsePosition.getErrorIndex() to find out
     * where in the string did the parsing failed.  The returned error
     * index is the starting offset of the sub-patterns that the string
     * is comparing with.  For example, if the parsing string "AAA {0} BBB"
     * is comparing against the pattern "AAD {0} BBB", the error index is
     * 0. When an error occurs, the call to this method will return null.
     * If the soruce is null, return an empty array.
     */
    public Map parse(String source, ParsePosition status) {
        HashMap resultMap = new HashMap();
        if (source == null) return resultMap;
        int patternOffset = 0;
        int sourceOffset = status.getIndex();
        ParsePosition tempStatus = new ParsePosition(0);
        for (int i = 0; i <= argPositions.size(); ++i) {
            // match up to format
            int len = intAt (argPositions,i) - patternOffset;
            if (len == 0 || pattern.regionMatches(patternOffset,
                                                  source, sourceOffset, len)) {
                sourceOffset += len;
                patternOffset += len;
            } else {
                status.setErrorIndex(sourceOffset);
                return null; // leave index as is to signal error
            }

            // now use format
            Format f = (Format)formatters.get(i);
            if (f == null) {   // string format
                // if at end, use longest possible match
                // otherwise uses first match to intervening string
                // does NOT recursively try all possibilities
                int tempLength = (i != argPositions.size()) ? intAt (argPositions,i) : pattern.length();

                int next;
                if (patternOffset >= tempLength) {
                    next = source.length();
                }else{
                    next = source.indexOf( pattern.substring(patternOffset,tempLength), sourceOffset);
                }

                if (next < 0) {
                    status.setErrorIndex(sourceOffset);
                    return null; // leave index as is to signal error
                } else {
                    String strValue= source.substring(sourceOffset,next);
                    if (!strValue.equals("{"+stringAt(argumentTokens,i)+"}"))
                        resultMap.put (stringAt(argumentTokens,i),
                              source.substring(sourceOffset,next));
                    sourceOffset = next;
                }
            } else {
                tempStatus.setIndex(sourceOffset);
                resultMap.put (stringAt(argumentTokens,i),
                      ((Format)formatters.get(i)).parseObject(source,tempStatus));
                if (tempStatus.getIndex() == sourceOffset) {
                    status.setErrorIndex(sourceOffset);
                    return null; // leave index as is to signal error
                }
                sourceOffset = tempStatus.getIndex(); // update
            }
        }
        int len = pattern.length() - patternOffset;
        if (len == 0 || pattern.regionMatches(patternOffset,
                                              source, sourceOffset, len)) {
            status.setIndex(sourceOffset + len);
        } else {
            status.setErrorIndex(sourceOffset);
            return null; // leave index as is to signal error
        }
        return resultMap;
    }

    /**
     * Parses the string. Does not yet handle recursion (where
     * the substituted strings contain {n} references.)
     * @exception ParseException if the string can't be parsed.
     */
    public Map parse(String source) throws ParseException {
        ParsePosition status  = new ParsePosition(0);
        Map result = parse(source, status);
        if (status.getIndex() == 0)  // unchanged, returned object is null
            throw new ParseException("MessageFormat parse error!", status.getErrorIndex());

        return result;
    }

    /**
     * Parses the string. Does not yet handle recursion (where
     * the substituted strings contain %n references.)
     */
    public Object parseObject (String text, ParsePosition status) {
        return parse(text, status);
    }

    /**
     * Overrides Cloneable
     */
    public Object clone()
    {
        MessageFormat other = (MessageFormat) super.clone();

        // clone arrays. Can't do with utility because of bug in Cloneable
        other.formatters = (ArrayList) formatters.clone(); // shallow clone
        for (int i = 0; i < formatters.size(); ++i) {
            Format f = (Format)formatters.get(i);
            if (f != null)
                other.formatters.set(i, f.clone());
            else
                other.formatters.set(i, null);
        }
        // for primitives or immutables, shallow clone is enough
        // other.offsets = (int[]) offsets.clone();
        // other.argumentNumbers = (int[]) argumentNumbers.clone();
        // others TBD - TS

        return other;
    }

    /**
     * Equality comparision between two message format objects
     */
    public boolean equals(Object obj) {
        if (this == obj)                      // quick check
            return true;
        if (getClass() != obj.getClass())
            return false;
        MessageFormat other = (MessageFormat) obj;
        return (pattern.equals(other.pattern)
            && objectEquals(locale, other.locale)   // does null check
                && argPositions.equals(other.argPositions)
            && argumentTokens.equals(other.argumentTokens)
            && formatters.equals(other.formatters));
    }

    /**
     * Generates a hash code for the message format object.
     */
    public int hashCode() {
        return pattern.hashCode(); // enough for reasonable distribution
    }


    // ===========================privates============================

    private Locale locale = Locale.getDefault();
    private String pattern = "";

    // private Format[] formats = new Format[10];
    private ArrayList formatters = new ArrayList(10);      // format objects l to r

    // private int[] offsets = new int[10];
    private ArrayList argPositions = new ArrayList(10);    // format patterns l to r

    // private int[] argumentNumbers = new int[10];
    private ArrayList argumentTokens = new ArrayList(10);  //Format tokens l to r

    // private int maxOffset = -1;    -- There's no longer a maxOffset!
    // maxOffset was, in fact, not an offset itself but the number of placement
    // patterns found in the format string

    /**
     * Constructs with the specified pattern.
     * @see MessageFormat#applyPattern
     */
    private MessageFormat(String pattern, Locale loc) {
        locale = (Locale)loc.clone();
        applyPattern(pattern);
    }

    /**
     * Internal routine used by format.
     * @param recursionProtection Initially zero. Bits 0..9 are used to indicate
     * that a parameter has already been seen, to avoid recursion.  Currently
     * unused.
     */

    private StringBuffer format(Map arguments, StringBuffer result,
                                FieldPosition status, int recursionProtection) {
        // note: this implementation assumes a fast substring & index.
        // if this is not true, would be better to append chars one by one.
        int lastOffset = 0;
        for (int i = 0; i < argPositions.size(); ++i) {
            result.append(pattern.substring(lastOffset, intAt (argPositions, i)));
            lastOffset = intAt (argPositions, i);
            StringI argumentToken = stringAt (argumentTokens, i);
            if (arguments == null || !arguments.containsKey(argumentToken)) {
                result.append("{" + argumentToken + "}");
                continue;
            }
            // int argRecursion = ((recursionProtection >> (argumentNumber*2)) & 0x3);
            if (false) {
                // prevent loop!!!
                result.append('\uFFFD');
            } else {
                Object obj = arguments.get(argumentToken);
                String arg;
                Format f = (Format)formatters.get(i);
                boolean tryRecursion = false;
                if (obj == null) {
                    arg = "null";
                } else if (f != null) {
                  //System.out.println("FORMATTING " + argumentToken + " value " + obj);
                    arg = f.format(obj);
                    tryRecursion = f instanceof ChoiceFormat;
                } else if (obj instanceof Number) {
                    // format number if can
                    arg = NumberFormat.getInstance(locale).format(obj); // fix
                } else if (obj instanceof Date) {
                    // format a Date if can
                    arg = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                       DateFormat.SHORT,
                                                       locale).format(obj);//fix
                } else if (obj instanceof String) {
                    arg = (String) obj;

                } else {
                    arg = obj.toString();
                    if (arg == null) arg = "null";
                }

                // recurse if necessary
                if (tryRecursion && arg.indexOf('{') >= 0) {
                    MessageFormat temp = new MessageFormat(arg, locale);
                    temp.format(arguments,result,status,recursionProtection);
                } else {
                    result.append(arg);
                }
            }
        }
        result.append(pattern.substring(lastOffset, pattern.length()));
        return result;
    }
    private static final String[] typeList =
    {"", "", "number", "", "date", "", "time", "", "choice", "string"};
    private static final String[] modifierList =
    {"", "", "currency", "", "percent", "", "integer"};
    private static final String[] dateModifierList =
    {"", "", "short", "", "medium", "", "long", "", "full"};

    private void makeFormat(int position, int offsetNumber,
                            StringBuffer[] segments)
    {

        // get the token
        String argumentToken;
        // int oldMaxOffset = maxOffset; -- there's no maxOffset any more
        argumentToken = segments[1].toString();
        // maxOffset = offsetNumber;

        // relies on the fact that makeFormat is called once for each
        // placement token found in the pattern, left to right
        argPositions.add (new Integer(segments[0].length()));
        argumentTokens.add(new ConstString(argumentToken));

        // Now get the format
        Format newFormat = null;
        switch (findKeyword(segments[2].toString(), typeList)) {
        case 0:
            newFormat = new AnyFormat(); // for strings
            break;
        case 1: case 2:// number
            switch (findKeyword(segments[3].toString(), modifierList)) {
            case 0: // default;
                newFormat = NumberFormat.getInstance(locale);
                break;
            case 1: case 2:// currency
                newFormat = NumberFormat.getCurrencyInstance(locale);
                break;
            case 3: case 4:// percent
                newFormat = NumberFormat.getPercentInstance(locale);
                break;
            case 5: case 6:// integer
                newFormat = getIntegerFormat(locale);
                break;
            default: // pattern
                newFormat = NumberFormat.getInstance(locale);
                try {
                    ((DecimalFormat)newFormat).applyPattern(segments[3].toString());
                } catch (Exception e) {
                    // maxOffset = oldMaxOffset; -- we just don't add the
                    // format object to the array
                    throw new IllegalArgumentException(
                                             "Pattern incorrect or locale does not support formats, error at ");
                }
                break;
            }
            break;
        case 3: case 4: // date
            switch (findKeyword(segments[3].toString(), dateModifierList)) {
            case 0: // default
                newFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
                break;
            case 1: case 2: // short
                newFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
                break;
            case 3: case 4: // medium
                newFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
                break;
            case 5: case 6: // long
                newFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
                break;
            case 7: case 8: // full
                newFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
                break;
            default:
                newFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
                try {
                    ((SimpleDateFormat)newFormat).applyPattern(segments[3].toString());
                } catch (Exception e) {
                    // maxOffset = oldMaxOffset;
                    throw new IllegalArgumentException(
                                             "Pattern incorrect or locale does not support formats, error at ");
                }
                break;
            }
            break;
        case 5: case 6:// time
            switch (findKeyword(segments[3].toString(), dateModifierList)) {
            case 0: // default
                newFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
                break;
            case 1: case 2: // short
                newFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
                break;
            case 3: case 4: // medium
                newFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
                break;
            case 5: case 6: // long
                newFormat = DateFormat.getTimeInstance(DateFormat.LONG, locale);
                break;
            case 7: case 8: // full
                newFormat = DateFormat.getTimeInstance(DateFormat.FULL, locale);
                break;
            default:
                newFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
                try {
                    ((SimpleDateFormat)newFormat).applyPattern(segments[3].toString());
                } catch (Exception e) {
                    // maxOffset = oldMaxOffset;
                    throw new IllegalArgumentException(
                                             "Pattern incorrect or locale does not support formats, error at ");
                }
                break;
            }
            break;
        case 7: case 8:// choice
            try {
                newFormat = new ChoiceFormat(segments[3].toString());
            } catch (Exception e) {
                // maxOffset = oldMaxOffset;
                throw new IllegalArgumentException(
                                         "Choice Pattern incorrect, error at ");
            }
        case 9:// Tom's dodginess for SQL % wildcarding
            newFormat = AnyFormat.makeFormat(AnyString.EMPTY, "{x}" + segments[3].toString()); // for strings
            break;
        default:
            // maxOffset = oldMaxOffset;
            throw new IllegalArgumentException("unknown format type at ");
        }
        // if we get here we may have a format object or we may have null
        // whatever we have we add it to the array of formatters
        // formats[offsetNumber] = newFormat;
        segments[1].setLength(0);   // throw away other segments
        segments[2].setLength(0);
        segments[3].setLength(0);
        formatters.add (newFormat);
    }

    private static final int findKeyword(String s, String[] list) {
        s = s.trim().toLowerCase();
        for (int i = 0; i < list.length; ++i) {
            if (s.equals(list[i]))
                return i;
        }
        return -1;
    }

    /**
     * Convenience method that ought to be in NumberFormat
     */
    NumberFormat getIntegerFormat(Locale locale) {
        NumberFormat temp = NumberFormat.getInstance(locale);
        if (temp instanceof DecimalFormat) {
            DecimalFormat temp2 = (DecimalFormat) temp;
            temp2.setMaximumFractionDigits(0);
            temp2.setDecimalSeparatorAlwaysShown(false);
            temp2.setParseIntegerOnly(true);
        }
        return temp;
    }

    private static final void copyAndFixQuotes(
                                               String source, int start, int end, StringBuffer target) {
        for (int i = start; i < end; ++i) {
            char ch = source.charAt(i);
            if (ch == '{') {
                target.append("'{'");
            } else if (ch == '}') {
                target.append("'}'");
            } else if (ch == '\'') {
                target.append("''");
            } else {
                target.append(ch);
            }
        }
    }

    private static final int intAt (ArrayList a, int pos)
    {
      return ((Integer)a.get(pos)).intValue();
    }

    private static final StringI stringAt (ArrayList a, int pos)
    {
      return (StringI)a.get(pos);
    }

    final static boolean objectEquals(Object source, Object target) {
    if (source == null)
            return (target == null);
    else
            return source.equals(target);
    }
}
