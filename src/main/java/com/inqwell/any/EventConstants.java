/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/EventConstants.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Define constants which act as event types.  By convention these are
 * often integers but any Any type is acceptable.
 * @author $Author: sanderst $
 * @version $$
 * @see ServerEvent
 * @see EventListener
 */
public final class EventConstants
{
	// Sub-types of bot instance events.  These are qualified by their
	// bot descriptor so the actual event id is a Map.
	public static final IntI BOT_CREATE    = new ConstInt(1);
	public static final IntI BOT_DELETE    = new ConstInt(2);
	public static final IntI BOT_UPDATE    = new ConstInt(3);
	public static final IntI BOT_EXPIRE    = new ConstInt(31);
	public static final IntI BOT_CATALOGED = new ConstInt(42);

	public static final IntI EXEC_START    = new ConstInt(4);
	public static final IntI EXEC_COMPLETE = new ConstInt(5);
  
	public static final IntI INVOKE_SVC    = new ConstInt(6);
	public static final IntI INVOKE_WEBSVC = new ConstInt(61);
  public static final IntI WEBSVC_RESP   = new ConstInt(62);

	public static final IntI NODE_REPLACED       = new ConstInt(7);
	public static final IntI NODE_REPLACED_CHILD = new ConstInt(8);
	
	public static final IntI NODE_REMOVED        = new ConstInt(9);
	public static final IntI NODE_REMOVED_CHILD  = new ConstInt(10);
	
	//public static final IntI ROUTE_MSG           = new ConstInt(11); ... this can be recycled
	
	public static final IntI START_USERPROCESS    = new ConstInt(12);
	//public static final IntI START_ROUTERPROCESS  = new ConstInt(13); ... this can be recycled
	public static final IntI START_WEBPROCESS     = new ConstInt(14);
	public static final IntI START_IOPROCESS      = new ConstInt(34);
	public static final IntI START_MONITORPROCESS = new ConstInt(45);

	public static final IntI NODE_ADDED          = new ConstInt(15);
	public static final IntI NODE_ADDED_CHILD    = new ConstInt(16);
	
	public static final IntI PING_KEEPALIVE      = new ConstInt(17);
	public static final IntI PONG_KEEPALIVE      = new ConstInt(18);
	public static final IntI SESSION_SETID       = new ConstInt(19);
	public static final IntI SESSION_RESUME      = new ConstInt(20);
	public static final IntI RESUME_READ         = new ConstInt(21);
	public static final IntI RESUME_WRITE        = new ConstInt(22);
	public static final IntI SESSION_RECONNECT   = new ConstInt(23);
	public static final IntI SESSION_DEFUNCT     = new ConstInt(24);
	public static final IntI SERVER_LOST         = new ConstInt(25);
	public static final IntI LOGIN_DETAILS       = new ConstInt(26);
	public static final IntI INVOKE_LOGINSVC     = new ConstInt(27);
	public static final IntI INVOKE_SVRLOGIN     = new ConstInt(59);
	public static final IntI LOGIN_OK            = new ConstInt(28);
	public static final IntI LOGIN_DENIED        = new ConstInt(29);
	public static final IntI LOGIN_REQUEST       = new ConstInt(30);
	public static final IntI EVENT_INVOKER       = new ConstInt(32);
  public static final IntI TIMER_TASK          = new ConstInt(33);// TODO: remove
  public static final IntI DISPATCHED          = new ConstInt(33);

	public static final IntI IO_READREQUEST       = new ConstInt(35);
	public static final IntI IO_READRESULTS       = new ConstInt(36);
	public static final IntI IO_WRITEREQUEST      = new ConstInt(37);
	public static final IntI IO_WRITEACK          = new ConstInt(38);
	public static final IntI IO_FETCHDESCRIPTOR   = new ConstInt(39);
	public static final IntI IO_DESCRIPTORACK     = new ConstInt(40);
	public static final IntI IO_CACHEREMOVAL      = new ConstInt(41);
	public static final IntI IO_DELETEREQUEST     = new ConstInt(43);
	public static final IntI IO_DELETEACK         = new ConstInt(44);
	public static final IntI IO_COMMIT            = new ConstInt(53);
	public static final IntI IO_COMMITACK         = new ConstInt(54);
  
  // Remote transaction/locking
	public static final IntI LCK_LOCKREQUEST       = new ConstInt(46);
	public static final IntI LCK_LOCKACK           = new ConstInt(47);
	public static final IntI LCK_UNLOCKNOTIFY      = new ConstInt(48);
	public static final IntI LCK_PROCESS           = new ConstInt(49);
	public static final IntI LCK_OBJECT            = new ConstInt(50);
	public static final IntI LCK_TIMEOUT           = new ConstInt(51);
	public static final IntI LCK_UNLOCKREQUEST     = new ConstInt(46);
	public static final IntI LCK_UNLOCKACK         = new ConstInt(52);
  
  // Compression mode on serialized channels
	public static final IntI COMPRESS_ON           = new ConstInt(55);
	public static final IntI COMPRESS_OFF          = new ConstInt(56);
	public static final IntI ACK_COMPRESS_ON       = new ConstInt(57);
  public static final IntI ACK_COMPRESS_OFF      = new ConstInt(58);
  public static final IntI PURGE_BUFFER          = new ConstInt(60);
  
	// If an EventListener implementation wants to receive all event types
	// an EventGenerator can generate it can return this array
	public static final IntI ANY_TYPE = new ConstInt(-1);
	public static final IntI DEFAULT_TYPE_ = new ConstInt(-2);
	public static final Array  ALL_TYPES;
	public static final Array  DEFAULT_TYPE;
	
	// Adapted Swing events
	
	// General Events
	public static final Any E_ACTION           = new ConstString("E_ACTION");
	public static final Any E_FOCUSGAINED      = new ConstString("E_FOCUSGAINED");
	public static final Any E_FOCUSLOST        = new ConstString("E_FOCUSLOST");
	public static final Any E_SELECTIONCHANGED = new ConstString("E_SELECTIONCHANGED");
	public static final Any E_ITEM             = new ConstString("E_ITEM");
	public static final Any E_CHANGE           = new ConstString("E_CHANGE");
	
	// Menu Events
	public static final Any MENU_SELECTED     = new ConstString("MENU_SELECTED");
	public static final Any MENU_DESELECTED   = new ConstString("MENU_DESELECTED");
	public static final Any MENU_CANCELED     = new ConstString("MENU_CANCELED");
  
	// Popup Menu Events
	public static final Any POPUP_VISIBLE     = new ConstString("POPUP_VISIBLE");
	public static final Any POPUP_INVISIBLE   = new ConstString("POPUP_INVISIBLE");
	public static final Any POPUP_CANCELED    = new ConstString("POPUP_CANCELED");

  // Window Events
	public static final Any W_ACTIVATED   = new ConstString("W_ACTIVATED");
	public static final Any W_CLOSING     = new ConstString("W_CLOSING");
	public static final Any W_CLOSED      = new ConstString("W_CLOSED");
	public static final Any W_DEACTIVATED = new ConstString("W_DEACTIVATED");
	public static final Any W_DEICONIFIED = new ConstString("W_DEICONIFIED");
	public static final Any W_ICONIFIED   = new ConstString("W_ICONIFIED");
	public static final Any W_OPENED      = new ConstString("W_OPENED");
	
	// Those 'interesting' mouse events...
	public static final Any M_CLICKED     = new ConstString("M_CLICKED");
	public static final Any M_ENTERED     = new ConstString("M_ENTERED");
	public static final Any M_EXITED      = new ConstString("M_EXITED");
	public static final Any M_PRESSED     = new ConstString("M_PRESSED");
	public static final Any M_RELEASED    = new ConstString("M_RELEASED");
	public static final Any M_POPUP       = new ConstString("M_POPUP"); // spoof
	
	public static final Any M_MOVED       = new ConstString("M_MOVED");
	public static final Any M_DRAGGED     = new ConstString("M_DRAGGED");
	
	// Mouse/Key modifiers
	public static final Any MOD_BUTTON1     = new ConstInt(1);
	public static final Any MOD_BUTTON2     = new ConstInt(2);
	public static final Any MOD_BUTTON3     = new ConstInt(3);
	public static final Any MOD_MBUTTON     = new ConstString("button");
	public static final Any MOD_CLICKCOUNT  = new ConstString("count");
	public static final Any MOD_ALT         = new ConstString("alt");
	public static final Any MOD_CTRL        = new ConstString("ctrl");
	public static final Any MOD_SHIFT       = new ConstString("shift");
	public static final Any MOD_ALTGRAPH    = new ConstString("altgraph");
	public static final Any MOD_META        = new ConstString("meta");
	public static final Any MOD_KEYCODE     = new ConstString("keycode");
	// ... and these are added by INQ to support popups
	//public static final Any MOD_POPUP  = new ConstString("popup");

  // Key events
	public static final Any K_TYPED       = new ConstString("K_TYPED");
	public static final Any K_PRESSED     = new ConstString("K_PRESSED");
	public static final Any K_RELEASED    = new ConstString("K_RELEASED");
	
	// Tree events
	public static final Any E_TREESELECTION    = new ConstString("E_TREESELECTION");
	public static final Any E_TREEEXPANDED     = new ConstString("E_TREEEXPANDED");
	public static final Any E_TREECOLLAPSED    = new ConstString("E_TREECOLLAPSED");
	public static final Any E_TREEWILLEXPAND   = new ConstString("E_TREEWILLEXPAND");
	public static final Any E_TREEWILLCOLLAPSE = new ConstString("E_TREEWILLCOLLAPSE");

  // Component events
  public static final Any E_HIDDEN    = new ConstString("E_HIDDEN");
  public static final Any E_MOVED     = new ConstString("E_MOVED");
  public static final Any E_RESIZED   = new ConstString("E_RESIZED");
  public static final Any E_SHOWN     = new ConstString("E_SHOWN");

  // Document Events
	public static final Any D_CHANGE        = new ConstString("D_CHANGE");
	public static final Any D_INSERT        = new ConstString("D_INSERT");
	public static final Any D_REMOVE        = new ConstString("D_REMOVE");
	
	// Spoofed Dialog Events
	public static final Any DIALOG_OK       = new ConstString("DIALOG_OK");
	public static final Any DIALOG_CANCEL   = new ConstString("DIALOG_CANCEL");
	
	// Spoofed Table Edit Events
	public static final Any TABLEEDIT_DONE     = new ConstString("TABLEEDIT_DONE");
	public static final Any TABLEEDIT_CANCEL   = new ConstString("TABLEEDIT_CANCEL");
	
	// Spoofed Spinner Edit Events
	public static final Any SPINEDIT_ACTION    = new ConstString("SPINEDIT_ACTION");
	public static final Any SPINEDIT_FOCUS     = new ConstString("SPINEDIT_FOCUS");
	
	// Spoofed File Chooser Events
	public static final Any FILECHOOSER_APPROVE = new ConstString("FILECHOOSER_APPROVE");
	public static final Any FILECHOOSER_CANCEL  = new ConstString("FILECHOOSER_CANCEL");
	
	// Spoofed Context Established Event
	public static final Any CONTEXT_ESTABLISHED = new ConstString("CONTEXT_ESTABLISHED");
  
  public static final Any CELLEDITOR_STOPPED  = new ConstString("CELLEDITOR_STOPPED");
  
	static
	{
		ALL_TYPES = AbstractComposite.array();
		ALL_TYPES.add(ANY_TYPE);
		DEFAULT_TYPE = AbstractComposite.array();
		DEFAULT_TYPE.add(DEFAULT_TYPE_);
	}
  
  // These are the various child keys of a compound event id.
  // The field Descriptor.descriptor__ can appear also.
	public static final Any EVENT_TYPE   = AbstractValue.flyweightString("type");
	public static final Any EVENT_FIELDS = AbstractValue.flyweightString("fields");
  public static final Any EVENT_PATH   = AbstractValue.flyweightString("path");
  public static final Any EVENT_PARENT = AbstractValue.flyweightString("parent");
	// This one is only for node removal events and indicates the
	// vector position of the removed node (if available)
	public static final Any EVENT_VECTOR = AbstractValue.flyweightString("vector");
	public static final Any EVENT_CREATE = AbstractValue.flyweightString("create");
	
	// If an event is processed by a service request then the event,
  // its id and any context carried in the event are included in
  // the input arguments as these keys
	public static final Any EVENT           = AbstractValue.flyweightString("@event");
	public static final Any EVENT_ID        = AbstractValue.flyweightString("@eventId");
	public static final Any EVENT_CONTEXT   = AbstractValue.flyweightString("@eventData");
  public static final Any EVENT_COMPONENT = AbstractValue.flyweightString("@component");
  public static final Any component__     = AbstractValue.flyweightString("component");
}
