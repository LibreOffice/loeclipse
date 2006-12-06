/*************************************************************************
 *
 * $RCSfile: UreTab.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/12/06 07:49:25 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.core.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.UnoProjectProvider;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

public class UreTab extends AbstractLaunchConfigurationTab {

	private class ChangeListener implements SelectionListener, ModifyListener {
		
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}

		public void widgetSelected(SelectionEvent e) {
			
			if (e.getSource().equals(mProjectBtn)) {
				ILabelProvider labelProvider = new UnoProjectProvider();
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(
						getShell(), labelProvider);
				dialog.setTitle(Messages.getString("UreTab.ProjectChooserTitle")); //$NON-NLS-1$
				dialog.setMessage(Messages.getString("UreTab.ProjectChooserMessage")); //$NON-NLS-1$
				dialog.setElements(ProjectsManager.getInstance().getProjects());

				if (dialog.open() == Window.OK) {
					mProject = (IUnoidlProject)dialog.getFirstResult();
					mProjectTxt.setText(mProject.getName());
					setDirty(true);
					getLaunchConfigurationDialog().updateButtons();
				}
			} else if (e.getSource().equals(mMainBtn)) {
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(
						getShell(), new LabelProvider(){
							@Override
							public String getText(Object element) {
								String label = null;
								if (element instanceof String) {
									label = (String)element;
								}
								return label;
							}
						});
				dialog.setTitle(Messages.getString("UreTab.MainImplementationChooserTitle")); //$NON-NLS-1$
				dialog.setMessage(Messages.getString("UreTab.MainImplementationChooserMessage")); //$NON-NLS-1$
				dialog.setElements(new MainImplementationsProvider().getImplementations(mProject));
				
				if (dialog.open() == Window.OK) {
					mMainTxt.setText((String)dialog.getFirstResult());
					setDirty(true);
					getLaunchConfigurationDialog().updateButtons();
				}
			}
		}

		public void modifyText(ModifyEvent e) {
			
			if (e.getSource() == mProjectTxt) {
				IUnoidlProject prj = ProjectsManager.getInstance().getProject(
						mProjectTxt.getText().trim());
				if (prj != null) {
					mProject = prj;
					setErrorMessage(null);
				} else {
					mProject = null;
					setErrorMessage(Messages.getString("UreTab.ProjectChooserError")); //$NON-NLS-1$
				}
				getLaunchConfigurationDialog().updateMessage();
			}
			setDirty(true);
			getLaunchConfigurationDialog().updateButtons();
		}
	}
	
	private IUnoidlProject mProject;
	
	private Text mProjectTxt;
	private Button mProjectBtn;
	private Text mMainTxt;
	private Button mMainBtn;
	private Text mArgumentsTxt;
	
	private ChangeListener mListener = new ChangeListener();
	
	public void createControl(Composite parent) {
		
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout());
		
		Group group = new Group(comp, SWT.NONE);
		group.setText(Messages.getString("UreTab.RunApplicationLabel")); //$NON-NLS-1$
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		createProjectField(group);
		
		createMainField(group);
		
		Group argGroup = new Group(comp, SWT.NONE);
		argGroup.setText(Messages.getString("UreTab.ApplicationArgsLabel")); //$NON-NLS-1$
		argGroup.setLayout(new GridLayout());
		argGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		mArgumentsTxt = new Text(argGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		mArgumentsTxt.setLayoutData(new GridData(GridData.FILL_BOTH));
		mArgumentsTxt.addModifyListener(mListener);
		
		setControl(comp);
	}
	
	public Image getImage() {
		return OOEclipsePlugin.getImage(ImagesConstants.URE_APP);
	}
	
	public String getName() {
		return Messages.getString("UreTab.TabTitle"); //$NON-NLS-1$
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			mProjectTxt.setText(configuration.getAttribute(
					IUreLaunchConstants.PROJECT_NAME, "")); //$NON-NLS-1$
			mMainTxt.setText(configuration.getAttribute(
					IUreLaunchConstants.MAIN_TYPE, "")); //$NON-NLS-1$
			mArgumentsTxt.setText(configuration.getAttribute(
					IUreLaunchConstants.PROGRAM_ARGS, "")); //$NON-NLS-1$
		} catch (CoreException e) {
			PluginLogger.error(Messages.getString("UreTab.ConfigurationError"), e); //$NON-NLS-1$
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		
		configuration.setAttribute(IUreLaunchConstants.PROJECT_NAME, 
				mProjectTxt.getText().trim());
		configuration.setAttribute(IUreLaunchConstants.MAIN_TYPE, 
				mMainTxt.getText().trim());
		configuration.setAttribute(IUreLaunchConstants.PROGRAM_ARGS, 
				mArgumentsTxt.getText().trim());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
	}
	
	public boolean isValid(ILaunchConfiguration launchConfig) {
		
		try {
			if (launchConfig.getAttribute(IUreLaunchConstants.PROJECT_NAME, "").equals("") || //$NON-NLS-1$ //$NON-NLS-2$
					launchConfig.getAttribute(IUreLaunchConstants.MAIN_TYPE, "").equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			} else {
				String name = launchConfig.getAttribute(
						IUreLaunchConstants.PROJECT_NAME, ""); //$NON-NLS-1$
				return (ProjectsManager.getInstance().getProject(name) != null);
			}
		} catch (CoreException e) {
			return false;
		}
	}
	
	private void createProjectField(Composite parent) {
		Composite field = new Composite(parent, SWT.NONE);
		field.setLayout(new GridLayout(3, false));
		field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label lbl = new Label(field, SWT.NONE);
		lbl.setText(Messages.getString("UreTab.ProjectLabel")); //$NON-NLS-1$
		lbl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		mProjectTxt = new Text(field, SWT.SINGLE | SWT.BORDER); 
		mProjectTxt.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		mProjectTxt.addModifyListener(mListener);
		
		mProjectBtn = new Button(field, SWT.PUSH);
		mProjectBtn.setText("..."); //$NON-NLS-1$
		mProjectBtn.addSelectionListener(mListener);
	}
	
	private void createMainField(Composite parent) {
		Composite field = new Composite(parent, SWT.NONE);
		field.setLayout(new GridLayout(3, false));
		field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label lbl = new Label(field, SWT.NONE);
		lbl.setText(Messages.getString("UreTab.MainImplementationLabel")); //$NON-NLS-1$
		lbl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		mMainTxt = new Text(field, SWT.SINGLE | SWT.BORDER); 
		mMainTxt.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		mMainTxt.addModifyListener(mListener);
		
		mMainBtn = new Button(field, SWT.PUSH);
		mMainBtn.setText("..."); //$NON-NLS-1$
		mMainBtn.addSelectionListener(mListener);
	}
}
