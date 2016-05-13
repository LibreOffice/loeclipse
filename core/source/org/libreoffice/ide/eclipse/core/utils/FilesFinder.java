package org.libreoffice.ide.eclipse.core.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Visitor looking for all the files with given extensions.
 *
 *
 */
public class FilesFinder implements IResourceVisitor {

    private String[] mExtensions;
    private ArrayList<IFile> mFiles;
    private Set<IPath> mExcludedPaths = new HashSet<IPath>();

    /**
     * Constructor.
     *
     * @param pExtensions
     *            the file extensions to match
     */
    public FilesFinder(String[] pExtensions) {
        mExtensions = pExtensions;
        mFiles = new ArrayList<IFile>();
    }

    /**
     * @return the found files
     */
    public ArrayList<IFile> getResults() {
        return mFiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(IResource pResource) throws CoreException {

        boolean result = false;
        IPath resourcePath = pResource.getFullPath();
        if (!this.mExcludedPaths.contains(resourcePath)) {
            if (pResource.getType() == IResource.FILE) {
                boolean matches = false;
                String name = pResource.getName();

                int i = 0;
                while (i < mExtensions.length && !matches) {
                    matches = name.toLowerCase().endsWith(mExtensions[i].toLowerCase());
                    i++;
                }

                if (matches) {
                    mFiles.add((IFile) pResource);
                }
            }
            result = true;
        }

        return result;
    }

    /**
     * Add a path to exclude in the search.
     *
     * @param pDistPath
     *            the path to exclude
     */
    public void addExclude(IPath pDistPath) {
        this.mExcludedPaths.add(pDistPath);
    }
}
