package org.openoffice.ide.eclipse.core.editors.main;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author cbosdonnat
 *
 */
public abstract class LocalizedSection extends SectionPart implements ILocaleListener {
    
    protected Locale mCurrentLocale;
    
    private FormPage mPage;
    
    /**
     * @param pParent the parent composite where to add the section
     * @param pPage the page page of the section
     * @param pStyle a bit-or of the styles defined in Section class
     */
    public LocalizedSection ( Composite pParent, FormPage pPage, int pStyle ) {
        super( pParent, pPage.getManagedForm().getToolkit(), pStyle );
        
        mPage = pPage;
        
        
        createContent( );
    }
    
    /**
     * Create the localized controls in the given parent.
     * 
     * @param pToolkit the toolkit to use for the controls creation
     * @param pParent the parent to use for the new controls.
     */
    protected abstract void createControls( FormToolkit pToolkit, Composite pParent ) ;
    
    /**
     * {@inheritDoc}
     */
    public void selectLocale(final Locale pLocale) {
        mCurrentLocale = pLocale;
    }
    
    /**
     * Creates the dialog content.
     */
    private void createContent( ) {
        // Create the Language selection tools
        Section section = getSection();
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        FormToolkit toolkit = mPage.getManagedForm().getToolkit();
        
        Composite clientArea = toolkit.createComposite(section);
        clientArea.setLayout(new GridLayout());
        clientArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        createControls( toolkit, clientArea );
        
        toolkit.paintBordersFor( clientArea );
        
        section.setClient( clientArea );
    }
}
