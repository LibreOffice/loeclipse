//
// JODConverter - Java OpenDocument Converter
// OpenOffice.org Configuration checker
// Copyright (C) 2007 - Laurent Godard <lgodard@nuxeo.com>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// http://www.gnu.org/copyleft/lesser.html
//
// Contributor:
// Mirko Nasato <mirko@artofsolving.com>
//

package com.artofsolving.jodconverter.openoffice.connection;

import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XInterface;

/**
 * Utility class to access OpenOffice.org configuration properties at runtime
 */
public class OpenOfficeConfiguration {

    public static final String NODE_L10N = "org.openoffice.Setup/L10N";
    public static final String NODE_PRODUCT = "org.openoffice.Setup/Product";
	
    private OpenOfficeConnection connection;

	public OpenOfficeConfiguration(OpenOfficeConnection connection){
		this.connection = connection;
	}

    public String getOpenOfficeProperty(String nodePath, String node){
    	if (!nodePath.startsWith("/")){
    		nodePath = "/" + nodePath;
    	}
    	String property = "";
    	// create the provider and remember it as a XMultiServiceFactory
    	try {
        	final String sProviderService = "com.sun.star.configuration.ConfigurationProvider";
            Object configProvider = connection.getRemoteServiceManager().createInstanceWithContext(
            		sProviderService, connection.getComponentContext());
            XMultiServiceFactory  xConfigProvider = (XMultiServiceFactory) UnoRuntime.queryInterface(
                    com.sun.star.lang.XMultiServiceFactory.class, configProvider);

            // The service name: Need only read access:
            final String sReadOnlyView = "com.sun.star.configuration.ConfigurationAccess";
            // creation arguments: nodepath
            PropertyValue aPathArgument = new PropertyValue();
            aPathArgument.Name = "nodepath";
            aPathArgument.Value = nodePath;
            Object[] aArguments = new Object[1];
            aArguments[0] = aPathArgument;

            // create the view
            XInterface xElement = (XInterface) xConfigProvider.createInstanceWithArguments(sReadOnlyView, aArguments);
            XNameAccess xChildAccess =
                (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, xElement);

            // get the value
            property = (String) xChildAccess.getByName(node);
    	} catch (Exception exception){
    		throw new OpenOfficeException("Could not retrieve property", exception);
    	}
    	return property;
    }

    public String getOpenOfficeVersion(){
        try {
            // OOo >= 2.2 returns major.minor.micro
            return getOpenOfficeProperty(NODE_PRODUCT, "ooSetupVersionAboutBox");
        } catch (OpenOfficeException noSuchElementException) {
            // OOo < 2.2 only returns major.minor
            return getOpenOfficeProperty(NODE_PRODUCT, "ooSetupVersion");
        }
    }

    public String getOpenOfficeLocale(){
    	return getOpenOfficeProperty(NODE_L10N, "ooLocale");
    }

}
