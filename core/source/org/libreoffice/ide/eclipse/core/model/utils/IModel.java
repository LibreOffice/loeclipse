/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.model.utils;

/**
 * Interface for listened models.
*/
public interface IModel {

    /**
     * Add a model listener.
     *
     * @param pListener
     *            the listener to add
     */
    public void addListener(IModelChangedListener pListener);

    /**
     * Remove a model listener.
     *
     * @param pListener
     *            the listener to remove
     */
    public void removeListener(IModelChangedListener pListener);

    /**
     * @return whether the model has been changed without being saved or not.
     */
    public boolean isDirty();

    /**
     * @param pSuspend
     *            <code>true</code> to suspend the events for the model
     */
    public void setSuspendEvent(boolean pSuspend);
}