/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A simple text formatter for log messages. Its principle improvements over
 * {@link java.util.logging.SimpleFormatter} are that it
 * <ul>
 * <li>formats log times to
 * the millisecond and outputs one message per line, instead of splitting
 * the time and message over two lines)
 * </li>
 * <li>
 * includes the thread name
 * </li>
 * 
 * @author tom
 *
 */
public class SimpleFormatter extends Formatter
{
  private Date       dat        = new Date();

  private DateFormat formatter_ = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");

  private String lineSeparator_ = System.getProperty("line.separator");
  
  /**
   * Format the given LogRecord.
   * 
   * @param record
   *          the log record to be formatted.
   * @return a formatted log record
   */
  public synchronized String format(LogRecord record)
  {
    StringBuffer sb = new StringBuffer("[");

    dat.setTime(record.getMillis());
    
    sb.append(formatter_.format(dat));
    
    sb.append(" ");
    sb.append(record.getLevel().getLocalizedName());
    sb.append("] ");

    String message = formatMessage(record);
    sb.append(message);

    sb.append(" (");
    if (record.getSourceClassName() != null)
    {
      sb.append(record.getSourceClassName());
    }
    else
    {
      sb.append(record.getLoggerName());
    }
    
    if (record.getSourceMethodName() != null)
    {
      sb.append(" ");
      sb.append(record.getSourceMethodName());
    }
    
    if (record.getThrown() != null)
    {
      try
      {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        record.getThrown().printStackTrace(pw);
        pw.close();
        sb.append(sw.toString());
      }
      catch (Exception ex)
      {
      }
    }

    sb.append(" in thread \"");
    sb.append(Thread.currentThread().getName());
    sb.append("\")");
    
    sb.append(lineSeparator_);
    return sb.toString();
  }
}
