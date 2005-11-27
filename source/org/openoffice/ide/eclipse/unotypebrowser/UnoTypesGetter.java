/*************************************************************************
 *
 * $RCSfile: UnoTypesGetter.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:20 $
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
package org.openoffice.ide.eclipse.unotypebrowser;

import java.util.Hashtable;
import java.util.Vector;

import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.reflection.InvalidTypeNameException;
import com.sun.star.reflection.NoSuchTypeNameException;
import com.sun.star.reflection.TypeDescriptionSearchDepth;
import com.sun.star.reflection.XTypeDescription;
import com.sun.star.reflection.XTypeDescriptionEnumeration;
import com.sun.star.reflection.XTypeDescriptionEnumerationAccess;
import com.sun.star.registry.XSimpleRegistry;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.TypeClass;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class UnoTypesGetter {
	
	public final static String S_MODULE = "module";
	
	public final static String S_INTERFACE = "interface";
	
	public final static String S_SERVICE = "service";
	
	public final static String S_STRUCT = "struct";
	
	public final static String S_ENUM = "enum";
	
	public final static String S_EXCEPTION = "exception";
	
	public final static String S_TYPEDEF = "typedef";
	
	public final static String S_CONSTANT = "constant";
	
	public final static String S_CONSTANTS = "constants";
		
	public final static String S_SINGLETON = "singleton";
	
	
	public final static String LOCAL_TAG = "L";
	
	public final static String UNO_TAG = "U";

	private String sOfficePath;
	private String root;
	private String localRegistry;
	private Vector typesNames = new Vector();
	
	private String unoini;
	private XComponentContext xCtx;
    
	private XTypeDescriptionEnumerationAccess xTDMgr;
    private XTypeDescriptionEnumerationAccess localTDMgr;
    
    
	public UnoTypesGetter(String aOfficePath, String aRoot, 
					String aLocalRegistry, Vector aTypesNames) {
		
		sOfficePath = aOfficePath;
		unoini = sOfficePath + "/program/uno";
		localRegistry = aLocalRegistry;
		
		String platform = System.getProperty("os.name").toLowerCase();
		if (platform.startsWith("windows")){
			unoini = unoini + ".ini";
		} else {
			unoini = unoini + "rc";
		}
		
		
		// Sets the root to a quite correct value
		if (aRoot.equals("/")){
			root = "";
		} else {
			root = aRoot;
		}
		
		// Sets the typesNames
		if (null != aTypesNames){			
			typesNames = aTypesNames;
		}
	}

	/**
	 * @param args arguments given to fetch the types
	 *    <ul>
	 *      <li>-Ffile:///path to openoffice home</li>
	 *      <li>-Btypes root</li>
	 *      <li>-Lfile:///path to the local rdb</li>
	 *      <li>types to find</li>
	 *    </ul>
	 */
	public static void main(String[] args) {
		
		if (1 < args.length) {

			String officePath = null;
			String root = null;
			String localRegistry = null;
			
			// Gets the optional arguments that defines the types to search.
			Vector typesNames = new Vector();
			for (int i=0, length=args.length; i<length; i++){
				if (args[i].startsWith("-F")) {
					// OOo home path option
					officePath = args[i].substring(2);
					
				} else if (args[i].startsWith("-L")) {
					// Local rdb file option
					localRegistry = args[i].substring(2);
					
				} else if (args[i].startsWith("-B")) {
					// Root name option
					root = args[i].substring(2);
					
				} else {
					typesNames.add(args[i]);
				}
			}
			
			if (null != officePath) {
				UnoTypesGetter test = new UnoTypesGetter(officePath, root, 
													localRegistry, typesNames);
				test.initTypeDescriptionManager();
			
				test.getTypes();
			}
		}
		System.exit(0);
	}
	
	protected void initTypeDescriptionManager() {
        try {
        	Hashtable bootParams = new Hashtable();
        	bootParams.put("SYSBINDIR", sOfficePath+"/program");

        	xCtx = Bootstrap.defaultBootstrap_InitialComponentContext(unoini, bootParams);
        	
            Object o = xCtx.getValueByName(
            		"/singletons/com.sun.star.reflection.theTypeDescriptionManager");
            xTDMgr = (XTypeDescriptionEnumerationAccess) AnyConverter.toObject(
            		XTypeDescriptionEnumerationAccess.class, o);
            
            if (null != localRegistry) {
            	
	            XMultiComponentFactory xMCF = xCtx.getServiceManager();
	            XSimpleRegistry xReg = (XSimpleRegistry)UnoRuntime.queryInterface(
	            		XSimpleRegistry.class,
	            		xMCF.createInstanceWithContext("com.sun.star.registry.SimpleRegistry",
	            				xCtx));
	            
	            xReg.open(localRegistry, true, false);
	            
	            Object[] seqArgs = { xReg };
	            
	            Object oTDMgr = xMCF.createInstanceWithArgumentsAndContext(
              		  "com.sun.star.reflection.TypeDescriptionProvider",
              		   seqArgs, xCtx);
	            
	            // Set the local Type Description Manager
	            localTDMgr = (XTypeDescriptionEnumerationAccess)UnoRuntime.
	            		queryInterface(
	            				XTypeDescriptionEnumerationAccess.class,
	            				oTDMgr);
            }
            
        } catch ( java.lang.Exception e) {
            System.err.println(e.getMessage());
        }
    }
	
	protected void getTypes() {
		try {
			// Creates the TypeClass[] array from the given types names
			Vector typeClasses = new Vector();
			
			for (int i=0, length=typesNames.size(); i<length; i++){
				String type = ((String)typesNames.get(i)).toLowerCase();
				
				if (type.equals(S_MODULE)) {
					typeClasses.add(TypeClass.MODULE);
				} else if (type.equals(S_INTERFACE)) {
					typeClasses.add(TypeClass.INTERFACE);
				} else if (type.equals(S_SERVICE)) {
					typeClasses.add(TypeClass.SERVICE);
				} else if (type.equals(S_STRUCT)) {
					typeClasses.add(TypeClass.STRUCT);
				} else if (type.equals(S_ENUM)) {
					typeClasses.add(TypeClass.ENUM);
				} else if (type.equals(S_EXCEPTION)) {
					typeClasses.add(TypeClass.EXCEPTION);
				} else if (type.equals(S_TYPEDEF)) {
					typeClasses.add(TypeClass.TYPEDEF);
				} else if (type.equals(S_CONSTANT)) {
					typeClasses.add(TypeClass.CONSTANT);
				} else if (type.equals(S_CONSTANTS)) {
					typeClasses.add(TypeClass.CONSTANTS);
				} else if (type.equals(S_SINGLETON)) {
					typeClasses.add(TypeClass.SINGLETON);
				}
			}
			
			TypeClass[] types = new TypeClass[typeClasses.size()];
			for (int i=0, length=typeClasses.size(); i<length; i++){
				types[i] = (TypeClass)typeClasses.get(i);
			}
			
			XTypeDescriptionEnumeration xTypeEnum = xTDMgr.
					createTypeDescriptionEnumeration(
							root, 
							types, 
							TypeDescriptionSearchDepth.INFINITE);
			
			printTypes(xTypeEnum, false);
			
			XTypeDescriptionEnumeration xLocalTypeEnum = localTDMgr.
					createTypeDescriptionEnumeration(
							root,
							types,
							TypeDescriptionSearchDepth.INFINITE);
			
			printTypes(xLocalTypeEnum, true);
			
			
		} catch (NoSuchTypeNameException e) {
			System.err.println("Invalid root: " + root);
		} catch (InvalidTypeNameException e) {
			//Should never happen
			if (null != System.getProperty("DEBUG")){
				e.printStackTrace();
			}
		}
		
		System.err.println("OK");
	}
	
	private void printTypes(XTypeDescriptionEnumeration xTypeEnum, boolean local) {
		
		try {
			while (xTypeEnum.hasMoreElements()){
				XTypeDescription typeDescr = xTypeEnum.nextTypeDescription();
				
				// Computes the line like : <type name> <type scoped name>
				TypeClass typeClass = typeDescr.getTypeClass();
				String type = "";
				switch (typeClass.getValue()) {
				
					case TypeClass.MODULE_value:
						type = S_MODULE;
						break;
					
					case TypeClass.INTERFACE_value:
						type = S_INTERFACE;
						break;
					
					case TypeClass.SERVICE_value:
						type = S_SERVICE;
						break;
						
					case TypeClass.STRUCT_value:
						type = S_STRUCT;
						break;
						
					case TypeClass.ENUM_value:
						type = S_ENUM;
						break;
						
					case TypeClass.EXCEPTION_value:
						type = S_EXCEPTION;
						break;
						
					case TypeClass.TYPEDEF_value:
						type = S_TYPEDEF;
						break;
						
					case TypeClass.CONSTANT_value:
						type = S_CONSTANT;
						break;
						
					case TypeClass.CONSTANTS_value:
						type = S_CONSTANTS;
						break;
						
					case TypeClass.SINGLETON_value:
						type = S_SINGLETON;
						break;
				}
				
				if (!type.equals("")){
					type = type + " ";
					
					type = local ? LOCAL_TAG + " " + type: UNO_TAG + " " + type;
				}
				
				System.out.println(type + typeDescr.getName());
			}
		} catch (NoSuchElementException e) {
			// Should never happen
			if (null != System.getProperty("DEBUG")){
				e.printStackTrace();
			}
		}
	}
}
