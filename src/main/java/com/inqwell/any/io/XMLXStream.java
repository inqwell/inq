/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.22 $
 * $Date: 2011-05-02 20:06:45 $
 */
 

package com.inqwell.any.io;

import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.AbstractVisitor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyArray;
import com.inqwell.any.AnyBoolean;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyFormat;
import com.inqwell.any.AnyFuncHolder;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyNull;
import com.inqwell.any.AnyOrderedMap;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.BooleanI;
import com.inqwell.any.ByteI;
import com.inqwell.any.Call;
import com.inqwell.any.Catalog;
import com.inqwell.any.CharI;
import com.inqwell.any.Composite;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstString;
import com.inqwell.any.ContainedException;
import com.inqwell.any.DateI;
import com.inqwell.any.Decimal;
import com.inqwell.any.Descriptor;
import com.inqwell.any.DoubleI;
import com.inqwell.any.EvalExpr;
import com.inqwell.any.FloatI;
import com.inqwell.any.Func;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.KeyDef;
import com.inqwell.any.LocateNode;
import com.inqwell.any.LongI;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.ObjectI;
import com.inqwell.any.Orderable;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Set;
import com.inqwell.any.ShortI;
import com.inqwell.any.Stack;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;
import com.inqwell.any.Value;
import com.inqwell.any.Vectored;
import com.inqwell.any.Visitor;
import com.inqwell.any.util.Util;
import com.inqwell.json.Handler;

/**
 * Perform IO to the underlying stream in Inq Rich XML format
 * <p>
 * 
 */

public class XMLXStream extends AbstractStream
{
  private static final long serialVersionUID = 1L;

  private transient AbstractProducer writer_;
  
  // For writing
  private PrintWriter pw_;
  
  // For reading
  private DocHandler dh_;
  private Map        seed_;
  
  private String  rootName_        = ROOT;
  private String  prologue_;
  private String  metaName_        = META_NAME;
  private Call    metaFunc_;
  private Any     nscName_         = NODE_SET_CHILD;
  private boolean finaliseOnWrite_ = true;
  private boolean formatOutput_    = true;
  private Map     tagNames_;
  private Map     tagFuncs_;
  private Map     formatters_;
  private Set     includes_;
  private Set     excludes_;
  private Set     excludesBelow_;
  private Set     synthExcludes_;
  private Set     cData_;
  private boolean writeMeta_;
  private boolean inqAttributes_;
  private boolean preserveTypes_;
  private boolean enumExt_;
  private boolean groupingUsed_ = true;
  
  // The DOM result if we were in DOM mode
  private Document root_;
  
  // Current child for content if we are in DOM mode
  private Element  child_;
  
  // Formatters we have made so far
  private Map     formatCache_;
  
  private static String ROOT = "root";
  private static Any    NODE_SET_CHILD = AbstractValue.flyweightString("child");
  private static Any    DATA           = AbstractValue.flyweightString("data");
  private static String META_NAME = "INQmetadata";
  
  // Constants for tag function arguments
  public static final Any PARENT      = AbstractValue.flyweightString("parent");
  public static final Any NODE        = AbstractValue.flyweightString("node");
  public static final Any NODE_NAME   = AbstractValue.flyweightString("nodeName");
  public static final Any ORDINAL     = AbstractValue.flyweightString("ordinal");
  public static final Any CONTENT     = AbstractValue.flyweightString("content");
  public static final Any LAST        = AbstractValue.flyweightString("last");
  public static final Any DESCEND     = AbstractValue.flyweightString("descend");
  public static final Any ATTRIBUTES  = AbstractValue.flyweightString("attributes");

  public static final Any CLASS       = AbstractValue.flyweightString("class");
  public static final Any SCALE       = AbstractValue.flyweightString("scale");
  
  public static final Any TYPEDEF_ARG = AbstractValue.flyweightString("Typedef");
  public static final Any FIELD       = AbstractValue.flyweightString("Field");
  public static final Any WIDTH       = AbstractValue.flyweightString("Width");
  public static final Any LABEL       = AbstractValue.flyweightString("Label");

  private static final String MAP_TYPE = "maptype";
  private static final String TYPEDEF  = "typedef";
  
  
  // Args for call tag/metatag function
//  private Map args_;
//  private Map metaArgs_;
  
  // Arguments to any tag function we may call
  private StringI  nodeName_ = new AnyString();
  private IntI     ordinal_  = new AnyInt();
  private StringI  content_  = new AnyString();
  private BooleanI last_     = new AnyBoolean();
  private BooleanI descend_  = new AnyBoolean(true);
  private Map      attrs_    = AbstractComposite.orderedMap();

  
  
  static private void validateMap(Map m, Class key, Class value)
  {
    validateMap(m, key, value, null);
  }

  // Validate a map to determine that all keys and values
  // are type compatible with the given arguments.
  static private void validateMap(Map m, Class key, Class value, Class otherValue)
  {
    Iter i = m.keys().createIterator();
    
    while (i.hasNext())
    {
      Any k = i.next();
      
      if (k == null)
        throw new IllegalArgumentException("null key is not allowed");
      
      if (!key.isAssignableFrom(k.getClass()))
        throw new IllegalArgumentException(key.toString() +
                                           " not assignable from " +
                                           k.getClass());
      
      Any v = m.get(k);
      
      if (v == null)
        throw new IllegalArgumentException("null value is not allowed");
      
      if (!value.isAssignableFrom(v.getClass()))
      {
        if (otherValue == null || (!otherValue.isAssignableFrom(v.getClass())))
          throw new IllegalArgumentException(value.toString() +
                                             " not assignable from " +
                                             v.getClass());
        
      }
      
      if (v instanceof AnyFuncHolder.FuncHolder)
      {
        v = AbstractFunc.verifyCall(v, true);
        m.replaceItem(k, v);
      }
    }
  }
  
  static private Set validateSet(Set s, Class c)
  {
    Iter i = s.createIterator();
    while (i.hasNext())
    {
      Any a = i.next();

      if (a instanceof ObjectI)
        a = (Any)((ObjectI)a).getValue();
      
      if (!c.isAssignableFrom(a.getClass()))
        throw new IllegalArgumentException(c.toString() +
                                           " not assignable from " +
                                           a.getClass());
    }
    return s;
  }
  
  public XMLXStream()
  {
  }
    
  public boolean open(Process p, Any toOpen, IntI mode) throws AnyException
  {
    boolean ret = makeStreams(p, toOpen, mode);
    
    if (ret)
    {
      acceptStreams();
    }
    
    return ret;
  }

  public Any read () throws AnyException
  {
    if (dh_ == null)
        throw new IllegalStateException("Not open for read");

    return dh_.result_;
  }

  /**
   * 
   */
  public int read (Map ioKey,
                   Map       outputProto,
                   Array     outputComposite,
                   int       maxCount) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

  /**
   * 
   */
  public Map read (Map ioKey,
                   Map outputProto) throws AnyException
  {
    throw new UnsupportedOperationException();
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
    throw new UnsupportedOperationException();
  }
  
  /**
   * 
   */
  public boolean write (Map ioKey,
                        Map outputItem,
                        Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
                        
  /**
   * 
   */
  public boolean delete (Map ioKey,
                         Map outputItem,
                         Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }                     
  
  /**
   *
   */
  public boolean delete (Map outputItem, Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }
  
  public Document getDOMResult()
  {
    return root_;
  }
  
  public void setDOMOutput(boolean domOutput)
  {
    close();
    
    if (domOutput)
    {
      writer_ = new DOMWriter();
      setWriteMeta(writeMeta_);
      setGroupingUsed(true);
    }
  }
  
  public void setJsonOutput(boolean jsonOutput)
  {
    close();
    
    if (jsonOutput)
    {
      writer_ = new JSONWriter();
      setWriteMeta(writeMeta_);
      setGroupingUsed(false);
    }
  }
  
  public void setXmlOutput(boolean xmlOutput)
  {
    close();
    
    if (xmlOutput)
    {
      writer_ = new StreamWriter();
      setWriteMeta(writeMeta_);
      setGroupingUsed(true);
    }
  }
  
  public void setXmlPrologue(String xmlPrologue)
  {
    prologue_ = xmlPrologue;
  }
  
  public void setFormatOutput(boolean formatOutput)
  {
    formatOutput_ = formatOutput;
  }
  
  protected boolean doCloseRead()
  {
    dh_ = null;
    return true;
  }
  
  protected boolean doCloseWrite()
  {
    if (writer_ != null)
    {
      if (!finaliseOnWrite_)
      {
        writer_.writeMeta();
        writer_.closeDocumentRoot();
      }

      writer_.close();
      
      writer_.init();
      child_ = null;
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
    XMLXStream s = (XMLXStream)super.clone();
    
    s.writer_    = null;
    
    return s;
  }

  protected void acceptStreams() throws AnyException
  {
    try
    {
      if (ostream_ != null)
      {
        pw_ = new PrintWriter(ostream_);
        if (writer_ == null)
          writer_ = new StreamWriter();
        else
          writer_.init();

        if (prologue_ == null)
          prologue_ = "<?xml version=\"1.0\" encoding=\"" +
                     System.getProperty("file.encoding") +
                     "\"?>";
        
        writer_.writeXMLPrologue();
        
//        if (tagFuncs_ != null || metaFunc_ != null)
//          initArgs();
        if (writeMeta_)
          writer_.meta_ = AbstractComposite.set();
        
        if (!finaliseOnWrite_)
          writer_.openDocumentRoot();
      }
      
      if (istream_ != null)
      {
        // throw new UnsupportedOperationException("Read not yet supported");
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();
        dh_ = new DocHandler();
        saxParser.parse(istream_, dh_);
      }
    }
    catch(Exception e)
    {
      throw new ContainedException(e);
    }
  }
  
  private void writeIt(Any outputItem) throws AnyException
  {
    if (!(outputItem instanceof Map))
      throw new IllegalArgumentException(outputItem.getClass().toString() + " is not a Map");

    if (pw_ == null)
      throw new IllegalStateException("Not open for write");

    if (finaliseOnWrite_)
    {
      writer_.openDocumentRoot();
    }

    writer_.openWriteItem();
    
    writer_.write(outputItem);
    
    writer_.closeWriteItem();

    if (finaliseOnWrite_)
      writer_.closeDocumentRoot();
  }
  
  // Call tag functions
  
  private void clearArgs()
  {
    content_.setNull();
    nodeName_.setNull();

//      metaArgs_.replaceItem(TYPEDEF_ARG, AnyNull.instance());
//      metaArgs_.replaceItem(FIELD, AnyNull.instance());
//      metaArgs_.replaceItem(WIDTH, AnyNull.instance());
//      metaArgs_.replaceItem(LABEL, AnyNull.instance());
    attrs_.empty();
  }
  

//  private void initArgs()
//  {
//    // Create the args map we reuse and fill it with values/place-holders
//    if (args_ == null)
//    {
//      args_ = AbstractComposite.simpleMap();
//      
//      args_.add(NODE, AnyNull.instance());
//      args_.add(NODE_NAME, nodeName_);
//      args_.add(PARENT, AnyNull.instance());
//      args_.add(ORDINAL, ordinal_);
//      args_.add(CONTENT, content_);
//      args_.add(LAST, last_);
//      args_.add(DESCEND, descend_);
//      args_.add(ATTRIBUTES, attrs_);
//      
//      metaArgs_ = AbstractComposite.simpleMap();
//      metaArgs_.replaceItem(TYPEDEF_ARG, AnyNull.instance());
//      metaArgs_.replaceItem(FIELD, AnyNull.instance());
//      metaArgs_.replaceItem(WIDTH, AnyNull.instance());
//      metaArgs_.replaceItem(LABEL, AnyNull.instance());
//    }
//  }
  
  // Properties
  
  public void setRootName(Any rootName)
  {
    if (rootName == null || AnyNull.isNullInstance(rootName))
      rootName_ = ROOT;
    else
      rootName_ = rootName.toString();
  }
  
  public void setSeed(Any m)
  {
    if (AnyNull.isNull(m))
      seed_= null;
    else
    {
      if (!(m instanceof Map))
        throw new AnyRuntimeException(m.getClass().toString() + " is not a map");
      
      seed_ = (Map)m;
    }
  }
  
  public void setTagNames(Map tagNames)
  {
    if (AnyNull.isNull(tagNames))
      tagNames_ = null;
    else
    {
      validateMap(tagNames, NodeSpecification.class, StringI.class);
      tagNames_ = tagNames;
    }
  }
  
  public void setTagFuncs(Map tagFuncs)
  {
    if (AnyNull.isNull(tagFuncs))
      tagFuncs_ = null;
    else
    {
      validateMap(tagFuncs, NodeSpecification.class, AnyFuncHolder.FuncHolder.class);
      //initArgs();
      tagFuncs_ = tagFuncs;
      
      // Put the standard arguments into the call statements, adding to
      // any they may have already defined for themselves. We only put
      // in the various values that are maintained during traversal. The
      // remaining arguments, NODE and PARENT are replaced on each call.
      // Should the call statement have defined arguments by the same
      // name then these are silently replaced, rather than throwing.
      Iter i = tagFuncs.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        Call c = (Call)tagFuncs.get(k);
        Map args = c.getArgs();
        args.replaceItem(ATTRIBUTES, attrs_);
        args.replaceItem(ORDINAL, ordinal_);
        args.replaceItem(CONTENT, content_);
        args.replaceItem(DESCEND, descend_);
        args.replaceItem(LAST, last_);
        args.replaceItem(NODE_NAME, nodeName_);
      }
    }
  }
  
  public void setFormatters(Map formatters)
  {
    if (AnyNull.isNull(formatters))
      formatters_ = null;
    else
    {
      validateMap(formatters, NodeSpecification.class, StringI.class, AnyFormat.class);
      formatters_ = formatters;
    }
  }
  
  public void setIncludes(Set includes)
  {
    if (AnyNull.isNull(includes))
    {
      includes_      = null;
      synthExcludes_ = null;
    }
    else
    {
      Set synthExcludes = AbstractComposite.set();
      
      Iter i = includes.createIterator();
      while (i.hasNext())
      {
        Any a = i.next();
        
        // Just to make scripting easier, as paths may get used elsewhere
        // wrapped in ObjectI
        if (a instanceof ObjectI)
          a = (Any)((ObjectI)a).getValue();
        
        if (!(a instanceof NodeSpecification))
          throw new IllegalArgumentException("not a path " + a);
        
        NodeSpecification n = (NodeSpecification)a.cloneAny();
        n.removeLast();
        n.add(NodeSpecification.thisEquals__);
        if (!synthExcludes.contains(n))
          synthExcludes.add(n);
      }
      
      includes_      = includes;
      synthExcludes_ = synthExcludes;
    }
  }
  
  /**
   * A set of paths identifying the nodes to not produce during the traversal.
   * A path that matches any within this set will not be produced in the XML
   * output and will not be descended into.
   * @param excludes
   */
  public void setExcludesAt(Set excludes)
  {
    if (AnyNull.isNull(excludes))
    {
      excludes_ = null;
    }
    else
    {
      validateSet(excludes, NodeSpecification.class);
      excludes_ = excludes;
    }
  }
  
  /**
   * A set of paths identifying the nodes below which there will be
   * no production during the traversal.
   * A path that matches any within this set will be produced in the XML
   * output but will not be descended into.
   * <p/>
   * This property is useful when Inq child nodes will be processed
   * as attributes of their parent.
   * @param excludes
   */
  public void setExcludesBelow(Set excludes)
  {
    if (AnyNull.isNull(excludes))
    {
      excludesBelow_     = null;
    }
    else
    {
      validateSet(excludes, NodeSpecification.class);
      excludesBelow_ = excludes;
    }
  }
  
  /**
   * A set of paths identifying the nodes to produce as a CDATA section
   * @param cdata
   */
  public void setCdata(Set cdata)
  {
    if (AnyNull.isNull(cdata))
    {
      cData_ = null;
    }
    else
    {
      validateSet(cdata, NodeSpecification.class);
      cData_ = cdata;
    }
  }
  
  public void setWriteMeta(boolean writeMeta)
  {
    writeMeta_ = writeMeta;
    
    // If switching meta data production on then it also makes
    // sense to generate Inq attributes in the data section
    if (writeMeta)
      inqAttributes_ = true;
    
    if (writeMeta && writer_ != null)
      writer_.meta_ = AbstractComposite.set();
  }
  
  public void setPreserveTypes(boolean preserveTypes)
  {
    preserveTypes_ = preserveTypes;
  }
  
  public void setInqAttributes(boolean inqAttributes)
  {
    inqAttributes_ = inqAttributes;
  }
  
  public void setEnumExt(boolean enumExt)
  {
    enumExt_ = enumExt;
  }
  
  public void setGroupingUsed(boolean groupingUsed)
  {
    groupingUsed_ = groupingUsed;
  }
  
  public void setMetaName(Any metaName)
  {
    if(AnyNull.isNull(metaName))
    {
      writeMeta_ = false;
    }
    else
    {
      writeMeta_ = true;
      metaName_  = metaName.toString();
      if (writer_ != null)
        writer_.meta_ = AbstractComposite.set();
    }
  }
  
  public void setChildName(Any childName)
  {
    if(AnyNull.isNull(childName))
      nscName_ = NODE_SET_CHILD;
    else
      nscName_  = childName;
  }
  
  public void setMetaFunc(Any func)
  {
    if (AnyNull.isNull(func))
      metaFunc_ = null;
    else
    {
      metaFunc_ = AnyFuncHolder.verifyCall(func, true);
      //initArgs();
      setWriteMeta(true);
    }
  }
  
  public void setWriteMultiple(boolean multiple)
  {
    finaliseOnWrite_ = !multiple;
  }
  
  // End properties
  
  interface XMLProducer
  {
    void write(Any outputItem);

    void writeXMLPrologue();
    
    void openDocumentRoot();

    void closeDocumentRoot();

    void openWriteItem();

    void closeWriteItem();
    
    void close();
  }

  private class StreamWriter extends    AbstractProducer
                             implements XMLProducer
  {    
    private StreamWriter()
    {
      super();
    }
    
    public void writeXMLPrologue()
    {
      // May be some of the attribute values here need to be specified
      // via properties...
      if (pw_ != null)
      {
        //pw_.print("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        pw_.print(prologue_);
        pw_.print("\n");
      }
      else
        notOpenedWrite();
    }
    
    public void openDocumentRoot()
    {
      // Consider namespace requirements sometime
      pw_.print("<");
      pw_.print(rootName_);
      pw_.print(">");
    }
    
    public void closeDocumentRoot()
    {
      pw_.print("</");
      pw_.print(rootName_);
      pw_.println(">");
    }
    
    public void openWriteItem()
    {
      if (!finaliseOnWrite_)
        pw_.print("<write>");
    }

    public void closeWriteItem()
    {
      if (!finaliseOnWrite_)
        pw_.print("</write>");
    }
    
    public void close()
    {
      pw_.close();
      pw_ = null;
    }
    
    protected void startTag(Map m, Any a)
    {
      if (key_ == null)
        return;
      
      indent(NO_INFO);
      
      pw_.print("<");
      
      if (key_ instanceof Composite)
        pw_.print(nscName_.toString());
      else
        pw_.print(key_.toString());
      
      if (m != null)
      {
        typedefAttr(m.getDescriptor());
        nodeSetAttr(m);
        keyAttrs(m);
      }
      else
      {
        fieldAttr(d_, realKey_);
        typeAttrs(a);
      }
      
      attrs();
    }
    
    protected void content(String content, Any a)
    {
      if (cData_ != null && cData_.contains(path_))
      {
        pw_.print("<![CDATA[");
        pw_.print(content);
        pw_.print("]]>");
      }
      else
        pw_.print(Util.escapeXML(content));
    }

    protected void endTag(Map m)
    {
      if (key_ == null)
        return;
      
      pw_.print("</");
      
      if (key_ instanceof Composite)
        pw_.print(nscName_.toString());
      else
        pw_.print(key_.toString());
      pw_.print(">");
      
      newLine(END_TAG);
    }

    protected void emptyTag()
    {
      if (key_ != null)
      {
        indent(NO_INFO);

        pw_.print("<");
        if (key_ instanceof Composite)
          pw_.print(nscName_.toString());
        else
          pw_.print(key_.toString());
        pw_.print("/>");

        newLine(EMPTY_TAG);
      }
    }
    
    protected void newLine(int newLineType)
    {
      if (formatOutput_)
        pw_.print("\n");
    }

    private void attrs()
    {
      if (attrs_.entries() != 0)
      {
        Iter i = attrs_.createKeysIterator();
        while (i.hasNext())
        {
          Any k = i.next();
          if (k != null)
          {
            Any v = attrs_.get(k);
            if (v != null)
            {
              pw_.print(" ");
              pw_.print(k.toString());
              pw_.print("=\"");
              pw_.print(Util.escapeXML(v.toString()));
              pw_.print("\"");
            }
          }
        }
        attrs_.empty();
      }
      
      pw_.print(">");
    }
    
    protected void indent(int indentType)
    {
      if (formatOutput_)
      {
        int spaces = depth_ * 2;
        while (spaces-- > 0)
          pw_.print(' ');
      }
    }
    
    protected Node metaTag(int     metaType,
                           Node    parent,
                           String  tagName,
                           String  content,
                           String  attrName,
                           String  attrVal,
                           boolean open,
                           boolean close,
                           int     count,
                           boolean last)
    {
      if (open)
      {
        pw_.print("<");
        pw_.print(tagName);
      }
      
      if (attrName != null)
      {
        pw_.print(" ");
        pw_.print(attrName);
        pw_.print("=\"");
        pw_.print(attrVal);
        pw_.print("\"");
      }
      
      if (content != null)
      {
        if (open)
          pw_.print(">");
        
        pw_.print(content);
        if (close)
        {
          pw_.print("</");
          pw_.print(tagName);
          pw_.print(">");
        }
      }
      else
      {
        if (close)
          pw_.print("/>");
        else
          pw_.print(">");
      }
      return null;
    }
  }
  
  private class DOMWriter extends    AbstractProducer
                          implements XMLProducer
  {
    // The current node during the production
    private Node     node_;
    
    public void visitMap (Map m)
    {
      Node node = node_;
      super.visitMap(m);
      node_ = node;
    }
    
    protected void content(String content, Any a)
    {
      Node n = root_.createTextNode(content);
      child_.appendChild(n);
    }

    protected void emptyTag()
    {
      if (key_ != null)
      {
        Any k = key_;
        if (k instanceof Composite)
          k = nscName_;
        
        Node node = root_.createElement(k.toString());
        
        node_.appendChild(node);
      }
    }

    protected void endTag(Map m)
    {
      // Nothing to do for DOM
    }

    protected Node metaTag(int     metaType,
                           Node    parent,
                           String  tagName,
                           String  content,
                           String  attrName,
                           String  attrVal,
                           boolean open,
                           boolean close,
                           int     count,
                           boolean last)
    {
      // Never anything to do on just close for DOM
      if (close && !open)
        return null;
      
      Element n = root_.createElement(tagName);
      
      if (attrName != null)
      {
        Attr attr = root_.createAttribute(attrName);
        attr.setNodeValue(attrVal);
        n.setAttributeNode(attr);
      }
      
      if (content != null)
      {
        Node text = root_.createTextNode(content);
        n.appendChild(text);
      }
      
      if (parent == null)
        parent = node_;
      
      parent.appendChild(n);
      
      return n;
    }

    protected void indent(int indentType)
    {
      // Nothing to do for DOM
    }

    protected void newLine(int newLineType)
    {
      // Nothing to do for DOM
    }

    protected void startTag(Map m, Any a)
    {
      if (key_ == null)
        return;
      
      Any k = key_;
      if (k instanceof Composite)
        k = nscName_;
      
      Element node = root_.createElement(k.toString());
      
      node_.appendChild(node);
      
      if (m != null)
      {
        typedefAttr(m.getDescriptor());
        nodeSetAttr(m);
        keyAttrs(m);

        // Step down in the structure
        node_ = node;
        child_ = null;
      }
      else
      {
        fieldAttr(d_, k);
        child_ = node;
      }
      
      attrs(node);
    }

    public void close()
    {
      // Nothing to do for DOM
    }

    public void closeDocumentRoot()
    {
      // Nothing to do for DOM
    }

    public void closeWriteItem()
    {
      // Nothing to do for DOM
    }

    public void openDocumentRoot()
    {
      String domimpl = System.getProperty("inq.xml.dom");
      if (domimpl == null)
        throw new AnyRuntimeException("System property inq.xml.dom is not set");
      
      try
      {
        root_ = (Document)Class.forName(domimpl).newInstance();
      }
      catch (InstantiationException e)
      {
        throw new RuntimeContainedException(e);
      }
      catch (IllegalAccessException e)
      {
        throw new RuntimeContainedException(e);
      }
      catch (ClassNotFoundException e)
      {
        throw new RuntimeContainedException(e);
      }

      Node n = root_.createElement(rootName_);
      root_.appendChild(n);
      node_ = n;
    }

    public void openWriteItem()
    {
      if (!finaliseOnWrite_)
      {
        Node n = root_.createElement("write");
        node_.appendChild(n);
        node_ = n;
      }
    }

    public void writeXMLPrologue()
    {
      // Nothing to do for DOM
    }

    protected void init()
    {
      super.init();
      node_ = null;
    }
    
    private void attrs(Element node)
    {
      // apply the current contents of attrs_ to node
      if (attrs_.entries() != 0)
      {
        Iter i = attrs_.createKeysIterator();
        while (i.hasNext())
        {
          Any k = i.next();
          if (k != null)
          {
            Any v = attrs_.get(k);
            if (v != null)
            {
              Attr attr = root_.createAttribute(k.toString());
              attr.setNodeValue(v.toString());
              node.setAttributeNode(attr);
            }
          }
        }
        attrs_.empty();
      }
    }
  }
  
  private class JSONWriter extends    AbstractProducer
                           implements XMLProducer
  {
    private Map typedefAttrs_ = AbstractComposite.simpleMap();
    
//    public void visitMap(Map m)
//    {
//      if (depth_ == 0 && writeMeta_ && finaliseOnWrite_)
//      {
//        Map mm = AbstractComposite.simpleMap();
//        mm.add(DATA, m);
//        super.visitMap(mm);
//      }
//      else
//        super.visitMap(m);
//    }

    protected void typedefAttr(Descriptor d)
    {
      if (inqAttributes_ &&  d != Descriptor.degenerateDescriptor__)
      {
        Any fQName = d.getFQName();
        Any s = typedefAttrs_.getIfContains(fQName);
        if (s == null)
        {
          s = new ConstString(fQName.toString().replace('.', '_'));
          typedefAttrs_.add(fQName, s);
        }
        attrs_.add(Descriptor.typedef__, s);
      }
    }
    
    protected void keyAttrs(Map m)
    {
      if (inqAttributes_ && m.contains(KeyDef.key__))
      {
      	Descriptor d = (Descriptor)m.get(Descriptor.descriptor__);
      	// attrs_ is an ordered map so the __key member is
        // produced (and more importantly parsed) first. See JSONHandler.
      	attrs_.add(KeyDef.key__, m.get(KeyDef.key__));
      	typedefAttr(d);
      }
    }
    
    protected void content(String content, Any a)
    {
      // We have to do a bit of extra messing about for json. If the
      // content is a string then it needs to be quoted. We apply quotes
      // to dates too because they are strings in json (it has no
      // concept of dates).
      // If its a numeric then content is as-is.
      // Null is returned as "null" (without quotes)
      // Booleans are true/false (no quotes either)

      if (a != null && AnyNull.isNull(a))
        content = "null";
      else if (a == null || a instanceof StringI || a instanceof DateI)
        content = "\"" + Util.escapeJSON(content) + "\"";
        
      pw_.print(content);
    }

    // TODO
    //protected void writeMeta() {}

    private JSONWriter()
    {
      super();
    }
    
    protected void emptyTag()
    {
      // TODO Auto-generated method stub
      // Is this really necessary?
    }

    protected void endTag(Map m)
    {
      if (m != null)
      {
        boolean array = m.getNodeSet() != null;
        if (array)
          pw_.print("]");
        else
          pw_.print("}");
      }
      
      if (depth_ == 0)
        return;

      if (last_.getValue())
      {
//        if (depth_ == 1 && finaliseOnWrite_ && writeMeta_ && meta_.entries() > 0)
//          pw_.print(",");

        newLine(NO_INFO);
      }

      //newLine(END_TAG);
    }

    protected void indent(int indentType)
    {
      if (indentType == END_TAG)
        return;
      
      //if (!finaliseOnWrite_ && indentType == START_META)
      if (indentType == START_META)
      {
        if (!finaliseOnWrite_)
          pw_.print("}"); // close the data object
        
        pw_.print(",\n");
      }

      if (formatOutput_)
      {
        int spaces = depth_ * 2;
        while (spaces-- > 0)
          pw_.print(' ');
      }
    }

    protected Node metaTag(int     metaType,
                           Node    parent,
                           String  tagName,
                           String  content,
                           String  attrName,
                           String  attrVal,
                           boolean open,
                           boolean close,
                           int     count,
                           boolean last)
    {
      int depth = depth_;
      
      if (open)
      {
        pw_.print("\"");
        if (metaType == META_TYPE ||
            metaType == META_FIELD)
        {
          if (metaType == META_TYPE)
            pw_.print(attrVal.replace('.', '_'));
          else
            pw_.print(attrVal);
        }
        else
        {
          pw_.print(tagName);
          if (count >= 0)
            pw_.print(count);
        }
        pw_.print("\": ");
        
        // Only some things have more than one element in them
        if (metaType == META_ROOT ||
            metaType == META_TYPE ||
            metaType == META_FIELD ||
            metaType == META_ENUM)
        {
          pw_.print("{");
          depth_++;
        }
        
        if (metaType == META_TYPE || metaType == META_FIELD)
        {
//          newLine(NO_INFO);
//          indent(NO_INFO);
          
//          pw_.print("\"");
//          pw_.print(attrName);
//          pw_.print("\": ");
//          pw_.print("\"");
//          pw_.print(attrVal);
//          pw_.print("\",");
        }
      }
      
      if (content != null)
      {
        // Only when "opening" for json
        if (open)
        {
          if (metaType == META_ENUM)
          {
            newLine(NO_INFO);
            indent(NO_INFO);
            
            pw_.print("\"INQexternal\": " );
          }
          
          if (metaType != META_WIDTH &&
              metaType != META_NUMERIC)
            pw_.print("\"");
          
          pw_.print(content);

          if (metaType != META_WIDTH &&
              metaType != META_NUMERIC)
            pw_.print("\"");
          
          if (attrName != null && metaType == META_ENUM)
          {
            pw_.print(",");
            newLine(NO_INFO);
            indent(NO_INFO);
            pw_.print("\"");
            pw_.print(attrName);
            pw_.print("\": ");
            pw_.print("\"");
            pw_.print(attrVal);
            pw_.print("\"");
          }
        }
        
        if (close)
        {
          if (metaType == META_ENUM)
            depth_--;
          
          if (metaType == META_ROOT ||
              metaType == META_TYPE ||
              metaType == META_FIELD ||
              metaType == META_ENUM)
          {
            newLine(NO_INFO);
            indent(NO_INFO);
            pw_.print("}");
          }
          if (!last && (metaType == META_TYPE ||
                        metaType == META_FIELD ||
                        metaType == META_ENUM ||
                        metaType == META_WIDTH ||
                        metaType == META_NUMERIC ||
                        metaType == META_LABEL))
            pw_.print(",");
        }
      }
      else
      {
        if (close)
        {
          if (metaType == META_ENUM)
            depth_--;
          
          if (metaType == META_ROOT ||
              metaType == META_TYPE ||
              metaType == META_FIELD ||
              metaType == META_ENUM)
          {
            newLine(NO_INFO);
            indent(NO_INFO);
            pw_.print("}");
          }
          if (!last && (metaType == META_TYPE ||
                        metaType == META_FIELD ||
                        metaType == META_ENUM ||
                        metaType == META_WIDTH ||
                        metaType == META_NUMERIC ||
                        metaType == META_LABEL))
            pw_.print(",");
        }
      }

      depth_ = depth;
      
      return null;
    }

    protected void newLine(int newLineType)
    {
      if (newLineType == START_TAG)
        return;
      
      if (formatOutput_)
        pw_.print("\n");
    }

    protected void startTag(Map m, Any a)
    {
      if (ordinal_.getValue() > 0)
        pw_.print(",");
      
      newLine(END_TAG);
      
      indent(NO_INFO);
      
      // If we are processing array children then a key is not appropriate
      if (!array_ && key_ != null)
      {
        pw_.print("\"");
        
        if (key_ instanceof Composite)
        {
          pw_.print(nscName_.toString());
          pw_.print(ordinal_.getValue());
        }
        else
          pw_.print(key_.toString());
        
        pw_.print("\": ");
      }
      
      if (m != null)
      {
      	// NB only one of typedefAttr() and keyAttrs() should
      	// generate anything
        typedefAttr(m.getDescriptor());
        keyAttrs(m);
        boolean array = nodeSetAttr(m) != null;
        
        // When a map is supplied we are entering a nested JSON object or array
        // Produce the appropriate item open character 
        if (array)
          pw_.print("[");
        else
        {
          pw_.print("{");
          
          // Output the (special) "attributes" as members. They
          // can be handled specially when parsing.
          attrs();
        }
        attrs_.empty();
      }
      else
      {
      	// In fact JSON is unable to support field "attributes"
      	// so it doesn't make sense to attempt to produce them.
        //fieldAttr(d_, realKey_);
      }
    }

    public void close()
    {
      pw_.close();
      pw_ = null;
    }

    public void closeDocumentRoot()
    {
      if (!finaliseOnWrite_ || writeMeta_)
        pw_.print("}");

      pw_.print("}\n");
      typedefAttrs_.empty();
    }

    public void closeWriteItem()
    {
//      if (!finaliseOnWrite_)
//      {
//        pw_.print("}");
//      }
    }

    public void openDocumentRoot()
    {
      typedefAttrs_.empty();
      pw_.print("{\"");
      pw_.print(rootName_);
      pw_.print("\"");
      
      if (writeMeta_)
      {
        // When including metadata put the data itself inside "data":{ ... }
        pw_.print(": {\"data\"");
      }
        
      if (!finaliseOnWrite_)
        pw_.print(": {");
      else
        pw_.print(": ");
    }

    public void openWriteItem()
    {
      if (!finaliseOnWrite_)
      {
        if (writeCount_ > 0)
          pw_.print(",");
        
        pw_.print("\"write");
        pw_.print(writeCount_);
        pw_.print("\": ");
      }
    }

    public void writeXMLPrologue()
    {
      // Nothing for JSON
    }
    
    private void attrs()
    {
    	// The key member must come first, so the right
    	// kind of Java object is created by the parser.
    	Any key = attrs_.getIfContains(KeyDef.key__);
    	if (key != null)
    	{
    		newLine(NO_INFO);
    		indent(NO_INFO);
    		pw_.print("  \"");
    		pw_.print(KeyDef.key__);
    		pw_.print("\": \"");
    		pw_.print(key);
    		pw_.print("\",");
    	}

    	Any typ = attrs_.getIfContains(Descriptor.typedef__);
      if (typ != null)
      {
        newLine(NO_INFO);
        indent(NO_INFO);
        pw_.print("  \"");
        pw_.print(Descriptor.typedef__);
        pw_.print("\": \"");
        pw_.print(typ);
        pw_.print("\",");
      }
      
      attrs_.empty();
    }
  }
  
  private abstract class AbstractProducer extends    AbstractVisitor
                                          implements XMLProducer
  {
    
    // New line/indent types. Helps subclasses decide whether to honour
    // the new line request.
    protected static final int EMPTY_TAG  = 1;
    protected static final int END_TAG    = 2;
    protected static final int START_TAG  = 3;
    protected static final int START_META = 4;
    protected static final int NO_INFO    = 0;
    
    // Meta tag types.
    protected static final int META_ROOT    = 1;
    protected static final int META_TYPE    = 2;
    protected static final int META_FIELD   = 3;
    protected static final int META_DATA    = 4;
    protected static final int META_NUMERIC = 5;
    protected static final int META_WIDTH   = 6;
    protected static final int META_ENUM    = 7;
    protected static final int META_LABEL   = 8;
    
    // The recursion depth, initially zero.
    protected int depth_ = 0;
    
    // Whether the current container is (or is being regarded as) an array.
    // Set when we are processing the container's children (not the container
    // itself)
    protected boolean array_  = false;
    
    protected int writeCount_;

    // A working pathspec to represent where we are during the
    // traversal. Used to check for properties like functions
    // that can be established according to a pathspec (possibly
    // with wildcards)
    protected NodeSpecification path_  = new NodeSpecification();
    
    // The current Map key at the current point in the traversal. Our xml form
    // is the map key is the tag name and the value the content.
    // [Note once we have done the drains-up for the Visitor hierarchy/reentrancy
    // this will no longer need to be a member]
    protected Any      key_;
    
    // As above but always the original value. key_ can be altered by tag
    // names/functions config
    protected Any      realKey_;
    
    // The parent node of the current point in the traversal
    protected Any parent_;
        
    protected Set meta_;
    
    protected HashMap<Class<? extends Any>, StringI> classes_;
    
    // The current Descriptor, if any
    protected Descriptor d_;
    
    protected AbstractProducer()
    {
      content_.setNull();
    }

    public void write(Any outputItem)
    {
      if (writeCount_ > 0 && finaliseOnWrite_)
        throw new AnyRuntimeException("Not set for multiple writes");

      outputItem.accept(this);
      
      writeCount_++;
      
      if (finaliseOnWrite_)
        writeMeta();
    }

    public void visitAnyBoolean (BooleanI b)
    {
      if(openTag(null, b))
      {
        if (content_.isNull())
        {
          Any a = enumExt(realKey_, b);
          content(a.toString(), a);
        }
        else
          content(content_.toString(), null);

        closeTag(null);
      }

    }

    public void visitAnyByte (ByteI b)
    {
      if (openTag(null, b))
      {
        if (content_.isNull())
        {
          Any a = enumExt(realKey_, b);
          content(makeContent(a), a);
        }
        else
          content(content_.toString(), null);

        closeTag(null);
      }
    }

    public void visitAnyChar (CharI c)
    {
      if (openTag(null, c))
      {
        if (content_.isNull())
        {
          Any a = enumExt(realKey_, c);
          content(a.toString(), a);
        }
        else
          content(content_.toString(), null);

        closeTag(null);
      }
    }

    public void visitAnyInt (IntI i)
    {
      if (openTag(null, i))
      {
        if (content_.isNull())
        {
          Any a = enumExt(realKey_, i);
          content(makeContent(a), a);
        }
        else
          content(content_.toString(), null);

        closeTag(null);
      }
    }

    public void visitAnyShort (ShortI s)
    {
      if (openTag(null, s))
      {
        if (content_.isNull())
        {
          Any a = enumExt(realKey_, s);
          content(makeContent(a), a);
        }
        else
          content(content_.toString(), null);

        closeTag(null);
      }
    }

    public void visitAnyLong (LongI l)
    {
      if (openTag(null, l))
      {
        if (content_.isNull())
        {
          Any a = enumExt(realKey_, l);
          content(makeContent(a), a);
        }
        else
          content(content_.toString(), null);
        
        closeTag(null);
      }
    }

    public void visitAnyFloat (FloatI f)
    {
      if (openTag(null, f))
      {
        if (content_.isNull())
        {
          Any a = enumExt(realKey_, f);
          content(makeContent(a), a);
        }
        else
          content(content_.toString(), null);
        
        closeTag(null);
      }
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (openTag(null, d))
      {
        if (content_.isNull())
        {
          Any a = enumExt(realKey_, d);
          content(makeContent(a), a);
        }
        else
          content(content_.toString(), null);
        
        closeTag(null);
      }
    }

    public void visitDecimal (Decimal d)
    {
      if (openTag(null, d))
      {
        content(makeContent(d), d);
        closeTag(null);
      }
    }

    public void visitAnyString (StringI s)
    {
      if (openTag(null, s))
      {
        if (content_.isNull())
        {
          Any a = enumExt(realKey_, s);
          content(a.toString(), a);
        }
        else
          content(content_.toString(), null);
        
        closeTag(null);
      }
    }

    public void visitAnyDate (DateI d)
    {
      if (openTag(null, d))
      {
        if (content_.isNull())
          content(makeContent(d), d);
        else
          content(content_.toString(), null);

        closeTag(null);
      }
    }

    public void visitMap (Map m)
    {
      openTag(m, null);
      
      // Save the current key
      Any key = key_;
      Any realKey = realKey_;
      boolean last = last_.getValue();
      
      // Save the current parent
      Any parent = parent_;
      parent_ = m;
      
      int     curOrdinal = ordinal_.getValue();
      boolean array      = array_;
      
      
      Descriptor d = d_;
      d_ = m.getDescriptor();
      
      // If including the metadata in the result remember each typedef
      // we encounter 
      if (writeMeta_ && d_ != Descriptor.degenerateDescriptor__ && !meta_.contains(d_))
        meta_.add(d_);
        
      if ((excludesBelow_ == null || !excludesBelow_.contains(path_)) && descend_.getValue())
      {
        newLine(START_TAG);
        
        // Bump the depth
        depth_++;
        
        int ordinal = 0;
        // Traverse Map content
        ordinal_.setValue(0);

        // For the moment, hard-code that nodesets are treated as arrays.
        // Possibly configure via a property. Only affects json output.
        array_ = m.getNodeSet() != null;
        
        Iter i;
        
        if (m instanceof Orderable)
        {
          i = m.createKeysIterator();
        }
        else
        {
          Object[] keys = m.keys().toArray();
          Arrays.sort(keys);
          AnyArray akeys = new AnyArray();
          for (Object s : keys)
          	akeys.add((Any)s);
          
          i = akeys.createIterator();
        }
        
        while (i.hasNext())
        {
          Any k = i.next();
          last_.setValue(!i.hasNext());
          
          // Skip null keys
          if (k == null)
            continue;
          
          // If inq attributes are being generated then skip
          // any special fields that characterise keys. They
          // will appear as attributes instead.
          if (inqAttributes_ && (k.equals(KeyDef.key__) || k.equals(Descriptor.descriptor__)))
          	continue;	
  
          // Append current key to the path for matching
          path_.addLiteral(NodeSpecification.strict__);
          path_.addLiteral(k);
  
          // Check if this path is explicitly excluded
          if (excludes_ == null || !excludes_.contains(path_))
          {
            // Check if this path is explicitly included and therefore not
            // implicitly excluded
            boolean b = true;
            if (includes_ == null ||
                (((b = synthExcludes_.contains(path_)) && includes_.contains(path_)) || !b))
            {
              key_     = k;
              realKey_ = k;
              
              // Get the current value and process it
              Any v = m.get(k);
              if (v != null)
              {
              	// There could be many things that are present in the
              	// Map that cannot be 'serialised' by the current
              	// implementation. Most likely an exception will arise,
              	// however for Descriptors (that are present in maps
              	// representing key values) we serialise these
              	// as their fqname string (possibly modified by the
              	// implementation).
              	// TODO: consider using the substitution mechanism, as
              	// this is a bit messy.
              	if (v instanceof Descriptor)
              	{
              		v = ((Descriptor)v).getFQName();
              	}
                ordinal_.setValue(ordinal++);
                v.accept(this);
              }
            }
          }
          
          path_.removeLast();
          path_.removeLast();
        }
        
        depth_--;
        
        indent(NO_INFO);
      }
      
      d_ = d;
      ordinal_.setValue(curOrdinal);
      array_ = array;
      parent_ = parent;
      last_.setValue(last);
      realKey_ = realKey;
      key_ = key;

      closeTag(m);
    }

    public void visitArray (Array a)
    {
      throw new IllegalArgumentException("Array encountered");
    }

    public void visitSet (Set s)
    {
      throw new IllegalArgumentException("Set encountered");
    }
    
    public void visitUnknown(Any o)
    {
//      if (AnyNull.isNullInstance(o))
//      {
//        emptyTag();
//      }
//      else
//      {
        // Assume string representation will do
        if (openTag(null, o))
        {
          // Unknowns are not subject to any formatting
          if (content_.isNull())
            content(o.toString(), null);
          else
            content(content_.toString(), null);
          
          closeTag(null);
//        }
      }
    }
    
    public void visitAnyObject(ObjectI o)
    {
			// Assume string representation will do
			if (openTag(null, o))
			{
				// Objects are not subject to any formatting
				if (content_.isNull())
					content(o.toString(), null);
				else
					content(content_.toString(), null);

				closeTag(null);
			}
    }
    
    protected abstract void startTag(Map m, Any a);
    
    /**
     * Generate content in the production.
     * @param content The content to produce. This may have been subjected
     * to any formatting desired but this is irrespective of any additional
     * requirements the implementation may have. 
     * @param a The original value prior to formatting. Provided should the
     * implementation require type information. May be <code>null</code>
     * meaning no type information is supplied.
     */
    protected abstract void content(String content, Any a);

    protected abstract void endTag(Map m);
    
    protected abstract void emptyTag();

    protected abstract Node metaTag(int     metaType,
                                    Node    parent,
                                    String  tagName,
                                    String  content,
                                    String  attrName,
                                    String  attrVal,
                                    boolean open,
                                    boolean close,
                                    int     count,
                                    boolean last);
    
    protected abstract void newLine(int newLineType);
    
    protected abstract void indent(int indentType);
    
    // Whether or not the grouping character is produced in formatted
    // numeric output
    protected boolean isGroupingUsed()
    {
      return groupingUsed_;
    }
    
    protected boolean openTag(Map m, Any a)
    {
      // For the root node, whose key is null, we still call startTag
      // (Would probably have been better to generate the root element
      // by passing its key here but for now no). This only does anything
      // in the JSON case anyway.
      if (depth_ == 0)
      {
        startTag(m, a);
        return true;
      }
      
      // If the current key is null then we are either at the root map or
      // we have encountered a null key, which we skip
      if (key_ != null)
      {
        // Check for tag name substitution
        tagName(m, a);
        
        // Check if the key was reset to null by any tag function, meaning
        // it will not be produced.
        if (key_ != null)
          startTag(m, a);
      }
      
      return key_ != null;
    }
    
    protected void closeTag(Map m)
    {
      // See comments in openTag
      if (depth_ == 0)
      {
        endTag(m);
        return;
      }
        
      // If the current key is null then we are either at the root map or
      // we have encountered a null key, which we skip 
      if (key_ != null)
      {
        endTag(m);
      }
      
      clearArgs();
    }

    protected void tagName(Map m, Any a)
    {
      Any k;
      if (tagNames_ != null && ((k = tagNames_.getIfContains(path_)) != null))
        key_ = k;
      
      // If we have a tag function at this path then call it
      descend_.setValue(true);
      Call c = null;
      if (tagFuncs_ != null && ((c = (Call)tagFuncs_.getIfContains(path_)) != null))
      {
        Any node = (m != null) ? m : a;
        key_ = callTagFunc2(c, node, key_, parent_, attrs_);
      }
    }
    
    protected void typedefAttr(Descriptor d)
    {
      if (inqAttributes_ && d != Descriptor.degenerateDescriptor__)
      {
        attrs_.add(Descriptor.typedef__, d.getFQName());
      }
    }
    
    protected void keyAttrs(Map m)
    {
      if (inqAttributes_ && m.contains(KeyDef.key__))
      {
      	Descriptor d = (Descriptor)m.get(Descriptor.descriptor__);
        attrs_.add(Descriptor.typedef__, d.getFQName());
        attrs_.add(KeyDef.key__, m.get(KeyDef.key__));
      }
    }
    
    protected Any nodeSetAttr(Map m)
    {
      Any a = m.getNodeSet();
      if (inqAttributes_ && a != null)
      {
        attrs_.add(Descriptor.nodeset__, a);
      }
      return a;
    }
    
    protected void fieldAttr(Descriptor d, Any field)
    {
      // Note if we substituted the tag name then this will
      // probably be inappropriate
      if (inqAttributes_ && d != Descriptor.degenerateDescriptor__)
      {
        attrs_.add(Descriptor.field__, field);
      }
    }
    
    protected void typeAttrs(Any a)
    {
      if (preserveTypes_)
      {
        if (classes_ == null)
          classes_ = new HashMap<Class<? extends Any>, StringI>();
        
        StringI className = classes_.get(a.getClass());
        if (className == null)
        {
          className = new ConstString(a.getClass().getName());
          classes_.put(a.getClass(), className);
        }
        attrs_.add(CLASS, className);
        
        // Tad messy...
        if (a instanceof Decimal)
        {
          Decimal d = (Decimal)a;
          attrs_.add(SCALE, new ConstInt(d.scale()));
        }
      }
    }
    
    // Generate the content for the given any
    protected String makeContent(Any a)
    {
      // If there was content set by a tag function then just
      // use that.
      if (!content_.isNull())
        return content_.toString();
      
      // If there is any enum morphing then just return that
      Any e = enumExt(realKey_, a);
      if (e != a)
        return e.toString();
      
      String ret = null;
      
      // Check for configured formatter. These override any specified
      // for the current typedef/field
      Any f = null;
      AnyFormat formatter = null;
      if (formatters_ != null)
      {
        f = formatters_.getIfContains(path_);
      
        // Replace map value with a real formatter if not already
        if (f != null && !(f instanceof AnyFormat))
        {
          formatter = AnyFormat.makeFormat(a, f.toString());
          
          // Eek use getMapKey on the formatters_ map because the key
          // we compared equal to may be wild-carded
          formatters_.replaceItem(formatters_.getMapKey(path_), formatter);
        }
        else if (f != null)
          formatter = (AnyFormat)f;
      }
      
      // When still no formatter use any from the current descriptor
      if (formatter == null && d_ != Descriptor.degenerateDescriptor__)
      {
        if (formatCache_ == null)
          formatCache_ = AbstractComposite.simpleMap();
        
        // Check for a cached formatter first - two levels of Map
        Map m = (Map)formatCache_.getIfContains(d_);
        if (m == null)
        {
          m = AbstractComposite.simpleMap();
          formatCache_.add(d_, m);
        }
        
        formatter = (AnyFormat)m.getIfContains(realKey_);
        if (formatter == null)
        {
          formatter = AnyFormat.makeFormat(a, d_.getFormat(realKey_));
          m.add(realKey_, formatter);
        }
      }
      
      // If *still* no formatter then just use toString
      if (formatter == null)
        ret = a.toString();
      else
      {
        formatter.setGroupingUsed(isGroupingUsed());
        ret = formatter.format(a);
      }
      
      return ret;
    }
    
    // Provide hooks for derived classes to perform operations
    // either side of content processing. Default is nothing to do.
    protected void beforeContent() {}
    protected void afterContent() {}
    
    protected void writeMeta()
    {
      if (writeMeta_ && meta_.entries() > 0)
      {
        depth_++;
        
        indent(START_META);
        
        Node node = metaTag(META_ROOT, null, metaName_, null, null, null, true, false, -1, false);
        
        newLine(NO_INFO);
        
        IsNumeric n     = new IsNumeric();
        StringI   label = null;
        IntI      width = null;
        label = new AnyString();
        width = new AnyInt();
        
        Iter i = meta_.createIterator();
        while (i.hasNext())
        {
          Descriptor d = (Descriptor)i.next();
          writeTypedef(node, d, n, width, label, !i.hasNext());
        }

        indent(NO_INFO);
        
        metaTag(META_ROOT, null, metaName_, "", null, null, false, true, -1, true);

        newLine(NO_INFO);
        
        depth_--;
      }
    }
    
    private void writeTypedef(Node       parent,
                              Descriptor d,
                              IsNumeric  n,
                              IntI       width,
                              StringI    label,
                              boolean    last)
    {
      depth_++;
      
      indent(NO_INFO);
      
      Node node = metaTag(META_TYPE, parent, "INQtypedef", null, "INQfqname", d.getFQName().toString(), true, false, -1, false);
      
      newLine(NO_INFO);
      
      Map proto = d.getProto();
      int count = 0;
      Iter i = proto.createKeysIterator();
      while (i.hasNext())
      {
        Any k = i.next();
        Any v = proto.get(k);
        writeField(node, d, k, v, n, width, label, count++, !i.hasNext());
      }
      
      indent(NO_INFO);

      metaTag(META_TYPE, null, "INQtypedef", "", null, null, false, true, -1, last);
      //pw_.print("</typedef>");

      newLine(NO_INFO);
      
      depth_--;
    }
    
    private void writeField(Node       parent,
                            Descriptor d,
                            Any        fieldName,
                            Any        field,
                            IsNumeric  n,
                            IntI       width,
                            StringI    label,
                            int        count,
                            boolean    last)
    {
      depth_++;
      
      indent(NO_INFO);
      
      Node node = metaTag(META_FIELD, parent, "INQfield", null, "INQname", fieldName.toString(), true, false, count, false);
      
      newLine(NO_INFO);
      
      writeFieldMeta(node, d, fieldName, field, width, label, n);
      
      indent(NO_INFO);
      
      metaTag(META_FIELD, null, "INQfield", "", null, null, false, true, -1, last);
      
      newLine(NO_INFO);

      depth_--;
    }
    
    private void writeFieldMeta(Node       parent,
                                Descriptor d,
                                Any        fieldName,
                                Any        field,
                                IntI       width,
                                StringI    label,
                                IsNumeric  n)
    {
      depth_++;
      
      indent(NO_INFO);
      
      field.accept(n);
      if (n.isNumeric())
      {
        metaTag(META_NUMERIC, parent, "INQisnumeric", "true", null, null, true, true, -1, false);
        
        newLine(NO_INFO);
        indent(NO_INFO);
      }
      
      width.setValue(d.getWidth(fieldName));
      label.setValue(d.getTitle(fieldName).toString());
      if (metaFunc_ != null)
        callMetaTagFunc2(metaFunc_, d, fieldName, width, label);
      
      metaTag(META_WIDTH, parent, "INQwidth", String.valueOf(width), "INQname", fieldName.toString(), true, true, -1, false);
      
      newLine(NO_INFO);
      indent(NO_INFO);
      
      boolean isEnum = d.isEnum(fieldName);

      
      metaTag(META_LABEL, parent, "INQlabel", label.toString(), "INQname", fieldName.toString(), true, true, -1, !isEnum);
      
      newLine(NO_INFO);
      
      if (d.isEnum(fieldName))
      {
        Map enums = d.getEnums();
        Map enumExts = (Map)enums.get(fieldName);
        int count = 0;
        Iter i = enumExts.createKeysIterator();
        while (i.hasNext())
        {
          Any enumInt = i.next();
          Any enumExt = enumExts.get(enumInt);
          
          indent(NO_INFO);
          metaTag(META_ENUM, parent, "INQenum", enumExt.toString(), "INQinternal", enumInt.toString(), true, true, count++, !i.hasNext());
          newLine(NO_INFO);
        }
      }
      
      depth_--;
    }
    
    protected Any enumExt(Any fieldName, Any val)
    {
      Any ret = val;
      
      if (enumExt_ && d_ != Descriptor.degenerateDescriptor__ && d_.isEnum(fieldName))
      {
        Map enums = d_.getEnums();
        Map enumExts = (Map)enums.get(fieldName);
        ret = enumExts.get(val);
      }
      
      return ret;
    }
    
//    private Any callTagFunc(Call    c,
//                            Any     node,
//                            Any     nodeName,
//                            Any     parent,
//                            Map     attrs)
//    {
//      args_.replaceItem(NODE, node);
//      nodeName_.copyFrom(nodeName);
//      args_.replaceItem(PARENT, parent);
//      content_.setNull();
//      attrs_.empty();
//      
//      Any ret = Call.call(c, args_);
//      
//      // Scripts can return value null (or Java null if they really try)
//      // so check and return Java null in either case
//      if (AnyNull.isNullInstance(ret))
//        ret = null;
//      
//      return ret;
//    }
    
    private Any callTagFunc2(Call    c,
                            Any     node,
                            Any     nodeName,
                            Any     parent,
                            Map     attrs)
    {
      Map args = c.getArgs();
      args.replaceItem(NODE, node);
      nodeName_.copyFrom(nodeName);
      args.replaceItem(PARENT, parent);
      content_.setNull();
      attrs_.empty();
      
      Any ret = Call.call(c, null);
      
      // Scripts can return value null (or Java null if they really try)
      // so check and return Java null in either case
      if (AnyNull.isNullInstance(ret))
        ret = null;
      
      return ret;
    }
    
//    private void callMetaTagFunc(Call c, Descriptor d, Any fieldName, Any width, Any label)
//    {
//      metaArgs_.replaceItem(TYPEDEF_ARG, d);
//      metaArgs_.replaceItem(FIELD, fieldName);
//      metaArgs_.replaceItem(WIDTH, width);
//      metaArgs_.replaceItem(LABEL, label);
//      
//      Any ret = Call.call(c, metaArgs_);
//    }
    
    private void callMetaTagFunc2(Call c, Descriptor d, Any fieldName, Any width, Any label)
    {
      Map args = c.getArgs();
      args.replaceItem(TYPEDEF_ARG, d);
      args.replaceItem(FIELD, fieldName);
      args.replaceItem(WIDTH, width);
      args.replaceItem(LABEL, label);
      
      Call.call(c, null);
    }
    
    protected void init()
    {
      path_.empty();
      key_        = null;
      realKey_    = null;
      d_          = null;
      depth_      = 0;
      writeCount_ = 0;
      
      if (meta_ != null)
        meta_.empty();
    }
  }
  
  private class DocHandler extends DefaultHandler
  {
    private java.util.Stack<HashMap<String, String>> attrStack_    = new java.util.Stack<HashMap<String, String>>();
    private java.util.Stack<StringBuilder>           contentStack_ = new java.util.Stack<StringBuilder>();
    private java.util.Stack<Map>                     mapStack_     = new java.util.Stack<Map>();
    private Stack                                    ordStack_     = AbstractComposite.stack();
    private int                                      depth_        = -1;
    
    private java.util.Map<String, StringI> tagNameFw_ = new java.util.HashMap<String, StringI>();
    private Any result_;
    
//    private Map      args_     = AbstractComposite.simpleMap();
//    private StringI  content_  = new AnyString();
//    private StringI  nodeName_  = new AnyString();

    // Current parse path location
    private NodeSpecification path_  = new NodeSpecification();
    
    @Override
    public void characters(char[] ch, int start, int length)
        throws SAXException
    {
      StringBuilder ss = null;
      if ((ss = contentStack_.peek()) == null)
      {
        contentStack_.pop();
        ss = new StringBuilder(length);
        ss.append(ch, start, length);
        contentStack_.push(ss);
      }
      else
      {
        ss.append(ch, start, length);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
        throws SAXException
    {
      ordStack_.pop();
      Map                     m    = mapStack_.pop();
      HashMap<String, String> attr = popAttrs();
      StringBuilder           c    = contentStack_.pop();
      Any content = null;
      if (c != null)
      {
        String s = c.toString().trim();
        if (s.length() != 0)
        {
          String className = (attr != null) ? attr.get("class") : null;
          if (className == null)
            content = new AnyString(s);
          else
          {
            String scale = attr.get("scale");
            content = Util.makeAny(className, s, scale);
          }
        }
      }
      
      Any tagName = flyweightTagName(qName);
      
      // Check for overridden tagName
      Any k;
      if (tagNames_ != null && ((k = tagNames_.getIfContains(path_)) != null))
        tagName = k;
      
      Call call = null;
      if (tagFuncs_ != null)
      {
        call = (Call)tagFuncs_.getIfContains(path_);
      
        if (!ordStack_.isEmpty())
        {
          IntI ii = (IntI)ordStack_.peek();
          ordinal_.setValue(ii.getValue());
        }
        else
          ordinal_.setValue(0); // root element
      }
      
      if (content != null)
      {
        if (m == null)
        {
          if (mapStack_.empty())
          {
            // TODO: Have only content within the root element. Result will be
            // a scalar.
            
            result_ = m;
            return;
          }
          
          m = mapStack_.peek();
          
          if (m == null)
          {
            if (seed_ != null)
              m = (Map)seed_.buildNew(null);
            else
              m = AbstractComposite.simpleMap();
            
            mapStack_.pop();
            mapStack_.push(m);
          }

          if (call != null)
          {
            tagName = callTagFunc(call, content, tagName, m, attr);
          }
          
          if (m.getDescriptor() != Descriptor.degenerateDescriptor__)
          {
            if (m.contains(tagName))
            {
              // Typedef instance. Expect to have a field by the current tag name.
              // TODO: proper parsing
              m.get(tagName).copyFrom(content);
            }
            else
            {
              // TODO: missing field...
            }
          }
          else
          {
            if (m.contains(tagName))
            {
              // TODO: duplicate tag at this level
            }
            else
            {
              m.add(tagName, content);
            }
          }
        }
        else
        {
          // TODO lost content
        }
      }
      else
      {
        // No content.

        if (mapStack_.empty())
        {
          // Leaving root element
          
          // m is null if root element was empty
          result_ = m;
          return;
        }
        
        Map parent = mapStack_.peek();
        
        if (parent == null)
        {
          if (seed_ != null)
            parent = (Map)seed_.buildNew(null);
          else
            parent = AbstractComposite.simpleMap();
          
          mapStack_.pop();
          mapStack_.push(parent);
        }

        if (call != null)
        {
          tagName = callTagFunc(call, m, tagName, parent, attr);
        }
        
        if (parent.contains(tagName))
        {
          // TODO: duplicate tag at this level
        }
        else
        {
          parent.add(tagName, m);
        }
      }
      
      path_.removeLast();
      path_.removeLast();
      depth_--;
    }

    @Override
    public void startDocument() throws SAXException
    {
      // TODO Auto-generated method stub
      super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
        Attributes attributes) throws SAXException
    {
      depth_++;
      
      Any tagName = flyweightTagName(qName);
      
      // If the stack is not empty (all cases except root) increment
      // the value at the top
      if (!ordStack_.isEmpty())
      {
        IntI count = (IntI)ordStack_.peek();
        count.increment();
      }
      
      // Push a new ordinal counter for any children we may have
      IntI count = new AnyInt(-1);
      ordStack_.push(count);

      if (depth_ > 0)
      {
        // Append current key to the path for matching. The root
        // element is not included in the path - in Inq terms the
        // root element is "$this".
        path_.addLiteral(NodeSpecification.strict__);
        path_.addLiteral(tagName);
      }
      
      // Push the attributes
      pushAttrs(attributes);

      String mapType = attributes.getValue(MAP_TYPE);
      String typedef = attributes.getValue(TYPEDEF);

      Map m = null;

      if (mapType != null)
      {
        m = newMap(mapType);
      }
      else if (typedef != null)
      {
        m = newInstance(typedef);
      }
      mapStack_.push(m);

      // Push null on to content stack as a place-holder
      contentStack_.push(null);
    }

    private void pushAttrs(Attributes attrs)
    {
      // Sax doesn't say anything about the Attributes collection
      // not being reused between tags so we have to take a copy.
      // No shallow/deep copy is supported so we roll our own
      int len = attrs.getLength();
      HashMap<String, String> m = null;
      if (len > 0)
      {
        m = new HashMap<String, String>(len);
        for (int i = 0; i < len; i++)
        {
          String qName = attrs.getQName(i);
          String val = attrs.getValue(qName);
          m.put(qName, val);
        }
      }
      attrStack_.push(m);
    }

    private HashMap<String, String> popAttrs()
    {
      HashMap<String, String> attr = null;

      if (!attrStack_.empty())
        attr = attrStack_.pop();

      return attr;
    }

    private Map newMap(String className)
    {
      try
      {
        Map m = (Map) Class.forName(className).newInstance();
        return m;
      }
      catch (ClassNotFoundException e)
      {
        throw new RuntimeContainedException(e);
      }
      catch (IllegalAccessException e)
      {
        throw new RuntimeContainedException(e);
      }
      catch (InstantiationException e)
      {
        throw new RuntimeContainedException(e);
      }
    }

    private Map newInstance(String typedef)
    {
      Descriptor d = null;

      LocateNode ln = new LocateNode(typedef);
      try
      {
        d = (Descriptor) EvalExpr.evalFunc(Transaction.NULL_TRANSACTION,
            Catalog.instance().getCatalog(), ln, Descriptor.class);

        Map m;
        if (d != null)
        {
          m = (Map) d.newInstance();
        }
        else
        {
          m = AbstractComposite.simpleMap();
        }
        return m;
      }
      catch (AnyException e)
      {
        throw new RuntimeContainedException(new InvalidObjectException(e
            .getMessage()));
      }
    }
    
    private Any flyweightTagName(String tagName)
    {
      StringI ret = tagNameFw_.get(tagName);
      if (ret == null)
      {
        ret = new ConstString(tagName);
        tagNameFw_.put(tagName, ret);
      }
      return ret;
    }
    
    private Map hashMapToAnyMap(HashMap<String, String> hm)
    {
      attrs_.empty();
      
      if(hm == null || hm.isEmpty())
        return attrs_;
      
      java.util.Set<Entry<String,String>> s = hm.entrySet();
      Iterator<Entry<String, String>> i = s.iterator();
      while (i.hasNext())
      {
        Entry<String, String> e = i.next();
        attrs_.add(flyweightTagName(e.getKey()), new AnyString(e.getValue()));
      }

      return attrs_;
    }
    
    private Any callTagFunc(Call    c,
                            Any     node,
                            Any     nodeName,
                            Any     parent,
                            HashMap<String, String> attrs)
    {
      Map args = c.getArgs();
      args.replaceItem(NODE, node);
      nodeName_.copyFrom(nodeName);
      args.replaceItem(PARENT, parent);
      //args.replaceItem(CONTENT, content);
      content_.setNull();
      hashMapToAnyMap(attrs);
      
      Any ret = Call.call(c, null);
      
      // Scripts can return value null (or Java null if they really try)
      // so check and return Java null in either case
      if (AnyNull.isNullInstance(ret))
        ret = null;
      
      return ret;
    }
    
  }
  
  // The handler for reading JSON. Objects are represented by
  private class JSONHandler implements Handler<Map, Vectored>
  {
  	// To identify the special container types required
  	// when deserializing typedef and key instances, special
  	// members must be present in the JSON form.
  	private String key_;
  	private String typedef_;
  	
  	private final AnyString str_ = new AnyString(); 
  	
  	private java.util.Map<String, Descriptor> typeCache_;
  	
  	@Override
  	/**
  	 * Clear down the special members. Object creation is deferred until
  	 * the first JSON member is found. If a key, these must be in the
  	 * order key followed by typedef. Note that the supported "special"
  	 * object productions (of typedef instance or key instance) do not
  	 * themselves contain objects, so we do not need to preserve key_
  	 * and typedef_ as we go.
  	 */
  	public Map startObject(String name, Map parentObject, Vectored parentArray)
  	{
  		key_     = null;
  		typedef_ = null;
  		
  		return null;
  	}
  	
  	@Override
  	public Vectored startArray(String name, Map parentObject, Vectored parentArray)
  	{
  		// TODO: We could include the node set attribute in the JSON
  		// form and even beef this up with what implementation to use,
  		// so at the moment this is as basic as it gets
  		return new AnyOrderedMap();
  	}
  	
		@Override
		public Vectored endArray(String name, Vectored array,
				Map parentObject, Vectored parentArray)
		{
			if (parentObject != null)
  			parentObject.add(AbstractValue.flyweightString(name), array);
			else if (parentArray != null)
			{
				// Arrays are, in fact, AnyOrderedMaps, see above
				Map m = (Map)parentArray;
  			m.add(AbstractValue.flyweightString(name), array);
			}
			
			return array;
		}

		@Override
		public Map endObject(String name, Map object, Map parentObject,
				Vectored parentArray)
		{
			if (parentObject != null)
  			parentObject.add(AbstractValue.flyweightString(name), object);
			else if (parentArray != null)
			{
				// Arrays are, in fact, AnyOrderedMaps, see above
				Map m = (Map)parentArray;
  			m.add(new ConstInt(System.identityHashCode(object)), object);
			}
			
			return object;
		}

		@Override
		public String name(String name, Map object)
		{
			return name;
		}

		@Override
		public Vectored valueToArray(String value, Vectored array, int count, boolean isNumeric)
		{
			// 1. We have no meta-data to drive the Any we should make, so
			// leave it as a string
			// 2. As we are using ordered maps for the array implementation,
			// we need a key to add the value with. Use its identity
			IntI k = new ConstInt(System.identityHashCode(value));
			Map m = (Map)array;
			m.add(k, new AnyString(value));
			
			return array;
		}

		@Override
		public Map valueToObject(String name, String value, Map object,	boolean isNumeric)
		{
			// The order of the "special" JSON members is important and should be
			// kept as follows if JSONWriter is modified:
			// 1. KeyDef.key__
			// 2. Descriptor.typedef__
			// There is no handling of nodesets yet, but this would be mutually exclusive
			// with keys/typedefs
			
			if (name.equals(KeyDef.key__.toString()))
			{
				// The special member indicating that this is a key. Just note
				// it down - until we know the typedef we cannot make the appropriate
				// map.
				key_ = value;
			}
			else if (name.equals(Descriptor.typedef__.toString()))
			{
				// TODO: we are only doing this for the case where the metadata
				// is embedded in the production and the typedef name is used
				// as a JSON member name. That doesn't happen in this part of
				// the production, so consider 
				typedef_ = value.replace('_', '.');
				
				Descriptor d = getTypedef(typedef_);
				
				if (key_ == null)
					object = (Map)d.newInstance();
				else
				{
					str_.setValue(value);
					KeyDef kd = d.getKey(str_);
					object = kd.getKeyProto(); 
				}
			}
			else
			{
				// Not a special member. If we are using an object
				// created by a special member then this already
				// has the expected fields in it. Copy the value
				// over.
				if (typedef_ != null)
				{
					str_.setValue(name);
					Any v = object.get(str_);

					// Is it JSON null ?
					if (com.inqwell.json.DefaultHandler.isNull(value))
					{
						((Value)v).setNull();
					}
					else if (com.inqwell.json.DefaultHandler.isTrue(value))
					{
						v.copyFrom(AnyBoolean.TRUE);
					}
					else if (com.inqwell.json.DefaultHandler.isFalse(value))
					{
						v.copyFrom(AnyBoolean.FALSE);
					}
					else
					{
						// Just copy from the string value. Because it is a string
						// this is fine for decimal values also.
						str_.setValue(value);
						v.copyFrom(str_);
					}
				}
				else
				{
					// Not reconstructing a particular instance type conveyed
					// via the special members. Just add the string to the
					// current map, creating one if not already done so.
					if (object == null)
						object = AbstractComposite.simpleMap();
					
					object.add(AbstractValue.flyweightString(name), new AnyString(value));
				}
			}
			
			return object;
		}
		
		private Descriptor getTypedef(String name)
		{
			Descriptor d = null;
			d = typeCache_.get(name);
			if (d == null)
			{
				// Find the typedef
        LocateNode ln = new LocateNode(name);
        try
        {
          d = (Descriptor)EvalExpr.evalFunc(Transaction.NULL_TRANSACTION,
              Catalog.instance().getCatalog(),
              ln,
              Descriptor.class);
        }
        catch(AnyException e)
        {
          throw new RuntimeContainedException(e);
        }
			}
			
			if (d == null)
				throw new AnyRuntimeException("Unknown type " + name);
			
			return d;
		}
  }
  
  static public class IsNumeric implements Visitor
  {
    private boolean isNumeric_;
    
    public boolean isNumeric()
    {
    	return isNumeric_;
    }

    public Transaction getTransaction()
    {
      return null;
    }

    public void setTransaction(Transaction t)
    {
    }

    public void visitAnyBoolean(BooleanI b)
    {
      isNumeric_ = false;
    }

    public void visitAnyByte(ByteI b)
    {
      isNumeric_ = true;
    }

    public void visitAnyChar(CharI c)
    {
      isNumeric_ = false;
    }

    public void visitAnyDate(DateI d)
    {
      isNumeric_ = false;
    }

    public void visitAnyDouble(DoubleI d)
    {
      isNumeric_ = true;
    }

    public void visitAnyFloat(FloatI f)
    {
      isNumeric_ = true;
    }

    public void visitAnyInt(IntI i)
    {
      isNumeric_ = true;
    }

    public void visitAnyLong(LongI l)
    {
      isNumeric_ = true;
    }

    public void visitAnyObject(ObjectI o)
    {
      isNumeric_ = false;
    }

    public void visitAnyShort(ShortI s)
    {
      isNumeric_ = true;
    }

    public void visitAnyString(StringI s)
    {
      isNumeric_ = false;
    }

    public void visitArray(Array a)
    {
      isNumeric_ = true;
    }

    public void visitDecimal(Decimal d)
    {
      isNumeric_ = true;
    }

    public void visitFunc(Func f)
    {
      isNumeric_ = false;
    }

    public void visitMap(Map m)
    {
      isNumeric_ = false;
    }

    public void visitSet(Set s)
    {
      isNumeric_ = false;
    }

    public void visitUnknown(Any o)
    {
      isNumeric_ = false;
    }
  }

  private static final class SinkOutputStream extends OutputStream
  {
    public void write(byte[] b, int off, int len)
    {
      throw new UnsupportedOperationException();
    }

    public void write(int b)
    {
      throw new UnsupportedOperationException();
    }
  }
}
