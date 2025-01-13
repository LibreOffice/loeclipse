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
package org.libreoffice.ide.eclipse.core.model.description;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class handling the SAX events for the description.xml file.
 */
public class DescriptionHandler extends DefaultHandler {

    private static final int LANG_GROUP = 1;
    private static final int COUNTRY_GROUP = 2;
    private final Pattern mLocalePattern = Pattern.compile("([a-z]{2})(?:-([A-Z]{2})(?:-[^-]*)?)?"); //$NON-NLS-1$

    private DescriptionModel mModel;

    private String mHandlerRemoveOn;
    private DefaultHandler mCurrentHandler;

    /**
     * The handler uses a reference to the model and only fills it.
     *
     * @param model
     *            the descriptions.xml model to fill
     */
    public DescriptionHandler(DescriptionModel model) {
        mModel = model;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, name, attributes);

        if (mCurrentHandler != null) {
            mCurrentHandler.startElement(uri, localName, name, attributes);
        } else {
            switch (name) {
                case XMLTokens.ELEMENT_VERSION:
                    mModel.setVersion(attributes.getValue(XMLTokens.ATTR_VALUE));
                    break;
                case XMLTokens.ELEMENT_IDENTIFIER:
                    mModel.setId(attributes.getValue(XMLTokens.ATTR_VALUE));
                    break;
                case XMLTokens.ELEMENT_PLATFORM:
                    mModel.setPlatforms(attributes.getValue(XMLTokens.ATTR_VALUE));
                    break;
                case XMLTokens.ELEMENT_DEPENDENCIES:
                    mCurrentHandler = new DependenciesHandler();
                    mHandlerRemoveOn = name;
                    break;
                case XMLTokens.ELEMENT_UPDATE_INFORMATION:
                    mCurrentHandler = new UpdatesHandler();
                    mHandlerRemoveOn = name;
                    break;
                case XMLTokens.ELEMENT_REGISTRATION:
                    mCurrentHandler = new LicenseHandler();
                    mHandlerRemoveOn = name;
                    break;
                case XMLTokens.ELEMENT_PUBLISHER:
                    mCurrentHandler = new PublisherHandler();
                    mHandlerRemoveOn = name;
                    break;
                case XMLTokens.ELEMENT_RELEASE_NOTES:
                    mCurrentHandler = new ReleasesHandler();
                    mHandlerRemoveOn = name;
                    break;
                case XMLTokens.ELEMENT_DISPLAY_NAME:
                    mCurrentHandler = new NameHandler();
                    mHandlerRemoveOn = name;
                    break;
                case XMLTokens.ELEMENT_ICON:
                    mCurrentHandler = new IconHandler();
                    mHandlerRemoveOn = name;
                    break;
                case XMLTokens.ELEMENT_EXTENSION_DESCRIPTION:
                    mCurrentHandler = new DescriptionsHandler();
                    mHandlerRemoveOn = name;
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);

        if (mCurrentHandler != null) {
            mCurrentHandler.characters(ch, start, length);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        super.endElement(uri, localName, name);

        if (mCurrentHandler != null) {
            if (mHandlerRemoveOn.equals(name)) {
                mCurrentHandler = null;
            } else {
                mCurrentHandler.endElement(uri, localName, name);
            }
        }
    }

    /**
     * Parses the locales on the form la-CO-variant to create a locale with the language and country parts.
     *
     * @param pValue
     *            the string representation of the locale.
     *
     * @return the locale or <code>null</code>.
     */
    private Locale parseLocale(String pValue) {
        Locale locale = null;
        if (pValue != null) {
            Matcher m = mLocalePattern.matcher(pValue);
            if (m.matches()) {
                String language = m.group(LANG_GROUP);
                String country = m.group(COUNTRY_GROUP);
                if (country != null) {
                    locale = new Locale(language, country);
                } else {
                    locale = new Locale(language);
                }
            }
        }
        return locale;
    }

    /**
     * Child handler for the dependencies element.
     */
    private class DependenciesHandler extends DefaultHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
            super.startElement(uri, localName, name, attributes);

            if (XMLTokens.ELEMENT_OOO_MIN.equals(name)) {
                mModel.setMinOOo(attributes.getValue(XMLTokens.ATTR_VALUE));
            } else if (XMLTokens.ELEMENT_OOO_MAX.equals(name)) {
                mModel.setMaxOOo(attributes.getValue(XMLTokens.ATTR_VALUE));
            }
        }
    }

    /**
     * Child handler for the update-information element.
     */
    private class UpdatesHandler extends DefaultHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
            super.startElement(uri, localName, name, attributes);

            if (XMLTokens.ELEMENT_SRC.equals(name)) {
                String value = attributes.getValue(XMLTokens.URI_XLINK, XMLTokens.ATTR_HREF);
                mModel.addUpdateInfo(value);
            }
        }
    }

    /**
     * Child handler for the registration element.
     */
    private class LicenseHandler extends DefaultHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
            super.startElement(uri, localName, name, attributes);

            if (XMLTokens.ELEMENT_SIMPLE_LICENSE.equals(name)) {
                String value = attributes.getValue(XMLTokens.ATTR_ACCEPT_BY);
                if (value != null) {
                    mModel.setAcceptByUser(value.equals(XMLTokens.VALUE_USER));
                }

                // Optional attribute defaulting to false
                value = attributes.getValue(XMLTokens.ATTR_SUPPRESS_ON_UPDATE);
                if (value == null) {
                    value = Boolean.FALSE.toString();
                }
                mModel.setSuppressOnUpdate(Boolean.parseBoolean(value));

            } else if (XMLTokens.ELEMENT_LICENSE_TEXT.equals(name)) {
                String ref = attributes.getValue(XMLTokens.URI_XLINK, XMLTokens.ATTR_HREF);
                String lang = attributes.getValue(XMLTokens.ATTR_LANG);

                Locale locale = parseLocale(lang);
                if (locale != null) {
                    mModel.addLicense(locale, ref);
                }
            }
        }
    }

    /**
     * Child handler for the publisher element.
     */
    private class PublisherHandler extends DefaultHandler {

        private String mRef;
        private Locale mLocale;
        private String mName;

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
            super.startElement(uri, localName, name, attributes);

            if (XMLTokens.ELEMENT_NAME.equals(name)) {
                mRef = attributes.getValue(XMLTokens.URI_XLINK, XMLTokens.ATTR_HREF);
                mLocale = parseLocale(attributes.getValue(XMLTokens.ATTR_LANG));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            mName = new String(ch, start, length);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            super.endElement(uri, localName, name);

            if (XMLTokens.ELEMENT_NAME.equals(name) && mLocale != null) {
                PublisherInfos infos = new PublisherInfos();
                infos.setModel(mModel);
                infos.setName(mName);
                infos.setUrl(mRef);
                mModel.addPublisherInfo(mLocale, infos);

                // Cleans all the members for the next entry
                mName = null;
                mLocale = null;
                mRef = null;
            }
        }
    }

    /**
     * Child handler for the release-notes element.
     */
    private class ReleasesHandler extends DefaultHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
            super.startElement(uri, localName, name, attributes);

            if (XMLTokens.ELEMENT_SRC.equals(name)) {
                String value = attributes.getValue(XMLTokens.URI_XLINK, XMLTokens.ATTR_HREF);
                Locale locale = parseLocale(attributes.getValue(XMLTokens.ATTR_LANG));
                if (locale != null) {
                    mModel.addReleaseNote(locale, value);
                }
            }
        }
    }

    /**
     * Child handler for the display-name element.
     */
    private class NameHandler extends DefaultHandler {

        private Locale mLocale;
        private String mName;

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
            super.startElement(uri, localName, name, attributes);

            if (XMLTokens.ELEMENT_NAME.equals(name)) {
                mLocale = parseLocale(attributes.getValue(XMLTokens.ATTR_LANG));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            mName = new String(ch, start, length);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            super.endElement(uri, localName, name);

            if (XMLTokens.ELEMENT_NAME.equals(name) && mLocale != null) {
                mModel.getDisplayNames().put(mLocale, mName);

                // Cleans all the members for the next entry
                mName = null;
                mLocale = null;
            }
        }
    }

    /**
     * Child handler for the icon element.
     */
    private class IconHandler extends DefaultHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
            super.startElement(uri, localName, name, attributes);

            String ref = attributes.getValue(XMLTokens.URI_XLINK, XMLTokens.ATTR_HREF);
            if (XMLTokens.ELEMENT_DEFAULT.equals(name)) {
                mModel.setDefaultIcon(ref);

            } else if (XMLTokens.ELEMENT_HIGH_CONTRAST.equals(name)) {
                mModel.setHCIcon(ref);
            }
        }
    }

    /**
     * Child handler for the extension-description element.
     */
    private class DescriptionsHandler extends DefaultHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
            super.startElement(uri, localName, name, attributes);

            if (XMLTokens.ELEMENT_SRC.equals(name)) {
                String value = attributes.getValue(XMLTokens.URI_XLINK, XMLTokens.ATTR_HREF);
                Locale locale = parseLocale(attributes.getValue(XMLTokens.ATTR_LANG));
                if (locale != null) {
                    mModel.addDescription(locale, value);
                }
            }
        }
    }
}
