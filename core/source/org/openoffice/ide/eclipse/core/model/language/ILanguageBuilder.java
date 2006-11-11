/*************************************************************************
 *
 * $RCSfile: ILanguageBuilder.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/11 18:39:50 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.core.model.language;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

/**
 * Interface defining a set of methods for to do the language specific
 * tasks to build Uno-idl projects
 * 
 * @author cedricbosdo
 *
 */
public interface ILanguageBuilder {

	/**
	 * Computes the environment variables needed to build the library.
	 * 
	 * @param unoProject the uno project of the library
	 * @param project the underlying eclipse project
	 * @return an array containing all the environment variables under
	 * 		the form <code>NAME=VALUE</code>
	 */
	public String[] getBuildEnv(IUnoidlProject unoProject, IProject project);

	/**
	 * Creates the library containing the component.
	 * 
	 * @param unoProject the project to build into a library
	 * @return the created library path
	 * @throws Exception if anything wrong happened
	 */
	public IPath createLibrary(IUnoidlProject unoProject) throws Exception;

	/**
	 * <p>Generates the language specific interfaces corresponding
	 * to the project unoidl specifications. This method needs an
	 * OpenOffice.org instance, the project <code>types.rdb</code> 
	 * path, the build path where to put the generated files and
	 * the root module to avoid massive idl types creation</p>
	 * 
	 * @param sdk the sdk containing the tools for generation
	 * @param ooo the working OpenOffice.org instance
	 * @param typesFile the project types.rdb path
	 * @param buildFolder the path to the folder where to the files will
	 * 		be generated
	 * @param rootModule the project root module (eg: <code>foo::bar</code>)
	 * @param monitor the progress monitor
	 */
	public void generateFromTypes(ISdk sdk, IOOo ooo, IFile typesFile, 
			IFolder buildFolder, String rootModule, IProgressMonitor monitor);

}
