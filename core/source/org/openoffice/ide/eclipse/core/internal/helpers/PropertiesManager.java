package org.openoffice.ide.eclipse.core.internal.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.model.OOo;
import org.openoffice.ide.eclipse.core.internal.model.SDK;
import org.openoffice.ide.eclipse.core.internal.model.URE;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * Helper class for OOo and SDK preferences handling. These aren't stored in
 * the standard plugin preferences, but in a separate file: 
 * {@link org.openoffice.ide.eclipse.core.OOEclipsePlugin#OOO_CONFIG} 
 * 
 * @author cbosdonnat
 *
 */
public class PropertiesManager {
	
	/**
	 * OOo SDK path preference key. Used to store the preferences
	 */
	private static final String SDKPATH_PREFERENCE_KEY    = "sdkpath"; //$NON-NLS-1$
	
	/**
	 * OOo path preference key. Used to store the preferences
	 */
	private static final String OOOPATH_PREFERENCE_KEY    = "ooopath"; //$NON-NLS-1$
	
	private static final String OOONAME_PREFERENCE_KEY	  = "oooname"; //$NON-NLS-1$
	
	/**
	 * Loads the SDK properties
	 * 
	 * @return the loaded SDKs
	 */
	static public ISdk[] loadSDKs(){
		
		ISdk[] result = null;
		
		try {
			
			// Loads the sdks config file into a properties object
			Properties sdksProperties = getProperties();
			
			// Analyse the properties to get the SDKs 
			
			int i=0;
			boolean found = false;
			Vector<SDK> sdks = new Vector<SDK>(); 
			
			do {
				String path = sdksProperties.getProperty(SDKPATH_PREFERENCE_KEY+i);
				
				found = !(null == path);
				i++;
				
				if (found){
					try {
						SDK sdk = new SDK(path);
						sdks.add(sdk);
					} catch (InvalidConfigException e){
						PluginLogger.error(
								e.getLocalizedMessage(), e); 
						// This message is localized in SDK class
					}
				}				
			} while (found);
			
			// transform the vector into an array
			result = new ISdk[sdks.size()];
			
			for (int j=0, length=sdks.size(); j<length; j++){
				result[j] = sdks.get(j);
			}
			
			// clean the vector
			sdks.clear();
			
		} catch (IOException e) {
			PluginLogger.error(
					Messages.getString("PropertiesManager.UnreadableFileError") + OOEclipsePlugin.OOO_CONFIG, e); //$NON-NLS-1$
		}
		
		return result;
	}
	
	/**
	 * Saves the SDK properties.
	 * 
	 * @param sdks the SDKs to save
	 */
	static public void saveSDKs(ISdk[] sdks){
		
		try {
			Properties sdksProperties = getProperties();
			
			// Load all the existing properties and remove the SDKPATH_PREFERENCE_KEY ones
			Enumeration keys = sdksProperties.keys();
			while (keys.hasMoreElements()) {
				String key = (String)keys.nextElement();
								
				if (key.startsWith(SDKPATH_PREFERENCE_KEY)){
					sdksProperties.remove(key);
				}
			}
			
			// Saving the new SDKs 
			for (int i=0; i<sdks.length; i++){
				ISdk sdki = sdks[i];
				sdksProperties.put(SDKPATH_PREFERENCE_KEY+i, sdki.getHome());
			}
		
			
			String sdks_config_url = OOEclipsePlugin.getDefault().
					getStateLocation().toString();
			File file = new File(sdks_config_url+"/"+OOEclipsePlugin.OOO_CONFIG); //$NON-NLS-1$
			if (!file.exists()){
				file.createNewFile();
			}
			
			FileOutputStream out = new FileOutputStream(file);
			try {
				sdksProperties.store(out, ""); //$NON-NLS-1$
			} finally {
				try { out.close(); } catch (IOException e) {}
			}
		} catch (FileNotFoundException e) {
			PluginLogger.error(e.getLocalizedMessage(), e);
		} catch (IOException e){
			PluginLogger.error(e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * Loads the OOo properties
	 * 
	 * @return the loaded OOos
	 */
	static public IOOo[] loadOOos(){
		
		IOOo[] result = null;
		
		try {
			
			// Loads the ooos config file into a properties object
			Properties ooosProperties = getProperties();
			
			// Analyse the properties to get the OOos 
			
			int i=0;
			boolean found = false;
			Vector<IOOo> ooos = new Vector<IOOo>(); 
			
			do {
				String path = ooosProperties.getProperty(
						OOOPATH_PREFERENCE_KEY+i);
				String name = ooosProperties.getProperty(
						OOONAME_PREFERENCE_KEY+i);
				
				found = !(null == path);
				i++;
				
				if (found){
					try {
						OOo ooo = new OOo(path, name);
						ooos.add(ooo);
					} catch (InvalidConfigException e){
						
						try {
							URE ure = new URE(path, name);
							ooos.add(ure);
						}
						catch (InvalidConfigException ex) {
							PluginLogger.error(
									e.getLocalizedMessage(), ex);
						}
					}
				}				
			} while (found);
			
			// transform the vector into an array
			result = new IOOo[ooos.size()];
			
			for (int j=0, length=ooos.size(); j<length; j++){
				result[j] = ooos.get(j);
			}
			
			// Clean the vector
			ooos.clear();
			
		} catch (IOException e) {
			PluginLogger.error(
				Messages.getString("PropertiesManager.UnreadableFileError") + OOEclipsePlugin.OOO_CONFIG, e); //$NON-NLS-1$
		}
		
		return result;
	}
	
	/**
	 * Saves the OOo properties.
	 * 
	 * @param sdks the OOos to save
	 */
	static public void saveOOos(IOOo[] ooos){
		
		try {
			Properties ooosProperties = getProperties();
			
			// Load all the existing properties and remove the OOOPATH_PREFERENCE_KEY ones
			Enumeration keys = ooosProperties.keys();
			while (keys.hasMoreElements()) {
				String key = (String)keys.nextElement();
								
				if (key.startsWith(OOOPATH_PREFERENCE_KEY) ||
						key.startsWith(OOONAME_PREFERENCE_KEY)){
					ooosProperties.remove(key);
				}
			}
			
			// Saving the new OOos 
			for (int i=0; i<ooos.length; i++){
				IOOo oooi = ooos[i];
				ooosProperties.put(OOOPATH_PREFERENCE_KEY+i, oooi.getHome());
				ooosProperties.put(OOONAME_PREFERENCE_KEY+i, oooi.getName());
			}
		
			
			String ooos_config_url = OOEclipsePlugin.getDefault().
						getStateLocation().toString();
			File file = new File(ooos_config_url+"/"+OOEclipsePlugin.OOO_CONFIG); //$NON-NLS-1$
			if (!file.exists()){
				file.createNewFile();
			}
			
			FileOutputStream out = new FileOutputStream(file);
			try {
				ooosProperties.store(out, ""); //$NON-NLS-1$
			} finally {
				try { out.close(); } catch (IOException e) {}
			}
		} catch (FileNotFoundException e) {
			PluginLogger.error(e.getLocalizedMessage(), e);
		} catch (IOException e){
			PluginLogger.error(e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * Loads the OOo and SDK properties from the 
	 * {@link OOEclipsePlugin#OOO_CONFIG} file.
	 * 
	 * @return the loaded properties
	 * @throws IOException is thrown if any problem happened during the file 
	 * 			reading
	 */
	static private Properties getProperties() throws IOException{
		// Loads the ooos config file into a properties object
		Properties ooosProperties = new Properties();
		
		String ooos_config_url = OOEclipsePlugin.getDefault().
						getStateLocation().toString();
		File file = new File(ooos_config_url+"/"+OOEclipsePlugin.OOO_CONFIG); //$NON-NLS-1$
		if (!file.exists()){
			file.createNewFile();
		}

		FileInputStream in = new FileInputStream(file);
		try {
			ooosProperties.load(in);
		} finally {
			try { in.close(); } catch (IOException e) {}
		}
		
		return ooosProperties;
	}
}
