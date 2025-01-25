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
 */
public class FilesFinder implements IResourceVisitor {

    private String[] mExtensions;
    private ArrayList<IFile> mFiles;
    private Set<IPath> mExcludedPaths = new HashSet<IPath>();

    /**
     * Constructor.
     *
     * @param extensions
     *            the file extensions to match
     */
    public FilesFinder(String[] extensions) {
        mExtensions = extensions;
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
    public boolean visit(IResource res) throws CoreException {

        boolean result = false;
        IPath resourcePath = res.getFullPath();
        if (!this.mExcludedPaths.contains(resourcePath)) {
            if (res.getType() == IResource.FILE) {
                boolean matches = false;
                String name = res.getName();

                int i = 0;
                while (i < mExtensions.length && !matches) {
                    matches = name.toLowerCase().endsWith(mExtensions[i].toLowerCase());
                    i++;
                }

                if (matches) {
                    mFiles.add((IFile) res);
                }
            }
            result = true;
        }

        return result;
    }

    /**
     * Add a path to exclude in the search.
     *
     * @param distPath
     *            the path to exclude
     */
    public void addExclude(IPath distPath) {
        this.mExcludedPaths.add(distPath);
    }
}
