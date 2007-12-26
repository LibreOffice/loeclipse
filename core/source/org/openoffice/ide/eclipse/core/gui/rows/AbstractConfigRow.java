/*************************************************************************
 *
 * $RCSfile: AbstractConfigRow.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/12/26 14:40:25 $
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
package org.openoffice.ide.eclipse.core.gui.rows;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.openoffice.ide.eclipse.core.gui.AbstractTable;
import org.openoffice.ide.eclipse.core.preferences.IConfigListener;

/**
 * Row for the selection of a configuration element.
 * 
 * @author cedricbosdo
 *
 */
public abstract class AbstractConfigRow extends ChoiceRow {
    
    private IConfigListener mConfigListener;

    /**
     * Constructor.
     * 
     * @param pParent the composite where to create the row
     * @param pProperty the property for the row events
     * @param pSelection the configuration element to select first
     */
    public AbstractConfigRow(final Composite pParent, String pProperty, Object pSelection) {
        super(pParent, pProperty, "configuration", "Browse");
        
        addListener(mConfigListener);
        
        setLabel(getRowLabel());
        
        setBrowseSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent pEvent) {
                super.widgetSelected(pEvent);
                
                // Open the Configuration page
                TableDialog dialog = new TableDialog(pParent.getShell());
                dialog.create();
                dialog.open();
            }
        });
        
        fillRow(pSelection);
    }

    /**
     * Dispose the row.
     */
    public void dispose() {
        removeListener(mConfigListener);
    }
    
    /**
     * Add the configuration listener to the correct configuration container.
     * 
     * @param pConfigListener the listener to add
     */
    protected abstract void addListener(IConfigListener pConfigListener);

    /**
     * Remove the configuration listener from the correct configuration container.
     * 
     * @param pConfigListener the listener to remove
     */
    protected abstract void removeListener(IConfigListener pConfigListener);
    
    /**
     * @return the label to show for the row
     */
    protected abstract String getRowLabel();
    
    /**
     * @return the values to show in the list box.
     */
    protected abstract String[] getConfigValues();
    
    /**
     * Computes the name to use to select the given object.
     * 
     * @param pToSelect the configuration object to select
     * @return the name to use for the selection
     */
    protected abstract String getSelectionName(Object pToSelect);
    
    /**
     * Fills the row with the existing values from the configuration.
     * 
     * @param pToSelect the configuration object to select
     */
    private void fillRow(Object pToSelect) {
        
        String[] values = getConfigValues();

        removeAll();
        addAll(values);
        if (null != pToSelect) {
            select(getSelectionName(pToSelect));
        } else {
            select(0);
        }
    }
    
    /**
     * @return the title of the configuration dialog.
     */
    protected abstract String getTableDialogTitle();
    
    /**
     * Create the table to show the configuration elements in the dialog.
     * 
     * @param pParent the parent for the table.
     * @return the initialized table
     */
    protected abstract AbstractTable createTable(Composite pParent);
    
    /**
     * Save the configuration element preferences.
     */
    protected abstract void savePreferences();
    
    /**
     * The dialog to configure the plugin configuration objects.
     * 
     * @author cedricbosdo
     *
     */
    private class TableDialog extends Dialog {
        
        /**
         * Constructor.
         * 
         * @param pParentShell the shell used for the dialog creation
         */
        TableDialog (Shell pParentShell) {
            super(pParentShell);
            setShellStyle(getShellStyle() | SWT.RESIZE);
            
            // This dialog is a modal one
            setBlockOnOpen(true);
            pParentShell.setText(getTableDialogTitle());
        }
        
        /**
         * {@inheritDoc}
         */
        protected Control createDialogArea(Composite pParent) {
            
            createTable(pParent);
            return pParent;
        }
        
        /**
         * {@inheritDoc}
         */
        protected void okPressed() {
            super.okPressed();

            savePreferences();
        }
    }
}
