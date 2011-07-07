/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/DialogListener.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.util.EventListener;

/**
 *
 */
public interface DialogListener extends EventListener
{
	public void dialogOk(DialogEvent e);
	public void dialogCancel(DialogEvent e);
}
