/*************************************************************************
 *
 * $RCSfile: JavaWizardPage.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:38 $
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
package org.libreoffice.ide.eclipse.java;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.libreoffice.ide.eclipse.core.gui.rows.BooleanRow;
import org.libreoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.libreoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.libreoffice.ide.eclipse.core.model.UnoFactoryData;
import org.libreoffice.ide.eclipse.core.model.language.LanguageWizardPage;

/**
 * Java configuration page of the new UNO project wizard.
 */
public class JavaWizardPage extends LanguageWizardPage {

    public static final String JAVA_VERSION = "java_version"; //$NON-NLS-1$
    public static final String JAVA_TESTS = "java_tests"; //$NON-NLS-1$

    private BooleanRow mJavaTestsRow;

    private String mJavaVersion;
    private boolean mUseTests;

    /**
     * Constructor.
     */
    public JavaWizardPage() {
        super();
        setImageDescriptor(OOoJavaPlugin.getDefault().getImageRegistry().getDescriptor(OOoJavaPlugin.WIZBAN));
        setTitle(Messages.getString("JavaWizardPage.PageTitle")); //$NON-NLS-1$
        setDescription(Messages.getString("JavaWizardPage.PageDescription")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProjectInfos(UnoFactoryData pData) {
        // default values
        mJavaVersion = "java5"; //$NON-NLS-1$
        mUseTests = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnoFactoryData fillData(UnoFactoryData pData) {

        if (pData != null) {
            pData.setProperty(JAVA_VERSION, mJavaVersion);
            pData.setProperty(JAVA_TESTS, mUseTests);
        }

        return pData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(Composite pParent) {

        Composite body = new Composite(pParent, SWT.NONE);
        body.setLayout(new GridLayout(1, false));
        body.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Create the test row
        mJavaTestsRow = new BooleanRow(body, JAVA_TESTS,
            Messages.getString("JavaWizardPage.IncludeTestClasses")); //$NON-NLS-1$
        mJavaTestsRow.setValue(true);
        mJavaTestsRow.setFieldChangedListener(new IFieldChangedListener() {

            @Override
            public void fieldChanged(FieldEvent pEvent) {
                mUseTests = mJavaTestsRow.getBooleanValue();
            }
        });

        setControl(body);
    }
}
