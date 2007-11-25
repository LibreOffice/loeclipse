/*************************************************************************
 *
 * $RCSfile: LocaleCellProvider.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:28 $
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
package org.openoffice.ide.eclipse.core.gui;

import java.util.Locale;
import java.util.Vector;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author cedricbosdo
 *
 */
public class LocaleCellProvider extends CellEditor {

    private Locale mValue;
    private Vector<String> mLanguages;
    private Vector<String> mCountries;
    
    private CCombo mLanguage;
    private CCombo mCountry;

    /**
     * @param pParent the composite containing the cell editor
     */
    public LocaleCellProvider(Composite pParent) {
        super(pParent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createControl(Composite pParent) {
        
        Composite body = new Composite(pParent, getStyle());
        body.setLayout(new GridLayout(2, false));
        
        // Create the language Combobox
        String[] languagesISO = Locale.getISOLanguages();
        String[] languagesDisplay = new String[languagesISO.length];
        if (mLanguages == null) {
            mLanguages = new Vector<String>();
        }
        mLanguages.clear();
        for (int i = 0; i < languagesISO.length; i++) {
            String lang = languagesISO[i];
            Locale locale = new Locale(lang);
            languagesDisplay[i] = locale.getDisplayLanguage();
            mLanguages.add(i, lang);
        }
        
        mLanguage = new CCombo(body, getStyle());
        mLanguage.setLayoutData(new GridData());
        mLanguage.setFont(pParent.getFont());
        mLanguage.setItems(languagesDisplay);
        mLanguage.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pEvent) {
                String lang = mLanguages.get(mLanguage.getSelectionIndex());
                mValue = new Locale(lang, mValue.getCountry());
            }
        });
        
        
        // Create the country Combobox
        String[] countriesISO = Locale.getISOCountries();
        String[] countriesDisplay = new String[countriesISO.length + 1];
        if (mCountries == null) {
            mCountries = new Vector<String>();
        }
        mCountries.clear();
        countriesDisplay[0] = ""; //$NON-NLS-1$
        mCountries.add(""); //$NON-NLS-1$
        // Allows the user to select no country
        for (int i = 0; i < countriesISO.length; i++) {
            String country = countriesISO[i];
            Locale locale = new Locale("en", country); // $NON-NLS-1$ //$NON-NLS-1$
            countriesDisplay[i + 1] = locale.getDisplayCountry();
            mCountries.add(i + 1, country);
        }
        
        mCountry = new CCombo(body, getStyle());
        mCountry.setLayoutData(new GridData());
        mCountry.setFont(pParent.getFont());
        mCountry.setItems(countriesDisplay);
        mCountry.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pEvent) {
                String country = mCountries.get(mCountry.getSelectionIndex());
                mValue = new Locale(mValue.getLanguage(), country);
            }
        });
        
        return body;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object doGetValue() {
        return mValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetFocus() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSetValue(Object pValue) {
        if (pValue instanceof Locale) {
            mValue = (Locale)pValue;
            
            mLanguage.select(mLanguages.indexOf(mValue.getLanguage()));
            mCountry.select(mCountries.indexOf(mValue.getCountry()));
        }
    }
}
