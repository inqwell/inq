package com.inqwell.any.client.dock;

import java.awt.Container;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import bibliothek.gui.dock.ScreenDockStation;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.MultipleCDockableFactory;
import bibliothek.gui.dock.common.event.CDockableLocationEvent;
import bibliothek.gui.dock.common.event.CDockableLocationListener;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.gui.dock.station.screen.window.DefaultScreenDockWindowFactory;
import bibliothek.gui.dock.station.screen.window.DefaultScreenDockWindowFactory.Kind;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;

import com.inqwell.any.AbstractValue;
import com.inqwell.any.Any;
import com.inqwell.any.AnyObject;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.AnyString;
import com.inqwell.any.Map;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.client.AnyFrame;

/**
 * Wraps a {@link CControl} so it can be placed in the {@link Any} node space.
 * 
 * @author tom
 * 
 */
public class AnyCControl extends AnyObject
{
  public  static Any  cControl__    = AbstractValue.flyweightString("ccontrol__");
  public  static Any  dockLayout__  = AbstractValue.flyweightString("docklayout");

  private TrackLayout trackLayout__ = new TrackLayout();
  
  private AnyFrame    f_;

  /**
   * Look for AnyCControl contained as ccControl__ starting at node and
   * progressing to successive parents. If not found then create and store at
   * node or the nearest parent, which must be an AnyFrame and is used as the
   * docking environment's window.
   * 
   * @param node
   * @param search TODO
   * @return AnyCControl
   */
  public static AnyCControl getCControl(Map node, boolean search)
  {
    Any ret = null;
    Map cur = node;

    if (search)
    {
      while (node != null && ((ret = node.getIfContains(cControl__)) == null))
        node = (Map) node.getParentAny();
    }
    
    if (ret == null)
    {
      // Creating the CControl - find the nearest frame if cur is not
      // already one
      while (!(cur instanceof AnyFrame))
        cur = (Map) cur.getParentAny();

      if (cur == null)
        throw new AnyRuntimeException("No hosting frame found");

      AnyFrame f = (AnyFrame) cur;

      // Create the CControl and add it to the frame it is associated with
      CControl c = new CControl(f.getJFrame());
      ThemeMap themes = c.getThemes();
      themes.select(ThemeMap.KEY_ECLIPSE_THEME); // Hard code for now to eclipse

      AnyCControl cc = new AnyCControl(c);
      AnyMultipleCDockable.ensureFactory(cc);
      cc.setFrame(f);
      f.add(cControl__, cc);
      
      DefaultScreenDockWindowFactory factory = new DefaultScreenDockWindowFactory();
      factory.setUndecorated(false);
      factory.setKind(Kind.FRAME);
       
      // install new factory
      c.putProperty(ScreenDockStation.WINDOW_FACTORY, factory);

      // Add the CControl's default CContentArea to its associated
      // frame.
      // TODO: Note that for the moment this is the cheapest integration
      // with docking frames "docking stations" concept.
      ((Container)f.getAddIn()).add(cc.getCControl().getContentArea());

      ret = cc;
    }

    return (AnyCControl) ret;
  }

  /**
   * If the given node contains a AnyCControl then dispose of it
   * 
   * @param node
   */
  public static void destroyCControl(Map node)
  {
    Any c = node.getIfContains(cControl__);
    if (c != null)
    {
      ((AnyCControl) c).destroy();
    }
  }

  public AnyCControl(CControl c)
  {
    super(c);
  }

  private CControl getCControl()
  {
    return (CControl) getValue();
  }

  public Any copyFrom(Any a)
  {
    if (a != null && a != this)
    {
      if (!(a instanceof AnyCControl))
        throw new IllegalArgumentException("Not an AnyCControl");

      AnyCControl c = (AnyCControl) a;
      this.setValue(c.getValue());
    }
    return this;
  }

  public void destroy()
  {
    getCControl().destroy();
  }

  public void addDockable(AnySingleCDockable dockable)
  {
    getCControl().addDockable(dockable.getDefaultSingleCDockable());
    trackLayout(dockable);
  }

  public void addDockable(AnyMultipleCDockable dockable)
  {
    getCControl().addDockable(dockable.getDefaultMultipleCDockable());
    trackLayout(dockable);
  }

  public void removeDockable(AnySingleCDockable dockable)
  {
    untrackLayout(dockable);
    getCControl().removeDockable(dockable.getDefaultSingleCDockable());
  }

  public void removeDockable(AnyMultipleCDockable dockable)
  {
    untrackLayout(dockable);
    getCControl().removeDockable(dockable.getDefaultMultipleCDockable());
  }

  public void addMultipleDockableFactory(String id,
      MultipleCDockableFactory<?, ?> f)
  {
    getCControl().addMultipleDockableFactory(id, f);
  }
  
  public void trackLayout(AnyCDockable dockable)
  {
    dockable.getCDockable().addCDockableLocationListener(trackLayout__);
  }

  public void untrackLayout(AnyCDockable dockable)
  {
    dockable.getCDockable().removeCDockableLocationListener(trackLayout__);
  }

  public void saveLayout(Map m)
  {
    try
    {
      // Write the current layout to a string and place it in
      // the given map as "docklayout". If there the XML header
      // is present then remove it, as the string will be
      // written to a super-xml structure.
      CControl cc = getCControl();
      cc.save("default");

      XElement root = new XElement("root");
      cc.getResources().writeXML(root);
      StringBuilder b = new StringBuilder(20 * 1204);
      XIO.write(root, b);
      
      AnyString str = new AnyString();
      int idx = -1;
      if (b.indexOf("<?xml") >= 0 &&
          (idx = b.indexOf("?>")) > 0)
      {
        // Ok this is a bit messy.
        // TODO: Ask docking frames if the header can be made optional?
        str.setValue(b.substring(idx+2));
      }
      else
        str.setValue( b.toString());
      
      // str now contains the XML.
      m.add(dockLayout__, str);
    }
    catch(IOException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
  
  public void restoreLayout(Any layout)
  {
    try
    {
      BufferedInputStream in = 
        new BufferedInputStream(new ByteArrayInputStream(layout.toString().getBytes(Charset.forName("UTF-8"))));
      XElement element = XIO.readUTF(in);
      in.close();
      CControl cc = getCControl();
      cc.readXML(element);
      cc.load("default");
    }
    catch (IOException e)
    {
      throw new RuntimeContainedException(e);
    }
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }

  private void setFrame(AnyFrame f)
  {
    f_ = f;
  }
  
  private class TrackLayout implements CDockableLocationListener
  {
    private TrackLayout()
    {
    }

    @Override
    public void changed(CDockableLocationEvent event)
    {
      AnyCControl.this.f_.getJFrame().raiseComponentMovedEvent();
    }
  }
}
