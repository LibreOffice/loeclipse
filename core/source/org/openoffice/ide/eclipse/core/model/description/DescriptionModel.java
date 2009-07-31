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
import java.util.Set;
import java.util.Map.Entry;

import org.openoffice.ide.eclipse.core.utils.XMLWriter;

/**
 * Class representing the description.xml file.
 * 
 * @author Cédric Bosdonnat
 *
 */
public class DescriptionModel {

    public String mId = new String( );
    public String mVersion = new String( );
    public HashMap<Locale, String> mDisplayNames;
    
    public String mMinOOo = new String( );
    public String mMaxOOo = new String( );
    public String mPlatforms = new String( );
    
    public String mDefaultIcon = new String( );
    public String mHCIcon = new String( );

    public HashMap<Locale, String> mDescriptions;
    
    public HashMap<Locale, String> mReleaseNotes;
    public ArrayList<String> mUpdateInfos;
    
    public boolean mAcceptByUser = false;
    public boolean mSuppressOnUpdate = false;
    public HashMap<Locale, String> mLicenses;
    
    public HashMap<Locale, PublisherInfos> mPublisherInfos;
    
    /**
     * Default constructor.
     */
    public DescriptionModel( ) {
        mDisplayNames = new HashMap<Locale, String>( );
        mDescriptions = new HashMap<Locale, String>( );
        mReleaseNotes = new HashMap<Locale, String>( );
        mUpdateInfos = new ArrayList<String>( );
        mLicenses = new HashMap<Locale, String>( );
        mPublisherInfos = new HashMap<Locale, PublisherInfos>( );
    }
    
    /**
     * @return all the locales defined in the different parts of the model.
     */
    public ArrayList<Locale> getAllLocales( ) {        
        ArrayList<Locale> locales = new ArrayList<Locale>( );
        
        appendNew( locales, mDisplayNames.keySet() );
        appendNew( locales, mDescriptions.keySet() );
        appendNew( locales, mReleaseNotes.keySet() );
        appendNew( locales, mLicenses.keySet() );
        appendNew( locales, mPublisherInfos.keySet() );
        
        return locales;
    }
    
    /**
     * Serializes the data in XML to an output stream.
     * 
     * @param pOut the output stream where to write the data
     */
    public void serialize( OutputStream pOut ) {
        XMLWriter writer = null;
        try {
            writer = new XMLWriter( pOut );
            
            HashMap<String, String> mapping = new HashMap<String, String>( );
            mapping.put( XMLTokens.ATTR_XMLNS, XMLTokens.URI_DESCRIPTION );
            mapping.put( XMLTokens.createQName( XMLTokens.ATTR_XMLNS, XMLTokens.PREFIX_DESCRIPTION ), 
                    XMLTokens.URI_DESCRIPTION );
            mapping.put( XMLTokens.createQName( XMLTokens.ATTR_XMLNS, XMLTokens.PREFIX_XLINK), 
                    XMLTokens.URI_XLINK );
            writer.startTag( XMLTokens.ELEMENT_DESCRIPTION, mapping );
            
            // Write the version element
            printValueElement( writer, XMLTokens.ELEMENT_VERSION, mVersion );
            printValueElement( writer, XMLTokens.ELEMENT_IDENTIFIER, mId );
            printValueElement( writer, XMLTokens.ELEMENT_PLATFORM, mPlatforms );
            
            writeDependencies( writer );
            writeUpdateInfos( writer );
            writeLicenses( writer );
            writePublisherInfos( writer );
            writeReleaseNotes( writer );
            writeDisplayNames( writer );
            writeIcons( writer );
            writeDescriptions( writer );
            
            writer.endTag( XMLTokens.ELEMENT_DESCRIPTION );
            
        } catch (UnsupportedEncodingException e) {
            // Should never happen
        } finally {
            writer.close();
        }
    }

    /**
     * Write the dependencies element and its children.
     * 
     * @param pWriter the XML writer
     */
    private void writeDependencies(XMLWriter pWriter) {
        
        boolean hasMin = !mMinOOo.trim().isEmpty();
        boolean hasMax = !mMaxOOo.trim().isEmpty();
        if ( hasMin || hasMax ) {
            pWriter.startTag( XMLTokens.ELEMENT_DEPENDENCIES, null );

            if ( hasMin ) {
                HashMap<String, String> attrs = new HashMap<String, String>( );
                attrs.put( XMLTokens.ATTR_VALUE, mMinOOo.trim() );
                attrs.put( XMLTokens.createQName(XMLTokens.PREFIX_DESCRIPTION, XMLTokens.ELEMENT_NAME ),
                        "OpenOffice.org " + mMinOOo.trim() ); //$NON-NLS-1$
                pWriter.printSingleTag( XMLTokens.ELEMENT_OOO_MIN, attrs );
            }

            if ( hasMax ) {
                HashMap<String, String> attrs = new HashMap<String, String>( );
                attrs.put( XMLTokens.ATTR_VALUE, mMaxOOo.trim() );
                attrs.put( XMLTokens.createQName(XMLTokens.PREFIX_DESCRIPTION, XMLTokens.ELEMENT_NAME ),
                        "OpenOffice.org " + mMaxOOo.trim( ) ); //$NON-NLS-1$
                pWriter.printSingleTag( XMLTokens.ELEMENT_OOO_MAX, attrs );
            }
            
            pWriter.endTag( XMLTokens.ELEMENT_DEPENDENCIES );
        }
    }
    
    /**
     * Write the update-information element and its children.
     * 
     * @param pWriter the XML writer
     */
    private void writeUpdateInfos(XMLWriter pWriter) {
        if ( mUpdateInfos.size() > 0 ) {
            pWriter.startTag( XMLTokens.ELEMENT_UPDATE_INFORMATION, null );
            
            HashMap<String, String> attrs = new HashMap<String, String>( );
            for (String mirror : mUpdateInfos) {
                attrs.clear();
                attrs.put( XMLTokens.createQName( XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF ),
                        mirror.trim( ) );
                pWriter.printSingleTag( XMLTokens.ELEMENT_SRC, attrs );
            }
            
            pWriter.endTag( XMLTokens.ELEMENT_UPDATE_INFORMATION );
        }
    }
    
    /**
     * Write the registration element and its children.
     * 
     * @param pWriter the XML writer
     */
    private void writeLicenses(XMLWriter pWriter) {
        
        // Check the presence of a license
        boolean hasLicenses = false;
        Iterator<String> i = mLicenses.values().iterator();
        while ( !hasLicenses && i.hasNext( ) ) {
            String value = i.next();
            hasLicenses |= !value.trim().isEmpty();
        }
        
        //Write the block
        if ( hasLicenses ) {
            pWriter.startTag( XMLTokens.ELEMENT_REGISTRATION, null );
            
            HashMap<String, String> attrs = new HashMap<String, String>( );
            String acceptLevel = "admin"; //$NON-NLS-1$
            if ( mAcceptByUser ) {
                acceptLevel = "user"; //$NON-NLS-1$
            }
            attrs.put( XMLTokens.ATTR_ACCEPT_BY, acceptLevel );
            if ( mSuppressOnUpdate ) {
                attrs.put( XMLTokens.ATTR_SUPPRESS_ON_UPDATE, Boolean.toString( mSuppressOnUpdate ) );
            }
            pWriter.startTag( XMLTokens.ELEMENT_SIMPLE_LICENSE, attrs );
            
            Iterator<Entry<Locale, String>> iter = mLicenses.entrySet().iterator();
            while ( iter.hasNext() ) {
                Entry<Locale, String> entry = iter.next();
                String locale = writeLocale( entry.getKey() );
                attrs.clear();
                attrs.put( XMLTokens.createQName( XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF ),
                        entry.getValue().trim( ) );
                attrs.put( XMLTokens.ATTR_LANG, locale );
                pWriter.printSingleTag( XMLTokens.ELEMENT_LICENSE_TEXT, attrs );
            }
            
            pWriter.endTag( XMLTokens.ELEMENT_SIMPLE_LICENSE );
            pWriter.endTag( XMLTokens.ELEMENT_REGISTRATION );
        }
    }
    
    /**
     * Write the publisher element and its children.
     * 
     * @param pWriter the XML writer
     */
    private void writePublisherInfos(XMLWriter pWriter) {
        boolean hasInfos = false;
        // Check the presence of an information
        Iterator<PublisherInfos> i = mPublisherInfos.values().iterator();
        while ( !hasInfos && i.hasNext() ) {
            PublisherInfos info = i.next();
            boolean hasName = !info.mName.trim().isEmpty();
            boolean hasUrl = !info.mUrl.trim().isEmpty();
            
            hasInfos |= hasName && hasUrl;
        }
        
        // Write the infos
        if ( hasInfos ) {
            pWriter.startTag( XMLTokens.ELEMENT_PUBLISHER, null );
            
            HashMap<String, String> attrs = new HashMap<String, String>( );
            Iterator<Entry<Locale, PublisherInfos>> iter = mPublisherInfos.entrySet().iterator();
            while ( iter.hasNext() ) {
                Entry<Locale, PublisherInfos> entry = iter.next();
                String locale = writeLocale( entry.getKey() );
                attrs.clear();
                
                PublisherInfos info = entry.getValue();
                boolean hasName = !info.mName.trim().isEmpty();
                boolean hasUrl = !info.mUrl.trim().isEmpty();
                
                attrs.put( XMLTokens.createQName( XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF), 
                        info.mUrl.trim() );
                attrs.put( XMLTokens.ATTR_LANG, locale );
                
                if ( hasName && hasUrl ) {
                    pWriter.startTag( XMLTokens.ELEMENT_NAME, attrs, false );
                    pWriter.print( XMLWriter.getEscaped( info.mName.trim( ) ) );
                    pWriter.endTag( XMLTokens.ELEMENT_NAME, false );
                }
            }
            
            pWriter.endTag( XMLTokens.ELEMENT_PUBLISHER );
        }
    }
    
    /**
     * Write the release-notes element and its children.
     * 
     * @param pWriter the XML writer
     */
    private void writeReleaseNotes(XMLWriter pWriter) {
        
        // Check the presence of a release note
        boolean hasReleaseNote = false;
        Iterator<String> i = mReleaseNotes.values().iterator();
        while ( !hasReleaseNote && i.hasNext( ) ) {
            String value = i.next();
            hasReleaseNote |= !value.trim().isEmpty();
        }
        
        //Write the block
        if ( hasReleaseNote ) {
            pWriter.startTag( XMLTokens.ELEMENT_RELEASE_NOTES, null );
            
            HashMap<String, String> attrs = new HashMap<String, String>( );
            Iterator<Entry<Locale, String>> iter = mReleaseNotes.entrySet().iterator();
            while ( iter.hasNext() ) {
                Entry<Locale, String> entry = iter.next();
                String locale = writeLocale( entry.getKey() );
                attrs.clear();
                attrs.put( XMLTokens.createQName( XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF ),
                        entry.getValue().trim( ) );
                attrs.put( XMLTokens.ATTR_LANG, locale );
                pWriter.printSingleTag( XMLTokens.ELEMENT_SRC, attrs );
            }
            
            pWriter.endTag( XMLTokens.ELEMENT_RELEASE_NOTES );
        }
    }

    /**
     * Write the display-name element and its children.
     * 
     * @param pWriter the XML writer
     */
    private void writeDisplayNames(XMLWriter pWriter) {
        
        // Check the presence of a release note
        boolean hasReleaseNote = false;
        Iterator<String> i = mDisplayNames.values().iterator();
        while ( !hasReleaseNote && i.hasNext( ) ) {
            String value = i.next();
            hasReleaseNote |= !value.trim().isEmpty();
        }
        
        //Write the block
        if ( hasReleaseNote ) {
            pWriter.startTag( XMLTokens.ELEMENT_DISPLAY_NAME, null );
            
            HashMap<String, String> attrs = new HashMap<String, String>( );
            Iterator<Entry<Locale, String>> iter = mDisplayNames.entrySet().iterator();
            while ( iter.hasNext() ) {
                Entry<Locale, String> entry = iter.next();
                String locale = writeLocale( entry.getKey() );
                attrs.clear();
                attrs.put( XMLTokens.ATTR_LANG, locale );
                pWriter.startTag( XMLTokens.ELEMENT_NAME, attrs, false );
                pWriter.print( XMLWriter.getEscaped( entry.getValue().trim() ) );
                pWriter.endTag( XMLTokens.ELEMENT_NAME, false );
            }
            
            pWriter.endTag( XMLTokens.ELEMENT_DISPLAY_NAME );
        }
    }
    
    /**
     * Write the icon element and its children.
     * 
     * @param pWriter the XML writer
     */
    private void writeIcons(XMLWriter pWriter) {
        boolean hasDefault = !mDefaultIcon.trim().isEmpty();
        boolean hasHC = !mHCIcon.trim().isEmpty();
        
        if ( hasDefault || hasHC ) {
            pWriter.startTag( XMLTokens.ELEMENT_ICON, null );
            
            HashMap<String, String> attrs = new HashMap<String, String>( );
            if ( hasDefault ) {
                attrs.put( XMLTokens.createQName( XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF), 
                        mDefaultIcon.trim() );
                pWriter.printSingleTag( XMLTokens.ELEMENT_DEFAULT, attrs);
            }
            
            if ( hasHC ) {
                attrs.clear();
                attrs.put( XMLTokens.createQName( XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF), 
                        mHCIcon.trim() );
                pWriter.printSingleTag( XMLTokens.ELEMENT_HIGH_CONTRAST, attrs);
            }
            
            pWriter.endTag( XMLTokens.ELEMENT_ICON );
        }
    }
    
    /**
     * Write the extension-descriptions element and its children.
     * 
     * @param pWriter the XML writer
     */
    private void writeDescriptions(XMLWriter pWriter) {
        
        // Check the presence of a description
        boolean hasDescription = false;
        Iterator<String> i = mDescriptions.values().iterator();
        while ( !hasDescription && i.hasNext( ) ) {
            String value = i.next();
            hasDescription |= !value.trim().isEmpty();
        }
        
        //Write the block
        if ( hasDescription ) {
            pWriter.startTag( XMLTokens.ELEMENT_EXTENSION_DESCRIPTION, null );
            
            HashMap<String, String> attrs = new HashMap<String, String>( );
            Iterator<Entry<Locale, String>> iter = mDescriptions.entrySet().iterator();
            while ( iter.hasNext() ) {
                Entry<Locale, String> entry = iter.next();
                String locale = writeLocale( entry.getKey() );
                attrs.clear();
                attrs.put( XMLTokens.createQName( XMLTokens.PREFIX_XLINK, XMLTokens.ATTR_HREF ),
                        entry.getValue().trim( ) );
                attrs.put( XMLTokens.ATTR_LANG, locale );
                pWriter.printSingleTag( XMLTokens.ELEMENT_SRC, attrs );
            }
            
            pWriter.endTag( XMLTokens.ELEMENT_EXTENSION_DESCRIPTION );
        }
    }
    
    /**
     * Outputs the locale in a form ready to output to description.xml file.
     * 
     * @param pLocale the locale to write.
     * 
     * @return the string form of the locale
     */
    private String writeLocale( Locale pLocale ) {
        char sep = '-'; //$NON-NLS-1$
        String result = new String( );
        
        result = pLocale.toString().replace( '_', sep ); //$NON-NLS-1$
        
        return result;
    }
    
    /**
     * Writes an XML element of the following form:
     * &lt;pElementName value="pValue"/&gt;.
     * 
     * @param pWriter the XML writer
     * @param pElementName the element name
     * @param pValue the element value
     */
    private void printValueElement(XMLWriter pWriter, String pElementName,
            String pValue) {
        HashMap<String, String> pAttributes = new HashMap<String, String>( );
        pAttributes.put( XMLTokens.ATTR_VALUE , pValue );
        pWriter.printSingleTag( pElementName, pAttributes );
    }

    /**
     * Merges the pNewLocales into the pLocales, but avoids duplicates elements.
     * 
     * @param pLocales the target list
     * @param pNewLocales the set of locales to add
     */
    private void appendNew( ArrayList<Locale> pLocales, Set<Locale> pNewLocales ) {
        Iterator<Locale> iter = pNewLocales.iterator();
        
        while ( iter.hasNext() ) {
            Locale locale = iter.next();
            if ( !pLocales.contains( locale ) ) {
                pLocales.add( locale );
            }
        }
    }
}
