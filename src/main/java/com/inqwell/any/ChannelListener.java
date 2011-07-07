/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/ChannelListener.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
 
package com.inqwell.any;

/**
 * Listener for received events at process input channels.
 * <p>
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 * @see
 */
public interface ChannelListener extends EventListener
{
	public void setRoot(Map root);
}
