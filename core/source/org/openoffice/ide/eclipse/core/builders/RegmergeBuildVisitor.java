/*************************************************************************
 *
 * $RCSfile: RegmergeBuildVisitor.java,v $
 *
 * $Revision: 1.6 $
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
package org.openoffice.ide.eclipse.core.builders;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;

/**
 * Class visiting each child of the urd folder to merge it with the common
 * <code>types.rdb</code> registry.
 * 
 * @author cbosdonnat
 *
 */
public class RegmergeBuildVisitor implements IFileVisitor {

    /**
     * Progress monitor used during all the visits.
     */
    private IProgressMonitor mProgressMonitor; 
    private IUnoidlProject mUnoprj;
    
    /**
     * Default constructor.
     * 
     * @param pUnoprj the UNO project to visit
     * @param pMonitor progress monitor for the regmerge
     */
    public RegmergeBuildVisitor(IUnoidlProject pUnoprj, IProgressMonitor pMonitor) {
        super();
        mProgressMonitor = pMonitor;
        mUnoprj = pUnoprj;
    }

    /**
     * {@inheritDoc}
     */
    public boolean visit(File pResource) {
        
        boolean visitChildren = false;
        
        if (pResource.isFile()) {
            
            // Try to compile the file if it is an idl file
            if (pResource.getName().endsWith("urd")) { //$NON-NLS-1$
                
                RegmergeBuilder.runRegmergeOnFile(pResource, mUnoprj, mProgressMonitor);
                if (mProgressMonitor != null) {
                    mProgressMonitor.worked(1);
                }
            }
            
        } else if (pResource.isDirectory()) {
            String urdBasis = UnoidlProjectHelper.URD_BASIS;
            if (Platform.getOS().equals(Platform.OS_WIN32)) {
                urdBasis = urdBasis.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (pResource.getAbsolutePath().contains(urdBasis)) {   
                visitChildren = true;
            }
            
        } else {
            PluginLogger.debug("Non handled resource"); //$NON-NLS-1$
        }
        
        // helps cleaning
        mProgressMonitor = null;
        
        return visitChildren;
    }
}
