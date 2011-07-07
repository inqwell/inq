/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/io/inq/AsIsDescriptorDecor.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */

package com.inqwell.any.io.inq;

import com.inqwell.any.DegenerateDescriptor;
import com.inqwell.any.server.BOTDescriptor;
import com.inqwell.any.io.ReplacingStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

/*
 * A decorator for the <code>BOTDescriptor</code> class that causes
 * intervention in the serialization of the <code>BOTDescriptor</code>.
 * <p>
 * Normally, a <code>BOTDescriptor</code> will be subject to some
 * sort of replacement during serialization, typically with an instance
 * of <code>ProxyDescriptor</code>. However, it is sometimes required
 * that this replacement is varied or does not take place at all for
 * a given object transfer on an
 * established <code>ReplacingOutputStream</code>. By wrapping the
 * <code>BOTDescriptor</code> in an instance of this class, together
 * with corresponding replacements and <code>writeObject()</code>
 * implementations we can control the replacement of
 * the <code>BOTDescriptor</code>.
 * <p>
 * $Archive: /src/com/inqwell/any/io/inq/AsIsDescriptorDecor.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:21 $
 */
public class AsIsDescriptorDecor extends DegenerateDescriptor
{
  private BOTDescriptor d_;
  
  AsIsDescriptorDecor(BOTDescriptor d) { d_ = d; }
  
  private void writeObject(ObjectOutputStream stream) throws IOException
  {
    ReplacingStream rs = (ReplacingStream)stream;
    stream.defaultWriteObject();
    // Reset the replacement flag in the
    // stream (previously set in Replacements.java)
    rs.setReplacementInfo(null, null);
  }
}

