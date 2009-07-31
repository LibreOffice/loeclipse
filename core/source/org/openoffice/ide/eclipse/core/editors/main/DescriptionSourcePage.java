package org.openoffice.ide.eclipse.core.editors.main;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.openoffice.ide.eclipse.core.editors.PackagePropertiesEditor;
import org.openoffice.ide.eclipse.core.editors.SourcePage;

/**
 * The source page for the description.xml file.
 * 
 * @author Cédric Bosdonnat
 *
 */
public class DescriptionSourcePage extends SourcePage {
    
    /**
     * Description source editor page constructor.
     * 
     * @param pFormEditor the editor hosting the page.
     * @param pId the page identifier
     * @param pTitle the page title
     */
    public DescriptionSourcePage(FormEditor pFormEditor, String pId, String pTitle) {
        super( pFormEditor, pId, pTitle );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canLeaveThePage() {
        PackagePropertiesEditor editor = (PackagePropertiesEditor)getEditor();
        editor.loadDescFromSource();
        
        return super.canLeaveThePage();
    }
}
