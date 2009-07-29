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
 * @author cbosdonnat
 *
 */
public class PublisherSection extends LocalizedSection {

    private HashMap<Locale, PublisherInfos> mInfos;
    
    private Text mUrlTxt;
    private Text mNameTxt;
    
    /**
     * @param pParent the parent composite where to add the section
     * @param pPage the parent page
     */
    public PublisherSection(Composite pParent, PackageOverviewFormPage pPage) {
        super(pParent, pPage);
        
        getSection().setText( "Provider informations" );
        
        mInfos = new HashMap<Locale, PublisherInfos>( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createControls(FormToolkit pToolkit, Composite pParent) {
        
        pParent.setLayout( new GridLayout( 2, false ) );
        
        Label descrLbl = pToolkit.createLabel( pParent, "Define the localized informartions on the " +
                "extension publisher.", 
                SWT.WRAP );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 2;
        descrLbl.setLayoutData( gd );
        
        // Name controls
        pToolkit.createLabel( pParent, "Name" );
        mNameTxt = pToolkit.createText( pParent, "" ); //$NON-NLS-1$
        mNameTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mNameTxt.setEnabled( false );
        
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
        mInfos.put( pLocale, new PublisherInfos( ) );
        mNameTxt.setEnabled( true );
        mUrlTxt.setEnabled( true );
    }

    /**
     * {@inheritDoc}
     */
    public void deleteLocale(Locale pLocale) {
        mInfos.remove( pLocale );
        if ( mInfos.size( ) == 0 ) {
            mNameTxt.setEnabled( false );
            mUrlTxt.setEnabled( false );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void selectLocale(Locale pLocale) {
        
        if ( mCurrentLocale != null ) {
            PublisherInfos infos = mInfos.get( mCurrentLocale );
            infos.mName = mNameTxt.getText();
            infos.mUrl = mUrlTxt.getText();
        }
        super.selectLocale(pLocale);
        PublisherInfos infos = mInfos.get( pLocale );
        mNameTxt.setText( infos.mName );
        mUrlTxt.setText( infos.mUrl );
    }
    
    /**
     * Simple structure storing the publisher informations.
     * 
     * @author cbosdonnat
     *
     */
    private class PublisherInfos {
        String mUrl = ""; //$NON-NLS-1$
        String mName = ""; //$NON-NLS-1$
    }
}