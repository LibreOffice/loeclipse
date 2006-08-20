package org.openoffice.ide.eclipse.core.internal.helpers;

import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.openoffice.ide.eclipse.core.model.ILanguage;

/**
 * Helper class for implementation languages handling
 * 
 * @author cbosdonnat
 *
 */
public class LanguagesHelper {
	
	/**
	 * Returns all the available uno-idl implementation language names
	 * installed on the platform
	 */
	public static String[] getAvailableLanguageNames(){
		
		String[] result = null;
		
		IConfigurationElement[] languages = getLanguagesDefs();
		result = new String[languages.length];
		
		for (int i=0; i<languages.length; i++)  {
			result[i] = languages[i].getAttribute("name"); //$NON-NLS-1$
		}
		
		return result;
	}
	
	/**
	 * Returns the language name as specified in the <code>plugin.xml</code>
	 * file from the language object.
	 * 
	 * @param language the language object
	 * @return the language name
	 */
	public static String getNameFromLanguage(ILanguage language) {
		
		String name = null;
		
		IConfigurationElement[] languages = getLanguagesDefs();
		int i = 0;
		
		while (name == null && i < languages.length) {
			IConfigurationElement languagei = languages[i];
			if (languagei.getAttribute("class").equals( //$NON-NLS-1$
					language.getClass().getName())) {
				
				name = languagei.getAttribute("name"); //$NON-NLS-1$
			}
			i++;
		}
		
		return name;
	}
	
	/**
	 * <p>Returns the language corresponding to a language name. The result may
	 * be null if:
	 *   <ul>
	 *   	<li>There is no such language name</li>
	 *      <li>The corresponding class cannot be found</li>
	 *      <li>The corresponding class doesn't implement ILanguage</li>
	 *   </ul>
	 * </p>
	 */
	public static ILanguage getLanguageFromName(String name){
		
		ILanguage language = null;
		
		IConfigurationElement[] languages = getLanguagesDefs();
		int i=0;
		
		while (language == null && i < languages.length) {
			IConfigurationElement languagei = languages[i];
			if (languagei.getAttribute("name").equals(name)) { //$NON-NLS-1$
				try {
					Object oLanguage = languagei.
						createExecutableExtension("class"); //$NON-NLS-1$
					
					if (oLanguage instanceof ILanguage) {
						language = (ILanguage)oLanguage;
					}
					
				} catch (Exception e) {
					// No such class
				}
			}
			i++;
		}
		
		return language;
	}
	
	/**
	 * Convenience method returning the language definitions from the plugins
	 * extensions points descriptions.
	 * 
	 * @return the array of the configuration element for the languages.
	 */
	private static IConfigurationElement[] getLanguagesDefs(){
		IConfigurationElement[] result = null;
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(
				"org.openoffice.ide.eclipse.core.language"); //$NON-NLS-1$
		if (point != null){
			
			IExtension[] extensions = point.getExtensions();
			Vector languages = new Vector();
			
			for (int i=0; i<extensions.length; i++){
				
				IConfigurationElement[] elements = extensions[i].
						getConfigurationElements();
			
				for (int j=0; j<elements.length; j++) {
					IConfigurationElement elementj = elements[j];
					if (elementj.getName().equals("language")){ //$NON-NLS-1$
						languages.add(elementj);
					}
				}
			}
			
			result = new IConfigurationElement[languages.size()];
			for (int i=0, length=languages.size(); i<length; i++) {
				result[i] = (IConfigurationElement)languages.get(i);
			}
			
			// clean the vector
			languages.clear();
		}
		
		return result;
	}
}
