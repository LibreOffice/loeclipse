/*************************************************************************
 *
 * $RCSfile: PackageExportWizard.java,v $
 *
 * $Revision: 1.7 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/07/17 21:01:01 $
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
package org.openoffice.ide.eclipse.core.wizards;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.builders.ServicesBuilder;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.PackagePropertiesModel;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.UnoPackage;
import org.openoffice.ide.eclipse.core.utils.FileHelper;
import org.openoffice.ide.eclipse.core.wizards.pages.PackageExportWizardPage;

/**
 * A wizard to export the project as a UNO package.
 * 
 * @author cedricbosdo
 *
 */
public class PackageExportWizard extends Wizard implements IExportWizard {

	private IStructuredSelection mSelection;
	private PackageExportWizardPage mPage;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		
		IUnoidlProject prj = mPage.getProject();
		String extension = mPage.getPackageExtension();
		File outputDir = mPage.getOutputPath();
	
		new PackageExportJob(prj, extension, outputDir).schedule();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		mSelection = selection;
		
		boolean canExport = false;
		
		Iterator iter = mSelection.iterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable)o;
				IResource res = (IResource)adaptable.getAdapter(IResource.class);
				if (res != null && ProjectsManager.getProject(res.getProject().getName()) != null) {
					canExport = true;
				}
			}
		}
		
		if (canExport) {
			mPage = new PackageExportWizardPage("main", mSelection); //$NON-NLS-1$
			addPage(mPage);
		}
	}

	/**
	 * The class performing the task
	 * 
	 * @author cedricbosdo
	 */
	private class PackageExportJob extends Job {

		private IUnoidlProject mPrj;
		private String mExtension;
		private File mOutputDir;
		
		public PackageExportJob(IUnoidlProject prj, String version, File output) {
			super(Messages.getString("PackageExportWizard.JobTitle")); //$NON-NLS-1$
			setPriority(Job.INTERACTIVE);
			
			mPrj = prj;
			mExtension = version;
			mOutputDir = output;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			// First run the services builder
			Status status = ServicesBuilder.syncRun(mPrj, monitor);
			
			if (status.getSeverity() == Status.CANCEL || status.getSeverity() == Status.ERROR) {
				return status;
			}
			
			// Remove the temporarily created Manifest and keep the library
			try {
				mPrj.getFile("MANIFEST.MF").delete(true, monitor); //$NON-NLS-1$
				mPrj.getFile("services.rdb").delete(true, monitor); //$NON-NLS-1$
			} catch (CoreException e) {
				// Not important
			}
			
			// Create the package
			IPath prjPath = mPrj.getProjectPath();
			File dir = new File(prjPath.toOSString());
			File dest = new File(mOutputDir, mPrj.getName() + "." + mExtension); //$NON-NLS-1$
			UnoPackage unoPackage = UnoidlProjectHelper.createMinimalUnoPackage(mPrj, dest, dir);			
			
			/*
			 *  TODO Read the package.properties files to add user selected files.
			 *  Recognize the following types from their extensions:
			 *  	- .xcu, xcs
			 *  	- .rdb
			 */
			IFile pkgProperties = mPrj.getFile("package.properties"); //$NON-NLS-1$
			if (pkgProperties.exists()) {
				PackagePropertiesModel pkgModel = new PackagePropertiesModel(pkgProperties);
				
				List<IFolder> basicLibs = pkgModel.getBasicLibraries();
				for (IFolder lib : basicLibs) {
					unoPackage.addBasicLibraryFile(lib.getLocation().toFile());
				}
				
				List<IFolder> dialogLibs = pkgModel.getDialogLibraries();
				for (IFolder lib : dialogLibs) {
					unoPackage.addDialogLibraryFile(lib.getLocation().toFile());
				}
				
				List<IResource> contents = pkgModel.getContents();
				for (IResource res : contents) {
					if (res.getType() == IResource.FILE) {
						if (res.getName().endsWith(".xcs")) { // $NON-NLS-1$ //$NON-NLS-1$
							unoPackage.addConfigurationSchemaFile(res.getLocation().toFile());
						} else if (res.getName().endsWith(".xcu")) { // $NON-NLS-1$ //$NON-NLS-1$
							unoPackage.addConfigurationDataFile(res.getLocation().toFile());
						} else if (res.getName().endsWith(".rdb")) { // $NON-NLS-1$ //$NON-NLS-1$
							unoPackage.addTypelibraryFile(res.getLocation().toFile(), "RDB"); // $NON-NLS-1$ //$NON-NLS-1$
						} else {
							unoPackage.addOtherFile(res.getLocation().toFile());
						}
					} else {
						unoPackage.addOtherFile(res.getLocation().toFile());
					}
				}
				
				Map<Locale, IFile> descriptions = pkgModel.getDescriptionFiles();
				Iterator<Entry<Locale, IFile>> iter = descriptions.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Locale, IFile> entry = iter.next();
					unoPackage.addPackageDescription(entry.getValue().getLocation().toFile(), 
							entry.getKey());
				}
			}
			
			// Close and write the package
			dest = unoPackage.close();
			
			// Clean up the library file and META-INF directory
			FileHelper.remove(new File(dir, "META-INF")); //$NON-NLS-1$
			File libFile = new File(mPrj.getLanguage().getProjectHandler().getLibraryPath(mPrj));
			FileHelper.remove(libFile);
			
			// Refresh the project and return the status
			UnoidlProjectHelper.refreshProject(mPrj, monitor);
			
			// Propose to update the package in OpenOffice.org instance
			Display.getDefault().asyncExec(new DeployerJob(mPrj, dest));
			
			
			return new Status(IStatus.OK, 
					OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, 
					IStatus.OK, 
					Messages.getString("PackageExportWizard.ExportedMessage"), //$NON-NLS-1$
					null);
		}
	}
	
	class DeployerJob implements Runnable {
		
		private IUnoidlProject mPrj;
		private File mDest;
		
		DeployerJob(IUnoidlProject prj, File dest) {
			mPrj = prj;
			mDest = dest;
		}
		
		public void run() {
			if (mPrj.getOOo().canManagePackages()) {
				// Ask to update the package
				if (MessageDialog.openQuestion(Display.getDefault().getActiveShell(), 
						Messages.getString("PackageExportWizard.DeployPackageTitle"),  //$NON-NLS-1$
						Messages.getString("PackageExportWizard.DeployPackageMessage"))) { //$NON-NLS-1$
					mPrj.getOOo().updatePackage(mDest);
				}
			}
		}
	}
}