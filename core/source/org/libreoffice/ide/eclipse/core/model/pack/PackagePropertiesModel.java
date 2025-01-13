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
package org.libreoffice.ide.eclipse.core.model.pack;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.Messages;
import org.libreoffice.ide.eclipse.core.model.utils.IModelChangedListener;

/**
 *
 */
public class PackagePropertiesModel {

    private static final String CONTENTS = "contents"; //$NON-NLS-1$
    private static final String BASICLIBS = "basicLibs"; //$NON-NLS-1$
    private static final String DIALOGLIBS = "dialogLibs"; //$NON-NLS-1$
    private static final String DESCRIPTION = "description"; //$NON-NLS-1$
    private static final String SEPARATOR = ", "; //$NON-NLS-1$

    private IFile mPropertiesFile;
    private Properties mProperties = new Properties();
    private List<IResource> mFiles = null;
    private Map<IResource, Boolean> mFolders = null;

    private boolean mIsDirty = false;
    private boolean mIsQuiet = false;
    private Vector<IModelChangedListener> mListeners = new Vector<IModelChangedListener>();

    /**
     * Create a new package.properties model for a given file. If the file can be read, the existing properties will be
     * imported.
     *
     * @param file
     *            the package.properties file represented by the object.
     * @throws IllegalArgumentException
     *             if the file is <code>null</code>
     */
    public PackagePropertiesModel(IFile file) throws IllegalArgumentException {

        FileInputStream is = null;

        try {
            is = new FileInputStream(file.getLocation().toFile());
            mPropertiesFile = file;
        } catch (FileNotFoundException e) {
            mPropertiesFile = null;
            String msg = Messages.getString("PackagePropertiesModel.NullFileException");
            throw new IllegalArgumentException(msg); //$NON-NLS-1$
        }

        try {
            mProperties.load(is);
        } catch (IOException e) {
            PluginLogger.warning(Messages.getString("PackagePropertiesModel.FileReadException")
                + file.getLocation()); //$NON-NLS-1$
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
            mFiles = deserializeContent();
            mFolders = getFolderCheckState();
        }
    }

    /**
     * Set whether the changes should be notified to the listeners or not.
     *
     * @param pQuiet
     *            <code>true</code> if the changes should be notified, <code>false</code> otherwise.
     */
    public void setQuiet(boolean pQuiet) {
        mIsQuiet = pQuiet;
    }

    /**
     * Add a listener notified of the model changes.
     *
     * @param listener
     *            the listener to add.
     */
    public void addChangeListener(IModelChangedListener listener) {
        mListeners.add(listener);
    }

    /**
     * Removes a class listening the model changes.
     *
     * @param listener
     *            the listener to remove
     */
    public void removeChangedListener(IModelChangedListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

    /**
     * Notify that the package properties model has been saved.
     */
    public void firePackageSaved() {
        if (!mIsQuiet) {
            mIsDirty = false;
            for (IModelChangedListener listener : mListeners) {
                listener.modelSaved();
            }
        }
    }

    /**
     * Notify that the package properties model has changed.
     */
    public void firePackageChanged() {
        if (!mIsQuiet) {
            mIsDirty = true;
            for (IModelChangedListener listener : mListeners) {
                listener.modelChanged();
            }
        }
    }

    /**
     * @return <code>true</code> if the properties model has changed but isn't saved, <code>false</code> otherwise.
     */
    public boolean isDirty() {
        return mIsDirty;
    }

    /**
     * Writes the Package properties to the file.
     *
     * @return the content of the package properties under the form of a string
     *         as it would have been written to the file.
     *
     * @throws Exception
     *             if the data can't be written
     */
    public String write() throws Exception {
        String content = writeToString();
        FileOutputStream os = new FileOutputStream(mPropertiesFile.getLocation().toFile());
        try {
            mProperties.store(os, Messages.getString("PackagePropertiesModel.Comment")); //$NON-NLS-1$
            firePackageSaved();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                os.close();
            } catch (Exception e) {
                // Nothing to log
            }
        }
        return content;
    }

    /**
     * Clears all the content of the package properties and replace it by a string as if it would have been the
     * properties file content.
     *
     * @param content
     *            the string describing the data
     */
    public void reloadFromString(String content) {
        String initContent = writeToString();
        if (!content.equals(initContent)) {
            mProperties.clear();
            try {
                mProperties.load(new StringReader(content));
            } catch (IOException e) {
                // Nothing to log
                return;
            } 
            mFiles.clear(); 
            mFiles.addAll(deserializeContent());
            mFolders.clear();
            mFolders.putAll(getFolderCheckState());

            firePackageChanged();
        }
    }

    /**
     * @return the content of the package properties under the form of a string
     *         as it would have been written to the file.
     */
    public String writeToString() {
        String fileContent = ""; //$NON-NLS-1$
        mProperties.setProperty(CONTENTS, serializeContent());
        Set<Entry<Object, Object>> entries = mProperties.entrySet();
        Iterator<Entry<Object, Object>> iter = entries.iterator();
        while (iter.hasNext()) {
            Entry<Object, Object> entry = iter.next();
            fileContent += (String) entry.getKey() + "=" + (String) entry.getValue() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return fileContent;
    }

    /**
     * Adds a Basic library folder to the package.
     *
     * @param libFolder
     *            the library folder to add
     * @throws IllegalArgumentException
     *             is thrown if the argument is <code>null</code>
     */
    public void addBasicLibrary(IFolder libFolder) throws IllegalArgumentException {

        String libs = mProperties.getProperty(BASICLIBS);
        if (libs == null) {
            libs = ""; //$NON-NLS-1$
        }

        try {
            if (!libs.equals("")) { //$NON-NLS-1$
                libs += SEPARATOR; //$NON-NLS-1$
            }
            libs += libFolder.getProjectRelativePath().toString();
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        mProperties.setProperty(BASICLIBS, libs);
        firePackageChanged();
    }

    /**
     * Adds a basic dialog library folder to the package.
     *
     * @param libFolder
     *            the library folder to add
     * @throws IllegalArgumentException
     *             is thrown if the argument is <code>null</code>
     */
    public void addDialogLibrary(IFolder libFolder) throws IllegalArgumentException {
        String libs = mProperties.getProperty(DIALOGLIBS);
        if (libs == null) {
            libs = ""; //$NON-NLS-1$
        }

        try {
            if (!libs.equals("")) { //$NON-NLS-1$
                libs += SEPARATOR; //$NON-NLS-1$
            }
            libs += libFolder.getProjectRelativePath().toString();
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        mProperties.setProperty(DIALOGLIBS, libs);
        firePackageChanged();
    }

    /**
     * @return the list of the dialog libraries addedd to the package properties
     */
    public List<IFolder> getDialogLibraries() {

        ArrayList<IFolder> result = new ArrayList<IFolder>();

        try {
            String libs = mProperties.getProperty(DIALOGLIBS);
            IProject prj = mPropertiesFile.getProject();

            if (libs != null && !libs.equals("")) { //$NON-NLS-1$
                String[] fileNames = libs.split(SEPARATOR); //$NON-NLS-1$
                for (String fileName : fileNames) {
                    result.add(prj.getFolder(fileName));
                }
            }
        } catch (NullPointerException e) {
            // Nothing to do nor return
        }
        return result;
    }

    /**
     * @return the list of the basic libraries addedd to the package properties
     */
    public List<IFolder> getBasicLibraries() {
        ArrayList<IFolder> result = new ArrayList<IFolder>();

        try {
            String libs = mProperties.getProperty(BASICLIBS);
            IProject prj = mPropertiesFile.getProject();

            if (libs != null && !libs.equals("")) { //$NON-NLS-1$
                String[] fileNames = libs.split(SEPARATOR); //$NON-NLS-1$
                for (String fileName : fileNames) {
                    result.add(prj.getFolder(fileName));
                }
            }
        } catch (NullPointerException e) {
            // Nothing to do nor return
        }
        return result;
    }

    /**
     * Removes all the basic libraries from the package properties.
     */
    public void clearBasicLibraries() {
        mProperties.setProperty(BASICLIBS, ""); //$NON-NLS-1$
        firePackageChanged();
    }

    /**
     * Removes all the dialog libraries from the package properties.
     */
    public void clearDialogLibraries() {
        mProperties.setProperty(DIALOGLIBS, ""); //$NON-NLS-1$
        firePackageChanged();
    }

    /**
     * add resource entry if not already in.
     *
     * @param pRes the resource entry to add
     */
    public void addResource(IResource pRes) {
        // If it's a folder we need to add all children resources
        try {
            if (pRes.getType() == IResource.FOLDER) {
                addFolderResource(pRes);
            } else if (!mFiles.contains(pRes)) {
                addFileResource(pRes);
            }
            firePackageChanged();
        } catch (CoreException e) {
            // Log ?
        }
    }

    /**
     * remove resource entry if already in.
     *
     * @param pRes the resource entry to remove
     */
    public void removeResource(IResource pRes) {
        // If it's a folder we need to remove all children resources to
        try {
            if (pRes.getType() == IResource.FOLDER) {
                removeFolderResource(pRes);
            } else if (mFiles.contains(pRes)) {
                removeFileResource(pRes);
            }
            firePackageChanged();
        } catch (CoreException e) {
            // Log ?
        }
    }

    /**
     * @param pRes the resource entry to check
     *
     * @return if resource is checked
     */
    public boolean isChecked(IResource pRes) {
        boolean checked = false;
        if (pRes.getType() == IResource.FILE) {
            checked = mFiles.contains(pRes);
        } else if (pRes.getType() == IResource.FOLDER) {
            checked = mFolders.containsKey(pRes);
        }
        return checked;
    }

    /**
     * @param pRes the resource entry to check
     *
     * @return if resource is grayed
     */
    public boolean isGrayed(IResource pRes) {
        boolean grayed = false;
        if (pRes.getType() == IResource.FOLDER) {
            grayed = mFolders.getOrDefault(pRes, false);
        }
        return grayed;
    }

    /**
     * @return the list of the the files and directories added to the package properties that are not dialog or basic
     *         libraries or package descriptions
     */
    public List<IResource> getContents() {
        return mFiles;
    }

    /**
     * Removes all the file and directories from the package properties that has been added using
     * {@link #addResource(IResource)}.
     */
    public void clearContents() {
        mProperties.setProperty(CONTENTS, ""); //$NON-NLS-1$
        mFiles.clear();
        mFolders.clear();
        firePackageChanged();
    }

    /**
     * Adds a localized package description file. The description file has to exist and the locale can't be
     * <code>null</code>.
     *
     * @param description
     *            the description file
     * @param locale
     *            the file locale.
     *
     * @throws IllegalArgumentException
     *             is thrown if the file is <code>null</code> or doesn't exists or if the locale is <code>null</code>.
     */
    public void addDescriptionFile(IFile description, Locale locale) throws IllegalArgumentException {

        if (locale == null) {
            String msg = Messages.getString("PackagePropertiesModel.NoLocaleException");
            throw new IllegalArgumentException(msg); //$NON-NLS-1$
        }

        if (description == null || !description.exists()) {
            String msg = Messages.getString("PackagePropertiesModel.NoDescriptionFileException");
            throw new IllegalArgumentException(msg); //$NON-NLS-1$
        }

        String countryName = ""; //$NON-NLS-1$
        if (locale.getCountry() != "") { //$NON-NLS-1$
            countryName = "_" + locale.getCountry(); //$NON-NLS-1$
        }

        String propertyName = DESCRIPTION + "-" + locale.getLanguage() + countryName; //$NON-NLS-1$
        mProperties.setProperty(propertyName, description.getProjectRelativePath().toString());
        firePackageChanged();
    }

    /**
     * @return a map of the description files accessed by their locale. There is no support of a default locale.
     */
    public Map<Locale, IFile> getDescriptionFiles() {
        Map<Locale, IFile> descriptions = new HashMap<>();
        IProject prj = mPropertiesFile.getProject();

        Iterator<Object> keys = mProperties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String regex = DESCRIPTION + "-([a-zA-Z]{2})(?:_([a-zA-Z]{2}))?"; //$NON-NLS-1$
            Matcher matcher = Pattern.compile(regex).matcher(key);
            if (matcher.matches()) {
                String language = matcher.group(1);
                String country = matcher.group(2);

                Locale locale = new Locale(language);
                if (country != null) {
                    locale = new Locale(language, country);
                }

                IFile file = prj.getFile(mProperties.getProperty(key));

                if (file != null) {
                    descriptions.put(locale, file);
                }
            }
        }
        return descriptions;
    }

    /**
     * Removes all the description files from the package properties.
     */
    public void clearDescriptions() {
        int nbRemoved = 0;

        Iterator<Object> keys = ((Properties) mProperties.clone()).keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String regex = DESCRIPTION + "-([a-zA-Z]{2})(?:_([a-zA-Z]{2}))?"; //$NON-NLS-1$
            Matcher matcher = Pattern.compile(regex).matcher(key);
            if (matcher.matches()) {
                mProperties.remove(key);
                nbRemoved++;
            }
        }

        if (nbRemoved > 0) {
            firePackageChanged();
        }
    }

    /**
     * Add all files that are members of a folder resource recursively.
     *
     * @param folder the resource folder entry
     *
     */
    private void addFolderResource(IResource folder) throws CoreException {
        mFolders.put(folder, false);
        IResource[] members = ((IContainer) folder).members();
        for (IResource res :members) {
            if (res.getType() == IResource.FOLDER) {
                addFolderResource(res);
            } else if (!mFiles.contains(res)) {
                mFiles.add(res);
            }
        }
    }

    /**
     * Remove all files that are members of a folder resource recursively.
     *
     * @param folder the resource folder entry
     *
     */
    private void removeFolderResource(IResource folder) throws CoreException {
        if (mFolders.containsKey(folder)) {
            mFolders.remove(folder);
        }
        IResource[] members = ((IContainer) folder).members();
        for (IResource res : members) {
            if (res.getType() == IResource.FOLDER) {
                removeFolderResource(res);
            } else if (mFiles.contains(res)) {
                mFiles.remove(res);
            }
        }
    }

    /**
     * Add a files and updated folders.
     *
     * @param file the resource file entry
     *
     */
    private void addFileResource(IResource file) throws CoreException {
        mFiles.add(file);
        IProject prj = mPropertiesFile.getProject();
        IContainer parent = file.getParent();
        while (parent != null && parent != prj) {
            parent = getParentCheckState(parent);
        }
    }

    /**
     * Remove a files and updated folders.
     *
     * @param file the resource file entry
     *
     */
    private void removeFileResource(IResource file) throws CoreException {
        if (mFiles.contains(file)) {
            mFiles.remove(file);
        }
        IProject prj = mPropertiesFile.getProject();
        IContainer parent = file.getParent();
        while (parent != null && parent != prj) {
            if (mFolders.containsKey(parent)) {
                if (parent.members().length == 1) {
                    mFolders.remove(parent);
                } else {
                    mFolders.put(parent, true);
                }
            }
            parent = parent.getParent();
        }
    }

    /**
     * Serialize all files resource to the package properties.
     *
     * @return a string corresponding to the value of the contents property of the package.properties file
     */
    private String serializeContent() {
        List<String> result = new ArrayList<>();
        for (IResource res : mFiles) {
            if (res.getType() == IResource.FILE && res.exists()) {
                result.add(res.getProjectRelativePath().toString());
            }
        }
        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return String.join(SEPARATOR, result);
    }

    /**
     * De-serialize all files resource from the package properties.
     *
     * @return a list of resource corresponding to the value of the contents property of the package.properties file
     */
    private List<IResource> deserializeContent() {
        List<IResource> resources = new ArrayList<>();
        try {
            String libs = mProperties.getProperty(CONTENTS);
            IProject prj = mPropertiesFile.getProject();

            if (libs != null && !libs.equals("")) { //$NON-NLS-1$
                for (String path : libs.split(SEPARATOR)) {
                    if (prj.getFile(path).exists()) {
                        resources.add(prj.getFile(path));
                    }
                }
            }
        } catch (NullPointerException e) {
            // Nothing to do nor return
        }
        return resources;
    }

    /**
     * Get project check state from files resource.
     *
     * @return a map of resource / boolean corresponding to the folder resources
     */
    private Map<IResource, Boolean> getFolderCheckState() {
        Map<IResource, Boolean> resources = new HashMap<>();
        try {
            IResource [] members = mPropertiesFile.getProject().members();
            for (IResource res : members) {
                if (res.getType() == IResource.FOLDER) {
                    getSubFolderCheckState(resources, res);
                }
            }
        } catch (NullPointerException | CoreException e) {
            // Nothing to do nor return
        }
        return resources;
    }

    /**
     * Get folder check state from files resource.
     *
     * @param folders the map resource / boolean to update
     *
     * @param parent the folder resource entry
     */
    private void getSubFolderCheckState(Map<IResource, Boolean> folders, IResource parent) throws CoreException {
        IResource[] members = ((IContainer) parent).members();
        boolean checked = true;
        boolean grayed = false;
        for (IResource res : members) {
            if (res.getType() == IResource.FOLDER) {
                getSubFolderCheckState(folders, res);
            } else if (mFiles.contains(res)) {
                grayed = true;
            } else {
                checked = false;
            }
        }
        if (members.length == 0) {
            folders.put(parent, false);
        } else if (checked || grayed) {
            folders.put(parent, grayed && !checked);
        }
    }

    /**
     * Get parent check state from files resource.
     *
     * @param pParent the folder resource entry
     *
     * @return the parent resource of the folder resource entry or null
     */
    private IContainer getParentCheckState(IResource pParent) throws CoreException {
        IResource[] members = ((IContainer) pParent).members();
        boolean checked = true;
        boolean grayed = false;
        for (IResource res : members) {
            if (res.getType() == IResource.FOLDER) {
                if (mFolders.containsKey(res)) {
                    grayed = mFolders.get(res);
                } else {
                    checked = false;
                }
            } else if (mFiles.contains(res)) {
                grayed = true;
            } else {
                checked = false;
            }
        }
        if (members.length == 0) {
            mFolders.put(pParent, false);
        } else if (checked || grayed) {
            mFolders.put(pParent, grayed && !checked);
        }
        return pParent.getParent();
    }

}
