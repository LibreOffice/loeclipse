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
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
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
import org.libreoffice.ide.eclipse.core.model.utils.IModelDataListener;
import org.libreoffice.ide.eclipse.core.model.utils.IModelTreeListener;

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

    private short mDirty = 0;
    private boolean mIsQuiet = false;
    private boolean mIsModified = false;
    private Vector<IModelDataListener> mDataListeners = new Vector<IModelDataListener>();
    private Vector<IModelTreeListener> mTreeListeners = new Vector<IModelTreeListener>();

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
            String msg = Messages.getString("PackagePropertiesModel.NullFileException"); //$NON-NLS-1$
            throw new IllegalArgumentException(msg); //$NON-NLS-1$
        }

        try {
            mProperties.load(is);
        } catch (IOException e) {
            String msg = Messages.getString("PackagePropertiesModel.FileReadException"); //$NON-NLS-1$
            PluginLogger.warning(msg + file.getLocation()); //$NON-NLS-1$
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
        Map<String, Boolean> paths = new HashMap<>();
        Map<IResource, Boolean> folders = new HashMap<>();
        mFiles = deserializeContent(folders, paths);
        mFolders = getFolderCheckState(folders);
        // If some resources are missing or modified, we need to log these resources.
        if (!paths.isEmpty()) {
            logMissingPaths(paths, true);
            logMissingPaths(paths, false);
            firePackageChanged();
            // the model is marked as dirty with pending changes.
            mDirty = 2;
        }
    }

    /**
     * Set whether the changes should be notified to the listeners or not.
     *
     * @param quiet
     *            <code>true</code> if the changes should be notified, <code>false</code> otherwise.
     */
    public void setQuiet(boolean quiet) {
        mIsQuiet = quiet;
    }

    /**
     * Set whether the changes should come from this data model or not.
     *
     * @param modified
     *            <code>true</code> if the changes come from this data model, <code>false</code> otherwise.
     */
    public void setModified(boolean modified) {
        mIsModified = modified;
    }

    /**
     * Get whether the changes come from this data model or not.
     *
     * @return <code>true</code> if the changes come from this data model, <code>false</code> otherwise.
     */
    public boolean isModified() {
        return mIsModified;
    }

    /**
     * Get whether the resource is hidden or not.
     *
     * @param res
     *            the resource to check.
     *
     * @return <code>true</code> if resource is hidden, <code>false</code> otherwise.
     */
    public boolean isHidden(IResource res) {
        return res.getName().startsWith("."); //$NON-NLS-1$
    }

    /**
     * @return <code>true</code> if the properties model has changed but isn't saved, <code>false</code> otherwise.
     */
    public boolean isDirty() {
        return mDirty != 0;
    }

    /**
     * Add a listener notified of the model changes.
     *
     * @param listener
     *            the listener to add.
     */
    public void addDataListener(IModelDataListener listener) {
        mDataListeners.add(listener);
    }

    /**
     * Removes a class listening the model changes.
     *
     * @param listener
     *            the listener to remove
     */
    public void removeDataListener(IModelDataListener listener) {
        if (mDataListeners.contains(listener)) {
            mDataListeners.remove(listener);
        }
    }

    /**
     * Add a listener notified of the TreeView changes.
     *
     * @param listener
     *            the listener to add.
     */
    public void addTreeListener(IModelTreeListener listener) {
        mTreeListeners.add(listener);
    }

    /**
     * Removes a class listening the TreeView changes.
     *
     * @param listener
     *            the listener to remove
     */
    public void removeTreeListener(IModelTreeListener listener) {
        if (mTreeListeners.contains(listener)) {
            mTreeListeners.remove(listener);
        }
    }


    /**IModelChangedListener
     * Notify that the package properties model has been saved.
     */
    public void firePackageSaved() {
        if (!mIsQuiet) {
            mDirty = 0;
            for (IModelDataListener listener : mDataListeners) {
                listener.modelSaved();
            }
        }
    }

    /**
     * Notify that the package properties model has changed.
     */
    public void firePackageChanged() {
        if (!mIsQuiet) {
            mDirty = 1;
            for (IModelDataListener listener : mDataListeners) {
                listener.modelChanged();
            }
        }
    }

    /**
     * Notify that the tree view must do a refresh.
     */
    public void fireTreeRefresh() {
        mDirty = 2;
        for (IModelTreeListener listener : mTreeListeners) {
            listener.modelRefreshed();
        }
    }

    /**
     * Writes the Package properties to the file.
     *
     * @throws Exception
     *             if the data can't be written
     *
     * @return <code>true</code> if pending change exist, <code>false</code> otherwise.
     */
    public boolean write() throws Exception {
        boolean hasChanges = hasPendingChanges();
        if (hasChanges) {
            mProperties.setProperty(CONTENTS, serializeContent());
        }
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
        return hasChanges;
    }

    /**
     * Clears all the content of the package properties and replace it by a string as if it would have been the
     * properties file content.
     *
     * @param content
     *            the string describing the data
     */
    public void reloadFromString(String content) {
        if (!content.equals(writeToString())) {
            mProperties.clear();
            try {
                mProperties.load(new StringReader(content));
            } catch (IOException e) {
                // Nothing to log
                return;
            }
            Map<IResource, Boolean> folders = new HashMap<>();
            mFiles.clear();
            mFiles.addAll(deserializeContent(folders, null));
            mFolders.clear();
            mFolders.putAll(getFolderCheckState(folders));

        }
    }

    /**
     * @return the content of the package properties under the form of a string
     *         as it would have been written to the file.
     */
    public String writeToString() {
        Writer writer = new StringWriter();
        try {
            mProperties.store(writer, Messages.getString("PackagePropertiesModel.Comment")); //$NON-NLS-1$
        } catch (IOException e) {
            // Nothing to log
        }
        return writer.toString();
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
     * @param res
     *            the resource (ie: file or folder) to add
     */
    public void addResource(IResource res) {
        // If it's a folder we need to add all children resources
        try {
            if (res.getType() == IResource.FOLDER) {
                addFolderResource(res);
            } else if (res.getType() == IResource.FILE) {
                addFileResource(res);
            }
            mProperties.setProperty(CONTENTS, serializeContent());
            mIsModified = true;
            firePackageChanged();
            mIsModified = false;
        } catch (CoreException e) {
            // Nothing to log
        }
    }

    /**
     * remove resource entry if already in.
     *
     * @param res
     *            the resource (ie: file or folder) to add
     */
    public void removeResource(IResource res) {
        // If it's a folder we need to remove all children resources to
        try {
            if (res.getType() == IResource.FOLDER) {
                removeFolderResource(res);
            } else if (res.getType() == IResource.FILE) {
                removeFileResource(res);
            }
            mProperties.setProperty(CONTENTS, serializeContent());
            mIsModified = true;
            firePackageChanged();
            mIsModified = false;
        } catch (CoreException e) {
            // Nothing to log
        }
    }

    /**
     * @return if resource is checked.
     *
     * @param res
     *            the resource (ie: file or folder) to check
     */
    public boolean isChecked(IResource res) {
        boolean checked = false;
        if (res.getType() == IResource.FILE) {
            checked = mFiles.contains(res);
        } else if (res.getType() == IResource.FOLDER) {
            checked = mFolders.containsKey(res);
        }
        return checked;
    }

    /**
     * @return if resource is grayed.
     *
     * @param res
     *            the resource (ie: file or folder) to check
     */
    public boolean isGrayed(IResource res) {
        boolean grayed = false;
        if (res.getType() == IResource.FOLDER) {
            grayed = mFolders.getOrDefault(res, false);
        }
        return grayed;
    }

    /**
     * @return the list of the the files and selected empty folders added to the package properties
     *          that are not dialog or basic libraries or package descriptions
     */
    public List<IResource> getContents() {
        List<IResource> contents = new ArrayList<>();
        contents.addAll(mFiles);
        try {
            for (Entry<IResource, Boolean> entry : mFolders.entrySet()) {
                if (entry.getValue()) {
                    continue;
                }
                IResource folder = entry.getKey();
                if (!hasVisibleMembers(folder)) {
                    contents.add(folder);
                }
            }
        } catch (CoreException e) {
            // Nothing to log
        }
        return contents;
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
            String msg = Messages.getString("PackagePropertiesModel.NoLocaleException"); //$NON-NLS-1$
            throw new IllegalArgumentException(msg); //$NON-NLS-1$
        }

        if (description == null || !description.exists()) {
            String msg = Messages.getString("PackagePropertiesModel.NoDescriptionFileException"); //$NON-NLS-1$
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
     * Get pending change status.
     *
     * @return <code>true</code> if pending change exist, <code>false</code> otherwise.
     */
    private boolean hasPendingChanges() {
        return mDirty == 2;
    }

    /**
     * Add all files that are members of a folder resource recursively.
     *
     * @param folder
     *            the folder resource
     */
    private void addFolderResource(IResource folder) throws CoreException {
        mFolders.put(folder, false);
        IResource[] members = ((IContainer) folder).members();
        for (IResource res : members) {
            if (res.getType() == IResource.FOLDER) {
                if (!isHidden(res)) {
                    addFolderResource(res);
                }
            } else if (!mFiles.contains(res)) {
                if (!isHidden(res)) {
                    mFiles.add(res);
                }
            }
        }
    }

    /**
     * Remove all files that are members of a folder resource recursively.
     *
     * @param folder
     *            the folder resource
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
     * @param file
     *            the folder resource
     */
    private void addFileResource(IResource file) throws CoreException {
        if (!mFiles.contains(file)) {
            mFiles.add(file);
        }
        setFileCheckState(file);
    }

    /**
     * Remove a files and updated folders.
     *
     * @param file
     *            the folder resource
     */
    private void removeFileResource(IResource file) throws CoreException {
        if (mFiles.contains(file)) {
            mFiles.remove(file);
        }
        setFileCheckState(file);
    }

    /**
     * Serialize all files resource to the package properties.
     *
     * @return a string of all files and empty folder.
     */
    private String serializeContent() {
        List<String> results = new ArrayList<>();
        int nbFiles = 0;
        int nbFolders = 0;
        try {
            for (IResource file : mFiles) {
                if (file.getType() == IResource.FILE && file.exists()) {
                    results.add(file.getProjectRelativePath().toString());
                    nbFiles++;
                }
            }
            for (Entry<IResource, Boolean> entry : mFolders.entrySet()) {
                nbFolders += serializeFolder(results, entry);
            }

        } catch (CoreException e) {
            e.printStackTrace();
        }
        String template = Messages.getString("PackagePropertiesModel.SerializeContent"); //$NON-NLS-1$
        PluginLogger.info(String.format(template, nbFiles, nbFolders)); //$NON-NLS-1$
        Collections.sort(results, String.CASE_INSENSITIVE_ORDER);
        return String.join(SEPARATOR, results);
    }

    private int serializeFolder(List<String> results, Entry<IResource, Boolean> entry) throws CoreException {
        // Only empty folder will be saved if checked
        int nbFolders = 0;
        IResource folder = entry.getKey();
        if (folder.getType() == IResource.FOLDER && folder.exists() && !isHidden(folder)) {
            if (!entry.getValue() && !hasVisibleMembers(folder)) {
                results.add(folder.getProjectRelativePath().toString());
                nbFolders++;
            }
        }
        return nbFolders;
    }

    /**
     * De-serialize all files resource from the package properties.
     *
     * @param folders
     *            the folder
     *
     * @param paths
     *            the path of missing resource
     *
     * @return a list of the file resource
     */
    private List<IResource> deserializeContent(Map<IResource, Boolean> folders, Map<String, Boolean> paths) {
        List<IResource> files = new ArrayList<>();
        IProject prj = mPropertiesFile.getProject();
        int nbFiles = 0;
        int nbFolders = 0;
        try {
            String contents = mProperties.getProperty(CONTENTS);

            if (contents != null && !contents.equals("")) { //$NON-NLS-1$
                for (String path : contents.split(SEPARATOR)) {
                    if (prj.getFile(path).exists()) {
                        IFile file = prj.getFile(path);
                        if (!files.contains(file)) {
                            files.add(file);
                            nbFiles++;
                        }
                    } else if (prj.getFolder(path).exists()) {
                        nbFolders += deserializeFolder(prj, folders, paths, path);
                    } else if (paths != null) {
                        paths.put(path, true);
                    }
                }
            }
        } catch (NullPointerException | CoreException e) {
            // Nothing to do nor return
        }
        String template = Messages.getString("PackagePropertiesModel.DeserializeContent"); //$NON-NLS-1$
        PluginLogger.info(String.format(template, nbFiles, nbFolders)); //$NON-NLS-1$
        return files;
    }

    private int deserializeFolder(IProject prj, Map<IResource, Boolean> folders,
                                  Map<String, Boolean> paths, String path) throws CoreException {
        // Only empty folder will be restored
        int nbFolders = 0;
        IFolder folder = prj.getFolder(path);
        if (!hasVisibleMembers(folder)) {
            folders.put(folder, false);
            nbFolders++;
        } else if (paths != null) {
            paths.put(path, false);
        }
        return nbFolders;
    }

        /**
     * Get project folder check state from file resources.
     *
     * @param folders
     *            the folders (ie: map <IResource, Boolean>)
     *
     * @return a map of the folder resource / check state
     */
    private Map<IResource, Boolean> getFolderCheckState(Map<IResource, Boolean> folders) {
        try {
            IContainer parent = mPropertiesFile.getProject();
            setFolderCheckState(folders, parent);
        } catch (CoreException e) {
            // Nothing to do nor return
        }
        return folders;
    }

    /**
     * Set folder check state from files resource.
     *
     * @param folders
     *            the folders (ie: map <IResource, Boolean>)
     *
     * @param parent
     *            the parent resource
     */
    private void setFolderCheckState(Map<IResource, Boolean> folders, IContainer parent) throws CoreException {
        IResource[] members = parent.members();
        boolean all = true;
        boolean any = false;
        for (IResource res : members) {
            // We need to consider only non-hidden resource
            if (isHidden(res)) {
                continue;
            }
            if (res.getType() == IResource.FILE) {
                if (mFiles.contains(res)) {
                    any = true;
                } else {
                    all = false;
                }
            } else if (res.getType() == IResource.FOLDER) {
                setFolderCheckState(folders, (IContainer) res);
                if (!folders.containsKey(res)) {
                    all = false;
                } else if (folders.get(res)) {
                    any = true;
                }
            }
        }
        if (isCheckStateSetable(folders, parent)) {
            if (members.length == 0) {
                all = false;
            }
            setCheckState(folders, parent, all, any);
        }
    }

    private boolean isCheckStateSetable(Map<IResource, Boolean> folders, IContainer res) {
        // We must exclude empty folders already present otherwise they will be lost
        return res.getType() != IResource.PROJECT && !folders.containsKey(res);
    }

    private void setFileCheckState(IResource file) throws CoreException {
        IContainer parent = file.getParent();
        while (parent.getType() != IResource.PROJECT) {
            setParentCheckState(parent);
            parent = parent.getParent();
        }
    }

    /**
     * Set parent check state from files resource.
     *
     * @param parent
     *            the parent resource
     */
    private void setParentCheckState(IContainer parent) throws CoreException {
        IResource[] members = parent.members();
        boolean all = true;
        boolean any = false;
        for (IResource res : members) {
            // We need to consider only non-hidden resource
            if (isHidden(res)) {
                continue;
            }
            if (res.getType() == IResource.FILE) {
                if (mFiles.contains(res)) {
                    any = true;
                } else {
                    all = false;
                }
            } else if (res.getType() == IResource.FOLDER) {
                if (!mFolders.containsKey(res)) {
                    all = false;
                } else if (mFolders.get(res)) {
                    any = true;
                }
            }
        }
        if (members.length == 0) {
            all = false;
        }
        setCheckState(mFolders, parent, all, any);
    }

    private boolean hasVisibleMembers(IResource folder) throws CoreException {
        boolean hasMembers = false;
        IResource[] members = ((IContainer) folder).members();
        for (IResource res : members) {
            if (!isHidden(res)) {
                hasMembers = true;
                break;
            }
        }
        return hasMembers;
    }

    private void setCheckState(Map<IResource, Boolean> folders, IResource parent, boolean all, boolean any) {
        if (all || any) {
            folders.put(parent, any && !all);
        } else if (folders.containsKey(parent)) {
            folders.remove(parent);
        }
    }

    private void logMissingPaths(Map<String, Boolean> paths, boolean missing) {
        List<String> resources = new ArrayList<>();
        for (Entry<String, Boolean> entry : paths.entrySet()) {
            if (entry.getValue() == missing) {
                resources.add(entry.getKey());
            }
        }
        if (!resources.isEmpty()) {
            String template;
            if (missing) {
                template = Messages.getString("PackagePropertiesModel.DeserializeAsMissingResource"); //$NON-NLS-1$
            } else {
                template = Messages.getString("PackagePropertiesModel.DeserializeAsModifiedResource"); //$NON-NLS-1$
            }
            String msg = String.join(SEPARATOR, resources.toArray(new String[0])); //$NON-NLS-1$
            PluginLogger.warning(String.format(template, msg)); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}
