/*************************************************************************
 *
 * $RCSfile: CreateServicesAction.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/11 18:39:53 $
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
package org.openoffice.ide.eclipse.core.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.openoffice.ide.eclipse.core.builders.ServicesBuilder;

/**
 * Action running the services.rdb file creation
 * 
 * @author cedricbosdo
 *
 */
public class CreateServicesAction implements IActionDelegate {

	private ISelection mSelection;
	
	public void run(IAction action) {

		if (mSelection instanceof IStructuredSelection) {

			IStructuredSelection structSel = (IStructuredSelection)mSelection;
			if (!structSel.isEmpty()) {
				Object o = structSel.getFirstElement();
				if (o instanceof IProject) {
					IProject project = (IProject)o;
					ServicesBuilder builder = new ServicesBuilder(project);
					builder.schedule();
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		mSelection = selection;
	}
}
