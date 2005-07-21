package org.openoffice.ide.eclipse.gui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.model.UnoidlProject;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlDecorator extends LabelProvider implements ILabelDecorator {

	/**
	 * Methods that returns the idl icons for idl resources
	 */
	public Image decorateImage(Image image, Object element) {
		
		
		Image newImage = null;
		
		if (isIdlFolder(element)){
			newImage = OOEclipsePlugin.getImage(ImagesConstants.IDL_FOLDER);
		}
		
		return newImage;
	}

	/**
	 * Replace the "." by "/". It avoid the package explorer to show it as a package
	 */
	public String decorateText(String text, Object element) {
		
		if (isIdlFolder(element)){
			
			text = text.replaceAll("\\.", "/"); 
		}
		
		return text;
	}

	/**
	 * Tests if the element is a folder containing the IDL_FOLDER persistent property
	 * 
	 * @param element element to check
	 * @return <code>true</code> if the element if a folder a possess the IDL_FOLDER
	 *         persistent property, <code>false</code> otherwise.
	 */
	private boolean isIdlFolder(Object element){
		boolean result = false;
		
		/** 
		 * If the element is a folder that has the property
		 * IDL_FOLDER set to <code>true</code>, apply the IDL_FOLDER icon
         */
		if (element instanceof IResource){
			IResource resource = (IResource)element;

			try {
				if (IResource.FOLDER == resource.getType()){
					String propertyValue = resource.getPersistentProperty(
							new QualifiedName(OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
										      UnoidlProject.IDL_FOLDER)); 
					
					if (null != propertyValue && propertyValue.equals("true")){
						result = true;
					}
				}
			} catch (CoreException e){
				OOEclipsePlugin.logError("Impossible to get a property", e); // TODO i18n
				result = false;
			}
		}
		
		return result;
	}
}
