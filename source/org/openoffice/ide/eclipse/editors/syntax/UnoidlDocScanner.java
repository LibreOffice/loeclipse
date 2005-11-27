/*************************************************************************
 *
 * $RCSfile: UnoidlDocScanner.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:24 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.editors.syntax;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.openoffice.ide.eclipse.editors.ColorProvider;
import org.openoffice.ide.eclipse.editors.Colors;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlDocScanner extends RuleBasedScanner {
	
	public UnoidlDocScanner(ColorProvider colorManager) {
		
		IToken body = new Token (
				new TextAttribute(colorManager.getColor(Colors.C_AUTODOC_COMMENT)));
		
		IToken tag = new Token (
				new TextAttribute(colorManager.getColor(Colors.C_AUTODOC_COMMENT),
						          null, 
						          SWT.BOLD));
		IToken xmlTag = new Token (
				new TextAttribute(colorManager.getColor(Colors.C_XML_TAG)));

		
		IRule[] rules = new IRule[2];
		rules[0] = new SingleLineRule("<", ">", xmlTag);  
		rules[1] = new RegexRule("@[a-zA-Z]+", tag);
		
		setRules(rules);
		
		setDefaultReturnToken(body);
	}
}
