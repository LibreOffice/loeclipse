/*************************************************************************
 *
 * $RCSfile: VisitableFile.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:27 $
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * Sun Microsystems Inc., October, 2000
 *
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
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
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 *
 * Copyright: 2002 by Sun Microsystems, Inc.
 *
 * All Rights Reserved.
 *
 * Contributor(s): Cedric Bosdonnat
 *
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.builders;

import java.io.File;

/**
 * A little tiny interface to visit File objects. This has to be used to avoid multiple and annoying project refresh
 * operations trigerring unwanted changes.
 */
public class VisitableFile {

    private File mFile;

    /**
     * Create a new visitable file, ready to accept a visit.
     *
     * @param pFile
     *            the file to visit later.
     */
    public VisitableFile(File pFile) {
        mFile = pFile;
    }

    /**
     * @return if the file exists
     */
    public boolean exists() {
        return mFile != null && mFile.exists();
    }

    /**
     * @return if the visitable file has been correctly initialised and is a directory
     */
    public boolean isDirectory() {
        return mFile != null && mFile.isDirectory();
    }

    /**
     * @return if the visitable file has been correctly initialised and is a file
     */
    public boolean isFile() {
        return mFile != null && mFile.isFile();
    }

    /**
     * Welcome a visitor and let him explore the file hierarchy as he needs to.
     *
     * @param pVisitor
     *            the File visitor
     */
    public void accept(IFileVisitor pVisitor) {
        if (pVisitor.visit(mFile) && isDirectory()) {
            String[] children = mFile.list();
            for (String child : children) {
                File fileChild = new File(mFile, child);
                VisitableFile visitableChild = new VisitableFile(fileChild);
                visitableChild.accept(pVisitor);
            }
        }
    }
}
