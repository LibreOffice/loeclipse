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
package org.openoffice.ide.eclipse.core.model.language;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;

/**
 * Base class for the language extensions.
 * 
 * @author cbosdo
 * 
 */
public abstract class AbstractLanguage {

    private IConfigurationElement mConfig;

    /**
     * @param pConfig
     *            the configuration element for the language
     */
    protected void setConfigurationElement(IConfigurationElement pConfig) {
        mConfig = pConfig;
    }

    /**
     * @return the language display name
     */
    public String getName() {
        return mConfig.getAttribute("name"); //$NON-NLS-1$
    }

    /**
     * @return the wizard page for the New UNO project wizard or <code>null</code> if none has been defined.
     */
    public LanguageWizardPage getNewWizardPage() {
        LanguageWizardPage result = null;
        IConfigurationElement[] children = mConfig.getChildren("newWizardPage"); //$NON-NLS-1$
        if (children.length > 0) {
            // There can't be more than one
            try {
                Object o = children[0].createExecutableExtension("class"); //$NON-NLS-1$
                if (o instanceof LanguageWizardPage) {
                    result = (LanguageWizardPage) o;
                }
            } catch (Exception e) {
            }
        }
        return result;
    }

    /**
     * @return the export build part for the UNO export wizard or <code>null</code> if none has been defined.
     */
    public LanguageExportPart getExportBuildPart() {
        LanguageExportPart result = null;
        IConfigurationElement[] children = mConfig.getChildren("exportBuildPart"); //$NON-NLS-1$
        if (children.length > 0) {
            // There can't be more than one
            try {
                Object o = children[0].createExecutableExtension("class"); //$NON-NLS-1$
                if (o instanceof LanguageExportPart) {
                    result = (LanguageExportPart) o;
                }
            } catch (Exception e) {
            }
        }
        return result;
    }

    /**
     * @return the utility class for projects handling.
     */
    public abstract IProjectHandler getProjectHandler();

    /**
     * @return the utility class for building.
     */
    public abstract ILanguageBuilder getLanguageBuidler();

    /**
     * Launch OpenOffice for debugging and connect the eclipse debugger to it. 
     * Currently only Java debugging is supported.
     * 
     * @param pPrj the target project.
     * @param pLaunch the launch configuration to add our debug target to.
     * @param pUserInstallation user profile.
     * @param pMonitor monitor to report progress to.
     */
    public abstract void connectDebuggerToOpenOffice(IUnoidlProject pPrj, ILaunch pLaunch, IPath pUserInstallation,
                    IProgressMonitor pMonitor);

    /**
     * When in debug mode, we have to configure the 
     * appropriate source locator for the respective language.
     * 
     * The rest will be taken care by the {@link SourceLookupTab}.
     * 
     * @param pConfiguration the configuration to add extra attributes to.
     * @throws CoreException if something went wrong.
     */
    public abstract void configureSourceLocator(ILaunchConfigurationWorkingCopy pConfiguration) throws CoreException;
}
