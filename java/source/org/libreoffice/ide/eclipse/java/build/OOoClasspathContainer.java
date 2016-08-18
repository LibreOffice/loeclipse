/*************************************************************************
 *
 * $RCSfile: OOoClasspathContainer.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/12/26 14:40:18 $
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
package org.libreoffice.ide.eclipse.java.build;

import java.text.MessageFormat;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.java.JavaProjectHandler;

/**
 * Container for the OOo classes jars.
 */
public class OOoClasspathContainer implements IClasspathContainer {

    public static final String ID = "org.libreoffice.ide.eclipse.java.OOO_CONTAINER"; //$NON-NLS-1$

    private IOOo mOOo;

    private IPath mPath;

    /**
     * Constructor.
     *
     * @param pOoo the OOo represented by the container.
     * @param pPath the path used for the container.
     */
    public OOoClasspathContainer(IOOo pOoo, IPath pPath) {
        mOOo = pOoo;
        mPath = pPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IClasspathEntry[] getClasspathEntries() {
        Vector<Path> jars = JavaProjectHandler.findJarsFromPath(mOOo);
        Vector<IClasspathEntry> entries = new Vector<IClasspathEntry>();

        for (Path path : jars) {
            entries.add(JavaCore.newLibraryEntry(path, null, null));
        }
        return entries.toArray(new IClasspathEntry[entries.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        String pattern = Messages.getString("OOoClasspathContainer.LibrariesName"); //$NON-NLS-1$
        String descr = MessageFormat.format(pattern, mOOo.getName());
        return descr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getKind() {
        return K_APPLICATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPath getPath() {
        return mPath;
    }

}
