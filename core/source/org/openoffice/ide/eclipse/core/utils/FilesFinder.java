package org.openoffice.ide.eclipse.core.utils;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Visitor looking for all the files with given extensions.
 * 
 * @author cbosdo
 *
 */
public class FilesFinder implements IResourceVisitor {

    private String[] mExtensions;
    private ArrayList<IFile> mFiles;
    
    /**
     * Constructor.
     * 
     * @param pExtensions the file extensions to match
     */
    public FilesFinder( String[] pExtensions ) {
        mExtensions = pExtensions;
        mFiles = new ArrayList<IFile>(); 
    }
    
    /**
     * @return the found files
     */
    public ArrayList<IFile> getResults( ) {
        return mFiles;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean visit(IResource pResource) throws CoreException {
        
        if ( pResource.getType() == IResource.FILE ) {
            boolean matches = false;
            String name = pResource.getName();

            int i = 0;
            while ( i < mExtensions.length && !matches ) {
                matches = name.toLowerCase().endsWith( mExtensions[i].toLowerCase() );
                i++;
            }

            if ( matches ) {
                mFiles.add( ( IFile )pResource );
            }
        }
        
        return true;
    }
}
