/*************************************************************************
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
 */
public class OfficeClassLoader extends URLClassLoader {

    private static Map<String, OfficeClassLoader> sClassLoaders = new HashMap<>();

    /**
     * Creates and initializes an {@link OfficeClassLoader} for a given office instance.
     *
     * @param ooo
     *            the LibreOffice instance to use for the class loader
     * @param parent
     *            the parent class loader to set
     */
    private OfficeClassLoader(IOOo ooo, ClassLoader parent) {
        super(getUrls(ooo), parent);
    }

    /**
     * Create or load the classloader for the given LibreOffice instance.
     *
     * @param ooo
     *            the LibreOffice instance to load
     * @param parent
     *            the parent classloader to use if the classloader has to be created.
     *
     * @return the classloader corresponding to the LibreOffice instance
     */
    public static OfficeClassLoader getClassLoader(IOOo ooo, ClassLoader parent) {
        // First try the class loaders cache
        OfficeClassLoader loader = sClassLoaders.get(ooo.getHome());

        // If not found, create a new class loader for this office instance
        if (loader == null) {
            loader = new OfficeClassLoader(ooo, parent);
            sClassLoaders.put(ooo.getHome(), loader);
        }

        return loader;
    }

    /**
     * Load a class in a different order than the standard one: first look in the URLs then call the parent's class
     * loader loadClass method. This order is applied only if the class to load is in the
     * <code>org.libreoffice.ide.eclipse.core.internal.office</code> package.
     *
     * @param name
     *            the name of the class to load
     * @param resolve
     *            if <code>true</code> then resolves the class
     *
     * @return the loaded class
     *
     * @throws ClassNotFoundException
     *             if the class cannot be found
     */
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

        Class<?> clazz = null;
        clazz = findLoadedClass(name);

        try {
            if (clazz == null && name.startsWith(OfficeHelper.OOO_PACKAGE)) {
                clazz = findClass(name);
            }
        } catch (ClassNotFoundException e) {
        }

        if (clazz == null) {
            clazz = super.loadClass(name, resolve);
        }

        return clazz;
    }

    /**
     * Get the URLs to add to the class loader from an office instance.
     *
     * @param ooo
     *            the LibreOffice instance
     *
     * @return the URL to set to the class loader
     */
    private static URL[] getUrls(IOOo ooo) {
        LinkedList<URL> oUrls = new LinkedList<URL>();
        try {
            String[] javaPaths = ooo.getClassesPath();

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

            String[] libsPath = ooo.getLibsPath();
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
