/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Cédric Bosdonnat
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
 * Copyright: 2009 by Cédric Bosdonnat
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.java.build;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * Resource visitor collecting the files of a directory.
 */
public class FilesVisitor implements IResourceVisitor {

    ArrayList<IFile> mFiles = new ArrayList<IFile>();
    ArrayList<IResource> mExceptions = new ArrayList<IResource>();

    /**
     * Adds a resource to skip during the visit.
     *
     * @param pRes the resource to skip
     */
    public void addException(IResource pRes) {
        mExceptions.add(pRes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(IResource res) throws CoreException {

        if (res.getType() == IResource.FILE) {
            mFiles.add((IFile) res);
        }

        boolean visitChildren = true;

        int i = 0;
        while (visitChildren && i < mExceptions.size()) {
            visitChildren = !mExceptions.get(i).equals(res);
            i++;
        }

        return visitChildren;
    }

    /**
     * @return all the files found during the visit.
     */
    public IFile[] getFiles() {
        return mFiles.toArray(new IFile[mFiles.size()]);
    }
}
