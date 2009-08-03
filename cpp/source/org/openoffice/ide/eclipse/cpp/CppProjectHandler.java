package org.openoffice.ide.eclipse.cpp;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.language.IProjectHandler;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

public class CppProjectHandler implements IProjectHandler {

    @Override
    public void addLanguageDependencies(IUnoidlProject unoproject,
            IProgressMonitor monitor) throws CoreException {
        
        ISdk sdk = unoproject.getSdk();
        ISourceEntry sourceEntry = CoreModel.newSourceEntry( unoproject.getSourcePath() );
        IIncludeEntry includesEntry = CoreModel.newIncludeEntry( null, null, 
                sdk.getIncludePath(), true ); 
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( unoproject.getName() );
        ICProject cprj = CoreModel.getDefault().create( prj );
        
        cprj.setRawPathEntries(new IPathEntry[]{ 
                sourceEntry, includesEntry }, monitor);
    }

    @Override
    public void addOOoDependencies(IOOo ooo, IProject project) {
        // TODO Auto-generated method stub

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
            MakeProjectNature.addNature( pProject, null ) ;
            
            PluginLogger.debug( "C++ project nature set" );
            
        } catch (CoreException e) {
            PluginLogger.error( "Failed to set C++ project nature" );
        }
    }

    @Override
    public void configureProject(UnoFactoryData data) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void createRegistrationSystem(IUnoidlProject prj) {
        // TODO Auto-generated method stub

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

    @Override
    public void removeOOoDependencies(IOOo ooo, IProject project) {
        // TODO Auto-generated method stub

    }
}
