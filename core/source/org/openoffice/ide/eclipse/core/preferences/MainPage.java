/*************************************************************************
 *
 * $RCSfile: MainPage.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:55:55 $
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
package org.openoffice.ide.eclipse.core.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.rows.ChoiceRow;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;


/**
 * This preferences page defines plugin generic values like log level.
 * 
 * @author cbosdonnat
 *
 */
public class MainPage extends PreferencePage implements IWorkbenchPreferencePage {

    private static final String LOGLEVEL = "__log_level"; //$NON-NLS-1$
    private IFieldChangedListener mListener = new loglevelListener();

    private ChoiceRow mLoglevel;
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
	protected Control createContents(Composite parent) {
		
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout(2, false));
		body.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        mLoglevel = new ChoiceRow(body, LOGLEVEL, 
                Messages.getString("MainPage.LogLevel")); //$NON-NLS-1$
        mLoglevel.add(Messages.getString("MainPage.Error"), //$NON-NLS-1$
                PluginLogger.ERROR);
        mLoglevel.add(Messages.getString("MainPage.Warning"), //$NON-NLS-1$
                PluginLogger.WARNING);
        mLoglevel.add(Messages.getString("MainPage.Info"), //$NON-NLS-1$
                PluginLogger.INFO);
        mLoglevel.add(Messages.getString("MainPage.Debug"), //$NON-NLS-1$
                PluginLogger.DEBUG);
        
        IPreferenceStore store = getPreferenceStore();
        mLoglevel.select(store.getString(
        		OOEclipsePlugin.LOGLEVEL_PREFERENCE_KEY));
        mLoglevel.setFieldChangedListener(mListener);
            
		return body;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#doGetPreferenceStore()
	 */
	protected IPreferenceStore doGetPreferenceStore() {
		return OOEclipsePlugin.getDefault().getPreferenceStore();
	}
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
    	boolean result = super.performOk();
    	
    	IPreferenceStore store = getPreferenceStore();
    	store.setValue(OOEclipsePlugin.LOGLEVEL_PREFERENCE_KEY, 
    			mLoglevel.getValue());
    	
    	return result;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults() {
    	super.performDefaults();
    	
    	IPreferenceStore store = getPreferenceStore();
    	mLoglevel.select(store.getDefaultString(
    			OOEclipsePlugin.LOGLEVEL_PREFERENCE_KEY));
    }
    
    /**
	 * Listens to the log level changes and set the correct level to the
	 * plugin logger.
	 * 
	 * @author cbosdonnat
	 */
    private class loglevelListener implements IFieldChangedListener {
        
    	public void fieldChanged(FieldEvent e) {
            if (e.getProperty().equals(LOGLEVEL)) {
                // set the new logger level
                PluginLogger.setLevel(e.getValue());
            }
        }
    }
}
