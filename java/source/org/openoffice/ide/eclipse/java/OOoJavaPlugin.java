/*************************************************************************
 *
 * $RCSfile: OOoJavaPlugin.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:38 $
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
package org.openoffice.ide.eclipse.java;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class OOoJavaPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "org.openoffice.ide.eclipse.java"; //$NON-NLS-1$
    public static final String WIZBAN = "wizban"; //$NON-NLS-1$
    
    //The shared instance.
    private static OOoJavaPlugin sPlugin;
    
    /**
     * The constructor.
     */
    public OOoJavaPlugin() {
        sPlugin = this;
    }

    /**
     * This method is called upon plug-in activation.
     * 
     * @param pContext the bundle context
     * @throws Exception if the plugin can't be started
     */
    public void start(BundleContext pContext) throws Exception {
        super.start(pContext);
    }

    /**
     * This method is called when the plug-in is stopped.
     * 
     * @param pContext the bundle context
     * @throws Exception if the plugin can't be stopped
     */
    public void stop(BundleContext pContext) throws Exception {
        super.stop(pContext);
        sPlugin = null;
    }

    /**
     * @return the shared instance.
     */
    public static OOoJavaPlugin getDefault() {
        return sPlugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path.
     *
     * @param pPath the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String pPath) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, pPath);
    }
    
    /**
     * @return the image registry of the plugin.
     */
    protected ImageRegistry createImageRegistry() {    
        ImageRegistry reg = super.createImageRegistry();
        
        reg.put(WIZBAN, getImageDescriptor("/icons/java_app_wiz.png")); //$NON-NLS-1$
        return reg;
    }
}
