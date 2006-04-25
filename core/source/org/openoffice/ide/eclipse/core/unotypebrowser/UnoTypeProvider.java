/*************************************************************************
 *
 * $RCSfile: UnoTypeProvider.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/25 19:09:59 $
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
package org.openoffice.ide.eclipse.core.unotypebrowser;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.preferences.IOOo;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class UnoTypeProvider {

	public final static int MODULE = 1;
	
	public final static int INTERFACE = 2;
	
	public final static int SERVICE = 4;
	
	public final static int STRUCT = 8;
	
	public final static int ENUM = 16;
	
	public final static int EXCEPTION = 32;
	
	public final static int TYPEDEF = 64;
	
	public final static int CONSTANT = 128;
	
	public final static int CONSTANTS = 256;
	
	public final static int SINGLETON = 512;
	
	public UnoTypeProvider(IUnoidlProject aProject, int aTypes) {
		setTypes(aTypes);
		setProject(aProject);
	}
	
	public UnoTypeProvider(IOOo aOOoInstance, int aTypes) {
		setTypes(aTypes);
		setOOoInstance(aOOoInstance);
	}
	
	public void dispose(){
		removeAllTypes();
		
		internalTypes = null;
		oooInstance = null;
		pathToRegister = null;
		
		if (null != getTypesJob){
			getTypesJob = null;
		}
	}
	
	//---------------------------------------------------------- Type managment
	
	private int types = 1023;
	
	private static int[] allowededTypes = {
		MODULE,
		INTERFACE,
		SERVICE,
		STRUCT,
		ENUM,
		EXCEPTION,
		TYPEDEF,
		CONSTANT,
		CONSTANTS,
		SINGLETON
	};
	
	private static String[] stringTypes = {
		UnoTypesGetter.S_MODULE,
		UnoTypesGetter.S_INTERFACE,
		UnoTypesGetter.S_SERVICE,
		UnoTypesGetter.S_STRUCT,
		UnoTypesGetter.S_ENUM,
		UnoTypesGetter.S_EXCEPTION,
		UnoTypesGetter.S_TYPEDEF,
		UnoTypesGetter.S_CONSTANT,
		UnoTypesGetter.S_CONSTANTS,
		UnoTypesGetter.S_SINGLETON
	};
	
	public static int invertTypeBits(int aType){
		int result = 0;
		
		String sInv = Integer.toBinaryString(aType);
		int length = allowededTypes.length - sInv.length();
		
		if (length <= 10){
			
			for (int i=0; i<length; i++) {
				sInv = '0' + sInv;
			}
			
			sInv = sInv.replace('0', '2').replace('1', '0');
			sInv = sInv.replace('2', '1');
			result = Integer.parseInt(sInv, 2);
		}
		
		return result;
	}
	
	public static int convertTypeToInt(String aType) {
		
		int i = 0;
		boolean found = false;
		
		while (i<stringTypes.length && !found){
			if (stringTypes[i].equals(aType)) {
				found = true;
			} else {
				i++;
			}
		}
		
		int result = -1;
		if (found) {
			result = allowededTypes[i];
		}
		return result;
	}
	
	public void setTypes(int aTypes) {
		
		// Only 10 bits available
		if (aTypes >= 0 && aTypes < 1024) {
			types = aTypes;
		}
	}
	
	public int getTypes(){
		return types;
	}
	
	public boolean isTypeSet(int type){
		return (getTypes() & type) == type;
	}
	
	protected String computeTypesString(){
		String result = "";
		
		for (int i=0, length=allowededTypes.length; i<length; i++){
			int typeValue = allowededTypes[i];
			
			if (isTypeSet(typeValue)){
				result = result + " " + stringTypes[i];
			}
		}
		
		return result;
	}
	
	public boolean contains(String scopedName) {
		
		boolean result = false;
		scopedName = scopedName.replaceAll("::", ".");
		
		Iterator iter = internalTypes.iterator();
		while (iter.hasNext() && !result) {
			InternalUnoType type = (InternalUnoType)iter.next();
			if (type.getPath().equals(scopedName)) {
				result = true;
			}
		}
		
		return result;
	}
	
	//------------------------------------------------------- Project managment

	private IOOo	oooInstance;
	private String pathToRegister;
	
	private boolean initialized = false;
	
	public void setProject(IUnoidlProject aProject){
		
		if (null != aProject) {
			if (null != getTypesJob) {
				getTypesJob.cancel();
			}
			
			oooInstance = aProject.getOOo();
			pathToRegister = (aProject.getFile(
					aProject.getTypesPath()).getLocation()).toOSString();
			
			initialized = false;
			askUnoTypes();
		}
	}
	
	/**
	 * Sets the OOo if the new one is different from the old one.
	 * 
	 *  @param aOOoInstance OpenOffice.org instance to bootstrap
	 */
	public void setOOoInstance(IOOo aOOoInstance) {
		
		if (!(null != oooInstance && oooInstance.equals(aOOoInstance))) {
			
			// Stops the current job if there is already one.
			if (null != getTypesJob) {
				getTypesJob.cancel();
			}
			
			oooInstance = aOOoInstance;
			
			initialized = false;
			askUnoTypes();
		}
	}
	
	public boolean isInitialized(){
		return initialized;
	}
	
	//---------------------------------------------------- TypeGetter launching
	
	private String computeGetterCommand() throws IOException {
		String command = null;
		
		if (null != oooInstance) {
			// Defines OS specific constants
			String pathSeparator = System.getProperty("path.separator");
			String fileSeparator = System.getProperty("file.separator");
			
			
			
			// Constitute the classpath for OOo Boostrapping
			String classpath = "-cp ";
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				classpath = classpath + "\"";
			}
			
			String oooClassesPath = oooInstance.getClassesPath();
			File oooClasses = new File(oooClassesPath);
			String[] content = oooClasses.list();
			
			for (int i=0, length=content.length; i<length; i++){
				String contenti = content[i];
				if (contenti.endsWith(".jar")) {
					classpath = classpath + oooClassesPath + fileSeparator + 
									contenti + pathSeparator;
				}
			}
			
			// Add the UnoTypeGetter jar to the classpath
			URL pluginURL = Platform.find(
					OOEclipsePlugin.getDefault().getBundle(), 
					new Path("/."));
			
			String path = Platform.asLocalURL(pluginURL).getFile();
			path = path + "UnoTypesGetter.jar";
			
			if (Platform.getOS().equals(Platform.WS_WIN32)){
				path = path.substring(1).replaceAll("/", "\\\\");
			}
			
			classpath = classpath + path;
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				classpath = classpath + "\"";
			}
			classpath = classpath + " ";
			
			
			// Transforms the soffice path into a bootsrap valid one
			String sofficePath = oooInstance.getHome();
			sofficePath = sofficePath.replace(" ", "%20");
			
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				sofficePath = sofficePath.replace(fileSeparator, "/");
			}
			sofficePath = "-Ffile:///" + sofficePath + " ";
			
			// Add the local registry path
			String localRegistryPath = "";
			// If the path to the registry isn't set, don't take
			// it into account in the command build
			if (null != pathToRegister) {
				localRegistryPath = " -Lfile:///" + 
					pathToRegister.replace(" ", "%20");
			}
			
			// Computes the command  to execute
			command = "java " + classpath + 
					"org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypesGetter " + 
					sofficePath + "-B/" +computeTypesString() + localRegistryPath;
		}
		return command;
	}
	
	private Job getTypesJob;
	
	
	public void askUnoTypes() {
		
		if (null == getTypesJob || Job.RUNNING != getTypesJob.getState()) {
			getTypesJob = new Job(OOEclipsePlugin.getTranslationString(
					I18nConstants.FETCHING_TYPES)) {
	
				protected IStatus run(IProgressMonitor monitor) {
					
					IStatus status = new Status(IStatus.OK,
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							IStatus.OK,
							"",
							null);
					
					try {
						monitor.beginTask(
								OOEclipsePlugin.getTranslationString(
										I18nConstants.FETCHING_TYPES),
								2);
						removeAllTypes();
						
						String command = computeGetterCommand();
						
						// Computes the environment variables
						String[] vars = new String[1];
						if (Platform.getOS().equals(Platform.OS_WIN32)) {
							vars[0] = "PATH=" + 
									oooInstance.getHome() + "\\program";
						} else {
							vars[0] = "LD_LIBRARY_PATH=" + 
									oooInstance.getHome() + "/program";
						}
						
						Process process = Runtime.getRuntime().exec(command, vars);
						
						// Reads the types and add them to the list
						LineNumberReader reader = new LineNumberReader(
									new InputStreamReader(process.getInputStream()));
						
						String line = reader.readLine();
						monitor.worked(1);
						
						while (null != line) {
							InternalUnoType internalType = new InternalUnoType(line);
							addType(internalType);
							line = reader.readLine();
						}
						monitor.worked(1);
						setInitialized();
						
					} catch (IOException e) {
						monitor.worked(0);
						status = new Status(IStatus.ERROR,
								OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
								IStatus.ERROR,
								"",
								e);
						
						PluginLogger.getInstance().debug(e.getMessage());
					} catch (NullPointerException e) {
						PluginLogger.getInstance().debug(e.getMessage());
						monitor.worked(0);
						cancel();
					}
					
					return status;
				}
			};
			
			getTypesJob.setSystem(true);
			getTypesJob.setPriority(Job.INTERACTIVE);
			
			
			// Execute the job asynchronously
			getTypesJob.schedule();
		}
	}
	
	private Vector listeners = new Vector(); 
	
	public void addInitListener(IInitListener listener) {
		listeners.add(listener);
	}
	
	public void removeInitListener(IInitListener listener) {
		listeners.remove(listener);
	}
	
	private void setInitialized(){
		initialized = true;
		
		for (int i=0, length=listeners.size(); i<length; i++) {
			((IInitListener)listeners.get(i)).initialized();
		}
	}

	//---------------------------------------------------- Collection managment
	
	private Vector internalTypes = new Vector();
	
	
	public InternalUnoType getType(String typePath) {
		
		Iterator iter = internalTypes.iterator();
		InternalUnoType result = null;
		
		while (null == result && iter.hasNext()) {
			InternalUnoType type = (InternalUnoType)iter.next();
			if (type.getPath().equals(typePath)) {
				result = type;
			}
		}
		return result;
		
	}
	
	protected Object[] toArray() {
		return internalTypes.toArray();
	}
	
	protected void addType(InternalUnoType internalType) {
		internalTypes.add(internalType);
	}

	protected void removeAllTypes() {
		internalTypes.clear();
	}
}
