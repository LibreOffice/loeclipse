/*************************************************************************
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright: 2010 by Cédric Bosdonnat
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.launch.office;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.gui.PackageContentSelector;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * Class launching the selected uno project as a LibreOffice extension.
 */
public class OfficeLaunchShortcut implements ILaunchShortcut {

    private static final String OFFICE_LAUNCH_CONFIG_ID = "org.libreoffice.ide.eclipse.core.launchLibreOffice"; //$NON-NLS-1$

    /**
     * {@inheritDoc}
     */
    @Override
    public void launch(ISelection pSelection, String pMode) {
        if (pSelection instanceof IStructuredSelection) {
            IStructuredSelection sel = (IStructuredSelection) pSelection;
            Iterator<?> it = sel.iterator();

            IUnoidlProject project = null;
            while (it.hasNext() && project == null) {
                Object o = it.next();
                if (o instanceof IAdaptable) {
                    IAdaptable adaptable = (IAdaptable) o;
                    IResource res = adaptable.getAdapter(IResource.class);
                    if (res != null) {
                        project = ProjectsManager.getProject(res.getProject().getName());
                    }
                }
            }

            if (project != null) {
                launch(project, pMode);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void launch(IEditorPart pEditor, String pMode) {
        IEditorInput input = pEditor.getEditorInput();
        IFile file = input.getAdapter(IFile.class);

        if (file != null) {
            IUnoidlProject prj = ProjectsManager.getProject(file.getProject().getName());
            if (prj != null) {
                launch(prj, pMode);
            }
        }
    }

    /**
     * Create a default launch configuration for the UNO project.
     *
     * @param pProject
     *            the UNO project for which to create the default launch config
     * @return the newly created and saved launch configuration.
     */
    private ILaunchConfiguration createDefaultLaunchConfiguration(IUnoidlProject pProject) {
        ILaunchConfiguration created = null;
        try {
            ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
            ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(OFFICE_LAUNCH_CONFIG_ID);

            String name = launchManager.generateLaunchConfigurationName(pProject.getName());
            ILaunchConfigurationWorkingCopy createdConfiguration = type.newInstance(null, name);

            createdConfiguration.setAttribute(IOfficeLaunchConstants.PROJECT_NAME, pProject.getName());
            createdConfiguration.setAttribute(IOfficeLaunchConstants.CLEAN_USER_INSTALLATION, true);

            List<IFile> content = PackageContentSelector.getDefaultContent(pProject);
            String paths = new String();
            for (IFile file : content) {
                if (!paths.isEmpty()) {
                    paths += IOfficeLaunchConstants.PATHS_SEPARATOR;
                }
                paths += file.getProjectRelativePath().toString();
            }
            createdConfiguration.setAttribute(IOfficeLaunchConstants.CONTENT_PATHS, paths);

            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(pProject.getName());
            createdConfiguration.setMappedResources(new IResource[] { project });

            // Common Tab Arguments
            CommonTab tab = new CommonTab();
            tab.setDefaults(createdConfiguration);
            tab.dispose();

            created = createdConfiguration.doSave();
        } catch (CoreException e) {
            PluginLogger.error("Error creating the launch configuration", e);
            created = null;
        }

        return created;
    }

    /**
     * COPIED/MODIFIED from AntLaunchShortcut Returns a list of existing launch configuration for the given file.
     *
     * @param pProject
     *            the UNO project for which to look for existing launch configurations
     * @return the list of the matching launch configurations
     */
    protected List<ILaunchConfiguration> findExistingLaunchConfigurations(IUnoidlProject pProject) {
        ILaunchManager manager = org.eclipse.debug.core.DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type = manager.getLaunchConfigurationType(OFFICE_LAUNCH_CONFIG_ID);
        List<ILaunchConfiguration> validConfigs = new ArrayList<ILaunchConfiguration>();
        if (type != null) {
            try {
                ILaunchConfiguration[] configs = manager.getLaunchConfigurations(type);

                for (int i = 0; i < configs.length; i++) {
                    String projectName = configs[i].getAttribute(IOfficeLaunchConstants.PROJECT_NAME, "");
                    if (pProject.getName().equals(projectName)) {
                        validConfigs.add(configs[i]);
                    }
                }
            } catch (CoreException e) {
                PluginLogger.error("Unexpected error", e);
            }
        }
        return validConfigs;
    }

    /**
     * Launch a unoidl project using the default configuration.
     *
     * @param pProject
     *            the project to launch
     * @param pMode
     *            the mode of the launch
     */
    private void launch(IUnoidlProject pProject, String pMode) {
        ILaunchConfiguration conf = null;
        List<ILaunchConfiguration> configurations = findExistingLaunchConfigurations(pProject);
        if (configurations.isEmpty()) {
            conf = createDefaultLaunchConfiguration(pProject);
        } else {
            conf = configurations.get(0);
        }

        if (conf != null) {
            DebugUITools.launch(conf, pMode);
        }
    }

}
