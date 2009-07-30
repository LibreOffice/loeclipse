/**
 * 
 */
package org.openoffice.ide.eclipse.core.editors.main;

import java.util.HashMap;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openoffice.ide.eclipse.core.model.description.DescriptionModel;

/**
 * @author cbosdonnat
 *
 */
public class GeneralSection extends LocalizedSection {
    
    private DescriptionModel mModel;
//    private HashMap<Locale, String> mDisplayNames;
    
    private Text mNameTxt;
    private Text mIdTxt;
    private Text mVersionTxt;
    
    /**
     * @param pParent the parent composite where to add the section
     * @param pPage the parent page
     */
    public GeneralSection( Composite pParent, PackageOverviewFormPage pPage ) {
        super( pParent, pPage, Section.TITLE_BAR );
        
        getSection().setText( "General informations" );
        
        mModel = pPage.getModel();
        loadValues( );
        
        if ( mModel.mDisplayNames == null ) {
            mModel.mDisplayNames = new HashMap<Locale, String>( );
        }
    }

    /**
     * Loads the values from the model into the controls.
     */
    public void loadValues( ) {
        mIdTxt.setText( mModel.mId );
        mVersionTxt.setText( mModel.mVersion );
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
        mNameTxt.addModifyListener( new ModifyListener () {
            public void modifyText(ModifyEvent pE) {
                mModel.mDisplayNames.put( mCurrentLocale, mNameTxt.getText() );
            }
        });
        
        // Identifier controls
        pToolkit.createLabel( pParent, "Identifier" );
        mIdTxt = pToolkit.createText( pParent, new String( ) );
        mIdTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mIdTxt.addModifyListener( new ModifyListener () {
            public void modifyText(ModifyEvent pE) {
                mModel.mId = mIdTxt.getText();
            }
        });
        
        // Version controls
        pToolkit.createLabel( pParent, "Version" );
        mVersionTxt = pToolkit.createText( pParent, new String( ) );
        mVersionTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mVersionTxt.addModifyListener( new ModifyListener () {
            public void modifyText(ModifyEvent pE) {
                mModel.mVersion = mVersionTxt.getText();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void addLocale(Locale pLocale) {
        if ( !mModel.mDisplayNames.containsKey( pLocale ) ) {
            mModel.mDisplayNames.put( pLocale, new String( ) );
        }
        mNameTxt.setEnabled( true );
    }

    /**
     * {@inheritDoc}
     */
    public void deleteLocale(Locale pLocale) {
        mModel.mDisplayNames.remove( pLocale );
        if ( mModel.mDisplayNames.size() == 0 ) {
            mNameTxt.setEnabled( false );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectLocale(Locale pLocale) {
        
        if ( mCurrentLocale != null ) {
            mModel.mDisplayNames.put( mCurrentLocale, mNameTxt.getText( ) );
        }
        super.selectLocale(pLocale);
        String name = mModel.mDisplayNames.get( pLocale );
        mNameTxt.setText( name );
    }
}
