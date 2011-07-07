/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Locate.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

/**
 * Defines the interface for function implementations which perform
 * some sort of traversal of an Any structure and return one or more
 * nodes.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public interface Locate extends Func
{
  public Array getArrayParent();
  public Map getMapParent();
  public Map getTMapFound();
  public Array hasArrayParent();
  public Any getPath();
  public Map getLastNodeSet();
  public Any getLast();
  public int getVectorNumber();
  public boolean isVectorElement();
  public NodeSpecification getNodePath();
  public void setNodePath(Any path);
  public void reset();
}
