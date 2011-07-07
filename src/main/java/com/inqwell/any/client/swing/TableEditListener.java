/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/TableEditListener.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.util.EventListener;

/**
 *
 */
public interface TableEditListener extends EventListener
{
	public void editDone(TableEditEvent e);
	public void editCanceled(TableEditEvent e);
}
