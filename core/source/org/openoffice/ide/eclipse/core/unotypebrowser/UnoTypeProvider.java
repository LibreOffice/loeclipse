/*************************************************************************
 *
 * $RCSfile: UnoTypeProvider.java,v $
 *
 * $Revision: 1.8 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/26 21:36:16 $
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.model.OOo;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.preferences.IOOo;

/**
 * Class providing UNO types from an OpenOffice.org instance and optionally
 * from a UNO project.
 * 
 * @author cbosdonnat
 *
 */
public class UnoTypeProvider {
	
	private static UnoTypeProvider sInstance = new UnoTypeProvider();
	
	public static UnoTypeProvider getInstance() {
		return sInstance;
	}
	
	private UnoTypeProvider() {
	}
	
	/**
	 * Initializes the type provider from a UNO project.
	 * 
	 * @param aProject the UNO project to query (with its OOo parameter)
	 * @param aTypes the types to get
	 */
	public void initialize(IUnoidlProject aProject, int aTypes) {
		setTypes(aTypes);
		setProject(aProject);
	}
	
	/**
	 * Initializes the UNO type provider from an OpenOffice.org instance
	 * 
	 * @param aOOoInstance the OOo instance to query
	 * @param aTypes the types to get
	 */
	public void initialize(IOOo aOOoInstance, int aTypes) {
		setTypes(aTypes);
		setOOoInstance(aOOoInstance);
	}
	
	/**
	 * Stop the type provider
	 */
	public void stopProvider(){
		removeAllTypes();
		
		mInternalTypes = null;
		oooInstance = null;
		pathToRegister = null;
		
		if (getTypesThread != null && getTypesThread.isAlive()){
			getTypesThread.shutdown(); // Not sure it stops when running
			getTypesThread = null;
			PluginLogger.debug("UnoTypeProvider stopped"); //$NON-NLS-1$
		}
	}
	
	//---------------------------------------------------------- Type management
	
	private int types = 2047;
	
	private static int[] allowedTypes = {
		IUnoFactoryConstants.BASICS,
		IUnoFactoryConstants.MODULE,
		IUnoFactoryConstants.INTERFACE,
		IUnoFactoryConstants.SERVICE,
		IUnoFactoryConstants.STRUCT,
		IUnoFactoryConstants.ENUM,
		IUnoFactoryConstants.EXCEPTION,
		IUnoFactoryConstants.TYPEDEF,
		IUnoFactoryConstants.CONSTANT,
		IUnoFactoryConstants.CONSTANTS,
		IUnoFactoryConstants.SINGLETON
	};
	
	/**
	 * Method changing all the '1' into '0' and the '0' into '1' but only
	 * on the interesting bytes for the types.
	 * 
	 * @param aType
	 * @return the negated type
	 */
	public static int invertTypeBits(int aType){
		int result = 0;
		
		String sInv = Integer.toBinaryString(aType);
		int length = allowedTypes.length - sInv.length();
		
		if (length <= 11){
			
			for (int i=0; i<length; i++) {
				sInv = '0' + sInv;
			}
			
			sInv = sInv.replace('0', '2').replace('1', '0');
			sInv = sInv.replace('2', '1');
			result = Integer.parseInt(sInv, 2);
		}
		
		return result;
	}
	
	/**
	 * Set one or more types. To specify more than one types give the bit or
	 * of all the types, eg <code>INTERFACE | SERVICE</code>
	 * 
	 * @param aTypes the bit or of the types
	 */
	public void setTypes(int aTypes) {
		
		// Only 10 bits available
		if (aTypes >= 0 && aTypes <= InternalUnoType.ALL_TYPES) {
			if (types != aTypes) {
				types = aTypes;
				IOOo ooo = oooInstance;
				oooInstance = null;
				setOOoInstance(ooo);
			}
		}
	}
	
	/**
	 * Get the types set as an integer. The types field is a bit or of all the
	 * types set.
	 */
	public int getTypes(){
		return types;
	}
	
	/**
	 * Checks if the given type will be queried
	 * 
	 * @param type the type to match
	 * @return <code>true</code> if the type is one of the types set
	 */
	public boolean isTypeSet(int type){
		return (getTypes() & type) == type;
	}
	
	/**
	 * Checks whether the list contains the given type name
	 * 
	 * @param scopedName the type name to match
	 * @return <code>true</code> if the list contains a type with this name
	 */
	public boolean contains(String scopedName) {
		
		boolean result = false;
		scopedName = scopedName.replaceAll("::", "."); //$NON-NLS-1$ //$NON-NLS-2$
		
		if (isInitialized()) {
			Iterator iter = mInternalTypes.iterator();
			while (iter.hasNext() && !result) {
				InternalUnoType type = (InternalUnoType)iter.next();
				if (type.getFullName().equals(scopedName)) {
					result = true;
				}
			}
		}
		
		return result;
	}
	
	//------------------------------------------------------- Project managment

	private IOOo	oooInstance;
	private String pathToRegister;
	
	private boolean initialized = false;
	
	/**
	 * Set the UNO projet for which to get the UNO types. This project's
	 * <code>types.rdb</code> registry will be used as external registry
	 * for the types query.
	 * 
	 * @param aProject the project for which to launch the type query 
	 */
	private void setProject(IUnoidlProject aProject){
		
		if (null != aProject) {
			
			// Stop the provider before everything
			stopProvider();
			mInternalTypes = new Vector<InternalUnoType>();
			
			oooInstance = aProject.getOOo();
			pathToRegister = (aProject.getFile(
					aProject.getTypesPath()).getLocation()).toOSString();
			
			PluginLogger.debug(
					"UnoTypeProvider initialized with " + aProject); //$NON-NLS-1$
			
			initialized = false;
			askUnoTypes();
		}
	}
	
	/**
	 * Sets the OOo if the new one is different from the old one.
	 * 
	 *  @param aOOoInstance OpenOffice.org instance to bootstrap
	 */
	private void setOOoInstance(IOOo aOOoInstance) {
		
		if (null != aOOoInstance && !aOOoInstance.equals(oooInstance)) {
			
			// Stop the provider before everything
			stopProvider();
			mInternalTypes = new Vector<InternalUnoType>();
			
			oooInstance = aOOoInstance;
			PluginLogger.debug(
					"UnoTypeProvider initialized with " + aOOoInstance); //$NON-NLS-1$
			
			initialized = false;
			askUnoTypes();
		}
	}
	
	/**
	 * Return whether the type provider has been initialized
	 */
	public boolean isInitialized(){
		return initialized;
	}
	
	//---------------------------------------------------- TypeGetter launching
	
	private String computeGetterCommand() throws IOException {
		String command = null;
		
		if (null != oooInstance) {
			// Compute the library location (UnoTypesGetter.jar file)
			URL pluginURL = OOEclipsePlugin.getDefault().getBundle().getEntry("/."); //$NON-NLS-1$
			// NOTE not replaced by FileLocator to avoid dependency on Eclipse 3.2 
			URL libURL = FileLocator.toFileURL(pluginURL);

			// Compute the types mask argument
			String typesMask = "-T" + types; //$NON-NLS-1$
			
			// Get the OOo types.rdb registry path as external registry
			String typesPath = new Path(oooInstance.getTypesPath()).toString();
			typesPath = "-Efile:///" + typesPath.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
			// Add the local registry path
			String localRegistryPath = ""; //$NON-NLS-1$
			// If the path to the registry isn't set, don't take
			// it into account in the command build
			if (null != pathToRegister) {
				localRegistryPath = " -Lfile:///" +  //$NON-NLS-1$
					pathToRegister.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
				localRegistryPath = localRegistryPath.replace('\\', '/');
			}
			
			// compute the arguments array
			String[] args = new String[] {
					typesPath,
					localRegistryPath,
					typesMask
			};
			
			// Computes the command to execute if oooInstance isn't the URE
			if (oooInstance instanceof OOo) {
				
				String libPath = new Path(libURL.getPath()).toOSString();
				libPath = libPath + "UnoTypesGetter.jar"; //$NON-NLS-1$
				
				command = oooInstance.createUnoCommand(
						"org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypesGetter",  //$NON-NLS-1$
						libPath, new String[0], args);
			} else {

				String libPath = new Path(libURL.getPath()).toString();
				libPath = libPath + "UnoTypesGetter.jar"; //$NON-NLS-1$
				libPath = libPath.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
				
				command = oooInstance.createUnoCommand(
						"org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypesGetter",  //$NON-NLS-1$
						"file:///"+libPath, new String[]{}, args); //$NON-NLS-1$
			}
		}
		return command;
	}
	
	private UnoTypesGetterThread getTypesThread = new UnoTypesGetterThread();
	
	/**
	 * Launches the UNO type query process
	 */
	private void askUnoTypes() {
		
		if (null == getTypesThread || !getTypesThread.isAlive()) {
			
			mInternalTypes = new Vector<InternalUnoType>();
			
			getTypesThread = new UnoTypesGetterThread();
			getTypesThread.start();
		}
	}
	
	private Vector<IInitListener> listeners = new Vector<IInitListener>(); 
	
	/**
	 * Register the given listener
	 */
	public void addInitListener(IInitListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Makes the given initialization listener stop listening
	 */
	public void removeInitListener(IInitListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Propagate the news to the listeners that it has been initialized.
	 */
	private void setInitialized(){
		initialized = true;
		
		for (int i=0, length=listeners.size(); i<length; i++) {
			((IInitListener)listeners.get(i)).initialized();
		}
	}

	//---------------------------------------------------- Collection managment
	
	private Vector<InternalUnoType> mInternalTypes = new Vector<InternalUnoType>();
	
	/**
	 * Get a type from its path
	 * 
	 * @param typePath the type path
	 * 
	 * @return the corresponding complete type description
	 */
	public InternalUnoType getType(String typePath) {
		
		Iterator iter = mInternalTypes.iterator();
		InternalUnoType result = null;
		
		while (null == result && iter.hasNext()) {
			InternalUnoType type = (InternalUnoType)iter.next();
			if (type.getFullName().equals(typePath)) {
				result = type;
			}
		}
		return result;
		
	}
	
	/**
	 * Returns the types list as an array
	 */
	protected Object[] toArray() {
		Object[] types = new Object[0];
		if (mInternalTypes != null) {
			types = mInternalTypes.toArray();
		}
		return types;
	}
	
	/**
	 * Add a type to the list
	 */
	protected void addType(InternalUnoType internalType) {
		mInternalTypes.add(internalType);
	}

	/**
	 * purge the types list
	 */
	protected void removeAllTypes() {
		if (mInternalTypes != null) {
			mInternalTypes.clear();
		}
	}
	
	private class UnoTypesGetterThread extends Thread {

		private Process mProcess;
		private boolean mStop = false;
		
		public void shutdown() {
			if (mProcess != null) {
				mProcess.destroy();
			}
			mProcess = null;
			mStop = true;
		}

		public void run() {
			try {
				removeAllTypes();
				String command = computeGetterCommand();

				// Computes the environment variables

				mProcess = Runtime.getRuntime().exec(command);

				if (!mStop) {
					// Reads the types and add them to the list
					InputStreamReader in = new InputStreamReader(mProcess.getInputStream());
					LineNumberReader reader = new LineNumberReader(in);

					try {
						String line = reader.readLine();

						while (null != line) {
							InternalUnoType internalType = new InternalUnoType(line);
							addType(internalType);
							line = reader.readLine();
						}
					} finally {
						reader.close();
						in.close();
					}
					setInitialized();
					PluginLogger.debug("Types fetched"); //$NON-NLS-1$
				}

			} catch (IOException e) {				
				PluginLogger.error(Messages.getString("UnoTypeProvider.IOError"), e); //$NON-NLS-1$
			} catch (Exception e) {
				PluginLogger.error(Messages.getString("UnoTypeProvider.UnexpectedError"), e); //$NON-NLS-1$
			}
		}
	}
}
