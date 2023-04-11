/*************************************************************************
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
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
package org.libreoffice.ide.eclipse.core.model.language;

import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Helper class for implementation languages handling.
 */
public class LanguagesHelper {

    /**
     * @return all the available uno-idl implementation language names installed on the platform.
     */
    public static String[] getAvailableLanguageNames() {

        String[] result = null;

        IConfigurationElement[] languages = getLanguagesDefs();
        result = new String[languages.length];

        for (int i = 0; i < languages.length; i++) {
            result[i] = languages[i].getAttribute("name"); //$NON-NLS-1$
        }

        return result;
    }

    /**
     * <p>
     * Returns the language corresponding to a language name. The result may be <code>null</code> if:
     * <ul>
     * <li>There is no such language name</li>
     * <li>The corresponding class cannot be found</li>
     * <li>The corresponding class doesn't implement ILanguage</li>
     * </ul>
     * </p>
     *
     * @param pName
     *            the language name to find
     *
     * @return the language object if found, <code>null</code> otherwise.
     */
    public static AbstractLanguage getLanguageFromName(String pName) {

        AbstractLanguage language = null;

        IConfigurationElement[] languages = getLanguagesDefs();
        int i = 0;

        while (language == null && i < languages.length) {
            IConfigurationElement languagei = languages[i];
            if (languagei.getAttribute("name").equals(pName)) { //$NON-NLS-1$
                try {
                    Object o = languagei.createExecutableExtension("class"); //$NON-NLS-1$

                    if (o instanceof AbstractLanguage) {
                        language = (AbstractLanguage) o;
                        language.setConfigurationElement(languagei);
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
     * Convenience method returning the language definitions from the plugins extensions points descriptions.
     *
     * @return the array of the configuration element for the languages.
     */
    private static IConfigurationElement[] getLanguagesDefs() {
        IConfigurationElement[] result = null;

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("org.libreoffice.ide.eclipse.core.language"); //$NON-NLS-1$
        if (point != null) {

            IExtension[] extensions = point.getExtensions();
            Vector<IConfigurationElement> languages = new Vector<IConfigurationElement>();

            for (int i = 0; i < extensions.length; i++) {

                IConfigurationElement[] elements = extensions[i].getConfigurationElements();

                for (int j = 0; j < elements.length; j++) {
                    IConfigurationElement elementj = elements[j];
                    if (elementj.getName().equals("language")) { //$NON-NLS-1$
                        languages.add(elementj);
                    }
                }
            }

            result = new IConfigurationElement[languages.size()];
            for (int i = 0, length = languages.size(); i < length; i++) {
                result[i] = languages.get(i);
            }

            // clean the vector
            languages.clear();
        }

        return result;
    }
}
