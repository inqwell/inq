/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/SyncGraphicsService.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;


/**
 * A service that will always execute on the graphics thread.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class SyncGraphicsService extends Service
{
  public boolean isSyncGraphics()
  {
    return true;
  }
}
