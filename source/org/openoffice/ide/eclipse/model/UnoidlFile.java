/*************************************************************************
 *
 * $RCSfile: UnoidlFile.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/30 13:24:30 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the following licenses
 *
 *     - GNU Lesser General Public License Version 2.1
 *     - Sun Industry Standards Source License Version 1.1
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
 *
 * Sun Industry Standards Source License Version 1.1
 * =================================================
 * The contents of this file are subject to the Sun Industry Standards
 * Source License Version 1.1 (the "License"); You may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.openoffice.org/license.html.
 *
 * Software provided under this License is provided on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 * MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 * See the License for the specific provisions governing your rights and
 * obligations concerning the Software.
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
package org.openoffice.ide.eclipse.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;

/**
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlFile extends ResourceNode {

	public UnoidlFile(UnoidlProject aProject, IFile aFile) {
		
		super(aProject, aFile, computePath(aFile));  
	}

	/**
	 * Returns a handle object on the associated file. The later one may
	 * not exist.
	 * 
	 * @return the associated file handle
	 */
	public IFile getFile() {
		return (IFile)getResource();
	}
	
	public void open() {
		// TODO Parse the idl file when opening and construct all the children nodes
		
	}
	
	public String computeBeforeString(TreeNode callingNode) {
		
		String text = "";
		
		// Creates the define constant for the file
		UnoidlProject unoProject = (UnoidlProject)getParent();
		IPath idlPath = unoProject.getIdlRelativePath(getFile());
		
		String fileDefine = idlPath.toString().replace('/', '_');
		fileDefine = fileDefine.replace('\\', '_').replace('.', '_');
		fileDefine = "__" + fileDefine + "__";
		
		// Creates the text to write
		String ifndef = "#ifndef " + fileDefine + "\n";
		String define = "#define " + fileDefine + "\n";
		
		// Creates all the include lines
		String sIncludes = includes.size()>0? "\n": "";
		for (int i=0, length=includes.size(); i<length; i++){
			sIncludes = sIncludes + includes.get(i).toString();
		}
		
		text = text + ifndef + define + sIncludes + "\n";
		
		return text;
	}
	
	public String computeAfterString(TreeNode callingNode) {
		String endif  = "#endif \n";
		
		return "\n" + endif;
	}
	
	/**
	 * Saves the AST content into a file. The previous file with that name is overwritten or
	 * created if it doesn't exists.  
	 *
	 */
	public void save(){
		
		final String text = toString(this);
		
		try {
			InputStream contentStream = new InputStream(){
	
				private StringReader reader = new StringReader(text);
				
				public int read() throws IOException {
					return reader.read();
				}
			};
			
			if (getFile().exists()){
				getFile().setContents(contentStream, true, true, null);
			} else {
				getFile().create(contentStream, true, null);
			}
				
		} catch (CoreException e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
					I18nConstants.CREATE_FILE_FAILED) + getFile().getName(), e);
		}		
	}
	
	public void addDeclaration(Declaration declaration){
		
		TreeNode ancestor = declaration;
		Declaration actualDeclaration = null;
		while (null != ancestor && ancestor instanceof Declaration) {
			actualDeclaration = (Declaration)ancestor;
			actualDeclaration.addFile(this);
			ancestor = ancestor.getParent();
		}
		
		addNode(actualDeclaration);
	}
	
	public static String computePath(IFile file){
		
		String result = "";
		
		try {
			UnoidlProject unoProject = (UnoidlProject)file.getProject().
					getNature(OOEclipsePlugin.UNO_NATURE_ID);
			IPath idlPath = unoProject.getIdlRelativePath(file);
			result = idlPath.toString().replace('.', '_');
			
		} catch (CoreException e) {
			if (null != System.getProperty("DEBUG")){
				e.printStackTrace();
			}
		}
		return result;
	}
	
	//------------------------------------------------------ Includes managment
	
	private Vector includes = new Vector();
	
	public void addInclude(Include include){
		if (!includes.contains(include)){
			includes.add(include);
			// TODO It might be interesting to notify these changes
		}
	}
	
	public void removeInclude(Include include){
		// TODO It might be interesting to notify these changes
		includes.removeElement(include);
	}
	
	public Vector getIncludes(){
		return includes;
	}
	
	public boolean containInclude(Include include){
		boolean contain = false;
		
		if (includes.contains(include)){
			contain = true;
		}
		return contain;
	}
	
	public int getIncludesCount(){
		return includes.size();
	}
}
