//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2007 - Mirko Nasato <mirko@artofsolving.com>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// http://www.gnu.org/copyleft/lesser.html
//
package com.artofsolving.jodconverter.openoffice.connection;

import java.net.ConnectException;

import com.sun.star.bridge.XBridge;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.uno.XComponentContext;

/**
 * A UNO remote protocol connection to a listening OpenOffice.org instance
 */
public interface OpenOfficeConnection {

	public void connect() throws ConnectException;

	public void disconnect();

	public boolean isConnected();

	/**
	 * @return the com.sun.star.frame.Desktop service
	 */
	public XComponentLoader getDesktop();

	/**
	 * @return the com.sun.star.ucb.FileContentProvider service
	 */
	public XFileIdentifierConverter getFileContentProvider();

	public XBridge getBridge();
	
	public XMultiComponentFactory getRemoteServiceManager();

	public XComponentContext getComponentContext();

}
