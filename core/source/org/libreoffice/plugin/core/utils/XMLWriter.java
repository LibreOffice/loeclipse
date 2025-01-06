/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cédric Bosdonnat - cleaned up the code
 *******************************************************************************/
package org.libreoffice.plugin.core.utils;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple XML writer.
 */
public class XMLWriter extends PrintWriter {

    /* constants */
    protected static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; //$NON-NLS-1$
    private static final int BUFFER_FREE_SPACE = 10;

    protected int mTab;

    /**
     * Creates a new writer using the given output stream to write the data.
     *
     * @param output
     *            where to write the XML
     * @throws UnsupportedEncodingException
     *             if the UTF8 charset isn't supported (would be strange)
     */
    public XMLWriter(OutputStream output) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(output, "UTF8")); //$NON-NLS-1$
        mTab = 0;
        println(XML_VERSION);
    }

    /**
     * Write the end of an XML tag.
     *
     * @param name
     *            the name of the tag
     */
    public void endTag(String name) {
        mTab--;
        printTag('/' + name, null);
    }

    /**
     * Write the end of an XML tag.
     *
     * @param name
     *            the name of the tag
     * @param indentation
     *            whether to print the indentation or not
     */
    public void endTag(String name, boolean indentation) {
        mTab--;
        printTag('/' + name, null, indentation, true, false);
    }

    /**
     * Write a simple XML tag, on the form &lt;name&gt;value&lt;/name&gt;.
     *
     * @param name
     *            the name of the tag
     * @param value
     *            the value
     */
    public void printSimpleTag(String name, Object value) {
        if (value != null) {
            printTag(name, null, true, false, false);
            print(getEscaped(String.valueOf(value)));
            printTag('/' + name, null, false, true, false);
        }
    }

    /**
     * Write the tab characters at the beginning of the line.
     */
    public void printTabulation() {
        for (int i = 0; i < mTab; i++) {
            super.print('\t');
        }
    }

    /**
     * Print an XML Tag in the form &lt;name .../&gt;.
     *
     * @param name
     *            the tag name
     * @param parameters
     *            the tag attributes
     *
     * @see #startTag(String, Map)
     * @see #startTag(String, Map, boolean)
     */
    public void printSingleTag(String name, Map<String, ? extends Object> parameters) {
        printTag(name, parameters, true, true, true);
    }

    /**
     * Print an XML Tag.
     *
     * @param name
     *            the tag name
     * @param parameters
     *            the tag attributes
     *
     * @see #startTag(String, Map)
     * @see #startTag(String, Map, boolean)
     */
    public void printTag(String name, Map<String, ? extends Object> parameters) {
        printTag(name, parameters, true, true, false);
    }

    /**
     * Print an XML tag.
     *
     * @param name
     *            the tag name
     * @param parameters
     *            the tag attributes
     * @param shouldTab
     *            whether to add a tab or not before the tag
     * @param newLine
     *            whether to add a new line or not after the tag
     * @param singleTag
     *            writes a tag in the form &lt;name /&gt;
     *
     * @see #startTag(String, Map)
     * @see #startTag(String, Map, boolean)
     */
    public void printTag(String name, Map<String, ? extends Object> parameters, boolean shouldTab,
        boolean newLine, boolean singleTag) {
        StringBuffer sb = new StringBuffer();
        sb.append("<"); //$NON-NLS-1$
        sb.append(name);
        if (parameters != null) {
            for (Iterator<String> it = parameters.keySet().iterator(); it.hasNext();) {
                sb.append(" "); //$NON-NLS-1$
                String key = it.next();
                sb.append(key);
                sb.append("=\""); //$NON-NLS-1$
                sb.append(getEscaped(String.valueOf(parameters.get(key))));
                sb.append("\""); //$NON-NLS-1$
            }
        }
        if (singleTag) {
            sb.append("/"); //$NON-NLS-1$
        }
        sb.append(">"); //$NON-NLS-1$
        if (shouldTab) {
            printTabulation();
        }
        if (newLine) {
            println(sb.toString());
        } else {
            print(sb.toString());
        }
    }

    /**
     * Write the start of an XML element.
     *
     * @param name
     *            the name of the element
     * @param parameters
     *            the attributes of the element
     */
    public void startTag(String name, Map<String, ? extends Object> parameters) {
        startTag(name, parameters, true);
    }

    /**
     * Write the start of an XML element.
     *
     * @param name
     *            the name of the element
     * @param parameters
     *            the attributes of the element
     * @param newLine
     *            whether to add a line after the tag or not.
     */
    public void startTag(String name, Map<String, ? extends Object> parameters, boolean newLine) {
        printTag(name, parameters, true, newLine, false);
        mTab++;
    }

    /**
     * Safely add a character to the buffer, replaces it by the corresponding XML entity if needed.
     *
     * @param buffer
     *            where to write the character
     * @param c
     *            the character to add
     */
    private static void appendEscapedChar(StringBuffer buffer, char c) {
        String replacement = getReplacement(c);
        if (replacement != null) {
            buffer.append('&');
            buffer.append(replacement);
            buffer.append(';');
        } else {
            buffer.append(c);
        }
    }

    /**
     * Replace the XML problematic characters by their entities in the string.
     *
     * @param pS
     *            the string to escape
     *
     * @return the same string with the XML entities instead.
     */
    public static String getEscaped(String pS) {
        StringBuffer result = new StringBuffer(pS.length() + BUFFER_FREE_SPACE);
        for (int i = 0; i < pS.length(); ++i) {
            appendEscapedChar(result, pS.charAt(i));
        }
        return result.toString();
    }

    /**
     * Get the XML entity name for a character, or <code>null</code> if there is no replacement for this character.
     *
     * @param c
     *            the character for which to get an XML entity
     * @return the XML entity name
     */
    private static String getReplacement(char c) {
        // Encode special XML characters into the equivalent character references.
        // These five are defined by default for all XML documents.
        String result = null;
        switch (c) {
            case '<':
                result = "lt"; //$NON-NLS-1$
            case '>':
                result = "gt"; //$NON-NLS-1$
            case '"':
                result = "quot"; //$NON-NLS-1$
            case '\'':
                result = "apos"; //$NON-NLS-1$
            case '&':
                result = "amp"; //$NON-NLS-1$
        }
        return result;
    }
}
