/*************************************************************************
 *
 * $RCSfile: UnoidlScanner.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/07/18 19:36:01 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the following licenses
 *
 *     - GNU Lesser General Public License Version 2.1
 *     - Sun Industry Standards Source License Version 1.1
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
 *
 * Sun Industry Standards Source License Version 1.1
 * =================================================
 * The contents of this file are subject to the Sun Industry Standards
 * Source License Version 1.1 (the "License"); You may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.openoffice.org/license.html.
 *
 * Software provided under this License is provided on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 * MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 * See the License for the specific provisions governing your rights and
 * obligations concerning the Software.
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
package org.openoffice.ide.eclipse.editors.syntax;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.openoffice.ide.eclipse.editors.ColorProvider;
import org.openoffice.ide.eclipse.editors.Colors;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlScanner extends RuleBasedScanner implements IUnoidlSyntax{
	
	private ColorProvider provider;
	
	public UnoidlScanner(ColorProvider cp) {
		provider = cp;
		
		// Tokens' definitions
		IToken keyword = new Token(
			new TextAttribute(provider.getColor(Colors.C_KEYWORD),
				null,
				SWT.BOLD));
				
		IToken type = new Token(
			new TextAttribute(
				provider.getColor(Colors.C_TYPE),
				null,
				SWT.BOLD
			)
		);
		
		IToken modifier = new Token(
			new TextAttribute(provider.getColor(Colors.C_MODIFIER)));
		
		IToken string = new Token(
			new TextAttribute(provider.getColor(Colors.C_STRING))
		);
		
		IToken other = new Token(
			new TextAttribute(provider.getColor(Colors.C_TEXT))
		);
		
		setDefaultReturnToken(other);
		
		IRule[] rules = new IRule[4];
		
		//Add rules for strings and character constants
		rules[0] = new SingleLineRule("\"","\"", string, '\\');
		rules[1] = new SingleLineRule("'","'", string, '\\');
		
		//Add generic whitespace rule
		rules[2] = new WhitespaceRule(new UnoidlWhiteSpaceDetector());
		
		//Add word rule for keywords, types and constants
		WordRule wordRule = new WordRule(new UnoidlWordDetector(),other);
		for (int i = 0, length=RESERVED_WORDS.length; i<length; i++){
			wordRule.addWord(RESERVED_WORDS[i], keyword);
		}		
		for (int i = 0, length=CONSTANTS.length; i<length; i++){
			wordRule.addWord(CONSTANTS[i], keyword);
		}
		for (int i = 0, length=TYPES.length; i<length; i++){
			wordRule.addWord(TYPES[i], type);
		}
		for (int i=0, length=MODIFIERS.length; i<length; i++){
			wordRule.addWord(MODIFIERS[i], modifier);
		}
		
		rules[3] = wordRule;
		setRules(rules);
	}

}