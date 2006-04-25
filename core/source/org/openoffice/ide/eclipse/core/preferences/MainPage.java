/*************************************************************************
 *
 * $RCSfile: MainPage.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/25 19:10:01 $
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
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
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

    private static final String LOGLEVEL = "__log_level";
    private IFieldChangedListener __listener = new loglevelListener();

    private ChoiceRow loglevel;
    
	protected Control createContents(Composite parent) {
		
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout(2, false));
		body.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        loglevel = new ChoiceRow(body, LOGLEVEL, 
                OOEclipsePlugin.getTranslationString(I18nConstants.LOGLEVEL));
        loglevel.add(OOEclipsePlugin.getTranslationString(I18nConstants.ERROR),
                PluginLogger.ERROR);
        loglevel.add(OOEclipsePlugin.getTranslationString(I18nConstants.WARNING),
                PluginLogger.WARNING);
        loglevel.add(OOEclipsePlugin.getTranslationString(I18nConstants.INFO),
                PluginLogger.INFO);
        loglevel.add(OOEclipsePlugin.getTranslationString(I18nConstants.DEBUG),
                PluginLogger.DEBUG);
        
        IPreferenceStore store = getPreferenceStore();
        loglevel.select(store.getString(
        		OOEclipsePlugin.LOGLEVEL_PREFERENCE_KEY));
        loglevel.setFieldChangedListener(__listener);
            
		return body;
	}

	public void init(IWorkbench workbench) {
		
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return OOEclipsePlugin.getDefault().getPreferenceStore();
	}
	
    private class loglevelListener implements IFieldChangedListener {
        
    	public void fieldChanged(FieldEvent e) {
            if (e.getProperty().equals(LOGLEVEL)) {
                // set the new logger level
                PluginLogger.getInstance().setLevel(e.getValue());
            }
        }
    }
    
    public boolean performOk() {
    	boolean result = super.performOk();
    	
    	IPreferenceStore store = getPreferenceStore();
    	store.setValue(OOEclipsePlugin.LOGLEVEL_PREFERENCE_KEY, 
    			loglevel.getValue());
    	
    	return result;
    }
    
    protected void performDefaults() {
    	super.performDefaults();
    	
    	IPreferenceStore store = getPreferenceStore();
    	loglevel.select(store.getDefaultString(
    			OOEclipsePlugin.LOGLEVEL_PREFERENCE_KEY));
    }
}
