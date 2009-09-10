package org.openoffice.ide.eclipse.cpp;

import java.util.List;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openoffice.ide.eclipse.core.PluginLogger;

public class CDTHelper {

    public static void addCDTNature( IProject pProject, IProgressMonitor pMonitor ) throws CoreException {
            if (!pProject.exists()) {
                pProject.create( pMonitor );
                PluginLogger.debug(
                        "Project created during language specific operation"); //$NON-NLS-1$
            }
            
            if (!pProject.isOpen()) {
                pProject.open( pMonitor );
                PluginLogger.debug("Project opened"); //$NON-NLS-1$
            }
            
            CProjectNature.addCNature(pProject, pMonitor );
            CCProjectNature.addCCNature( pProject, pMonitor );
            ManagedCProjectNature.addManagedNature( pProject, pMonitor );
    }
    
    public static void addEntries( IProject pProject, ICLanguageSettingEntry[] pNewEntries, int pEntriesType ) {
        ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription( pProject );
        ICConfigurationDescription[] configs = prjDesc.getConfigurations();
        
        // Set them on all the languages of all the configurations
        for (ICConfigurationDescription config : configs) {
            ICFolderDescription folder = config.getRootFolderDescription();
            ICLanguageSetting[] languages = folder.getLanguageSettings();
            for (ICLanguageSetting lang : languages) {
                List<ICLanguageSettingEntry> entries = lang.getSettingEntriesList( pEntriesType );
                for ( ICLanguageSettingEntry newEntry : pNewEntries ) {
                    if ( !entries.contains( newEntry ) ) {
                        entries.add( newEntry );
                    }
                }
                lang.setSettingEntries( pEntriesType, entries );
            }
        }
        
        try {
            CoreModel.getDefault().setProjectDescription( pProject, prjDesc );
        } catch ( CoreException e ) {
            PluginLogger.error( "Error setting the includes and libraries", e );
        }
    }
    
    public static void removeEntries( IProject pProject, ICLanguageSettingEntry[] pOldEntries, int pEntriesType ) {
        ICProjectDescription prjDesc = CoreModel.getDefault().getProjectDescription( pProject );
        ICConfigurationDescription[] configs = prjDesc.getConfigurations();
        
        // Set them on all the languages of all the configurations
        for (ICConfigurationDescription config : configs) {
            ICFolderDescription folder = config.getRootFolderDescription();
            ICLanguageSetting[] languages = folder.getLanguageSettings();
            for (ICLanguageSetting lang : languages) {
                List<ICLanguageSettingEntry> entries = lang.getSettingEntriesList( pEntriesType );
                for ( ICLanguageSettingEntry oldEntry : pOldEntries ) {
                    if ( entries.contains( oldEntry ) ) {
                        entries.remove( oldEntry );
                    }
                }
                lang.setSettingEntries( pEntriesType, entries );
            }
        }
        
        try {
            CoreModel.getDefault().setProjectDescription( pProject, prjDesc );
        } catch ( CoreException e ) {
            PluginLogger.error( "Error setting the includes and libaries", e );
        }
    }
}
