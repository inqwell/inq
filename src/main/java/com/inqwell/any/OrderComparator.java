/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/OrderComparator.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:20 $
 */

package com.inqwell.any;

import java.util.Comparator;

/**
 * Compare <code>Orderable</code> children so that they can be
 * ordered according to arbitrary elements under each child node.
 * <p>
 * In the Any framework there is a need for only one comparator
 * implementation.
 *
 * @author $Author: sanderst $
 * @version $Revision: 1.2 $
 */
public interface OrderComparator extends Comparator
{
  // The null modes. How the comparator handles comparisons when one
  // element is the null constant.
  /**
   * Sorts the null constant as higher than non-null elements
   */
  public static int NULL_HIGH = 1;
  
  /**
   * Sorts the null constant as lower than non-null elements
   */
  public static int NULL_LOW = 0;

  public int compare (Any a1, Any a2);
	public int compare (Any a1, Any a2, Array orderingItems);
	public void setOrderBy(Array orderBy);
	public void setOrderMode(int orderMode);
	public void setToOrder(Map toOrder);
	public Array getOrderBy();
	public void setTransaction(Transaction t);
	
	public void setDescending(boolean isDescending);
  public void setIgnoreCase(boolean ignore);
	public boolean isDescending();
  public void setNullMode(int nullMode);
	
}
