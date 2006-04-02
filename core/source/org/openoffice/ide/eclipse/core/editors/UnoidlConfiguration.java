/*************************************************************************
 *
 * $RCSfile: UnoidlConfiguration.java,v $
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

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.openoffice.ide.eclipse.core.editors.syntax.NonRuleBasedDamagerRepairer;
import org.openoffice.ide.eclipse.core.editors.syntax.UnoidlDocScanner;
import org.openoffice.ide.eclipse.core.editors.syntax.UnoidlPartitionScanner;
import org.openoffice.ide.eclipse.core.editors.syntax.UnoidlPreprocessorScanner;
import org.openoffice.ide.eclipse.core.editors.syntax.UnoidlScanner;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlConfiguration extends SourceViewerConfiguration {
	private UnoidlDoubleClickStrategy doubleClickStrategy;
	private UnoidlScanner scanner;
	private UnoidlDocScanner docScanner;
	private UnoidlPreprocessorScanner preprocScanner;
	private ColorProvider colorManager;

	public UnoidlConfiguration(ColorProvider colorManager) {
		this.colorManager = colorManager;
	}
	
	//----------------------------------------- Text editing facilities support
	
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new UnoidlDoubleClickStrategy();
		return doubleClickStrategy;
	}
	
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer,
											String contentType) {
		
		// TODO Add here the IAutoEditStrategies to manage:
		//   - Automatic indentation
		//   - Automatic bracket closing
		//   - Automatic comment closing
		
		return super.getAutoEditStrategies(sourceViewer, contentType);
	}

	//--------------------------------------------- Syntax highlighting support
	
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			UnoidlPartitionScanner.IDL_AUTOCOMMENT,
			UnoidlPartitionScanner.IDL_COMMENT,
			UnoidlPartitionScanner.IDL_PREPROCESSOR};
	}
	
	protected UnoidlScanner getCodeScanner() {
		if (scanner == null) {
			scanner = new UnoidlScanner(colorManager);
			scanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(Colors.C_TEXT))));
		}
		return scanner;
	}
	
	protected UnoidlDocScanner getDocScanner(){
		if (docScanner == null) {
			docScanner = new UnoidlDocScanner(colorManager);
			docScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(Colors.C_AUTODOC_COMMENT))));
		}
		return docScanner;
	}
	
	protected UnoidlPreprocessorScanner getPreprocScanner(){
		if (preprocScanner == null) {
			preprocScanner = new UnoidlPreprocessorScanner(colorManager);
			preprocScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(Colors.C_PREPROCESSOR))));
		}
		return preprocScanner;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		
		// Scans the AutoDoc comments
		DefaultDamagerRepairer drAC = new DefaultDamagerRepairer(getDocScanner());
		reconciler.setDamager(drAC, UnoidlPartitionScanner.IDL_AUTOCOMMENT);
		reconciler.setRepairer(drAC, UnoidlPartitionScanner.IDL_AUTOCOMMENT);

		// Affects a Damager repairer for IDL_COMMENT sections
		NonRuleBasedDamagerRepairer ndr =
            new NonRuleBasedDamagerRepairer(
                new TextAttribute(
                    colorManager.getColor(Colors.C_COMMENT)));
        
        reconciler.setDamager(ndr, UnoidlPartitionScanner.IDL_COMMENT);
        reconciler.setRepairer(ndr, UnoidlPartitionScanner.IDL_COMMENT);
        
        // Scans the code for more precise syntax highlighting
		DefaultDamagerRepairer drCode =
			new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(drCode, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(drCode, IDocument.DEFAULT_CONTENT_TYPE);
		
		DefaultDamagerRepairer drPreproc = new DefaultDamagerRepairer(getPreprocScanner());
		reconciler.setDamager(drPreproc, UnoidlPartitionScanner.IDL_PREPROCESSOR);
		reconciler.setRepairer(drPreproc, UnoidlPartitionScanner.IDL_PREPROCESSOR);

		return reconciler;
	}
}