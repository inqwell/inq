/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive:  $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;


/**
 * A property that can be read and written
 * 
 * @author tom
 *
 */
public interface PropertyBinding extends Any
{
  public void setProperty(Any value);

  public Any getProperty();
  
  public void setPropertyInfo(Any info);
}
