/*************************************************************************
 *
 * $RCSfile: MainImplementationsProvider.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:32 $
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
package org.libreoffice.ide.eclipse.core.launch;

import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.libreoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;

/**
 * Class providing all the <code>XMain</code> implementations of a UNO project.
 */
public class MainImplementationsProvider {

    /**
     * Gets all the classes implementing the <code>XMain</code> interface in a UNO project.
     *
     * <p>
     * This method delegates the search to the different language main providers.
     * </p>
     *
     * @param pPrj
     *            the project where to find the <code>XMain</code> implementations
     * @return the list of all the classes implementing the <code>XMain</code> interface
     */
    public String[] getImplementations(IUnoidlProject pPrj) {
        Vector<String> implementations = new Vector<String>();

        if (pPrj instanceof UnoidlProject) {
            IProject project = ((UnoidlProject) pPrj).getProject();
            Vector<IConfigurationElement> mainProviders = getProvidersDefs();

            for (IConfigurationElement providerDef : mainProviders) {
                try {
                    IMainProvider provider = (IMainProvider) providerDef.createExecutableExtension("class"); //$NON-NLS-1$
                    implementations.addAll(provider.getMainNames(project));
                } catch (Exception e) {
                    // Impossible to get the provider
                }
            }
        }
        return implementations.toArray(new String[implementations.size()]);
    }

    /**
     * Convenience method returning the providers definitions from the plugins extensions points descriptions.
     *
     * @return the array of the configuration element for the providers.
     */
    private static Vector<IConfigurationElement> getProvidersDefs() {
        Vector<IConfigurationElement> result = new Vector<IConfigurationElement>();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("org.libreoffice.ide.eclipse.core.MainProvider"); //$NON-NLS-1$
        if (point != null) {

            IExtension[] extensions = point.getExtensions();

            for (int i = 0; i < extensions.length; i++) {

                IConfigurationElement[] elements = extensions[i].getConfigurationElements();

                for (int j = 0; j < elements.length; j++) {
                    IConfigurationElement elementj = elements[j];
                    if (elementj.getName().equals("MainProvider")) { //$NON-NLS-1$
                        result.add(elementj);
                    }
                }
            }
        }

        return result;
    }
}
