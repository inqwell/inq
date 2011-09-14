/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.8 $
 * $Date: 2011-05-02 20:06:45 $
 */
 

package com.inqwell.any.io;

import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.AbstractVisitor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ByteI;
import com.inqwell.any.Catalog;
import com.inqwell.any.CharI;
import com.inqwell.any.Composite;
import com.inqwell.any.ContainedException;
import com.inqwell.any.DateI;
import com.inqwell.any.Decimal;
import com.inqwell.any.Descriptor;
import com.inqwell.any.DoubleI;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.FloatI;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.LocateNode;
import com.inqwell.any.LongI;
import com.inqwell.any.Map;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.ShortI;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;
import com.inqwell.any.beans.ClassMap;
import com.inqwell.any.util.Util;

/**
 * Perform IO to the underlying stream in Inq "serialised form" XMLformat
 * <p>
 * 
 */

public class XMLStream extends AbstractStream
{
  private static final long serialVersionUID = 1L;

  private transient java.io.PrintWriter pw_;
  
  // A record of the vmids of what has been written so far. If the
  // structure is cyclic then second and subsequent encounters are
  // written as <ref vmid="xxx"/> tags. When reading back, 
  private transient Set                 written_;
  
  // What's been read from the source. The parser consumes
  // everything but the xml format supports structures written
  // by distinct calls to write() will be read back as such.
  private transient Array               read_;
  private transient int                 readCount_;
  
  private transient Writer              writer_;
  
  private static ClassMap writeReplacements_;
  
  // Special ctor args
  private static Class[] long__;
  private static Class[] string__;
  private static Class[] boolean__;
  private static Class[] stringScale__;
  
  private static String TAG_WRITE       = "write";
  private static String TAG_ANY         = "any";
  private static String TAG_MAP         = "map";
  private static String TAG_KEY         = "key";
  private static String TAG_VALUE       = "value";
  private static String TAG_SET         = "set";
  private static String TAG_ARRAY       = "array";
  private static String TAG_UNIQUEKEY   = "unique-key";
  private static String TAG_NODESET     = "node-set";
  private static String TAG_REF         = "ref";
  private static String TAG_NULL        = "null";
  
  private static String ATTR_VMID       = "vmid";
  private static String ATTR_TYPE       = "type";
  private static String ATTR_DESCR      = "typedef";
  private static String ATTR_SCALE      = "scale";
  
  static
  {
    long__    = new Class[1];
    long__[0] = long.class;

    string__    = new Class[1];
    string__[0] = String.class;
    
    boolean__    = new Class[1];
    boolean__[0] = boolean.class;

    stringScale__ = new Class[2];
    stringScale__[0] = String.class;
    stringScale__[1] = int.class;
  }
  
  public XMLStream()
  {
  }
    
  public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
  {
    boolean ret = makeStreams(p, toOpen, mode);
    
    if (ret)
    {
      try
      {
        acceptStreams();
      }
      catch(Exception e)
      {
        // Allow open to fail not with an exception but
        // by returning false. If this happens then also close
        // the streams. By this time they are made but the
        // xml parsing has barfed.
  			System.out.println ("open(): Got an exception : " + e.toString());
      	e.printStackTrace();
        close();
        ret = false;
      }
    }
    
    return ret;
  }

  public Any read () throws AnyException
  {
    Any ret = null;
    
    if (read_ != null)
    {
      if (readCount_ < read_.entries())
        ret = read_.get(readCount_++);
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
    throw (new UnsupportedOperationException());
  }

  /**
   * 
   */
  public Map read (Map ioKey,
                   Map outputProto) throws AnyException
  {
    throw (new UnsupportedOperationException());
  }

  /**
   * 
   */
  public boolean write (Any outputItem, Transaction t) throws AnyException
  {
    writeIt(outputItem);
    return true;
  }

  public boolean writeln (Any outputItem, Transaction t) throws AnyException
  {
    throw (new UnsupportedOperationException());
  }
  
  /**
   * 
   */
  public boolean write (Map ioKey,
                        Map outputItem,
                        Transaction t) throws AnyException
  {
    throw (new UnsupportedOperationException());
  }
                        
  /**
   * 
   */
  public boolean delete (Map ioKey,
                         Map outputItem,
                         Transaction t) throws AnyException
  {
    throw (new UnsupportedOperationException());
  }                     
  
  /**
   *
   */
  public boolean delete (Map outputItem, Transaction t) throws AnyException
  {
    throw (new UnsupportedOperationException());
  }
  
  protected boolean doCloseRead()
  {
    readCount_ = 0;
    read_      = null;
    return true;
  }
  
  protected boolean doCloseWrite()
  {
    try
    {
      if (pw_ != null)
      {
        closeDocumentRoot();
        pw_.close();
      }
    }
    catch(Exception e)
    {
      throw new RuntimeContainedException(e);
    }
    finally
    {
      pw_      = null;
      written_ = null;
    }
    return true;
  }

  protected void doFlush()
  {
    if (pw_ != null)
      pw_.flush();
  }
  
  public Object clone() throws CloneNotSupportedException
  {
    XMLStream s = (XMLStream)super.clone();
    
    s.pw_        = null;
    s.readCount_ = 0;
    s.read_      = null;
    s.written_   = null;
    s.writer_    = null;
    
    return s;
  }

  protected void acceptStreams() throws AnyException
  {
    try
    {
      if (ostream_ != null)
      {
        pw_ = new java.io.PrintWriter(ostream_);
        writeXMLHeader();
        written_ = AbstractComposite.set(10);
        writer_ = new Writer();
        openDocumentRoot();
      }
      
      if (istream_ != null)
      {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();

        XMLReader xmlReader = saxParser.getXMLReader();
        DocHandler dh = new DocHandler();
        xmlReader.setContentHandler(dh);
        xmlReader.parse(new InputSource(new InputStreamReader(istream_)));
        read_ = dh.items_;
        readCount_ = 0;
      }
    }
    catch(Exception e)
    {
      throw new ContainedException(e);
    }
  }
  
  private void writeIt(Any outputItem) throws AnyException
  {
    openWriteItem();
    
    writer_.write(outputItem);
    
    closeWriteItem();
  }
  
  private void writeXMLHeader()
  {
    // May be some of the attribute values here need to be specified
    // via properties...
    if (pw_ != null)
      pw_.print("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
    else
      notOpenedWrite();
  }
  
  private void openDocumentRoot()
  {
    // Consider namespace requirements sometime
    pw_.print("<root>");
  }
  
  private void closeDocumentRoot()
  {
    pw_.print("</root>");
  }
  
  private void openWriteItem()
  {
    pw_.print("<write>");
  }

  private void closeWriteItem()
  {
    written_.empty();
    pw_.print("</write>");
  }

  private class Writer extends AbstractVisitor
  {
    private IntI  i_ = new AnyInt();
    
    // The current descriptor, if any
    private Descriptor d_;
    
    public void visitAnyBoolean (BooleanI b)
    {
      openAny(b);

      pw_.print(b.toString());
      
      closeAny();
    }

    public void visitAnyByte (ByteI b)
    {
      openAny(b);

      pw_.print(b.toString());
      
      closeAny();
    }

    public void visitAnyChar (CharI c)
    {
      openAny(c);

      pw_.print(c.toString());
      
      closeAny();
    }

    public void visitAnyInt (IntI i)
    {
      openAny(i);

      pw_.print(i.toString());
      
      closeAny();
    }

    public void visitAnyShort (ShortI s)
    {
      openAny(s);

      pw_.print(s.toString());
      
      closeAny();
    }

    public void visitAnyLong (LongI l)
    {
      openAny(l);

      pw_.print(l.toString());
      
      closeAny();
    }

    public void visitAnyFloat (FloatI f)
    {
      openAny(f);

      pw_.print(f.toString());
      
      closeAny();
    }

    public void visitAnyDouble (DoubleI d)
    {
      openAny(d);

      pw_.print(d.toString());
      
      closeAny();
    }

    public void visitDecimal (Decimal d)
    {
      openAny(d, d.scale());

      pw_.print(d.toString());
      
      closeAny();
    }

    public void visitAnyString (StringI s)
    {
      openAny(s);

      pw_.print(s.toString());
      
      closeAny();
    }

    public void visitAnyDate (DateI d)
    {
      openAny(d);

      pw_.print(d.getTime());
      
      closeAny();
    }

    public void visitMap (Map m)
    {
      openMap(m);
      
      // Traverse Map content
      Iter i = m.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        openKey();
        write(k);
        closeKey();
        
        Any v = m.get(k);
        
        openValue();
        write(v);
        closeValue();
      }
      
      closeMap();
    }

    public void visitArray (Array a)
    {
      openArray(a);
      
      // Traverse Array content
      Iter i = a.createIterator();
      while (i.hasNext())
      {
        Any v = i.next();
        write(v);
      }
      
      closeArray();
    }

    public void visitSet (Set s)
    {
      openSet(s);
      
      // Traverse Array content
      Iter i = s.createIterator();
      while (i.hasNext())
      {
        Any v = i.next();
        write(v);
      }
      
      closeSet();
    }
    
    
    public void visitUnknown(Any o)
    {
      if (AnyNull.isNullInstance(o))
      {
        openAny(o);
        closeAny();
      }
      else
      {
      	// Assume string representation will do
      	openAny(o);
        pw_.print(o.toString());
        closeAny();
      }
    }

    // Write the given any to the stream, or if already seen then write
    // a reference entry.
    private void write(Any a)
    {
      if (a == null)
        nullTag();
      else
      {
      	i_.setValue(System.identityHashCode(a));
      	
    		// Have we already written the object?
    		// Note: Do this Before any replacing since that creates new objects and we 
      	// don't want to write out the same object more then once
      	if (written_.contains(i_))
      	{
      		refTag(i_);
      	}
      	else
      	{
      		// We need to write it. Cater for multiple replace operations.
      		Any replaced = a;
	        StreamFunc f = (StreamFunc)Globals.xmlStreamOutputReplacements__.get(replaced.getClass());
      		while (f != null)
      		{
		        replaced = f.exec(replaced, null);
		      	// Check to see if what we've replaced, needs replacing!
		        f = (StreamFunc)Globals.xmlStreamOutputReplacements__.get(replaced.getClass());
      		}
		     
      		// write the replacement but make sure we use the original refTag id. 
          written_.add(i_.cloneAny());          
          replaced.accept(this);
        }
      }
    }
    
    private void openAny(Any a)
    {
      openAny(a, -1);
    }

    private void openAny(Any a, int scale)
    {
      pw_.print("<any ");
      typeAttr(a);
      
      if (scale >= 0)
      {
        pw_.print("scale=\"");
        pw_.print(scale);
        pw_.print("\" ");
      }
      
      vmIdAttr(a);        // Must be last attr
    }
    
    private void openMap(Map m)
    {
      pw_.print("<map ");
      typeAttr(m);
      descrAttr(m);
      
      vmIdAttr(m);        // Must be last attr
      
      // Write out std content for a Map
      if (m.getUniqueKey() != null)
      {
        openUniqueKey();
        write(m.getUniqueKey());
        closeUniqueKey();
      }
      
      if (m.getNodeSet() != null)
      {
        openNodeSet();
        write(m.getNodeSet());
        closeNodeSet();
      }
    }

    private void openArray(Array a)
    {
      pw_.print("<array ");
      typeAttr(a);

      vmIdAttr(a);        // Must be last attr
    }
    
    private void openSet(Set s)
    {
      pw_.print("<set ");
      typeAttr(s);

      vmIdAttr(s);        // Must be last attr
    }
    
    private void openKey()
    {
      pw_.print("<key>");
    }

    private void openValue()
    {
      pw_.print("<value>");
    }

    private void openUniqueKey()
    {
      pw_.print("<unique-key>");
    }

    private void openNodeSet()
    {
      pw_.print("<node-set>");
    }

    private void typeAttr(Any a)
    {
      pw_.print("type=\"");
      pw_.print(a.getClass().getName());
      pw_.print("\" ");
    }

    private void descrAttr(Map m)
    {
      Descriptor d = m.getDescriptor();
      
      if (d != Descriptor.degenerateDescriptor__)
      {
        pw_.print("typedef=\"");
        pw_.print(d.toString());
        pw_.print("\" ");
      }
    }

    private void vmIdAttr(Any a)
    {
      // Should be the last attribute written as finishes the
      // open <whatever...> tag 
      pw_.print("vmid=\"");
      pw_.print(i_);
      pw_.print("\">");
    }

    private void closeAny()
    {
      pw_.print("</any>");
    }

    private void closeMap()
    {
      pw_.print("</map>");
    }
    
    private void closeArray()
    {
      pw_.print("</array>");
    }
    
    private void closeSet()
    {
      pw_.print("</set>");
    }
    
    private void closeKey()
    {
      pw_.print("</key>");
    }

    private void closeValue()
    {
      pw_.print("</value>");
    }

    private void closeUniqueKey()
    {
      pw_.print("</unique-key>");
    }

    private void closeNodeSet()
    {
      pw_.print("</node-set>");
    }

    private void refTag(IntI i)
    {
      pw_.print("<ref vmid=\"");
      pw_.print(i.getValue());
      pw_.print("\"/>");
    }
    
    private void nullTag()
    {
      // Represents java null, not Inq null
      pw_.print("<null/>");
    }
  }
  
  private class DocHandler extends DefaultHandler
  {
    // Working character buffer
    private StringBuffer chars_ = new StringBuffer();
    
    // The items we have parsed
    private Array        items_ = AbstractComposite.array();
    
    // Character content
    private StringI      content_ = new AnyString();
    
    // The stack of collection instances (maps, sets and arrays)
    // pushed each time we descend into a new level
    private java.util.Stack  collectionStack_ = new java.util.Stack();

    // For some tags we don't process the attrubutes until endElement
    // is called. For those, the attributes presented on startElement
    // are held here.
    private java.util.Stack  attrStack_ = new java.util.Stack();
    
    // 
    private java.util.Stack  keyStack_ = new java.util.Stack();
    
    // Only one of these is non-null at any given time and it
    // represents the current collection we are accumulating
    private Map   map_    = null;
    private Array array_  = null;
    private Set   set_    = null;
    
    // Map support
    //private Any          uniqueKey_;
    //private Any          nodeSet_;
    private Any          key_;
    //private Any          value_;
    
    // The thing we've just finished creating
    private Any          a_;
    
    // Support for <ref> tags
    private IntI         i_    = new AnyInt();
    private Map          read_ = AbstractComposite.simpleMap();
    
    public void characters(char[] ch, int start, int length) throws SAXException
    {
      chars_.append(ch, start, length);
    }

    public void endDocument() throws SAXException
    {
      chars_     = null;
    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
//      System.out.println(qName);
//      System.out.println(localName);
      
      HashMap attr = popAttrs();
      
      if (qName.equals(TAG_MAP))
      {
        // Check if there is a typedef attribute
        String typedef = getAttrVal(attr, ATTR_DESCR);
        if (typedef != null)
        {
          Descriptor d = null;
          
          LocateNode ln = new LocateNode(typedef);
          try
          {
            d = (Descriptor)EvalExpr.evalFunc(Transaction.NULL_TRANSACTION,
                Catalog.instance().getCatalog(),
                ln,
                Descriptor.class);
            
            if (d != null)
              map_.setDescriptor(d);
          }
          catch(AnyException e)
          {
            throw new RuntimeContainedException(new InvalidObjectException
                (e.getMessage()));
          }
        }
        
        a_ = map_;
        popCollection();
      }
      else if (qName.equals(TAG_ARRAY))
      {
        a_ = array_;
        popCollection();
      }
      else if (qName.equals(TAG_SET))
      {
        a_ = set_;
        popCollection();
      }
      else if (qName.equals(TAG_ANY))
      {
        content_.setValue(chars_.toString());
        a_ = makeAny(attr, content_);
      }
      else if (qName.equals(TAG_NULL))
      {
        a_ = null;
        
        // Handle non-map collection of null here because it is
        // ambiguous elsewhere
        if (set_ != null)
          set_.add(null);
        else if (array_ != null)
          array_.add(null);
      }
      else if (qName.equals(TAG_KEY))
      {
        key_ = a_;
        a_ = null;
      }
      else if (qName.equals(TAG_VALUE))
      {
        if (map_ == null)
          throw new IllegalStateException("No current map");
        map_.add(key_, a_);
        
        //key_   = null;
        popKey();
        a_     = null;
      }
      else if (qName.equals(TAG_UNIQUEKEY))
      {
        if (map_ == null)
          throw new IllegalStateException("No current map");
        map_.setUniqueKey(a_);
        a_ = null;
      }
      else if (qName.equals(TAG_NODESET))
      {
        if (map_ == null)
          throw new IllegalStateException("No current map");
        map_.setNodeSet(a_);
        a_ = null;
      }
      else if (qName.equals(TAG_WRITE))
      {
        // The end of a write operation when the document
        // was produced.
        items_.add(a_);
        a_ = null;
      }

      // All content is contained within matching open/close elements
      // and represents the value of the current scalar, if any. We
      // can throw it away now.
      chars_.delete(0, chars_.length());
      
      // Maps have the key/value tags to assist in knowing when
      // to add an entry. If there's a set or an array (mutually
      // exclusive with map and if document is correct) then add
      // current any to it.
      if (set_ != null && a_ != null)
        set_.add(a_);
      else if (array_ != null && a_ != null)
        array_.add(a_);

      if (!qName.equals(TAG_REF) && a_ != null)
      {
        String vmid = getAttrVal(attr, ATTR_VMID);
        if (vmid != null)
        {
          i_.setValue(Integer.parseInt(vmid));
          read_.add(i_.cloneAny(), a_);
        }
      }
    }

    public void endPrefixMapping(String prefix) throws SAXException
    {
      super.endPrefixMapping(prefix);
    }

    public void error(SAXParseException e) throws SAXException
    {
      super.error(e);
    }

    public void fatalError(SAXParseException e) throws SAXException
    {
      super.fatalError(e);
    }

    public void startDocument() throws SAXException
    {
      // no-op
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
      // Push the attributes
      pushAttrs(attributes);
      
      String className = attributes.getValue(ATTR_TYPE);
      
      if (qName.equals(TAG_MAP))
      {
        newMap(className);
      }
      else if (qName.equals(TAG_ARRAY))
      {
        newArray(className);
      }
      else if (qName.equals(TAG_SET))
      {
        newSet(className);
      }
      else if (qName.equals(TAG_KEY))
      {
        pushKey();
      }
      else if (qName.equals(TAG_REF))
      {
        i_.fromString(attributes.getValue(ATTR_VMID));
        a_ = read_.get(i_);  // croaks if not found - should always be OK
      }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
      super.startPrefixMapping(prefix, uri);
    }

    public void warning(SAXParseException e) throws SAXException
    {
      super.warning(e);
    }
    
    private void newMap(String className)
    {
      map_ = (Map)newCollection(className);
    }
    
    private void newArray(String className)
    {
      array_ = (Array)newCollection(className);
    }
    
    private void newSet(String className)
    {
      set_ = (Set)newCollection(className);
    }
    
    private Composite newCollection(String className)
    {
      try
      {
        pushCollection();
        Composite c = (Composite)Class.forName(className).newInstance();
        return c;
      }
      catch(ClassNotFoundException e)
      {
        throw new RuntimeContainedException(e);
      }
      catch(IllegalAccessException e)
      {
        throw new RuntimeContainedException(e);
      }
      catch(InstantiationException e)
      {
        throw new RuntimeContainedException(e);
      }
    }
    
    private Any meh(HashMap attr, StringI content)
    {
      Any ret = null;
      
      try
      {
        String className = getAttrVal(attr, ATTR_TYPE);
        // Special handling for dates - they are stored as long values
        // and there is no support for these as strings
        if (className.endsWith("Date"))
        {
          long l = Long.parseLong(content.toString());
          Constructor c = Class.forName(className).getConstructor(long__);
          Object[] ctorArgs = new Object[1];
          ctorArgs[0] = new Long(l);
          ret = (Any)c.newInstance(ctorArgs);
        }
        // Special handling for booleans. String conversion in Inq
        // does not regard "true" and "false" as such. Anything non-zero/null
        // is true.
        else if (className.endsWith("Boolean"))
        {
          boolean b = Boolean.parseBoolean(content.toString());
          Constructor c = Class.forName(className).getConstructor(boolean__);
          Object[] ctorArgs = new Object[1];
          ctorArgs[0] = new Boolean(b);
          ret = (Any)c.newInstance(ctorArgs);
        }
        else if (className.endsWith("AnyNull"))
        {
          ret = AnyNull.instance();
        }
        else
        {
          // All other values provide a String constructor. Check for
          // scale attr
          String strScale = getAttrVal(attr, ATTR_SCALE);
          Object[] ctorArgs;
          Class[] ctorSig;
          if (strScale == null)
          {
            ctorArgs = new Object[1];
            ctorSig = string__;
          }
          else
          {
            ctorArgs = new Object[2];
            ctorSig = stringScale__;
            ctorArgs[1] = new Integer(Integer.parseInt(strScale));
          }
          
          Constructor c = Class.forName(className).getConstructor(ctorSig);
          ctorArgs[0] = content.toString();
          
          Any xmlObj = null;
          xmlObj = (Any)c.newInstance(ctorArgs);

          // Consider object resolution
          StreamFunc f = (StreamFunc)Globals.xmlStreamInputReplacements__.get(xmlObj.getClass());
          
          // If there's no func then just hope its OK as is.
          if (f != null)
          {
          	ret = f.exec(xmlObj, null);
      			//System.out.println ("Resolved : " + xmlObj.getClass());
      			//System.out.println ("With : " + ret.getClass());
          }
          else
          {
          	ret = xmlObj;
          }
        }
      }
      catch(Exception e)
      {
        throw new RuntimeContainedException(e);
      }
      
      if (ret.isConst())
      {
        ret = AbstractValue.flyweightConst(ret);
      }
      
      return ret;
    }
    
    private Any makeAny(HashMap attr, StringI content)
    {
      String className = getAttrVal(attr, ATTR_TYPE);
      String strScale  = getAttrVal(attr, ATTR_SCALE);
      Any ret = Util.makeAny(className, content.toString(), strScale);
      if (className.endsWith("Date") ||
          className.endsWith("Boolean") ||
          className.endsWith("AnyNull"))
        return ret;
      
      // Any other class is subject to possible substitution (not
      // other scalar types really but anything else, yes)
      StreamFunc f = (StreamFunc)Globals.xmlStreamInputReplacements__.get(ret.getClass());
      
      // If there's no func then just hope its OK as is.
      if (f != null)
      {
        ret = f.exec(ret, null);
        //System.out.println ("Resolved : " + xmlObj.getClass());
        //System.out.println ("With : " + ret.getClass());
      }
      
      return ret;
    }

    private void pushCollection()
    {
      if (map_ != null)
      {
        collectionStack_.push(map_);
        map_ = null;
      }
      else if (array_ != null)
      {
        collectionStack_.push(array_);
        array_ = null;
      }
      else if (set_ != null)
      {
        collectionStack_.push(set_);
        set_ = null;
      }
    }
    
    private void popCollection()
    {
      if (!collectionStack_.empty())
      {
        Object collection = collectionStack_.pop();
        if (collection instanceof Map)
        {
          map_ = (Map)collection;
          array_ = null;
          set_ = null;
        }
        else if (collection instanceof Array)
        {
          array_ = (Array)collection;
          map_ = null;
          set_ = null;
        }
        else if (collection instanceof Set)
        {
          set_ = (Set)collection;
          array_ = null;
          map_ = null;
        }
      }
      else
      {
        map_   = null;
        array_ = null;
        set_   = null;
      }
    }
    
    private void pushAttrs(Attributes attrs)
    {
      // Sax doesn't say anything about the Attributes collection
      // not being reused between tags so we have to take a copy.
      // No shallow/deep copy is supported so we roll our own
      int len = attrs.getLength();
      HashMap m = null;
      if (len > 0)
      {
        m = new HashMap(len);
        for (int i = 0; i < len; i++)
        {
          String qName = attrs.getQName(i);
          String val   = attrs.getValue(qName);
          m.put(qName, val);
        }
      }
      attrStack_.push(m);
    }

    private HashMap popAttrs()
    {
      HashMap attr = null;
      
      if (!attrStack_.empty())
        attr = (HashMap)attrStack_.pop();
      
      return attr;
    }
    
    private void pushKey()
    {
      keyStack_.push(key_);
    }
    
    private void popKey()
    {
      key_ = (Any)keyStack_.pop();
    }
    
    private String getAttrVal(HashMap attrs, String key)
    {
      String ret = null;
      if (attrs != null)
      {
        Object o = attrs.get(key);
        if (o != null)
          ret = o.toString();
      }
      
      return ret;
    }
  }

}
