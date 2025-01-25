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
package org.libreoffice.ide.eclipse.core.export;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.language.LanguageExportPart;
import org.libreoffice.ide.eclipse.core.Messages;
import org.libreoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.libreoffice.ide.eclipse.core.utils.TemplatesHelper;

/**
 * Dialog part for the Ant scripts export configuration.
 */
public class ProjectExportPart extends LanguageExportPart {

    public static final int HORIZONTAL_INDENT = 20;

    private static AntScriptExportWizardPage sAntScriptPage;
    private static boolean sAntSectionDisplay = false;

    private Composite mNameRow;
    private Label mNameRowLbl;
    private Label mNameRowValueLbl;
    private Label mTitleLbl;

    private ProjectExportPageControl mController;

    /**
     * @param antScriptPage
     *            Helps in retrieving the project selected in this part.
     */
    public static void setAntScriptExportPage(AntScriptExportWizardPage antScriptPage) {
        sAntScriptPage = antScriptPage;
    }

    /**
     * @param antSectionDisplay
     *            Helps in blocking the Title and Other Displays in wizard after 1st time
     */
    public static void setCheckAntSectionDisplay(boolean antSectionDisplay) {
        sAntSectionDisplay = antSectionDisplay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControls(Composite parent) {

        mController = new ProjectExportPageControl();
        mController.setSaveAntScript(true);

        if (!sAntSectionDisplay) {
            mTitleLbl = new Label(parent, SWT.NONE);
            mTitleLbl.setText(Messages.getString("ProjectExportPart.Title")); //$NON-NLS-1$
            mTitleLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

            Composite content = new Composite(parent, SWT.NONE);
            GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
            gd.horizontalIndent = HORIZONTAL_INDENT;
            content.setLayoutData(gd);
            content.setLayout(new GridLayout());

            mNameRow = new Composite(content, SWT.NONE);
            mNameRow.setLayout(new GridLayout(2, false));
            gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
            gd.horizontalIndent = HORIZONTAL_INDENT;
            mNameRow.setLayoutData(gd);

            mNameRowLbl = new Label(mNameRow, SWT.NONE);
            mNameRowLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            mNameRowLbl.setText(Messages.getString("ProjectExportPart.AntFile")); //$NON-NLS-1$

            mNameRowValueLbl = new Label(mNameRow, SWT.NONE);
            mNameRowValueLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            mNameRowValueLbl.setText(mController.getSavePath()); //$NON-NLS-1$

            mNameRowLbl.setEnabled(mController.isSavePathEnabled());
            mNameRowValueLbl.setEnabled(mController.isSavePathEnabled());

        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFinish() {

        String directory = sAntScriptPage.getPath();
        File antFile = new File(directory + "/build.xml");
        boolean result = false;

        if (antFile.exists()) {
            Shell shell = Display.getDefault().getActiveShell();
            result = MessageDialog.openConfirm(shell, "Confirm",
                "build.xml exists under the selected project.\nDo you wish to overwrite it?");
        } else {
            result = true;
        }

        if (mController.getSaveAntScript() && result) {

            // Generate the build script
            // IUnoidlProject unoProject = getPage().getProject(); <- Can be used When the parent class
            // LanguageExportPart is using the Object Class rather than ManifestExportPage
            IUnoidlProject unoProject = sAntScriptPage.getProject();
            String prjName = unoProject.getName();
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(prjName);

            TemplatesHelper.copyTemplate(project, mController.getSavePath(),
                ProjectExportPart.class, new String(), prjName);

            // Create build.properties file if not exist
            String path = unoProject.getProjectPath().toOSString(); //$NON-NLS-1$
            File buildFile = new File(path, UnoidlProject.BUILD_FILE); //$NON-NLS-1$ //$NON-NLS-2$
            if (!buildFile.exists()) {
                unoProject.saveBuildProperties(buildFile);
            }
        }
    }
}
