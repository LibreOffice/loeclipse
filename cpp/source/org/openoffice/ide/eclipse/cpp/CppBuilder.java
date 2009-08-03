package org.openoffice.ide.eclipse.cpp;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.core.LogLevels;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.UnoPackage;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

public class CppBuilder implements ILanguageBuilder {

    private Language mLanguage;
    
    /**
     * Constructor.
     * 
     * @param pLanguage the C++ Language object
     */
    public CppBuilder( Language pLanguage ) {
        mLanguage = pLanguage;
    }
    
    @Override
    public IPath createLibrary(IUnoidlProject unoProject) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void fillUnoPackage(UnoPackage unoPackage, IUnoidlProject prj) {
        // TODO Auto-generated method stub

    }

    @Override
    public String[] getBuildEnv(IUnoidlProject unoProject) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void generateFromTypes(ISdk pSdk, IOOo pOoo, IProject pPrj,
            File pTypesFile, File pBuildFolder, String pRootModule,
            IProgressMonitor pMonitor) {
        
        if (pTypesFile.exists()) {

            try {

                if (null != pSdk && null != pOoo) {

                    String[] paths = pOoo.getTypesPath();
                    String oooTypesArgs = ""; //$NON-NLS-1$
                    for (String path : paths) {
                        IPath ooTypesPath = new Path (path);
                        oooTypesArgs += " \"" + ooTypesPath.toOSString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
                    }

                    String command = "cppumaker -T\"*\"" +  //$NON-NLS-1$
                    " -Gc -BUCR " +  //$NON-NLS-1$
                    "-O \"" + new File( pBuildFolder, "include" ).getAbsolutePath() + "\" \"" + //$NON-NLS-1$ //$NON-NLS-2$
                    pTypesFile.getAbsolutePath() + "\" " + //$NON-NLS-1$
                    oooTypesArgs; 

                    IUnoidlProject unoprj = ProjectsManager.getProject(pPrj.getName());
                    Process process = pSdk.runTool(unoprj,command, pMonitor);

                    LineNumberReader lineReader = new LineNumberReader(
                            new InputStreamReader(process.getErrorStream()));

                    // Only for debugging purpose
                    if (PluginLogger.isLevel(LogLevels.DEBUG)) {

                        String line = lineReader.readLine();
                        while (null != line) {
                            System.out.println(line);
                            line = lineReader.readLine();
                        }
                    }
                    
                    process.waitFor();
                    
                    // Check if the build/include dir is in the includes
                    ICProject cprj = CoreModel.getDefault().create( pPrj );
                    IPath incPath = unoprj.getBuildPath().append( "include" );
                    if ( !cprj.isOnSourceRoot( pPrj.getFolder( incPath ) ) ) {
                        try {
                            IIncludeEntry entry = CoreModel.newIncludeEntry( null, null, incPath );
                            IPathEntry[] entries = cprj.getRawPathEntries();
                            IPathEntry[] newEntries = new IPathEntry[ entries.length + 1 ];
                            System.arraycopy( entries, 0, newEntries, 0, entries.length );
                            newEntries[entries.length] = entry;
                            cprj.setRawPathEntries( newEntries, pMonitor );
                        } catch ( Exception e ) {
                            PluginLogger.warning( "Unable to add the local includes directory to the C++ project",e  );
                        }
                        
                    }
                }
            } catch (InterruptedException e) {
                PluginLogger.error(
                        "cppumaker code generation failed", e);
            } catch (IOException e) {
                PluginLogger.warning(
                        "Unreadable output error");
            }
        }
    }
}
