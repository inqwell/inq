/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/identity/HasIdentity.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:22 $
 */
 
package com.inqwell.any.identity;

import com.inqwell.any.Map;

/**
 * A marker interface to ensure that a given object is not wrapped inside
 * an identity class more than once.
 * @author $Author: sanderst $
 * @version $Revision: 1.3 $
 * @see com.inqwell.any.Any
 */ 
public interface HasIdentity
{
  /**
   * This method is public to provide access to the decorated object
   * for the purposes of serialisation only.  It should NOT be called
   * for any other use.
   */
  public Map getInstance();
}

