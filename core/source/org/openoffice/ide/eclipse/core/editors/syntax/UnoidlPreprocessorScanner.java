/*************************************************************************
 *
 * $RCSfile: UnoidlPreprocessorScanner.java,v $
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
package org.openoffice.ide.eclipse.core.editors.syntax;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.openoffice.ide.eclipse.core.editors.idl.Colors;
import org.openoffice.ide.eclipse.core.editors.utils.ColorProvider;

/**
 * Scanner splitting the preprocessor commands into items to be colorized.
 * In order to fully understand the editor mechanisms, please report to
 * Eclipse plugin developer's guide.
 *
 * @author cedricbosdo
 */
public class UnoidlPreprocessorScanner extends RuleBasedScanner {

    /**
     * The preprocessor commands to match.
     */
    public static final String[] PREPROC_COMMANDS = {
                    "include", //$NON-NLS-1$
                    "ifdef", //$NON-NLS-1$
                    "ifndef", //$NON-NLS-1$
                    "endif", //$NON-NLS-1$
                    "elif", //$NON-NLS-1$
                    "else", //$NON-NLS-1$
                    "define", //$NON-NLS-1$
                    "undef", //$NON-NLS-1$
                    "line", //$NON-NLS-1$
                    "error", //$NON-NLS-1$
                    "pragma" //$NON-NLS-1$
    };

    /**
     * Constructor initializing the rules for the preprocessor command analysis.
     *
     * @param pColorManager the color manager from where to get the colors
     */
    public UnoidlPreprocessorScanner(ColorProvider pColorManager) {

        IToken path = new Token(
                        new TextAttribute(pColorManager.getColor(Colors.C_PREPROCESSOR),
                                        null, SWT.ITALIC));

        IToken condition = new Token(
                        new TextAttribute(pColorManager.getColor(Colors.C_PREPROCESSOR),
                                        null, SWT.ITALIC));

        IToken definition = condition;

        IToken other = new Token(
                        new TextAttribute(pColorManager.getColor(Colors.C_PREPROCESSOR)));

        IToken command = new Token (
                        new TextAttribute(pColorManager.getColor(Colors.C_PREPROCESSOR),
                                        null, SWT.BOLD));


        WordRule wordRule = new WordRule(new UnoidlWordDetector(),other);
        for (int i = 0; i < PREPROC_COMMANDS.length; i++) {
            wordRule.addWord(PREPROC_COMMANDS[i], command);
        }
        IRule[] rules = new IRule[] {
                        new SingleLineRule("<", ">", path), //$NON-NLS-1$ //$NON-NLS-2$
                        new SingleLineRule("\"", "\"", path), //$NON-NLS-1$ //$NON-NLS-2$
                        new RegexRule("\\p{Blank}+[a-zA-Z]+$", condition), //$NON-NLS-1$
                        new EqualityNameRule(definition),
                        wordRule
        };

        setRules(rules);
    }
}
