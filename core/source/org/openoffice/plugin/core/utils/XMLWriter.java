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
package org.openoffice.plugin.core.utils;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

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
     * @param pOutput where to write the XML
     * @throws UnsupportedEncodingException if the UTF8 charset isn't supported (would be strange)
     */
    public XMLWriter(OutputStream pOutput) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(pOutput, "UTF8")); //$NON-NLS-1$
        mTab = 0;
        println(XML_VERSION);
    }

    /**
     * Write the end of an XML tag.
     * 
     * @param pName the name of the tag
     */
    public void endTag(String pName) {
        mTab--;
        printTag('/' + pName, null);
    }
    
    /**
     * Write the end of an XML tag.
     * 
     * @param pName the name of the tag
     * @param pIndentation whether to print the indentation or not
     */
    public void endTag(String pName, boolean pIndentation ) {
        mTab--;
        printTag('/' + pName, null, pIndentation, true, false );
    }

    /**
     * Write a simple XML tag, on the form &lt;name&gt;value&lt;/name&gt;.
     * 
     * @param pName the name of the tag
     * @param pValue the value
     */
    public void printSimpleTag(String pName, Object pValue) {
        if (pValue != null) {
            printTag(pName, null, true, false, false);
            print(getEscaped(String.valueOf(pValue)));
            printTag('/' + pName, null, false, true, false);
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
     * @param pName the tag name
     * @param pParameters the tag attributes
     * 
     * @see #startTag(String, HashMap)
     * @see #startTag(String, HashMap, boolean)
     */
    public void printSingleTag(String pName, HashMap<String, ? extends Object> pParameters) {
        printTag(pName, pParameters, true, true, true);
    }
    
    /**
     * Print an XML Tag.
     * 
     * @param pName the tag name
     * @param pParameters the tag attributes
     * 
     * @see #startTag(String, HashMap)
     * @see #startTag(String, HashMap, boolean)
     */
    public void printTag(String pName, HashMap<String, ? extends Object> pParameters) {
        printTag(pName, pParameters, true, true, false);
    }

    /**
     * Print an XML tag.
     * 
     * @param pName the tag name
     * @param pParameters the tag attributes
     * @param pShouldTab whether to add a tab or not before the tag
     * @param pNewLine whether to add a new line or not after the tag
     * @param pSingleTag writes a tag in the form &lt;name /&gt;
     * 
     * @see #startTag(String, HashMap)
     * @see #startTag(String, HashMap, boolean)
     */
    public void printTag(String pName, HashMap<String, ? extends Object> pParameters, 
            boolean pShouldTab, boolean pNewLine, boolean pSingleTag ) {
        StringBuffer sb = new StringBuffer();
        sb.append("<"); //$NON-NLS-1$
        sb.append(pName);
        if (pParameters != null) {
            for (Iterator<String> it = pParameters.keySet().iterator(); it.hasNext();) {
                sb.append(" "); //$NON-NLS-1$
                String key = it.next();
                sb.append(key);
                sb.append("=\""); //$NON-NLS-1$
                sb.append(getEscaped(String.valueOf(pParameters.get(key))));
                sb.append("\""); //$NON-NLS-1$
            }
        }
        if ( pSingleTag ) {
            sb.append( "/" ); //$NON-NLS-1$
        }
        sb.append(">"); //$NON-NLS-1$
        if (pShouldTab) {
            printTabulation();
        }
        if (pNewLine) {
            println(sb.toString());
        } else {
            print(sb.toString());
        }
    }

    /**
     * Write the start of an XML element.
     * 
     * @param pName the name of the element
     * @param pParameters the attributes of the element
     */
    public void startTag(String pName, HashMap<String, ? extends Object> pParameters) {
        startTag(pName, pParameters, true);
    }

    /**
     * Write the start of an XML element.
     * 
     * @param pName the name of the element
     * @param pParameters the attributes of the element
     * @param pNewLine whether to add a line after the tag or not.
     */
    public void startTag(String pName, HashMap<String, ? extends Object> pParameters, boolean pNewLine) {
        printTag(pName, pParameters, true, pNewLine, false);
        mTab++;
    }

    /**
     * Safely add a character to the buffer, replaces it by the corresponding XML entity
     * if needed.
     * 
     * @param pBuffer where to write the character
     * @param pC the character to add
     */
    private static void appendEscapedChar(StringBuffer pBuffer, char pC) {
        String replacement = getReplacement(pC);
        if (replacement != null) {
            pBuffer.append('&');
            pBuffer.append(replacement);
            pBuffer.append(';');
        } else {
            pBuffer.append(pC);
        }
    }

    /**
     * Replace the XML problematic characters by their entities in the string.
     *   
     * @param pS the string to escape
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
     * Get the XML entity name for a character, or <code>null</code> if there is no 
     * replacement for this character.
     * 
     * @param pC the character for which to get an XML entity
     * @return the XML entity name
     */
    private static String getReplacement(char pC) {
        // Encode special XML characters into the equivalent character references.
        // These five are defined by default for all XML documents.
        String result = null;
        switch (pC) {
            case '<' :
                result = "lt"; //$NON-NLS-1$
            case '>' :
                result = "gt"; //$NON-NLS-1$
            case '"' :
                result = "quot"; //$NON-NLS-1$
            case '\'' :
                result = "apos"; //$NON-NLS-1$
            case '&' :
                result = "amp"; //$NON-NLS-1$
        }
        return result;
    }
}
