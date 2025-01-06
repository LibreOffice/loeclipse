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
// Contributor:
// Laurent Godard <lgodard@nuxeo.com>
//
package com.artofsolving.jodconverter.openoffice.connection;

import java.net.ConnectException;

import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.NoConnectException;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ucb.XFileIdentifierConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public abstract class AbstractOpenOfficeConnection implements OpenOfficeConnection, XEventListener {

    private String mConnectionString;
    private XComponent mBridgeComponent;
    private XMultiComponentFactory mServiceManager;
    private XComponentContext mComponentContext;
    private XBridge mBridge;
    private boolean mConnected = false;
    private boolean mExpectingDisconnection = false;

    protected AbstractOpenOfficeConnection(String connectionString) {
        mConnectionString = connectionString;
    }

    @Override
    public synchronized void connect() throws ConnectException {
        try {
            XComponentContext localContext = Bootstrap.createInitialComponentContext(null);
            XMultiComponentFactory localServiceManager = localContext.getServiceManager();
            XConnector connector = UnoRuntime.queryInterface(XConnector.class,
                localServiceManager.createInstanceWithContext("com.sun.star.connection.Connector", localContext));
            XConnection connection = connector.connect(mConnectionString);
            XBridgeFactory bridgeFactory = UnoRuntime.queryInterface(XBridgeFactory.class,
                localServiceManager.createInstanceWithContext("com.sun.star.bridge.BridgeFactory", localContext));
            mBridge = bridgeFactory.createBridge("", "urp", connection, null);
            mBridgeComponent = UnoRuntime.queryInterface(XComponent.class, mBridge);
            mBridgeComponent.addEventListener(this);
            mServiceManager = UnoRuntime.queryInterface(XMultiComponentFactory.class,
                mBridge.getInstance("StarOffice.ServiceManager"));
            XPropertySet properties = UnoRuntime.queryInterface(XPropertySet.class, mServiceManager);
            mComponentContext = UnoRuntime.queryInterface(XComponentContext.class,
                properties.getPropertyValue("DefaultContext"));
            mConnected = true;
        } catch (NoConnectException connectException) {
            String msg = "connection failed: " + mConnectionString + ": " + connectException.getMessage();
            throw new ConnectException(msg);
        } catch (Exception exception) {
            throw new OpenOfficeException("connection failed: " + mConnectionString, exception);
        }
    }

    @Override
    public synchronized void disconnect() {
        mExpectingDisconnection = true;
        mBridgeComponent.dispose();
    }

    @Override
    public boolean isConnected() {
        return mConnected;
    }

    @Override
    public void disposing(EventObject event) {
        mConnected = false;
        if (!mExpectingDisconnection) {
            throw new OpenOfficeException("disconnected unexpectedly");
        }
        mExpectingDisconnection = false;
    }

    // for unit tests only
    void simulateUnexpectedDisconnection() {
        disposing(null);
        mBridgeComponent.dispose();
    }

    private Object getService(String className) {
        try {
            if (!mConnected) {
                connect();
            }
            return mServiceManager.createInstanceWithContext(className, mComponentContext);
        } catch (Exception exception) {
            throw new OpenOfficeException("could not obtain service: " + className, exception);
        }
    }

    @Override
    public XComponentLoader getDesktop() {
        return UnoRuntime.queryInterface(XComponentLoader.class,
            getService("com.sun.star.frame.Desktop"));
    }

    @Override
    public XFileIdentifierConverter getFileContentProvider() {
        return UnoRuntime.queryInterface(XFileIdentifierConverter.class,
            getService("com.sun.star.ucb.FileContentProvider"));
    }

    @Override
    public XBridge getBridge() {
        return mBridge;
    }

    @Override
    public XMultiComponentFactory getRemoteServiceManager() {
        return mServiceManager;
    }

    @Override
    public XComponentContext getComponentContext() {
        return mComponentContext;
    }

}
