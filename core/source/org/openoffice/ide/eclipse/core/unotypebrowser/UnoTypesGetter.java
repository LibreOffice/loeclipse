/*************************************************************************
 *
 * $RCSfile: UnoTypesGetter.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:55:53 $
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

import java.io.File;
import java.util.Vector;

import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;

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

	private String mRoot;
	private Vector mLocalRegistries;
    private Vector mExternalRegistries;
	private int mTypesMask = 1023;
	
	private XComponentContext mCtx;
    
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
            mTypesMask = 1023;
            String root = ""; //$NON-NLS-1$
            
			// Gets the optional arguments that defines the types to search.
			for (int i=0, length=args.length; i<length; i++){

				if (args[i].startsWith("-L")) { //$NON-NLS-1$
					// Local registry option
					String localregistry = args[i].substring(2);
					
					// First, test if the file exists...
					if (localregistry.startsWith("file:///")) { //$NON-NLS-1$
						String path = localregistry.replace("%20", " "); //$NON-NLS-1$ //$NON-NLS-2$
						path = path.substring("file:///".length()); //$NON-NLS-1$
						File regFile = new File(path);
						
						if (regFile.exists()) {
							localRegistries.add(args[i].substring(2));
						}
					}
					
				} else if (args[i].startsWith("-E")) { //$NON-NLS-1$
					// External registry option
					externalRegistries.add(args[i].substring(2));
					
				} else if (args[i].startsWith("-B")) { //$NON-NLS-1$
					// Root name option
					root = args[i].substring(2);
					
				} else if (args[i].startsWith("-T")) { //$NON-NLS-1$
					mTypesMask = Integer.parseInt(args[i].substring(2));
				}
			}
			
			if ((localRegistries.size() + externalRegistries.size()) > 0) {
                initialize(localRegistries, externalRegistries, root, mTypesMask);

                Vector unoTypes = queryTypes();
                printTypes(unoTypes);
                
                unoTypes.clear();
			}
			
			localRegistries.clear();
			externalRegistries.clear();
		}
    }
        
    /**
     * Default constructor used by the URE
     */
    public UnoTypesGetter(XComponentContext xCtx) {
        this.mCtx = xCtx;
    }
    
    /**
     * Method called to initialize the types getter with the correct parameters
     */
    public void initialize (Vector aLocalRegistries, Vector aExternalRegistries, 
                    String aRoot, int aTypesMask) {
		
	    mLocalRegistries = aLocalRegistries;
        mExternalRegistries = aExternalRegistries;
        
		// Sets the root to a quite correct value
		if (aRoot.equals("/")) { //$NON-NLS-1$
			mRoot = ""; //$NON-NLS-1$
		} else {
			mRoot = aRoot;
		}
		
		// Sets the typesMask
		if (aTypesMask >= 0 && 1024 > aTypesMask) {			
			mTypesMask = aTypesMask;
		}
	}

    /**
     * Query the types and return them in a vector of {@link InternalUnoType}.
     */
	protected Vector queryTypes() throws Exception {
            
        Vector results = new Vector();
		
        for (int i=0, length=mLocalRegistries.size(); i<length; i++)	{
            String registryPath = (String)mLocalRegistries.get(i);
            results.addAll(getTypesFromRegistry(registryPath, true));
        }

        for (int i=0, length=mExternalRegistries.size(); i<length; i++) {
            String registryPath = (String)mExternalRegistries.get(i);
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
                
        if (null != registryPath && registryPath.startsWith("file:///")) { //$NON-NLS-1$
				
            // Get the UNO Type enumeration access    
			XMultiComponentFactory xMCF = mCtx.getServiceManager();
			XSimpleRegistry xReg = (XSimpleRegistry)UnoRuntime.queryInterface(
					XSimpleRegistry.class,
                    xMCF.createInstanceWithContext(
                        "com.sun.star.registry.SimpleRegistry", mCtx)); //$NON-NLS-1$
				
			xReg.open(registryPath, true, false);
			
			Object[] seqArgs = { xReg };
				
			Object oTDMgr = xMCF.createInstanceWithArgumentsAndContext(
					"com.sun.star.reflection.TypeDescriptionProvider", //$NON-NLS-1$
					seqArgs, mCtx);
				
			// Set the local Type Description Manager
			XTypeDescriptionEnumerationAccess localTDMgr = 
                (XTypeDescriptionEnumerationAccess)UnoRuntime.queryInterface(
					XTypeDescriptionEnumerationAccess.class,
					oTDMgr);

            // Query the types from the enumeration access
			XTypeDescriptionEnumeration xLocalTypeEnum = localTDMgr.
					createTypeDescriptionEnumeration(
							mRoot,
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
			
		if (isOfType(mTypesMask, IUnoFactoryConstants.MODULE)) {
			typeClasses.add(TypeClass.MODULE);
		} else if (isOfType(mTypesMask, IUnoFactoryConstants.INTERFACE)) {
			typeClasses.add(TypeClass.INTERFACE);
		} else if (isOfType(mTypesMask, IUnoFactoryConstants.SERVICE)) {
			typeClasses.add(TypeClass.SERVICE);
		} else if (isOfType(mTypesMask, IUnoFactoryConstants.STRUCT)) {
			typeClasses.add(TypeClass.STRUCT);
		} else if (isOfType(mTypesMask, IUnoFactoryConstants.ENUM)) {
			typeClasses.add(TypeClass.ENUM);
		} else if (isOfType(mTypesMask, IUnoFactoryConstants.EXCEPTION)) {
			typeClasses.add(TypeClass.EXCEPTION);
		} else if (isOfType(mTypesMask, IUnoFactoryConstants.TYPEDEF)) {
			typeClasses.add(TypeClass.TYPEDEF);
		} else if (isOfType(mTypesMask, IUnoFactoryConstants.CONSTANT)) {
			typeClasses.add(TypeClass.CONSTANT);
		} else if (isOfType(mTypesMask, IUnoFactoryConstants.CONSTANTS)) {
			typeClasses.add(TypeClass.CONSTANTS);
		} else if (isOfType(mTypesMask, IUnoFactoryConstants.SINGLETON)) {
			typeClasses.add(TypeClass.SINGLETON);
		}
		
		TypeClass[] types = new TypeClass[typeClasses.size()];
		for (int i=0, length=typeClasses.size(); i<length; i++){
			types[i] = (TypeClass)typeClasses.get(i);
		}
		typeClasses.clear();

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
				type = IUnoFactoryConstants.MODULE;
				break;
					
			case TypeClass.INTERFACE_value:
				type = IUnoFactoryConstants.INTERFACE;
				break;
					
			case TypeClass.SERVICE_value:
				type = IUnoFactoryConstants.SERVICE;
				break;
						
			case TypeClass.STRUCT_value:
				type = IUnoFactoryConstants.STRUCT;
				break;
						
			case TypeClass.ENUM_value:
				type = IUnoFactoryConstants.ENUM;
				break;
						
			case TypeClass.EXCEPTION_value:
				type = IUnoFactoryConstants.EXCEPTION;
				break;
						
			case TypeClass.TYPEDEF_value:
				type = IUnoFactoryConstants.TYPEDEF;
				break;
						
			case TypeClass.CONSTANT_value:
				type = IUnoFactoryConstants.CONSTANT;
				break;
						
			case TypeClass.CONSTANTS_value:
				type = IUnoFactoryConstants.CONSTANTS;
				break;
				
			case TypeClass.SINGLETON_value:
				type = IUnoFactoryConstants.SINGLETON;
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
		"org.openoffice.ide.eclipse.unotypebrowser.UnoTypesGetter"; //$NON-NLS-1$
	
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
