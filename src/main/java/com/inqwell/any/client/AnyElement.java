/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/AnyElement.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.client;

import java.io.ObjectStreamException;
import java.util.NoSuchElementException;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;

import com.inqwell.any.AbstractIter;
import com.inqwell.any.AbstractMap;
import com.inqwell.any.Any;
import com.inqwell.any.AnyInt;
import com.inqwell.any.AnyString;
import com.inqwell.any.Array;
import com.inqwell.any.DegenerateIter;
import com.inqwell.any.FieldNotFoundException;
import com.inqwell.any.Iter;
import com.inqwell.any.Map;
import com.inqwell.any.ObjectI;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.Vectored;
import com.inqwell.any.client.swing.StyledDocument;
import com.inqwell.any.client.swing.SwingInvoker;


/**
 * Subsume a Swing text element into the Inq environment. This class
 * wraps an instance of <code>javax.swing.text.Element</code> providing
 * access to it and its element content via Inq node references and acting
 * as the required type for any element-specific operations.
 * <p>
 * Although a map, it does not contain anything in its own right.
 * As according to the Swing element interface, every element has a
 * name and this name is used to get elements from the 'Map'.
 * <p>
 * Vectored access is also supported, as an Element is defined
 * as having zero or more children accessible by index.
 * <p>
 * The mutation methods are supported.  If an element is
 * removed then the parent document is maniuplated with
 * the <code>Document.remove()</code> method, according
 * to the start and end offsets the element being removed contains.
 * Elements cannot be added directly. They are created by
 * the underlying <code>Document</code> implementation as
 * document insertions are carried out.  The
 * various <code>add</code> methods are supported but, rather than
 * elements, the value supplied must be a map containing the
 * various well-known keys for styled content to be rendered.
 * <p>
 * Since the smallest level of granularity currently supported
 * is the element, rather than positions within elements,
 * overall document support is limited to what ever the
 * underling element structure is, for example paragraphs in
 * <code>DefautStyledDocument</code>s.  This makes it
 * possible to add new content at the end of documents and
 * trim it from the beginning for things like scrolling windows
 * which should have a finite history size:
 * <code><pre>
 *
 *      &lt;add-to&gt;
 *        &lt;call name="render-text"/&gt;
 *        &lt;any value="textpane.model.root[last]"/&gt;
 *      &lt;/add-to&gt;
 *      &lt;remove-from select="textpane.model.root[first]"/&gt;
 *
 * </pre></code>
 * As well, if the document supports HTML and the structure is
 * known then the following node paths are examples of document
 * manipulation:
 * <code><pre>
 *
 *      &lt;add-to&gt;
 *        &lt;call name="render-text"/&gt;
 *        &lt;any value="textpane.model*body[last]"/&gt;
 *      &lt;/add-to&gt;
 *      &lt;remove-from select="textpane.model.body[first]"/&gt;
 *
 * </pre></code>
 * <p>
 * Further Document support will be added in a future release.
 * <p>
 * The contents of the document, and therefore any given element
 * contained within it, can vary according to insertions carried
 * out by GUI operations etc. Instances of AnyElement returned by the
 * retrieval methods are always new references.
 *
 * @see AnyDocument
 */
public class AnyElement extends    AbstractMap
                        implements Map,
                                   Vectored
{
  private Element   e_;

  private AnyInt    index_;

	public AnyElement(Element e)
	{
    e_ = e;
	}

  public Any get (Any key)
  {
    Element e;
    if ((e = findByName(key)) != null)
      return new AnyElement(e);

		throw new FieldNotFoundException("AnyElement.get() key is " + key);
  }

  public Any getIfContains(Any key)
  {
    Element e;
    if ((e = findByName(key)) != null)
      return new AnyElement(e);
    else
    {
      return null;
    }
  }

  public void removeByVector (int at)
  {
    Element e = findByVector(at);
    ElementRemover er = new ElementRemover(e);
		er.maybeSync();
  }

  public void removeByVector (Any at)
  {
    if (index_ == null)
      index_ = new AnyInt();

    index_.copyFrom(at);

    removeByVector(index_.getValue());
  }

  public void removeInParent()
  {
    ElementRemover er = new ElementRemover(e_);
		er.maybeSync();
	}

  public void empty()
  {
	  // To empty from this point we remove ourselves from
	  // the document
    ElementRemover er = new ElementRemover(e_);
		er.maybeSync();
	}
  
  public int indexOf(Any a)
  {
    return findIndexByName(a);
  }

  public void reverse()
  {
    throw new UnsupportedOperationException();
  }

  public Any getByVector (int at)
  {
    return new AnyElement(findByVector(at));
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
  
  public boolean contains (Any key)
  {
    return findByName(key) != null;
  }

  /**
   * Place the item given by <code>value</code> in the
   * document after the end position of the last child
   * element.
   */
  public void addByVector(Any value)
  {
    insertDocument(-1, value, AnyDocument.AFTER);
  }

  /**
   * Place the item given by <code>value</code> in the
   * document before the start position of the child element
   * at the given index.
   */
  public void addByVector(int at, Any value)
  {
    insertDocument(at, value, AnyDocument.BEFORE);
  }

  /**
   * Place the item given by <code>value</code> in the
   * document before the start position of the child element
   * at the given index.
   * <p>
   * This method behaves exactly
   * as <code>addByVector(at, value)</code>. The <code>key</code>
   * is ignored as this implementation does not, itself,
   * contain anything.
   */
  public void addByVector(int at, Any key, Any value)
  {
    addByVector(at, value);
  }

  public Array initOrderBacking()
  {
    throw new UnsupportedOperationException();
  }
  
  public void setSparse(boolean isSparse)
  {
    throw new UnsupportedOperationException();
  }

  public boolean isEmpty()
  {
    return e_.isLeaf() || (e_.getElementCount() == 0);
  }

  public int entries()
  {
    return e_.getElementCount();
  }

  public Object[] toArray()
  {
    throw new UnsupportedOperationException();
  }
  
  public Iter createIterator()
  {
    if (e_.isLeaf() || e_.getElementCount() == 0)
      return DegenerateIter.i__;
    else
      return new AnyElementIter();
  }
  
  public Any copyFrom (Any a)
  {
    return this;
  }
  
  /**
   * Return the text represented by this element
   */
  public String toString()
  {
		int length = e_.getDocument().getLength();
		int end    = e_.getEndOffset();
		if (end > length)
		  end = length;

		String ret = null;
		try
		{
			ret = e_.getDocument().getText(e_.getStartOffset(), end - e_.getStartOffset());
		}
		catch (BadLocationException e)
		{
			throw new RuntimeContainedException(e);
		}
		
		return ret;
  }

  /**
   * Serialize <code>AnyElement</code> objects.  Instances
   * of <code>AnyElement</code> are serialized as
   * an <code>AnyString</code> initialised to the text
   * content represented by this element.
   */
	protected Object writeReplace() throws ObjectStreamException
	{
		AnyString s = new AnyString(this.toString());

		return s;
	}

	protected boolean beforeAdd(Any key, Any value) { return true; }
	protected void afterAdd(Any key, Any value) {}
	protected void beforeRemove(Any key) {}
	protected void afterRemove(Any key, Any value) {}
	protected void emptying() {}

  private Element findByName(Any name)
  {
    int entries = e_.getElementCount();

    for (int i = 0; i < entries; i++)
    {
      Element e = e_.getElement(i);
      if (e.getName().equals(name.toString()))
        return e;
    }
    return null;
  }

  private int findIndexByName(Any name)
  {
    int entries = e_.getElementCount();

    for (int i = 0; i < entries; i++)
    {
      Element e = e_.getElement(i);
      if (e.getName().equals(name.toString()))
        return i;
    }
    return -1;
  }

  private void insertDocument(int position, Any item, int loc)
  {
	  DocumentInserter i = new DocumentInserter(position, item, loc);
	  i.maybeSync();
	}

  private Element findByVector(int vector)
  {
    if (vector < 0 || vector >= e_.getElementCount())
      throw new ArrayIndexOutOfBoundsException("AnyElement.findByVector(): " + vector);

    return e_.getElement(vector);
  }

  private class DocumentInserter extends SwingInvoker
  {
	  private int     position_;
	  private Any     item_;
	  private Element thisE_;
	
	  DocumentInserter(int vectorPosition, Any item, int loc)
	  {
		  item_     = item;

		  if (vectorPosition < 0)
        thisE_ = e_.getElement(e_.getElementCount() - 1);
      else
        thisE_ = e_.getElement(vectorPosition);

      if (loc == AnyDocument.AFTER)
		    position_ = thisE_.getEndOffset();
		  else
		    position_ = thisE_.getStartOffset();
		}
		
		protected void doSwing()
		{
	    try
	    {
        int length = thisE_.getDocument().getLength();
        if (position_ > length)
          position_ = length;
          
		    String string = AnyDocument.getString(item_);
		    if (string != null)
		    {
          AttributeSet style = AnyDocument.getStyle(item_);
          //System.out.println ("inserting " + string + " as " + style + " at " + position_);
          //System.out.println ("doc length is " + length);
          /*DefaultStyledDocument.ElementSpec esContent = new DefaultStyledDocument.ElementSpec(style, DefaultStyledDocument.ElementSpec.ContentType, string.toCharArray(), 0, string.length());
          StyledDocument sd = (StyledDocument)thisE_.getDocument();
          DefaultStyledDocument.ElementSpec[] es =
            {TableDef.esSectionStart__,
             esContent,
             TableDef.esSectionEnd__};
          if (length == 0)
            sd.create(es);
          else
            sd.insert(position_, es);*/

          thisE_.getDocument().insertString(position_,
                                            string,
                                            style);
        }
        else
        {
          // Assume new content is an element specification
          // array contained within an AnyObject
          ObjectI o = (ObjectI)item_;
          DefaultStyledDocument.ElementSpec[] es = 
            (DefaultStyledDocument.ElementSpec[])o.getValue();
          StyledDocument sd = (StyledDocument)thisE_.getDocument();
          //if (length == 0)
            //sd.create(es);
          //else
            sd.insert(position_, es);
        }
	    }
			catch(Exception e)
			{
			  throw new RuntimeContainedException(e);
			}
		}
  }


	private static class ElementRemover extends SwingInvoker
	{
	  Element e_;

    // Remove a specific child element
	  ElementRemover(Element e)
	  {
      e_ = e;
    }

		protected void doSwing()
		{
      try
      {
		    int length = e_.getDocument().getLength();
		    int end    = e_.getEndOffset();
		    if (end > length)
		      end = length;
		
        e_.getDocument().remove(e_.getStartOffset(), end - e_.getStartOffset());
      }
      catch (Exception e)
      {
        throw new RuntimeContainedException(e);
      }
		}
  }

  // We only create one of these if the element is not a leaf
	private class AnyElementIter extends AbstractIter implements Iter
	{
	  private boolean hasNext_;
	  private int     count_;

	  public AnyElementIter()
	  {
	    count_ = 0;
	    hasNext_ = count_ < AnyElement.this.e_.getElementCount();
	  }

    public boolean hasNext()
    {
      return hasNext_;
    }

    public Any next()
    {
      if (!hasNext_)
        throw new NoSuchElementException();

      Any ret = AnyElement.this.getByVector(count_++);
      hasNext_ = count_ < AnyElement.this.e_.getElementCount();

      return ret;
    }

    public void remove()
    {
      // count has already been bumped
      AnyElement.this.removeByVector(--count_);
    }
  }
}
