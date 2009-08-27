package org.openoffice.ide.eclipse.core.editors.main;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;

/**
 * Abstract section class providing mechanisms to suspend the dirty state change
 * notifications.
 * 
 * @author Cédric Bosdonnat
 *
 */
public abstract class AbstractOverviewSection extends SectionPart {

    private boolean mNotifyChanges;
    
    /**
     * The SectionPart constructor.
     * 
     * @param pParent the parent composite
     * @param pPage the form page to use
     * @param pStyle the SectionPart style
     */
    public AbstractOverviewSection(Composite pParent, FormPage pPage,
            int pStyle) {
        super(pParent, pPage.getManagedForm().getToolkit(), pStyle);
        initialize( pPage.getManagedForm() );
        getManagedForm().addPart( this );
        getSection( ).setLayoutData( new GridData( GridData.FILL_BOTH ) );
    }
    
    /**
     * Inhibate the {@link #markDirty()} function.
     * 
     * @param pNotify whether to notify or not.
     */
    public void setNotifyChanges( boolean pNotify ) {
        mNotifyChanges = pNotify;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void markDirty() {
        if ( mNotifyChanges ) {
            super.markDirty();
        }
    }

    /**
     * Load the non-localized data from the model into the fields.
     */
    public abstract void loadData();
}
