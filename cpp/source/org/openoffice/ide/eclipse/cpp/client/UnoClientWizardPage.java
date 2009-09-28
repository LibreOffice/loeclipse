/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 * 
 ************************************************************************/
package org.openoffice.ide.eclipse.cpp.client;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.jface.wizard.IWizardPage;

/**
 * Class for the main UNO client wizard page.
 * 
 * This page is derived from the CDT class, but some restrictions are 
 * applied to the selectable projects to fit the UNO Client case.
 *  
 * @author cbosdonnat
 *
 */
public class UnoClientWizardPage extends CDTMainWizardPage {

    private UnoConnectionPage mUnoCnxPage;
    
    /**
     * Creates a new project creation wizard page.
     *
     * @param pPageName the name of this page
     * @param pUnoPage the page for the UNO connection config
     */
    public UnoClientWizardPage(String pPageName, UnoConnectionPage pUnoPage ) {
        super(pPageName);
        mUnoCnxPage = pUnoPage;
        setPageComplete(false);
    }
    
    /**
     * Only show the executable project types here.
     * 
     * @param pItems the items to filter.
     * @return the items to show
     */
    @SuppressWarnings("unchecked")
    @Override
    public List filterItems(List pItems) {
        ArrayList filtered = new ArrayList();
        
        for (Object item : pItems) {
            EntryDescriptor desc = (EntryDescriptor)item;
            String parentId = desc.getParentId();
            boolean exeParent = parentId != null && 
                parentId.equals( ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_EXE );
            boolean exe = desc.getId().equals( ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_EXE );
            
            boolean kept = exeParent || exe;
            if ( kept ) {
                filtered.add( item );
            }
        }
        
        return filtered;
    }
    
    /**
     * @return the Uno connection page, instead of the normal CDT next page.
     */
    @Override
    public IWizardPage getNextPage() {
        return mUnoCnxPage;
    }
    
    /**
     * @return the normal CDT next page
     */
    public IWizardPage getNextCdtPage( ) {
        return super.getNextPage();
    }
}
