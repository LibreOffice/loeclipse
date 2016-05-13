/*************************************************************************
 *
 * $RCSfile: LanguageWizardPage.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:30 $
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
package org.libreoffice.ide.eclipse.core.model.language;

import org.eclipse.jface.wizard.WizardPage;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;

/**
 * This abstract class has to be implemented to add a language specific configuration page in the project wizard.
 *
 * <p>
 * Implementations should be aware that the {@link #fillData(UnoFactoryData)} method can be called even if the page
 * content has not been created.
 * </p>
 *
 *
 */
public abstract class LanguageWizardPage extends WizardPage {

    /**
     * Constructor.
     */
    public LanguageWizardPage() {
        super("language"); //$NON-NLS-1$
    }

    /**
     * Fills the page with the project creation informations.
     *
     * @param pData
     *            the data describing the informations to set.
     */
    public abstract void setProjectInfos(UnoFactoryData pData);

    /**
     * @param pData
     *            to fill with the language specific data.
     *
     * @return the given data with the completed properties, <code>null</code> if the provided data is <code>null</code>
     */
    public abstract UnoFactoryData fillData(UnoFactoryData pData);
}
