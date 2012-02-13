/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/FixedFieldStream.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-05-02 20:06:45 $
 */
 

package com.inqwell.any.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFormat;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.Array;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstString;
import com.inqwell.any.ContainedException;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Transaction;
import com.inqwell.any.Vectored;

/**
 * A fixed field width character stream.  This stream supports the
 * mapping of fields whose position and length within a record, along
 * with the data type is specified as part of the stream's
 * configuration.
 * <p>
 * A stream can be optionally line-based, in which case records
 * are delimited by the line terminator, which itself is discarded.
 * This is the default but can be configured via
 * the <code>lineBased</code> property.
 * </p>
 * <p>
 * Each field is described by a map containing the following:
 * <ul>
 * <li><b>start</b>: An <code>integer</code> that defines the
 * zero-based starting position of the field data.</li>
 * <li><b>length</b>: An <code>integer</code> that defines the
 * length of the field data.</li>
 * <li><b>name</b>: An <code>any</code> that names this
 * field for use by <code>writeln()</code>.</li>
 * <li><b>type</b>: An <code>any</code> from which instances will
 * be cloned as fields are extracted from the source.</li>
 * <li><b>pattern</b>: A <code>string</code> which will be used
 * as the format pattern when parsing/formatting the data
 * duirng input/output.</li>
 * </ul>
 * </p>
 * <p>
 * Fields do not have to be contiguous and when writing to a stream
 * whose field configurations are sparse the <code>fillChar</code>
 * property is used.  By default the record length is defined
 * by the length of the field with the greatest <code>start</code>
 * offset. However if the <code>recordLength</code> property is
 * set and this is longer then the <code>fillChar</code> is used
 * to pad the record.
 * <p>
 * The <code>write(Any)</code> method will accept primitives or
 * composites. In the latter case the children are written in the
 * order they are yielded by the composite's iterator. As item(s)
 * get written so the stream's internal state keeps track of the
 * next field definition to be used.  If the stream is line-based
 * then a line terminator is written out when the last field
 * definition is processed.
 * </p>
 * <p>
 * The <code>writeln()</code> method is supported.  It must be
 * supplied with a <code>Map</code> whose named children will be
 * extracted according to the <code>name</code> of each field
 * definition. The map must, therefore, contain sufficient
 * children and of the appropriate names to satisfy the stream
 * configuration or an exception will be thrown.  Any elements
 * that are not named by the field set are ignored.  If the stream
 * is line-based then a line terminator is also written out.
 * </p>
 * <p>
 * The <code>read()</code> method attempts to read a record of
 * data from the underlying source and creates a <code>smap</code>
 * according to the field list containing the results.  If EOF
 * is seen during this process then <code>null</code> is returned.
 * If parsing fails at any point then an exception is thrown.
 * </p>
 * <p>
 * The <code>read(Map ioKey, Map outputProto)</code> is
 * implemented - the children of outputProto are initialised
 * with the record data whose field names it contains. 
 */
public class FixedFieldStream extends    AbstractStream
															implements PhysicalIO,
                                         Cloneable
{
	private static final long serialVersionUID = 1L;

	// Config properties
  private Vectored fields_;
  private boolean  isLineBased_;
  private char     fillChar_ = ' ';
  private int      recordLength_;
  
  private transient BufferedReader reader_;
  private transient BufferedWriter writer_;
  
  private int            fieldIndex_;

  static private Any start__   = new ConstString("start");
  static private Any length__  = new ConstString("length");
  static private Any type__    = new ConstString("type");
  static private Any name__    = new ConstString("name");
  static private Any pattern__ = new ConstString("pattern");
  
  public FixedFieldStream()
  {
    isLineBased_ = true;
  }
  
	public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
	{
		boolean ret = makeStreams(p, toOpen, mode);
    
    acceptStreams();
		
    fieldIndex_ = 0;
    
		return ret;
	}

  /**
   * Read the next record from the stream.  Attempts to read a
   * whole record and applies the field definitions to produce
   * a map of values. 
   */
  public Any read () throws AnyException
  {
		Any ret = null;
		
		if (reader_ != null)
		{
      Map m = AbstractComposite.simpleMap();
      ret = readRecord(m, true);
    }

  	return ret;
  }

  /**
   * 
   */
  public int read (Map ioKey,
                   Map       outputProto,
                   Array     outputComposite,
                   int       maxCount) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " read(Map,Map,Array)"));
  }

  /**
   * 
   */
  public Map read (Map ioKey,
									 Map outputProto) throws AnyException
	{
    Map ret = null;
    
		if (reader_ != null)
		{
      ret = readRecord(outputProto, false);
    }
    
    return ret;
	}



  /**
   * 
   */
	public boolean write (Any outputItem, Transaction t) throws AnyException
  {
    if (outputItem instanceof Composite)
    {
      Iter i = outputItem.createIterator();
      while (i.hasNext())
      {
        Any a = i.next();
        writeField(a);
      }
    }
    else
    {
      writeField(outputItem);
    }
      
  	return true;
  }

  /**
   * 
   */
  public boolean write (Map ioKey,
	                      Map outputItem,
                        Transaction t) throws AnyException
	{
		return false;
	}
	                      
	
	public boolean writeln (Any outputItem, Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException("writeln " + getClass());
	}
	
  /**
   * 
   */
  public boolean delete (Map ioKey,
												 Map outputItem,
                         Transaction t) throws AnyException
  {
  	return false;
  }                     
	
  /**
   *
   */
	public boolean delete (Map outputItem, Transaction t) throws AnyException
	{
		return false;
	}
	
  protected boolean doCloseRead()
  {
    try
    {
      if (reader_ != null)
        reader_.close();
    }
    catch(Exception e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      reader_ = null;
    }
    return true;
  }
  
  protected boolean doCloseWrite()
  {
    try
    {
      if (writer_ != null)
        writer_.close();
    }
    catch(Exception e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      writer_ = null;
    }
    return true;
  }

  protected void doFlush()
  {
    try
    {
      if (writer_ != null)
        writer_.flush();
    }
    catch(IOException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
//  public void setStreams(InputStream is, OutputStream os) throws AnyException
//  {
//    //istream_ = is;
//    //ostream_ = os;
//  }
  
  /**
   * Set an explicit record length. The supplied value overrides
   * the implicit length established by the field set and may be
   * used to ensure trailing padding is produces by write
   * operations.
   * <p>
   * When reading records, the amount of data in the stream must
   * be sufficient to satisfy the record length or EOF is assumed
   * and <code>null</code>returned.
   */
  public void setRecordLength(int recordLength)
  {
    recordLength_ = recordLength;
  }
  
  public void setFields(Vectored fields)
  {
    if (fields.entries() == 0)
    {
      fields_ = null;
      return;
    }
    
    if (fields_ == null)
      fields_ = (Vectored)AbstractComposite.array();
    else
      fields_.empty();
    
    for (int i = 0; i < fields.entries(); i++)
    {
      Map m = (Map)fields.getByVector(i);
      validateMap(m);
      
      Map m1 = AbstractComposite.simpleMap();
      fields_.add(m1.copyFrom(m));
      
      setupFormatter(m1);
    }
  }
  
  public void setFillChar(char fillChar)
  {
    fillChar_ = fillChar;
  }
  
/*
  protected void doCopyFrom(Any a)
  {
    if (!(a instanceof FixedFieldStream))
      throw new IllegalArgumentException("Not a FixedFieldStream");
    
    FixedFieldStream fs = (FixedFieldStream)a;
    
    this.fields_       = fs.fields_;
    this.isLineBased_  = fs.isLineBased_;
    this.fillChar_     = fs.fillChar_;
    this.recordLength_ = fs.recordLength_;;
    this.reader_       = fs.reader_;
    this.writer_       = fs.writer_;
    this.fieldIndex_   = fs.fieldIndex_;

    this.propertyMap_  = null;
  }
*/  
  protected void acceptStreams() throws AnyException
  {
		try
		{
			if (ostream_ != null)
				writer_ = new BufferedWriter(new OutputStreamWriter(ostream_));
      
      if (istream_ != null)
      {
        reader_ = new BufferedReader(new InputStreamReader(istream_));
      }
		}
		catch(Exception e)
		{
			throw new ContainedException(e);
		}
  }
  
  public void setAuxInfo (Any a) {}

  public Object clone() throws CloneNotSupportedException
  {
    FixedFieldStream f = (FixedFieldStream)super.clone();

    f.reader_       = null;
    f.writer_       = null;
    f.fieldIndex_   = 0;

		return this;
  }
  
  private void writeField(Any a) throws AnyException
  {
    Map         fieldDefn = (Map)fields_.getByVector(fieldIndex_);
    AnyFormat   f         = (AnyFormat)fieldDefn.get(pattern__);
    
    String s = f.format(a);
    
    // The formatter has been set up with the field width,
    // padding character and justification so it will
    // generate the correct field output
    try
    {
      writer_.write(s, 0, s.length());
      fieldIndex_ = ++fieldIndex_ % fields_.entries();
    
      // Check for sparse fill
      if (fieldIndex_ != 0)
      {
        Map nextField = (Map)fields_.getByVector(fieldIndex_);
        
        int fill = getStart(nextField) - getStart(fieldDefn)
                                       - getLength(fieldDefn);
        
        if (fill != 0)
        {
          for (int i = 0; i < fill; i++)
            writer_.write(fillChar_);
        }
      }
      
      // Check for line termination; flush stream per record.
      if (fieldIndex_ == 0)
      {
        if (isLineBased_)
          writer_.newLine();
        
        writer_.flush();
      }
    }
    catch (IOException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  private Map readRecord(Map m, boolean make) throws AnyException
  {
    Map ret = null;
    try
    {
      if (isLineBased_)
      {
        String s = reader_.readLine();
        if (s != null && s.length() >= recordLength_)
        {
          ret = processLine(s.toCharArray(), m, make);
        }
      }
      else
      {
        char[] c = new char[recordLength_];
        int bytesRead = reader_.read(c, 0, recordLength_);
        
        // Check for EOF in our terms
        if (bytesRead == recordLength_)
        {
          ret = processLine(c, m, make);
        }
      }
    }
    catch (IOException iox)
    {
      throw new ContainedException(iox);
    }
    return ret;
  }
  
  private Map processLine(char[] c, Map m, boolean make)
  {
    // Extract one line of input into fields
    for (int i = 0; i < fields_.entries(); i++)
    {
      Map fieldDefn = (Map)fields_.getByVector(i);
      Any fieldAny = null;
      
      // If the given map contains an entry by the current field
      // name then use it directly (it's an outputProto)
      // else pass null and the field type definition will be
      // used and we enter it in the map by that name.
      if (m.contains(fieldDefn.get(name__)))
      {
        fieldAny = m.get(fieldDefn.get(name__));
        processField(c, fieldDefn, fieldAny);
      }
      else
      {
        // If we are not making the field on the fly from the
        // definition prototype then skip the field (because
        // the output proto supplied by the caller doesn't contain
        // this field.
        if (make)
          m.add(fieldDefn.get(name__),
                processField(c, fieldDefn, fieldAny));
      }
    }
    return m;
  }
  
  private Any processField(char[] c, Map fieldDefn, Any fieldAny)
  {
    int start   = getStart(fieldDefn);
    int len     = getLength(fieldDefn);
    Any any     = (fieldAny == null) ? getType(fieldDefn).cloneAny()
                                     : fieldAny;
    AnyFormat f = getPattern(fieldDefn);
    
    // Strip any trailing spaces now rather than creating a new
    // string.  If the field is numeric then there likely won't
    // be any.
    int spc = start + len - 1;
    while (c[spc] == ' ')
      spc--;
    len = spc - start + 1;
    
    String s = new String(c, start, len);
    f.parseAny(s, any, false);
    return any;
  }
  
  private int getStart(Map fieldDefn)
  {
    IntI i = (IntI)fieldDefn.get(start__);
    return i.getValue();
  }
  
  private int getLength(Map fieldDefn)
  {
    IntI i = (IntI)fieldDefn.get(length__);
    return i.getValue();
  }
  
  private Any getType(Map fieldDefn)
  {
    return fieldDefn.get(type__);
  }
  
  private AnyFormat getPattern(Map fieldDefn)
  {
    return (AnyFormat)fieldDefn.get(pattern__);
  }
  
  private void validateMap(Map m)
  {
    if (!m.contains(name__))
      throw new AnyRuntimeException("Field definition does not specify name");

    if (!m.contains(type__))
      throw new AnyRuntimeException("Field definition does not specify type");

    if (!m.contains(start__))
      throw new AnyRuntimeException("Field definition does not specify start");

    if (!m.contains(length__))
      throw new AnyRuntimeException("Field definition does not specify length");
    
    // If there is no pattern a default formatter fot the type
    // will be used.
  }
  
  private void setupFormatter(Map m)
  {
    // replace the pattern with a formatter or create default if none.
    
    AnyFormat f = null;
    
    if (m.contains(pattern__))
    {
      f = AnyFormat.makeFormat(m.get(type__),
                               m.get(pattern__).toString());
    }
    else
    {
      f = AnyFormat.makeFormat(m.get(type__));
    }
    
    // Override any field width implied by the format pattern (in case
    // justification was used) with the specified length.  Means that
    // the pattern does not have to be tediously set up with the
    // right number of >>>'s since we know the length explicitly.
    // f.setFieldWidth(getLength(m));
    
    m.replaceItem(pattern__, f);
  }
}
