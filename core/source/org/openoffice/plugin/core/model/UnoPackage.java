/*************************************************************************
 *
 * $RCSfile: UnoPackage.java,v $
 *
 * $Revision: 1.7 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:30 $
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
package org.openoffice.plugin.core.model;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

import org.openoffice.plugin.core.utils.*;

/**
 * This class represents a UNO package and should be used to create a UNO
 * package.
 * 
 * <p>In the same way than ant jar target does, the UNO package is defined by an
 * output file and a root directory. All the file that will be added to the
 * package will have to be contained in this directory or one of its
 * children.</p>
 * 
 * <p>For the different file types see 
 * {@link "http://wiki.services.openoffice.org/wiki/Documentation/DevGuide/Extensions/File_Format"}.
 * </p>
 * 
 * @author cedricbosdo
 * 
 */
public class UnoPackage {

    public static final String MANIFEST_PATH = "manifest.xml";

    public static final String ZIP = "zip";
    public static final String UNOPKG = "uno.pkg";
    public static final String OXT = "oxt";

    private static final Logger log = Logger.getLogger(UnoPackage.class.getName());
    private static final String BASIC_LIBRARY_INDEX = "script.xlb";
    private static final String DIALOG_LIBRARY_INDEX = "dialog.xlb";

    private File mDestination;
    private boolean mBuilding = false;

    private HashMap<String, ZipContent> mZipEntries = new HashMap<String, ZipContent>();
    private ManifestModel mManifest;
    private ArrayList<File> mToClean = new ArrayList<File>();

    private File mReadManifestFile;
    private File mCopyManifestFileTo;

    /**
     * Create a new package object.
     * 
     * <p>The extension has be one of the following. The default extension is
     * {@link #OXT}. If the extension is invalid or missing, the file will be
     * renamed in <code>.oxt</code>. <ul> <li>{@link #ZIP}</li> <li>
     * {@link #UNOPKG}</li> <li>{@link #OXT}</li> </ul> </p>
     * 
     * @param pOut
     *            the file of the package.
     */
    public UnoPackage(File pOut) {
        File dest = pOut;
        if (!(dest.getName().endsWith(ZIP) || dest.getName().endsWith(UNOPKG) || dest.getName().endsWith(OXT))) {
            int pos = dest.getName().lastIndexOf(".");
            if (pos > 0) {
                String name = dest.getName().substring(0, pos);
                dest = new File(dest.getParentFile(), name + "." + OXT);
            } else {
                dest = new File(dest.getParentFile(), dest.getName() + "." + OXT);
            }
        }

        mDestination = dest;
        mManifest = new ManifestModel();
    }

    public static String getPathRelativeToBase(File file, File basePath) {
        String abs = file.getAbsolutePath();

        int pos = abs.indexOf(basePath.getPath());
        if (pos == -1)
            throw new InvalidParameterException("File [" + file + // 
                    "] is not part of the base path tree [" + basePath + "]");

        return abs.substring(basePath.getPath().length() + 1);
    }

    /**
     * Cleans up the data structure. There is no need to call this method if the
     * package has been closed using {@link #close()}
     */
    public void dispose() {
        mDestination = null;
        mZipEntries.clear();
    }

    /**
     * @return the manifest.xml model contained in the package
     */
    public ManifestModel getManifestModel() {
        return mManifest;
    }

    /**
     * Set the manifest.xml file to use for the package: setting this value will
     * skip the manifest.xml file automatic generation.
     * 
     * <p><strong>Setting this value to a non-existing file is the same as
     * setting it with <code>null</code>: the default value will be
     * used.</strong></p>
     * 
     * @param pFile
     *            the file to read.
     * 
     * @see #MANIFEST_PATH The default path value relative to the project
     */
    public void setReadManifestFile(File pFile) {
        if (pFile != null && pFile.exists()) {
            mReadManifestFile = pFile;
        }
    }
    
    public void setCopyManifestFileTo(File pFile) {
    	if (pFile != null && !pFile.exists()) {
    		mCopyManifestFileTo = pFile;
    	}
    }

    /**
     * Adds the content of the given (root) file or directory.
     *
     * @param file the file
     */
    public void addContent(File file) {
    	this.addContent("", file);
    }

    /**
     * Add a file or directory to the package.
     * 
     * <p>This method doesn't know about the different languages contributions
     * to the <code>manifest.xml</code> file.</p>
     * 
     * @param pContent
     *            the file or folder to add
     */
    public void addContent(String pathInArchive, File pContent) {
    	String pathname = pathInArchive == null ? "" : FilenameUtils.normalize(pathInArchive);
        if (pContent.isFile()) {
            this.addFile(pathname, pContent);
        } else if (pContent.isDirectory()) {
        	this.addDirectory(pathname, pContent);
        } else {
            throw new IllegalArgumentException("pContent [" + pContent + "] does not exists");
        }
    }

	/**
	 * Add a file to the package.
	 * 
     * <p>This method doesn't know about the different languages contributions
     * to the <code>manifest.xml</code> file.</p>
	 *
	 * @param pathname the pathname inside the package
	 * @param pContent the content
	 */
	public void addFile(String pathname, File pContent) {
		if (pContent.getName().endsWith(".xcs")) {
		    addConfigurationSchemaFile(pathname, pContent);
		} else if (pContent.getName().endsWith(".xcu")) {
		    addConfigurationDataFile(pathname, pContent);
		} else if (pContent.getName().endsWith(".rdb")) {
		    addTypelibraryFile(pathname, pContent, "RDB");
		} else if (pContent.getName().endsWith(".jar")) {
			addTypelibraryFile(pathname, pContent, "Java");
		} else if (pContent.getName().equals("description.xml")) {
			addPackageDescription(pathname, pContent, Locale.getDefault());
		} else {
		    addOtherFile(pathname, pContent);
		}
	}
    
    /**
     * This method was added for symmetric reason because of the other
     * addDirectory(..) methods.
     * 
     * @param directory the directory
     * @see #addDirectory(File, String[], String[])
     */
    public void addDirectory(final File directory) {
    	this.addDirectory("", directory);
    }
    
    /**
     * Adds the directory.
     *
     * @param pathInArchive the path in archive
     * @param directory the directory
     */
    public void addDirectory(final String pathInArchive, final File directory) {
    	if (!directory.isDirectory()) {
    		throw new IllegalArgumentException(directory + " is not a directory!");
    	}
    	this.addDirectory(pathInArchive, directory, new String[0], new String[0]);
    }

    /**
     * Adds the directory.
     *
     * @param directory the directory
     * @param includes the includes
     * @param excludes the excludes
     */
	public void addDirectory(final File directory, final String[] includes,
			final String[] excludes) {
		addDirectory("", directory, includes, excludes);
    }
    
    private void addDirectory(final String pathInArchive, final File directory, final String[] includes, final String[] excludes) {
    	String pathname = FilenameUtils.normalize(pathInArchive);
    	assert directory.isDirectory();
        if (isBasicLibrary(directory)) {
            addBasicLibraryFile(pathname, directory, includes, excludes);
        } else if (isDialogLibrary(directory)) {
            addDialogLibraryFile(pathname, directory, includes, excludes);
        } else {
            for (File child : directory.listFiles()) {
            	String path = child.getName();
            	if (StringUtils.isNotEmpty(pathname)) {
            		path = FilenameUtils.normalize(pathname + "/" + path);
            	}
            	if (shouldBeExcluded(path, includes, excludes)) {
            		continue;
            	}
            	if (child.isFile()) {
            		addFile(path, child);
            	} else {
            		addDirectory(path + "/", child, includes, excludes);
            	}
            }
        }
    }
    
    private static boolean shouldBeExcluded(final String path, final String[] includes, final String[] excludes) {
    	if ((includes.length > 0) && !match(path, includes)) {
    		System.out.println(path + " will be not included");
    		return true;
    	}
    	if (match(path, excludes)) {
    		System.out.println(path + " will be excluded");
    		return true;
    	}
    	return false;
    }
    
    private static boolean match(final String filename, final String[] filePatterns) {
    	for (int i = 0; i < filePatterns.length; i++) {
			if (match(filename, filePatterns[i])) {
				return true;
			}
		}
    	return false;
    }
    
    private static boolean match(final String filename, final String filePattern) {
		String pattern = filePattern.replace("*", ".*");
		pattern = pattern.replace(".*.*/", ".*/");
		return filename.matches(pattern);
    }
    
    /**
     * Add a uno component file, for example a jar, shared library or python
     * file containing the uno implementation. The type of the file defines the
     * language and should be given as defined in the OOo Developer's Guide,
     * like Java, native, Python.
     * 
     * @param pFile
     *            the file to add to the package
     * @param pType
     *            the type of the file to add.
     * 
     * @see #addComponentFile(IFile, String, String) for platform support
     */
    public void addComponentFile(String pathInArchive, File pFile, String pType) {
        addComponentFile(pathInArchive, pFile, pType, null);
    }

    /**
     * Add a uno component file, for example a jar, shared library or python
     * file containing the uno implementation.
     * 
     * <p>The type of the file defines the language and should be given as
     * defined in the OOo Developer's Guide, like Java, native, Python.</p>
     * 
     * @param pFile
     *            the file to add to the package
     * @param pType
     *            the type of the file to add.
     * @param pPlatform
     *            optional parameter to use only with native type. Please refer
     *            to the OOo Developer's Guide for more information.
     */
    public void addComponentFile(String pathInArchive, File pFile, String pType, String pPlatform) {
        if (!pFile.isFile())
            throw new IllegalArgumentException("pFile [" + pFile + "] is not a file");

        // Do not change the extension from now
        initializeOutput();

        addZipContent(pathInArchive, pFile);
    }

    /**
     * Add a type library to the package.
     * 
     * <p>Note that by some strange way, a jar dependency can be added in the
     * package as a type library like RDB files.</p>
     * 
     * @param pathInArchive the path in the Zip archive
     * @param pFile
     *            the file to add
     * @param pType
     *            the type of the file as specified in the OOo Developer's Guide
     */
    public void addTypelibraryFile(String pathInArchive, File pFile, String pType) {
        if (!pFile.isFile())
            throw new IllegalArgumentException("pFile [" + pFile + "] is not a file");

        // Do not change the extension from now
        initializeOutput();

        if ("Java".equalsIgnoreCase(pType) && hasRegistrationHandlerInside(pFile)) {
        	mManifest.addComponentFile(pathInArchive, pType);
        } else {
        	mManifest.addTypelibraryFile(pathInArchive, pType);
        }
        addZipContent(pathInArchive, pFile);
    }
    
    /**
     * Here we look if a RegistrationHandler.class is inside the given
     * jar file. In this case no entry is needed for the generated manifest
     * file.
     * <br/>
     * This method has package default visibility for testing.
     *
     * @param file the jar file
     * @return true, if successful
     * @since 24-Sep-2010 (oliver.boehm@agentes.de)
     */
    static boolean hasRegistrationHandlerInside(final File file) {
    	try {
			JarFile jarFile = new JarFile(file);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					continue;
				}
				if (entry.getName().endsWith("RegistrationHandler.class")
						|| entry.getName().endsWith(
								"RegistrationHandler.classes")) {
					return true;
				}
			}
		} catch (IOException ioe) {
			log.warning("can't read " + file + " (" + ioe + ")");
		}
    	return false;
    }
    
    /**
     * Add a basic library to the package.
     * 
     * @param dir the directory of the basic library.
     */
    public void addBasicLibraryFile(File dir) {
    	this.addBasicLibraryFile(null, dir);
    }

    /**
     * Add a basic library to the package.
     * 
     * <p>Even if this method may not be used, it is possible.</p>
     * 
     * @param pDir
     *            the directory of the basic library.
     */
    public void addBasicLibraryFile(String pathInArchive, File pDir) {
        this.addBasicLibraryFile(pathInArchive, pDir, new String[0], new String[0]);
    }

    /**
     * Add a basic library to the package.
     * 
     * <p>Even if this method may not be used, it is possible.</p>
     *
     * @param pathInArchive the path in archive
     * @param pDir the directory of the basic library.
     * @param includes the includes
     * @param excludes the excludes
     */
	public void addBasicLibraryFile(String pathInArchive, File pDir,
			String[] includes, String[] excludes) {
        if (!pDir.isDirectory())
            throw new IllegalArgumentException("pDir [" + pDir + "] is not a folder");

        // Do not change the extension from now
        initializeOutput();

        mManifest.addBasicLibrary(pathInArchive);
        addZipContent(pathInArchive, pDir, includes, excludes);
    }

    /**
     * Add a dialog library to the package.
     * 
     * @param dir the directory of the dialog library.
     */
    public void addDialogLibraryFile(File dir) {
    	this.addDialogLibraryFile(null, dir);
    }
    
    /**
     * Add a dialog library to the package.
     * 
     * <p>Even if this method may not be used, it is possible.</p>
     * 
     * @param pDir
     *            the directory of the dialog library.
     */
    public void addDialogLibraryFile(String pathInArchive, File pDir) {
        if (!pDir.isDirectory())
            throw new IllegalArgumentException("pDir [" + pDir + "] is not a folder");

        // Do not change the extension from now
        initializeOutput();

        mManifest.addDialogLibrary(pathInArchive);
        addZipContent(pathInArchive, pDir);
    }

    /**
     * Add a dialog library to the package.
     * 
     * <p>Even if this method may not be used, it is possible.</p>
     *
     * @param pathInArchive the path in archive
     * @param pDir the directory of the dialog library.
     * @param includes the includes
     * @param excludes the excludes
     */
    public void addDialogLibraryFile(String pathInArchive, File pDir,
			String[] includes, String[] excludes) {
        if (!pDir.isDirectory())
            throw new IllegalArgumentException("pDir [" + pDir + "] is not a folder");

        // Do not change the extension from now
        initializeOutput();

        mManifest.addDialogLibrary(pathInArchive);
        addZipContent(pathInArchive, pDir, includes, excludes);
    }

    /**
     * Add an xcu configuration to the package.
     * 
     * @param pFile
     *            the xcu file to add
     */
    public void addConfigurationDataFile(String pathInArchive, File pFile) {
        if (!pFile.isFile())
            throw new IllegalArgumentException("pFile [" + pFile + "] is not a file");

        // Do not change the extension from now
        initializeOutput();

        mManifest.addConfigurationDataFile(pathInArchive);
        addZipContent(pathInArchive, pFile);
    }

    /**
     * Add an xcs configuration to the package.
     * 
     * @param pFile
     *            the xcs file to add
     */
    public void addConfigurationSchemaFile(String pathInArchive, File pFile) {
        if (!pFile.isFile())
            throw new IllegalArgumentException("pFile [" + pFile + "] is not a file");

        // Do not change the extension from now
        initializeOutput();

        mManifest.addConfigurationSchemaFile(pathInArchive);
        addZipContent(pathInArchive, pFile);
    }

    /**
     * Add a localized description of the package.
     * 
     * @param pFile
     *            the file containing the description for that locale
     * @param pLocale
     *            the locale of the description. Can be <code>null</code>.
     */
    public void addPackageDescription(String pathInArchive, File pFile, Locale pLocale) {
        if (!pFile.isFile())
            throw new IllegalArgumentException("pFile [" + pFile + "] is not a file");

        mManifest.addDescription(pathInArchive, pLocale);
        addZipContent(pathInArchive, pFile);
    }

    /**
     * Adds a file or directory to the package but do not include it in the
     * manifest.
     * 
     * <p>This could be used for example for images.</p>
     * 
     * @param pFile
     *            the file or directory to add.
     */
    public void addOtherFile(String pathInArchive, File pFile) {
        if (!pFile.isFile()) {
			throw new IllegalArgumentException("pFile ["
					+ pFile.getAbsolutePath() + "] is not a file");
        }
        // Do not change the extension from now
        initializeOutput();

        addZipContent(pathInArchive, pFile);
    }

    /**
     * Writes the package on the disk and cleans up the data. The UnoPackage
     * instance cannot be used after this operation: it should unreferenced.
     * 
     * @param pMonitor
     *            the progress monitor
     * 
     * @return the file of the package or <code>null</code> if nothing happened.
     */
    public File close() {
        File result = null;

        if (mBuilding) {
            try {
                // Write the ZipContent
                FileOutputStream out = new FileOutputStream(mDestination);
                ZipOutputStream zipOut = new ZipOutputStream(out);

                Iterator<ZipContent> entries = mZipEntries.values().iterator();
                while (entries.hasNext()) {
                    ZipContent content = entries.next();
                    content.writeContentToZip(zipOut);
                }

                // Add the manifest to the zip (if not already inside)
                if (!mZipEntries.containsKey("META-INF/manifest.xml")) {
                    File manifestFile = mReadManifestFile;
                    if (manifestFile == null) {
                        manifestFile = createManifestFile();
                        this.addToClean(manifestFile);
                        
                        // Copy the manifest file if required
                        if (mCopyManifestFileTo != null) {
                        	FileHelper.copyFile(manifestFile, mCopyManifestFileTo, true);
                        }
                    }
	                ZipContent manifest = new ZipContent("META-INF/manifest.xml", manifestFile);
	                manifest.writeContentToZip(zipOut);
                }

                // close the streams
                zipOut.close();
                out.close();

            } catch (Exception e) {
                System.err.println("Error while package creation: " + e);
            }

            result = mDestination;

            cleanResources();

            dispose();
        }
        return result;
    }
    
    private File createManifestFile() throws IOException {
    	File manifest = new File(System.getProperty("java.io.tmpdir"), MANIFEST_PATH);
    	if (manifest.exists()) {
			throw new IOException("I don't risk to overwrite "
					+ manifest.getAbsolutePath() + " - please delete it manually!");
    	}
        FileOutputStream writer = new FileOutputStream(manifest);
        mManifest.write(writer);
        writer.close();
    	return manifest;
    }

    /**
     * @return a list of the files that are already queued for addition to the
     *         package.
     */
    public List<File> getContainedFiles() {
        ArrayList<File> files = new ArrayList<File>(mZipEntries.size());
        for (ZipContent content : mZipEntries.values()) {
            files.add(content.getFile());
        }
        return files;
    }
    
    /**
     * Gets the list of the names that are already queued for addition to the
     *         package.
     *
     * @return the contained names
     */
    public List<String> getContainedNames() {
    	return new ArrayList<String>(mZipEntries.keySet());
    }

    /**
     * Add the path to a resource to clean after having exported the package.
     * The resource won't be cleaned if the package isn't exported.
     * 
     * @param pPath
     *            the path to the resource to clean.
     */
    public void addToClean(File pPath) {
        mToClean.add(pPath);
    }

    /**
     * Creates the main elements for the package creation.
     * 
     * <p>After this step, the extension cannot be changed. Calling this method
     * when the package has already been initialized does nothing.</p>
     * 
     */
    private void initializeOutput() {
        mBuilding = true;
    }

    /**
     * Recursively add the file or directory to the Zip entries.
     * 
     * @param pRelativePath
     *            the relative path of the file to add
     * @param pFile
     *            the file or directory to add
     */
    private void addZipContent(String pRelativePath, File pFile) {
    	addZipContent(pRelativePath, pFile, new String[0], new String[0]);
    }

    /**
     * Recursively add the file or directory to the Zip entries.
     *
     * @param pRelativePath the relative path of the file to add
     * @param pFile the file or directory to add
     * @param includes the includes
     * @param excludes the excludes
     */
	private void addZipContent(String pRelativePath, File pFile,
			String[] includes, String[] excludes) {
        if (pRelativePath != null) {
            if (pFile.isDirectory()) {
                // Add all the children
                try {
					for (File child : pFile.listFiles()) {
						String path = FilenameUtils.normalize(pRelativePath
								+ "/" + child.getName());
						if (shouldBeExcluded(path, includes, excludes)) {
							continue;
						}
						addZipContent(path, child);
					}
                } catch (Exception e) {
                }
            } else {
                String zipPath = FileHelper.separatorsToUnix(pRelativePath);
                System.out.println("Adding " + zipPath + " to oxt package");
                ZipContent content = new ZipContent(zipPath, pFile);
                mZipEntries.put(pRelativePath, content);
            }
        }
    }

    /**
     * Clean the resources added using {@link #addToClean(IPath)}.
     */
    private void cleanResources() {
        for (File file : mToClean) {
            FileHelper.remove(file);
        }
    }

    /**
     * Checks if the resource is a dialog library.
     * 
     * @param pRes
     *            the resource to check
     * 
     * @return <code>true</code> if the resource is a dialog library,
     *         <code>false</code> in any other case
     */
    private boolean isDialogLibrary(File pRes) {
        boolean result = false;
        if (pRes.isDirectory()) {
            result = new File(pRes, DIALOG_LIBRARY_INDEX).exists();
        }
        return result;
    }

    /**
     * Checks if the resource is a basic library.
     * 
     * @param pRes
     *            the resource to check
     * 
     * @return <code>true</code> if the resource is a basic library,
     *         <code>false</code> in any other case
     */
    private boolean isBasicLibrary(File pRes) {
        boolean result = false;
        if (pRes.isDirectory()) {
            result = new File(pRes, BASIC_LIBRARY_INDEX).exists();
        }
        return result;
    }
}
