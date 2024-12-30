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
package org.libreoffice.plugin.core.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IPath;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.plugin.core.utils.FileHelper;
import org.libreoffice.plugin.core.utils.FilenameUtils;
import org.libreoffice.plugin.core.utils.StringUtils;
import org.libreoffice.plugin.core.utils.ZipContent;

/**
 * This class represents a UNO package and should be used to create a UNO package.
 *
 * <p>
 * In the same way than ant jar target does, the UNO package is defined by an output file and a root directory. All the
 * file that will be added to the package will have to be contained in this directory or one of its children.
 * </p>
 *
 * <p>
 * For the different file types see
 * {@link "http://wiki.services.openoffice.org/wiki/Documentation/DevGuide/Extensions/File_Format"}.
 * </p>
 */
public class UnoPackage {

    public static final String MANIFEST_PATH = "manifest.xml";

    public static final String ZIP = "zip";
    public static final String UNOPKG = "uno.pkg";
    public static final String OXT = "oxt";

    private static final String BASIC_LIBRARY_INDEX = "script.xlb";
    private static final String DIALOG_LIBRARY_INDEX = "dialog.xlb";

    private static final String MIMETYPE = "mimetype";
    private static final String MIMETYPE_CONTENT = "application/vnd.openofficeorg.extension";

    private File mDestination;
    private boolean mBuilding = false;

    private Map<String, ZipContent> mZipEntries = new HashMap<>();
    private ManifestModel mManifest;
    private ArrayList<File> mToClean = new ArrayList<File>();

    private File mReadManifestFile;
    private File mCopyManifestFileTo;

    /**
     * Create a new package object.
     *
     * <p>
     * The extension has be one of the following. The default extension is {@link #OXT}. If the extension is invalid or
     * missing, the file will be renamed in <code>.oxt</code>.
     * <ul>
     * <li>{@link #ZIP}</li>
     * <li>{@link #UNOPKG}</li>
     * <li>{@link #OXT}</li>
     * </ul>
     * </p>
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

    public static String getPathRelativeToBase(File pFile, File pBasePath) {
        String abs = pFile.getAbsolutePath();

        int pos = abs.indexOf(pBasePath.getPath());
        if (pos == -1) {
            String msg = "File [" + pFile + "] is not part of the base path tree [" + pBasePath + "]";
            throw new InvalidParameterException(msg);
        }

        return abs.substring(pBasePath.getPath().length() + 1);
    }

    /**
     * Cleans up the data structure. There is no need to call this method if the package has been closed using
     * {@link #close()}
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
     * Set the manifest.xml file to use for the package: setting this value will skip the manifest.xml file automatic
     * generation.
     *
     * <p>
     * <strong>Setting this value to a non-existing file is the same as setting it with <code>null</code>: the default
     * value will be used.</strong>
     * </p>
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
     * @param pFile
     *            the file
     */
    public void addContent(File pFile) {
        this.addContent("", pFile);
    }

    /**
     * Add a file or directory to the package.
     *
     * <p>
     * This method doesn't know about the different languages contributions to the <code>manifest.xml</code> file.
     * </p>
     * 
     * @param pPathInArchive
     *            the pathname inside the package
     *
     * @param pContent
     *            the file or folder to add
     */
    public void addContent(String pPathInArchive, File pContent) {
        String pathname = "";
        if (pPathInArchive != null) {
            pathname = FilenameUtils.normalize(pPathInArchive);
        }
        pathname = FilenameUtils.separatorsToUnix(pathname);
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
     * <p>
     * This method doesn't know about the different languages contributions to the <code>manifest.xml</code> file.
     * </p>
     *
     * @param pPathname
     *            the pathname inside the package
     * @param pContent
     *            the content
     */
    public void addFile(String pPathname, File pContent) {
        if (pContent.getName().endsWith(".xcs")) {
            addConfigurationSchemaFile(pPathname, pContent);
        } else if (pContent.getName().endsWith(".xcu")) {
            addConfigurationDataFile(pPathname, pContent);
        } else if (pContent.getName().endsWith(".rdb")) {
            addTypelibraryFile(pPathname, pContent);
        } else if (pContent.getName().equals("description.xml")) {
            addPackageDescription(pPathname, pContent, Locale.getDefault());
        } else {
            addOtherFile(pPathname, pContent);
        }
    }

    /**
     * This method was added for symmetric reason because of the other addDirectory(..) methods.
     *
     * @param pDirectory
     *            the directory
     * @see #addDirectory(File, String[], String[])
     */
    public void addDirectory(final File pDirectory) {
        this.addDirectory("", pDirectory);
    }

    /**
     * Adds the directory.
     *
     * @param pPathInArchive
     *            the path in archive
     * @param pDirectory
     *            the directory
     */
    public void addDirectory(final String pPathInArchive, final File pDirectory) {
        if (!pDirectory.isDirectory()) {
            throw new IllegalArgumentException(pDirectory + " is not a directory!");
        }
        this.addDirectory(pPathInArchive, pDirectory, new String[0], new String[0]);
    }

    /**
     * Adds the directory.
     *
     * @param pDirectory
     *            the directory
     * @param pIncludes
     *            the includes
     * @param pExcludes
     *            the excludes
     */
    public void addDirectory(final File pDirectory, final String[] pIncludes, final String[] pExcludes) {
        addDirectory("", pDirectory, pIncludes, pExcludes);
    }

    private void addDirectory(final String pPathInArchive, final File pDirectory,
                              final String[] pIncludes, final String[] pExcludes) {
        String pathname = FilenameUtils.normalize(pPathInArchive);
        assert pDirectory.isDirectory();
        if (isBasicLibrary(pDirectory)) {
            addBasicLibraryFile(pathname, pDirectory, pIncludes, pExcludes);
        } else if (isDialogLibrary(pDirectory)) {
            addDialogLibraryFile(pathname, pDirectory, pIncludes, pExcludes);
        } else {
            for (File child : pDirectory.listFiles()) {
                String path = child.getName();
                if (StringUtils.isNotEmpty(pathname)) {
                    path = FilenameUtils.normalize(pathname + "/" + path);
                }
                if (shouldBeExcluded(path, pIncludes, pExcludes)) {
                    continue;
                }
                if (child.isFile()) {
                    addFile(path, child);
                } else {
                    addDirectory(path + "/", child, pIncludes, pExcludes);
                }
            }
        }
    }

    private static boolean shouldBeExcluded(final String pPath, final String[] pIncludes, final String[] pExcludes) {
        boolean excluded = false;
        if (pIncludes.length > 0 && !match(pPath, pIncludes)) {
            System.out.println(pPath + " will be not included");
            excluded = true;
        } else if (match(pPath, pExcludes)) {
            System.out.println(pPath + " will be excluded");
            excluded = true;
        }
        return excluded;
    }

    private static boolean match(final String pFileName, final String[] pFilePatterns) {
        boolean matched = false;
        for (int i = 0; i < pFilePatterns.length; i++) {
            if (match(pFileName, pFilePatterns[i])) {
                matched = true;
                break;
            }
        }
        return matched;
    }

    private static boolean match(final String pFileName, final String pFilePattern) {
        String pattern = pFilePattern.replace("*", ".*");
        pattern = pattern.replace(".*.*/", ".*/");
        return pFileName.matches(pattern);
    }

    /**
     * Add a uno component file, for example a jar, shared library or python file containing the uno implementation. The
     * type of the file defines the language and should be given as defined in the OOo Developer's Guide, like Java,
     * native, Python.
     *
     * @param pPathInArchive
     *            the path in the Zip archive
     * @param pFile
     *            the file to add to the package
     * @param pType
     *            the type of the file to add.
     */
    public void addComponentFile(String pPathInArchive, File pFile, String pType) {
        if (!pFile.isFile()) {
            throw new IllegalArgumentException("pFile [" + pFile + "] is not a file");
        }

        pPathInArchive = FilenameUtils.separatorsToUnix(pPathInArchive);

        // Do not change the extension from now
        initializeOutput();

        mManifest.addComponentFile(pPathInArchive, pType);
        addZipContent(pPathInArchive, pFile);
    }

    /**
     * Add a type library to the package.
     *
     * @param pPathInArchive
     *            the path in the Zip archive
     * @param pFile
     *            the file to add
     */
    public void addTypelibraryFile(String pPathInArchive, File pFile) {
        if (!pFile.isFile()) {
            throw new IllegalArgumentException("pFile [" + pFile + "] is not a file");
        }

        pPathInArchive = FilenameUtils.separatorsToUnix(pPathInArchive);

        // Do not change the extension from now
        initializeOutput();

        mManifest.addTypelibraryFile(pPathInArchive);
        addZipContent(pPathInArchive, pFile);
    }

    /**
     * Here we look if a RegistrationHandler.class is inside the given jar file. In this case no entry is needed for the
     * generated manifest file. <br/>
     * This method has package default visibility for testing.
     *
     * @param pFile
     *            the jar file
     * @return true, if successful
     * @since 24-Sep-2010 (oliver.boehm@agentes.de)
     */
    static boolean hasRegistrationHandlerInside(final File pFile) {
        boolean has = false;
        try {
            JarFile jarFile = new JarFile(pFile);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                if (entry.getName().endsWith("RegistrationHandler.class")
                    || entry.getName().endsWith("RegistrationHandler.classes")) {
                    jarFile.close();
                    has = true;
                    break;
                }
            }
            jarFile.close();
        } catch (IOException ioe) {
            PluginLogger.warning("can't read " + pFile + " (" + ioe + ")");
        }
        return has;
    }

    /**
     * Add a basic library to the package.
     *
     * @param pDir
     *            the directory of the basic library.
     */
    public void addBasicLibraryFile(File pDir) {
        this.addBasicLibraryFile(null, pDir);
    }

    /**
     * Add a basic library to the package.
     *
     * <p>
     * Even if this method may not be used, it is possible.
     * </p>
     *
     * @param pPathInArchive
     *            the path in archive
     * @param pDir
     *            the directory of the basic library.
     */
    public void addBasicLibraryFile(String pPathInArchive, File pDir) {
        this.addBasicLibraryFile(pPathInArchive, pDir, new String[0], new String[0]);
    }

    /**
     * Add a basic library to the package.
     *
     * <p>
     * Even if this method may not be used, it is possible.
     * </p>
     *
     * @param pPathInArchive
     *            the path in archive
     * @param pDir
     *            the directory of the basic library.
     * @param pIncludes
     *            the includes
     * @param pExcludes
     *            the excludes
     */
    public void addBasicLibraryFile(String pPathInArchive, File pDir, String[] pIncludes, String[] pExcludes) {
        if (!pDir.isDirectory()) {
            throw new IllegalArgumentException("pDir [" + pDir + "] is not a folder");
        }

        // Do not change the extension from now
        initializeOutput();

        mManifest.addBasicLibrary(pPathInArchive);
        addZipContent(pPathInArchive, pDir, pIncludes, pExcludes);
    }

    /**
     * Add a dialog library to the package.
     *
     * @param pDir
     *            the directory of the dialog library.
     */
    public void addDialogLibraryFile(File pDir) {
        this.addDialogLibraryFile(null, pDir);
    }

    /**
     * Add a dialog library to the package.
     *
     * <p>
     * Even if this method may not be used, it is possible.
     * </p>
     *
     * @param pPathInArchive
     *            the path in archive
     * @param pDir
     *            the directory of the dialog library.
     */
    public void addDialogLibraryFile(String pPathInArchive, File pDir) {
        if (!pDir.isDirectory()) {
            throw new IllegalArgumentException("pDir [" + pDir + "] is not a folder");
        }

        // Do not change the extension from now
        initializeOutput();

        mManifest.addDialogLibrary(pPathInArchive);
        addZipContent(pPathInArchive, pDir);
    }

    /**
     * Add a dialog library to the package.
     *
     * <p>
     * Even if this method may not be used, it is possible.
     * </p>
     *
     * @param pPathInArchive
     *            the path in archive
     * @param pDir
     *            the directory of the dialog library.
     * @param pIncludes
     *            the includes
     * @param pExcludes
     *            the excludes
     */
    public void addDialogLibraryFile(String pPathInArchive, File pDir, String[] pIncludes, String[] pExcludes) {
        if (!pDir.isDirectory()) {
            throw new IllegalArgumentException("pDir [" + pDir + "] is not a folder");
        }

        // Do not change the extension from now
        initializeOutput();

        mManifest.addDialogLibrary(pPathInArchive);
        addZipContent(pPathInArchive, pDir, pIncludes, pExcludes);
    }

    /**
     * Add an xcu configuration to the package.
     *
     * @param pPathInArchive
     *            the path in archive
     *
     * @param pFile
     *            the xcu file to add
     */
    public void addConfigurationDataFile(String pPathInArchive, File pFile) {
        if (!pFile.isFile()) {
            throw new IllegalArgumentException("pFile [" + pFile + "] is not a file");
        }

        // Do not change the extension from now
        initializeOutput();

        mManifest.addConfigurationDataFile(pPathInArchive);
        addZipContent(pPathInArchive, pFile);
    }

    /**
     * Add an xcs configuration to the package.
     *
     * @param pPathInArchive
     *            the path in archive
     * @param pFile
     *            the xcs file to add
     */
    public void addConfigurationSchemaFile(String pPathInArchive, File pFile) {
        if (!pFile.isFile()) {
            throw new IllegalArgumentException("pFile [" + pFile + "] is not a file");
        }

        // Do not change the extension from now
        initializeOutput();

        mManifest.addConfigurationSchemaFile(pPathInArchive);
        addZipContent(pPathInArchive, pFile);
    }

    /**
     * Add a localized description of the package.
     *
     * @param pPathInArchive
     *            the path in archive
     * @param pFile
     *            the file containing the description for that locale
     * @param pLocale
     *            the locale of the description. Can be <code>null</code>.
     */
    public void addPackageDescription(String pPathInArchive, File pFile, Locale pLocale) {
        if (!pFile.isFile()) {
            throw new IllegalArgumentException("pFile [" + pFile + "] is not a file");
        }

        mManifest.addDescription(pPathInArchive, pLocale);
        addZipContent(pPathInArchive, pFile);
    }

    /**
     * Adds a file or directory to the package but do not include it in the manifest.
     *
     * <p>
     * This could be used for example for images.
     * </p>
     *
     * @param pPathInArchive
     *            the path in archive
     * @param pFile
     *            the file or directory to add.
     */
    public void addOtherFile(String pPathInArchive, File pFile) {
        if (!pFile.isFile()) {
            throw new IllegalArgumentException("pFile [" + pFile.getAbsolutePath() + "] is not a file");
        }

        pPathInArchive = FilenameUtils.separatorsToUnix(pPathInArchive);

        // Do not change the extension from now
        initializeOutput();

        addZipContent(pPathInArchive, pFile);
    }

    /**
     * Writes the package on the disk and cleans up the data. The UnoPackage instance cannot be used after this
     * operation: it should unreferenced.
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

                // Add mimetype file to zip file (first entry file and no compressed)
                ZipEntry entry = new ZipEntry(MIMETYPE);
                byte[] mimetype = MIMETYPE_CONTENT.getBytes(StandardCharsets.UTF_8);
                entry.setMethod(ZipEntry.STORED);
                entry.setSize(mimetype.length);
                entry.setCompressedSize(mimetype.length);
                CRC32 crc = new CRC32();
                crc.update(mimetype);
                entry.setCrc(crc.getValue());
                zipOut.putNextEntry(entry);
                zipOut.write(mimetype);

                // Add the content files to the zip
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
            throw new IOException("I don't risk to overwrite " + manifest.getAbsolutePath()
            + " - please delete it manually!");
        }
        FileOutputStream writer = new FileOutputStream(manifest);
        mManifest.write(writer);
        writer.close();
        return manifest;
    }

    /**
     * @return a list of the files that are already queued for addition to the package.
     */
    public List<File> getContainedFiles() {
        ArrayList<File> files = new ArrayList<File>(mZipEntries.size());
        for (ZipContent content : mZipEntries.values()) {
            files.add(content.getFile());
        }
        return files;
    }

    /**
     * Gets the list of the names that are already queued for addition to the package.
     *
     * @return the contained names
     */
    public List<String> getContainedNames() {
        return new ArrayList<String>(mZipEntries.keySet());
    }

    /**
     * Add the path to a resource to clean after having exported the package. The resource won't be cleaned if the
     * package isn't exported.
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
     * <p>
     * After this step, the extension cannot be changed. Calling this method when the package has already been
     * initialized does nothing.
     * </p>
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
     * @param pRelativePath
     *            the relative path of the file to add
     * @param pFile
     *            the file or directory to add
     * @param pIncludes
     *            the includes
     * @param pExcludes
     *            the excludes
     */
    private void addZipContent(String pRelativePath, File pFile, String[] pIncludes, String[] pExcludes) {
        if (pRelativePath == null) {
            return;
        }

        if (pFile.isDirectory()) {
            // Add all the children
            try {
                for (File child : pFile.listFiles()) {
                    String path = FilenameUtils.normalize(pRelativePath + "/" + child.getName());
                    if (shouldBeExcluded(path, pIncludes, pExcludes)) {
                        continue;
                    }
                    addZipContent(path, child);
                }
            } catch (Exception e) {
            }
        } else {
            String zipPath = FileHelper.separatorsToUnix(pRelativePath);
            PluginLogger.debug("Adding " + zipPath + " to oxt package");
            ZipContent content = new ZipContent(zipPath, pFile);
            mZipEntries.put(pRelativePath, content);
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
     * @return <code>true</code> if the resource is a dialog library, <code>false</code> in any other case
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
     * @return <code>true</code> if the resource is a basic library, <code>false</code> in any other case
     */
    private boolean isBasicLibrary(File pRes) {
        boolean result = false;
        if (pRes.isDirectory()) {
            result = new File(pRes, BASIC_LIBRARY_INDEX).exists();
        }
        return result;
    }
}
