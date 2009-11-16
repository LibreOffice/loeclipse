package org.openoffice.ide.eclipse.java.build;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public class FilesVisitor implements IResourceVisitor {

    ArrayList<IFile> mFiles = new ArrayList<IFile>();
    ArrayList<IResource> mExceptions = new ArrayList<IResource>();
    
    public void addException( IResource pRes ) {
        mExceptions.add( pRes );
    }
    
    public boolean visit(IResource pResource) throws CoreException {
        
        if ( pResource.getType() == IResource.FILE ) {
            mFiles.add( ( IFile )pResource );
        }
        
        boolean visitChildren = true;
        
        int i = 0;
        while ( visitChildren && i < mExceptions.size() ) {
            visitChildren = !mExceptions.get( i ).equals( pResource );
            i++;
        }
        
        return visitChildren;
    }

    public IFile[] getFiles( ) {
        return mFiles.toArray( new IFile[ mFiles.size() ] );
    }
}
