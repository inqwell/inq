/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */


package com.inqwell.any;


/**
 * Concrete class AnyString.  String data type of Any.  The underlying
 * implementation is the java.lang.String class.  While objects of class
 * AnyString are mutable it is generally expected that once created AnyString
 * objects will not change.  If a new value is assigned the underlying
 * String object is re-created.
 */
public class AnyString extends    ConstString
                       implements StringI, Cloneable
{
	public static final AnyObject class__ = new AnyObject(AnyString.class);

  // String representations for BooleanI values.
  public static final ConstString TRUE  = new ConstString ("true");
  public static final ConstString FALSE = new ConstString ("false");

	public static final StringI EMPTY = new ConstString("");
	public static final StringI NULL  = new ConstString(null__);

  private transient CopyFrom copier_;

  public AnyString() { super(); }
  public AnyString(char[] c) { super(c); }
  public AnyString(byte[] b) { super(b); }
  public AnyString(String s) { super(s); }
  public AnyString(StringI s) { super(s.getValue()); }

  public Object clone() throws CloneNotSupportedException
  {
    AnyString a = (AnyString)super.clone();

    // The underlying String object is immutable so no need to
    // allocate a new one to the clone.
    a.copier_ = null;

    return a;
  }

//  public void accept (Visitor v)
//  {
//    v.visitAnyString(this);
//  }

  public Any copyFrom (Any a)
  {
    if (a != null)
    {
      if (a != this)
      {
        makeCopier();
        copier_.copy (a);
      }
    }
    else
      setNull();

    return this;
  }

  /**
   * Concatenate string representation of op2 to this.  Return new object.
   */
  public Any concat (Any op2)
  {
    return concat (op2, null);
  }

  /**
   * Concatenate string representation of op2 to this.  If result is not
   * null place it there, otherwise return new object.
   */
  public Any concat (Any op2, Any result)
  {
    StringI res = (result == null) ? (StringI)buildNew(null)
                                  : (StringI)result;

    String op1 = getValue();

    res.copyFrom (op2);

    res.setValue(op1.concat(res.getValue()));

    return res;
  }

  public void setValue(String value) { setString(value); }
  public void setValue(char[] c) { setString(new String(c)); }

  public void setNull()
  {
    setNullValue();
  }

  public Any bestowConstness()
  {
    return new ConstStringDecor(this);
  }

  public boolean isConst()
  {
    return false;
  }

  private void makeCopier()
  {
		if (copier_ == null)
			copier_ = new CopyFrom();
  }

  private class CopyFrom extends AssignerVisitor
  {
    protected void copy (Any from)
    {
			if (from != null)
				from.accept (this);
			else
				AnyString.this.setValue (EMPTY.toString());
    }

    public void visitAnyShort (ShortI s)
    {
      if (s.isNull())
        AnyString.this.setNull();
      else
        AnyString.this.setValue (String.valueOf(s.getValue()));
    }

    public void visitAnyInt (IntI i)
    {
      if (i.isNull())
        AnyString.this.setNull();
      else
        AnyString.this.setValue (String.valueOf(i.getValue()));
    }

    public void visitAnyLong (LongI l)
    {
      if (l.isNull())
        AnyString.this.setNull();
      else
        AnyString.this.setValue (String.valueOf(l.getValue()));
    }

    public void visitAnyFloat (FloatI f)
    {
      if (f.isNull())
        AnyString.this.setNull();
      else
        AnyString.this.setValue (String.valueOf(f.getValue()));
    }

    public void visitAnyDouble (DoubleI d)
    {
      if (d.isNull())
        AnyString.this.setNull();
      else
        AnyString.this.setValue (String.valueOf(d.getValue()));
    }

    public void visitDecimal (Decimal d)
    {
      if (d.isNull())
        AnyString.this.setNull();
      else
        AnyString.this.setValue (d.toString());
    }

    public void visitAnyString (StringI s)
    {
      if (s.isNull())
        AnyString.this.setNull();
      else
        AnyString.this.setValue (s.getValue());
    }

    public void visitAnyDate (DateI d)
    {
      if (d.isNull())
        AnyString.this.setNull();
      else
        AnyString.this.setValue (d.toString());
    }

    public void visitAnyByte (ByteI b)
    {
      if (b.isNull())
        AnyString.this.setNull();
      else
        AnyString.this.setValue (String.valueOf(b.getValue()));
    }

    public void visitAnyChar (CharI c)
    {
      if (c.isNull())
        AnyString.this.setNull();
      else
        AnyString.this.setValue (String.valueOf(c.getValue()));
    }

    public void visitAnyBoolean (BooleanI b)
    {
      AnyString.this.setValue (String.valueOf(b.getValue()));
    }

    public void visitMap (Map m)
    {
			AnyString.this.setValue (m.toString());
    }

    public void visitArray (Array a)
    {
      if (a instanceof AnyByteArray)
      {
        byte[] b = ((AnyByteArray)a).getValue();
        if (b == null)
          AnyString.this.setNull();
        else
          AnyString.this.setValue(new String(b));
      }
      else
			  AnyString.this.setValue (a.toString());
    }

    public void visitSet (Set s)
    {
      AnyString.this.setValue (s.toString());
    }

    public void visitAnyObject (ObjectI o)
    {
			Object ao = o.getValue();

			if (ao == null || ao == AnyNull.instance())
        AnyString.this.setNull();
			else
				AnyString.this.setValue (o.toString());
    }

    public void visitUnknown(Any o)
    {
      if (o == AnyNull.instance())
        AnyString.this.setNull();
      else
        AnyString.this.setValue (o.toString());
    }
  }
}

