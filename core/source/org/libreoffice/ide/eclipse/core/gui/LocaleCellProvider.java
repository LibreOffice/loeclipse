/*************************************************************************
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
package org.libreoffice.ide.eclipse.core.gui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
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
 *
 */
public class LocaleCellProvider extends CellEditor {

    private Locale mValue;
    private Vector<String> mLanguages;
    private Vector<String> mCountries;

    private CCombo mLanguage;
    private CCombo mCountry;

    /**
     * @param pParent
     *            the composite containing the cell editor
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
        Map<String, String> languagesISO = new HashMap<>();
        for (String language : Locale.getISOLanguages()) {
            languagesISO.put(new Locale(language).getDisplayLanguage(), language);
        }
        String[] languagesDisplay = new String[languagesISO.size()];
        if (mLanguages == null) {
            mLanguages = new Vector<String>();
        }
        mLanguages.clear();

        SortedSet<String> languages = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        languages.addAll(languagesISO.keySet());
        int i = 0;
        for (String language : languages) {
            languagesDisplay[i] = language;
            mLanguages.add(i, languagesISO.get(language));
            i ++;
        }

        mLanguage = new CCombo(body, getStyle());
        mLanguage.setLayoutData(new GridData());
        mLanguage.setFont(pParent.getFont());
        mLanguage.setItems(languagesDisplay);
        mLanguage.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String lang = mLanguages.get(mLanguage.getSelectionIndex());
                mValue = new Locale(lang, mValue.getCountry());
            }
        });

        // Create the country Combobox
        Map<String, String> countriesISO = new HashMap<>();
        for (String country : Locale.getISOCountries()) {
            countriesISO.put(new Locale("en", country).getDisplayCountry(), country);
        }
        String[] countriesDisplay = new String[countriesISO.size() + 1];
        if (mCountries == null) {
            mCountries = new Vector<String>();
        }
        mCountries.clear();
        // Allows the user to select no country
        countriesDisplay[0] = ""; //$NON-NLS-1$
        mCountries.add(""); //$NON-NLS-1$

        SortedSet<String> countries = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        countries.addAll(countriesISO.keySet());
        i = 1;
        for (String country : countries) {
            countriesDisplay[i] = country;
            mCountries.add(i, countriesISO.get(country));
            i ++;
        }

        mCountry = new CCombo(body, getStyle());
        mCountry.setLayoutData(new GridData());
        mCountry.setFont(pParent.getFont());
        mCountry.setItems(countriesDisplay);
        mCountry.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
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
            mValue = (Locale) pValue;

            mLanguage.select(mLanguages.indexOf(mValue.getLanguage()));
            mCountry.select(mCountries.indexOf(mValue.getCountry()));
        }
    }
}
