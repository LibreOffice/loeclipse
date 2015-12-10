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
package org.libreoffice.ide.eclipse.core.model.description;

/**
 * Simple structure storing the publisher informations.
 *
 * @author cbosdonnat
 *
 */
public class PublisherInfos {

    private DescriptionModel mModel;

    private String mUrl = ""; //$NON-NLS-1$
    private String mName = ""; //$NON-NLS-1$

    /**
     * @return the url
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * @param pUrl
     *            the url to set
     */
    public void setUrl(String pUrl) {
        mUrl = pUrl;
        if (mUrl != null) {
            mUrl = mUrl.trim();
        }
        mModel.fireModelChanged();
    }

    /**
     * @return the name
     */
    public String getName() {
        return mName;
    }

    /**
     * @param pName
     *            the name to set
     */
    public void setName(String pName) {
        mName = pName;
        if (mName != null) {
            mName = mName.trim();
        }
        mModel.fireModelChanged();
    }

    /**
     * Set the description model in order to be able to fire the model changes.
     *
     * @param pModel
     *            the parent model
     */
    protected void setModel(DescriptionModel pModel) {
        mModel = pModel;
    }
}
