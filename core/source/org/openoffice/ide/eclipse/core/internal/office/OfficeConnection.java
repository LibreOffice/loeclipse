/*************************************************************************
 *
 * $RCSfile: OfficeConnection.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2009/04/20 06:16:00 $
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * Sun Microsystems Inc., October, 2000
 *
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 *
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 *
 * Copyright: 2002 by Sun Microsystems, Inc.
 *
 * All Rights Reserved.
 *
 * Contributor(s): Cedric Bosdonnat
 *
 *
 ************************************************************************/
package org.openoffice.ide.eclipse.core.internal.office;

import java.io.File;
import java.net.URL;

import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.config.IOOo;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uri.ExternalUriReferenceTranslator;
import com.sun.star.uri.XExternalUriReferenceTranslator;

/**
 * Abstract class for tasks requiring an office connection.
 *
 * @author cedricbosdo
 *
 */
public class OfficeConnection {

    private IOOo mOOo;
    private XComponentContext mContext;

    /**
     * Creates a connection representation for a given office.
     *
     * @param pOOo
     *            the office to connect to
     */
    public OfficeConnection(IOOo pOOo) {
        mOOo = pOOo;
    }

    /**
     * @return the office instance on which to run the task.
     */
    public IOOo getOOo() {
        return mOOo;
    }

    /**
     * @return the remote office context
     */
    public XComponentContext getContext() {
        return mContext;
    }

    /**
     * Starts the office connection and initializes the component context.
     *
     * @throws BootstrapException
     *             if the office could be bootstrapped
     */
    public void startOffice() throws BootstrapException {
        mContext = Bootstrap.bootstrap();
        PluginLogger.info("Office bootstrapped"); //$NON-NLS-1$
    }

    /**
     * Stops the office if it is running.
     */
    public void stopOffice() {
        try {
            if (mContext != null) {
                // Only the uno test suite which started the office can stop it
                XMultiComponentFactory xMngr = mContext.getServiceManager();
                Object oDesktop = xMngr.createInstanceWithContext("com.sun.star.frame.Desktop", mContext); //$NON-NLS-1$
                XDesktop xDesktop = UnoRuntime.queryInterface(XDesktop.class, oDesktop);

                xDesktop.terminate();
                mContext = null;
                PluginLogger.info("Office stopped"); //$NON-NLS-1$
            }
        } catch (Exception e) {
            PluginLogger.error(Messages.getString("OfficeConnection.ERROR_STOP"), e); //$NON-NLS-1$
        }
    }

    /**
     * Convert an OS dependent file path to an OOo valid URL.
     *
     * @param pPath
     *            the OS dependent path to convert
     *
     * @return the resulting LibreOffice URL
     */
    public String convertToUrl(String pPath) {
        String internalUrl = null;

        try {
            if (pPath != null) {
                URL externalUrl = new File(pPath).toURI().toURL();
                XExternalUriReferenceTranslator translator = ExternalUriReferenceTranslator.create(mContext);
                internalUrl = translator.translateToInternal(externalUrl.toExternalForm());
            }
        } catch (Exception e) {
            PluginLogger.error(Messages.getString("OfficeConnection.ERROR_CONVERT_URL") + pPath, e); //$NON-NLS-1$
        }

        return internalUrl;
    }
}
