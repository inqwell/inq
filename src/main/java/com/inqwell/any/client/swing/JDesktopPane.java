/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/client/swing/JDesktopPane.java $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-07 22:18:22 $
 */
package com.inqwell.any.client.swing;

import javax.swing.JInternalFrame;
import javax.swing.DefaultDesktopManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Rectangle;

/**
 * An extension of JDesktopPane for Java bug 4765256
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.5 $
 */
public class JDesktopPane extends javax.swing.JDesktopPane
{
	/**
	 * Install the bug fix desktop manager
	 */
  public JDesktopPane()
  {
  	super();
    this.setDragMode(OUTLINE_DRAG_MODE);
    this.addComponentListener(new ComponentAdapter()
    {
      public void componentResized(ComponentEvent ce)
      {
        // handle icon placement
        int yOff = 1;
        int xOff = 0;
        JInternalFrame[] oif = JDesktopPane.this.getAllFrames();
        for (int k = 0; k < oif.length; k++)
        {
          if (oif[k].isIcon())
          {
            JInternalFrame.JDesktopIcon jdi = oif[k].getDesktopIcon();
            if ((xOff + 1) * jdi.getWidth() > JDesktopPane.this.getWidth())
            {
              yOff = yOff + 1;
              xOff = 0;
            }
            jdi.setLocation(xOff * jdi.getWidth(), JDesktopPane.this.getHeight() -
                                                   (yOff * jdi.getHeight()));
            xOff = xOff + 1;
          }
        }
      }
    });
    /*
    this.setDesktopManager(new IconDesktopManager());
    
    this.addComponentListener(new ComponentAdapter()
      {
        public void componentResized(ComponentEvent ce)
        {
          JDesktopPane jdpPane = (JDesktopPane)ce.getComponent();
          IconDesktopManager dm = (IconDesktopManager)jdpPane.getDesktopManager();
          JInternalFrame[] jifs = jdpPane.getAllFrames();
          for (int i = 0; i < jifs.length; i++)
          {
            if (jifs[i].isIcon())
            {
                dm.reIconifyFrame(jifs[i]);
            }
          }
        }
      });
      */
  }

  /*
  private static class IconDesktopManager extends DefaultDesktopManager
  {
    public void reIconifyFrame(JInternalFrame jif)
    {
        super.deiconifyFrame(jif);
        Rectangle r = getBoundsForIconOf(jif);
        super.iconifyFrame(jif);
        jif.getDesktopIcon().setBounds(r);
    }
  }
  */
}
