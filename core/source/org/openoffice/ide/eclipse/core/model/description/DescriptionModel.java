/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.openoffice.ide.eclipse.core.model.description;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.openoffice.ide.eclipse.core.model.utils.IModel;
import org.openoffice.ide.eclipse.core.model.utils.IModelChangedListener;
import org.openoffice.plugin.core.utils.XMLWriter;

/**
 * Class representing the description.xml file.
 *
 * @author Cédric Bosdonnat
 *
 */
public class DescriptionModel implements IModel {

    private ArrayList<IModelChangedListener> mListeners;
    private boolean mDirty;
    private boolean mSuspendEvents;

    private String mId = new String();
    private String mVersion = new String();
    private HashMap<Locale, String> mDisplayNames;

    private String mMinOOo = new String();
    private String mMaxOOo = new String();
    private String mPlatforms = new String();

    private String mDefaultIcon = new String();
    private String mHCIcon = new String();

    private HashMap<Locale, String> mDescriptions;

    private HashMap<Locale, String> mReleaseNotes;
    private ArrayList<String> mUpdateInfos;

    private boolean mAcceptByUser = false;
    private boolean mSuppressOnUpdate = false;
    private HashMap<Locale, String> mLicenses;

    private HashMap<Locale, PublisherInfos> mPublisherInfos;

    /**
     * Default constructor.
     */
    public DescriptionModel() {
        mListeners = new ArrayList<IModelChangedListener>();

        mDisplayNames = new HashMap<Locale, String>();
        mDescriptions = new HashMap<Locale, String>();
        mReleaseNotes = new HashMap<Locale, String>();
        mUpdateInfos = new ArrayList<String>();
        mLicenses = new HashMap<Locale, String>();
        mPublisherInfos = new HashMap<Locale, PublisherInfos>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(IModelChangedListener pListener) {
        mListeners.add(pListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(IModelChangedListener pListener) {
        mListeners.remove(pListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirty() {
        return mDirty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSuspendEvent(boolean pSuspend) {
        mSuspendEvents = pSuspend;
    }

    /**
     * @return all the locales defined in the different parts of the model.
     */
    public ArrayList<Locale> getAllLocales() {
        ArrayList<Locale> locales = new ArrayList<Locale>();

        appendNew(locales, mDisplayNames.keySet());
        appendNew(locales, mDescriptions.keySet());
        appendNew(locales, mReleaseNotes.keySet());
        appendNew(locales, mLicenses.keySet());
        appendNew(locales, mPublisherInfos.keySet());

        return locales;
    }

    /**
     * @return the id
     */
    public String getId() {
        return mId;
    }

    /**
     * @param pId
     *            the id to set
     */
    public void setId(String pId) {
        if (mId != pId) {
            mId = pId;
            fireModelChanged();
        }
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return mVersion;
    }

    /**
     * @param pVersion
     *            the version to set
     */
    public void setVersion(String pVersion) {
        if (mVersion != pVersion) {
            mVersion = pVersion;
            fireModelChanged();
        }
    }

    /**
     * @return the displayNames
     */
    public HashMap<Locale, String> getDisplayNames() {
        if (mDisplayNames == null) {
            mDisplayNames = new HashMap<Locale, String>();
        }
        return mDisplayNames;
    }

    /**
     * Add or update a display name.
     *
     * @param pLocale
     *            the locale for the display name to add / update
     * @param pValue
     *            the value to add / update.
     */
    public void addDisplayName(Locale pLocale, String pValue) {
        if (getDisplayNames().containsKey(pLocale) && !getDisplayNames().get(pLocale).equals(pValue)
            || !getDisplayNames().containsKey(pLocale)) {
            getDisplayNames().put(pLocale, pValue);
            fireModelChanged();
        }
    }

    /**
     * Remove a display name.
     *
     * @param pLocale
     *            the locale of the display name to remove
     */
    public void removeDisplayName(Locale pLocale) {
        if (getDisplayNames().containsKey(pLocale)) {
            getDisplayNames().remove(pLocale);
            fireModelChanged();
        }
    }

    /**
     * @return the minOOo
     */
    public String getMinOOo() {
        return mMinOOo;
    }

    /**
     * @param pMinOOo
     *            the minOOo to set
     */
    public void setMinOOo(String pMinOOo) {
        if (mMinOOo != pMinOOo) {
            mMinOOo = pMinOOo;
            fireModelChanged();
        }
    }

    /**
     * @return the maxOOo
     */
    public String getMaxOOo() {
        return mMaxOOo;
    }

    /**
     * @param pMaxOOo
     *            the maxOOo to set
     */
    public void setMaxOOo(String pMaxOOo) {
        if (mMaxOOo != pMaxOOo) {
            mMaxOOo = pMaxOOo;
            fireModelChanged();
        }
    }

    /**
     * @return the platforms
     */
    public String getPlatforms() {
        return mPlatforms;
    }

    /**
     * @param pPlatforms
     *            the platforms to set
     */
    public void setPlatforms(String pPlatforms) {
        if (mPlatforms != pPlatforms) {
            mPlatforms = pPlatforms;
            fireModelChanged();
        }
    }

    /**
     * @return the defaultIcon
     */
    public String getDefaultIcon() {
        return mDefaultIcon;
    }

    /**
     * @param pDefaultIcon
     *            the defaultIcon to set
     */
    public void setDefaultIcon(String pDefaultIcon) {
        if (mDefaultIcon != pDefaultIcon) {
            mDefaultIcon = pDefaultIcon;
            fireModelChanged();
        }
    }

    /**
     * @return the hCIcon
     */
    public String getHCIcon() {
        return mHCIcon;
    }

    /**
     * @param pHCIcon
     *            the hCIcon to set
     */
    public void setHCIcon(String pHCIcon) {
        if (mHCIcon != pHCIcon) {
            mHCIcon = pHCIcon;
            fireModelChanged();
        }
    }

    /**
     * @return the descriptions
     */
    public HashMap<Locale, String> getDescriptions() {
        if (mDescriptions == null) {
            mDescriptions = new HashMap<Locale, String>();
        }
        return mDescriptions;
    }

    /**
     * Add or update a description.
     *
     * @param pLocale
     *            the locale for the description to add / update
     * @param pValue
     *            the value to add / update.
     */
    public void addDescription(Locale pLocale, String pValue) {
        if (getDescriptions().containsKey(pLocale) && !getDescriptions().get(pLocale).equals(pValue)
            || !getDescriptions().containsKey(pLocale)) {
            getDescriptions().put(pLocale, pValue);
            fireModelChanged();
        }
    }

    /**
     * Remove a description.
     *
     * @param pLocale
     *            the locale of the description to remove
     */
    public void removeDescription(Locale pLocale) {
        if (getDescriptions().containsKey(pLocale)) {
            getDescriptions().remove(pLocale);
            fireModelChanged();
        }
    }

    /**
     * @return the releaseNotes
     */
    public HashMap<Locale, String> getReleaseNotes() {
        if (mReleaseNotes == null) {
            mReleaseNotes = new HashMap<Locale, String>();
        }
        return mReleaseNotes;
    }

    /**
     * Add or update a release note.
     *
     * @param pLocale
     *            the locale for the release note to add / update
     * @param pValue
     *            the value to add / update.
     */
    public void addReleaseNote(Locale pLocale, String pValue) {
        if (getReleaseNotes().containsKey(pLocale) && !getReleaseNotes().get(pLocale).equals(pValue)
            || !getReleaseNotes().containsKey(pLocale)) {
            getReleaseNotes().put(pLocale, pValue);
            fireModelChanged();
        }
    }

    /**
     * Remove a release note.
     *
     * @param pLocale
     *            the locale of the release note to remove
     */
    public void removeReleaseNote(Locale pLocale) {
        if (getReleaseNotes().containsKey(pLocale)) {
            getReleaseNotes().remove(pLocale);
            fireModelChanged();
        }
    }

    /**
     * @return the updateInfos
     */
    public ArrayList<String> getUpdateInfos() {
        if (mUpdateInfos == null) {
            mUpdateInfos = new ArrayList<String>();
        }
        return mUpdateInfos;
    }

    /**
     * Add or update a updateInfos.
     *
     * @param pValue
     *            the value to add / update.
     */
    public void addUpdateInfo(String pValue) {
        if (!getUpdateInfos().contains(pValue)) {
            getUpdateInfos().add(pValue);
            fireModelChanged();
        }
    }

    /**
     * Replace an existing update info.
     *
     * @param pIndex
     *            the index to change
     * @param pValue
     *            the new value
     */
    public void replaceUpdateInfo(int pIndex, String pValue) {
        String o = getUpdateInfos().get(pIndex);
        if (o != null && !o.equals(pValue)) {
            getUpdateInfos().set(pIndex, pValue);
            fireModelChanged();
        }
    }

    /**
     * Remove a updateInfos.
     *
     * @param pValue
     *            the value to remove
     */
    public void removeUpdateInfo(String pValue) {
        if (getUpdateInfos().contains(pValue)) {
            getUpdateInfos().remove(pValue);
            fireModelChanged();
        }
    }

    /**
     * @return the acceptByUser
     */
    public boolean isAcceptByUser() {
        return mAcceptByUser;
    }

    /**
     * @param pAcceptByUser
     *            the acceptByUser to set
     */
    public void setAcceptByUser(boolean pAcceptByUser) {
        if (mAcceptByUser != pAcceptByUser) {
            mAcceptByUser = pAcceptByUser;
            fireModelChanged();
        }
    }

    /**
     * @return the suppressOnUpdate
     */
    public boolean isSuppressOnUpdate() {
        return mSuppressOnUpdate;
    }

    /**
     * @param pSuppressOnUpdate
     *            the suppressOnUpdate to set
     */
    public void setSuppressOnUpdate(boolean pSuppressOnUpdate) {
        if (mSuppressOnUpdate != pSuppressOnUpdate) {
            mSuppressOnUpdate = pSuppressOnUpdate;
            fireModelChanged();
        }
    }

    /**
     * @return the licenses
     */
    public HashMap<Locale, String> getLicenses() {
        if (mLicenses == null) {
            mLicenses = new HashMap<Locale, String>();
        }
        return mLicenses;
    }

    /**
     * Add or update a license.
     *
     * @param pLocale
     *            the locale for the license to add / update
     * @param pValue
     *            the value to add / update.
     */
    public void addLicense(Locale pLocale, String pValue) {
        if (getLicenses().containsKey(pLocale) && !getLicenses().get(pLocale).equals(pValue)
            || !getLicenses().containsKey(pLocale)) {
            getLicenses().put(pLocale, pValue);
            fireModelChanged();
        }
    }

    /**
     * Remove a license.
     *
     * @param pLocale
     *            the locale of the license to remove
     */
    public void removeLicense(Locale pLocale) {
        if (getLicenses().containsKey(pLocale)) {
            getLicenses().remove(pLocale);
            fireModelChanged();
        }
    }

    /**
     * @return the publisherInfos
     */
    public HashMap<Locale, PublisherInfos> getPublisherInfos() {
        if (mPublisherInfos == null) {
            mPublisherInfos = new HashMap<Locale, PublisherInfos>();
        }
        return mPublisherInfos;
    }

    /**
     * Add or update a publisherInfo.
     *
     * @param pLocale
     *            the locale for the publisherInfo to add / update
     * @param pValue
     *            the value to add / update.
     */
    public void addPublisherInfo(Locale pLocale, PublisherInfos pValue) {
        if (getPublisherInfos().containsKey(pLocale) && !getPublisherInfos().get(pLocale).equals(pValue)
            || !getPublisherInfos().containsKey(pLocale)) {
            pValue.setModel(this);
            getPublisherInfos().put(pLocale, pValue);
            fireModelChanged();
        }
    }

    /**
     * Remove a publisherInfo.
     *
     * @param pLocale
     *            the locale of the publisherInfo to remove
     */
    public void removePublisherInfo(Locale pLocale) {
        if (getLicenses().containsKey(pLocale)) {
            getPublisherInfos().remove(pLocale);
            fireModelChanged();
        }
    }

    /**
     * Serializes the data in XML to an output stream.
     *
     * @param pOut
     *            the output stream where to write the data
     */
    public void serialize(OutputStream pOut) {
        XMLWriter writer = null;
        try {
            writer = new XMLWriter(pOut);

            HashMap<String, String> mapping = new HashMap<String, String>();
            mapping.put(XMLTokens.ATTR_XMLNS, XMLTokens.URI_DESCRIPTION);
            mapping.put(XMLTokens.createQName(XMLTokens.ATTR_XMLNS, XMLTokens.PREFIX_DESCRIPTION),
                XMLTokens.URI_DESCRIPTION);
            mapping.put(XMLTokens.createQName(XMLTokens.ATTR_XMLNS, XMLTokens.PREFIX_XLINK), XMLTokens.URI_XLINK);
            writer.startTag(XMLTokens.ELEMENT_DESCRIPTION, mapping);

            // Write the version element
            if (mVersion != null && !"".equals(mVersion)) {
                printValueElement(writer, XMLTokens.ELEMENT_VERSION, mVersion);
            }
            printValueElement(writer, XMLTokens.ELEMENT_IDENTIFIER, mId);
            if (mPlatforms != null && !"".equals(mPlatforms)) {
                printValueElement(writer, XMLTokens.ELEMENT_PLATFORM, mPlatforms);
            }

            writeDependencies(writer);
            writeUpdateInfos(writer);
            writeLicenses(writer);
            writePublisherInfos(writer);
            writeReleaseNotes(writer);
            writeDisplayNames(writer);
            writeIcons(writer);
            writeDescriptions(writer);

            writer.endTag(XMLTokens.ELEMENT_DESCRIPTION);

        } catch (UnsupportedEncodingException e) {
            // Should never happen
        } finally {
            writer.close();
        }

        fireModelSaved();
    }

    /**
     * Write the dependencies element and its children.
     *
     * @param pWriter
     *            the XML writer
     */
    private void writeDependencies(XMLWriter pWriter) {

        boolean hasMin = !(0 == mMinOOo.trim().length());
        boolean hasMax = !(0 == mMaxOOo.trim().length());
        if (hasMin || hasMax) {
            pWriter.startTag(XMLTokens.ELEMENT_DEPENDENCIES, null);

            if (hasMin) {
                HashMap<String, String> attrs = new HashMap<String, String>();
                attrs.put(XMLTokens.ATTR_VALUE, mMinOOo.trim());
                attrs.put(XMLTokens.createQName(XMLTokens.PREFIX_DESCRIPTION, XMLTokens.ELEMENT_NAME), mMinOOo.trim());
                pWriter.printSingleTag(XMLTokens.ELEMENT_OOO_MIN, attrs);
            }

            if (hasMax) {
                HashMap<String, String> attrs = new HashMap<String, String>();
                attrs.put(XMLTokens.ATTR_VALUE, mMaxOOo.trim());
                attrs.put(XMLTokens.createQName(XMLTokens.PREFIX_DESCRIPTION, XMLTokens.ELEMENT_NAME), mMaxOOo.trim());
                pWriter.printSingleTag(XMLTokens.ELEMENT_OOO_MAX, attrs);
            }

            pWriter.endTag(XMLTokens.ELEMENT_DEPENDENCIES);
        }
    }

    /**
     * Write the update-information element and its children.
     *
     * @param pWriter
     *            the XML writer
     */
    private void writeUpdateInfos(XMLWriter pWriter) {
        if (mUpdateInfos.size() > 0) {
            pWriter.startTag(XMLTokens.ELEMENT_UPDATE_INFORMATION, null);

            HashMap<String, String> attrs = new HashMap<String, String>();
            for (String mirror : mUpdateInfos) {
                attrs.clear();
                attrs.put(XMLTokens.createQName(XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF), mirror.trim());
                pWriter.printSingleTag(XMLTokens.ELEMENT_SRC, attrs);
            }

            pWriter.endTag(XMLTokens.ELEMENT_UPDATE_INFORMATION);
        }
    }

    /**
     * Write the registration element and its children.
     *
     * @param pWriter
     *            the XML writer
     */
    private void writeLicenses(XMLWriter pWriter) {

        // Check the presence of a license
        boolean hasLicenses = false;
        Iterator<String> i = mLicenses.values().iterator();
        while (!hasLicenses && i.hasNext()) {
            String value = i.next();
            hasLicenses |= !(0 == value.trim().length());
        }

        // Write the block
        if (hasLicenses) {
            pWriter.startTag(XMLTokens.ELEMENT_REGISTRATION, null);

            HashMap<String, String> attrs = new HashMap<String, String>();
            String acceptLevel = "admin"; //$NON-NLS-1$
            if (mAcceptByUser) {
                acceptLevel = "user"; //$NON-NLS-1$
            }
            attrs.put(XMLTokens.ATTR_ACCEPT_BY, acceptLevel);
            if (mSuppressOnUpdate) {
                attrs.put(XMLTokens.ATTR_SUPPRESS_ON_UPDATE, Boolean.toString(mSuppressOnUpdate));
            }
            pWriter.startTag(XMLTokens.ELEMENT_SIMPLE_LICENSE, attrs);

            Iterator<Entry<Locale, String>> iter = mLicenses.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Locale, String> entry = iter.next();
                String locale = writeLocale(entry.getKey());
                attrs.clear();
                attrs.put(XMLTokens.createQName(XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF), entry.getValue().trim());
                attrs.put(XMLTokens.ATTR_LANG, locale);
                pWriter.printSingleTag(XMLTokens.ELEMENT_LICENSE_TEXT, attrs);
            }

            pWriter.endTag(XMLTokens.ELEMENT_SIMPLE_LICENSE);
            pWriter.endTag(XMLTokens.ELEMENT_REGISTRATION);
        }
    }

    /**
     * Write the publisher element and its children.
     *
     * @param pWriter
     *            the XML writer
     */
    private void writePublisherInfos(XMLWriter pWriter) {
        boolean hasInfos = false;
        // Check the presence of an information
        Iterator<PublisherInfos> i = mPublisherInfos.values().iterator();
        while (!hasInfos && i.hasNext()) {
            PublisherInfos info = i.next();
            boolean hasName = !(0 == info.getName().length());
            boolean hasUrl = !(0 == info.getUrl().length());

            hasInfos |= hasName && hasUrl;
        }

        // Write the infos
        if (hasInfos) {
            pWriter.startTag(XMLTokens.ELEMENT_PUBLISHER, null);

            HashMap<String, String> attrs = new HashMap<String, String>();
            Iterator<Entry<Locale, PublisherInfos>> iter = mPublisherInfos.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Locale, PublisherInfos> entry = iter.next();
                String locale = writeLocale(entry.getKey());
                attrs.clear();

                PublisherInfos info = entry.getValue();
                boolean hasName = !(0 == info.getName().length());
                boolean hasUrl = !(0 == info.getUrl().length());

                attrs.put(XMLTokens.createQName(XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF), info.getUrl());
                attrs.put(XMLTokens.ATTR_LANG, locale);

                if (hasName && hasUrl) {
                    pWriter.startTag(XMLTokens.ELEMENT_NAME, attrs, false);
                    pWriter.print(XMLWriter.getEscaped(info.getName().trim()));
                    pWriter.endTag(XMLTokens.ELEMENT_NAME, false);
                }
            }

            pWriter.endTag(XMLTokens.ELEMENT_PUBLISHER);
        }
    }

    /**
     * Write the release-notes element and its children.
     *
     * @param pWriter
     *            the XML writer
     */
    private void writeReleaseNotes(XMLWriter pWriter) {

        // Check the presence of a release note
        boolean hasReleaseNote = false;
        Iterator<String> i = mReleaseNotes.values().iterator();
        while (!hasReleaseNote && i.hasNext()) {
            String value = i.next();
            hasReleaseNote |= !(0 == value.trim().length());
        }

        // Write the block
        if (hasReleaseNote) {
            pWriter.startTag(XMLTokens.ELEMENT_RELEASE_NOTES, null);

            HashMap<String, String> attrs = new HashMap<String, String>();
            Iterator<Entry<Locale, String>> iter = mReleaseNotes.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Locale, String> entry = iter.next();
                String locale = writeLocale(entry.getKey());
                attrs.clear();
                attrs.put(XMLTokens.createQName(XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF), entry.getValue().trim());
                attrs.put(XMLTokens.ATTR_LANG, locale);
                pWriter.printSingleTag(XMLTokens.ELEMENT_SRC, attrs);
            }

            pWriter.endTag(XMLTokens.ELEMENT_RELEASE_NOTES);
        }
    }

    /**
     * Write the display-name element and its children.
     *
     * @param pWriter
     *            the XML writer
     */
    private void writeDisplayNames(XMLWriter pWriter) {

        // Check the presence of a release note
        boolean hasReleaseNote = false;
        Iterator<String> i = mDisplayNames.values().iterator();
        while (!hasReleaseNote && i.hasNext()) {
            String value = i.next();
            hasReleaseNote |= !(0 == value.trim().length());
        }

        // Write the block
        if (hasReleaseNote) {
            pWriter.startTag(XMLTokens.ELEMENT_DISPLAY_NAME, null);

            HashMap<String, String> attrs = new HashMap<String, String>();
            Iterator<Entry<Locale, String>> iter = mDisplayNames.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Locale, String> entry = iter.next();
                String locale = writeLocale(entry.getKey());
                attrs.clear();
                attrs.put(XMLTokens.ATTR_LANG, locale);
                pWriter.startTag(XMLTokens.ELEMENT_NAME, attrs, false);
                pWriter.print(XMLWriter.getEscaped(entry.getValue().trim()));
                pWriter.endTag(XMLTokens.ELEMENT_NAME, false);
            }

            pWriter.endTag(XMLTokens.ELEMENT_DISPLAY_NAME);
        }
    }

    /**
     * Write the icon element and its children.
     *
     * @param pWriter
     *            the XML writer
     */
    private void writeIcons(XMLWriter pWriter) {
        boolean hasDefault = !(0 == mDefaultIcon.trim().length());
        boolean hasHC = !(0 == mHCIcon.trim().length());

        if (hasDefault || hasHC) {
            pWriter.startTag(XMLTokens.ELEMENT_ICON, null);

            HashMap<String, String> attrs = new HashMap<String, String>();
            if (hasDefault) {
                attrs.put(XMLTokens.createQName(XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF), mDefaultIcon.trim());
                pWriter.printSingleTag(XMLTokens.ELEMENT_DEFAULT, attrs);
            }

            if (hasHC) {
                attrs.clear();
                attrs.put(XMLTokens.createQName(XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF), mHCIcon.trim());
                pWriter.printSingleTag(XMLTokens.ELEMENT_HIGH_CONTRAST, attrs);
            }

            pWriter.endTag(XMLTokens.ELEMENT_ICON);
        }
    }

    /**
     * Write the extension-descriptions element and its children.
     *
     * @param pWriter
     *            the XML writer
     */
    private void writeDescriptions(XMLWriter pWriter) {

        // Check the presence of a description
        boolean hasDescription = false;
        Iterator<String> i = mDescriptions.values().iterator();
        while (!hasDescription && i.hasNext()) {
            String value = i.next();
            hasDescription |= !(0 == value.trim().length());
        }

        // Write the block
        if (hasDescription) {
            pWriter.startTag(XMLTokens.ELEMENT_EXTENSION_DESCRIPTION, null);

            HashMap<String, String> attrs = new HashMap<String, String>();
            Iterator<Entry<Locale, String>> iter = mDescriptions.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Locale, String> entry = iter.next();
                String locale = writeLocale(entry.getKey());
                attrs.clear();
                attrs.put(XMLTokens.createQName(XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF), entry.getValue().trim());
                attrs.put(XMLTokens.ATTR_LANG, locale);
                pWriter.printSingleTag(XMLTokens.ELEMENT_SRC, attrs);
            }

            pWriter.endTag(XMLTokens.ELEMENT_EXTENSION_DESCRIPTION);
        }
    }

    /**
     * Outputs the locale in a form ready to output to description.xml file.
     *
     * @param pLocale
     *            the locale to write.
     *
     * @return the string form of the locale
     */
    private String writeLocale(Locale pLocale) {
        char sep = '-';
        String result = new String();

        result = pLocale.toString().replace('_', sep);

        return result;
    }

    /**
     * Writes an XML element of the following form: &lt;pElementName value="pValue"/&gt;.
     *
     * @param pWriter
     *            the XML writer
     * @param pElementName
     *            the element name
     * @param pValue
     *            the element value
     */
    private void printValueElement(XMLWriter pWriter, String pElementName, String pValue) {
        HashMap<String, String> pAttributes = new HashMap<String, String>();
        pAttributes.put(XMLTokens.ATTR_VALUE, pValue);
        pWriter.printSingleTag(pElementName, pAttributes);
    }

    /**
     * Merges the pNewLocales into the pLocales, but avoids duplicates elements.
     *
     * @param pLocales
     *            the target list
     * @param pNewLocales
     *            the set of locales to add
     */
    private void appendNew(ArrayList<Locale> pLocales, Set<Locale> pNewLocales) {
        Iterator<Locale> iter = pNewLocales.iterator();

        while (iter.hasNext()) {
            Locale locale = iter.next();
            if (!pLocales.contains(locale)) {
                pLocales.add(locale);
            }
        }
    }

    /**
     * Tells everyone that the model has changed.
     */
    protected void fireModelChanged() {
        if (!mSuspendEvents) {
            mDirty = true;
            for (IModelChangedListener listener : mListeners) {
                listener.modelChanged();
            }
        }
    }

    /**
     * Tells everyone that the model has been saved.
     */
    protected void fireModelSaved() {
        if (!mSuspendEvents) {
            mDirty = false;
            for (IModelChangedListener listener : mListeners) {
                listener.modelSaved();
            }
        }
    }
}
