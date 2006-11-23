/**
 * 
 */
package org.openoffice.ide.eclipse.core.internal.model;

import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openoffice.ide.eclipse.core.model.IUnoComposite;

/**
 * Implements the UNO-IDL model composite. This class could certainly be
 * rewritten using the Java Format tools.
 * 
 * @author cbosdonnat
 *
 */
public class UnoComposite implements IUnoComposite {

	private Vector<IUnoComposite> mChildren = new Vector<IUnoComposite>();
	
	private int mType = COMPOSITE_TYPE_NOTSET;
	
	private Hashtable<String, Object> mProperties;
	private String mTemplate;
	private String mFilename;
	private String mSeparator;
	
	private boolean mIndentation = false;
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.IUnoComposite#dispose()
	 */
	public void dispose() {
		removeAll();
		if (mProperties != null) mProperties.clear();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see unotest.IUnoComposite#getChildren()
	 */
	public IUnoComposite[] getChildren() {
		
		IUnoComposite[] composites = new IUnoComposite[mChildren.size()];
		for (int i=0, length=mChildren.size(); i<length; i++) {
			composites[i] = (IUnoComposite)mChildren.get(i);
		}
		
		return composites;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see unotest.IUnoComposite#addChild(unotest.IUnoComposite)
	 */
	public void addChild(IUnoComposite aChild) {
		
		if (aChild != null){
			mChildren.add(aChild);
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see unotest.IUnoComposite#removeAll()
	 */
	public void removeAll() {

		IUnoComposite[] composites = getChildren();
		
		for (int i=0; i<composites.length; i++){
			IUnoComposite compositei = composites[i];
			compositei.dispose();
			mChildren.removeElement(compositei);
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see unotest.IUnoComposite#setType(int)
	 */
	public void setType(int aType) {
		
		if (mType == COMPOSITE_TYPE_NOTSET && 
				(aType == COMPOSITE_TYPE_FILE ||
			     aType == COMPOSITE_TYPE_FOLDER ||
			     aType == COMPOSITE_TYPE_TEXT)) {
			
			mType = aType;
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see unotest.IUnoComposite#getType()
	 */
	public int getType() {
		return mType;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see unotest.IUnoComposite#configure(java.util.Hashtable, java.lang.String)
	 */
	public void configure(Hashtable<String, Object> aProperties, String aTemplate) {
		
		mTemplate = aTemplate;
		String[] parts = splitTemplate();
		mProperties = new Hashtable<String, Object>();
		
		// Get the variable parts and their name
		for (int i=0; i<parts.length; i++) {
			
			String parti = parts[i];
			Matcher matcher = Pattern.compile("\\$\\{(\\w+)\\}").matcher(parti); //$NON-NLS-1$
			
			// If the part is "${children}", it's not a property
			if (!parti.equals("${children}") && matcher.matches()){ //$NON-NLS-1$
				
				String namei = matcher.group(1);
				if (aProperties.containsKey(namei)){
					mProperties.put(namei, aProperties.get(namei));
				} else {
					// The property isn't described in the vector.
					mProperties.put(namei, ""); //$NON-NLS-1$
				}
			}
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see unotest.IUnoComposite#configure(java.lang.String)
	 */
	public void configure(String aFilename) {
		
		if (mType == COMPOSITE_TYPE_FILE || mType == COMPOSITE_TYPE_FOLDER) {
			mFilename = aFilename;
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.IUnoComposite#setIndented(boolean)
	 */
	public void setIndented(boolean toIndent) {
		if (mType == COMPOSITE_TYPE_TEXT){
			mIndentation = toIndent;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.IUnoComposite#setChildrenSeparator(java.lang.String)
	 */
	public void setChildrenSeparator(String separator) {
		if (separator == null) separator = ""; //$NON-NLS-1$
		mSeparator = separator;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see unotest.IUnoComposite#create(boolean)
	 */
	public void create(boolean force) throws Exception {
		
		File file;
		if (mType == COMPOSITE_TYPE_FILE || mType == COMPOSITE_TYPE_FOLDER) {
		
			file = new File(mFilename);
			
			// Create the parent directories
			if (file.getParentFile() != null){
				file.getParentFile().mkdirs();
			}
				
			// if the file exists and the force flag is up
			if ((file.exists() && force) || !file.exists()) {
				if (mType == COMPOSITE_TYPE_FILE) {
					file.createNewFile();
					
					// Write the children toString() in the file
					FileWriter out = new FileWriter(file);
					String content = new String();
					IUnoComposite[] composites = getChildren();
					for (int i=0; i<composites.length; i++) {
						content = content + composites[i].toString();
					}
					out.write(content);
					out.close();
					
				} else {
					file.mkdir();
				}
			}
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see unotest.IUnoComposite#toString()
	 */
	public String toString() {
		
		
		String result = new String();
		if (mType == COMPOSITE_TYPE_TEXT) {
			
			// String reconstitution
			String[] parts = splitTemplate();
			
			for (int i=0; i<parts.length; i++) {
				String parti = parts[i];
				
				if (parti.equals("${children}")) { //$NON-NLS-1$
					
					IUnoComposite[] composites = getChildren();
					for (int j=0; j<composites.length; j++){
						if (composites[j].getType() == COMPOSITE_TYPE_TEXT) {
							result = result + composites[j].toString();
							if (j < composites.length - 1 && mSeparator != null) {
								if (result == null) result = ""; //$NON-NLS-1$
								result += mSeparator;
							}
						}
					}
					
				} else {
				
					Matcher matcher = Pattern.compile("\\$\\{(\\w+)\\}").matcher(parti); //$NON-NLS-1$
					if (matcher.matches()){
						result =  result + mProperties.get(matcher.group(1));
					} else {
						result = result + parti;
					}
				}
			}
			
			// Indentation management
			// Do not add a \t between \n\n or \n$
			if (mIndentation) {
				
				for (int i=0; i<result.length(); i++){
					
					if (result.charAt(i) == '\n'){
						// '\n' found
						if ((i != result.length()-1) && 
								result.charAt(i+1) != '\n') {
							
							result = result.substring(0, i+1) + "\t" +  //$NON-NLS-1$
								result.substring(i+1);
						}
					}
				}
				result = "\t" + result; //$NON-NLS-1$
			}
			
		} else {
			result = super.toString();
		}
		
		return result;
	}
	
	/** 
	 * splits the template into text parts and variables.
	 * 
	 * @return an array containing each part in the right order
	 */
	private String[] splitTemplate() {
		
		String templateCopy = new String(mTemplate);
		Vector<String> parts = new Vector<String>();
		
		/* The state machine has two states: TEXT_STATE or VARIABLE_STATE
		 * if the last string found was "${" or "}".
		 * 
		 * At the beginning the string is assumed to be in TEXT_STATE. The
		 * template copy will be checked for the substrings "${" or "}"
		 * depending on the state. On each substring discovery, the following 
		 * operations will be done:
		 *     pos = templateCopy position of the substring
		 *     parts.add(templateCopy before pos)
		 *     templateCopy = templateCopy from pos
		 * And the loop will be executed until the templateCopy is empty or the
		 * substring is not found. In such a case the operation will depend on
		 * the current state:
		 *     + TEXT_STATE: templateCopy is added as the last part
		 *     + VARIABLE_STATE: adds a "}" before to add as the last part 
		 */
		final int TEXT_STATE     = 0;
		final int VARIABLE_STATE = 1;
		
		int state = TEXT_STATE;
		int pos = -1;
		
		do {
			
			// Find the position of the next substring
			if (state == TEXT_STATE) {
				pos = templateCopy.indexOf("${"); //$NON-NLS-1$
				if (pos != -1) {
					state = VARIABLE_STATE;
				}
			} else {
				// The "}" character has to be included with the variable part
				pos = templateCopy.indexOf("}"); //$NON-NLS-1$
				if (pos != -1){
					pos++;
					state = TEXT_STATE;
				}
			}
			
			if (pos > 0) {
				parts.add(templateCopy.substring(0, pos));
				templateCopy = templateCopy.substring(pos);
			}
			
		} while (pos != -1 && !templateCopy.equals("")); //$NON-NLS-1$
		
		// manages the last part
		if (state == VARIABLE_STATE && !templateCopy.equals("")) { //$NON-NLS-1$
			if (!templateCopy.endsWith("}")) { //$NON-NLS-1$
				templateCopy += "}"; //$NON-NLS-1$
			}
		}
		
		// Adds the last part
		if (!templateCopy.equals("")) { //$NON-NLS-1$
			parts.add(templateCopy);
		}
		
		// Convert the parts vector into a String array
		String[] strings = new String[parts.size()];
		for (int i=0; i<strings.length; i++) {
			strings[i] = (String)parts.get(i);
		}
		
		return strings;
	}
}
