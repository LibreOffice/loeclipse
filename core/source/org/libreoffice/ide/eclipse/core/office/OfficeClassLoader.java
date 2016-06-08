/*************************************************************************
 *
 * $RCSfile: OfficeClassLoader.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:48 $
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
package org.libreoffice.ide.eclipse.core.office;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;

/**
 * Special class loader to use to load OOo related classes.
 *
 * This class loader is important to bootstrap LibreOffice.
 *
 * @author cedricbosdo
 *
 */
public class OfficeClassLoader extends URLClassLoader {

    private static Map<String, OfficeClassLoader> sClassLoaders = new HashMap<>();

    /**
     * Creates and initializes an {@link OfficeClassLoader} for a given office instance.
     *
     * @param pOOo
     *            the LibreOffice instance to use for the class loader
     * @param pParent
     *            the parent class loader to set
     */
    private OfficeClassLoader(IOOo pOOo, ClassLoader pParent) {
        super(getUrls(pOOo), pParent);
    }

    /**
     * Create or load the classloader for the given LibreOffice instance.
     *
     * @param pOOo
     *            the LibreOffice instance to load
     * @param pParent
     *            the parent classloader to use if the classloader has to be created.
     *
     * @return the classloader corresponding to the LibreOffice instance
     */
    public static OfficeClassLoader getClassLoader(IOOo pOOo, ClassLoader pParent) {
        // First try the class loaders cache
        OfficeClassLoader loader = sClassLoaders.get(pOOo.getHome());

        // If not found, create a new class loader for this office instance
        if (loader == null) {
            loader = new OfficeClassLoader(pOOo, pParent);
            sClassLoaders.put(pOOo.getHome(), loader);
        }

        return loader;
    }

    /**
     * Load a class in a different order than the standard one: first look in the URLs then call the parent's class
     * loader loadClass method. This order is applied only if the class to load is in the
     * <code>org.libreoffice.ide.eclipse.core.internal.office</code> package.
     *
     * @param pName
     *            the name of the class to load
     * @param pResolve
     *            if <code>true</code> then resolves the class
     *
     * @return the loaded class
     *
     * @throws ClassNotFoundException
     *             if the class cannot be found
     */
    @Override
    protected synchronized Class<?> loadClass(String pName, boolean pResolve) throws ClassNotFoundException {

        Class<?> clazz = null;
        clazz = findLoadedClass(pName);

        try {
            if (clazz == null && pName.startsWith(OfficeHelper.OOO_PACKAGE)) {
                clazz = findClass(pName);
            }
        } catch (ClassNotFoundException e) {
        }

        if (clazz == null) {
            clazz = super.loadClass(pName, pResolve);
        }

        return clazz;
    }

    /**
     * Get the URLs to add to the class loader from an office instance.
     *
     * @param pOOo
     *            the LibreOffice instance
     *
     * @return the URL to set to the class loader
     */
    private static URL[] getUrls(IOOo pOOo) {
        LinkedList<URL> oUrls = new LinkedList<URL>();
        try {
            String[] javaPaths = pOOo.getClassesPath();

            URL bundleUrl = OOEclipsePlugin.getDefault().getBundle().getResource("/"); //$NON-NLS-1$
            URL plugin = FileLocator.resolve(bundleUrl);

            for (String path : javaPaths) {
                File dir = new File(path);
                File[] jars = dir.listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File pTested) {
                        return pTested.getName().endsWith(".jar"); //$NON-NLS-1$
                    }

                });

                for (File jar : jars) {
                    oUrls.add(jar.toURI().toURL());
                }
            }

            String[] libsPath = pOOo.getLibsPath();
            for (String path : libsPath) {
                oUrls.add(new File(path).toURI().toURL());
            }

            oUrls.add(plugin);
        } catch (Exception e) {
            PluginLogger.error(Messages.getString("OfficeClassLoader.LoaderError"), e); //$NON-NLS-1$
        }
        return oUrls.toArray(new URL[oUrls.size()]);
    }
}
