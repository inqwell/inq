/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/CSVStream.java $
 * $Author: sanderst $
 * $Revision: 1.6 $
 * $Date: 2011-05-02 20:06:45 $
 */


package com.inqwell.any.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.Format;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFormat;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.Call;
import com.inqwell.any.Catalog;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstString;
import com.inqwell.any.ContainedException;
import com.inqwell.any.Descriptor;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.LocateNode;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;
import com.inqwell.any.Value;
import com.inqwell.any.Vectored;
import com.inqwell.any.client.AnyRenderInfo;
import com.inqwell.any.client.RenderInfo;
import com.inqwell.any.server.BOTDescriptor;

/**
 *
 *
 */

public class CSVStream extends AbstractStream
{
	private static final long serialVersionUID = 1L;

	private transient Csv   parser_;

	private Array protos_;
	private Map   formatters_;
	private Call  filterFunc_;

  // Relating to output
	private transient java.io.PrintStream ps_;
	private int                 cellsWritten_;
	private int                 linesWritten_;

  private boolean autoTable_;
  private boolean scan_;

  private boolean first_ = true;

  private static ByteArrayInputStream dummyStream__;
  private static String               lineSeparator__;
  private static String               doubleQuote__;
  private static String               singleQuote__;
	static private Any                  str__ = new ConstString("line");

  static
  {
    dummyStream__   = new ByteArrayInputStream(new byte[0]);
    lineSeparator__ = System.getProperties().getProperty("line.separator");

    doubleQuote__   = "\"\"";
    singleQuote__   = "\"";
  }

	public CSVStream()
	{
	}

	public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
	{
		boolean ret = makeStreams(p, toOpen, mode);

    acceptStreams();

		return ret;
	}

	public void setPrototypes(Array protos)
	{
    protos_ = protos;
  }

  public Array getPrototypes()
  {
    return protos_;
  }

  public int getLineCount()
  {
    createParser();
    return parser_.getLineCount();
  }

  public int getLinesWritten()
  {
    return linesWritten_;
  }

  public void setLineCount(int lineCount)
  {
    throw new AnyRuntimeException("lineCount property is read-only");
  }

  public int getCellCount()
  {
    createParser();
    return parser_.getCellCount();
  }

  public void setCellCount(int cellCount)
  {
    throw new AnyRuntimeException("cellCount property is read-only");
  }

  public Any read () throws AnyException
  {
		Array ret = null;

		if (parser_ != null)
		{
      boolean ignoreBlank = parser_.getIgnoreBlank();

      if (protos_ == null)
        protos_ = AbstractComposite.array();

      do
      {
        ret = parser_.readCSVLine(protos_);
      } while (ret != null && ignoreBlank && ret.entries() == 0);
		}

		// when eof is seen close the stream
		if (ret == null)
      close();

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
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " read(Map,Map)"));
	}

  /**
   * Write one or more items to this stream.  If the given argument
   * is a Vectored then the individual items are written.
   */
	public boolean write (Any outputItem, Transaction t) throws AnyException
  {
		if (scan_)
			writeNodeSets(outputItem);
		else
		{
  		if (outputItem instanceof Vectored)
  		{
  			Iter i = outputItem.createIterator();
  			while (i.hasNext())
  			{
  				writeIt(i.next());
  			}
  		}
  		else
  		{
  			writeIt(outputItem);
  		}
		}
  	return true;
  }

  /**
   * If the argument is null then just write a line terminator.
   */
	public boolean writeln (Any outputItem, Transaction t) throws AnyException
	{
    if (outputItem != null && !AnyNull.isNullInstance(outputItem))
      write (outputItem, t);
		ps_.print(lineSeparator__);
		ps_.flush();
    cellsWritten_ = 0;
    linesWritten_++;
		return true;
	}

  /**
   *
   */
  public boolean write (Map ioKey,
	                      Map outputItem,
                        Transaction t) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " write(Map,Map)"));
	}

  /**
   *
   */
  public boolean delete (Map ioKey,
												 Map outputItem,
                         Transaction t) throws AnyException
  {
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " delete(Map,Map)"));
  }

  /**
   *
   */
	public boolean delete (Map outputItem, Transaction t) throws AnyException
	{
		throw (new UnsupportedOperationException(getClass().toString() +
																						 " delete(Map)"));
	}

  protected boolean doCloseRead()
  {
    first_ = true;
    return true;
  }
  
  protected boolean doCloseWrite()
  {
    first_ = true;
    return true;
  }

  protected void doFlush()
  {
    if (ps_ != null)
      ps_.flush();
  }
  
  /*
  protected void doCopyFrom(Any a)
  {
    if (!(a instanceof CSVStream))
      throw new IllegalArgumentException("Not a CSVStream");

    CSVStream cs = (CSVStream)a;

    this.parser_       = cs.parser_;
    this.protos_       = cs.protos_;
    this.ps_           = cs.ps_;
    this.cellsWritten_ = cs.cellsWritten_;;
    this.autoTable_    = cs.autoTable_;
    this.first_        = cs.first_;

    this.propertyMap_  = null;
  }
*/
  public Object clone() throws CloneNotSupportedException
  {
		CSVStream s = (CSVStream)super.clone();
		s.parser_       = null;
		s.protos_       = null;
		s.first_        = true;
		s.cellsWritten_ = 0;
		s.ps_           = null;
		return s;
  }

  /**
   * Override.  If the key is <code>"properties"</code> then
   * return true.
   */
  public boolean contains (Any key)
  {
    if (properties__.equals(key))
      return true;

    return false;
  }

  public void setAutoTable(boolean autoTable)
  {
    autoTable_ = autoTable;
    if (autoTable_)
    {
      createParser();
      String path = "row.A";
      LocateNode l = new LocateNode(path);
      RenderInfo r = new AnyRenderInfo(l);
      r.setLabel("A");
      r.setData(new AnyString());
      protos_ = AbstractComposite.array();
      protos_.add(r);
      parser_.initAutoGenerated();
    }
  }

  public boolean isAutoTable()
  {
    return autoTable_;
  }

  public void setScanNodeSets(boolean scan)
  {
    scan_ = scan;
  }
  
  public void setIgnoreBlank(boolean ignoreBlank)
  {
    createParser();
    parser_.setIgnoreBlank(ignoreBlank);
  }

  public boolean isIgnoreBlank()
  {
    createParser();
    return parser_.getIgnoreBlank();
  }

  public void setRoot(Map root)
  {
    createParser();
    parser_.setRoot(root);
  }

  public Map getRoot()
  {
    createParser();
    return parser_.getRoot();
  }

  public void setProtos(Array protos)
  {
    protos_ = protos;
  }

  public Array getProtos()
  {
    return protos_;
  }
  
  public void setFilter(Any styleFuncF)
  {
    if (!first_)
      throw new IllegalStateException("Cannot set filter function on an already open stream");
      
    Call filterFunc = AbstractFunc.verifyCall(styleFuncF, true);
    filterFunc_     = filterFunc;
  }

  public void setFirstLineHeaders(boolean firstLineHeaders)
  {
    createParser();
    parser_.setFirstLineHeaders(firstLineHeaders);
  }

  public boolean isFirstLineHeaders()
  {
    createParser();
    return parser_.isFirstLineHeaders();
  }

  public Array getHeaders()
  {
    return parser_.getHeaders();
  }

  protected void acceptStreams() throws AnyException
  {
    if (istream_ != null)
    {
      createParser();
      if (protos_ == null)
        protos_ = AbstractComposite.array();
    }

    try
    {
			if (ostream_ != null)
				ps_ = new java.io.PrintStream(ostream_);
      
      linesWritten_ = 0;
      cellsWritten_ = 0;
    }
		catch(Exception e)
		{
			throw new ContainedException(e);
		}
  }

	private void writeIt(Any outputItem) throws AnyException
	{
    if (protos_ == null ||
        (protos_.get(cellsWritten_) instanceof Value))
    {
      if (outputItem != null)
        writeCell(outputItem.toString());
      else
        writeCell("");
    }
    else
    {
      // Assume protos_ element is a RenderInfo and use it
      // to retrieve and format the output item
      RenderInfo r = (RenderInfo)protos_.get(cellsWritten_);

      Any cell = r.resolveDataNode(outputItem, false);

      // May be we are only using the formatting data of the RenderInfo
      // and the caller has undertaken the iteration, in which case the
      // node resolution may well fail.  Just take the given item in
      // this case and hope for the best.
      if (cell == null)
        cell = outputItem;


      Format f = r.getFormat(cell);

      if (f != null)
        writeCell(f.format(cell));
      else
        writeCell((outputItem != null) ? cell.toString() : "");
    }
	}

	private void writeCell(String cell)
	{
    try
    {
      if (cellsWritten_ != 0)
        ps_.print(",");

      if (cell.indexOf('"') >= 0)
      {
        ps_.print(doubleQuote__);
        ps_.print(cell);
        ps_.print(doubleQuote__);
      }
      else if (cell.indexOf(',') >= 0)
      {
        ps_.print(singleQuote__);
        ps_.print(cell);
        ps_.print(singleQuote__);
      }
      else
        ps_.print(cell);

      cellsWritten_++;
    }
    catch(Exception e)
    {
      throw new RuntimeContainedException(e);
    }
  }
	
	private void writeNodeSets(Any root)
	{
		// Scan the root for node sets. When a node set is found
		// write out the contents of its primary typedef.
		if (!(root instanceof Map))
			throw new AnyRuntimeException("root is not a map");
		
		Composite c = (Composite)root;
		searchNodeSet(c);
	}
	
	private void searchNodeSet(Composite c)
	{
		Any ns;
		if ((ns = c.getNodeSet()) != null)
		{
			// Found one - write it out (if it's a map)
			if (c instanceof Map)
				writeNodeSet((Map)c, ns);
		}
		else
		{
			Iter i = c.createIterator();
			while (i.hasNext())
			{
				Any child = i.next();
				
				if (child instanceof Map)
				{
					// Don't descend into type instances
					Map m = (Map)child;
					if (m.getDescriptor() != Descriptor.degenerateDescriptor__)
						continue;
				}
				
				if (child instanceof Composite)
					searchNodeSet((Composite)child);
			}
		}
	}
	
	private void writeNodeSet(Map m, Any ns)
	{
		Descriptor d = null;
		
		if (ns instanceof StringI)
		{
			// May be the NS information is a typedef reference. They often are.
			d = findType(ns);
		}
		
		if (d == null || (!(d instanceof BOTDescriptor)))
			return;
		
		Iter i = m.createIterator();
		while (i.hasNext())
		{
			Any child = i.next();
			writeNSChild(child, (BOTDescriptor)d);
		}
		formatters_ = null;
		ps_.print(lineSeparator__);
		ps_.flush();

	}
	
	private void writeNSChild(Any c, BOTDescriptor d)
	{
		if (!(c instanceof Map))
			return;
		
		Map m = (Map)c;
		
		// Children of node sets are maps containing a primary type
		// indicated by the given Descriptor and any others
		// aggregated in. Because we are simple CSV at the moment
		// just write out the primary children.
		
		// Fetch the instance child
		Map ic =(Map) m.get(d.getName());
		
		// Make the formatters
		if (formatters_ == null)
		{
			formatters_ = AbstractComposite.simpleMap();
			Iter i = d.getFieldOrder().createIterator();
			while (i.hasNext())
			{
				Any fName = i.next();
				Any val = ic.get(fName);
				AnyFormat fmt = AnyFormat.makeFormat(val, d.getFormat(fName));
				fmt.setGroupingUsed(false);
				formatters_.add(fName, fmt);
				
				// Using this as the first line condition, write the headers
  			writeCell(fName.toString());
			}
			ps_.print(lineSeparator__);
			ps_.flush();
			cellsWritten_ = 0;
		}
		
		writeInstance(ic, d);
	}
	
	private void writeInstance(Map m, BOTDescriptor d)
	{
		Iter i = d.getFieldOrder().createIterator();
		while (i.hasNext())
		{
			Any fName = i.next();
			Any val   = m.get(fName);
			AnyFormat fmt = (AnyFormat)formatters_.get(fName);
			writeCell(fmt.format(val));
		}

		ps_.print(lineSeparator__);
		ps_.flush();
		cellsWritten_ = 0;
	}

	Descriptor findType(Any fqName)
	{
    LocateNode ln = new LocateNode(fqName);
    try
    {
      Descriptor d = (Descriptor) EvalExpr.evalFunc(Transaction.NULL_TRANSACTION,
          Catalog.instance().getCatalog(), ln, Descriptor.class);

      return d;
    }
    catch (AnyException e)
    {
      throw new RuntimeContainedException(e);
    }
	}
	
  private void createParser()
  {
    if (parser_ == null)
      parser_ = new Csv(dummyStream__);

    if (istream_ != null && first_)
    {
      // Set the current stream into the parser. If we have a filterFunc
      // then put the filtering reader in the way.
      if (filterFunc_ == null)
        parser_.ReInit(istream_);
      else
        parser_.ReInit(new FilterReader(istream_));
        
      parser_.initCSV();
      if (autoTable_)
        parser_.initAutoGenerated();
      first_ = false;
    }
  }
  
  // Buffers a line of text and applies the current regex to each
  // line read from the underlying reader before returning its
  // contents via the various read methods
  private class FilterReader extends Reader
  {
    private String            s_ = "";
    private AnyString         as_;
    private int               idx_;
    private BufferedReader    r_;
    private Map               fnArgs_;
    
    private FilterReader(Reader r)
    {
      r_  = new BufferedReader(r);
      as_ = new AnyString();
    }
    
    private FilterReader(InputStream is)
    {
      r_  = new BufferedReader(new InputStreamReader(is));
      as_ = new AnyString();
    }
    
    public int read(char[] cbuf,
                    int    off,
                    int    len) throws IOException
    {
      if (s_ == null)
        return -1;
        
      int read = 0;
      while(len-- > 0)
      {
        if (idx_ == s_.length())
        {
          fetchLine();
          if (s_ == null)
            return read == 0 ? -1 : read; // underlying eof reached
        }
        cbuf[off+read++] = s_.charAt(idx_++);
      }
      return read;
    }
    
    public void close() throws IOException
    {
      r_.close();
    }
    
    private void fetchLine() throws IOException
    {
      idx_ = 0;
      s_ = r_.readLine();
      if (s_ != null)
      {
        // Call the filter function
        as_.setValue(s_);
        Any ret = callFilterFunc(as_);
        s_ = ret.toString() + lineSeparator__;
      }
    }

    private Any callFilterFunc(Any line)
    {
      Any ret = null;
  
      // Use the magical way to get the process & transaction
      Process p = Globals.getProcessForThread(Thread.currentThread());
      Transaction t = p.getTransaction();
  
      try
      {
        if (fnArgs_ == null)
        {
          fnArgs_ = AbstractComposite.simpleMap();
        }
  
        if (line != null)
        {
          fnArgs_.add(str__, line);
        }
  
        filterFunc_.setArgs(fnArgs_);
        filterFunc_.setTransaction(t);
        ret = filterFunc_.exec(p.getContext());
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
      finally
      {
        filterFunc_.setArgs(null);
        fnArgs_.empty();
        filterFunc_.setTransaction(null);
      }
  
      if (AnyNull.isNullInstance(ret))
        return null;
  
      return ret;
    }
  }
}
