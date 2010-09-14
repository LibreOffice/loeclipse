/*************************************************************************
 *
 * $RCSfile: UnoidlEditor.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:28 $
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * Sun Microsystems Inc., October, 2000
 *
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 * 
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 *
 * Copyright: 2002 by Sun Microsystems, Inc.
 *
 * All Rights Reserved.
 *
 * Contributor(s): Cedric Bosdonnat
 *
 *
 ************************************************************************/
package org.openoffice.ide.eclipse.core.editors.idl;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.editors.utils.ColorProvider;
import org.openoffice.ide.eclipse.core.editors.utils.OOTextEditor;

/**
 * Class handling the UNO-IDL text to render them in an Eclipse editor. In order
 * to fully understand the editor mechanisms, please report to Eclipse plugin
 * developer's guide.
 * 
 * @see UnoidlConfiguration for the viewer configuration
 * @see UnoidlDocumentProvider for the document provider
 * 
 * @author cedricbosdo
 *
 */
public class UnoidlEditor extends OOTextEditor {
    
    /**
     * Member that listens to the preferences property changes.
     */
    private IPropertyChangeListener mPropertyListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent pEvent) {
            getSourceViewer().invalidateTextPresentation();
            
        }
    };
    
    /**
     * The color manager providing the colors for the editor.
     */
    private ColorProvider mColorManager;
    
    /**
     * Default constructor setting the correct document provider and viewer
     * configuration.
     */
    public UnoidlEditor() {
        super();
        
        mColorManager = new ColorProvider();
        setSourceViewerConfiguration(new UnoidlConfiguration(mColorManager));
        setDocumentProvider(new UnoidlDocumentProvider());
        OOEclipsePlugin.getDefault().getPreferenceStore().addPropertyChangeListener(mPropertyListener);
    }
    
    /**
     * {@inheritDoc}
     */
    public void dispose() {
        mColorManager.dispose();
        OOEclipsePlugin.getDefault().getPreferenceStore().removePropertyChangeListener(mPropertyListener);
        super.dispose();
    }
}
