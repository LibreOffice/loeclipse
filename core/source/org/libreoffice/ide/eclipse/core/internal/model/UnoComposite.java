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
package org.libreoffice.ide.eclipse.core.internal.model;

import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.libreoffice.ide.eclipse.core.model.IUnoComposite;

/**
 * Implements the UNO-IDL model composite. This class could certainly be rewritten using the Java Format tools.
 */
public class UnoComposite implements IUnoComposite {

    private Vector<IUnoComposite> mChildren = new Vector<IUnoComposite>();

    private int mType = COMPOSITE_TYPE_NOTSET;

    private Hashtable<String, Object> mProperties;
    private String mTemplate;
    private String mFilename;
    private String mSeparator = ""; //$NON-NLS-1$

    private boolean mIndentation = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        removeAll();
        if (mProperties != null) {
            mProperties.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IUnoComposite[] getChildren() {

        IUnoComposite[] composites = new IUnoComposite[mChildren.size()];
        for (int i = 0, length = mChildren.size(); i < length; i++) {
            composites[i] = mChildren.get(i);
        }

        return composites;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChild(IUnoComposite pChild) {

        if (pChild != null) {
            mChildren.add(pChild);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAll() {

        IUnoComposite[] composites = getChildren();

        for (int i = 0; i < composites.length; i++) {
            IUnoComposite compositei = composites[i];
            compositei.dispose();
            mChildren.removeElement(compositei);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setType(int pType) {

        if (mType == COMPOSITE_TYPE_NOTSET && (pType == COMPOSITE_TYPE_FILE || pType == COMPOSITE_TYPE_FOLDER
            || pType == COMPOSITE_TYPE_TEXT)) {

            mType = pType;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType() {
        return mType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Hashtable<String, Object> pProperties, String pTemplate) {

        mTemplate = pTemplate;
        String[] parts = splitTemplate();
        mProperties = new Hashtable<String, Object>();

        // Get the variable parts and their name
        for (int i = 0; i < parts.length; i++) {

            String parti = parts[i];
            Matcher matcher = Pattern.compile("\\$\\{(\\w+)\\}").matcher(parti); //$NON-NLS-1$

            // If the part is "${children}", it's not a property
            if (!parti.equals("${children}") && matcher.matches()) { //$NON-NLS-1$

                String namei = matcher.group(1);
                if (pProperties.containsKey(namei)) {
                    mProperties.put(namei, pProperties.get(namei));
                } else {
                    // The property isn't described in the vector.
                    mProperties.put(namei, ""); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(String pFilename) {

        if (mType == COMPOSITE_TYPE_FILE || mType == COMPOSITE_TYPE_FOLDER) {
            mFilename = pFilename;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIndented(boolean pToIndent) {
        if (mType == COMPOSITE_TYPE_TEXT) {
            mIndentation = pToIndent;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChildrenSeparator(String pSeparator) {
        if (pSeparator == null) {
            pSeparator = ""; //$NON-NLS-1$
        }
        mSeparator = pSeparator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(boolean pForce) throws Exception {

        File file;
        if (mType == COMPOSITE_TYPE_FILE || mType == COMPOSITE_TYPE_FOLDER) {

            file = new File(mFilename);

            // Create the parent directories
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            // if the file exists and the force flag is up
            if (file.exists() && pForce || !file.exists()) {
                if (mType == COMPOSITE_TYPE_FILE) {
                    file.createNewFile();

                    // Write the children toString() in the file
                    FileWriter out = new FileWriter(file);
                    String content = new String();
                    IUnoComposite[] composites = getChildren();
                    for (int i = 0; i < composites.length; i++) {
                        content = content + composites[i].toString();
                    }
                    out.write(content);
                    out.close();

                } else {
                    file.mkdir();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        String result = new String();

        if (mType == COMPOSITE_TYPE_TEXT) {

            // String reconstruction
            String[] parts = splitTemplate();

            for (int i = 0; i < parts.length; i++) {
                String parti = parts[i];

                if (parti.equals("${children}")) { //$NON-NLS-1$

                    IUnoComposite[] composites = getChildren();
                    for (int j = 0; j < composites.length; j++) {
                        if (composites[j].getType() == COMPOSITE_TYPE_TEXT) {
                            result += composites[j].toString() + mSeparator;
                        }
                    }

                    if (composites.length > 0) {
                        result = result.substring(0, result.length() - mSeparator.length());
                    }

                } else {

                    Matcher matcher = Pattern.compile("\\$\\{(\\w+)\\}").matcher(parti); //$NON-NLS-1$
                    if (matcher.matches()) {
                        result = result + mProperties.get(matcher.group(1));
                    } else {
                        result = result + parti;
                    }
                }
            }

            // Indentation management
            result = indent(result);

        } else {
            result = super.toString();
        }

        return result;
    }

    /**
     * Indent each line of a text with a <code>"\t"</code>.
     *
     * <p>
     * Do not add a <code>\t</code> between <code>\n\n</code> or <code>\n$</code>
     * </p>
     *
     * @param pToIndent
     *            the text to indent.
     *
     * @return the indented text
     */
    private String indent(String pToIndent) {
        //
        if (mIndentation) {

            for (int i = 0; i < pToIndent.length(); i++) {

                // '\n' found
                if (pToIndent.charAt(i) == '\n' && i != pToIndent.length() - 1 && pToIndent.charAt(i + 1) != '\n') {

                    pToIndent = pToIndent.substring(0, i + 1) + "\t" + //$NON-NLS-1$
                        pToIndent.substring(i + 1);
                }
            }
            pToIndent = "\t" + pToIndent; //$NON-NLS-1$
        }
        return pToIndent;
    }

    /**
     * splits the template into text parts and variables.
     *
     * <p>
     * The state machine has two states: <em>TEXT_STATE</em> or <em>VARIABLE_STATE</em> if the last string found was
     * <code>"${"</code> or <code>"}"</code>.
     * </p>
     *
     * <p>
     * At the beginning the string is assumed to be in <em>TEXT_STATE</em>. The template copy will be checked for the
     * substrings <code>"${"</code> or <code>"}"</code> depending on the state. On each substring discovery, the
     * following operations will be done:
     *
     * <pre>
     *     pos = templateCopy position of the substring
     *     parts.add(templateCopy before pos)
     *     templateCopy = templateCopy from pos
     * </pre>
     *
     * And the loop will be executed until the <code>templateCopy</code> is empty or the substring is not found. In such
     * a case the operation will depend on the current state:
     * <ul>
     * <li><em>TEXT_STATE</em>: templateCopy is added as the last part</li>
     * <li><em>VARIABLE_STATE</em>: adds a <code>"}"</code> before to add as the last part</li>
     * </ul>
     * </p>
     *
     * @return an array containing each part in the right order
     */
    private String[] splitTemplate() {

        String templateCopy = new String(mTemplate);
        Vector<String> parts = new Vector<String>();

        /*
         */
        final int TEXT_STATE = 0;
        final int VARIABLE_STATE = 1;

        int state = TEXT_STATE;
        int pos = -1;

        do {

            // Find the position of the next substring
            if (state == TEXT_STATE) {
                pos = templateCopy.indexOf("${"); //$NON-NLS-1$
                if (pos != -1) {
                    state = VARIABLE_STATE;

                    parts.add(templateCopy.substring(0, pos));
                    templateCopy = templateCopy.substring(pos);
                }
            } else {
                // The "}" character has to be included with the variable part
                pos = templateCopy.indexOf("}"); //$NON-NLS-1$
                if (pos != -1) {
                    pos++;
                    state = TEXT_STATE;

                    parts.add(templateCopy.substring(0, pos));
                    templateCopy = templateCopy.substring(pos);
                }
            }

        } while (pos != -1 && !templateCopy.equals("")); //$NON-NLS-1$

        // manages the last part
        if (state == VARIABLE_STATE && !templateCopy.equals("") && //$NON-NLS-1$
            !templateCopy.endsWith("}")) { //$NON-NLS-1$
            templateCopy += "}"; //$NON-NLS-1$
        }

        // Adds the last part
        if (!templateCopy.equals("")) { //$NON-NLS-1$
            parts.add(templateCopy);
        }

        return parts.toArray(new String[parts.size()]);
    }
}
