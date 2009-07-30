/**
 * 
 */
package org.openoffice.ide.eclipse.core.editors.main;

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
 * Section displaying the release notes part of the descriptions.xml file.
 * 
 * @author cbosdonnat
 *
 */
public class ReleaseNotesSection extends LocalizedSection implements
        ILocaleListener {

    private DescriptionModel mModel;
    
    private Text mUrlTxt;
    
    /**
     * @param pParent the parent composite where to add the section
     * @param pPage the parent page
     */
    public ReleaseNotesSection(Composite pParent, PackageOverviewFormPage pPage) {
        super( pParent, pPage, Section.TITLE_BAR );
        
        getSection().setText( "Release notes" );
        
        mModel = pPage.getModel();
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
        mUrlTxt = pToolkit.createText( pParent, new String( ) );
        mUrlTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mUrlTxt.setEnabled( false );
        mUrlTxt.addModifyListener( new ModifyListener () {
            public void modifyText(ModifyEvent pE) {
                mModel.mReleaseNotes.put( mCurrentLocale, mUrlTxt.getText() );
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void addLocale(Locale pLocale) {
        if ( !mModel.mReleaseNotes.containsKey( pLocale ) ) {
            mModel.mReleaseNotes.put( pLocale, new String( ) );
        }
        mUrlTxt.setEnabled( true );
    }

    /**
     * {@inheritDoc}
     */
    public void deleteLocale(Locale pLocale) {
        mModel.mReleaseNotes.remove( pLocale );
        if ( mModel.mReleaseNotes.size( ) == 0 ) {
            mUrlTxt.setEnabled( false );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void selectLocale(Locale pLocale) {
        
        if ( mCurrentLocale != null ) {
            mModel.mReleaseNotes.put( mCurrentLocale, mUrlTxt.getText() );
        }
        super.selectLocale(pLocale);
        mUrlTxt.setText( mModel.mReleaseNotes.get( pLocale ) );
    }
}
