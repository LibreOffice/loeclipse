/*************************************************************************
 *
 * $RCSfile: MainPage.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:27 $
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
package org.libreoffice.ide.eclipse.core.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.libreoffice.ide.eclipse.core.LogLevels;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.gui.rows.ChoiceRow;
import org.libreoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.libreoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;

/**
 * This preferences page defines plugin generic values like log level.
 *
 * @author cedricbosdo
 *
 */
public class MainPage extends PreferencePage implements IWorkbenchPreferencePage {

    private static final String LOGLEVEL = "__log_level"; //$NON-NLS-1$
    private IFieldChangedListener mListener = new loglevelListener();

    private ChoiceRow mLoglevel;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createContents(Composite pParent) {

        Composite body = new Composite(pParent, SWT.NONE);
        body.setLayout(new GridLayout(2, false));
        body.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        mLoglevel = new ChoiceRow(body, LOGLEVEL, Messages.getString("MainPage.LogLevel"), null, false); //$NON-NLS-1$
        mLoglevel.add(Messages.getString("MainPage.Error"), //$NON-NLS-1$
            LogLevels.ERROR.toString());
        mLoglevel.add(Messages.getString("MainPage.Warning"), //$NON-NLS-1$
            LogLevels.WARNING.toString());
        mLoglevel.add(Messages.getString("MainPage.Info"), //$NON-NLS-1$
            LogLevels.INFO.toString());
        mLoglevel.add(Messages.getString("MainPage.Debug"), //$NON-NLS-1$
            LogLevels.DEBUG.toString());

        IPreferenceStore store = getPreferenceStore();
        mLoglevel.select(store.getString(OOEclipsePlugin.LOGLEVEL_PREFERENCE_KEY));
        mLoglevel.setFieldChangedListener(mListener);

        return body;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IWorkbench pWorkbench) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return OOEclipsePlugin.getDefault().getPreferenceStore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performOk() {
        boolean result = super.performOk();

        IPreferenceStore store = getPreferenceStore();
        store.setValue(OOEclipsePlugin.LOGLEVEL_PREFERENCE_KEY, mLoglevel.getValue());

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performDefaults() {
        super.performDefaults();

        IPreferenceStore store = getPreferenceStore();
        mLoglevel.select(store.getDefaultString(OOEclipsePlugin.LOGLEVEL_PREFERENCE_KEY));
    }

    /**
     * Listens to the log level changes and set the correct level to the plugin logger.
     *
     * @author cbosdonnat
     */
    private class loglevelListener implements IFieldChangedListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void fieldChanged(FieldEvent pE) {
            if (pE.getProperty().equals(LOGLEVEL)) {
                // set the new logger level
                PluginLogger.setLevel(LogLevels.valueOf(pE.getValue()));
            }
        }
    }
}
