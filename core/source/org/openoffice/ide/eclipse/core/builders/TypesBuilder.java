package org.openoffice.ide.eclipse.core.builders;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * <p>Main builder of the UNO-IDL projects, it computes the language specific
 * type files and types registry from the <code>idl</code> files. In order
 * to split the work, the different tasks have been split into several builders:
 *   <ul>
 *     <li>{@link IdlcBuilder} generating the urd files from the idl ones</li>
 *     <li>{@link RegmergeBuilder} merging the urd files into the types 
 *     	   registry</li>
 *     <li>{@link org.openoffice.ide.eclipse.core.model.ILanguage#generateFromTypes(ISdk, IOOo, IFile, IFolder, String, IProgressMonitor)}
 *     	   generating the language specific type files</li>
 *   </ul>
 * </p>
 * 
 * @author cbosdonnat
 *
 */
public class TypesBuilder extends IncrementalProjectBuilder {

	/**
	 * The builder ID as set in the <code>plugin.xml</code> file
	 */
	public static final String BUILDER_ID = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID+".types";

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		
		IUnoidlProject unoproject = ProjectsManager.getInstance().getProject(
				getProject().getName());
		
		// Clears the registries before beginning
		removeAllRegistries(unoproject);
		
		IdlcBuilder idlcBuilder = new IdlcBuilder(unoproject);
		idlcBuilder.build(FULL_BUILD, args, monitor);
		
		// Workspace refreshing needed for the next tool
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		RegmergeBuilder regmergeBuilder = new RegmergeBuilder(unoproject);
		regmergeBuilder.build(FULL_BUILD, args, monitor);

		// Workspace refreshing needed for the next tool
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

		IFile typesFile = unoproject.getFile(unoproject.getTypesPath());
		IFolder buildFolder = unoproject.getFolder(unoproject.getBuildPath());
		
		unoproject.getLanguage().generateFromTypes(
				unoproject.getSdk(),
				unoproject.getOOo(),
				typesFile, buildFolder,
				unoproject.getRootModule(), monitor);
		
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		return null;
	}
	
	/**
	 * Removes all the registries, ie <code>.urd</code> and 
	 * <code>types.rdb</code> files.
	 * 
	 * @param unoproject the Uno project from which to remove the registries
	 */
	private void removeAllRegistries(IUnoidlProject unoproject) {
		
		try {
			IPath rdbPath = unoproject.getTypesPath();
			IFile rdbFile = getProject().getFile(rdbPath);
			if (rdbFile.exists()) {
				rdbFile.delete(true, null);
			}
			
			IPath urdPath = unoproject.getUrdPath();
			IFolder urdFolder = getProject().getFolder(urdPath);
			IResource[] members = urdFolder.members();
			
			for (int i=0, length=members.length; i<length; i++) {
				IResource resi = members[i];
				if (resi.exists()) {
					resi.delete(true, null);
				}
			}
			
		} catch (CoreException e) {
			PluginLogger.getInstance().debug(e.getMessage());
		}
	}
}
