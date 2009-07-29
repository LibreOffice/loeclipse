/*************************************************************************
 *
 * $RCSfile: PackageLicenseFormPage.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:51 $
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

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openoffice.ide.eclipse.core.editors.Messages;

/**
 * The form page of the package editor helping to configure the project's
 * licenses.
 * 
 * @author cedricbosdo
 *
 */
public class PackageLicenseFormPage extends FormPage {

    /**
     * Constructor.
     * 
     * @param pEditor the editor where to add the page
     * @param pId the page identifier
     */
    public PackageLicenseFormPage(FormEditor pEditor, String pId) {
        super(pEditor, pId, Messages.getString("PackageLicenseFormPage.Title")); //$NON-NLS-1$
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
        
        // TODO create the license form page here
    }
}
