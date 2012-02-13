/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.dock;

import java.util.ArrayList;

import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.action.CSeparator;

import com.inqwell.any.AbstractAny;
import com.inqwell.any.client.dock.AnyCAction.ActionContainer;


// Helper class for layout to build the action structure.
public class RootActionContainer implements ActionContainer
{
  private ArrayList<CAction> actions_ = new ArrayList<CAction>();
  
  @Override
  public void add(CAction action)
  {
    actions_.add(action);
  }

  @Override
  public void addSeparator()
  {
    actions_.add(CSeparator.SEPARATOR);
  }

  @Override
  public void insert(int index, CAction action)
  {
    actions_.add(index, action);
  }

  @Override
  public void insertSeparator(int index)
  {
    actions_.add(index, CSeparator.SEPARATOR);
  }

  @Override
  public void remove(int index)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void remove(CAction action)
  {
    throw new UnsupportedOperationException();
  }
  
  public ArrayList<CAction> getActions()
  {
    return actions_;
  }

  public void afterLayout() {}
}
