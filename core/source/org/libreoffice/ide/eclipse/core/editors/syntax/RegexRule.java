/*************************************************************************
 *
 * $RCSfile: RegexRule.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:26 $
 *
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
package org.libreoffice.ide.eclipse.core.editors.syntax;

import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * A scanning rule matching a regular expression.
 *
 * @author cedricbosdo
 */
public class RegexRule implements IRule {

    private IToken mToken;
    private String mRegex;

    private int mCharReadNb = 0;
    private char[][] mDelimiters;

    /**
     * Constructor, initializing the token to return and the regex to match.
     *
     * @param pRegex
     *            the regular expression to match
     * @param pToken
     *            the token to associate
     */
    public RegexRule(String pRegex, IToken pToken) {
        mToken = pToken;
        mRegex = pRegex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IToken evaluate(ICharacterScanner pScanner) {
        mCharReadNb = 0;
        IToken result = Token.UNDEFINED;
        mDelimiters = pScanner.getLegalLineDelimiters();

        // Reads the characters and test the pattern matching
        String line = new String();
        boolean matchingDone = false;
        boolean matchingBegun = false;
        int c;

        do {
            c = pScanner.read();
            line = line + new Character((char) c);
            mCharReadNb++;

            if (!isEOL(c)) {
                if (Pattern.matches(mRegex, line)) {
                    matchingBegun = true;
                } else {
                    if (matchingBegun) {
                        matchingDone = true;
                        // Unread the bad character
                        pScanner.unread();
                        result = mToken;
                        line = line.substring(0, line.length() - 2);
                    }
                }
            } else {
                if (matchingBegun) {
                    matchingDone = true;
                    result = mToken;
                    // scanner.unread();
                }
            }
        } while (!isEOL(c) && !matchingDone);

        // Unread all the line except the first character if the line is not valid
        if (result != mToken) {
            for (int i = 0; i < mCharReadNb; i++) {
                pScanner.unread();
            }
        }

        return result;
    }

    /**
     * @return the associated token.
     */
    protected IToken getToken() {
        return mToken;
    }

    /**
     * Convenience method to determine if a character corresponds to an end of line.
     *
     * @param pChar
     *            the character to check
     *
     * @return <code>true</code> if the character is an end of line, <code>false</code> otherwise.
     */
    protected boolean isEOL(int pChar) {
        boolean isEol = false;

        for (int i = 0; i < mDelimiters.length; i++) {
            if (pChar == mDelimiters[i][0] || isEOF(pChar)) {
                isEol = true;
            }
        }
        return isEol;
    }

    /**
     * Convenience method to determine if a character corresponds to an end of file.
     *
     * @param pChar
     *            the character to check
     *
     * @return <code>true</code> if the character is an end of file, <code>false</code> otherwise.
     */
    protected boolean isEOF(int pChar) {
        boolean result = false;
        if (ICharacterScanner.EOF == pChar) {
            result = true;
        }
        return result;
    }
}
