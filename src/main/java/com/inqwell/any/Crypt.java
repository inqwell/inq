/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/Crypt.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:19 $
 */
package com.inqwell.any;

import com.inqwell.any.channel.ContentCipher;
import java.io.UnsupportedEncodingException;

/**
 * Encrypt the given AnyString or AnyByteArray operand and return as
 * an AnyByteArray. The returned value is guaranteed to be printable
 * characters and not to contain the common string delimiters of
 * <code>"</code> and <code>'</code>.
 * <p>
 * If the argument is zero length then no operation is performed.
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public class Crypt extends    AbstractFunc
									 implements Cloneable
{
	static ContentCipher contentCipher__;

	private Any any_;

  static
  {
    contentCipher__ = ContentCipher.makeCipher();
  }

	public Crypt(Any any)
	{
		any_ = any;
	}

	public Any exec(Any a) throws AnyException
	{
		Any any = EvalExpr.evalFunc(getTransaction(),
                                a,
                                any_);

    if (any == null)
      return null;

    return crypt(any);
	}

  public Object clone () throws CloneNotSupportedException
  {
		Crypt c = (Crypt)super.clone();
		c.any_ = AbstractAny.cloneOrNull(any_);
		return c;
  }

  public static Any crypt(Any any) throws AnyException
  {
    AnyByteArray ab;
    if (!(any instanceof AnyByteArray))
    {
      ab = new AnyByteArray();
      ab.fromString(any, "ASCII");
    }
    else
    {
      ab = (AnyByteArray)any.cloneAny();
    }

    if (ab.getValue() != null && ab.getValue().length != 0)
    {
      // Make the source bytes at least 16
      if (ab.getValue().length < 16)
      {
        boolean reverse = false;
        byte[] s = ab.getValue();
        byte[] b = new byte[16];
        int count = 0;
        while (count < 16)
        {
          int idx = reverse ? s.length-1 : 0;
          for (int i = 0; (i < s.length && count < 16); i++)
          {
            b[count] = s[idx];
            count++;
            idx = (reverse) ? idx - 1 : idx + 1;
          }
          reverse = !reverse;
        }
        ab.setValue(b);
      }

      synchronized(contentCipher__)
      {
        try
        {
          ab.setValue(contentCipher__.encrypt(ab.getValue()));
          byte[] s = ab.getValue();
          if (s.length > 20)
          {
            byte[] o = new byte[s.length - 4];
            for (int i = 0; i < o.length; i++)
              o[i] = s[i];
            ab.setValue(o);
          }
        }
        catch(Exception e)
        {
          throw new ContainedException(e);
        }
      }

      // As a convenience make sure all the bytes are printable
      // non white-space characters.  Useful when encrypted passwords
      // are to be stored in a database. Rule out single/double quote
      // and back as well.
      byte[] b = ab.getValue();
      for (int i = 0; i < b.length; i++)
      {
        b[i] = (byte)(Math.abs(b[i]) % 94 + 33);
        if (b[i] == 39 || b[i] == 34 || b[i] == 92)
          b[i]++;
        //System.out.print(b[i] + " ");
      }
      //System.out.println("");
    }

    try
    {
      return new AnyString(new String(ab.getValue(), "ASCII"));
    }
    catch(UnsupportedEncodingException e)
    {
      throw new RuntimeContainedException(e);
    }
  }
}
