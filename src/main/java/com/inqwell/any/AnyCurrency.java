/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: $
 * $Author: sanderst $
 * $Revision: 1.5 $
 * $Date: 2011-04-20 22:11:53 $
 */

package com.inqwell.any;

import java.util.Currency;
import java.util.Locale;

public class AnyCurrency extends    DefaultPropertyAccessMap
                         implements Cloneable
{
  public static final AnyCurrency null__ = new AnyCurrency((Currency)null);
  
  public static final Any currency__     = AbstractValue.flyweightString("currency");  
  
  private Currency currency_;
  
  public AnyCurrency()
  {
    currency_ = Currency.getInstance(Locale.getDefault());
  }
  
  /**
   * Construct to wrap a pre-loaded Currency
   */
  public AnyCurrency(Currency c)
  {
    currency_ = c;
  }
  
  public AnyCurrency(Any source)
  {
    processCurrency(source.toString());
  }
  
  public AnyCurrency(String source)
  {
    processCurrency(source);
  }
  
  public Currency getCurrency()
  {
    return currency_;
  }
  
  public Any getCurrencyCode()
  {
    return new AnyString(currency_.getCurrencyCode());
  }
  
  public Any getDefaultFractionDigits()
  {
    return new AnyInt(currency_.getDefaultFractionDigits());
  }
  
  public Any getSymbol()
  {
    return new AnyString(currency_.getSymbol());
  }
  
  public void accept (Visitor v)
  {
    // Override from AbstractMap or equals operator doesn't work.
    // See AnyFile.accept also
    if (v instanceof Equals || v instanceof Assign || v instanceof NotEquals)
      v.visitUnknown(this);
    else
      super.accept(v);
  }

  public Iter createKeysIterator()
  {
    return DegenerateIter.i__;
  }

  public Any copyFrom (Any a)
  {
    if (a != null && a != this)
    {
      if (a instanceof StringI)
      {
        processCurrency(a.toString());
        return this;
      }
      
      if (!(a instanceof AnyCurrency))
        throw new IllegalArgumentException();
      
      AnyCurrency i = (AnyCurrency)a;
      this.currency_ = i.currency_;
    }
    return this;
  }
  
  // Properties

  public Object getPropertyBean()
  {
    return currency_;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }

  public String toString()
  {
    if (currency_ != null)
      return currency_.getCurrencyCode();
    else
      return "<no ccy>";
  }

  public boolean equals(Any a)
  {
    return (a instanceof AnyCurrency) &&
         (((AnyCurrency)a).currency_.equals(currency_));
  }

  private void processCurrency(String ccy)
  {
    currency_ = Currency.getInstance(ccy);
  }
}
