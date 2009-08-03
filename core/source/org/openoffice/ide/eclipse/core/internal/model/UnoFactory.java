/*************************************************************************
 *
 * $RCSfile: UnoFactory.java,v $
 *
 * $Revision: 1.12 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:48 $
 *
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
package org.openoffice.ide.eclipse.core.internal.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.CompositeFactory;
import org.openoffice.ide.eclipse.core.model.IUnoComposite;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.description.DescriptionModel;
import org.openoffice.ide.eclipse.core.model.language.ILanguage;
import org.openoffice.ide.eclipse.core.model.language.IProjectHandler;

/**
 * This class is a factory creating UNO projects and types from data sets
 * describing the object to get.
 * 
 * @author cedricbosdo
 */
public final class UnoFactory {
    
    /**
     * Creates a UNO project from scratch. It creates the directories,
     * initial types and generates the basic implementation. 
     * 
     * @param pData the project description
     * @param pMonitor the monitor to report the progress of the project creation
     * 
     * @return the created UNO project
     * 
     * @throws Exception if anything wrong happens during the project creation
     */
    public static IUnoidlProject createProject(UnoFactoryData pData, IProgressMonitor pMonitor) throws Exception {
        
        IUnoidlProject prj = UnoidlProjectHelper.createStructure(pData, pMonitor);
        
        // Creates an empty package.properties file
        prj.getFile("package.properties").getLocation().toFile().createNewFile(); //$NON-NLS-1$
        
        // Creates an empty description.xml file
        File file = prj.getFile("description.xml").getLocation().toFile(); //$NON-NLS-1$
        DescriptionModel descrModel = new DescriptionModel( );
        descrModel.mDisplayNames.put( Locale.ENGLISH, prj.getName() );
        descrModel.mId = prj.getCompanyPrefix().toLowerCase() + "." + //$NON-NLS-1$
            prj.getName().replaceAll( "[^a-zA-Z0-9]", new String( ) ).toLowerCase(); //$NON-NLS-1$
        FileOutputStream out = null;
        try {
            out = new FileOutputStream( file );
            descrModel.serialize( out );
        } catch ( Exception e ) {
            // TODO Log ?
        } finally {
            try { out.close(); } catch ( Exception e ) { }
        }
            
        UnoidlProjectHelper.refreshProject(prj, null);
        UnoidlProjectHelper.forceBuild(prj, pMonitor);
        
        // create the language-specific part
        ILanguage language = (ILanguage)pData.getProperty(
                IUnoFactoryConstants.PROJECT_LANGUAGE);
        language.getProjectHandler().configureProject(pData);
        
        language.getProjectHandler().createRegistrationSystem(prj);
        
        return prj;
    }
    
    /**
     * Creates a new component implementation skeleton.
     * 
     * <p>Creates a new component implementation skeleton from the project 
     * factory data and opens the generated file. This method executes the 
     * uno-skeletonmaker command line tool.</p>
     * 
     * <p>The data should contain at least the following properties:
     * <ul>
     *     <li>The projet language in {@link IUnoFactoryConstants#PROJECT_LANGUAGE}</li>
     *     <li>The projet name in {@link IUnoFactoryConstants#PROJECT_NAME}</li>
     *     <li>One service inner data defined by:
     *     <ul>
     *         <li>The service name in {@link IUnoFactoryConstants#TYPE_NAME}</li>
     *         <li>The service module in {@link IUnoFactoryConstants#PACKAGE_NAME}</li>
     *     </ul>
     *    </li>
     * </ul>
     * </p>
     * 
     * @param pData the project data for which to create the component 
     *             implementation skeleton.
     * @param pActivePage the page in which to open the created file 
     * @param pMonitor the progress monitor to report the operation progress
     * 
     * @throws Exception is thrown if anything wrong happens
     */
    public static void makeSkeleton(UnoFactoryData pData, 
            IWorkbenchPage pActivePage, IProgressMonitor pMonitor) throws Exception {
        
        String prjName = (String)pData.getProperty(IUnoFactoryConstants.PROJECT_NAME);
        IUnoidlProject prj = ProjectsManager.getProject(prjName);
        
        ILanguage lang = (ILanguage)pData.getProperty(IUnoFactoryConstants.PROJECT_LANGUAGE);
        IProjectHandler langProjectHandler = lang.getProjectHandler();
        String languageOption = langProjectHandler.getSkeletonMakerLanguage(pData);
        
        if (languageOption != null) {
            
            // Get the registries
            String typesReg = ""; //$NON-NLS-1$
            String[] oooTypes = prj.getOOo().getTypesPath();
            for (String oooType : oooTypes) {
                oooType = oooType.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
                oooType = oooType.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
                oooType = "file:///" + oooType; //$NON-NLS-1$
                
                typesReg += " -l " + oooType; //$NON-NLS-1$
            }
            
            
            String prjTypes = prj.getTypesPath().toString();
            typesReg += " -l " + prjTypes; //$NON-NLS-1$

            // Get the unorc file
            String unorc = "-env:BOOTSTRAPINI=\"" + prj.getOOo().getUnorcPath() + "\""; //$NON-NLS-1$ //$NON-NLS-2$

            UnoFactoryData[] inner =  pData.getInnerData();
            String service = ""; //$NON-NLS-1$
            int i = 0;
            
            while (i < inner.length && service.equals("")) { //$NON-NLS-1$
                
                int typeNature = ((Integer)inner[i].getProperty(IUnoFactoryConstants.TYPE_NATURE)).intValue();
                if (typeNature == IUnoFactoryConstants.SERVICE) {
                    String name = (String)inner[i].getProperty(IUnoFactoryConstants.TYPE_NAME);
                    String module = (String)inner[i].getProperty(IUnoFactoryConstants.PACKAGE_NAME);

                    String fullname = module + "::" + name; //$NON-NLS-1$
                    fullname = fullname.replaceAll("::", "."); //$NON-NLS-1$ //$NON-NLS-2$

                    service = fullname;
                }

                i++;
            }
            
            String implementationName = langProjectHandler.getImplementationName(prj, service);
            
            
            String command = "uno-skeletonmaker" +    //$NON-NLS-1$
                " " + unorc +  //$NON-NLS-1$
                " component " + languageOption +  //$NON-NLS-1$
                " --propertysetmixin" +  //$NON-NLS-1$
                " -o ./" + prj.getSourcePath().toOSString() + //$NON-NLS-1$
                " " + typesReg + //$NON-NLS-1$
                " -n " + implementationName + //$NON-NLS-1$
                " -t " + service; //$NON-NLS-1$

            Process process = prj.getSdk().runTool(prj, command, pMonitor);
    
            InputStream err = process.getErrorStream();
            StringWriter writer = new StringWriter();
            
            try {
                int c = err.read();
                while (c != -1) {
                    writer.write(c);
                    c = err.read();
                }
            } finally {
                try {
                    err.close();
                    String error = writer.toString();
                    if (!error.equals("")) { //$NON-NLS-1$
                        PluginLogger.error(error);
                    } else {
                        PluginLogger.info(Messages.getString("UnoFactory.SkeletonGeneratedMessage") +  //$NON-NLS-1$
                                implementationName);
                    }
                } catch (java.io.IOException e) { }
            }
            
            UnoidlProjectHelper.refreshProject(prj, null);

            // opens the generated files
            IPath implementationPath = langProjectHandler.getImplementationFile(implementationName);
            implementationPath = prj.getSourcePath().append(implementationPath);
            IFile implementationFile = prj.getFile(implementationPath);

            showFile(implementationFile, pActivePage);
        }
    }
    
    /**
     * Creates a service from its factory data and opens the created file.
     * 
     * <p>The data needed to create the service needs to be structured in the
     * following way:
     *     <ul>
     *         <li>{@link IUnoFactoryConstants#PACKAGE_NAME} the full name of the 
     *             module containing the service.</li>
     *         <li>{@link IUnoFactoryConstants#TYPE_NAME} the service name.</li>
     *         <li> {@link IUnoFactoryConstants#INHERITED_INTERFACES} the list
     *             of the inherited interfaces of the service as an array of 
     *             strings.</li>
     *         <li>{@link IUnoFactoryConstants#TYPE_PUBLISHED} a boolean indicating
     *             whether the type is marked as published or not.</li>
     *     </ul>
     * </p>
     * 
     * @param pData the data describing the service
     * @param pPrj the uno project that will contain the service
     * @param pActivePage the page in which to open the created file
     * @param pMonitor the progress monitor to report the operation progress
     * @throws Exception is thrown if anything wrong happens
     */
    public static void createService(UnoFactoryData pData, IUnoidlProject pPrj, 
            IWorkbenchPage pActivePage, IProgressMonitor pMonitor) throws Exception {
        createService(pData, pPrj, pActivePage, pMonitor, true);
    }
    
    /**
     * Creates a service from its factory data. 
     * 
     * <p>The data needed to create the service needs to be structured in the
     * following way:
     *     <ul>
     *         <li>{@link IUnoFactoryConstants#PACKAGE_NAME} the full name of the 
     *             module containing the service.</li>
     *         <li>{@link IUnoFactoryConstants#TYPE_NAME} the service name.</li>
     *         <li> {@link IUnoFactoryConstants#INHERITED_INTERFACES} the list
     *             of the inherited interfaces of the service as an array of 
     *             strings.</li>
     *         <li>{@link IUnoFactoryConstants#TYPE_PUBLISHED} a boolean indicating
     *             whether the type is marked as published or not.</li>
     *     </ul>
     * </p>
     * 
     * <p>The created file can be opened if <code>openFile</code> is set to 
     * <code>true</code>.</p>
     * 
     * @param pData the data describing the service
     * @param pPrj the uno project that will contain the service
     * @param pActivePage the page in which to open the created file
     * @param pMonitor the progress monitor to report the operation progress
     * @param pOpenFile opens the created file if set to <code>true</code>
     * @throws Exception is thrown if anything wrong happens
     */
    public static void createService(UnoFactoryData pData, IUnoidlProject pPrj, 
            IWorkbenchPage pActivePage, IProgressMonitor pMonitor, boolean pOpenFile) throws Exception {

        // Extract the data
        String path = (String)pData.getProperty(
                IUnoFactoryConstants.PACKAGE_NAME);
        path = path.replaceAll("\\.", "::"); //$NON-NLS-1$ //$NON-NLS-2$
        String name = (String)pData.getProperty(
                IUnoFactoryConstants.TYPE_NAME);
        String[] inheritedIfaces = (String[])pData.getProperty(
                IUnoFactoryConstants.INHERITED_INTERFACES);
        boolean published = ((Boolean)pData.getProperty(
                IUnoFactoryConstants.TYPE_PUBLISHED)).booleanValue();
        
        // Create the necessary modules
        UnoidlProjectHelper.createModules(path, pPrj, null);

        String typepath = path + "::" + name; //$NON-NLS-1$

        // Create the file node
        IUnoComposite file = CompositeFactory.createTypeFile(typepath, pPrj);

        // Create the file content skeleton
        IUnoComposite fileContent = CompositeFactory.createFileContent(typepath);
        file.addChild(fileContent);

        // Add the include line for the inheritance interface
        createIncludes(fileContent, inheritedIfaces);
        
        String[] includes = getNeededIncludes(pData);
        createIncludes(fileContent, includes);

        IUnoComposite currentModule = createParentModules(fileContent, path);

        // Create the service
        IUnoComposite service = CompositeFactory.createService(name,
                published, inheritedIfaces[0]);
        currentModule.addChild(service);

        // Create all the stuffs
        file.create(true);
        file.dispose();

        // show the generated file
        if (pOpenFile) {
            showType(typepath, pPrj, pActivePage);
        }
    }
    
    /**
     * Creates an interface from its factory data and opens the created file.
     * 
     * @param pData the data describing the interface
     * @param pPrj the UNO project that will contain the interface
     * @param pActivePage the page in which to open the created file
     * @param pMonitor the progress monitor to report the operation progress
     * @throws Exception is thrown if anything wrong happens
     */
    public static void createInterface(UnoFactoryData pData, IUnoidlProject pPrj, 
            IWorkbenchPage pActivePage, IProgressMonitor pMonitor)
        throws Exception {
        createInterface(pData, pPrj, pActivePage, pMonitor, true);
    }
    
    /**
     * Creates an interface from its factory data. The created file can be opened
     * if <code>openFile</code> is set to <code>true</code>.
     * 
     * @param pData the data describing the interface
     * @param pPrj the UNO project that will contain the interface
     * @param pActivePage the page in which to open the created file
     * @param pMonitor the progress monitor to report the operation progress
     * @param pOpenFile opens the created file if set to <code>true</code>
     * @throws Exception is thrown if anything wrong happens
     */
    public static void createInterface(UnoFactoryData pData, IUnoidlProject pPrj, 
            IWorkbenchPage pActivePage, IProgressMonitor pMonitor, boolean pOpenFile)
        throws Exception {
        
        // Extract the data
        String path = (String)pData.getProperty(IUnoFactoryConstants.PACKAGE_NAME);
        path = path.replaceAll("\\.", "::"); //$NON-NLS-1$ //$NON-NLS-2$
        String name = (String)pData.getProperty(IUnoFactoryConstants.TYPE_NAME);
        String[] interfaces = (String[])pData.getProperty(IUnoFactoryConstants.INHERITED_INTERFACES);
        String[] opt_interfaces = (String[])pData.getProperty(IUnoFactoryConstants.OPT_INHERITED_INTERFACES);
        boolean published = ((Boolean)pData.getProperty(IUnoFactoryConstants.TYPE_PUBLISHED)).booleanValue();

        if (0 == interfaces.length && 0 < opt_interfaces.length) {
            interfaces = new String[]{opt_interfaces[0]};
            
            // Remove the first optional interface
            String[] new_opt_interfaces = new String[opt_interfaces.length - 1];
            System.arraycopy(opt_interfaces, 1, new_opt_interfaces, 0, new_opt_interfaces.length);
            opt_interfaces = new_opt_interfaces;
        }
        
        // Create the necessary modules
        UnoidlProjectHelper.createModules(path, pPrj, null);

        String typepath = path + "::" + name; //$NON-NLS-1$

        // Create the file node
        IUnoComposite file = CompositeFactory.createTypeFile(typepath, pPrj);

        // Create the file content skeleton
        IUnoComposite fileContent = CompositeFactory.createFileContent(typepath);
        file.addChild(fileContent);

        createIncludes(fileContent, interfaces);
        createIncludes(fileContent, opt_interfaces);
        
        String[] includes = getNeededIncludes(pData);
        createIncludes(fileContent, includes);

        IUnoComposite currentModule = createParentModules(fileContent, path);

        IUnoComposite intf = CompositeFactory.createInterface(name, published, interfaces);
        currentModule.addChild(intf);

        // Create the optional inheritances
        for (int i = 0; i < opt_interfaces.length; i++) {
            IUnoComposite inherit = CompositeFactory.createInterfaceInheritance(
                    opt_interfaces[i], true);
            intf.addChild(inherit);
        }

        // Creates all the members
        for (UnoFactoryData memberData : pData.getInnerData()) {
            
            // Get the member type: Attribute or Method
            Integer memberType = (Integer)memberData.getProperty(IUnoFactoryConstants.MEMBER_TYPE);
            if (memberType.intValue() == IUnoFactoryConstants.ATTRIBUTE) {
                // create the method composite
                String attrName = (String)memberData.getProperty(IUnoFactoryConstants.NAME);
                String type = (String)memberData.getProperty(IUnoFactoryConstants.TYPE);
                String flags = (String)memberData.getProperty(IUnoFactoryConstants.FLAGS);
                intf.addChild(CompositeFactory.createAttribute(attrName, type, flags));
            } else if (memberType.intValue() == IUnoFactoryConstants.METHOD) {
                // create the attribute composite
                String methodName = (String)memberData.getProperty(IUnoFactoryConstants.NAME);
                String type = (String)memberData.getProperty(IUnoFactoryConstants.TYPE);
                IUnoComposite method = CompositeFactory.createMethod(methodName, type);
                for (UnoFactoryData argData : memberData.getInnerData()) {
                    String argName = (String)argData.getProperty(IUnoFactoryConstants.NAME);
                    String argType = (String)argData.getProperty(IUnoFactoryConstants.TYPE);
                    String direction = (String)argData.getProperty(IUnoFactoryConstants.ARGUMENT_INOUT);
                    method.addChild(CompositeFactory.createMethodArgument(argName, argType, direction));
                }
                intf.addChild(method);
            }
        }
        
        // Generate all the stuffs
        file.create(true);
        file.dispose();

        if (pOpenFile) {
            showType(typepath, pPrj, pActivePage);
        }
    }
    
    /**
     * Show the file declaring a UNO type.
     * 
     * @param pTypepath the complete name of the type to show separated by <code>::</code>.
     * @param pPrj the project containing the type
     * @param pActivePage the workbench active page
     */
    private static void showType(String pTypepath, IUnoidlProject pPrj, IWorkbenchPage pActivePage) {
        // show the generated file
        String filename = pTypepath.replace("::", "/") + ".idl"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        UnoidlProjectHelper.refreshProject(pPrj, null);
        IFile interfaceFile = pPrj.getFile(pPrj.getIdlPath().append(filename));
        showFile(interfaceFile, pActivePage);
    }

    /**
     * Get the includes to add from the children.
     * 
     * @param pData the data to look for includes declarations needs.
     * @return a list of UNO types (separator <code>::</code>)
     */
    private static String[] getNeededIncludes(UnoFactoryData pData) {
        
        ArrayList<String> includes = new ArrayList<String>();
        
        String[] properties = pData.getKeys();
        for (String name : properties) {
            String stringValue = pData.getProperty(name).toString();
            if (stringValue.contains("::") && !name.equals(IUnoFactoryConstants.PACKAGE_NAME)) { //$NON-NLS-1$
                includes.add(stringValue);
            }
        }
        
        for (UnoFactoryData child : pData.getInnerData()) {
            String[] childIncludes = getNeededIncludes(child);
            includes.addAll(Arrays.asList(childIncludes));
        }
        
        return includes.toArray(new String[includes.size()]);
    }

    /**
     * Create the parent modules and return the deepest one.
     * 
     * @param pFileContent the file content UNO composite where to add the modules
     * @param pTypePath the "::" separated path of the modules to create.
     * 
     * @return the deepest created module
     */
    private static IUnoComposite createParentModules(IUnoComposite pFileContent, String pTypePath) {
        // Create the module node using the cascading method
        IUnoComposite topModule = CompositeFactory.createModulesSpaces(pTypePath);
        pFileContent.addChild(topModule);

        IUnoComposite currentModule = topModule;
        while (currentModule.getChildren().length > 0) {

            // Remain that there should be only zero or one module
            IUnoComposite[] children = currentModule.getChildren();
            if (children.length == 1) {
                currentModule = children[0];
            }
        }
        
        return currentModule; 
    }
    
    /**
     * Adds includes composites in a type file.
     * 
     * @param pFileContent the file content composite where to add the includes
     * @param pTypes the types for which to add the includes
     */
    private static void createIncludes(IUnoComposite pFileContent, String[] pTypes) {
        for (int i = 0; i < pTypes.length; i++) {
            pFileContent.addChild(CompositeFactory.createInclude(pTypes[i]));
        }
    }
    
    /**
     * Simply shows the file in the IDE.
     * 
     * @param pFile the file to show
     * @param pPage the active workbench page
     */
    private static void showFile(IFile pFile, IWorkbenchPage pPage) {
        
        try {
            IWorkbench workbench = PlatformUI.getWorkbench();
            BasicNewResourceWizard.selectAndReveal(
                    pFile, workbench.getActiveWorkbenchWindow());

            final IWorkbenchPage activePage = pPage;
            final IFile toShow = pFile;

            if (activePage != null) {
                final Display display = Display.getDefault();
                if (display != null) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            try {
                                IDE.openEditor(activePage, toShow, true);
                            } catch (PartInitException e) {
                                PluginLogger.debug(e.getMessage());
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            PluginLogger.error("Can't open file", e); //$NON-NLS-1$
        }
    }
}
