/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/NativeDescriptor.java $
 * $Author: sanderst $
 * $Revision: 1.4 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;

import java.math.BigDecimal;

import com.inqwell.any.io.AnyIOException;

/**
 * A descriptor whose purpose is to define a single native type
 * with optional label, width formatting specification as for
 * managed object fields.
 */
public final class NativeDescriptor extends    AbstractDescriptor
                                    implements Descriptor
{
	public NativeDescriptor (Any name,
                           Any fQName,
                           Any inqPackage)
  {
    super(name, name, fQName, inqPackage);
  }
  
	public KeyDef getKey(Any keyName)
	{
    throw new UnsupportedOperationException();
	}

  public Map getUniqueKeys()
  {
    throw new UnsupportedOperationException();
	}
	
  public Map getAllKeys()
  {
		return null;
	}
  
  public Map getListenerData()
  {
    throw new UnsupportedOperationException();
  }
	
  public Any newInstance()
  {
    Any ret = getProto().get(getName());

		return ret.cloneAny();
	}
	
	/**
	 * Config setup method
	 */
	public void setDescriptorsInKeys()
	{
	}
	
  public String getFormat(Any key)
  {
    return super.getFormat(getName());
  }

  public Any getTitle(Any key)
  {
    return super.getTitle(getName());
  }

  public int getWidth(Any key)
  {
    return super.getWidth(getName());
  }
  
  public boolean isEnum(Any key)
  {
    return super.isEnum(getName());
  }
  
  public boolean isResolved(Descriptor d)
  {
    return true;
  }
  
  public Set reportUnresolved()
  {
    return null;
  }
  
  public String getRenderer(Any key)
  {
    Any a = getProto().get(getName());
  	if (a instanceof BooleanI)
  	  return Descriptor.checkbox__;
  	else
		  return Descriptor.label__;
  }
  
  public String getEditor(Any key)
  {
		if (isEnum(key))
			return Descriptor.combobox__;
		else
		{
	  	Any a = getProto().get(getName());
	  	if (a instanceof BooleanI)
	  	  return Descriptor.checkbox__;
	  	else
			  return Descriptor.textfield__;
	  }
  }
  
  public void addDataFieldReference(Any field, Any fQName, Any alias, Any initOverride)
  {
		throw new UnsupportedOperationException();
  }
  
  public int resolveReferences(Descriptor d)
  {
  	return RESOLVE_NOTHING;
  }
  
  public void resetResolved(Descriptor d)
  {
  }
  
  /* ------------- */

  public Any read(Process p, Map keyVal, int maxCount) throws AnyIOException
  {
		throw new UnsupportedOperationException();
  }

	public Any  read   (Process p, Any keyName, Map keyVal, int maxCount) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

	public Any  read   (Process p, KeyDef keyDef, Map keyVal, int maxCount) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

  public void write(Process p, Map m) throws AnyException
  {
		throw new UnsupportedOperationException();
  }
	
  public void write(Process p, Map k, Map o) throws AnyException
  {
		throw new UnsupportedOperationException();
  }
	public Map manage(Process p, Map m)
  {
		throw new UnsupportedOperationException();
  }

	public void resync (Process p, Map m) throws AnyException
  {
		throw new UnsupportedOperationException();
  }

	public void unmanage(Process p, Map m)
  {
		throw new UnsupportedOperationException();
  }

	public void delete(Process p, Map m) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

	public void delete(Process p, Map k, Map o) throws AnyException
	{
		throw new UnsupportedOperationException();
	}
	
	public void construct(Map m, Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

  public void join(Map m, Transaction t) throws AnyException
  {
    throw new UnsupportedOperationException();
  }

	public void mutate(Map newVal,
                     Map oldVal,
                     Map context,
                     Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

	public void destroy(Map m, Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

  public void expire(Map m, Transaction t) throws AnyException
	{
		throw new UnsupportedOperationException();
	}

  public Map getUniqueKey(Map m)
  {
		throw new UnsupportedOperationException();
  }

	public KeyDef getPrimaryKey()
  {
		throw new UnsupportedOperationException();
  }

  public void addDataField(Any key, Any field)
  {
  	if (getProto().entries() != 0)
  	  throw new AnyRuntimeException("Native descriptors only have 1 field");
  	getProto().add(getName(), field);
  }
  
  public static Any convertWithDescriptor(Any proto, Descriptor d)
  {
    return new ConvertWithDescriptor().convertWithDescriptor(proto, d);
  }
  
  public static class ConvertWithDescriptor extends AbstractVisitor
  {
    // With a NativeDescriptor, instances returned with new() are
    // just scalar types, there is no map to contain the meta data.
    // In this case make the prototype from an extension that carries
    // the associated Descriptor
    private Any        converted_;
    private Descriptor d_;
    
    private Any convertWithDescriptor(Any proto, Descriptor d)
    {
      d_ = d;
      proto.accept(this);
      return converted_;
    }
    
    public void visitAnyBoolean(BooleanI b)
    {
      converted_ = new EnumBoolean(b.getValue(), d_);
    }

    public void visitAnyByte (ByteI b)
    {
      converted_ = new EnumByte(b.getValue(), d_);
    }

    public void visitAnyChar (CharI c)
    {
      unsupportedOperation (c);
    }

    public void visitAnyInt (IntI i)
    {
      converted_ = new EnumInt(i.getValue(), d_);
    }

    public void visitAnyShort (ShortI s)
    {
      converted_ = new EnumShort(s.getValue(), d_);
    }

    public void visitAnyLong (LongI l)
    {
      converted_ = new EnumLong(l.getValue(), d_);
    }

    public void visitAnyFloat (FloatI f)
    {
      converted_ = new EnumFloat(f.getValue(), d_);
    }

    public void visitAnyDouble (DoubleI d)
    {
      converted_ = new EnumDouble(d.getValue(), d_);
    }

    public void visitDecimal (Decimal d)
    {
      converted_ = new EnumDecimal(d.getValue(), d_);
    }

    public void visitAnyString (StringI s)
    {
      converted_ = new EnumString(s.getValue(), d_);
    }

    public void visitAnyDate (DateI d)
    {
      converted_ = new EnumDate(d, d_);
    }

    public void visitMap (Map m)
    {
      unsupportedOperation (m);
    }
  }
  
  public interface NativeEnumProto
  {
    public Descriptor getDescriptor();
  }
  
  public static class EnumString extends AnyString implements NativeEnumProto
  {
    private Descriptor d_;
    
    public EnumString(String s, Descriptor d)
    {
      super(s);
      d_ = d;
    }

    @Override
    public Descriptor getDescriptor()
    {
      return d_;
    }
  }
  
  public static class EnumChar extends AnyChar implements NativeEnumProto
  {
    private Descriptor d_;
    
    public EnumChar(char c, Descriptor d)
    {
      super(c);
      d_ = d;
    }

    @Override
    public Descriptor getDescriptor()
    {
      return d_;
    }
  }
  
  public static class EnumByte extends AnyByte implements NativeEnumProto
  {
    private Descriptor d_;
    
    public EnumByte(byte b, Descriptor d)
    {
      super(b);
      d_ = d;
    }

    @Override
    public Descriptor getDescriptor()
    {
      return d_;
    }
  }
  
  public static class EnumShort extends AnyShort implements NativeEnumProto
  {
    private Descriptor d_;
    
    public EnumShort(short s, Descriptor d)
    {
      super(s);
      d_ = d;
    }

    @Override
    public Descriptor getDescriptor()
    {
      return d_;
    }
  }
  
  public static class EnumInt extends AnyInt implements NativeEnumProto
  {
    private Descriptor d_;
    
    public EnumInt(int i, Descriptor d)
    {
      super(i);
      d_ = d;
    }

    @Override
    public Descriptor getDescriptor()
    {
      return d_;
    }
  }
  
  public static class EnumLong extends AnyLong implements NativeEnumProto
  {
    private Descriptor d_;
    
    public EnumLong(long l, Descriptor d)
    {
      super(l);
      d_ = d;
    }

    @Override
    public Descriptor getDescriptor()
    {
      return d_;
    }
  }
  
  public static class EnumFloat extends AnyFloat implements NativeEnumProto
  {
    private Descriptor d_;
    
    public EnumFloat(Float f, Descriptor d)
    {
      super(f);
      d_ = d;
    }

    @Override
    public Descriptor getDescriptor()
    {
      return d_;
    }
  }
  
  public static class EnumDouble extends AnyDouble implements NativeEnumProto
  {
    private Descriptor d_;
    
    public EnumDouble(double db, Descriptor d)
    {
      super(db);
      d_ = d;
    }

    @Override
    public Descriptor getDescriptor()
    {
      return d_;
    }
  }
  
  public static class EnumDecimal extends AnyBigDecimal implements NativeEnumProto
  {
    private Descriptor d_;
    
    public EnumDecimal(BigDecimal dc, Descriptor d)
    {
      super(dc);
      d_ = d;
    }

    @Override
    public Descriptor getDescriptor()
    {
      return d_;
    }
  }
  
  public static class EnumDate extends AnyDate implements NativeEnumProto
  {
    private Descriptor d_;
    
    public EnumDate(DateI dt, Descriptor d)
    {
      super(dt);
      d_ = d;
    }
    
    @Override
    public Descriptor getDescriptor()
    {
      return d_;
    }
  }
  
  public static class EnumBoolean extends AnyBoolean implements NativeEnumProto
  {
    private Descriptor d_;
        
    public EnumBoolean(boolean b, Descriptor d)
    {
      super(b);
      d_ = d;
    }
    
    @Override
    public Descriptor getDescriptor()
    {
      return d_;
    }
  }
}
