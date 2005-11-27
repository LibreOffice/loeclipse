/*************************************************************************
 *
 * $RCSfile: IdlcErrorReader.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:21 $
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
package org.openoffice.ide.eclipse.builders;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;

/**
 * Class that can read the idlc error output to transform the errors into markers
 * 
 * @author cbosdonnat
 *
 */
public class IdlcErrorReader {
	
	/**
	 * <p>Include error regular expression:</p>
	 * <p><em>cpp: &lt;file&gt;:&lt;line number&gt; some text Could not find include file &lt;missing include&gt;</em></p>
	 */
	private final static String R_IDLCPP_ERROR = "cpp: (\\S+):([0-9]+)(.*:[0-9]+)? (.*)";
	
	/**
	 * <p>Syntax error expression:</p> 
	 * <p><em>&lt;file&gt; (&lt;line number&gt;) : &lt;message&gt;</em></p>
	 */
	private final static String R_IDLC_ERROR  = "(.*)\\(([0-9]+)\\) : (WARNING, )?(.*)";
	
	/**
	 * Stream from which the reader extract the errors
	 */
	private LineNumberReader reader;
	
	/**
	 * File which compilation has been asked
	 */
	private IFile compiledFile;
	
	public IdlcErrorReader (InputStream stream, IFile file){
		reader = new LineNumberReader(new InputStreamReader(stream));
		compiledFile = file;
	}
	
	
	public void readErrors(){
		
		try {
			// Cleans the idlc error previously added
			compiledFile.deleteMarkers(IdlcBuilder.IDLERROR_MARKER_ID, true, IResource.DEPTH_INFINITE);
			
			// Read each line until the stream end (null line)
			String line = reader.readLine();
			
			// Mark only the file errors. The changed files will be compiled too
			
			while (null != line){
				
				// Handle the error line
				IMarker marker = analyseIdlcppError(line);
				
				if (null == marker){
					marker = analyseIdlcError(line);
				} 
				
				if (null == marker) {
					// Nothing to do
					if (null != System.getProperties().getProperty("DEBUG")){
						System.out.println(line); // Only whilst debugging
					}
					
				} else {
					
						// Keep only the markers for the errors concerning the file which is compiled
					if (IResource.FILE == marker.getResource().getType() && 
							!marker.getResource().getProjectRelativePath().toString().equals(compiledFile.getProjectRelativePath().toString())) {
						marker.delete();
					}
				}
				
				line = reader.readLine();
			}
		} catch (IOException e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(I18nConstants.ERROR_OUTPUT_UNREADABLE), e);
		} catch (CoreException e) {
			OOEclipsePlugin.logError(
					OOEclipsePlugin.getTranslationString(I18nConstants.MARKER_CREATION_FAILED)
								+ compiledFile.getProjectRelativePath().toString(),
					e);
		}
	}
	
	/**
	 * <p>Method that analyses the error line and return the appropriate marker if it is possible.</p>
	 * 
	 * @param line error line to analyse 
	 * @return the corresponding marker if the line is an idlc error line. <code>null</code> if there were
	 *          problems by creating the marker or if the line isn't a idlc error line.
	 */
	private IMarker analyseIdlcError(String line){
		IMarker marker = null;
		
		Pattern pSyntax = Pattern.compile(R_IDLC_ERROR); 
		Matcher mSyntax = pSyntax.matcher(line);
		
		if (!line.startsWith("idlc:") && mSyntax.matches()){
			IProject project = compiledFile.getProject();
			
			boolean error = false;
			if (null == mSyntax.group(3)){
				error = true;
			}
			
			String filePath = mSyntax.group(1); // HELP the groups are indexed from 1. 0 is the whole string
			int lineNo = Integer.parseInt(mSyntax.group(2));
			String message = mSyntax.group(mSyntax.groupCount());
			
			// Get a handle on the bad file
			// Create a project relative path
			if (filePath.startsWith(project.getLocation().toOSString())){
				int pos = project.getLocation().toOSString().length();
				filePath = filePath.substring(pos);
			}
			
			IFile file = project.getFile(filePath);
			try {
				marker = file.createMarker(IdlcBuilder.IDLERROR_MARKER_ID);
				marker.setAttribute(IMarker.SEVERITY, 
							error?IMarker.SEVERITY_ERROR: IMarker.SEVERITY_WARNING);
				marker.setAttribute(IMarker.MESSAGE, message);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNo);
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
				
				// HELP To print an icon in the editor vertical and overview rulers
				// Set the CHAR_START and CHAR_END attributes. They are relative to the
				// beginning or the end of the document.
				
				// HELP Without specifying the CHAR END and START, the line is well placed
				// But afterwards, pay attention when the marker should be located under 
				// the bad words
				
				// Try to find the line or word that causes the error
//				Map positions = getWrongWord(lineNo, message);
//				
//				marker.setAttribute(IMarker.CHAR_START, ((Integer)positions.get(IMarker.CHAR_START)).intValue());
//				marker.setAttribute(IMarker.CHAR_END,  ((Integer)positions.get(IMarker.CHAR_END)).intValue());
				
			} catch (CoreException e) {
				OOEclipsePlugin.logError(
						OOEclipsePlugin.getTranslationString(I18nConstants.MARKER_CREATION_FAILED)
									+ file.getProjectRelativePath().toString(),
						e);
			}
		}
		
		return marker;
	}
	
	/**
	 * 
	 * @param line
	 * @return
	 */
	private IMarker analyseIdlcppError(String line){
		IMarker marker = null;
		
		Pattern pInclude = Pattern.compile(R_IDLCPP_ERROR);
		Matcher mInclude = pInclude.matcher(line);
		
		if (mInclude.matches()){
			IProject project = compiledFile.getProject();

			String errorFilePath = mInclude.group(1);
			int lineNo = Integer.parseInt(mInclude.group(2));
			String badIncludePath = mInclude.group(4);
			
			if (null == mInclude.group(3)) {
			
				IFile errorFile;
				
				if (errorFilePath.startsWith(".")){
					// A project local file, that means that the error is in a dependent file
					errorFile = project.getFile(errorFilePath);
					
				} else {
					// The error is in the file which was asked for compilation
					errorFile = compiledFile;	
				}
				
				String message = "idlcpp error: " + badIncludePath;
				
				try {
					marker = errorFile.createMarker(IdlcBuilder.IDLERROR_MARKER_ID);
					marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					marker.setAttribute(IMarker.MESSAGE, message);
					marker.setAttribute(IMarker.LINE_NUMBER, lineNo);
					marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
					
					// Try to find the line or Word that causes the error
					Map positions = getWrongWord(lineNo, message);
					
					marker.setAttribute(IMarker.CHAR_START, ((Integer)positions.get(IMarker.CHAR_START)).intValue());
					marker.setAttribute(IMarker.CHAR_END,  ((Integer)positions.get(IMarker.CHAR_END)).intValue());
					
				} catch (CoreException e) {
					// Nothing to do. Do not create noise in the logs
				}	
			}
		}
		
		return marker;
	}
	
	/**
	 * 
	 * @param line
	 * @param message
	 * @return
	 */
	private Map getWrongWord(int line, String message){
		HashMap map = new HashMap();
		int start = 0;
		int end = 0;
		
		try {
			
			// Get the line offset.
			LineNumberReader fileReader = new LineNumberReader(new InputStreamReader(compiledFile.getContents()));
			int offset = 0;
			
			for (int i=0, length=line; i<length; i++){
				String tmpLine = fileReader.readLine();
				offset += tmpLine.length();
				
				// TODO Get the character offset in the line
			}
			
			start = offset;
			end = offset;
		} catch (Exception e) {
			// Nothing to report: the marker will be bad placed perhaps...
		}
		
		// Create the map content
		map.put(IMarker.CHAR_START, new Integer(start));
		map.put(IMarker.CHAR_END, new Integer(end));
		
		return map;
	}
	
}