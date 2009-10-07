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
package org.openoffice.ide.eclipse.java.client;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.java.build.OOoClasspathContainer;

/**
 * The first page of the UNO Java client wizard overrides the default
 * New Java project first page to add the OOo libraries as default dependencies.
 * 
 * <p><strong>Many thanks to Karl Weber for pointing that and providing 
 * sample code.</strong></p>
 * 
 * @author cbosdonnat
 *
 */
public class ClientWizardPageOne extends NewJavaProjectWizardPageOne {

    private UnoConnectionPage mCnxPage;
    
    /**
     * Constructor.
     * 
     * @param pCnxPage the connection page of the wizard
     */
    public ClientWizardPageOne( UnoConnectionPage pCnxPage ) {
        super();
        mCnxPage = pCnxPage;
    }
    
    @Override
    public IClasspathEntry[] getDefaultClasspathEntries() {
        IClasspathEntry[] oldEntries = super.getDefaultClasspathEntries();
        IClasspathEntry[] entries = new IClasspathEntry[ oldEntries.length + 1 ];
        
        System.arraycopy( oldEntries, 0, entries, 0, oldEntries.length );
        
        IOOo ooo = mCnxPage.getOoo();
        IPath path = new Path(OOoClasspathContainer.ID + IPath.SEPARATOR + ooo.getName());
        IClasspathEntry oooEntry = JavaCore.newContainerEntry(path);
        entries[ entries.length - 1 ] = oooEntry;
        
        return entries;
    }
}
