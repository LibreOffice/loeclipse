/*************************************************************************
 *
 * $RCSfile: UnoProjectPulldownAction.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:31 $
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
package org.openoffice.ide.eclipse.core.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Pulldown action for the uno projects. The wizard has to be defined as follows:
 * <pre>
 *        &lt;wizard
 *           canFinishEarly="false"
 *           category="org.openoffice.ide.eclipse.core"
 *           hasPages="true"
 *           icon="icons/newunoproject.gif"
 *           id="org.openoffice.ide.eclipse.core.newunoproject"
 *           name="%wizards.unoidlproject"
 *           project="true"&gt;
 *        &lt;class class="org.openoffice.ide.eclipse.core.wizards.NewUnoProjectWizard"&gt;
 *            &lt;parameter name="unoproject" value="true"/&gt;
 *        &lt;/class&gt;
 *        &lt;description&gt;
 *           Create an empty UNO component with a service and it's implementation.
 *        &lt;/description&gt;
 *     &lt;/wizard&gt;
 * </pre>
 *
 * @author cedricbosdo
 *
 */
public class UnoProjectPulldownAction extends AbstractPulldownAction {

    /**
     * Projects pulldown actions constructor.
     */
    public UnoProjectPulldownAction() {
        super("unoproject"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IAction pAction) {
        new NewUnoProjectAction().run(pAction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidSelection(IStructuredSelection pSelection) {
        return true;
    }
}
