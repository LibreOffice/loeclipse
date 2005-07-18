/*************************************************************************
 *
 * $RCSfile: UnoidlPreprocessorScanner.java,v $
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
public class UnoidlPreprocessorScanner extends RuleBasedScanner {
	
	public static final String[] PREPROC_COMMANDS = {
		"include",
		"ifdef",
		"ifndef",
		"endif",
		"elif",
		"else",
		"define",
		"undef",
		"line",
		"error",
		"pragma"
	};
	
	public UnoidlPreprocessorScanner(ColorProvider colorManager){
		
		IToken path = new Token(
				new TextAttribute(colorManager.getColor(Colors.C_PREPROCESSOR),
						null, SWT.ITALIC));
		
		IToken condition = new Token(
				new TextAttribute(colorManager.getColor(Colors.C_PREPROCESSOR),
						null, SWT.ITALIC));
		
		IToken definition = condition;
		
		IToken other = new Token(
				new TextAttribute(colorManager.getColor(Colors.C_PREPROCESSOR)));
		
		IToken command = new Token (
				new TextAttribute(colorManager.getColor(Colors.C_PREPROCESSOR),
						null, SWT.BOLD));
		
		IRule[] rules = new IRule[5];
		
		// FIXME Create adapted rules for the #ifdef, #ifndef and #define 
		
		rules[0] = new SingleLineRule("<", ">", path);
		rules[1] = new SingleLineRule("\"", "\"", path);
		rules[2] = new RegexRule("\\p{Blank}+[a-zA-Z]+$", condition);
		rules[3] = new EqualityNameRule(definition);
		
		WordRule wordRule = new WordRule(new UnoidlWordDetector(),other);
		for (int i = 0; i < PREPROC_COMMANDS.length; i++){
			wordRule.addWord(PREPROC_COMMANDS[i], command);
		}
		rules[4] = wordRule;
		
		setRules(rules);
	}
}
