package org.openoffice.ide.eclipse.core.model.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class handling the SAX events for the description.xml file.
 * 
 * @author cbosdonnat
 *
 */
public class DescriptionHandler extends DefaultHandler {
    
    private static final int LANG_GROUP = 1;
    private static final int COUNTRY_GROUP = 2;
    private final Pattern mLocalePattern = Pattern.compile( "([a-z]{2})(?:-([A-Z]{2})(?:-[^-]*)?)?" ); //$NON-NLS-1$
    
    private DescriptionModel mModel;
    
    private String mHandlerRemoveOn;
    private DefaultHandler mCurrentHandler;
    
    /**
     * The handler uses a reference to the model and only fills it.
     *  
     * @param pModel the descriptions.xml model to fill
     */
    public DescriptionHandler( DescriptionModel pModel ) {
        mModel = pModel;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String pUri, String pLocalName, String pName,
            Attributes pAttributes) throws SAXException {
        super.startElement(pUri, pLocalName, pName, pAttributes);
        
        
        if ( mCurrentHandler != null ) {
            mCurrentHandler.startElement( pUri, pLocalName, pName, pAttributes);
            
        } else if ( XMLTokens.ELEMENT_VERSION.equals( pName ) ) {
            mModel.mVersion = pAttributes.getValue( XMLTokens.ATTR_VALUE );
            
        } else if ( XMLTokens.ELEMENT_IDENTIFIER.equals( pName ) ) {
            mModel.mId = pAttributes.getValue( XMLTokens.ATTR_VALUE );
            
        } else if ( XMLTokens.ELEMENT_PLATFORM.equals( pName ) ) {
            mModel.mPlatforms = pAttributes.getValue( XMLTokens.ATTR_VALUE );
            
        } else if ( XMLTokens.ELEMENT_DEPENDENCIES.equals( pName ) ) {
            mCurrentHandler = new DependenciesHandler( );
            mHandlerRemoveOn = pName;
            
        } else if ( XMLTokens.ELEMENT_UPDATE_INFORMATION.equals( pName ) ) {
            mCurrentHandler = new UpdatesHandler( );
            mHandlerRemoveOn = pName;
            
        } else if ( XMLTokens.ELEMENT_REGISTRATION.equals( pName ) ) {
            mCurrentHandler = new LicenseHandler( );
            mHandlerRemoveOn = pName;
        } else if ( XMLTokens.ELEMENT_PUBLISHER.equals( pName ) ) {
            mCurrentHandler = new PublisherHandler( );
            mHandlerRemoveOn = pName;
            
        } else if ( XMLTokens.ELEMENT_RELEASE_NOTES.equals( pName ) ) {
            mCurrentHandler = new ReleasesHandler( );
            mHandlerRemoveOn = pName;
            
        } else if ( XMLTokens.ELEMENT_DISPLAY_NAME.equals( pName ) ) {
            mCurrentHandler = new NameHandler( );
            mHandlerRemoveOn = pName;
            
        } else if ( XMLTokens.ELEMENT_ICON.equals( pName ) ) {
            mCurrentHandler = new IconHandler( );
            mHandlerRemoveOn = pName;
            
        } else if ( XMLTokens.ELEMENT_EXTENSION_DESCRIPTION.equals( pName ) ) {
            mCurrentHandler = new DescriptionsHandler( );
            mHandlerRemoveOn = pName;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] pCh, int pStart, int pLength) throws SAXException {
        super.characters(pCh, pStart, pLength);
        
        if ( mCurrentHandler != null ) {
            mCurrentHandler.characters( pCh, pStart, pLength );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String pUri, String pLocalName, String pName) throws SAXException {
        super.endElement(pUri, pLocalName, pName);
        
        if ( mCurrentHandler != null ) {
            if ( mHandlerRemoveOn.equals( pName ) ) {
                mCurrentHandler = null;
            } else {
                mCurrentHandler.endElement( pUri, pLocalName, pName );
            }
        }
    }
    
    /**
     * Parses the locales on the form la-CO-variant to create a locale
     * with the language and country parts.
     * 
     * @param pValue the string representation of the locale.
     * 
     * @return the locale or <code>null</code>.
     */
    private Locale parseLocale( String pValue ) {
        Locale locale = null;
        if ( pValue != null ) {
            Matcher m = mLocalePattern.matcher( pValue );
            if ( m.matches() ) {
                String language = m.group( LANG_GROUP );
                String country = m.group( COUNTRY_GROUP );
                if ( country != null ) {
                    locale = new Locale( language, country );
                } else {
                    locale = new Locale( language );
                }
            }
        }
        return locale;
    }
    
    /**
     * Child handler for the dependencies element.
     * 
     * @author cbosdonnat
     *
     */
    private class DependenciesHandler extends DefaultHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String pUri, String pLocalName, String pName,
                Attributes pAttributes) throws SAXException {
            super.startElement(pUri, pLocalName, pName, pAttributes);
            
            if ( XMLTokens.ATTR_OOO_MIN.equals( pName ) ) {
                mModel.mMinOOo = pAttributes.getValue( XMLTokens.ATTR_VALUE );
            } else if ( XMLTokens.ATTR_OOO_MAX.equals( pName ) ) {
                mModel.mMaxOOo = pAttributes.getValue( XMLTokens.ATTR_VALUE );
            }
        }
    }
    
    /**
     * Child handler for the update-information element.
     * 
     * @author cbosdonnat
     *
     */
    private class UpdatesHandler extends DefaultHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String pUri, String pLocalName, String pName,
                Attributes pAttributes) throws SAXException {
            super.startElement(pUri, pLocalName, pName, pAttributes);
            
            if ( XMLTokens.ELEMENT_SRC.equals( pName ) ) {
                if ( mModel.mUpdateInfos == null ) {
                    mModel.mUpdateInfos = new ArrayList<String>( );
                }
                String value = pAttributes.getValue( XMLTokens.XLINK_URL, XMLTokens.ATTR_HREF );
                mModel.mUpdateInfos.add( value );
            }
        }
    }
    
    /**
     * Child handler for the registration element.
     * 
     * @author cbosdonnat
     *
     */
    private class LicenseHandler extends DefaultHandler {
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String pUri, String pLocalName, String pName,
                Attributes pAttributes) throws SAXException {
            super.startElement(pUri, pLocalName, pName, pAttributes);
            
            if ( XMLTokens.ELEMENT_SIMPLE_LICENSE.equals( pName ) ) {
                if ( mModel.mLicenses == null) {
                    mModel.mLicenses = new HashMap<Locale, String>( );
                }
                
                String value = pAttributes.getValue( XMLTokens.ATTR_ACCEPT_BY );
                if ( value != null ) {
                    mModel.mAcceptByUser = value.equals( XMLTokens.VALUE_USER );
                }
                
                // Optional attribute defaulting to false
                value = pAttributes.getValue( XMLTokens.ATTR_SUPPRESS_ON_UPDATE );
                if ( value == null ) {
                    value = Boolean.FALSE.toString();
                }
                mModel.mSuppressOnUpdate = Boolean.parseBoolean( value );
                
            } else if ( XMLTokens.ELEMENT_LICENSE_TEXT.equals( pName ) ) {
                String ref = pAttributes.getValue( XMLTokens.XLINK_URL, XMLTokens.ATTR_HREF );
                String lang = pAttributes.getValue( XMLTokens.ATTR_LANG );
                
                Locale locale = parseLocale( lang );
                if ( locale != null ) {
                    mModel.mLicenses.put( locale, ref );
                }
            }
        }
    }
    
    /**
     * Child handler for the publisher element.
     * 
     * @author cbosdonnat
     *
     */
    private class PublisherHandler extends DefaultHandler {

        private String mRef;
        private Locale mLocale;
        private String mName;
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String pUri, String pLocalName, String pName,
                Attributes pAttributes) throws SAXException {
            super.startElement(pUri, pLocalName, pName, pAttributes);
            
            if ( XMLTokens.ELEMENT_NAME.equals( pName ) ) {
                if ( mModel.mPublisherInfos == null ) {
                    mModel.mPublisherInfos = new HashMap<Locale, PublisherInfos>( );
                }
                mRef = pAttributes.getValue( XMLTokens.XLINK_URL, XMLTokens.ATTR_HREF );
                mLocale = parseLocale( pAttributes.getValue( XMLTokens.ATTR_LANG ) );
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void characters(char[] pCh, int pStart, int pLength) throws SAXException {
            super.characters(pCh, pStart, pLength);
            mName = new String( pCh, pStart, pLength );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void endElement(String pUri, String pLocalName, String pName) throws SAXException {
            super.endElement(pUri, pLocalName, pName);
            
            if ( XMLTokens.ELEMENT_NAME.equals( pName )  && mLocale != null ) {
                PublisherInfos infos = new PublisherInfos( );
                infos.mName = mName;
                infos.mUrl = mRef;
                mModel.mPublisherInfos.put( mLocale, infos );
                
                // Cleans all the members for the next entry
                mName = null;
                mLocale = null;
                mRef = null;
            }
        }
    }

    /**
     * Child handler for the release-notes element.
     * 
     * @author cbosdonnat
     *
     */
    private class ReleasesHandler extends DefaultHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String pUri, String pLocalName, String pName,
                Attributes pAttributes) throws SAXException {
            super.startElement(pUri, pLocalName, pName, pAttributes);
            
            if ( XMLTokens.ELEMENT_SRC.equals( pName ) ) {
                if ( mModel.mReleaseNotes == null ) {
                    mModel.mReleaseNotes = new HashMap<Locale, String>( );
                }
                String value = pAttributes.getValue( XMLTokens.XLINK_URL, XMLTokens.ATTR_HREF );
                Locale locale = parseLocale( pAttributes.getValue( XMLTokens.ATTR_LANG ) );
                if ( locale != null ) {
                    mModel.mReleaseNotes.put( locale, value );
                }
            }
        }
    }

    /**
     * Child handler for the display-name element.
     * 
     * @author cbosdonnat
     *
     */
    private class NameHandler extends DefaultHandler {

        private Locale mLocale;
        private String mName;
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String pUri, String pLocalName, String pName,
                Attributes pAttributes) throws SAXException {
            super.startElement(pUri, pLocalName, pName, pAttributes);
            
            if ( XMLTokens.ELEMENT_NAME.equals( pName ) ) {
                if ( mModel.mDisplayNames == null ) {
                    mModel.mDisplayNames = new HashMap<Locale, String>( );
                }
                mLocale = parseLocale( pAttributes.getValue( XMLTokens.ATTR_LANG ) );
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void characters(char[] pCh, int pStart, int pLength) throws SAXException {
            super.characters(pCh, pStart, pLength);
            mName = new String( pCh, pStart, pLength );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void endElement(String pUri, String pLocalName, String pName) throws SAXException {
            super.endElement(pUri, pLocalName, pName);
            
            if ( XMLTokens.ELEMENT_NAME.equals( pName )  && mLocale != null ) {
                mModel.mDisplayNames.put( mLocale, mName );
                
                // Cleans all the members for the next entry
                mName = null;
                mLocale = null;
            }
        }
    }

    /**
     * Child handler for the icon element.
     * 
     * @author cbosdonnat
     *
     */
    private class IconHandler extends DefaultHandler {
    
        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String pUri, String pLocalName, String pName, 
                Attributes pAttributes) throws SAXException {
            super.startElement(pUri, pLocalName, pName, pAttributes);
            
            String ref = pAttributes.getValue( XMLTokens.XLINK_URL, XMLTokens.ATTR_HREF );
            if ( XMLTokens.ELEMENT_DEFAULT.equals( pName ) ) {
                mModel.mDefaultIcon = ref;
                
            } else if ( XMLTokens.ELEMENT_HIGH_CONTRAST.equals( pName ) ) {
                mModel.mHCIcon = ref;
            }
        }
    }
    
    /**
     * Child handler for the extension-description element.
     * 
     * @author cbosdonnat
     *
     */
    private class DescriptionsHandler extends DefaultHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String pUri, String pLocalName, String pName,
                Attributes pAttributes) throws SAXException {
            super.startElement(pUri, pLocalName, pName, pAttributes);
            
            if ( XMLTokens.ELEMENT_SRC.equals( pName ) ) {
                if ( mModel.mDescriptions == null ) {
                    mModel.mDescriptions = new HashMap<Locale, String>( );
                }
                String value = pAttributes.getValue( XMLTokens.XLINK_URL, XMLTokens.ATTR_HREF );
                Locale locale = parseLocale( pAttributes.getValue( XMLTokens.ATTR_LANG ) );
                if ( locale != null ) {
                    mModel.mDescriptions.put( locale, value );
                }
            }
        }
    }
}

