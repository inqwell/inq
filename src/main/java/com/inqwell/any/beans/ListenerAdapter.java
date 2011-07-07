/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/ListenerAdapter.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;
import com.inqwell.any.*;

/**
 * This interface describes the expected usage for any adapter class
 * which converts third party java events (eg AWT) to the Any environment.
 */
public interface ListenerAdapter extends Any
{
	/**
	 * Pass an INQ event, possibly wrapping an external event, to
	 * the registered adaptee
	 */
  public void notifyAdaptee(Event e);
  
  /**
	 * Set the adaptee we will notify
	 */
  public void setAdaptee(ListenerAdaptee l);

	/**
	 * Check if this adapter supports the given event type.  Some
	 * adapters will only handle a single event (such as <code>action</code>)
	 * while others will support more than one.  Specifically, this
	 * happens where Java defines adapter classes with multiple
	 * methods for event sources to call on their listeners.
	 */
  public boolean isSupported(Any eventType);
  
  /**
	 * One listener adapter might be capable of handling multiple
	 * events (e.g. the <code>WindowListener</code> interface).
	 * Return the name of event group this listener is responsible
	 * for (e.g. <code>"window"</code>.
	 */
  public Any eventCategory();
  
  /**
	 * If the <code>ListsnerAdaptee</code> has an interest
	 * in the specified event it should call this method to
	 * let the <code>ListenerAdapter</code> know.  This gives
	 * the adapter a chance to perform optimisation on the processing
	 * of the various types of events it receives before calling the
	 * adaptee
	 */
  public void hasInterest(Any eventType);
  
  /**
	 * Similar to <code>hasInterest</code>.  Adaptee is no longer
	 * interested in this event.
	 */
  public void hasNoInterest(Any eventType);
}

