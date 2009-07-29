package org.openoffice.ide.eclipse.core.editors.main;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openoffice.ide.eclipse.core.gui.LocaleDialog;

/**
 * Component for the selection of a locale.
 * 
 * @author cbosdonnat
 *
 */
public class LocaleSelector {
    
    private static final int LAYOUT_COLS = 3;
    private ComboViewer mLangList;
    private Button mAddBtn;
    private Button mDelBtn;
    
    private Locale mCurrentLocale;
    private ArrayList<Locale> mLocales;
    
    private ArrayList<ILocaleListener> mListeners;
    
    /**
     * Creates the control on a form.
     * 
     * @param pToolkit the toolkit to use for the controls creation
     * @param pParent the page composite
     */
    public LocaleSelector( FormToolkit pToolkit, Composite pParent) {
        
        mListeners = new ArrayList<ILocaleListener>( );
        mLocales = new ArrayList<Locale>( );
        
        // Controls initialization
        Composite langBody = pToolkit.createComposite( pParent );
        langBody.setLayoutData(new GridData(GridData.FILL_BOTH));
        langBody.setLayout(new GridLayout(LAYOUT_COLS, false));
        
        Combo list = new Combo( langBody, SWT.DROP_DOWN | SWT.READ_ONLY );
        list.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mLangList = new ComboViewer( list );
        mLangList.setContentProvider( new ArrayContentProvider( ) );
        mLangList.setLabelProvider( new LabelProvider( ) );
        mLangList.addSelectionChangedListener( new ISelectionChangedListener( ) {
            public void selectionChanged(SelectionChangedEvent pEvent) {
                IStructuredSelection sel = (IStructuredSelection)pEvent.getSelection();
                if ( !sel.isEmpty() ) {
                    mCurrentLocale = (Locale)sel.getFirstElement();
                    fireUpdateLocale( mCurrentLocale );
                }
            } 
        });
        
        
        mAddBtn = new Button( langBody, SWT.NONE );
        mAddBtn.setText( "Add" );
        mAddBtn.addSelectionListener( new SelectionAdapter( ) {
            @Override
            public void widgetSelected(SelectionEvent pE) {
                
                // Show the Locale selection dialog.
                LocaleDialog dlg = new LocaleDialog( );
                if ( dlg.open() == LocaleDialog.OK ) {
                    Locale locale = dlg.getLocale();
                    
                    // Add the result to the list and select it
                    if ( !mLocales.contains( locale ) ) {
                        mLocales.add( locale );
                        mLangList.add( locale );
                        fireAddLocale( locale );
                    }
                    mLangList.setSelection( new StructuredSelection( locale ), true );
                    fireUpdateLocale( locale );
                }
            }
        });
        
        mDelBtn = new Button( langBody, SWT.NONE );
        mDelBtn.setText( "Del" );
        mDelBtn.addSelectionListener( new SelectionAdapter( ) {
            @Override
            public void widgetSelected(SelectionEvent pE) {
                
                // Show the locale before the removed one
                Locale locale = getCurrentLocale( );
                mLangList.remove( locale );
                int pos = mLocales.indexOf( locale ) - 1;
                if ( pos < 0 ) {
                    pos = 0;
                }
                mLocales.remove( locale );
                fireDeleteLocale( locale );
                
                Locale newSel = mLocales.get( pos );
                mLangList.setSelection( new StructuredSelection( newSel ), true );
                fireUpdateLocale( getCurrentLocale( ) );
            }
        });
    }
    
    /**
     *  @param pListener the listener to add
     */
    protected void addListener( ILocaleListener pListener ) {
        mListeners.add( pListener );
    }
    
    /**
     * @param pListener the listener to remove.
     */
    protected void removeListener( ILocaleListener pListener ) {
        mListeners.remove( pListener );
    }
    
    /**
     * @return the currently selected locale. <code>null</code> if no locale selected.
     */
    protected Locale getCurrentLocale( ) {
        Locale locale = null;
        IStructuredSelection sel = (IStructuredSelection)mLangList.getSelection();
        if ( !sel.isEmpty() ) {
            locale = (Locale)sel.getFirstElement();
        }
        return locale;
    }
    
    /**
     * Replace all the previous locales by these new ones.
     * 
     * @param pLocales the new locales to set.
     */
    protected void loadLocales( ArrayList<Locale> pLocales ) {
        // notifies the removals
        for (Locale locale : mLocales) {
            fireDeleteLocale( locale );
        }
        mLocales.clear();
        
        mLocales.addAll( pLocales );
        // Notifies the additions
        for (Locale locale : mLocales) {
            fireAddLocale( locale );
        }
    }
    
    /**
     * Notifies the listeners that the locale selection has changed.
     * 
     * @param pLocale the locale.
     */
    private void fireUpdateLocale( Locale pLocale ) {
        for (ILocaleListener listener  : mListeners) {
            listener.selectLocale( pLocale );
        }
    }
    
    /**
     * Notifies the listeners that a locale has been removed.
     * 
     * @param pLocale the locale.
     */
    private void fireDeleteLocale( Locale pLocale ) {
        for (ILocaleListener listener  : mListeners) {
            listener.deleteLocale( pLocale );
        }
    }
    
    /**
     * Notifies the listeners that a locale has been added.
     * 
     * @param pLocale the locale.
     */
    private void fireAddLocale( Locale pLocale ) {
        for (ILocaleListener listener  : mListeners) {
            listener.addLocale( pLocale );
        }
    }
}
