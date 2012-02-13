/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ServerConstants.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:19 $
 */

package com.inqwell.any;



/**
 * Constants within the <code>com.inqwell.server</code> package
 */
public interface ServerConstants
{

	/**
	 * Service arguments are found under the key SVCARGS
	 */
	public static final Any SVCINAR   = new ConstString("__args-in");
	public static final Any SVCEXEC   = new ConstString("__exec");
	public static final Any SVCSGUI   = new ConstString("syncgui");
	public static final Any SVCCTXT   = new ConstString("__context");

	public static final StringI ROCHANNEL  = new ConstString("$root.ochannel");
	public static final StringI RICHANNEL  = new ConstString("$root.ichannel");
  public static final StringI PICHANNEL  = new ConstString("$process.ichannel");
  public static final StringI POCHANNEL  = new ConstString("$process.ochannel");
  public static final Any ICHANNEL   = new ConstString("ichannel");
  public static final Any PROCESS    = new ConstString("process");
	public static final Any CLIENTPATH = new ConstString("$root.client");
	public static final Any CLIENT     = new ConstString("client");
	public static final Any SERVERNAME = new ConstString("serverName");
  public static final Any ROOT       = new ConstString("$root");
  public static final Any NSROOT     = new NodeSpecification("$root");

	// Stuff for remote i/o.
	public static final Any IO_ITEM    = new ConstString("item__");

}
