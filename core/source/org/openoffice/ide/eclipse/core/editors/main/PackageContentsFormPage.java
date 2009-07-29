/*************************************************************************
 *
 * $RCSfile: PackageContentsFormPage.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:28 $
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
package org.openoffice.ide.eclipse.core.editors.main;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openoffice.ide.eclipse.core.editors.Messages;
import org.openoffice.ide.eclipse.core.editors.PackagePropertiesEditor;

/**
 * Page displaying the Package properties in a more user friendly way.
 * 
 * @author cedricbosdo
 *
 */
public class PackageContentsFormPage extends FormPage {
    
    private ContentsSection mContents;
    private LibsSection mLibs;
    private PackageDescriptionSection mDescriptions;
    
    /**
     * Content form page constructor.
     * 
     * @param pEditor the editor where to create the form page
     * @param pId the page identifier
     */
    public PackageContentsFormPage(FormEditor pEditor, String pId) {
        super(pEditor, pId, 
                Messages.getString("PackagePropertiesFormPage.PackagePropertiesText")); //$NON-NLS-1$
    }

    /**
     * @return the project for which the contents are shown
     */
    public IProject getProject() {
        IProject prj = null;
        if (getEditorInput() instanceof IFileEditorInput) {
            prj = ((IFileEditorInput)getEditorInput()).getFile().getProject();
        }
        return prj;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFormContent(IManagedForm pManagedForm) {
        super.createFormContent(pManagedForm);
        ScrolledForm form = pManagedForm.getForm();
        
        FormToolkit toolkit = pManagedForm.getToolkit();
        toolkit.decorateFormHeading( form.getForm() );
        
        form.setText(Messages.getString("PackagePropertiesFormPage.PackagePropertiesText")); //$NON-NLS-1$
        
        // Create the only section with a tree representing the files 
        // and dirs in its client area
        form.getBody().setLayout(new GridLayout(2, true));
        
        mContents = new ContentsSection(this);
        mLibs = new LibsSection(this);
        mDescriptions = new PackageDescriptionSection(this);
        
        // update the model from the source
        PackagePropertiesEditor editor = (PackagePropertiesEditor)getEditor();
        editor.getModel().setQuiet(true);
        
        editor.loadFromSource();

        List<IResource> contents = editor.getModel().getContents();
        mContents.setContents(contents);
        
        // Get the Libs and Descriptions properties from the document
        
        mLibs.setLibraries(editor.getModel());
        
        mDescriptions.setDescriptions(editor.getModel().getDescriptionFiles());
        editor.getModel().setQuiet(false);
    }
}
