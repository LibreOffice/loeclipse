package org.openoffice.ide.eclipse.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

/**
 * This interface has to be implemented to add a new supported language 
 * to the plugin. It only handles the interactions between the 
 * <code>UnoidlProject</code> and the language class.
 * 
 * @author cbosdonnat
 * @see org.openoffice.ide.eclipse.core.internal.model.UnoidlProject
 *
 */
public interface ILanguage {
	
	/**
	 * Add a language specific language nature. This one has to
	 * configure the language-specific properties of the project and 
	 * set the builders.
	 * 
	 * @param project the project on which to add the nature. 
	 * 		Must not be null, otherwise the nature won't be added
	 */
	public void addProjectNature(IProject project);
	
	/**
	 * Add the language specific build to the project.
	 */
	public void addLanguageBuilder(IProject project);
	
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
	
	/**
	 * Adds the language specific dependencies for the project. This
	 * is mostly about library path settings. This method althought has
	 * to add the build path to the libraries and the implementation as 
	 * the sources path. 
	 * 
	 * @param unoproject the uno project on which to add the dependencies
	 * @param project the underlying project handle
	 * @param monitor a progress monitor
	 * @throws CoreException if there is any problem during the operation
	 */
	public void addLanguageDependencies(IUnoidlProject unoproject, 
			IProject project, IProgressMonitor monitor) throws CoreException;
	
	public void addOOoDependencies(IOOo ooo, IProject project);
	
	public void removeOOoDependencies(IOOo ooo, IProject project);
}
