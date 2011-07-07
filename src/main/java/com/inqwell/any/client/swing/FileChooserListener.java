/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/FileChooserListener.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import java.util.EventListener;

/**
 *
 */
public interface FileChooserListener extends EventListener
{
	public void fileChooserApprove(FileChooserEvent e);
	public void fileChooserCancel(FileChooserEvent e);
}
