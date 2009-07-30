package org.openoffice.ide.eclipse.core.model.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

/**
 * Class representing the description.xml file.
 * 
 * @author cbosdonnat
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
