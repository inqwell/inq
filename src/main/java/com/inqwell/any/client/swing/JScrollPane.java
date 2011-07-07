/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JScrollPane.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import javax.swing.JComponent;
import com.inqwell.any.client.swing.JPanel;
import java.awt.Component;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.Dimension;

/**
 * Not in use.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class JScrollPane extends    javax.swing.JScrollPane
                         implements ComponentListener
{
  public JScrollPane()
  {
  	super();
    init();
  }
  
  public JScrollPane(Component view)
  {
    super(view);
    init();
  }

  public JScrollPane(int vsbPolicy, int hsbPolicy)
  {
    super(vsbPolicy, hsbPolicy);
    init();
  }

  public JScrollPane(Component view, int vsbPolicy, int hsbPolicy)
  {
    super(view, vsbPolicy, hsbPolicy);
    init();
  }

  //Methods required by the ComponentListener interface:
  public void componentHidden(ComponentEvent e) { }
  public void componentMoved(ComponentEvent e) { }
  public void componentShown(ComponentEvent e) { }
  
  public void componentResized(ComponentEvent e)
  {
//    Component c = getViewport().getView();
//    if (c != null)
//    {
//      if (c instanceof JPanel)
//      {
//        JPanel jp = (JPanel)c;
//        System.out.println(" viewport extent ********** is " + getViewport().getExtentSize());
//        System.out.println(" child size ********** is " + c.getSize());
//        System.out.println(" child preferred ********** is " + ((JComponent)c).getPreferredSize());
//        
//        Dimension pref = jp.getPreferredSize();
//        Dimension view = getViewport().getExtentSize();
//        int width  = pref.width;
//        int height = pref.height;
//        if (view.width  > pref.width)
//          width = view.width;
//        if (view.height  > pref.height)
//          height = view.height;
//        
//        System.out.println(" setting size to " + width + ", " + height);
//        jp.setSize(width, height);
//	revalidate();
//	repaint();
//      }
//    }
  }
  
  private void init()
  {
    addComponentListener(this);
  }
}
