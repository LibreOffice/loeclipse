/*************************************************************************
 *
 * $RCSfile: PackagePropertiesEditor.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/02/03 21:42:11 $
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
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.openoffice.ide.eclipse.core.model.IPackageChangeListener;
import org.openoffice.ide.eclipse.core.model.PackagePropertiesModel;

/**
 * @author cedricbosdo
 *
 */
public class PackagePropertiesEditor extends FormEditor {

	private SourcePage mSourcePage;
	private PackagePropertiesFormPage mFormPage;
	
	private PackagePropertiesModel mModel;
	private boolean mIgnoreSourceChanges = false;
	
	public PackagePropertiesEditor() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
	 */
	@Override
	protected void addPages() {
		
		try {
			// Add the form page with the tree
			mFormPage = new PackagePropertiesFormPage(this, "package"); //$NON-NLS-1$
			addPage(mFormPage);
			
			// Add the text page
			mSourcePage = new SourcePage(this, "source", "package.properties"); //$NON-NLS-1$ //$NON-NLS-2$
			mSourcePage.init(getEditorSite(), getEditorInput());
			mSourcePage.getDocumentProvider().addElementStateListener(new IElementStateListener() {

				public void elementContentAboutToBeReplaced(Object element) {
				}

				public void elementContentReplaced(Object element) {
				}

				public void elementDeleted(Object element) {
				}

				public void elementDirtyStateChanged(Object element, boolean isDirty) {
					if (!mIgnoreSourceChanges) {
						mModel.setQuiet(true);
					}
					loadFromSource();
					if (!mIgnoreSourceChanges) {
						mModel.setQuiet(false);
					}
				}

				public void elementMoved(Object originalElement, Object movedElement) {
				}				
			});
			addPage(mSourcePage);
		} catch (PartInitException e) {
			// log ?
		}
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		
		if (input instanceof IFileEditorInput) {
			
			IFileEditorInput fileInput = (IFileEditorInput)input;
			String projectName = fileInput.getFile().getProject().getName();
			setPartName(projectName);
			
			// Create the package properties
			mModel = new PackagePropertiesModel(fileInput.getFile());
			mModel.addChangeListener(new IPackageChangeListener() {

				public void packagePropertiesChanged() {
					editorDirtyStateChanged();
					writeToSource();
				}

				public void packagePropertiesSaved() {
					editorDirtyStateChanged();
				}
				
			});
		}
	}
	
	@Override
	public boolean isDirty() {
		return mModel.isDirty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			mModel.write();
			mSourcePage.doRevertToSaved();
		} catch (Exception e) {
			// Log ?
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		// Not allowed
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public PackagePropertiesModel getModel() {
		return mModel;
	}
	
	public void writeToSource() {
		if (mSourcePage.getDocumentProvider() instanceof TextFileDocumentProvider) {
			TextFileDocumentProvider docProvider = (TextFileDocumentProvider)mSourcePage.getDocumentProvider();
			IDocument doc = docProvider.getDocument(mSourcePage.getEditorInput());
			if (doc != null) {
				mIgnoreSourceChanges = true;
				doc.set(mModel.writeToString());
				mIgnoreSourceChanges = false;
			}
		}
	}
	
	public void loadFromSource() {
		
		if (mSourcePage.getDocumentProvider() instanceof TextFileDocumentProvider) {
			TextFileDocumentProvider docProvider = (TextFileDocumentProvider)mSourcePage.getDocumentProvider();
			IDocument doc = docProvider.getDocument(mSourcePage.getEditorInput());
			if (doc != null) {
				mModel.reloadFromString(doc.get());
			}
		}
	}
}
