/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/*
 * $Archive: /src/com/inqwell/any/StartProcessEvent.java $
 * $Author: sanderst $
 * $Revision: 1.3 $
 * $Date: 2011-04-07 22:18:20 $
 */
package com.inqwell.any;  

import com.inqwell.any.channel.AnyChannel;
import com.inqwell.any.channel.InputChannel;
import com.inqwell.any.channel.OutputChannel;
import com.inqwell.any.channel.ChannelDriver;
import com.inqwell.any.channel.Socket;
import com.inqwell.any.channel.HttpTunnel;
import com.inqwell.any.channel.ContentCipher;


/**
 * Start a process.  Processed by an receiving <code>inq</code> environment
 * to start a peer process to one in the initiating <code>inq</code>
 * environment.
 */
public class StartProcessEvent extends    SimpleEvent
															 implements Cloneable
{               
  // Process channels
  private InputChannel  ic_;
  private OutputChannel oc_;
  
  // Socket Channel
  private AnyChannel  socketChannel_;
  
  // ...and its driver
  private ChannelDriver cd_;
  
  private ContentCipher cipher_;
  
  private Func f_;
  
  private int keepAliveTimeout_;
  
  public StartProcessEvent(Any eventType)
  {
		this(eventType, null, null);
  }			       
  
  public StartProcessEvent(Any eventType, Any context)
  {
		this(eventType, context, null);
  }			       
  
  public StartProcessEvent(Any eventType, Any context, Any param)
  {
    this(eventType, context, param, 0);
  }
  
  public StartProcessEvent(Any eventType, Any context, Any param, int keepAliveTimeout)
  {
    super(eventType, context, param);
    keepAliveTimeout_ = keepAliveTimeout;
  }
  
  public int getKeepAliveTimeout()
  {
    return keepAliveTimeout_;
  }
  
  public void setInputChannel(InputChannel ic)
  {
		ic_ = ic;
  }
  
  public AnyChannel getInputChannel()
  {
		return (AnyChannel)ic_;
	}
  
  public void setOutputChannel(OutputChannel oc)
  {
		oc_ = oc;
  }
  
  public OutputChannel getOutputChannel()
  {
		return oc_;
	}
  
  public void setSocketChannel(AnyChannel socketChannel)
  {
		socketChannel_ = socketChannel;
  }
  
  public AnyChannel getSocketChannel()
  {
		return socketChannel_;
	}
  
  public void setNetworkDriver(ChannelDriver cd)
  {
		cd_ = cd;
  }
  
  public Socket getSocket()
  {
		return (Socket)cd_;
	}
  
  public HttpTunnel getTunnel()
  {
		return (HttpTunnel)cd_;
	}
  
  public ContentCipher getCipher()
  {
  	return cipher_;
  }
  
  public void setCipher(ContentCipher cipher)
  {
  	cipher_ = cipher;
  }
  
  public void setInitialAction(Func f)
  {
		f_ = f;
  }
  
  public Func getInitialAction()
  {
		return f_;
	}
}
