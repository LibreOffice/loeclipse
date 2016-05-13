/*************************************************************************
 *
 * $RCSfile: EqualityNameRule.java,v $
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

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;

/**
 * Rule using a regex rule to highlight an equality. For example, the following lines are valid for this rule:
 * <ul>
 * <li>foo=</li>
 * <li>foo=foo</li>
 * <li>foo = foo</li>
 * </ul>
 * However "=foo" wont be accepted.
 *
 *
 */
public class EqualityNameRule extends RegexRule {

    /**
     * Constructor.
     *
     * @param pToken
     *            the token to analyze.
     */
    public EqualityNameRule(IToken pToken) {
        super("[a-zA-Z]+\\p{Blank}?=", pToken); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IToken evaluate(ICharacterScanner pScanner) {
        IToken result = super.evaluate(pScanner);
        if (result == getToken()) {
            pScanner.unread();
        }
        return result;
    }
}