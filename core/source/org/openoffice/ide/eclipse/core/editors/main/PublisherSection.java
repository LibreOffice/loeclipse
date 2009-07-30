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
import org.openoffice.ide.eclipse.core.model.description.PublisherInfos;

/**
 * Class implementing the publisher form section.
 * 
 * @author cbosdonnat
 *
 */
public class PublisherSection extends LocalizedSection {
    
    private DescriptionModel mModel;
    
    private Text mUrlTxt;
    private Text mNameTxt;
    
    /**
     * @param pParent the parent composite where to add the section
     * @param pPage the parent page
     */
    public PublisherSection(Composite pParent, PackageOverviewFormPage pPage) {
        super( pParent, pPage, Section.TITLE_BAR );
        
        getSection().setText( "Provider informations" );
        
        mModel = pPage.getModel();
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
        mNameTxt.addModifyListener( new ModifyListener () {
            public void modifyText(ModifyEvent pE) {
                PublisherInfos infos = mModel.mPublisherInfos.get( mCurrentLocale );
                infos.mName = mNameTxt.getText();
            }
        });
        
        // Url controls
        pToolkit.createLabel( pParent, "Url" );
        mUrlTxt = pToolkit.createText( pParent, "" ); //$NON-NLS-1$
        mUrlTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mUrlTxt.setEnabled( false );
        mUrlTxt.addModifyListener( new ModifyListener () {
            public void modifyText(ModifyEvent pE) {
                PublisherInfos infos = mModel.mPublisherInfos.get( mCurrentLocale );
                infos.mUrl = mUrlTxt.getText();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void addLocale(Locale pLocale) {
        if ( !mModel.mPublisherInfos.containsKey( pLocale ) ) {
            mModel.mPublisherInfos.put( pLocale, new PublisherInfos( ) );
        }
        mNameTxt.setEnabled( true );
        mUrlTxt.setEnabled( true );
    }

    /**
     * {@inheritDoc}
     */
    public void deleteLocale(Locale pLocale) {
        mModel.mPublisherInfos.remove( pLocale );
        if ( mModel.mPublisherInfos.size( ) == 0 ) {
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
            PublisherInfos infos = mModel.mPublisherInfos.get( mCurrentLocale );
            infos.mName = mNameTxt.getText();
            infos.mUrl = mUrlTxt.getText();
        }
        super.selectLocale(pLocale);
        PublisherInfos infos = mModel.mPublisherInfos.get( pLocale );
        mNameTxt.setText( infos.mName );
        mUrlTxt.setText( infos.mUrl );
    }
}