/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.dock;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

import bibliothek.gui.dock.common.DefaultMultipleCDockable;
import bibliothek.gui.dock.common.MultipleCDockableFactory;
import bibliothek.gui.dock.common.MultipleCDockableLayout;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.util.xml.XAttribute;
import bibliothek.util.xml.XElement;

import com.inqwell.any.AbstractComposite;
import com.inqwell.any.AbstractValue;
import com.inqwell.any.AbstractVisitor;
import com.inqwell.any.Any;
import com.inqwell.any.AnyException;
import com.inqwell.any.AnyRuntimeException;
import com.inqwell.any.BooleanI;
import com.inqwell.any.BuildNodeMap;
import com.inqwell.any.ByteI;
import com.inqwell.any.Call;
import com.inqwell.any.CharI;
import com.inqwell.any.DateI;
import com.inqwell.any.Decimal;
import com.inqwell.any.DoubleI;
import com.inqwell.any.EventListener;
import com.inqwell.any.FloatI;
import com.inqwell.any.Globals;
import com.inqwell.any.IntI;
import com.inqwell.any.Iter;
import com.inqwell.any.Locate;
import com.inqwell.any.LocateNode;
import com.inqwell.any.LongI;
import com.inqwell.any.Map;
import com.inqwell.any.NodeSpecification;
import com.inqwell.any.Process;
import com.inqwell.any.RuntimeContainedException;
import com.inqwell.any.ShortI;
import com.inqwell.any.StringI;
import com.inqwell.any.Transaction;
import com.inqwell.any.parser.Inq;
import com.inqwell.any.util.Util;

public class AnyMultipleCDockable extends AnyCDockable
{
  static public  final Any PARENT      = AbstractValue.flyweightString("parent");
  static private Factory factory__ = null;
  
  private MetaData metaData_;
  
  public static void ensureFactory(AnyCControl c)
  {
    // Create factory on first use
    if (factory__ == null)
    {
      factory__ = new Factory();
      c.addMultipleDockableFactory( "any", factory__ );
    }
  }
  
  public static void clearFactory()
  {
  	factory__ = null;
  }
  
  public AnyMultipleCDockable(Map reference, Call buildGui)
  {
    Any referencePath = reference.getPath(null);
    if (referencePath == null)
      throw new IllegalStateException("Reference node must be navigable from $root");
    
    metaData_ = new MetaData(referencePath, null, buildGui);
    
    //setObject(new Dockable(factory__));
  }
  
  // Restoring from stored layout
  private AnyMultipleCDockable(MetaData  metaData)
  {
    metaData_ = metaData;
  }
  
  public void addEventListener (EventListener l, Any eventParam)
  {
    super.addEventListener(l, eventParam);
    if (l == getParentAny())
    {
      // Adding to the node space. If not restoring, find out our name
      // and create the the path from the reference node to complete the
      // meta data. Create the dockable.
      // If restoring, just create the dockable.
      
      // NOTE: moving a dockable around in the hierarchy and changing
      // its name in the process, or its dependent CControl, is not
      // supported at this time.
      
      // The dockPath_ is only null when the dockable is created by script.
      // When the dockable is being recovered from a saved layout, the dockPath_
      // is whatever was saved
      if (metaData_.dockPath_ == null)
      {
        Process p = Globals.getProcessForCurrentThread();
        LocateNode ln = new LocateNode(metaData_.referencePath_);
        ln.setTransaction(p.getTransaction());
        try
        {
          Any refNode = ln.exec(p.getContext());
          if (refNode == null)
            throw new IllegalStateException("Cannot find reference node at " + metaData_.referencePath_);
          
          metaData_.dockPath_ = (NodeSpecification)this.getPath(refNode);
          if (metaData_.dockPath_ == null)
            throw new IllegalStateException("Cannot find node path for " + this.getNameInParent());
          
          AnyCControl c = AnyCControl.getCControl(this, true);
          ensureFactory(c);
          
          setObject(new Dockable(factory__));
          c.addDockable(this);
        }
        catch(AnyException e)
        {
          throw new RuntimeContainedException(e);
        }
      }
    }
  }
  
  /* (non-Javadoc)
   * @see com.inqwell.any.beans.WindowF#dispose(boolean)
   */
  @Override
  public void dispose(boolean disposeChildren)
  {
    AnyCControl c = AnyCControl.getCControl(this, true);
    c.removeDockable(this);    
  }

  private MetaData getMetaData()
  {
    return metaData_;
  }
  
  DefaultMultipleCDockable getDefaultMultipleCDockable()
  {
    CDockable d = getCDockable();
    
    if (d instanceof DefaultMultipleCDockable)
      return (DefaultMultipleCDockable)d;
    
    throw new AnyRuntimeException("Not a DefaultMultipleCDockable");
  }
  
  private class Dockable extends DefaultMultipleCDockable
  {
    private Dockable(Factory f)
    {
      super(f);
      setRemoveOnClose(false);
    }
    
    private MetaData getMetaData()
    {
      return AnyMultipleCDockable.this.getMetaData();
    }
  }
  
  private static class Factory implements MultipleCDockableFactory<Dockable, MetaData>
  {
    // An empty layout is required to read a layout from an XML file or
    // from a byte stream
    public MetaData create()
    {
      return new MetaData();
    }

    /* An optional method allowing to reuse 'dockable' when loading a new layout */
    public boolean match(Dockable dockable, MetaData layout)
    {
      return dockable.getMetaData().equals(layout);
    }

    /* Called when applying a stored layout */
    public Dockable read(MetaData layout)
    {
      // Create the dockable and its wrapper. Using the meta data,
      // find the reference node and place the dockable at the
      // dock path. Call the buildGui function
      
      AnyMultipleCDockable anyDock = new AnyMultipleCDockable(layout);

      Dockable d = anyDock.new Dockable(factory__);
      anyDock.setObject(d);
      
      // Place in node space
      // Call function
      
      Process p = Globals.getProcessForCurrentThread();
      Map context = p.getContext();
      BuildNodeMap b = new BuildNodeMap();
      b.setTransaction(p.getTransaction());
      Map args = layout.buildGui_.getArgs();
      args.replaceItem(PARENT, anyDock);
      try
      {
        b.build(layout.dockPath_, anyDock, context);

        layout.buildGui_.setTransaction(p.getTransaction());
        layout.buildGui_.exec(context);
        AnyCControl cc = AnyCControl.getCControl(anyDock, true);
        cc.trackLayout(anyDock);
        return d;
      }
      catch(AnyException e)
      {
        throw new RuntimeContainedException(e);
      }
      finally
      {
        args.remove(PARENT);
        layout.buildGui_.setTransaction(Transaction.NULL_TRANSACTION);
      }
    }

    /* Called when storing the current layout */
    public MetaData write(Dockable dockable)
    {
      return dockable.getMetaData();
    }
  }
  
  private static class MetaData implements MultipleCDockableLayout
  {
    // The path from $root to the reference node for the dockable
    private Any referencePath_;
    
    // The path from the reference node to the dockable and where the
    // dockable will be placed when restored
    private NodeSpecification dockPath_;
    
    // A call statement to build the GUI that is contained by the
    // dockable. When invoked, the argument "parent" is passed as
    // the dockable itself. Any other arguments that were saved (which
    // must be either constants or node references) are included in
    // the call execution.
    private Call buildGui_;
    
    // Mandatory no-args constructor required by DockingFrames as
    // a precursor to calling one of the read...() methods
    public MetaData()
    {
    }
    
    public MetaData(Any referencePath, Any dockPath, Call buildGui)
    {
      referencePath_  = referencePath;
      dockPath_       = (NodeSpecification)dockPath;
      buildGui_       = buildGui;
    }
    
    @Override
    public boolean equals( Object obj )
    {
      if(this == obj)
      {
        return true;
      }
      
      if(obj == null)
      {
        return false;
      }
      
      if(getClass() != obj.getClass())
      {
        return false;
      }
      
      MetaData other = (MetaData)obj;
      // In fact, leave out the reference path as part of the equals check.
      // It would be possible for this to change between save/restore
      // if, for some reason, the absolute Inq path of the reference
      // node was different (for example the application was started
      // by some app manager, not directly, or vice-versa).
      return //equals(referencePath_, other.referencePath_) && 
             equals(dockPath_, other.dockPath_) && 
             equals(buildGui_, other.buildGui_);
    }
    
    private boolean equals( Object a, Object b )
    {
      if( a == null )
      {
        return b == null;
      }
      else
      {
        return a.equals(b);
      }
    }

    public void readStream( DataInputStream in ) throws IOException
    {
      throw new UnsupportedOperationException("DataInputStream not supported");
    }

    // Restore this from dockingframes' XML structure produced by
    // writeXML
    public void readXML( XElement element )
    {
      String path = element.getElement("reference").getString();
      NodeSpecification referencePath = new NodeSpecification(path);
      referencePath_ = Inq.setPrefices(referencePath);
      //referencePath_ = new LocateNode(referencePath);
      
      path = element.getElement("dockpath").getString();
      NodeSpecification dockPath = new NodeSpecification(path);
      dockPath_ = Inq.setPrefices(dockPath);
      //dockPath_ = new LocateNode(dockPath);

      XElement buildGui = element.getElement("buildgui");
      Locate func = new LocateNode(buildGui.getString());
      Iterator<XElement> i = buildGui.iterator();
      Map args = null;
      if (i.hasNext())
        args = AbstractComposite.simpleMap();
      while (i.hasNext())
      {
        XElement arg = i.next();
        Any name = AbstractValue.flyweightString(arg.getName());
        String val = arg.getString();
        String className = arg.getString("className");
        XAttribute scale = arg.getAttribute("scale");
        String strScale = null;
        if (scale != null)
          strScale = scale.getString();
        args.add(name, Util.makeAny(className, val, strScale));
      }
      buildGui_ = new Call(func, null, args);
    }

    public void writeStream( DataOutputStream out ) throws IOException
    {
      throw new UnsupportedOperationException("DataOutputStream not supported");
    }

    // Represent this in terms of dockingframes' XML classes
    public void writeXML(XElement element)
    {
      element.addElement("reference").setString(referencePath_.toString());
      element.addElement("dockpath").setString(dockPath_.toString());
      
      // For the function to build the GUI we define a structure
      // whose root is the function reference and children the
      // argument names/values.
      XElement callRoot = element.addElement("buildgui");
      callRoot.setString(buildGui_.getFunc().toString());
      
      Map args = buildGui_.getArgs();
      // For ease of integration with docking frames we support only
      // arguments that are scalar values, that is args is a one-level
      // map. This has already been determined by this point, so this
      // comment is just as a reminder.
      Iter i = args.createKeysIterator();
      ArgWriter aw = null;
      if (i.hasNext())
        aw = new ArgWriter();
      while (i.hasNext())
      {
        Any name = i.next();
        if (name.equals(PARENT))
          continue;
        Any val  = args.get(name);
        // The name of the argument is given as the element name
        XElement arg = callRoot.addElement(name.toString());
        // Add a className attribute so we can restore the
        // appropriate type on reading...
        arg.addString("className", val.getClass().getName());
        // ...then the value of the element itself.
        aw.setArgVal(val, arg);
      }
    }
    
    // Set the element value from the supplied Any, remembering the
    // scale if its a Decimal
    private static class ArgWriter extends AbstractVisitor
    {
      private XElement element_;
      
      private void setArgVal(Any value, XElement element)
      {
        element_ = element;
        value.accept(this);
      }
      
      @Override
      public void visitAnyBoolean(BooleanI b)
      {
        element_.setBoolean(b.getValue());
      }

      @Override
      public void visitAnyByte(ByteI b)
      {
        element_.setByte(b.getValue());
      }

      @Override
      public void visitAnyChar(CharI c)
      {
        element_.setChar(c.getValue());
      }

      @Override
      public void visitAnyDouble(DoubleI d)
      {
        element_.setDouble(d.getValue());
      }

      @Override
      public void visitAnyFloat(FloatI f)
      {
        element_.setFloat(f.getValue());
      }

      @Override
      public void visitAnyInt(IntI i)
      {
        element_.setInt(i.getValue());
      }

      @Override
      public void visitAnyLong(LongI l)
      {
        element_.setLong(l.getValue());
      }

      @Override
      public void visitAnyShort(ShortI s)
      {
        element_.setShort(s.getValue());
      }

      @Override
      public void visitAnyString(StringI s)
      {
        element_.setString(s.getValue());
      }

      @Override
      public void visitDecimal(Decimal d)
      {
        // For decimals add a scale attribute
        element_.addInt("scale", d.scale());
        element_.setString(d.toString());
      }

      @Override
      public void visitAnyDate(DateI d)
      {
        // Handle dates using their millis time
        element_.setLong(d.getTime());
      }
    }
  }
}
