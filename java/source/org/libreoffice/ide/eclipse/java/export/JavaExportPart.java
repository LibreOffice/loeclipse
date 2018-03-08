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
package org.libreoffice.ide.eclipse.java.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.language.LanguageExportPart;
import org.libreoffice.ide.eclipse.core.model.OOoContainer;
import org.libreoffice.ide.eclipse.core.model.SDKContainer;
import org.libreoffice.ide.eclipse.java.Messages;
import org.libreoffice.ide.eclipse.java.utils.TemplatesHelper;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * Dialog part for the Ant scripts export configuration.
 */
public class JavaExportPart extends LanguageExportPart {

    private Button mSaveScripts;
    private Composite mNameRow;
    private Label mNameRowLbl;
    private Label mNameRowValueLbl;

    private JavaExportPageControl mController;
    private static AntScriptExportWizardPage mAntScriptPage;

    public static final int HORIZONTAL_INDENT = 20;

    /**
     * @param pAntScriptPage
     *            Helps in retrieving the project selected in this part.
     */
    public static void setAntScriptExportPage(AntScriptExportWizardPage pAntScriptPage) {
        mAntScriptPage = pAntScriptPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControls(Composite pParent) {

        mController = new JavaExportPageControl();

        Label titleLbl = new Label(pParent, SWT.NONE);
        titleLbl.setText(Messages.getString("JavaExportPart.Title")); //$NON-NLS-1$
        titleLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        Composite content = new Composite(pParent, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalIndent = HORIZONTAL_INDENT;
        content.setLayoutData(gd);
        content.setLayout(new GridLayout());

        mController.setSaveAntScript(true);

        mNameRow = new Composite(content, SWT.NONE);
        mNameRow.setLayout(new GridLayout(2, false));
        gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        gd.horizontalIndent = HORIZONTAL_INDENT;
        mNameRow.setLayoutData(gd);

        mNameRowLbl = new Label(mNameRow, SWT.NONE);
        mNameRowLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        mNameRowLbl.setText(Messages.getString("JavaExportPart.AntFile")); //$NON-NLS-1$

        mNameRowValueLbl = new Label(mNameRow, SWT.NONE);
        mNameRowValueLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        mNameRowValueLbl.setText(mController.getSavePath()); //$NON-NLS-1$

        mNameRowLbl.setEnabled(mController.isSavePathEnabled());
        mNameRowValueLbl.setEnabled(mController.isSavePathEnabled());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (mSaveScripts != null) {
            mSaveScripts.dispose();
            mNameRow.dispose();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFinish(UnoPackage pModel) {
        String directory = mAntScriptPage.getPath();
        File tmpDir = new File(directory + "/build.xml");
        boolean result = false;

        if (tmpDir.exists()) {
            Shell shell = Display.getDefault().getActiveShell();
            result = MessageDialog.openConfirm(shell, "Confirm",
                "build.xml exists under the selected project.\nDo you wish to overwrite it?");
        } else {
            result = true;
        }

        if (mController.getSaveAntScript() && result) {

            // Generate the build script
            //****IUnoidlProject unoProject = getPage().getProject();   <--- Can be used When the parent class LanguageExportPart is using the Object Class rather than ManifestExportPage
            IUnoidlProject unoProject = mAntScriptPage.getProject();
            String prjName = unoProject.getName();
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(unoProject.getName());

            TemplatesHelper.copyTemplate(project, mController.getSavePath(),
                JavaExportPart.class, new String(), prjName);

            // Generate the build.properties file
            File dir = project.getFile(mController.getSavePath()).getLocation().toFile().getParentFile();
            File propsFile = new File(dir, "build.properties"); //$NON-NLS-1$
            FileWriter writer = null;

            try {
                writer = new FileWriter(propsFile);

                Properties props = new Properties();

                if (OOoContainer.getOOoKeys().size() > 0)
                    props.put("office.install.dir", //$NON-NLS-1$
                        OOoContainer.getOOo(OOoContainer.getOOoKeys().get(0)).getHome());
                else
                    props.put("office.install.dir", ""); //$NON-NLS-1$

                if (SDKContainer.getSDKKeys().size() > 0)
                    props.put("sdk.dir", SDKContainer.getSDK(SDKContainer.getSDKKeys().get(0)).getHome()); //$NON-NLS-1$
                else
                    props.put("sdk.dir", ""); //$NON-NLS-1$

                props.store(writer, null);
                writer.close();

            } catch (IOException e) {
                PluginLogger.error(Messages.getString("JavaExportPart.BuildPropertiesError"), e); //$NON-NLS-1$
            }

        }
    }
}
