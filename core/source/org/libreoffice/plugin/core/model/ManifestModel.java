/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Cédric Bosdonnat.
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2009 by Cédric Bosdonnat.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.plugin.core.model;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class representing the data contained in the manifest.xml file.
 */
public class ManifestModel {

    private static final String EXT_XCS = ".xcs";
    private static final String EXT_XCU = ".xcu";
    private static final String EXT_RDB = ".rdb";

    private Map<String, FileType> mEntries = new HashMap<>();

    /**
     * Add a file or directory to the package.
     *
     * <p>
     * This method doesn't know about the different languages contributions to the <code>manifest.xml</code> file.
     * </p>
     *
     * @param pPath the path
     *
     * @param content the file or folder to add
     */
    public void addContent(String pPath, File content) {
        if (content.isFile()) {
            if (content.getName().endsWith(EXT_XCS)) {
                addConfigurationSchemaFile(pPath);
            } else if (content.getName().endsWith(EXT_XCU)) {
                addConfigurationDataFile(pPath);
            } else if (content.getName().endsWith(EXT_RDB)) {
                addTypelibraryFile(pPath);
            } else if (content.getName().equals("description.xml")) {
                addDescription(pPath, Locale.getDefault());
            }
        } else if (content.isDirectory()) {
            // Recurse on the directory
            for (File child : content.listFiles()) {
                addContent(pPath + "/" + child.getName(), child);
            }
        } else {
            throw new IllegalArgumentException("pContent [" + content + "] does not exists");
        }
    }

    /**
     * Add a uno component file, for example a jar, shared library or python file containing the uno implementation. The
     * type of the file defines the language and should be given as defined in the OOo Developer's Guide, like Java,
     * native, Python.
     *
     * @param file
     *            the file to add to the package
     * @param type
     *            the type of the file to add.
     *
     * @see #addComponentFile(File, String, String) for platform support
     */
    public void addComponentFile(String file, String type) {
        addComponentFile(file, type, null);
    }

    /**
     * Add a uno component file, for example a jar, shared library or python file containing the uno implementation.
     *
     * <p>
     * The type of the file defines the language and should be given as defined in the OOo Developer's Guide, like Java,
     * native, Python.
     * </p>
     *
     * @param file
     *            the file to add to the package
     * @param type
     *            the type of the file to add.
     * @param platform
     *            optional parameter to use only with native type. Please refer to the OOo Developer's Guide for more
     *            information.
     */
    public void addComponentFile(String file, String type, String platform) {
        FileType newType = new FileType(FileType.MIME_UNO_COMPONENT);
        newType.addParam(FileType.PARAM_TYPE, type);
        if (platform != null && type.equals("native")) {
            newType.addParam(FileType.PARAM_PLATFORM, platform);
        }

        addEntry(file, newType);
    }

    /**
     * Add a UNO components XML file to the package.
     *
     * @param pFile
     *            the file to add
     *
     * @see #addComponentsFile(String, String) for platform support
     */
    public void addComponentsFile(String pFile) {
        addComponentsFile(pFile, null);
    }

    /**
     * Add a UNO components XML file to the package.
     *
     * @param pFile
     *            the file to add
     * @param pPlatform
     *            optional parameter to use only with native type. Please refer
     *            to the OOo Developer's Guide for more information.
     */
    public void addComponentsFile(String pFile, String pPlatform) {
        FileType type = new FileType(FileType.MIME_UNO_COMPONENTS);
        if (pPlatform != null) {
            type.addParam(FileType.PARAM_PLATFORM, pPlatform);
        }

        addEntry(pFile, type);
    }

    /**
     * Add a type library to the package.
     *
     * @param file
     *            the file to add
     */
    public void addTypelibraryFile(String file) {
        FileType type = new FileType(FileType.MIME_UNO_TYPES);
        type.addParam(FileType.PARAM_TYPE, "RDB");

        addEntry(file, type);
    }

    /**
     * Add a basic library to the package.
     *
     * <p>
     * Even if this method may not be used, it is possible.
     * </p>
     *
     * @param dir
     *            the directory of the basic library.
     */
    public void addBasicLibrary(String dir) {
        FileType type = new FileType(FileType.MIME_BASIC_LIB);

        addEntry(dir, type);
    }

    /**
     * Add a dialog library to the package.
     *
     * <p>
     * Even if this method may not be used, it is possible.
     * </p>
     *
     * @param dir
     *            the directory of the dialog library.
     */
    public void addDialogLibrary(String dir) {
        FileType type = new FileType(FileType.MIME_DIALOG_LIB);

        addEntry(dir, type);
    }

    /**
     * Add an xcu configuration to the package.
     *
     * @param file
     *            the xcu file to add
     */
    public void addConfigurationDataFile(String file) {
        if (new File(file).getName().endsWith(EXT_XCU)) {
            FileType type = new FileType(FileType.MIME_XCU);

            addEntry(file, type);
        }
    }

    /**
     * Add an xcs configuration to the package.
     *
     * @param file
     *            the xcs file to add
     */
    public void addConfigurationSchemaFile(String file) {
        if (new File(file).getName().endsWith(EXT_XCS)) {
            FileType type = new FileType(FileType.MIME_XCS);

            addEntry(file, type);
        }
    }

    /**
     * Add a localized description of the package.
     *
     * @param descriptionFile
     *            the file containing the description for that locale
     * @param locale
     *            the locale of the description. Can be <code>null</code>.
     */
    public void addDescription(String descriptionFile, Locale locale) {
        // write the description to a file
        String localeString = new String();
        if (locale != null) {
            localeString = locale.toString();
            localeString = localeString.replace("_", "-"); //$NON-NLS-2$
        }

        // Add the file entry to the manifest
        FileType type = new FileType(FileType.MIME_DESCRIPTION);
        if (!localeString.isEmpty()) {
            type.addParam(FileType.PARAM_LOCALE, localeString);
        }

        addEntry(descriptionFile, type);
    }

    /**
     * This is the generic method to add an element to the manifest: nothing is tested here.
     *
     * @param pRelativePath
     *            the path of the file relative to the package
     * @param pType
     *            the type of the file to write in the manifest
     */
    private void addEntry(String pRelativePath, FileType pType) {
        String path = pRelativePath;
        path = path.replace("\\", "/");
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        mEntries.put(path, pType);
    }

    /**
     * Output the manifest.xml file.
     *
     * @param out
     *            where to write the manifest.
     * @throws IOException
     *             if something happened when writing to the output stream
     */
    public void write(OutputStream out) throws IOException {
        // Iterator<Entry<String, FileType>> iter = mEntries.entrySet().iterator();
        // String entryPattern = "\t<manifest:file-entry manifest:full-path=\"{0}\"" +
        // " manifest:media-type=\"{1}\"/>\n";
        // pOut.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes());
        // pOut.write("<manifest:manifest xmlns:manifest=\"http://openoffice.org/2001/manifest\">\n".getBytes());
        // while (iter.hasNext()) {
        // Entry<String, FileType> entry = iter.next();
        // pOut.write(MessageFormat.format(entryPattern, entry.getKey(), entry.getValue().toString()).getBytes());
        // }
        // pOut.write("</manifest:manifest>\n".getBytes());
        // pOut.flush();
        write(new OutputStreamWriter(out));
        out.flush();
    }

    /**
     * Write the manifest file.
     *
     * @param pWriter
     *            the writer
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void write(Writer pWriter) throws IOException {
        Iterator<Entry<String, FileType>> iter = mEntries.entrySet().iterator();
        String entryPattern = "\t<manifest:file-entry manifest:full-path=\"{0}\"" + " manifest:media-type=\"{1}\"/>\n";
        pWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        pWriter.write("<manifest:manifest xmlns:manifest=\"http://openoffice.org/2001/manifest\">\n");
        while (iter.hasNext()) {
            Entry<String, FileType> entry = iter.next();
            pWriter.write(MessageFormat.format(entryPattern, entry.getKey(), entry.getValue().toString()));
        }
        pWriter.write("</manifest:manifest>\n");
        pWriter.flush();
    }

}
