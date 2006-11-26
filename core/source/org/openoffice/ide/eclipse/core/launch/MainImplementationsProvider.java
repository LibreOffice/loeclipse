package org.openoffice.ide.eclipse.core.launch;

import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;

public class MainImplementationsProvider {

	public String[] getImplementations(IUnoidlProject prj){
		Vector<String> implementations = new Vector<String>();
		
		if (prj instanceof UnoidlProject) {
			IProject project = ((UnoidlProject)prj).getProject();
			Vector<IConfigurationElement> mainProviders = getProvidersDefs();

			for (IConfigurationElement providerDef : mainProviders) {
				try {
					IMainProvider provider = (IMainProvider)providerDef.
					createExecutableExtension("class"); //$NON-NLS-1$
					implementations.addAll(provider.getMainNames(project));
				} catch (Exception e) {
					// Impossible to get the provider
				}
			}
		}
		return implementations.toArray(new String[implementations.size()]);
	}
	
	/**
	 * Convenience method returning the providers definitions from the plugins
	 * extensions points descriptions.
	 * 
	 * @return the array of the configuration element for the providers.
	 */
	private static Vector<IConfigurationElement> getProvidersDefs(){
		Vector<IConfigurationElement> result = new Vector<IConfigurationElement>();
		
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(
				"org.openoffice.ide.eclipse.core.MainProvider"); //$NON-NLS-1$
		if (point != null){
			
			IExtension[] extensions = point.getExtensions();
			
			for (int i=0; i<extensions.length; i++){
				
				IConfigurationElement[] elements = extensions[i].
						getConfigurationElements();
			
				for (int j=0; j<elements.length; j++) {
					IConfigurationElement elementj = elements[j];
					if (elementj.getName().equals("MainProvider")){ //$NON-NLS-1$
						result.add(elementj);
					}
				}
			}
		}
		
		return result;
	}
}
