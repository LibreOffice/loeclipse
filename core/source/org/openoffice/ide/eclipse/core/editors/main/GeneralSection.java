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
public class GeneralSection extends LocalizedSection {
    
    private HashMap<Locale, String> mDisplayNames;
    
    private Text mNameTxt;
    private Text mIdTxt;
    private Text mVersionTxt;
    
    /**
     * @param pParent the parent composite where to add the section
     * @param pPage the parent page
     */
    public GeneralSection( Composite pParent, PackageOverviewFormPage pPage ) {
        super( pParent, pPage );
        
        getSection().setText( "General informations" );
        
        mDisplayNames = new HashMap<Locale, String>( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createControls( FormToolkit pToolkit, Composite pParent ) {
        
        pParent.setLayout( new GridLayout( 2, false ) );
        
        Label descrLbl = pToolkit.createLabel( pParent, 
                "Define the extension localized name and identification informations.", 
                SWT.WRAP );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 2;
        descrLbl.setLayoutData( gd );
        
        // Name controls
        pToolkit.createLabel( pParent, "Name" );
        mNameTxt = pToolkit.createText( pParent, new String( ) );
        mNameTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mNameTxt.setEnabled( false );
        
        // Identifier controls
        pToolkit.createLabel( pParent, "Identifier" );
        mIdTxt = pToolkit.createText( pParent, new String( ) );
        mIdTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        
        // Version controls
        pToolkit.createLabel( pParent, "Version" );
        mVersionTxt = pToolkit.createText( pParent, new String( ) );
        mVersionTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    }

    /**
     * {@inheritDoc}
     */
    public void addLocale(Locale pLocale) {
        mDisplayNames.put( pLocale, new String( ) ); //$NON-NLS-1$
        mNameTxt.setEnabled( true );
    }

    /**
     * {@inheritDoc}
     */
    public void deleteLocale(Locale pLocale) {
        mDisplayNames.remove( pLocale );
        if ( mDisplayNames.size() == 0 ) {
            mNameTxt.setEnabled( false );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectLocale(Locale pLocale) {
        
        if ( mCurrentLocale != null ) {
            mDisplayNames.put( mCurrentLocale, mNameTxt.getText( ) );
        }
        super.selectLocale(pLocale);
        mNameTxt.setText( mDisplayNames.get( pLocale ) );
    }
}
