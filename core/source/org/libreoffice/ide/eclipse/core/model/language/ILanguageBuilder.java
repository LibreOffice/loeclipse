/*************************************************************************
 *
 * $RCSfile: ILanguageBuilder.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:30 $
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
package org.libreoffice.ide.eclipse.core.model.language;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * Interface defining a set of methods for to do the language specific tasks to build UNO projects.
 */
public interface ILanguageBuilder {

    /**
     * Computes the environment variables needed to build the library.
     *
     * @param pUnoProject
     *            the UNO project of the library
     * @return an array containing all the environment variables under the form <code>NAME=VALUE</code>
     */
    public String[] getBuildEnv(IUnoidlProject pUnoProject);

    /**
     * Creates the library containing the component.
     *
     * @param pUnoProject
     *            the project to build into a library
     * @return the created library path
     * @throws Exception
     *             if anything wrong happened
     */
    public IFile createLibrary(IUnoidlProject pUnoProject) throws Exception;

    /**
     * <p>
     * Generates the language specific interfaces corresponding to the project unoidl specifications. This method needs
     * an OpenOffice.org instance, the project <code>types.rdb</code> path, the build path where to put the generated
     * files and the root module to avoid massive idl types creation
     * </p>
     *
     * @param pSdk
     *            the SDK containing the tools for generation
     * @param pOoo
     *            the working OpenOffice.org instance
     * @param pPrj
     *            the project for which to generate the interfaces
     * @param pTypesFile
     *            the project types.rdb path
     * @param pBuildFolder
     *            the path to the folder where to the files will be generated
     * @param pRootModule
     *            the project root module (eg: <code>foo::bar</code>)
     * @param pMonitor
     *            the progress monitor
     */
    public void generateFromTypes(ISdk pSdk, IOOo pOoo, IProject pPrj, File pTypesFile, File pBuildFolder,
        String pRootModule, IProgressMonitor pMonitor);

    /**
     * Adds all the language specific libraries to the UNO package.
     *
     * @param pUnoPackage
     *            the UNO package to complete
     * @param pPrj
     *            the project to package
     */
    public void fillUnoPackage(UnoPackage pUnoPackage, IUnoidlProject pPrj);
}
