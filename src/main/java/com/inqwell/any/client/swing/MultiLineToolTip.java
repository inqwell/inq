/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

package com.inqwell.any.client.swing;


import java.awt.*;
import javax.swing.*;

/**
 * From the tame examples
 */

public class MultiLineToolTip extends JToolTip {
  public MultiLineToolTip() {  
    setUI(new MultiLineToolTipUI());
  }
}

