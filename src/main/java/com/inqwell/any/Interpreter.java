/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Interpreter.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import com.inqwell.any.client.AnyView;
import com.inqwell.any.client.swing.StyledDocument;
import java.io.InputStream;
import java.io.Reader;

public interface Interpreter extends Any
{
  static public ConstString parser__ = new ConstString("<parser>");

  public Any  run(Any root, AnyURL baseURL, Transaction t, InputStream i);
  public Any  run(Any root, AnyURL baseURL, Transaction t, Reader r);
  public Any  compile(Any root, AnyURL baseURL, Transaction t, InputStream i);
  public Any  compile(Any root, AnyURL baseURL, Transaction t, Reader r);
  public Any  layoutGui(Any root, Map components, Map mInqRoot, AnyView awtRoot, Any rootName, Transaction t, InputStream i);
  public Any  layoutGui(Any root, Map components, Map mInqRoot, AnyView awtRoot, Any rootName, Transaction t, Reader r);
  public void layoutDoc(Any context, StyledDocument doc, Transaction t, InputStream i);
  public void layoutDoc(Any context, StyledDocument doc, Transaction t, Reader r);
}
