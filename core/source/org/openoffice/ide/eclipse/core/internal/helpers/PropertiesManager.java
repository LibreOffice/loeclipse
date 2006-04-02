package org.openoffice.ide.eclipse.core.internal.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.internal.model.OOo;
import org.openoffice.ide.eclipse.core.internal.model.SDK;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

public class PropertiesManager {

	static public ISdk[] loadSDKs(){
		
		ISdk[] result = null;
		
		try {
			
			// Loads the sdks config file into a properties object
			Properties sdksProperties = getProperties();
			
			// Analyse the properties to get the SDKs 
			
			int i=0;
			boolean found = false;
			Vector sdks = new Vector(); 
			
			do {
				String path = sdksProperties.getProperty(OOEclipsePlugin.SDKPATH_PREFERENCE_KEY+i);
				
				found = !(null == path);
				i++;
				
				if (found){
					try {
						SDK sdk = new SDK(path);
						sdks.add(sdk);
					} catch (InvalidConfigException e){
						OOEclipsePlugin.logError(e.getLocalizedMessage(), e); // This message is localized in SDK class
					}
				}				
			} while (found);
			
			// transform the vector into an array
			result = new ISdk[sdks.size()];
			
			for (int j=0, length=sdks.size(); j<length; j++){
				result[j] = (ISdk)sdks.get(j);
			}
			
		} catch (IOException e) {
			OOEclipsePlugin.logError(
					OOEclipsePlugin.getTranslationString(
							I18nConstants.NOT_READABLE_FILE)+
							OOEclipsePlugin.OOO_CONFIG, e);
		}
		
		return result;
	}
	
	static public void saveSDKs(ISdk[] sdks){
		
		try {
			Properties sdksProperties = getProperties();
			
			// Load all the existing properties and remove the SDKPATH_PREFERENCE_KEY ones
			int j=0;
			boolean found = false;
			
			do {
				String sdkPath = sdksProperties.getProperty(
						OOEclipsePlugin.SDKPATH_PREFERENCE_KEY+j);
				found = (null != sdkPath);
				
				if (found){
					sdksProperties.remove(
							OOEclipsePlugin.SDKPATH_PREFERENCE_KEY+j);
				}
			} while (found);
			
			// Saving the new SDKs 
			for (int i=0; i<sdks.length; i++){
				ISdk sdki = sdks[i];
				sdksProperties.put(
						OOEclipsePlugin.SDKPATH_PREFERENCE_KEY+i, sdki.getHome());
			}
		
			
			String sdks_config_url = OOEclipsePlugin.getDefault().getStateLocation().toString();
			File file = new File(sdks_config_url+"/"+OOEclipsePlugin.OOO_CONFIG);
			if (!file.exists()){
				file.createNewFile();
			}
			
			sdksProperties.store(new FileOutputStream(file), "");
		} catch (FileNotFoundException e) {
			OOEclipsePlugin.logError(e.getLocalizedMessage(), e);
		} catch (IOException e){
			OOEclipsePlugin.logError(e.getLocalizedMessage(), e);
		}
	}
	
static public IOOo[] loadOOos(){
		
		IOOo[] result = null;
		
		try {
			
			// Loads the ooos config file into a properties object
			Properties ooosProperties = getProperties();
			
			// Analyse the properties to get the OOos 
			
			int i=0;
			boolean found = false;
			Vector ooos = new Vector(); 
			
			do {
				String path = ooosProperties.getProperty(
						OOEclipsePlugin.OOOPATH_PREFERENCE_KEY+i);
				
				found = !(null == path);
				i++;
				
				if (found){
					try {
						OOo ooo = new OOo(path);
						ooos.add(ooo);
					} catch (InvalidConfigException e){
						OOEclipsePlugin.logError(e.getLocalizedMessage(), e);
					}
				}				
			} while (found);
			
			// transform the vector into an array
			result = new IOOo[ooos.size()];
			
			for (int j=0, length=ooos.size(); j<length; j++){
				result[j] = (IOOo)ooos.get(j);
			}
			
		} catch (IOException e) {
			OOEclipsePlugin.logError(
					OOEclipsePlugin.getTranslationString(
							I18nConstants.NOT_READABLE_FILE)+
							OOEclipsePlugin.OOO_CONFIG, e);
		}
		
		return result;
	}
	
	static public void saveOOos(IOOo[] ooos){
		
		try {
			Properties ooosProperties = getProperties();
			
			// Load all the existing properties and remove the OOOPATH_PREFERENCE_KEY ones
			int j=0;
			boolean found = false;
			
			do {
				String oooPath = ooosProperties.getProperty(
						OOEclipsePlugin.OOOPATH_PREFERENCE_KEY+j);
				found = (null != oooPath);
				
				if (found){
					ooosProperties.remove(
							OOEclipsePlugin.OOOPATH_PREFERENCE_KEY+j);
				}
			} while (found);
			
			// Saving the new OOos 
			for (int i=0; i<ooos.length; i++){
				IOOo oooi = ooos[i];
				ooosProperties.put(
						OOEclipsePlugin.OOOPATH_PREFERENCE_KEY+i, oooi.getHome());
			}
		
			
			String ooos_config_url = OOEclipsePlugin.getDefault().
						getStateLocation().toString();
			File file = new File(ooos_config_url+"/"+OOEclipsePlugin.OOO_CONFIG);
			if (!file.exists()){
				file.createNewFile();
			}
			
			ooosProperties.store(new FileOutputStream(file), "");
		} catch (FileNotFoundException e) {
			OOEclipsePlugin.logError(e.getLocalizedMessage(), e);
		} catch (IOException e){
			OOEclipsePlugin.logError(e.getLocalizedMessage(), e);
		}
	}
	
	static private Properties getProperties() throws IOException{
		// Loads the ooos config file into a properties object
		String ooos_config_url = OOEclipsePlugin.getDefault().getStateLocation().toString();
		File file = new File(ooos_config_url+"/"+OOEclipsePlugin.OOO_CONFIG);
		if (!file.exists()){
			file.createNewFile();
		}
		
		Properties ooosProperties = new Properties();
	
		ooosProperties.load(new FileInputStream(file));
		
		return ooosProperties;
	}
}
