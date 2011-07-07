/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/beans/DialogF.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.beans;

import com.inqwell.any.*;

/**
 * Facade interface for Dialog objects.
 */
public interface DialogF extends WindowF
{
	public void fireOk();

	public void fireCancel();

	public boolean isModal();
	public Any getModality();
}

