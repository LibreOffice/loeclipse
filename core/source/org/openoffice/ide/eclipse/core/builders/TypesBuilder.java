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
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

public class TypesBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID+".types";

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
			if (null != System.getProperty("DEBUG")) {
				e.printStackTrace();
			}
		}
	}

}
