/**
 * Copyright (C) 2011 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

/* 
 * $Archive: /src/com/inqwell/any/channel/HttpTunnelConstants.java $
 * $Author: sanderst $
 * $Revision: 1.2 $
 * $Date: 2011-04-07 22:18:22 $
 */

package com.inqwell.any.channel;

import com.inqwell.any.Any;
import com.inqwell.any.ConstString;

public interface HttpTunnelConstants
{

	static final String    POST   = "POST ";
	static final String    GET    = "GET ";
	static final String    OK200  = "HTTP/1.0 200 OK\r\n";
	static final String    SERVER = "Server: Inq/1.0\r\n";
	static final Any INQ    = new ConstString("/inq");
	static final Any INQS   = new ConstString("/inqs");
	static final Any ID     = new ConstString("id");
	static final Any POST1  = new ConstString("POST");
	static final Any GET1   = new ConstString("GET");
	static final Any HEAD   = new ConstString("HEAD");
	
	static final String ENABLE_HTTP_PROXY = "http.proxySet";
	static final String ENABLE_PROXY      = "proxySet";
	static final String HTTP_PROXY_HOST   = "http.proxyHost";
	static final String PROXY_HOST        = "proxyHost";
	static final String HTTP_PROXY_PORT   = "http.proxyPort";
	static final String PROXY_PORT        = "proxyPort";
	
	static final String DATE_FORMAT = "EEE, dd MMM yyyy hh:mm:ss z";
	
	// Relevant header sent
	static final String WWW_AUTHORIZE   = "Authorization: ";
	static final String PROXY_AUTHORIZE = "Proxy-Authorization: ";
	
	static final Any WWW_AUTHENTICATE   = new ConstString("www-authenticate");
	static final Any PROXY_AUTHENTICATE = new ConstString("proxy-authenticate");
}
