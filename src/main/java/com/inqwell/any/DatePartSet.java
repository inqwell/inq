/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/DatePartSet.java $
 * $Author: sanderst $
 * $Revision: 1.7 $
 * $Date: 2011-05-07 16:53:31 $
 * @version $Revision: 1.7 $
 * @see 
 */

package com.inqwell.any;

import java.util.*;

/**
 * The <code>DatePartSet</code> function. Sets the given date field
 * or fields to the given value or values.
 * Returns the date that was set.
 */
public class  DatePartSet extends    AbstractFunc
                          implements Cloneable
{
  
  private Any    datepart_;
  private Locate date_;
  private Any    val_;
  private Any    cal_;

  /**
   * Set a specific datepart to the given value
   */
  public DatePartSet(Any datepart, Locate d, Any val, Any cal)
  {
    datepart_ = datepart;
    date_     = d;
    val_      = val;
    cal_      = cal;
  }

  /**
   * Set multiple date parts assuming val to be an array or either
   * three elements (year, month, date),
   * five elements (year, month, date, hourOfDay, minute) or
   * six elements (year, month, date, hourOfDay, minute, second) 
   */
  public DatePartSet(Locate d, Any val, Any cal)
  {
    date_     = d;
    val_      = val;
    cal_      = cal;
  }

  public Any exec(Any a) throws AnyException
  {
    IntI datepart  = (IntI)EvalExpr.evalFunc(getTransaction(),
                                a,
                                datepart_,
                                IntI.class);
    
    if (datepart_ != null & datepart == null)
      nullOperand(datepart_);
    
    // Save in case we are transactional
    datepart_ = datepart;
    
    // We don't know if val is an array or a single value yet.
    Any val        = EvalExpr.evalFunc(getTransaction(),
                                       a,
                                       val_);
    if (val == null)
      nullOperand(val_);
    
    // Save in case we are transactional
    val_ = val;
    
    DateI date     = (DateI)EvalExpr.evalFunc(getTransaction(),
                                a,
                                date_,
                                DateI.class);
    
    AnyCalendar cal = (AnyCalendar) EvalExpr.evalFunc(getTransaction(),
                                                      a,
                                                      cal_,
                                                      AnyCalendar.class);
    if (cal == null && cal_ != null)
      nullOperand(cal_);
    
    cal_ = cal;

    // What we do has to be done under transaction control so leave
    // it till then if appropriate
    Map m = date_.getMapParent();
    if (m == null)
      nullOperand(date_);
    
    if (m.isTransactional())
      return m;

    datepartset(a, date, datepart, val, cal);
    
    return date;
    
  }

  public Any doTransactionHandling (Any root, Any a) throws AnyException
  {
    Map m = (Map)a;
    
    DateI v; 
    
    // Check write PRIVILEGE
    Transaction t = getTransaction();
    t.copyOnWrite(m);
    v = (DateI)date_.doTransactionHandling(root, m);
    if (!v.isNull())
    {
      datepartset(root, v, (IntI)datepart_, val_, (AnyCalendar)cal_);
      t.fieldChanging(m, date_, null);
    }
    return v;
  }

  private void datepartset(Any         root,  
                           DateI       date,
                           IntI        datepart,
                           Any         val,
                           AnyCalendar cal) throws AnyException
  {
    Calendar d = (cal != null) ? cal.getCalendar() : Calendar.getInstance();
    
    // Must use setTimeInMillis rather than setDate for thread safety reasons
    d.setTimeInMillis(date.getTime());
    
    // Check what type of set operation to do, either for a specific
    // date part or one of the multiple operations by implication
    // the wrapped datepart correspond exactly with the Java Calendar
    
    if (datepart == null)
    {
      Array arr = (Array)val;
      if (arr.entries() != 5 && arr.entries() != 6)
        throw new IllegalArgumentException("Expected 5 or 6 values in this mode, got" + arr.entries());

      int[] vals = new int[arr.entries()];
      
      // Its just possible that our operand was a reference
      // to some sort of globally declared array so clone it now.
      arr = (Array)val.cloneAny();
      for (int i = 0; i < arr.entries(); i++)
      {
        Any aa = arr.get(i);

        Any ii  = EvalExpr.evalFunc(getTransaction(),
                                    root,
                                    aa);
        
        if (ii == null)
          nullOperand(aa);
        
        // null value means leave alone
        if (AnyNull.isNullInstance(ii))
          vals[i] = getFieldByIndex(d, i);
        else
        {
          IntI iii = (IntI)ii;
          vals[i] = iii.getValue();
        }
      }
      
      if (arr.entries() == 5)
        d.set(vals[0], vals[1], vals[2], vals[3], vals[4]);
      else
        d.set(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
    }
    else
    {
      if (!(val instanceof IntI))
        throw new IllegalArgumentException("Expected type int, found " + val.getClass());
      d.set(datepart.getValue(), ((IntI)val).getValue());
    }
    
    date.setValue(d.getTime());
  }

  private int getFieldByIndex(Calendar c, int index)
  {
    switch (index)
    {
      case 0:
        return c.get(Calendar.YEAR);
        
      case 1:
        return c.get(Calendar.MONTH);
        
      case 2:
        return c.get(Calendar.DAY_OF_MONTH);
        
      case 3:
        return c.get(Calendar.HOUR_OF_DAY);
        
      case 4:
        return c.get(Calendar.MINUTE);
        
      case 5:
        return c.get(Calendar.SECOND);
      
      default:
        return -1;
    }
  }

  public Iter createIterator ()
  {
    Array a = AbstractComposite.array();
    a.add(datepart_);
    a.add(date_);
    a.add(val_);
    if (cal_ != null)
      a.add(cal_);
    return a.createIterator();
  }

  public Object clone () throws CloneNotSupportedException
  {
    DatePartSet dp = (DatePartSet)super.clone();
    
    dp.datepart_    = AbstractAny.cloneOrNull(datepart_);
    dp.date_        = (Locate)date_.cloneAny();
    dp.val_         = val_.cloneAny();
    dp.cal_         = AbstractAny.cloneOrNull(cal_);

    return dp;
  }
}
