/*************************************************************************
 *
 * $RCSfile: RegistrationHelper.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/07/17 21:00:32 $
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
package org.openoffice.ide.eclipse.java.registration;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;

/**
 * This class provides utility methods to generate the class and files needed
 * by the UNO services implementation registration.
 * 
 * @author cedricbosdo
 *
 */
public abstract class RegistrationHelper {

	public static final String CLASS_FILENAME = "RegistrationHandler";
	
	/**
	 * Creates all the necessary files for the java registration of UNO services
	 * implementations to the <code>regcomp</code> tool.
	 * 
	 * @param project the project where to create the registration handler
	 */
	public static void generateFiles(IUnoidlProject project) {
		
		// Get the path where to place the class and the implementations list
		IPath relPath = project.getImplementationPath();
		IFolder dest = project.getFolder(relPath);
		
		// Compute the name of the main implementation class
		String implPkg = project.getCompanyPrefix() + "." + project.getOutputExtension();
		
		// Create the RegistrationHandler.class file

		StringBuffer pattern = new StringBuffer();
		
		BufferedReader patternReader = null;
		InputStream in = null;
		try {
			in = RegistrationHelper.class.getResourceAsStream("RegistrationHandler.java.tpl");
			patternReader = new BufferedReader(new InputStreamReader(in));
			String line = patternReader.readLine();
			while (line != null) {
				pattern.append(line + "\n");
				line = patternReader.readLine();
			}
		} catch (IOException e) {
			// log the error
			PluginLogger.error("Error during registration class creation [reading template]", e);
		} finally {
			try {
				patternReader.close();
				in.close();
			} catch (Exception e){}
		}
		
		if (pattern.length() > 0) {
			String content = MessageFormat.format(pattern.toString(), new Object[]{implPkg});

			FileWriter writer = null;
			try {
				IFile classIFile = dest.getFile(CLASS_FILENAME + ".java");
				File classFile = classIFile.getLocation().toFile();
				
				if (!classFile.exists()) {
					classFile.getParentFile().mkdirs();
					classFile.createNewFile();
				}
				
				writer = new FileWriter(classFile);
				writer.append(content);
				
				// Define the new registration class
				
				
			} catch (IOException e) {
				PluginLogger.error("Error during registration class creation [writing class]", e);
			} finally {
				try {
					writer.close();
				} catch (Exception e) {}
			}
		}
		
		// Create the empty RegistrationHandler.classes file
		ByteArrayInputStream empty = new ByteArrayInputStream(new byte[0]);
		try {
			 File listFile = getClassesListFile(project).getLocation().toFile();
			 listFile.createNewFile();
		} catch (IOException e) {
			PluginLogger.error("Error during registration classes list", e);
		} finally {
			try {
				empty.close();
			} catch (Exception e) {}
		}
	}
	
	/**
	 * Add a UNO service implementation to the list of the project ones.
	 *  
	 * @param project the project where to add the implementation
	 * @param implName the fully qualified name of the implementation to add,
	 * 		eg: <code>org.openoffice.comp.test.MyServiceImpl</code>
	 */
	public static void addImplementation(IUnoidlProject project, String implName) {
		Vector<String> classes = readClassesList(project);
		if (!classes.contains(implName)) {
			classes.add(implName);
		}
		writeClassesList(project, classes);
	}
	
	/**
	 * remove a UNO service implementation from the list of the project ones.
	 *  
	 * @param project the project where to remove the implementation
	 * @param implName the fully qualified name of the implementation to remove,
	 * 		eg: <code>org.openoffice.comp.test.MyServiceImpl</code>
	 */
	public static void removeImplementation(IUnoidlProject project, String implName) {
		Vector<String> classes = readClassesList(project);
		classes.remove(implName);
		writeClassesList(project, classes);
	}
	
	/**
	 * Computes the registration class name for the given Uno project.
	 * 
	 * The registration class name is generally 
	 * <code>&lt;COMPANY.PREFIX&gt;.&lt;OUTPUTEXT&gt;.RegistrationHandler</code>.
	 * 
	 * @param project the project for which to compute the class name
	 * @return the registration class name
	 */
	public static String getRegistrationClassName(IUnoidlProject project) {
		// Compute the name of the main implementation class
		String implPkg = project.getCompanyPrefix() + "." + project.getOutputExtension();
		return implPkg + "." + CLASS_FILENAME;
	}
	
	public static Vector<String> readClassesList(IUnoidlProject project) {

		Vector<String> classes = new Vector<String>();
		
		IFile list = getClassesListFile(project);
		File file = list.getLocation().toFile();
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				PluginLogger.error("Error during registration classes list", e);
			}
		}
		
		// First read all the lines
		FileInputStream in = null;
		BufferedReader reader = null;
		try {
			in = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(in));

			String line = reader.readLine();
			while (line != null) {
				if (!classes.contains(line)) {
					classes.add(line);
				}
				line = reader.readLine();
			}
		} catch (Exception e) {

		} finally {
			try {
				reader.close();
				in.close();
			} catch (Exception e) { }
		}
		return classes;
	}
	
	private static void writeClassesList(IUnoidlProject project, Vector<String> classes) {
		
		IFile list = getClassesListFile(project);
		File file = list.getLocation().toFile();
		
		FileWriter writer = null;
		try {
		writer = new FileWriter(file);
		for (String implClass : classes) {
			writer.append(implClass + "\n");
		}
		} catch (IOException e) {
			PluginLogger.error("Error during classes list writing", e);
		} finally {
			try {
				writer.close();
			} catch (Exception e) { }
		}
		
		// update the list file in the workspace
		new FileRefreshJob(list).schedule();
	}
	
	private static IFile getClassesListFile(IUnoidlProject project) {
		// Get the path where to place the class and the implementations list
		IPath relPath = project.getImplementationPath();
		IFolder dest = project.getFolder(relPath);
		
		return dest.getFile("RegistrationHandler.classes");
	}
}
