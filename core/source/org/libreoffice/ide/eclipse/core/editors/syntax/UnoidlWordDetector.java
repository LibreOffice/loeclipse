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

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * Basic rule to detect words in the text.
 *
 */
public class UnoidlWordDetector implements IWordDetector {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWordStart(char ch) {
        boolean isLower = ch >= 'a' && ch <= 'z';
        boolean isUpper = ch >= 'A' && ch <= 'Z';

        return isLower || isUpper || ch == '.' || ch == '_';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWordPart(char ch) {
        return isWordStart(ch) || ch >= '0' && ch <= '9';
    }
}
