/**
 *
 */
package org.libreoffice.ide.eclipse.core.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog class for the selection of a locale.
 */
public class LocaleDialog extends Dialog {

    private ComboViewer mLocaleList;

    private Locale mLocale;
    private ArrayList<Locale> mLocales;

    /**
     * Creates the dialog using the active shell as parent.
     *
     */
    public LocaleDialog() {
        super(new Shell(Display.getDefault()));

        setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    /**
     * @return the currently selected locale
     */
    public Locale getLocale() {

        return mLocale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createDialogArea(Composite pParent) {
        Composite body = (Composite) super.createDialogArea(pParent);
        body.setLayout(new GridLayout(2, false));
        body.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Language
        Label langLbl = new Label(body, SWT.NONE);
        langLbl.setText(Messages.getString("LocaleDialog.Title")); //$NON-NLS-1$
        langLbl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        Combo list = new Combo(body, SWT.READ_ONLY | SWT.DROP_DOWN);
        list.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mLocaleList = new ComboViewer(list);
        mLocaleList.setContentProvider(new ArrayContentProvider());
        mLocaleList.setLabelProvider(new LabelProvider());
        mLocaleList.setComparator(new ViewerComparator());
        mLocaleList.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                Locale locale = null;
                IStructuredSelection sel = (IStructuredSelection) mLocaleList.getSelection();
                if (!sel.isEmpty()) {
                    locale = (Locale) sel.getFirstElement();
                }
                mLocale = locale;
            }

        });

        mLocales = new ArrayList<Locale>();
        mLocales.addAll(Arrays.asList(Locale.getAvailableLocales()));
        mLocaleList.setInput(mLocales);

        return body;
    }
}
