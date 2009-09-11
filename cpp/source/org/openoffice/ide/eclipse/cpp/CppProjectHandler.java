/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 * 
 ************************************************************************/
package org.openoffice.ide.eclipse.cpp;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.ISdk;
import org.openoffice.ide.eclipse.core.model.language.IProjectHandler;

public class CppProjectHandler implements IProjectHandler {

    public static final String[] LIBS = {
        "uno_sal", //$NON-NLS-1$
        "uno_cppu", //$NON-NLS-1$
        "uno_cppuhelpergcc3", //$NON-NLS-1$
        "uno_salhelpergcc3" //$NON-NLS-1$
    };
    
    @Override
    public void addLanguageDependencies(IUnoidlProject unoproject,
            IProgressMonitor monitor) throws CoreException {
        // Everything is done in the configureProject method
    }

    @Override
    public void addOOoDependencies(IOOo ooo, IProject project) {
        IUnoidlProject unoprj = ProjectsManager.getProject( project.getName() );
        addOOoDependencies( ooo, unoprj.getSdk(), project );
    }

    @Override
    public void addProjectNature(IProject pProject) {
        try {
            CDTHelper.addCDTNature( pProject, null );
            PluginLogger.debug( "C++ project nature set" );
        } catch (CoreException e) {
            PluginLogger.error( "Failed to set C++ project nature" );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureProject(UnoFactoryData pData) throws Exception {
        IProject prj = (IProject)pData.getProperty(
                IUnoFactoryConstants.PROJECT_HANDLE);
        
        // I don't know why, but this is needed to avoid problems when saving the buildInfos
        String srcDir = (String)pData.getProperty( IUnoFactoryConstants.PROJECT_SRC_DIR );
        IPath srcPath = prj.getFolder( srcDir ).getProjectRelativePath();
        ISourceEntry srcEntry = CoreModel.newSourceEntry( srcPath ); 
        ICProject cPrj = CoreModel.getDefault().getCModel().getCProject( prj.getName() );
        CoreModel.setRawPathEntries( cPrj, new IPathEntry[] { srcEntry }, null );
        
        // Create the managed build configurations
        IProjectType prjType = null;
        ArrayList< IConfiguration > validConfigs = new ArrayList<IConfiguration>();
        
        IProjectType[] types = ManagedBuildManager.getDefinedProjectTypes();
        for (IProjectType type : types) {
            boolean isSharedLib = type.getBuildArtefactType().getId().equals( 
                    ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_SHAREDLIB );
            boolean isTest = type.isTestProjectType();
            boolean isSupported = type.isSupported();
            boolean isAbstract = type.isAbstract();
            
            if ( isSharedLib && !isTest && isSupported && !isAbstract ) {
                IConfiguration[] configs = type.getConfigurations();
                for (IConfiguration config : configs) {
                    if ( ManagedBuildManager.isPlatformOk( config.getToolChain() ) ) {
                        prjType = type;
                        validConfigs.add( config );
                    }
                }
            }
        }
        
        // Initialize the project
        ManagedBuildManager.createBuildInfo( prj );
        IManagedProject managedPrj = ManagedBuildManager.createManagedProject( prj, prjType );
        ICSourceEntry sourceEntry = new CSourceEntry( prj.getFolder(srcPath).getProjectRelativePath(), null, 0 );
        
        for ( IConfiguration config : validConfigs ) {   
            String newId = ManagedBuildManager.calculateChildId( config.getId(), null);
            IConfiguration newConf = managedPrj.createConfiguration( config, newId );
            newConf.setArtifactName( prj.getName().replaceAll( " ", new String( ) ) ); //$NON-NLS-1$
            newConf.setName( config.getName() );
            newConf.setSourceEntries( new ICSourceEntry[] { sourceEntry } );
            newConf.setArtifactExtension( "uno." + newConf.getArtifactExtension() ); //$NON-NLS-1$
        }
        
        ManagedBuildManager.saveBuildInfo( prj, true );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IPath getImplementationFile(String implementationName) {
        return new Path( implementationName + ".cxx" ); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImplementationName(IUnoidlProject prj, String service)
            throws Exception {
        return service.substring( service.lastIndexOf( '.' ) + 1 ) + "Impl"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLibraryPath(IUnoidlProject pUnoprj) {
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( pUnoprj.getName() );
        
        IConfiguration config = ManagedBuildManager.getBuildInfo( prj ).getSelectedConfiguration();
        IPath path = ManagedBuildManager.getBuildLocation( config, config.getBuilder() );
        return path.toFile().getAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSkeletonMakerLanguage(UnoFactoryData data)
            throws Exception {
        return "--cpp"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeOOoDependencies(IOOo ooo, IProject project) {
        IUnoidlProject unoprj = ProjectsManager.getProject( project.getName() );
        removeOOoDependencies( ooo, unoprj.getSdk(), project );
    }
    
    private static ICLanguageSettingEntry[] getMacrosForPlatform( String pOs ) {
        
        HashMap<String, String> macrosList = new HashMap<String, String>();
        macrosList.put( Platform.OS_LINUX, "UNX GCC LINUX CPPU_ENV=gcc3" ); //$NON-NLS-1$
        macrosList.put( Platform.OS_MACOSX, "UNX GCC MACOSX CPPU_ENV=gcc3" ); //$NON-NLS-1$
        macrosList.put( Platform.OS_SOLARIS, "UNX SOLARIS SPARC CPPU_ENV=sunpro5" ); //$NON-NLS-1$
        macrosList.put( Platform.OS_WIN32, "WIN32 WNT CPPU_ENV=msci" ); //$NON-NLS-1$
        
        
        ArrayList<ICLanguageSettingEntry> results = new ArrayList<ICLanguageSettingEntry>();
        String[] macros = macrosList.get( pOs ).split( " " ); //$NON-NLS-1$
        for (String macro : macros) {
            String[] parts = macro.split( "=" ); //$NON-NLS-1$
            String name = parts[0];
            String value = null;
            if ( parts.length > 1 ) {
                value = parts[1];
            }
            results.add( new CMacroEntry( name, value, 0 ) );
        }
        
        return results.toArray( new ICLanguageSettingEntry[ results.size() ]);
    }
    
    static public void addOOoDependencies(IOOo ooo, ISdk sdk, IProject project) {
        CIncludePathEntry sdkIncludes = new CIncludePathEntry( sdk.getIncludePath(), 0 );
        CIncludePathEntry includes = new CIncludePathEntry( OOoSdkProjectJob.getIncludes( ooo ), 0 );
        
        ArrayList< CLibraryPathEntry > libs = new ArrayList<CLibraryPathEntry>();
        String[] oooLibs = ooo.getLibsPath();
        for (String libPath : oooLibs) {
            libs.add( new CLibraryPathEntry( new Path( libPath ), 0 ) );   
        }
        IFolder oooSdkLibs = OOoSdkProjectJob.getLibraries( ooo ); 
        libs.add( new CLibraryPathEntry( oooSdkLibs, ICSettingEntry.VALUE_WORKSPACE_PATH ) );
        
        
        CDTHelper.addEntries( project, new CIncludePathEntry[] { sdkIncludes, includes }, ICSettingEntry.INCLUDE_PATH );
        CDTHelper.addEntries( project, libs.toArray( new CLibraryPathEntry[libs.size()]), ICSettingEntry.LIBRARY_PATH );
        CDTHelper.addEntries( project, getMacrosForPlatform( Platform.getOS() ), ICSettingEntry.MACRO );
        CDTHelper.addLibs( project, LIBS );
        
        // Run the cppumaker on the ooo types ( asynchronous )
        OOoSdkProjectJob job = new OOoSdkProjectJob(ooo, sdk );
        job.schedule();
        
        CDTHelper.addEntries( project, new ICLanguageSettingEntry[]{ includes }, ICSettingEntry.INCLUDE_PATH );
    }
    
    static public void removeOOoDependencies(IOOo ooo, ISdk sdk, IProject project) {
        CIncludePathEntry sdkIncludes = new CIncludePathEntry( sdk.getIncludePath(), 0 );
        CIncludePathEntry includes = new CIncludePathEntry( OOoSdkProjectJob.getIncludes( ooo ), 0 );
        
        ArrayList< CLibraryPathEntry > libs = new ArrayList<CLibraryPathEntry>();
        String[] oooLibs = ooo.getLibsPath();
        for (String libPath : oooLibs) {
            libs.add( new CLibraryPathEntry( new Path( libPath ), 0 ) );   
        }
        IFolder oooSdkLibs = OOoSdkProjectJob.getLibraries( ooo ); 
        libs.add( new CLibraryPathEntry( oooSdkLibs, ICSettingEntry.VALUE_WORKSPACE_PATH ) );
        
        CDTHelper.removeEntries( project, new CIncludePathEntry[] { sdkIncludes, includes }, ICSettingEntry.INCLUDE_PATH );
        CDTHelper.removeEntries( project, libs.toArray( new CLibraryPathEntry[libs.size()]), ICSettingEntry.LIBRARY_PATH );
        CDTHelper.removeEntries( project, getMacrosForPlatform( Platform.getOS() ), ICSettingEntry.MACRO );
        CDTHelper.removeLibs( project, LIBS );
    }
}
