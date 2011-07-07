/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


/*
 * $Archive: /src/com/inqwell/any/client/ListenerAdapterFactory.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-05-02 20:30:13 $
 */

package com.inqwell.any.client;

import com.inqwell.any.*;
import com.inqwell.any.beans.*;
import com.inqwell.any.client.swing.DialogListener;
import com.inqwell.any.client.swing.DialogEvent;
import com.inqwell.any.client.swing.FileChooserEvent;
import com.inqwell.any.client.swing.TableEditEvent;
import javax.swing.tree.TreePath;

/**
 * This class maintains a register of event names and their listeners
 * which convert external events to com.inqwell events
 */
public class ListenerAdapterFactory extends AbstractAny
{
	// Maps listener types, given by
	static Map adapters__ = AbstractComposite.map();
  static public  Any   selected__    = new ConstInt(java.awt.event.ItemEvent.SELECTED);
  static public  Any   deSelected__  = new ConstInt(java.awt.event.ItemEvent.DESELECTED);

  // Constants used for additional event information while passing
  // through
  static public  Any   stateChange__   = AbstractValue.flyweightString("stateChange");
  static public  Any   isAdjusting__   = AbstractValue.flyweightString("isAdjusting");
  static public  Any   isTemporary__   = AbstractValue.flyweightString("isTemporary");
  static public  Any   actionCommand__ = AbstractValue.flyweightString("actionCommand");

	static
	{
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.ACTION,
											new ActionListenerAdapter(ListenerConstants.ACTION));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.FOCUS,
											new FocusListenerAdapter(ListenerConstants.FOCUS));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.MENU,
											new MenuListenerAdapter(ListenerConstants.MENU));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.POPUPMENU,
											new PopupMenuListenerAdapter(ListenerConstants.POPUPMENU));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.MOUSE,
											new MouseListenerAdapter(ListenerConstants.MOUSE));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.MOUSEMOTION,
											new MouseMotionListenerAdapter(ListenerConstants.MOUSEMOTION));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.KEY,
											new KeyListenerAdapter(ListenerConstants.KEY));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.WINDOW,
											new WindowListenerAdapter(ListenerConstants.WINDOW));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.IWINDOW,
											new IWindowListenerAdapter(ListenerConstants.IWINDOW));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.LISTSELECTION,
											new SelectionListenerAdapter(ListenerConstants.LISTSELECTION));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.DOCUMENT,
											new DocumentListenerAdapter(ListenerConstants.DOCUMENT));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.DIALOG,
											new DialogListenerAdapter(ListenerConstants.DIALOG));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.TABLEEDIT,
											new TableEditListenerAdapter(ListenerConstants.TABLEEDIT));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.CONTEXT,
											new ContextListenerAdapter(ListenerConstants.CONTEXT));
//    ListenerAdapterFactory.addAdapter
//										 (ListenerConstants.SPINEDIT,
//											new SpinEditListenerAdapter(ListenerConstants.SPINEDIT));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.FILECHOOSER,
											new FileChooserListenerAdapter(ListenerConstants.FILECHOOSER));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.ITEM,
											new ItemListenerAdapter(ListenerConstants.ITEM));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.CHANGE,
											new ChangeListenerAdapter(ListenerConstants.CHANGE));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.TREESELECTION,
											new TreeSelectionListenerAdapter(ListenerConstants.TREESELECTION));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.TREEEXPANSION,
											new TreeExpansionListenerAdapter(ListenerConstants.TREEEXPANSION));
    ListenerAdapterFactory.addAdapter
										 (ListenerConstants.TREEWILLEXPAND,
											new TreeWillExpandListenerAdapter(ListenerConstants.TREEWILLEXPAND));
	}

	public static ListenerAdapter getAdapter(Any       eventType,
                                           Composite preferredListenerTypes)
	{
		// Look for a listener implementation that supports the given
		// event type and return the first one such
		//System.out.println ("getAdapter looking for " + eventType + " in " + adapters__);
		Iter i = adapters__.createIterator();
		while (i.hasNext())
		{
			Any a = i.next();
			ListenerAdapter l = (ListenerAdapter)a;
			if (l.isSupported(eventType))
			{
        if (preferredListenerTypes != null &&
            !preferredListenerTypes.contains(l.eventCategory()))
          continue;
				//System.out.println ("found " + a + " for eventType " + eventType);
				return (ListenerAdapter)a.cloneAny();
			}
		}

		//System.out.println ("getAdapter couldn't find " + eventType);
		throw new IllegalArgumentException
			("No listener available for event type " + eventType);
	}

	/**
	 * Map an event type to an adapter which must be of the appropriate
	 * interface for the external system it will listen to, as well
	 * as being an Any EventGenerator.
	 */
	public static void addAdapter(Any eventType, Any eg)
	{
	  if (!adapters__.contains(eventType))
	    adapters__.add(eventType, eg);
	}
}

abstract class BaseListenerAdapter extends    AbstractAny
																	 implements ListenerAdapter,
																							Cloneable
{
	// where we will send the adapted event to
  private ListenerAdaptee adaptee_;

  private Any eventCategory_;

	protected BaseListenerAdapter(Any eventCategory)
	{
		eventCategory_ = eventCategory;
	}

  public void notifyAdaptee(Event e)
  {
    adaptee_.adaptEvent(e);
	}

  public void setAdaptee(ListenerAdaptee l)
  {
		adaptee_ = l;
  }

  protected boolean checkIfSupported(Any eventType, Array eventTypes)
  {
		if (eventTypes.indexOf(eventType) >= 0)
			return true;
		else
			return false;
	}

  public Any eventCategory()
  {
		return eventCategory_;
	}

  public Object clone() throws CloneNotSupportedException
  {
		// in the prototype the adaptee_ is never set, so is
		// already null.  The eventTypes_ can be shared, so we
		// only really need to implement the interface since
		// clone() is called from cloneAny()
		return super.clone();
	}
}

/**
 * The ActionListener adapter
 */
class ActionListenerAdapter extends    BaseListenerAdapter
														implements java.awt.event.ActionListener
{
  // The event types this adapter can generate.  If we are simple, like
  // an ActionListener, then we can only generate one type of event.  But
  // other listeners, like WindowListener, can generate many event types
  // (e.g. iconified, closed etc) each of which results in a separate
  // Inq event.
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		//System.out.println ("ActionListenerAdapter: static initialiser " + eventTypes_);
		eventTypes_.add(EventConstants.E_ACTION);
		//System.out.println ("ActionListenerAdapter: static initialiser " + eventTypes_);
	}

	public ActionListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	public void actionPerformed(java.awt.event.ActionEvent e)
	{
	  //System.out.println("ActionListenerAdapter.actionPerformed(): event is " + e);
	  //System.out.println("I am " + this);
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.E_ACTION);
    String ac = e.getActionCommand();
    if (ac != null)
    {
      anyEvt.add(ListenerAdapterFactory.actionCommand__,
                 AbstractValue.flyweightString(ac));
    }

		notifyAdaptee(anyEvt);
	}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}
}

/**
 * The SelectionListenerAdapter adapter
 */
class SelectionListenerAdapter extends    BaseListenerAdapter
															 implements javax.swing.event.ListSelectionListener

{
  // The event types this adapter can generate.  If we are simple, like
  // an ActionListener, then we can only generate one type of event.  But
  // other listeners, like WindowListener, can generate many event types
  // (e.g. iconified, closed etc) each of which results in a separate
  // Inq event.
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.E_SELECTIONCHANGED);
	}

	public SelectionListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	public void valueChanged(javax.swing.event.ListSelectionEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.E_SELECTIONCHANGED);

    anyEvt.add(ListenerAdapterFactory.isAdjusting__,
               e.getValueIsAdjusting() ? AnyBoolean.TRUE
                                       : AnyBoolean.FALSE);

    // Leave the selection index for the time being - these are
    // available via the table wrapper itself.

		notifyAdaptee(anyEvt);
	}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}
}

/**
 * The FocusListener adapter.  This listener adapts the
 * focusGained and focusLost methods from the FocusListener
 * to the Inq event types E_FOCUSGAINED and E_FOCUSLOST
 */
class FocusListenerAdapter extends    BaseListenerAdapter
													 implements java.awt.event.FocusListener
{
  // The event types this adapter can generate.  If we are simple, like
  // an ActionListener, then we can only generate one type of event.  But
  // other listeners, like WindowListener, can generate many event types
  // (e.g. iconified, closed etc) each of which results in a separate
  // Inq event.
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.E_FOCUSGAINED);
		eventTypes_.add(EventConstants.E_FOCUSLOST);
	}

	public FocusListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	public void focusGained(java.awt.event.FocusEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.E_FOCUSGAINED);

    anyEvt.add(ListenerAdapterFactory.isTemporary__,
               e.isTemporary() ? AnyBoolean.TRUE
                               : AnyBoolean.FALSE);

		notifyAdaptee(anyEvt);
	}

	public void focusLost(java.awt.event.FocusEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.E_FOCUSLOST);

    anyEvt.add(ListenerAdapterFactory.isTemporary__,
               e.isTemporary() ? AnyBoolean.TRUE
                               : AnyBoolean.FALSE);

		notifyAdaptee(anyEvt);
	}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}
}

/**
 * The MenuListener adapter.  This listener adapts the
 * menuSelected, menuDeselected and menuCanceled methods
 * from the MenuListener to the Inq event types E_MENUSELECTED,
 * E_MENUDESELECTED and E_CANCELED.
 */
class MenuListenerAdapter extends    BaseListenerAdapter
													implements javax.swing.event.MenuListener
{
  // The event types this adapter can generate.  If we are simple, like
  // an ActionListener, then we can only generate one type of event.  But
  // other listeners, like WindowListener, can generate many event types
  // (e.g. iconified, closed etc) each of which results in a separate
  // Inq event.
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.MENU_SELECTED);
		eventTypes_.add(EventConstants.MENU_DESELECTED);
		eventTypes_.add(EventConstants.MENU_CANCELED);
	}

	public MenuListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	public void menuSelected(javax.swing.event.MenuEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.MENU_SELECTED);

		notifyAdaptee(anyEvt);
	}

	public void menuDeselected(javax.swing.event.MenuEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.MENU_DESELECTED);

		notifyAdaptee(anyEvt);
	}

	public void menuCanceled(javax.swing.event.MenuEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.MENU_CANCELED);

		notifyAdaptee(anyEvt);
	}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}
}

/**
 * The PopupMenuListener adapter.  This listener adapts the
 * menuSelected, menuDeselected and menuCanceled methods
 * from the MenuListener to the Inq event types E_POPUPVISIBLE,
 * E_POPUPINVISIBLE and E_POPUPCANCELED.
 */
class PopupMenuListenerAdapter extends    BaseListenerAdapter
                               implements javax.swing.event.PopupMenuListener
{
  // The event types this adapter can generate.  If we are simple, like
  // an ActionListener, then we can only generate one type of event.  But
  // other listeners, like WindowListener, can generate many event types
  // (e.g. iconified, closed etc) each of which results in a separate
  // Inq event.
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.POPUP_VISIBLE);
		eventTypes_.add(EventConstants.POPUP_INVISIBLE);
		eventTypes_.add(EventConstants.POPUP_CANCELED);
	}

	public PopupMenuListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.POPUP_VISIBLE);

		notifyAdaptee(anyEvt);
	}

	public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.POPUP_INVISIBLE);

		notifyAdaptee(anyEvt);
	}

	public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.POPUP_CANCELED);

		notifyAdaptee(anyEvt);
	}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}
}


/**
 * Provides base functionality for handling interest in
 * mouse and mouse motion events
 */
abstract class BaseMouseAdapter extends BaseListenerAdapter
{
  // Maps DecodedMouseEvents to inq event types
  private Map ofInterest_;

  private Map awtEvents_;  // read only static instance from sub-class

  private DecodedMouseEvent dme_;

	/**
	 * Define a class to decode a mouse event.  This is quicker
	 * than creating an event object, sending it to the adaptee
	 * and checking its event dispatcher, only to descover
	 * we are not interested in the event.
	 */
	protected class DecodedMouseEvent extends AbstractAny
	{
    // Spoof event id for popup trigger events. There is no event id
    // in awt of value "POPUP" as such, but it is convenient for
    // us to define one to make out handling of popups more cleanly
    // platform independent.
    static final int popupId__ = -501;

		int      id_;
		int      clickCount_;
		short    button_;
		boolean  alt_;
		boolean  ctrl_;
		boolean  shift_;
		boolean  altGraph_;
		boolean  meta_;
		boolean  popup_;

		DecodedMouseEvent() { clear(); }
		DecodedMouseEvent(Any eventType){ setEvent(eventType); }
		DecodedMouseEvent(java.awt.event.MouseEvent e){ setEvent(e); }

		/**
		 * Initialise from an awt event
		 */
		void setEvent(java.awt.event.MouseEvent e)
		{
			clear();

			id_ = e.getID();

			if (e.isPopupTrigger())
			{
				popup_ = true;
				id_    = popupId__;  // override system - can be press or release
			}
			else
			{
				// If its non a popup then unwind the buttons, i.e
				// buttons and popup trigger are mutually exclusive
				int modifiers = e.getModifiers();
				if ((modifiers & java.awt.event.InputEvent.BUTTON1_MASK) != 0)
					button_ = 1;
				else if ((modifiers & java.awt.event.InputEvent.BUTTON2_MASK) != 0)
					button_ = 2;
				else if ((modifiers & java.awt.event.InputEvent.BUTTON3_MASK) != 0)
					button_ = 3;
			}

			if (e.isAltDown())      alt_      = true;
			if (e.isControlDown())  ctrl_     = true;
			if (e.isShiftDown())    shift_    = true;
			if (!popup_)
			{
        // some curious behaviour of these when doing popups
        // so ignore if so
        if (e.isAltGraphDown()) altGraph_ = true;
        if (e.isMetaDown())     meta_     = true;
      }

			clickCount_ = e.getClickCount();
		}

		/**
		 * Initialise from an INQ event id
		 */
		void setEvent(Any eventType)
		{
			clear();

			// All inq mouse events are complex types
			Map em = (Map)eventType;

			// get the basic event type....
			Any et = em.get(EventConstants.EVENT_TYPE);
			// ...and set the id from it
			id_ = ((IntI)awtEvents_.get(et)).getValue();

			if (id_ == popupId__)
			{
				popup_ = true;
			}
			else
			{
        if (em.contains(EventConstants.MOD_MBUTTON)) button_ = ((ShortI)em.get(EventConstants.MOD_MBUTTON)).getValue();
				else if (em.contains(EventConstants.MOD_BUTTON1)) button_ = 1;
				else if (em.contains(EventConstants.MOD_BUTTON2)) button_ = 2;
				else if (em.contains(EventConstants.MOD_BUTTON3)) button_ = 3;
				else if ((id_ != java.awt.event.MouseEvent.MOUSE_ENTERED) &&
								 (id_ != java.awt.event.MouseEvent.MOUSE_EXITED) &&
								 (id_ != java.awt.event.MouseEvent.MOUSE_MOVED) &&
								 (id_ != java.awt.event.MouseEvent.MOUSE_DRAGGED)) button_ = 1;  // assume button 1 if none specified
			}

			// then check the keyboard modifiers
			if (em.contains(EventConstants.MOD_ALT))      alt_      = true;
			if (em.contains(EventConstants.MOD_CTRL))     ctrl_     = true;
			if (em.contains(EventConstants.MOD_SHIFT))    shift_    = true;
			if (!popup_)
			{
        if (em.contains(EventConstants.MOD_ALTGRAPH)) altGraph_ = true;
        if (em.contains(EventConstants.MOD_META))     meta_     = true;
      }

			if (em.contains(EventConstants.MOD_CLICKCOUNT))
			{
				IntI i = (IntI)em.get(EventConstants.MOD_CLICKCOUNT);
				clickCount_ = i.getValue();
			}
			else
			{
				clickCount_ = 0;
			}
		}

		public int hashCode()
		{
      // Note clickcount is a possible wildcard so we can't
      // hash it
			return (id_ + button_)
														 * (alt_      ?  2 : 1)
														 * (ctrl_     ?  4 : 1)
														 * (shift_    ?  8 : 1)
														 * (altGraph_ ? 16 : 1)
														 * (meta_     ? 32 : 1)
														 * (popup_    ? 64 : 1);
		}

		public boolean equals(Object o)
		{
			if (o == this)
				return true;
			if (!(o instanceof DecodedMouseEvent))
				return false;

			DecodedMouseEvent m = (DecodedMouseEvent)o;
      boolean ret =  (id_          == m.id_) &&
        						 (button_      == m.button_) &&
        						 (alt_         == m.alt_) &&
        						 (ctrl_        == m.ctrl_) &&
        						 (shift_       == m.shift_) &&
        						 (altGraph_    == m.altGraph_) &&
        						 (meta_        == m.meta_) &&
        						 (popup_       == m.popup_);

      // Click count of zero is a wild card
      if (clickCount_ != 0 && m.clickCount_ != 0)
        ret = ret && (clickCount_ == m.clickCount_);
      
      return ret;
		}

		public String toString()
		{
      return "id=" + id_ +
             " clickCount=" + clickCount_ +
             " button=" + button_ +
             " alt=" + alt_ +
             " ctrl=" + ctrl_ +
             " shift=" + shift_ +
             " altGraph=" + altGraph_ +
             " meta=" + meta_ +
             " popup=" + popup_;
		}

		private void clear()
		{
			id_         = 0;
			clickCount_ = 0;
			button_     = 0;
			alt_        = false;
			ctrl_       = false;
			shift_      = false;
			altGraph_   = false;
			meta_       = false;
			popup_      = false;
		}
	}

	protected BaseMouseAdapter(Any eventCategory,
                             Map awtEvents)
	{
		super(eventCategory);
		awtEvents_ = awtEvents;
	}

	protected void checkForNotify(java.awt.event.MouseEvent e)
	{
    //System.out.println("check for notify " + e);
    //System.out.println("isPopupTrigger " + e.isPopupTrigger());
		dme_.setEvent(e);
    //System.out.println("ofInterest " + ofInterest_);
    //System.out.println("dme " + dme_);
		if (ofInterest_.contains(dme_))
		{
			AnyEvent ev = new AnyEvent(e, ofInterest_.get(dme_));
      //System.out.println("NOTIFYING " + ev);
			notifyAdaptee(ev);
		}
	}

  public void hasInterest(Any eventType)
  {
		DecodedMouseEvent m = new DecodedMouseEvent(eventType);
		if (!ofInterest_.contains(m))
		{
			ofInterest_.add(m, eventType);
		}
  }

  public void hasNoInterest(Any eventType)
  {
		DecodedMouseEvent m = new DecodedMouseEvent(eventType);
		if (ofInterest_.contains(m))
		{
			ofInterest_.remove(m);
		}
	}

  public Object clone() throws CloneNotSupportedException
  {
		BaseMouseAdapter b = (BaseMouseAdapter)super.clone();
    b.ofInterest_ = AbstractComposite.map();
	  b.dme_        = new DecodedMouseEvent();
	  return b;
	}
}

/**
 * The MouseListener adapter.  Mouse events are of several broad
 * types (press, release etc) and further characterised my any
 * modifiers present.  The INQ framework classes each and any
 * modifiers as a distinct event type, represented by a Map.
 */
class MouseListenerAdapter extends    BaseMouseAdapter
													 implements java.awt.event.MouseListener
{
	// inq event types
  static private Array eventTypes_ = AbstractComposite.array();

	// maps inq types to awt types
  static private Map   awtEvents__ = AbstractComposite.map();

	static
	{
		eventTypes_.add(EventConstants.M_CLICKED);
		eventTypes_.add(EventConstants.M_ENTERED);
		eventTypes_.add(EventConstants.M_EXITED);
		eventTypes_.add(EventConstants.M_PRESSED);
		eventTypes_.add(EventConstants.M_RELEASED);
		eventTypes_.add(EventConstants.M_POPUP);

		awtEvents__.add(EventConstants.M_CLICKED,
									 new ConstInt(java.awt.event.MouseEvent.MOUSE_CLICKED));
		awtEvents__.add(EventConstants.M_ENTERED,
									 new ConstInt(java.awt.event.MouseEvent.MOUSE_ENTERED));
		awtEvents__.add(EventConstants.M_EXITED,
									 new ConstInt(java.awt.event.MouseEvent.MOUSE_EXITED));
		awtEvents__.add(EventConstants.M_PRESSED,
									 new ConstInt(java.awt.event.MouseEvent.MOUSE_PRESSED));
		awtEvents__.add(EventConstants.M_RELEASED,
									 new ConstInt(java.awt.event.MouseEvent.MOUSE_RELEASED));
		awtEvents__.add(EventConstants.M_POPUP,
									 new ConstInt(DecodedMouseEvent.popupId__));
	}

	public MouseListenerAdapter(Any eventGategory)
	{
		super(eventGategory, awtEvents__);
	}

  /**
   * Invoked when the mouse enters a component.
   */
  public void mouseEntered(java.awt.event.MouseEvent e)
  {
		checkForNotify(e);
  }

  /**
   * Invoked when the mouse exits a component.
   */
  public void mouseExited(java.awt.event.MouseEvent e)
  {
		checkForNotify(e);
  }

  /**
   * Invoked when the mouse has been clicked on a component.
   */
  public void mouseClicked(java.awt.event.MouseEvent e)
  {
		checkForNotify(e);
  }


  /**
   * Invoked when a mouse button has been pressed on a component.
   */
  public void mousePressed(java.awt.event.MouseEvent e)
  {
		checkForNotify(e);
  }


  /**
   * Invoked when a mouse button has been released on a component.
   */
  public void mouseReleased(java.awt.event.MouseEvent e)
  {
		checkForNotify(e);
  }

  public boolean isSupported(Any eventType)
  {
		Any et = eventType;

		// All mouse events should be maps
		if (!(eventType instanceof Map))
			return false;

		Map m = (Map)eventType;
		et    = m.get(EventConstants.EVENT_TYPE);

		return checkIfSupported(et, eventTypes_);
  }
}

class MouseMotionListenerAdapter extends    BaseMouseAdapter
													       implements java.awt.event.MouseMotionListener
{
	// inq event types
  static private Array eventTypes_ = AbstractComposite.array();

	// maps inq types to awt types
  static private Map   awtEvents__ = AbstractComposite.map();

	static
	{
		eventTypes_.add(EventConstants.M_MOVED);
		eventTypes_.add(EventConstants.M_DRAGGED);

		awtEvents__.add(EventConstants.M_MOVED,
									 new ConstInt(java.awt.event.MouseEvent.MOUSE_MOVED));
		awtEvents__.add(EventConstants.M_DRAGGED,
									 new ConstInt(java.awt.event.MouseEvent.MOUSE_DRAGGED));
	}

	public MouseMotionListenerAdapter(Any eventGategory)
	{
		super(eventGategory, awtEvents__);
	}

  /**
   * Invoked when the mouse enters a component.
   */
  public void mouseMoved(java.awt.event.MouseEvent e)
  {
		checkForNotify(e);
  }

  /**
   * Invoked when the mouse exits a component.
   */
  public void mouseDragged(java.awt.event.MouseEvent e)
  {
		checkForNotify(e);
  }

  public boolean isSupported(Any eventType)
  {
		Any et = eventType;

		// All mouse events should be maps
		if (!(eventType instanceof Map))
			return false;

		Map m = (Map)eventType;
		et    = m.get(EventConstants.EVENT_TYPE);

		return checkIfSupported(et, eventTypes_);
  }
}

/**
 * The KeyListener adapter.  Key events are also of several broad
 * types (press, release etc) and further characterised my any
 * modifiers present.  The INQ framework classes each and any
 * modifiers as a distinct event type, represented by a Map.
 */
class KeyListenerAdapter extends    BaseListenerAdapter
												 implements java.awt.event.KeyListener
{
	// inq event types
  static private Array eventTypes_ = AbstractComposite.array();

	// maps inq types to awt types
  static private Map   awtEvents_ = AbstractComposite.map();

  // Maps DecodedMouseEvents to inq event types
  private Map ofInterest_ = AbstractComposite.map();

	DecodedKeyEvent dke_ = new DecodedKeyEvent();

	static
	{
		eventTypes_.add(EventConstants.K_TYPED);
		eventTypes_.add(EventConstants.K_PRESSED);
		eventTypes_.add(EventConstants.K_RELEASED);

		awtEvents_.add(EventConstants.K_TYPED,
									 new ConstInt(java.awt.event.KeyEvent.KEY_TYPED));
		awtEvents_.add(EventConstants.K_PRESSED,
									 new ConstInt(java.awt.event.KeyEvent.KEY_PRESSED));
		awtEvents_.add(EventConstants.K_RELEASED,
									 new ConstInt(java.awt.event.KeyEvent.KEY_RELEASED));
	}

	/**
	 * Define a class to decode a mouse event.  This is quicker
	 * than creating an event object, sending it to the adaptee
	 * and checking its event dispatcher, only to descover
	 * we are not interested in the event.
	 */
	static class DecodedKeyEvent extends AbstractAny
	{
		int      id_;
		int      keyCode_;
		boolean  alt_;
		boolean  ctrl_;
		boolean  shift_;
		boolean  altGraph_;
		boolean  meta_;

		DecodedKeyEvent() { clear(); }
		DecodedKeyEvent(Any eventType){ setEvent(eventType); }
		DecodedKeyEvent(java.awt.event.KeyEvent e){ setEvent(e); }

		/**
		 * Initialise from an awt event
		 */
		void setEvent(java.awt.event.KeyEvent e)
		{
			clear();

			id_ = e.getID();

			if (e.isAltDown())      alt_      = true;
			if (e.isControlDown())  ctrl_     = true;
			if (e.isShiftDown())    shift_    = true;
			if (e.isAltGraphDown()) altGraph_ = true;
			if (e.isMetaDown())     meta_     = true;

			keyCode_ = e.getKeyCode();
		}

		/**
		 * Initialise from an INQ event id
		 */
		void setEvent(Any eventType)
		{
			clear();

			// All inq key events are complex types
			Map em = (Map)eventType;

			// get the basic event type....
			Any et = em.get(EventConstants.EVENT_TYPE);
			// ...and set the id from it
			id_ = ((IntI)awtEvents_.get(et)).getValue();

			// then check the keyboard modifiers
			if (em.contains(EventConstants.MOD_ALT))      alt_      = true;
			if (em.contains(EventConstants.MOD_CTRL))     ctrl_     = true;
			if (em.contains(EventConstants.MOD_SHIFT))    shift_    = true;
			if (em.contains(EventConstants.MOD_ALTGRAPH)) altGraph_ = true;
			if (em.contains(EventConstants.MOD_META))     meta_     = true;

			if (em.contains(EventConstants.MOD_KEYCODE))
			{
				IntI i = (IntI)em.get(EventConstants.MOD_KEYCODE);
				keyCode_ = i.getValue();
			}
			else
			{
				keyCode_ = java.awt.event.KeyEvent.VK_UNDEFINED;
			}
		}

		public int hashCode()
		{
			return (keyCode_)
														 * (alt_      ?  2 : 1)
														 * (ctrl_     ?  4 : 1)
														 * (shift_    ?  8 : 1)
														 * (altGraph_ ? 16 : 1)
														 * (meta_     ? 32 : 1);
		}

		public boolean equals(Object o)
		{
			if (o == this)
				return true;
			if (!(o instanceof DecodedKeyEvent))
				return false;

			DecodedKeyEvent k = (DecodedKeyEvent)o;
			boolean ret = (id_          == k.id_) &&
      						  (keyCode_     == k.keyCode_) &&
      						  (alt_         == k.alt_) &&
      						  (ctrl_        == k.ctrl_) &&
      						  (shift_       == k.shift_) &&
      						  (altGraph_    == k.altGraph_) &&
      						  (meta_        == k.meta_);

      // Key code of undefined is a wild card
      if (keyCode_   != java.awt.event.KeyEvent.VK_UNDEFINED &&
          k.keyCode_ != java.awt.event.KeyEvent.VK_UNDEFINED)
        ret = ret && (keyCode_ == k.keyCode_);
      
      return ret;
		}

		private void clear()
		{
			id_         = 0;
			keyCode_    = java.awt.event.KeyEvent.VK_UNDEFINED;
			alt_        = false;
			ctrl_       = false;
			shift_      = false;
			altGraph_   = false;
			meta_       = false;
		}
	}

	public KeyListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

  /**
   * Invoked when a key has been typed on a component.
   */
  public void keyTyped(java.awt.event.KeyEvent e)
  {
		checkForNotify(e);
  }


  /**
   * Invoked when a mouse button has been pressed on a component.
   */
  public void keyPressed(java.awt.event.KeyEvent e)
  {
		checkForNotify(e);
  }


  /**
   * Invoked when a mouse button has been released on a component.
   */
  public void keyReleased(java.awt.event.KeyEvent e)
  {
		checkForNotify(e);
  }

	private void checkForNotify(java.awt.event.KeyEvent e)
	{
		dke_.setEvent(e);
		if (ofInterest_.contains(dke_))
		{
			AnyEvent ev = new AnyEvent(e, ofInterest_.get(dke_));
			notifyAdaptee(ev);
		}
	}

  public void hasInterest(Any eventType)
  {
		DecodedKeyEvent k = new DecodedKeyEvent(eventType);
		if (!ofInterest_.contains(k))
		{
			ofInterest_.add(k, eventType);
		}
  }

  public void hasNoInterest(Any eventType)
  {
		DecodedKeyEvent k = new DecodedKeyEvent(eventType);
		if (ofInterest_.contains(k))
		{
			ofInterest_.remove(k);
		}
	}

  public boolean isSupported(Any eventType)
  {
		Any et = eventType;

		// All key events should be maps
		if (!(eventType instanceof Map))
			return false;

		Map m = (Map)eventType;
		et    = m.get(EventConstants.EVENT_TYPE);

		return checkIfSupported(et, eventTypes_);
  }
}

class WindowListenerAdapter extends    BaseListenerAdapter
														implements java.awt.event.WindowListener
{
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.W_ACTIVATED);
		eventTypes_.add(EventConstants.W_CLOSING);
		eventTypes_.add(EventConstants.W_CLOSED);
		eventTypes_.add(EventConstants.W_DEACTIVATED);
		eventTypes_.add(EventConstants.W_DEICONIFIED);
		eventTypes_.add(EventConstants.W_ICONIFIED);
		eventTypes_.add(EventConstants.W_OPENED);
	}

	public WindowListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	/**
	 * Invoked when the window is set to be the user's active window,
	 * which means the window (or one of its subcomponents) will receive
	 * keyboard events.
	 */
	public void windowActivated(java.awt.event.WindowEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_ACTIVATED));
	}

	/**
	 * Invoked when a window has been closed as the result of calling dispose
	 * on the window.
	 */
	public void windowClosed(java.awt.event.WindowEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_CLOSED));
	}

  /**
	 * Invoked when the user attempts to close the window from the
	 * window's system menu.
	 */
	public void windowClosing(java.awt.event.WindowEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_CLOSING));
	}

	/**
	 * Invoked when a window is no longer the user's active window,
	 * which means that keyboard events will no longer be delivered
	 * to the window or its subcomponents.
	 */
	public void windowDeactivated(java.awt.event.WindowEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_DEACTIVATED));
	}

	/**
	 * Invoked when a window is changed from a minimized to a normal
	 * state.
	 */
	public void windowDeiconified(java.awt.event.WindowEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_DEICONIFIED));
	}

	/**
	 * Invoked when a window is changed from a normal to a minimized
	 * state.
	 */
	public void windowIconified(java.awt.event.WindowEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_ICONIFIED));
	}

	/**
	 * Invoked the first time a window is made visible.
	 */
	public void windowOpened(java.awt.event.WindowEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_OPENED));
	}

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }
}

class IWindowListenerAdapter extends    BaseListenerAdapter
                             implements javax.swing.event.InternalFrameListener
{
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.W_ACTIVATED);
		eventTypes_.add(EventConstants.W_CLOSING);
		eventTypes_.add(EventConstants.W_CLOSED);
		eventTypes_.add(EventConstants.W_DEACTIVATED);
		eventTypes_.add(EventConstants.W_DEICONIFIED);
		eventTypes_.add(EventConstants.W_ICONIFIED);
		eventTypes_.add(EventConstants.W_OPENED);
	}

	public IWindowListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	/**
	 * Invoked when the window is set to be the user's active window,
	 * which means the window (or one of its subcomponents) will receive
	 * keyboard events.
	 */
	public void internalFrameActivated(javax.swing.event.InternalFrameEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_ACTIVATED));
	}

	/**
	 * Invoked when a window has been closed as the result of calling dispose
	 * on the window.
	 */
	public void internalFrameClosed(javax.swing.event.InternalFrameEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_CLOSED));
	}

  /**
	 * Invoked when the user attempts to close the window from the
	 * window's system menu.
	 */
	public void internalFrameClosing(javax.swing.event.InternalFrameEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_CLOSING));
	}

	/**
	 * Invoked when a window is no longer the user's active window,
	 * which means that keyboard events will no longer be delivered
	 * to the window or its subcomponents.
	 */
	public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_DEACTIVATED));
	}

	/**
	 * Invoked when a window is changed from a minimized to a normal
	 * state.
	 */
	public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_DEICONIFIED));
	}

	/**
	 * Invoked when a window is changed from a normal to a minimized
	 * state.
	 */
	public void internalFrameIconified(javax.swing.event.InternalFrameEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_ICONIFIED));
	}

	/**
	 * Invoked the first time a window is made visible.
	 */
	public void internalFrameOpened(javax.swing.event.InternalFrameEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.W_OPENED));
	}

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }
}

class DocumentListenerAdapter extends    BaseListenerAdapter
															implements javax.swing.event.DocumentListener
{
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.D_CHANGE);
		eventTypes_.add(EventConstants.D_INSERT);
		eventTypes_.add(EventConstants.D_REMOVE);
	}

	public DocumentListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	/**
	 * Gives notification that an attribute or set of attributes changed.
	 */
	public void changedUpdate(javax.swing.event.DocumentEvent e)
	{
		notifyAdaptee(new AnyDocumentEvent(e, EventConstants.D_CHANGE));
	}

	/**
	 * Gives notification that there was an insert into the document.
	 */
	public void insertUpdate(javax.swing.event.DocumentEvent e)
	{
		notifyAdaptee(new AnyDocumentEvent(e, EventConstants.D_INSERT));
	}

	/**
	 * Gives notification that a portion of the document has been removed.
	 */
	public void removeUpdate(javax.swing.event.DocumentEvent e)
	{
		notifyAdaptee(new AnyDocumentEvent(e, EventConstants.D_REMOVE));
	}


  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }
}

class DialogListenerAdapter   extends    BaseListenerAdapter
															implements com.inqwell.any.client.swing.DialogListener
{
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.DIALOG_OK);
		eventTypes_.add(EventConstants.DIALOG_CANCEL);
	}

	public DialogListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	/**
	 *
	 */
	public void dialogOk(DialogEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.DIALOG_OK));
	}

	public void dialogCancel(DialogEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.DIALOG_CANCEL));
	}

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }
}

class TableEditListenerAdapter   extends    BaseListenerAdapter
                                 implements com.inqwell.any.client.swing.TableEditListener
{
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.TABLEEDIT_DONE);
		eventTypes_.add(EventConstants.TABLEEDIT_CANCEL);
	}

	public TableEditListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	/**
	 *
	 */
	public void editDone(TableEditEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.TABLEEDIT_DONE));
	}

	public void editCanceled(TableEditEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.TABLEEDIT_CANCEL));
	}

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }
}

class ContextListenerAdapter   extends    BaseListenerAdapter
                               implements com.inqwell.any.client.ContextListener
{   
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.CONTEXT_ESTABLISHED);
	}

	public ContextListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}
	
	/**
	 * 
	 */
	public void contextEstablished(ContextEvent e) 
	{
    //System.out.println("ADAPTING");
		notifyAdaptee(new AnyEvent(e, EventConstants.CONTEXT_ESTABLISHED));
	}

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }
}

//class SpinEditListenerAdapter extends    BaseListenerAdapter
//                              implements com.inqwell.any.client.AnySpinner.SpinEditListener
//{
//  static private Array eventTypes_ = AbstractComposite.array();
//
//	static
//	{
//		eventTypes_.add(EventConstants.SPINEDIT_ACTION);
//		eventTypes_.add(EventConstants.SPINEDIT_FOCUS);
//	}
//
//	public SpinEditListenerAdapter(Any eventGategory)
//	{
//		super(eventGategory);
//	}
//
//  public void spinActionPerformed(com.inqwell.any.client.AnySpinner.SpinEditEvent e)
//	{
//		notifyAdaptee(new AnyEvent(e, EventConstants.SPINEDIT_ACTION));
//	}
//
//	public void spinFocusLost(com.inqwell.any.client.AnySpinner.SpinEditEvent e)
//	{
//		notifyAdaptee(new AnyEvent(e, EventConstants.SPINEDIT_FOCUS));
//	}
//
//  public void hasInterest(Any eventType) {}
//
//  public void hasNoInterest(Any eventType) {}
//
//  public boolean isSupported(Any eventType)
//  {
//		return checkIfSupported(eventType, eventTypes_);
//  }
//}

class FileChooserListenerAdapter   extends    BaseListenerAdapter
																	 implements com.inqwell.any.client.swing.FileChooserListener
{
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.FILECHOOSER_APPROVE);
		eventTypes_.add(EventConstants.FILECHOOSER_CANCEL);
	}

	public FileChooserListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	/**
	 *
	 */
	public void fileChooserApprove(FileChooserEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.FILECHOOSER_APPROVE));
	}

	public void fileChooserCancel(FileChooserEvent e)
	{
		notifyAdaptee(new AnyEvent(e, EventConstants.FILECHOOSER_CANCEL));
	}

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }
}

/**
 * The ItemListener adapter
 */
class ItemListenerAdapter extends    BaseListenerAdapter
													implements java.awt.event.ItemListener
{
  // The event types this adapter can generate.  If we are simple, like
  // an ActionListener, then we can only generate one type of event.  But
  // other listeners, like WindowListener, can generate many event types
  // (e.g. iconified, closed etc) each of which results in a separate
  // Inq event.
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.E_ITEM);
	}

	public ItemListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	public void itemStateChanged(java.awt.event.ItemEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.E_ITEM);
    anyEvt.add(ListenerAdapterFactory.stateChange__,
               ((e.getStateChange() == e.SELECTED) ? ListenerAdapterFactory.selected__
                                                   : ListenerAdapterFactory.deSelected__));

		notifyAdaptee(anyEvt);
	}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}
}

/**
 * The ChangeListener adapter
 */
class ChangeListenerAdapter extends    BaseListenerAdapter
													  implements javax.swing.event.ChangeListener
{
  // The event types this adapter can generate.  If we are simple, like
  // an ActionListener, then we can only generate one type of event.  But
  // other listeners, like WindowListener, can generate many event types
  // (e.g. iconified, closed etc) each of which results in a separate
  // Inq event.
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.E_CHANGE);
	}

	public ChangeListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	public void stateChanged(javax.swing.event.ChangeEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.E_CHANGE);

		notifyAdaptee(anyEvt);
	}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}
}

class TreeSelectionListenerAdapter extends    BaseListenerAdapter
                                   implements javax.swing.event.TreeSelectionListener
{
  // The event types this adapter can generate.  If we are simple, like
  // an ActionListener, then we can only generate one type of event.  But
  // other listeners, like WindowListener, can generate many event types
  // (e.g. iconified, closed etc) each of which results in a separate
  // Inq event.
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.E_TREESELECTION);
	}

	public TreeSelectionListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	public void valueChanged(javax.swing.event.TreeSelectionEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.E_TREESELECTION);

		notifyAdaptee(anyEvt);
	}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}
}

class TreeExpansionListenerAdapter extends    BaseListenerAdapter
                                   implements javax.swing.event.TreeExpansionListener
{
  // The event types this adapter can generate.  If we are simple, like
  // an ActionListener, then we can only generate one type of event.  But
  // other listeners, like WindowListener, can generate many event types
  // (e.g. iconified, closed etc) each of which results in a separate
  // Inq event.
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.E_TREEEXPANDED);
		eventTypes_.add(EventConstants.E_TREECOLLAPSED);
	}

	public TreeExpansionListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	public void treeExpanded(javax.swing.event.TreeExpansionEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.E_TREEEXPANDED);
    augmentInqEvent(e, anyEvt);

		notifyAdaptee(anyEvt);
	}

	public void treeCollapsed(javax.swing.event.TreeExpansionEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.E_TREECOLLAPSED);
    augmentInqEvent(e, anyEvt);

		notifyAdaptee(anyEvt);
	}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}

  static void augmentInqEvent(javax.swing.event.TreeExpansionEvent je,
                              Event                                ie)
  {
    // The last node in the tree path
    TreePath tp = je.getPath();
    AnyTreeNode node = (AnyTreeNode)tp.getLastPathComponent();
    ie.add(AnyTreeLevel.node__, node);

    // the tree path converted to an array of AnyTreeNode objects
    int max = tp.getPathCount();
    Array path = AbstractComposite.array(max);
    for (int i = 0; i < max; i++)
      path.add((AnyTreeNode)tp.getPathComponent(i));
    ie.add(AnyComponent.path__, path);
  }
}

class TreeWillExpandListenerAdapter extends    BaseListenerAdapter
                                    implements javax.swing.event.TreeWillExpandListener
{
  // The event types this adapter can generate.  If we are simple, like
  // an ActionListener, then we can only generate one type of event.  But
  // other listeners, like WindowListener, can generate many event types
  // (e.g. iconified, closed etc) each of which results in a separate
  // Inq event.
  static private Array eventTypes_ = AbstractComposite.array();

	static
	{
		eventTypes_.add(EventConstants.E_TREEWILLEXPAND);
		eventTypes_.add(EventConstants.E_TREEWILLCOLLAPSE);
	}

	public TreeWillExpandListenerAdapter(Any eventGategory)
	{
		super(eventGategory);
	}

	public void treeWillExpand(javax.swing.event.TreeExpansionEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.E_TREEWILLEXPAND);
    TreeExpansionListenerAdapter.augmentInqEvent(e, anyEvt);

		notifyAdaptee(anyEvt);
	}

	public void treeWillCollapse(javax.swing.event.TreeExpansionEvent e)
	{
    AnyEvent anyEvt = new AnyEvent(e, EventConstants.E_TREEWILLCOLLAPSE);
    TreeExpansionListenerAdapter.augmentInqEvent(e, anyEvt);

		notifyAdaptee(anyEvt);
	}

  public boolean isSupported(Any eventType)
  {
		return checkIfSupported(eventType, eventTypes_);
  }

  public void hasInterest(Any eventType) {}

  public void hasNoInterest(Any eventType) {}
}
