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
package org.libreoffice.ide.eclipse.java.registration;


import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.java.utils.TemplatesHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class provides utility methods to generate the class and files needed
 * by the UNO services implementation registration.
 */
public abstract class RegistrationHelper {

    public static final String CLASS_FILENAME = "RegistrationHandler"; //$NON-NLS-1$

    /**
     * Creates all the necessary files for the java registration of UNO services
     * implementations to the <code>regcomp</code> tool.
     *
     * @param project the project where to create the registration handler
     */
    public static void generateFiles(IUnoidlProject project) {

        // Copy the RegistrationHandler.java.tpl file
        TemplatesHelper.copyTemplate(project, CLASS_FILENAME + TemplatesHelper.JAVA_EXT,
                                     RegistrationHelper.class, new String());

    }

    /**
     * Add a UNO service implementation to the list of the project ones.
     *
     * @param project the project where to add the implementation
     * @param implementation the fully qualified name of the implementation to add,
     *         eg: <code>org.libreoffice.comp.test.MyServiceImpl</code>
     * @param services the fully qualified names of UNO services to add,
     */
    public static void addImplementation(IUnoidlProject project, String implementation, String[] services) {
        Element components = null;
        Document document = project.getComponentsDocument();
        if (document != null) {
            components = project.getComponentsElement(document);
        }
        if (components != null) {
            Element component = getJavaComponent(project, document, components);
            if (component != null) {
                boolean modified = addImplementation(project, document, component, implementation, services);
                if (modified) {
                    project.writeComponentsFile(document);
                }
            }
        }
    }

    /**
     * remove a UNO service implementation from the list of the project ones.
     *
     * @param project the project where to remove the implementation
     * @param implementation the fully qualified name of the implementation to remove,
     *         eg: <code>org.libreoffice.comp.test.MyServiceImpl</code>
     */
    public static void removeImplementation(IUnoidlProject project, String implementation) {
        boolean modified = false;
        Document document = project.getComponentsDocument(false);
        if (document != null) {
            Element components = project.getComponentsElement(document);
            if (components != null) {
                Element component = getJavaComponent(project, document, components, false);
                if (component != null) {
                    modified = project.removeImplementation(components, component, implementation);
                }
            }
        }
        if (modified) {
            project.writeComponentsFile(document);
        }
    }

    /**
     * Computes the registration class name for the given Uno project.
     *
     * The registration class name is generally
     * <code>&lt;COMPANY.PREFIX&gt;.&lt;OUTPUTEXT&gt;.RegistrationHandler</code>.
     *
     * @param project the project for which to compute the class name
     * @return the registration class name
     */
    public static String getRegistrationClassName(IUnoidlProject project) {
        // Compute the name of the main implementation class
        String implPkg = project.getCompanyPrefix() + "." + project.getOutputExtension(); //$NON-NLS-1$
        return implPkg + "." + CLASS_FILENAME; //$NON-NLS-1$
    }

    private static boolean addImplementation(IUnoidlProject project, Document document, Element component,
                                             String implementation, String[] services) {
        boolean added = false;
        setComponentUri(component, project);
        Element element = project.getImplementationElement(component, implementation);
        if (element != null) {
            added = project.addServiceElements(document, element, services);
        } else {
            project.createImplementation(document, component, implementation, services);
            added = true;
        }
        return added;
    }

    private static Element getJavaComponent(IUnoidlProject project, Document document, Element components) {
        return getJavaComponent(project, document, components, true);
    }

    private static Element getJavaComponent(IUnoidlProject project, Document document,
                                            Element components, boolean create) {
        Element component = null;
        int i = 0;
        NodeList nodes = components.getElementsByTagName("component"); //$NON-NLS-1$
        while (i < nodes.getLength() && component == null) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (isJavaComponent(element)) {
                    component = element;
                }
            }
            i++;
        }
        if (component == null && create) {
            component = createJavaComponent(project, document, components);
        }
        return component;
    }

    private static Element createJavaComponent(IUnoidlProject project, Document document, Element components) {
        Element component = document.createElement("component"); //$NON-NLS-1$
        component.setAttribute("loader", "com.sun.star.loader.Java2"); //$NON-NLS-1$ //$NON-NLS-2$
        setComponentUri(component, project);
        components.appendChild(component);
        return component;
    }

    private static boolean isJavaComponent(Element component) {
        return component.hasAttribute("loader") && //$NON-NLS-1$
               component.getAttribute("loader").equals("com.sun.star.loader.Java2"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static void setComponentUri(Element component, IUnoidlProject project) {
        component.setAttribute("uri", project.getName() + ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
