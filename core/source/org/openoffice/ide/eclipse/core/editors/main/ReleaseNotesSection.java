/**
 * 
 */
package org.openoffice.ide.eclipse.core.editors.main;

import java.util.HashMap;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Section displaying the release notes part of the descriptions.xml file.
 * 
 * @author cbosdonnat
 *
 */
public class ReleaseNotesSection extends LocalizedSection implements
        ILocaleListener {

    private HashMap<Locale, String> mInfos;
    
    private Text mUrlTxt;
    
    /**
     * @param pParent the parent composite where to add the section
     * @param pPage the parent page
     */
    public ReleaseNotesSection(Composite pParent, PackageOverviewFormPage pPage) {
        super(pParent, pPage);
        
        getSection().setText( "Release notes" );
        
        mInfos = new HashMap<Locale, String>( );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void createControls(FormToolkit pToolkit, Composite pParent) {
        pParent.setLayout( new GridLayout( 2, false ) );
        
        Label descrLbl = pToolkit.createLabel( pParent, 
                "Defines the localized release notes web pages for this version.", 
                SWT.WRAP );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 2;
        descrLbl.setLayoutData( gd );
        
        // Url controls
        pToolkit.createLabel( pParent, "Url" );
        mUrlTxt = pToolkit.createText( pParent, "" ); //$NON-NLS-1$
        mUrlTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mUrlTxt.setEnabled( false );
    }

    /**
     * {@inheritDoc}
     */
    public void addLocale(Locale pLocale) {
        mInfos.put( pLocale, "" ); //$NON-NLS-1$
        mUrlTxt.setEnabled( true );
    }

    /**
     * {@inheritDoc}
     */
    public void deleteLocale(Locale pLocale) {
        mInfos.remove( pLocale );
        if ( mInfos.size( ) == 0 ) {
            mUrlTxt.setEnabled( false );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void selectLocale(Locale pLocale) {
        
        if ( mCurrentLocale != null ) {
            mInfos.put( mCurrentLocale, mUrlTxt.getText() );
        }
        super.selectLocale(pLocale);
        mUrlTxt.setText( mInfos.get( pLocale ) );
    }
}
