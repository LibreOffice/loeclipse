/*************************************************************************
 *
 * $RCSfile: UnoidlEditor.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/02 20:13:04 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.core.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlEditor extends TextEditor {

	 /**
	  * Member that listens to the preferences porperty changes 
	  */
	 private IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				getSourceViewer().invalidateTextPresentation();
				
			}
		};
	
	
	private ColorProvider colorManager;
	
	public UnoidlEditor() {
		super();
		
		colorManager = new ColorProvider();
		setSourceViewerConfiguration(new UnoidlConfiguration(colorManager));
		setDocumentProvider(new UnoidlDocumentProvider());
		OOEclipsePlugin.getDefault().getPreferenceStore().addPropertyChangeListener(propertyListener);
	}
	
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		return super.createSourceViewer(parent, ruler, styles);
	}
	
	public void dispose() {
		colorManager.dispose();
		OOEclipsePlugin.getDefault().getPreferenceStore().removePropertyChangeListener(propertyListener);
		super.dispose();
	}
	
    public void doSave(IProgressMonitor progressMonitor) {
        super.doSave(progressMonitor);
    }
}
