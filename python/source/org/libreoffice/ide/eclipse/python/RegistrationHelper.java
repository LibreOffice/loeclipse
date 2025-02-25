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
package org.libreoffice.ide.eclipse.python;

import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class provides utility methods to generate the .components files needed
 * by the UNO services implementation registration.
 */
public abstract class RegistrationHelper {

    /**
     * Add a UNO service implementation to the list of the project ones.
     *
     * @param project the project where to add the implementation
     * @param uri the project relative file name of the implementation to add,
     * @param service the fully qualified name of the UNO service to add,
     */
    public static void addImplementation(IUnoidlProject project, String uri, String service) {
        Element components = null;
        Document document = project.getComponentsDocument();
        if (document != null) {
            components = project.getComponentsElement(document);
        }
        if (components != null) {
            Element component = getPythonComponent(project, document, components, uri);
            if (component != null) {
                if (addImplementation(project, document, component, uri, service)) {
                    project.writeComponentsFile(document);
                }
            }
        }
    }

    /**
     * remove a UNO service implementation from the list of the project ones.
     *
     * @param project the project where to remove the implementation
     * @param uri the fully qualified file of the implementation to remove,
     *         eg: <code>org.libreoffice.comp.test.MyServiceImpl</code>
     */
    public static void removeImplementation(IUnoidlProject project, String uri) {
        boolean removed = false;
        Document document = project.getComponentsDocument(false);
        if (document != null) {
            Element components = project.getComponentsElement(document);
            if (components != null) {
                Element component = getPythonComponent(project, document, components, uri, false);
                if (component != null) {
                    components.removeChild(component);
                    removed = true;
                }
            }
        }
        if (removed) {
            project.writeComponentsFile(document);
        }
    }

    private static boolean addImplementation(IUnoidlProject project, Document document, Element component,
                                             String uri, String service) {
        boolean added = false;
        setComponentUri(component, uri);
        Element element = project.getImplementationElement(component, service);
        if (element != null) {
            added = project.addServiceElement(document, element, service);
        } else {
            element = project.createImplementation(document, component, service, service);
            added = true;
        }
        // If an implementation has been added, we must remove any existing ones.
        if (added) {
            project.removeImplementationElements(component, element);
        }
        return added;
    }

    private static Element getPythonComponent(IUnoidlProject project, Document document,
                                              Element components, String uri) {
        return getPythonComponent(project, document, components, uri, true);
    }

    private static Element getPythonComponent(IUnoidlProject project, Document document,
                                              Element components, String uri, boolean create) {
        Element component = null;
        NodeList nodes = components.getElementsByTagName("component"); //$NON-NLS-1$
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (isPythonComponent(element, uri)) {
                    component = element;
                    break;
                }
            }
        }
        if (component == null && create) {
            component = createPythonComponent(project, document, components, uri);
        }
        return component;
    }

    private static Element createPythonComponent(IUnoidlProject project, Document document,
                                                 Element components, String uri) {
        Element component = document.createElement("component"); //$NON-NLS-1$
        component.setAttribute("loader", "com.sun.star.loader.Python"); //$NON-NLS-1$ //$NON-NLS-2$
        setComponentUri(component, uri);
        components.appendChild(component);
        return component;
    }

    private static boolean isPythonComponent(Element component, String uri) {
        return component.hasAttribute("loader") && //$NON-NLS-1$
               component.hasAttribute("uri") && //$NON-NLS-1$
               component.getAttribute("loader").equals("com.sun.star.loader.Python") && //$NON-NLS-1$ //$NON-NLS-2$
               component.getAttribute("uri").equals(uri); //$NON-NLS-1$
    }

    private static void setComponentUri(Element component, String uri) {
        component.setAttribute("uri", uri); //$NON-NLS-1$
    }

}
