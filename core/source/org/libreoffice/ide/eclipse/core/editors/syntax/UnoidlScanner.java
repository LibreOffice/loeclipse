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
package org.libreoffice.ide.eclipse.core.editors.syntax;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.libreoffice.ide.eclipse.core.editors.idl.Colors;
import org.libreoffice.ide.eclipse.core.editors.utils.ColorProvider;

/**
 * UNO-IDL code scanner. Used by the UNO-IDL viewer configuration. In order to fully understand the editor mechanisms,
 * please report to Eclipse plugin developer's guide.
 */
public class UnoidlScanner extends RuleBasedScanner implements IUnoidlSyntax {

    private ColorProvider mColorProvider;

    /**
     * Default constructor, initializing the rules to apply in the uno-idl code.
     *
     * @param colorProvider
     *            a color provider to colorize the resulting tokens
     */
    public UnoidlScanner(ColorProvider colorProvider) {
        mColorProvider = colorProvider;

        // Tokens' definitions
        IToken keyword = new Token(new TextAttribute(mColorProvider.getColor(Colors.C_KEYWORD), null, SWT.BOLD));

        IToken type = new Token(new TextAttribute(mColorProvider.getColor(Colors.C_TYPE), null, SWT.BOLD));

        IToken modifier = new Token(new TextAttribute(mColorProvider.getColor(Colors.C_MODIFIER)));

        IToken string = new Token(new TextAttribute(mColorProvider.getColor(Colors.C_STRING)));

        IToken other = new Token(new TextAttribute(mColorProvider.getColor(Colors.C_TEXT)));

        setDefaultReturnToken(other);

        // Add word rule for keywords, types and constants
        WordRule wordRule = new WordRule(new UnoidlWordDetector(), other);
        for (int i = 0, length = RESERVED_WORDS.length; i < length; i++) {
            wordRule.addWord(RESERVED_WORDS[i], keyword);
        }
        for (int i = 0, length = CONSTANTS.length; i < length; i++) {
            wordRule.addWord(CONSTANTS[i], keyword);
        }
        for (int i = 0, length = TYPES.length; i < length; i++) {
            wordRule.addWord(TYPES[i], type);
        }
        for (int i = 0, length = MODIFIERS.length; i < length; i++) {
            wordRule.addWord(MODIFIERS[i], modifier);
        }

        IRule[] rules = new IRule[] {

            // Add rules for strings and character constants
            new SingleLineRule("\"", "\"", string, '\\'), //$NON-NLS-1$ //$NON-NLS-2$
            new SingleLineRule("'", "'", string, '\\'), //$NON-NLS-1$ //$NON-NLS-2$

            // Add generic whitespace rule
            new WhitespaceRule(new UnoidlWhiteSpaceDetector()),

            wordRule };
        setRules(rules);
    }
}