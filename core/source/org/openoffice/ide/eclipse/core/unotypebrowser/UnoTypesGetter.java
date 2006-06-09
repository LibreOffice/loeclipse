/*************************************************************************
 *
 * $RCSfile: UnoTypesGetter.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/06/09 06:14:02 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.core.unotypebrowser;

import java.util.Vector;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.comp.loader.FactoryHelper;
import com.sun.star.lang.XMain;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.reflection.TypeDescriptionSearchDepth;
import com.sun.star.reflection.XTypeDescription;
import com.sun.star.reflection.XTypeDescriptionEnumeration;
import com.sun.star.reflection.XTypeDescriptionEnumerationAccess;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.registry.XSimpleRegistry;
import com.sun.star.uno.TypeClass;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Class bootstrapping OpenOffice.org to query its types. This class can't
 * be used directly and should be launched as an external program by the plugin.
 * 
 * @author cbosdonnat
 */
public class UnoTypesGetter implements XMain {

	private String root;
	private Vector localRegistries;
    private Vector externalRegistries;
	private int typesMask = 1023;
	
	private XComponentContext xCtx;
    
    /**
     * Hook launched when this class is used as URE starter component
     * 
     * @param args Arguments given to fetch the types
     * @see #execute(String[])
     */
    public int run(String[] args){
        
        int error = 0;
        
        try {
            execute(args);
        } catch (Exception e) {
            error = 1;
        }

        return error;
    }
    
    /**
     * Hook for used when launched as a normal Java application.
     *
     * @see #execute(String[])
     */
	public static void main(String[] args) {
	
        int error = 0;
        
        try {
            XComponentContext xCtx = bootstrap();
            UnoTypesGetter getter = new UnoTypesGetter(xCtx);
            
            getter.execute(args);
        } catch (Exception e) {
            error = 1;
        }
        
		System.exit(error);
	}

    /**
     * Bootstraps OOo before getting its types.
     *
     * @throws Exception when something wrong happened
     */
    private static XComponentContext bootstrap() throws Exception {

        return Bootstrap.bootstrap();
    }
    
	/**
     * Common function used either by URE run and Java main callbacks
     * 
	 * @param args arguments given to fetch the types
	 *    <ul>
     *      <li>-Lfile:///path/to/a/local/registry</li>
     *      <li>-Efile:///path/to/an/external/registry</li>
	 *      <li>-B<code>root</code></li>
	 *      <li>-T<code>mask</code> where <code>mask</code> is the integer type mask
     *          to apply.</li>
	 *    </ul>
     *    <p>If the types' root isn't specified, then all the tree is parsed.</p>
     *
     * @throws Exception is thrown when something happened during types querying.
	 */
    public void execute(String[] args) throws Exception {
		if (1 < args.length) {

            Vector localRegistries = new Vector();
            Vector externalRegistries = new Vector();
            typesMask = 1023;
            String root = "";
            
			// Gets the optional arguments that defines the types to search.
			for (int i=0, length=args.length; i<length; i++){

                
				if (args[i].startsWith("-L")) {
					// Local registry option
					localRegistries.add(args[i].substring(2));
					
				} else if (args[i].startsWith("-E")) {
					// External registry option
					externalRegistries.add(args[i].substring(2));
					
				} else if (args[i].startsWith("-B")) {
					// Root name option
					root = args[i].substring(2);
					
				} else if (args[i].startsWith("-T")) {
					typesMask = Integer.parseInt(args[i].substring(2));
				}
			}
			
			if ((localRegistries.size() + externalRegistries.size()) > 0) {
                initialize(localRegistries, externalRegistries, root, typesMask);

                Vector unoTypes = queryTypes();
                printTypes(unoTypes);
			}
		}
    }
        
    /**
     * Default constructor used by the URE
     */
    public UnoTypesGetter(XComponentContext xCtx) {
        this.xCtx = xCtx;
    }
    
    /**
     * Method called to initialize the types getter with the correct parameters
     */
    public void initialize (Vector aLocalRegistries, Vector aExternalRegistries, 
                    String aRoot, int aTypesMask) {
		
	    localRegistries = aLocalRegistries;
        externalRegistries = aExternalRegistries;
        
		// Sets the root to a quite correct value
		if (aRoot.equals("/")) {
			root = "";
		} else {
			root = aRoot;
		}
		
		// Sets the typesMask
		if (aTypesMask >= 0 && 1024 > aTypesMask) {			
			typesMask = aTypesMask;
		}
	}

    /**
     * Query the types and return them in a vector of {@link InternalUnoType}.
     */
	protected Vector queryTypes() throws Exception {
            
        Vector results = new Vector();
		
        for (int i=0, length=localRegistries.size(); i<length; i++)	{
            String registryPath = (String)localRegistries.get(i);
            results.addAll(getTypesFromRegistry(registryPath, true));
        }

        for (int i=0, length=externalRegistries.size(); i<length; i++) {
            String registryPath = (String)externalRegistries.get(i);
            results.addAll(getTypesFromRegistry(registryPath, true));
        }

        return results;
	}

    /**
     * Get all the types from a registry and return an {@link InternalUnoType}
     * vector.
     */
    private Vector getTypesFromRegistry(String registryPath, boolean isLocal) 
            throws Exception {
	    
        Vector result = new Vector();
                
        if (null != registryPath && registryPath.startsWith("file:///")) {
				
            // Get the UNO Type enumeration access    
			XMultiComponentFactory xMCF = xCtx.getServiceManager();
			XSimpleRegistry xReg = (XSimpleRegistry)UnoRuntime.queryInterface(
					XSimpleRegistry.class,
                    xMCF.createInstanceWithContext(
                        "com.sun.star.registry.SimpleRegistry", xCtx));
				
			xReg.open(registryPath, true, false);
			
			Object[] seqArgs = { xReg };
				
			Object oTDMgr = xMCF.createInstanceWithArgumentsAndContext(
					"com.sun.star.reflection.TypeDescriptionProvider",
					seqArgs, xCtx);
				
			// Set the local Type Description Manager
			XTypeDescriptionEnumerationAccess localTDMgr = 
                (XTypeDescriptionEnumerationAccess)UnoRuntime.queryInterface(
					XTypeDescriptionEnumerationAccess.class,
					oTDMgr);

            // Query the types from the enumeration access
			XTypeDescriptionEnumeration xLocalTypeEnum = localTDMgr.
					createTypeDescriptionEnumeration(
							root,
							convertToTypeClasses(),
							TypeDescriptionSearchDepth.INFINITE);

            // Convert the enumeration into a Vector
            while (xLocalTypeEnum.hasMoreElements()) {

                XTypeDescription xType = xLocalTypeEnum.nextTypeDescription();
                result.add(createInternalType(xType, isLocal));
            }
		}
       
        return result;
    }
    
    /**
     * Convenient method to check if the mask includes a type
     *
     * @param mask the mask to check
     * @param type the type to find in the mask
     *
     * @return <code>true</code> if the mask contains the type, 
     *      <code>false</code> otherwise.
     */
    private boolean isOfType(int mask, int type) {

        return ((mask & type) == type);
    }
   
    /**
     *  Convenient method to convert the types mask into an array of UNO 
     *  TypeClasses.
     *
     *  @return the corresponding TypeClass array
     */
    private TypeClass[] convertToTypeClasses() {
			
        // Creates the TypeClass[] array from the given types names
		Vector typeClasses = new Vector();
			
		if (isOfType(typesMask, UnoTypeProvider.MODULE)) {
			typeClasses.add(TypeClass.MODULE);
		} else if (isOfType(typesMask, UnoTypeProvider.INTERFACE)) {
			typeClasses.add(TypeClass.INTERFACE);
		} else if (isOfType(typesMask, UnoTypeProvider.SERVICE)) {
			typeClasses.add(TypeClass.SERVICE);
		} else if (isOfType(typesMask, UnoTypeProvider.STRUCT)) {
			typeClasses.add(TypeClass.STRUCT);
		} else if (isOfType(typesMask, UnoTypeProvider.ENUM)) {
			typeClasses.add(TypeClass.ENUM);
		} else if (isOfType(typesMask, UnoTypeProvider.EXCEPTION)) {
			typeClasses.add(TypeClass.EXCEPTION);
		} else if (isOfType(typesMask, UnoTypeProvider.TYPEDEF)) {
			typeClasses.add(TypeClass.TYPEDEF);
		} else if (isOfType(typesMask, UnoTypeProvider.CONSTANT)) {
			typeClasses.add(TypeClass.CONSTANT);
		} else if (isOfType(typesMask, UnoTypeProvider.CONSTANTS)) {
			typeClasses.add(TypeClass.CONSTANTS);
		} else if (isOfType(typesMask, UnoTypeProvider.SINGLETON)) {
			typeClasses.add(TypeClass.SINGLETON);
		}
		
		TypeClass[] types = new TypeClass[typeClasses.size()];
		for (int i=0, length=typeClasses.size(); i<length; i++){
			types[i] = (TypeClass)typeClasses.get(i);
		}

        return types;
    }
   
    /**
     * Creates an {@link InternalUnoType} from the Uno TypeDescription
     * and a flag to know wether the type is local or external.
     *
     * <p>Note: this method isn't very useful yet, but it prepares future
     * evolutions currently impossible.</p>
     */
    private InternalUnoType createInternalType(XTypeDescription xType, 
            boolean isLocal) {
        
		// convert the type into an integer
		TypeClass typeClass = xType.getTypeClass();
		int type = 0;
		switch (typeClass.getValue()) {
			
			case TypeClass.MODULE_value:
				type = UnoTypeProvider.MODULE;
				break;
					
			case TypeClass.INTERFACE_value:
				type = UnoTypeProvider.INTERFACE;
				break;
					
			case TypeClass.SERVICE_value:
				type = UnoTypeProvider.SERVICE;
				break;
						
			case TypeClass.STRUCT_value:
				type = UnoTypeProvider.STRUCT;
				break;
						
			case TypeClass.ENUM_value:
				type = UnoTypeProvider.ENUM;
				break;
						
			case TypeClass.EXCEPTION_value:
				type = UnoTypeProvider.EXCEPTION;
				break;
						
			case TypeClass.TYPEDEF_value:
				type = UnoTypeProvider.TYPEDEF;
				break;
						
			case TypeClass.CONSTANT_value:
				type = UnoTypeProvider.CONSTANT;
				break;
						
			case TypeClass.CONSTANTS_value:
				type = UnoTypeProvider.CONSTANTS;
				break;
				
			case TypeClass.SINGLETON_value:
				type = UnoTypeProvider.SINGLETON;
				break;
		}
			
        return new InternalUnoType(xType.getName(), type, isLocal);
    }
    
    /**
     * Prints the vector to the standard output: thus the types can be given to
     * another process using simple parsing.
     *
     * @param unoTypes vector of InternalUnoTypes to print
     */
	private void printTypes(Vector unoTypes) {
	    for (int i=0, length=unoTypes.size(); i<length; i++) {
            InternalUnoType type = (InternalUnoType)unoTypes.get(i);

            System.out.println(type.toString());
        }
	}
	
	private static String __serviceName = 
		"org.openoffice.ide.eclipse.unotypebrowser.UnoTypesGetter";
	
	public static XSingleServiceFactory __getServiceFactory(
			String implName, XMultiServiceFactory multiFactory, 
			XRegistryKey regKey){
		
		XSingleServiceFactory xSingleServiceFactory = null;
		
		if (implName.equals(UnoTypesGetter.class.getName())) {
			xSingleServiceFactory = FactoryHelper.getServiceFactory(
					UnoTypesGetter.class, UnoTypesGetter.__serviceName, 
					multiFactory, regKey);
		}
		
		return xSingleServiceFactory;
	}
	
	public static boolean __writeRegistryServiceInfo(XRegistryKey regKey) {
		boolean b = FactoryHelper.writeRegistryServiceInfo(
				UnoTypesGetter.class.getName(), 
				UnoTypesGetter.__serviceName, regKey);
		
		return b;
	}
}
