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
package org.libreoffice.ide.eclipse.core.editors.utils;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.gui.LocaleDialog;
import org.libreoffice.ide.eclipse.core.i18n.ImagesConstants;

/**
 * Component for the selection of a locale.
 */
public class LocaleSelector {

    private static final int LAYOUT_COLS = 4;
    private ComboViewer mLangList;
    private Button mAddBtn;
    private Button mDelBtn;

    private Locale mCurrentLocale;
    private ArrayList<Locale> mLocales;

    private ArrayList<ILocaleListener> mListeners;

    /**
     * Creates the control on a form.
     *
     * @param pToolkit
     *            the toolkit to use for the controls creation
     * @param pParent
     *            the page composite
     */
    public LocaleSelector(FormToolkit pToolkit, Composite pParent) {

        mListeners = new ArrayList<ILocaleListener>();
        mLocales = new ArrayList<Locale>();

        // Controls initialization
        Composite langBody = pToolkit.createComposite(pParent);
        langBody.setLayoutData(new GridData(GridData.FILL_BOTH));
        langBody.setLayout(new GridLayout(LAYOUT_COLS, false));

        Label separator = pToolkit.createSeparator(langBody, SWT.HORIZONTAL);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = LAYOUT_COLS;
        separator.setLayoutData(gd);

        createList(pToolkit, langBody);
        createButtons(langBody);
    }

    /**
     * @param pListener
     *            the listener to add
     */
    public void addListener(ILocaleListener pListener) {
        mListeners.add(pListener);
    }

    /**
     * @param pListener
     *            the listener to remove.
     */
    protected void removeListener(ILocaleListener pListener) {
        mListeners.remove(pListener);
    }

    /**
     * @return the currently selected locale. <code>null</code> if no locale selected.
     */
    protected Locale getCurrentLocale() {
        Locale locale = null;
        IStructuredSelection sel = (IStructuredSelection) mLangList.getSelection();
        if (!sel.isEmpty()) {
            locale = (Locale) sel.getFirstElement();
        }
        return locale;
    }

    /**
     * Replace all the previous locales by these new ones.
     *
     * @param pLocales
     *            the new locales to set.
     */
    public void loadLocales(ArrayList<Locale> pLocales) {
        // notifies the removals
        for (Locale locale : mLocales) {
            mLangList.remove(locale);
            fireDeleteLocale(locale);
        }
        mLocales.clear();

        mLocales.addAll(pLocales);
        // Notifies the additions
        for (Locale locale : mLocales) {
            mLangList.add(locale);
            fireAddLocale(locale);
        }
        if (mLocales.size() > 0) {
            Locale locale = mLocales.get(0);
            mLangList.setSelection(new StructuredSelection(locale));
            fireUpdateLocale(locale);
        }
    }

    /**
     * Creates the locale selection list.
     *
     * @param pToolkit
     *            the toolkit to use for the controls creation
     * @param pParent
     *            the composite parent where to create the label and list.
     */
    private void createList(FormToolkit pToolkit, Composite pParent) {
        String msg = Messages.getString("LocaleSelector.SelectedLocaleTitle"); //$NON-NLS-1$
        Label localeLbl = pToolkit.createLabel(pParent, msg);
        localeLbl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        Combo list = new Combo(pParent, SWT.DROP_DOWN | SWT.READ_ONLY);
        list.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mLangList = new ComboViewer(list);
        mLangList.setContentProvider(new ArrayContentProvider());
        mLangList.setLabelProvider(new LabelProvider());
        mLangList.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent pEvent) {
                IStructuredSelection sel = (IStructuredSelection) pEvent.getSelection();
                if (!sel.isEmpty()) {
                    mCurrentLocale = (Locale) sel.getFirstElement();
                    fireUpdateLocale(mCurrentLocale);
                }
            }
        });
    }

    /**
     * Creates the add and del buttons.
     *
     * @param pParent
     *            the composite parent where to create the buttons.
     */
    private void createButtons(Composite pParent) {
        mAddBtn = new Button(pParent, SWT.NONE);
        mAddBtn.setImage(OOEclipsePlugin.getImage(ImagesConstants.ADD));
        mAddBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pE) {

                // Show the Locale selection dialog.
                LocaleDialog dlg = new LocaleDialog();
                if (dlg.open() == Window.OK) {
                    Locale locale = dlg.getLocale();

                    // Add the result to the list and select it
                    if (!mLocales.contains(locale)) {
                        mLocales.add(locale);
                        mLangList.add(locale);
                        mDelBtn.setEnabled(true);
                        fireAddLocale(locale);
                    }
                    mLangList.setSelection(new StructuredSelection(locale), true);
                    fireUpdateLocale(locale);
                }
            }
        });

        mDelBtn = new Button(pParent, SWT.NONE);
        mDelBtn.setEnabled(false);
        mDelBtn.setImage(OOEclipsePlugin.getImage(ImagesConstants.DELETE));
        mDelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pE) {

                // Show the locale before the removed one
                Locale locale = getCurrentLocale();
                mLangList.remove(locale);
                int pos = mLocales.indexOf(locale) - 1;
                if (pos < 0) {
                    pos = 0;
                }
                mLocales.remove(locale);
                mDelBtn.setEnabled(!mLocales.isEmpty());
                fireDeleteLocale(locale);

                Locale newSel = mLocales.get(pos);
                mLangList.setSelection(new StructuredSelection(newSel), true);
                fireUpdateLocale(getCurrentLocale());
            }
        });
    }

    /**
     * Notifies the listeners that the locale selection has changed.
     *
     * @param pLocale
     *            the locale.
     */
    private void fireUpdateLocale(Locale pLocale) {
        for (ILocaleListener listener : mListeners) {
            listener.selectLocale(pLocale);
        }
    }

    /**
     * Notifies the listeners that a locale has been removed.
     *
     * @param pLocale
     *            the locale.
     */
    private void fireDeleteLocale(Locale pLocale) {
        for (ILocaleListener listener : mListeners) {
            listener.deleteLocale(pLocale);
        }
    }

    /**
     * Notifies the listeners that a locale has been added.
     *
     * @param pLocale
     *            the locale.
     */
    private void fireAddLocale(Locale pLocale) {
        for (ILocaleListener listener : mListeners) {
            listener.addLocale(pLocale);
        }
    }
}
