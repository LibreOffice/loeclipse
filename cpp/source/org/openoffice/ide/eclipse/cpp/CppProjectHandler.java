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
import java.util.List;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.language.IProjectHandler;

public class CppProjectHandler implements IProjectHandler {

    @Override
    public void addLanguageDependencies(IUnoidlProject unoproject,
            IProgressMonitor monitor) throws CoreException {
        // Everything is done in the configureProject method
    }

    @Override
    public void addOOoDependencies(IOOo ooo, IProject project) {
        IUnoidlProject unoprj = ProjectsManager.getProject( project.getName() );
        
        CIncludePathEntry sdkIncludes = new CIncludePathEntry( unoprj.getSdk().getIncludePath(), 0 );
        ArrayList< CLibraryPathEntry > libs = new ArrayList<CLibraryPathEntry>();
        String[] oooLibs = ooo.getLibsPath();
        for (String libPath : oooLibs) {
            libs.add( new CLibraryPathEntry( new Path( libPath ), 0 ) );   
        }
        addIncludesAndLibs( project, new CIncludePathEntry[] { sdkIncludes }, 
                libs.toArray( new CLibraryPathEntry[libs.size()]) );
    }

    @Override
    public void addProjectNature(IProject pProject) {
        try {
            if (!pProject.exists()) {
                pProject.create(null);
                PluginLogger.debug(
                        "Project created during language specific operation"); //$NON-NLS-1$
            }
            
            if (!pProject.isOpen()) {
                pProject.open(null);
                PluginLogger.debug("Project opened"); //$NON-NLS-1$
            }
            
            CProjectNature.addCNature(pProject, null);
            CCProjectNature.addCCNature( pProject, null );
            ManagedCProjectNature.addManagedNature( pProject, null );
            
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
    public String getLibraryPath(IUnoidlProject prj) {
        // TODO Auto-generated method stub
        return null;
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
        
        CIncludePathEntry sdkIncludes = new CIncludePathEntry( unoprj.getSdk().getIncludePath(), 0 );
        ArrayList< CLibraryPathEntry > libs = new ArrayList<CLibraryPathEntry>();
        String[] oooLibs = ooo.getLibsPath();
        for (String libPath : oooLibs) {
            libs.add( new CLibraryPathEntry( new Path( libPath ), 0 ) );   
        }
        removeIncludesAndLibs( project, new CIncludePathEntry[] { sdkIncludes }, 
                libs.toArray( new CLibraryPathEntry[libs.size()]) );
    }
    
    static public void addIncludesAndLibs( IProject pProject, CIncludePathEntry[] pNewIncludes, 
            CLibraryPathEntry[] pNewLibs ) {
        ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription( pProject );
        ICConfigurationDescription[] configs = prjDesc.getConfigurations();
        
        // Set them on all the languages of all the configurations
        for (ICConfigurationDescription config : configs) {
            ICFolderDescription folder = config.getRootFolderDescription();
            ICLanguageSetting[] languages = folder.getLanguageSettings();
            for (ICLanguageSetting lang : languages) {
                // The includes
                List<ICLanguageSettingEntry> includes = lang.getSettingEntriesList( ICSettingEntry.INCLUDE_PATH );
                for ( CIncludePathEntry newEntry : pNewIncludes ) {
                    if ( !includes.contains( newEntry ) ) {
                        includes.add( newEntry );
                    }
                }
                lang.setSettingEntries( ICSettingEntry.INCLUDE_PATH, includes );
                
                // The libs
                List<ICLanguageSettingEntry> libs = lang.getSettingEntriesList( ICSettingEntry.LIBRARY_PATH );
                for ( CLibraryPathEntry newLib : pNewLibs ) {
                    if ( !includes.contains( newLib ) ) {
                        libs.add( newLib );
                    }
                }
                lang.setSettingEntries( ICSettingEntry.LIBRARY_PATH, libs );
            }
        }
        
        try {
            CoreModel.getDefault().setProjectDescription( pProject, prjDesc );
        } catch ( CoreException e ) {
            PluginLogger.error( "Error setting the includes and libraries", e );
        }
    }
    
    static public void removeIncludesAndLibs( IProject pProject, CIncludePathEntry[] pOldIncludes, 
            CLibraryPathEntry[] pOldLibs ) {
        ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription( pProject );
        ICConfigurationDescription[] configs = prjDesc.getConfigurations();
        
        // Set them on all the languages of all the configurations
        for (ICConfigurationDescription config : configs) {
            ICFolderDescription folder = config.getRootFolderDescription();
            ICLanguageSetting[] languages = folder.getLanguageSettings();
            for (ICLanguageSetting lang : languages) {
                // The includes
                List<ICLanguageSettingEntry> includes = lang.getSettingEntriesList( ICSettingEntry.INCLUDE_PATH );
                for ( CIncludePathEntry oldEntry : pOldIncludes ) {
                    if ( includes.contains( oldEntry ) ) {
                        includes.remove( oldEntry );
                    }
                }
                lang.setSettingEntries( ICSettingEntry.INCLUDE_PATH, includes );
                
                // The libs
                List<ICLanguageSettingEntry> libs = lang.getSettingEntriesList( ICSettingEntry.LIBRARY_PATH );
                for ( CLibraryPathEntry oldLib : pOldLibs ) {
                    if ( includes.contains( oldLib ) ) {
                        libs.remove( oldLib );
                    }
                }
                lang.setSettingEntries( ICSettingEntry.LIBRARY_PATH, libs );
            }
        }
        
        try {
            CoreModel.getDefault().setProjectDescription( pProject, prjDesc );
        } catch ( CoreException e ) {
            PluginLogger.error( "Error setting the includes and libaries", e );
        }
    }
}
