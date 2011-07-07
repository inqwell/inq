/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyDocument.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import javax.swing.text.View;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractFunc;
import com.inqwell.any.AbstractIter;
import com.inqwell.any.AbstractMap;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyPrintable;
import com.inqwell.any.Array;
import com.inqwell.any.ConstFloat;
import com.inqwell.any.ConstInt;
import com.inqwell.any.ConstShort;
import com.inqwell.any.ConstString;
import com.inqwell.any.FieldNotFoundException;
import com.inqwell.any.FloatI;
import com.inqwell.any.Func;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.ObjectI;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.ShortI;
import com.inqwell.any.StringI;
import com.inqwell.any.Vectored;
import com.inqwell.any.beans.ClassMap;
import com.inqwell.any.client.swing.StyledDocument;
import com.inqwell.any.client.swing.StyledEditorKit;

/**
 * Subsume a Swing text document into the Inq environment. This class
 * wraps an instance of <code>javax.swing.text.Document</code> providing
 * access to it and its element content via Inq node references and acting
 * as the required type for document operations.
 * <p>
 * Although a map, it does not contain anything in its own right.
 * Because it represents a <code>Document</code>, which has only a
 * root node, the mutation methods are not supported and the 'Map'
 * only contains one element known as '<code>root</code>'.
 * <p>
 * This class also manages the styles that are used and supports
 * static methods for returning a style based on the given attributes.
 *
 * @see AnyElement
 */
public class AnyDocument extends    AbstractMap
                         implements Map,
                                    Vectored,
                                    AnyPrintable
{
	public static int BEFORE = 0;
	public static int AFTER  = 1;

  public static Any ANYTABLE     = new ConstString(StyledEditorKit.TABLE);
  public static Any ANYROW       = new ConstString(StyledEditorKit.ROW);
  public static Any ANYCELL      = new ConstString(StyledEditorKit.CELL);
  public static Any ANYPAGEBREAK = new ConstString(StyledEditorKit.PAGEBREAK);
  public static Any ANYPARA      = new ConstString(AbstractDocument.ParagraphElementName);
  
  public static DefaultStyledDocument.ElementSpec endPara__ = null;  // \n content - see parser
  
  public static DefaultStyledDocument.ElementSpec esSectionStart__ = null;
  public static DefaultStyledDocument.ElementSpec esSectionEnd__   = null;
  
  /*
  		  // Check if the 'style' item is, in fact, some
		  // auxiliary information that we will put into the
		  // style later
//		  if (auxInfoKeys__.contains(sk))
//		  {
		  	if (auxInfo == null)
		  	  auxInfo = AbstractComposite.simpleMap();
		  	
		  	auxInfo.add(sk, item.get(sk));
//		  }
		}

*/
  private static String root__ = "root";
  public static Any rootKey__ = new ConstString(root__);

	static private MutableAttributeSet currStyle__;
	static private MutableAttributeSet defaultStyle__;
	static public  DefaultStyledDocument.ElementSpec blankElement__;
	static public  DefaultStyledDocument.ElementSpec[] pageBreak__;
  
	static private Map          styleFuncs__;
  static private ClassMap     renderFuncs__;
  static private ClassMap     textFuncs__;

	static
	{
    defaultStyle__ = new SimpleAttributeSet();

    blankElement__ = new DefaultStyledDocument.ElementSpec(defaultStyle__,
                                                           DefaultStyledDocument.ElementSpec.ContentType,
                                                           " ".toCharArray(), 0, 1);

    pageBreak__ = new DefaultStyledDocument.ElementSpec[3];
    MutableAttributeSet pbPara  = new SimpleAttributeSet();
    pbPara.addAttribute(AbstractDocument.ElementNameAttribute,
                        AbstractDocument.ParagraphElementName);
    MutableAttributeSet pbStyle = new SimpleAttributeSet();
    pbStyle.addAttribute(AbstractDocument.ElementNameAttribute,
                         StyledEditorKit.PAGEBREAK);
    DefaultStyledDocument.ElementSpec esParaStart =
      new DefaultStyledDocument.ElementSpec(pbPara,
                                            DefaultStyledDocument.ElementSpec.StartTagType);
    DefaultStyledDocument.ElementSpec pbContent = new DefaultStyledDocument.ElementSpec(pbStyle,
                                                           DefaultStyledDocument.ElementSpec.ContentType,
                                                           " ".toCharArray(), 0, 1);
    DefaultStyledDocument.ElementSpec esParaEnd =
      new DefaultStyledDocument.ElementSpec(pbPara,
                                            DefaultStyledDocument.ElementSpec.EndTagType);

    pageBreak__[0] = esParaStart;
    pageBreak__[1] = pbContent;
    pageBreak__[2] = esParaEnd;

    styleFuncs__ = AbstractComposite.simpleMap();
		styleFuncs__.add(NodeSpecification.atTxt__, new StyleText());
		styleFuncs__.add(NodeSpecification.ico__, new StyleIcon());
		styleFuncs__.add(NodeSpecification.bg__, new StyleBackground());
		styleFuncs__.add(NodeSpecification.fg__, new StyleForeground());
		styleFuncs__.add(NodeSpecification.bold__, new StyleBold());
		styleFuncs__.add(NodeSpecification.fontFamily__, new StyleFontFamily());
		styleFuncs__.add(NodeSpecification.fontSize__, new StyleFontSize());
		styleFuncs__.add(NodeSpecification.italic__, new StyleItalic());
		styleFuncs__.add(NodeSpecification.strikeThrough__, new StyleStrikeThrough());
		styleFuncs__.add(NodeSpecification.underline__, new StyleUnderline());
		styleFuncs__.add(NodeSpecification.firstIndent__, new StyleFirstIndent());
		styleFuncs__.add(NodeSpecification.leftIndent__, new StyleLeftIndent());
		styleFuncs__.add(NodeSpecification.rightIndent__, new StyleRightIndent());
		styleFuncs__.add(NodeSpecification.alignment__, new StyleAlignment());
		styleFuncs__.add(NodeSpecification.lineSpacing__, new StyleLineSpacing());
		styleFuncs__.add(NodeSpecification.spaceAbove__, new StyleSpaceAbove());
		styleFuncs__.add(NodeSpecification.spaceBelow__, new StyleSpaceBelow());
		styleFuncs__.add(NodeSpecification.subscript__, new StyleSubscript());
		styleFuncs__.add(NodeSpecification.superscript__, new StyleSuperscript());
		styleFuncs__.add(NodeSpecification.tabStops__, new StyleTabStops());
		styleFuncs__.add(NodeSpecification.name__, new StyleName());
		styleFuncs__.add(NodeSpecification.borderWidth__, new StyleBorderWidth());
		styleFuncs__.add(NodeSpecification.cellPadding__, new StyleCellPadding());
		styleFuncs__.add(NodeSpecification.columnSpan__, new StyleColumnSpan());
		styleFuncs__.add(NodeSpecification.rowSpan__, new StyleRowSpan());
		styleFuncs__.add(NodeSpecification.atCellAlign__, new StyleCellAlign());
    styleFuncs__.add(NodeSpecification.atCellWidth__, new StyleCellWidth());
    styleFuncs__.add(NodeSpecification.atCellFill__, new StyleCellFill());
    styleFuncs__.add(NodeSpecification.atToolTip__, new StyleToolTip());

		renderFuncs__ = new ClassMap();
		renderFuncs__.add(Any.class,      new ProcessDefaultStyle());
		renderFuncs__.add(StringI.class,  new ProcessDefaultStyle());
		renderFuncs__.add(AnyIcon.class,  new ProcessIcon());
		renderFuncs__.add(Map.class,      new ProcessMap());

		textFuncs__ = new ClassMap();
		textFuncs__.add(Any.class,       new ProcessDefaultText());
		textFuncs__.add(StringI.class,   new ProcessDefaultText());
		textFuncs__.add(AnyIcon.class,   new ProcessIconText());
		textFuncs__.add(Map.class,       new ProcessMapText());
		textFuncs__.add(ObjectI.class,   new ProcessTableText());

    SimpleAttributeSet section = new SimpleAttributeSet();
    section.addAttribute(AbstractDocument.ElementNameAttribute, "section");
    esSectionStart__ = new DefaultStyledDocument.ElementSpec(section, DefaultStyledDocument.ElementSpec.StartTagType);
    esSectionEnd__   = new DefaultStyledDocument.ElementSpec(section, DefaultStyledDocument.ElementSpec.EndTagType);
	}

  private Document d_;

  private AnyInt   index_;

	public AnyDocument(Document d)
	{
	  setDocument(d);
	}

	public AnyDocument()
	{
    setDocument(new StyledDocument());
	}

  public Any get (Any key)
  {
    if (key.toString().equals(root__))
      return getByVector(0);

		throw new FieldNotFoundException("AnyDocument.get() key is " + key);
  }

  public Any getIfContains(Any key)
  {
    if (key.toString().equals(root__))
      return getByVector(0);
    else
    {
      return null;
    }
  }

  public void reverse()
  {
    throw new UnsupportedOperationException();
  }

  public void removeByVector (int at)
  {
    throw new UnsupportedOperationException();
  }

  public void removeByVector (Any at)
  {
    throw new UnsupportedOperationException();
  }

  public int indexOf(Any a)
  {
    throw new UnsupportedOperationException();
  }

  public boolean isEmpty()
  {
    return false;
  }

  public int entries()
  {
    return 1;
  }

  public boolean contains (Any key)
  {
    if (key.toString().equals(root__))
      return true;
    else
      return false;
  }

  public Any getByVector (int at)
  {
    if (at == 0)
      return new AnyElement(d_.getDefaultRootElement());

    throw new ArrayIndexOutOfBoundsException("" + at);
  }

  public Any getByVector (Any at)
  {
    if (index_ == null)
      index_ = new AnyInt();

    index_.copyFrom(at);

    return this.getByVector(index_.getValue());
  }

  public Any getKeyOfVector(int at)
  {
    throw new UnsupportedOperationException();
  }

  public Any getKeyOfVector(Any at)
  {
    throw new UnsupportedOperationException();
  }
  
  public void addByVector(Any value)
  {
    throw new UnsupportedOperationException();
  }

  public void addByVector(int at, Any value)
  {
    throw new UnsupportedOperationException();
  }

  public void addByVector(int at, Any key, Any value)
  {
    throw new UnsupportedOperationException();
  }

  public Array initOrderBacking()
  {
    throw new UnsupportedOperationException();
  }
  
  public void setSparse(boolean isSparse)
  {
    throw new UnsupportedOperationException();
  }

  public Iter createIterator()
  {
    return new AnyDocumentIter();
  }

  public Object[] toArray()
  {
    throw new UnsupportedOperationException();
  }
  
  public void setDocument(Document d)
  {
    d_ = d;
  }

  public Document getDocument()
  {
    return d_;
  }

  public StyledDocument getStyledDocument()
  {
    return (StyledDocument)d_;
  }

  public Any copyFrom (Any a)
  {
    if (!(a instanceof AnyDocument))
      throw new IllegalArgumentException ("Not an AnyDocument");
    
    AnyDocument d = (AnyDocument)a;
    setDocument(d.getDocument());
    
    return this;
  }

	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}

  // printable stuff

  public Any getNumberOfPages()
  {
    return null;
  }
  
  public Printable getPrintable()
  {
    return new DocumentRenderer();
  }
  
	private class AnyDocumentIter extends AbstractIter implements Iter
	{
	  private boolean hasNext_ = true;

    public boolean hasNext()
    {
      return hasNext_;
    }

    public Any next()
    {
      if (!hasNext_)
        throw new NoSuchElementException("Nothing after root");

      hasNext_ = false;

      return AnyDocument.this.getByVector(0);
    }

    public void remove()
    {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Get a Style appropriate to the contents of the given argument.
   * The argument may be an instance of AnyIcon, StringI or Map.
   * If it is a Map then the map should contain the well-known
   * keys and values for style representation.
   */
  public static MutableAttributeSet getStyle(Any a)
  {
    ProcessItem p = (ProcessItem)renderFuncs__.get(a);
    return p.getStyle(a);
  }

  /**
   * Get the string to be rendered.
   * The argument may be an instance of AnyIcon, StringI or Map.
   * If it is a Map then the map should contain the well-known
   * keys and values for style content.
   * <p>
   * As required by DefaultStyledDocument, this method returns
   * a single space for Icons.
   */
  public static String getString(Any a) throws AnyException
  {
    Func f = (Func)textFuncs__.get(a);
    a = f.execFunc(a);
    return a != null ? a.toString() : null;
  }


  /**
   * Create new style to render the given item.  The map is processed and
   * the items within it that are keyed by the well-known values for
   * style elements are converted to the appropriate type.
   * <p>
   * Care should be taken with the map as all items will be placed in the
   * style.  Those that are not recognised as style items are placed in
   * the style as is.  This is useful for storing auxiliary information
   * in the style that is required later, for example the URL reference
   * in hyperlink text.
   */
	public static MutableAttributeSet getRenderStyle(Map item) throws AnyException
	{
    if (item.contains(NodeSpecification.style__))
    {
      AnyAttributeSet as = (AnyAttributeSet)item.get(NodeSpecification.style__);
      if (as != null)
        return as.getAttributeSet();
      return null;
    }
    
    currStyle__ = (MutableAttributeSet)((SimpleAttributeSet)defaultStyle__).clone();
        
	  if (item.contains(NodeSpecification.ico__))
    {
      currStyle__ = (MutableAttributeSet)((SimpleAttributeSet)currStyle__).clone();
	    return getStyle(item.get(NodeSpecification.ico__));
    }

	  // the only other thing we support at this time is text (for
	  // example, there is no component support yet)

    // Requirement for text to be present removed for things
    // like paragraph attributes
//    if (!item.contains(NodeSpecification.txt__))
//      return defaultStyle__;

		Iter i = item.createKeysIterator();

    while (i.hasNext())
    {
      Any k = i.next();
      Any v = item.get(k);
      
      convertAttribute(k, v, currStyle__);
		}

    MutableAttributeSet s = currStyle__;
    currStyle__ = null;
    
    return s;
	}

  /**
   * Applies those style elements that are applicable to a basic
   * container, that is font family, size and style, foreground and
   * background colours.
   */
  public static void applyStyle(Container c, MutableAttributeSet as)
  {
    String  fontFamily = (String)as.getAttribute(StyleConstants.FontFamily);
    boolean isBold     = StyleConstants.isBold(as);
    boolean isItalic   = StyleConstants.isItalic(as);
    int     fontSize   = 0;
    
    Integer ii = (Integer) as.getAttribute(StyleConstants.FontSize);
    if (ii != null)
      fontSize = ii.intValue();

    Color fg = (Color) as.getAttribute(StyleConstants.Foreground);
    Color bg = (Color) as.getAttribute(StyleConstants.Background);

    // Apply any foreground
    if (fg != null && !fg.equals(c.getForeground()))
      c.setForeground(fg);
      
    // Apply any background
    if (bg != null && !bg.equals(c.getBackground()))
      c.setBackground(bg);
    
    // Apply any tooltip
    Object toolTip = as.getAttribute(NodeSpecification.atToolTip__);
    if (toolTip != null)
      ((JComponent)c).setToolTipText(toolTip.toString());
    
    Font curF = c.getFont();
    
    // If no family specified then use existing
    if (fontFamily == null)
      fontFamily = curF.getFamily();
    
    // If no size specified then use existing
    if (fontSize == 0)
      fontSize = curF.getSize();
    
    boolean styleChanged = (isBold != curF.isBold() ||
                            isItalic != curF.isItalic());

    if (styleChanged ||
        !fontFamily.equals(curF.getFamily()) ||
        !(fontSize != curF.getSize()))
      c.setFont(new Font(fontFamily,
                         Font.PLAIN | (isBold ? Font.BOLD : 0)
                                    | (isItalic ? Font.ITALIC : 0),
                                    fontSize));
  }
  
	public static void convertAttribute(Any k, Any v, MutableAttributeSet style) throws AnyException
	{
    if (styleFuncs__.contains(k))
    {
      MutableAttributeSet s = currStyle__;
      currStyle__ = style;
      try
      {
        Func f = (Func)styleFuncs__.get(k);
        f.execFunc(v);
      }
      finally
      {
        currStyle__ = s;
      }
    }
    else
    {
      // Just add the attribute, as it is assumed to be used by
      // other code than the Java rendering.
      // (We should really set up the aux info in a child style and
      // fall back to the text attrs in a cached parent)
      if (!k.equals(NodeSpecification.atTxt__))
        style.addAttribute(k, v);
    }
	}
	
  /**
   * Returns a style suitable for handling just plain icons
   */
  public static MutableAttributeSet getIconStyle(AnyIcon icon)
  {
    Icon i = icon.getIcon();

    MutableAttributeSet s = new SimpleAttributeSet();

    // alignment ?
    StyleConstants.setIcon(s, i);

    //System.out.println("Doing ICON " + i + "  " + s);

    return s;
  }

  public static MutableAttributeSet getDefaultStyle()
  {
    return defaultStyle__;
  }

  // Set of Funcs mapped from the class of the item we are
  // rendering.  Make a style
  static private abstract class ProcessItem extends AbstractFunc
  {
    protected MutableAttributeSet style_;

    public MutableAttributeSet getStyle(Any item)
    {
	    try
	    {
        this.execFunc(item);
      }
      catch (AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
      MutableAttributeSet s = style_;
      style_ = null;
      return s;
    }
  }

  static private class ProcessDefaultStyle extends ProcessItem
  {
    public Any exec(Any a) throws AnyException
    {
      style_ = AnyDocument.getDefaultStyle();
      return null;
    }
  }

  static private class ProcessIcon extends ProcessItem
  {
    public Any exec(Any a) throws AnyException
    {
      style_ = AnyDocument.getIconStyle((AnyIcon)a);
      return null;
    }
  }

  static private class ProcessMap extends ProcessItem
  {
    public Any exec(Any a) throws AnyException
    {
      style_ = AnyDocument.getRenderStyle((Map)a);
      return null;
    }
  }

  // Set of Funcs mapped from the class of the item we are
  // rendering.  Get the text
  static private class ProcessDefaultText extends AbstractFunc
  {
    public Any exec(Any a) throws AnyException
    {
      // just hope toString is good
      return new ConstString(a.toString());
    }
  }

  static private class ProcessIconText extends AbstractFunc
  {
    private static Any iconText__ = new ConstString(" ");

    public Any exec(Any a) throws AnyException
    {
      return iconText__;
    }
  }

  static private class ProcessMapText extends AbstractFunc
  {
    private static Any noText__ = new ConstString("");

    public Any exec(Any a) throws AnyException
    {
      Map m = (Map)a;

      if (m.contains(NodeSpecification.ico__))
        return AnyDocument.ProcessIconText.iconText__;
      
      if (!m.contains(NodeSpecification.atTxt__))
        return noText__;

      return m.get(NodeSpecification.atTxt__);
    }
  }

  static private class ProcessTableText extends AbstractFunc
  {
    public Any exec(Any a) throws AnyException
    {
      // There's no text for a table, content is already in the
      // element specs
      return null;
    }
  }
  
  // Set of Funcs mapped from the style element we are establishing.
  // The arg to exec(Any a) is the object we want to establish on
  // the style element
	static private class StyleName extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      currStyle__.addAttribute(AbstractDocument.ElementNameAttribute,
                               a.toString());
      return null;
	  }
  }

	static private class StyleBackground extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      //System.out.println("Setting BG");
      AnyColor c = (AnyColor)a;
			StyleConstants.setBackground(currStyle__, c.getColor());
      return null;
	  }
  }

	static private class StyleText extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      // Does nothing except to recognise that text is a
      return null;
	  }
  }

	static private class StyleForeground extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      AnyColor c = (AnyColor)a;
			StyleConstants.setForeground(currStyle__, c.getColor());
      return null;
	  }
  }

	static private class StyleBold extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
			StyleConstants.setBold(currStyle__, true);
      return null;
	  }
  }

	static private class StyleItalic extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
			StyleConstants.setItalic(currStyle__, true);
      return null;
	  }
  }

	static private class StyleStrikeThrough extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
			StyleConstants.setStrikeThrough(currStyle__, true);
      return null;
	  }
  }

	static private class StyleUnderline extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
			StyleConstants.setUnderline(currStyle__, true);
      return null;
	  }
  }

	static private class StyleFontSize extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      IntI i = (IntI)a;
			StyleConstants.setFontSize(currStyle__, i.getValue());
      return null;
	  }
  }

	static private class StyleFontFamily extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      StringI str = (StringI)a;
			StyleConstants.setFontFamily(currStyle__, str.getValue());
      return null;
	  }
  }

	static private class StyleIcon extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      AnyIcon icon = (AnyIcon)a;
      Icon i = icon.getIcon();
      StyleConstants.setIcon(currStyle__, i);
      //System.out.println("StyleIcon ICON " + i + "  " + currStyle__);
      return null;
	  }
  }

	static private class StyleFirstIndent extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      FloatI f = (FloatI)a;
			StyleConstants.setFirstLineIndent(currStyle__, f.getValue());
	  	//System.out.println("*********************FirstIndent: " + f);
      return null;
	  }
  }

	static private class StyleLeftIndent extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      FloatI f = (FloatI)a;
			StyleConstants.setLeftIndent(currStyle__, f.getValue());
	  	//System.out.println("*********************LeftIndent: " + f);
      return null;
	  }
  }

	static private class StyleRightIndent extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      FloatI f = (FloatI)a;
			StyleConstants.setRightIndent(currStyle__, f.getValue());
      return null;
	  }
  }

	static private class StyleSpaceAbove extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      FloatI f = (FloatI)a;
			StyleConstants.setSpaceAbove(currStyle__, f.getValue());
      return null;
	  }
  }

	static private class StyleSpaceBelow extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      FloatI f = (FloatI)a;
			StyleConstants.setSpaceBelow(currStyle__, f.getValue());
      return null;
	  }
  }

	static private class StyleLineSpacing extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      FloatI f = (FloatI)a;
			StyleConstants.setLineSpacing(currStyle__, f.getValue());
      return null;
	  }
  }

	static private class StyleBorderWidth extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      FloatI f = new ConstFloat(a);
      currStyle__.addAttribute(StyledEditorKit.BorderWidth,
                               new Float(f.getValue()));
      return null;
	  }
  }

	static private class StyleCellPadding extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      ShortI s = new ConstShort(a);
      currStyle__.addAttribute(StyledEditorKit.CellPadding,
                               new Short(s.getValue()));
      return null;
	  }
  }

	static private class StyleColumnSpan extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      // JDK classes require these as a String
      currStyle__.addAttribute(HTML.Attribute.COLSPAN,
                               a.toString());
      return null;
	  }
  }

  static private class StyleRowSpan extends AbstractFunc
  {
    public Any exec (Any a) throws AnyException
    {
      currStyle__.addAttribute(HTML.Attribute.ROWSPAN,
                               a.toString());
      return null;
    }
  }

  static private class StyleToolTip extends AbstractFunc
  {
    public Any exec (Any a) throws AnyException
    {
      currStyle__.addAttribute(NodeSpecification.atToolTip__,
                               a.toString());
      return null;
    }
  }

	static private class StyleSubscript extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
			StyleConstants.setSubscript(currStyle__, true);
      return null;
	  }
  }

	static private class StyleSuperscript extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
			StyleConstants.setSuperscript(currStyle__, true);
      return null;
	  }
  }

	static private class StyleAlignment extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      IntI i = (IntI)a;
			StyleConstants.setAlignment(currStyle__, i.getValue());
      return null;
	  }
  }

	static private class StyleCellAlign extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      FloatI f = new ConstFloat(a);
      currStyle__.addAttribute(StyledEditorKit.CellAlign,
                               new Float(f.getValue()));
      return null;
	  }
  }

	static private class StyleCellWidth extends AbstractFunc
	{
	  public Any exec (Any a) throws AnyException
	  {
      IntI i = new ConstInt(a);
      currStyle__.addAttribute(StyledEditorKit.CellWidth,
                               new Integer(i.getValue()));
      return null;
	  }
  }

  static private class StyleCellFill extends AbstractFunc
  {
    public Any exec (Any a) throws AnyException
    {
      AnyColor c = (AnyColor)a;
      currStyle__.addAttribute(StyledEditorKit.CellFill, c.getColor());
      return null;
    }
  }

	static private class StyleTabStops extends AbstractFunc
	{
		static private TabStopComparator comparator__ = new TabStopComparator();
		
	  public Any exec (Any a) throws AnyException
	  {
      Map m = (Map)a;
      
      // Each of the map's immediate children represents a tab stop.
      // Make a tab set.
      
      TabStop[] tabStops = new TabStop[m.entries()];
      
      Iter i = a.createIterator();
      int  count = 0;
      while (i.hasNext())
      {
        Map tabAttrs = (Map)i.next();
        AnyTabStop ts = new AnyTabStop(tabAttrs);
        tabStops[count++] = ts.getTabStop();
      }
      Arrays.sort(tabStops, comparator__);
      
			StyleConstants.setTabSet(currStyle__, new TabSet(tabStops));
	  	System.out.println("*********************TabStops: " + m);
	  	System.out.println("*********************TabStops: " + tabStops);
      return null;
	  }
	  
	  static private class TabStopComparator implements Comparator
	  {
	  	public int compare(Object o1, Object o2)
	  	{
	  		TabStop t1 = (TabStop)o1;
	  		TabStop t2 = (TabStop)o2;
	  		
	  		float p1 = t1.getPosition();
	  		float p2 = t2.getPosition();

	  		if (p1 < p2)
	  		  return -1;
	  		else if (p1 > p2)
	  		  return 1;
	  		else
	  		  return 0;
	  	}
	  }
  }
  
  // printing stuff

  /*  Copyright 2002
      Kei G. Gauthier
      Suite 301
      77 Winsor Street
      Ludlow, MA  01056
  */

  /**
      DocumentRenderer prints objects of type Document. Text attributes, including
      fonts, color, and small icons, will be rendered to a printed page.
      DocumentRenderer computes line breaks, paginates, and performs other
      formatting.

      An HTMLDocument is printed by sending it as an argument to the
      print(HTMLDocument) method. A PlainDocument is printed the same way. Other
      types of documents must be sent in a JEditorPane as an argument to the
      print(JEditorPane) method. Printing Documents in this way will automatically
      display a print dialog.

      As objects which implement the Printable Interface, instances of the
      DocumentRenderer class can also be used as the argument in the setPrintable
      method of the PrinterJob class. Instead of using the print() methods
      detailed above, a programmer may gain access to the formatting capabilities
      of this class without using its print dialog by creating an instance of
      DocumentRenderer and setting the document to be printed with the
      setDocument() or setJEditorPane(). The Document may then be printed by
      setting the instance of DocumentRenderer in any PrinterJob.
  */
  private class DocumentRenderer implements Printable
  {
    protected int currentPage_ = -1;              //Used to keep track of when
                                                  //the page to print changes.

    protected JEditorPane jeditorPane_;           //Container to hold the
                                                  //Document. This object will
                                                  //be used to lay out the
                                                  //Document for printing.

    protected double pageEndY_ = 0;               //Location of the current page
                                                  //end.

    protected double pageStartY_ = 0;             //Location of the current page
                                                  //start.

    protected boolean scaleWidthToFit_ = true;    //boolean to allow control over
                                                  //whether pages too wide to fit
                                                  //on a page will be scaled.
    private int viewsPainted_;

    private Graphics printGraphics_;
    
    /**
     * Construct a document renderer that uses an
     * awt PrinterJob and PageFormat for classic awt printing.
     */
    public DocumentRenderer()
    {
      setDocument(AnyDocument.this.getDocument());
    }

    /**
     * Construct a document renderer for jps printing.
     * Only the print(OutputStream) method may be called
     * when DocumentRenderer is constructed in this way.
    public DocumentRenderer()//(OutputStream ostream)
    {
      setDocument(AnyDocument.this.getDocument());
    }
     */
    
    /**
     * Method to get the current Document
     */
    public Document getDocument()
    {
      if (jeditorPane_ != null)
        return jeditorPane_.getDocument();
      else
        return null;
    }

    /**
     * Get the current choice the width scaling option.
     */
    public boolean getScaleWidthToFit()
    {
      return scaleWidthToFit_;
    }

    /**
     * pageDialog() displays a page setup dialog.
    public void pageDialog()
    {
      pFormat_ = pJob_.pageDialog(pFormat_);
    }
     */

    /**
      The print method implements the Printable interface. Although Printables
      may be called to render a page more than once, each page is painted in
      order. We may, therefore, keep track of changes in the page being rendered
      by setting the currentPage_ variable to equal the pageIndex, and then
      comparing these variables on subsequent calls to this method. When the two
      variables match, it means that the page is being rendered for the second or
      third time. When the currentPage_ differs from the pageIndex, a new page is
      being requested.

      The highlights of the process used print a page are as follows:

      I.    The Graphics object is cast to a Graphics2D object to allow for
            scaling.
      II.   The JEditorPane is laid out using the width of a printable page.
            This will handle line breaks. If the JEditorPane cannot be sized at
            the width of the graphics clip, scaling will be allowed.
      III.  The root view of the JEditorPane is obtained. By examining this root
            view and all of its children, printView will be able to determine
            the location of each printable element of the document.
      IV.   If the scaleWidthToFit_ option is chosen, a scaling ratio is
            determined, and the graphics2D object is scaled.
      V.    The Graphics2D object is clipped to the size of the printable page.
      VI.   currentPage_ is checked to see if this is a new page to render. If so,
            pageStartY_ and pageEndY_ are reset.
      VII.  To match the coordinates of the printable clip of graphics2D and the
            allocation rectangle which will be used to lay out the views,
            graphics2D is translated to begin at the printable X and Y
            coordinates of the graphics clip.
      VIII. An allocation Rectangle is created to represent the layout of the
            Views.

            The Printable Interface always prints the area indexed by reference
            to the Graphics object. For instance, with a standard 8.5 x 11 inch
            page with 1 inch margins the rectangle X = 72, Y = 72, Width = 468,
            and Height = 648, the area 72, 72, 468, 648 will be painted regardless
            of which page is actually being printed.

            To align the allocation Rectangle with the graphics2D object two
            things are done. The first step is to translate the X and Y
            coordinates of the graphics2D object to begin at the X and Y
            coordinates of the printable clip, see step VII. Next, when printing
            other than the first page, the allocation rectangle must start laying
            out in coordinates represented by negative numbers. After page one,
            the beginning of the allocation is started at minus the page end of
            the prior page. This moves the part which has already been rendered to
            before the printable clip of the graphics2D object.

      X.    The printView method is called to paint the page. Its return value
            will indicate if a page has been rendered.

      Although public, print should not ordinarily be called by programs other
      than PrinterJob.
     */
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
    {
//      System.out.println("PageFormat: " + pageFormat.getImageableX() + " " +
//                         pageFormat.getImageableY() + " " +
//                         pageFormat.getImageableWidth() + " " +
//                         pageFormat.getImageableHeight());
      printGraphics_ = graphics;
      double scale = 1.0;
      Graphics2D graphics2D;
      View rootView;

      //  I
      graphics2D = (Graphics2D) graphics;

      //  II
      jeditorPane_.setSize((int)pageFormat.getImageableWidth(),
                          Integer.MAX_VALUE);
      jeditorPane_.validate();

      //  III
      rootView = jeditorPane_.getUI().getRootView(jeditorPane_);

      //  IV
      if ((scaleWidthToFit_) &&
          (jeditorPane_.getMinimumSize().getWidth() >
           pageFormat.getImageableWidth()))
      {
        scale = pageFormat.getImageableWidth()/
        jeditorPane_.getMinimumSize().getWidth();
        graphics2D.scale(scale,scale);
      }

      //  V
      graphics2D.setClip((int) (pageFormat.getImageableX()      / scale),
                         (int) (pageFormat.getImageableY()      / scale),
                         (int) (pageFormat.getImageableWidth()  / scale),
                         (int) (pageFormat.getImageableHeight() / scale));

      //System.out.println("Print page: " + pageIndex);

      //  VI
      if (pageIndex > currentPage_)
      {
        currentPage_ = pageIndex;
        pageStartY_ += pageEndY_;
        pageEndY_ = graphics2D.getClipBounds().getHeight();
      }

      //  VII
      graphics2D.translate(graphics2D.getClipBounds().getX(),
                           graphics2D.getClipBounds().getY());

      //  VIII
      Rectangle allocation = new Rectangle(0,
                                           (int) -pageStartY_,
                                           (int) (jeditorPane_.getMinimumSize().getWidth()),
                                           (int) (jeditorPane_.getPreferredSize().getHeight()));

      viewsPainted_ = 0;
      
      //  X
      if (printView(graphics2D, allocation, rootView))
      {
        printGraphics_ = null;
        return Printable.PAGE_EXISTS;
      }
      else
      {
        pageStartY_ = 0;
        pageEndY_ = 0;
        currentPage_ = -1;
        printGraphics_ = null;
        return Printable.NO_SUCH_PAGE;
      }
    }
    
    /**
     * A protected method, printDialog(), displays the print dialog and initiates
     * printing in response to user input.
    protected void printDialog()
    {
      if (pJob_.printDialog())
      {
        pJob_.setPrintable(this, pFormat_);
        try
        {
          pJob_.print();
        }
        catch (PrinterException printerException)
        {
          pageStartY_ = 0;
          pageEndY_ = 0;
          currentPage_ = -1;
          System.out.println("Error Printing Document");
        }
      }
    }
     */

    /**
     * A recursive method which iterates through the tree structure
     * of the view sent to it. If the view sent to printView is a branch view,
     * that is one with children, the method calls itself on each of these
     * children. If the view is a leaf view, that is a view without children which
     * represents an actual piece of text to be painted, printView attempts to
     * render the view to the Graphics2D object.
     *
     * I.    When any view starts after the beginning of the current printable
     *       page, this means that there are pages to print and the method sets
     *       pageExists to true.
     * II.   When a leaf view is taller than the printable area of a page, it
     *       cannot, of course, be broken down to fit a single page. Such a View
     *       will be printed whenever it intersects with the Graphics2D clip.
     * III.  If a leaf view intersects the printable area of the graphics clip and
     *       fits vertically within the printable area, it will be rendered.
     * IV.   If a leaf view does not exceed the printable area of a page but does
     *       not fit vertically within the Graphics2D clip of the current page, the
     *       method records that this page should end at the start of the view.
     *       This information is stored in pageEndY_.
     */
    protected boolean printView(Graphics2D graphics2D,
                                Shape      allocation,
                                View       view)
    {
      boolean   pageExists      = false;
      Rectangle clipRectangle   = graphics2D.getClipBounds();
      Shape     childAllocation;
      View      childView;

      Element elem = view.getElement();

      // table rows have children but are nonetheless
      // printable - they draw a border if there is one
      if (view.getViewCount() > 0 &&
          !elem.getName().equals("row"))
      {
        for (int i = 0; i < view.getViewCount(); i++)
        {
          childAllocation = view.getChildAllocation(i,allocation);
          if (childAllocation != null)
          {
            childView = view.getView(i);
            if (printView(graphics2D, childAllocation, childView))
            {
              pageExists = true;
            }
          }
        }
      }
      else
      {
        //  I
        if (allocation.getBounds().getMaxY() >= clipRectangle.getY())
        {
          pageExists = true;
          
          // In case we broke the page early don't paint anything further...
          if (allocation.getBounds().getY() >= pageEndY_)
            return pageExists;

          //  II
          if ((allocation.getBounds().getHeight() > clipRectangle.getHeight()) &&
              (allocation.intersects(clipRectangle)))
          {
            view.paint(graphics2D,allocation);
            viewsPainted_++;
          }
          else
          {
            //  III
            if (allocation.getBounds().getY() >= clipRectangle.getY())
            {
              if (allocation.getBounds().getMaxY() <= clipRectangle.getMaxY())
              {
                if (!view.getElement().getName().equals(StyledEditorKit.PAGEBREAK))
                {
                  view.paint(graphics2D,allocation);
                  viewsPainted_++;
                }
                else
                {
                  //System.out.println("PageBreak");
                  if (viewsPainted_ == 0) // if pb is at top of page just skip it
                    return pageExists;
                  pageEndY_ = allocation.getBounds().getY() + 1; // force page break and step over pb view
                }
              }
              else
              {
                //  IV
                if (allocation.getBounds().getY() < pageEndY_)
                {
                  pageEndY_ = allocation.getBounds().getY();
                }
              }
            }
          }
        }
      }
      return pageExists && viewsPainted_ != 0;
    }

    /**
     * Set the content type the JEditorPane.
     */
    protected void setContentType(String type)
    {
      jeditorPane_.setContentType(type);
    }

    /**
     * Method to set an HTMLDocument as the Document to print.
     */
    public void setDocument(HTMLDocument htmlDocument)
    {
      jeditorPane_ = new JEditorPane();
      setDocument("text/html",htmlDocument);
    }

    /**
     * Set the Document to print as the one contained in a JEditorPane.
     * This method is useful when Java does not provide direct access to a
     * particular Document type, such as a Rich Text Format document. With this
     * method such a document can be sent to the DocumentRenderer class enclosed
     * in a JEditorPane.
     */
    public void setDocument(JEditorPane jedPane)
    {
      jeditorPane_ = createEditorPane();
      setDocument(jedPane.getContentType(),jedPane.getDocument());
    }

    /**
     * Method to set a PlainDocument as the Document to print.
     */
    public void setDocument(PlainDocument plainDocument)
    {
      jeditorPane_ = createEditorPane();
      setDocument("text/plain",plainDocument);
    }

    /**
     * Set the content type and document of the JEditorPane.
     */
    protected void setDocument(String type, Document document)
    {
      setContentType(type);
      jeditorPane_.setDocument(document);
    }

    /**
     * Set the document of the JEditorPane.
     */
    protected void setDocument(Document document)
    {
      jeditorPane_ = createEditorPane();
      jeditorPane_.setDocument(document);
    }

    /**
     * Set the current choice of the width scaling option.
     */
    public void setScaleWidthToFit(boolean scaleWidth)
    {
      scaleWidthToFit_ = scaleWidth;
    }
    
    private JEditorPane createEditorPane()
    {
      // Create a TextPane that can access the current printing
      // graphics.  The graphics are used by custom views that
      // access the font metrics when sizing fixed cells, etc
      return new com.inqwell.any.client.swing.JTextPane()
      {
        public Graphics getGraphics()
        {
          return printGraphics_;
        }
      };
    }
  }
}
