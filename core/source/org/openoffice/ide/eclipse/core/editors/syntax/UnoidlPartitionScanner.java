/*************************************************************************
 *
 * $RCSfile: UnoidlPartitionScanner.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/06/09 06:13:59 $
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
package org.openoffice.ide.eclipse.core.editors.syntax;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

/**
 * Scanner splitting the text into partitions. In order to fully understand
 * the editor mechanisms, please report to Eclipse plugin developer's guide.
 * 
 * @author cbosdonnat
 */
public class UnoidlPartitionScanner extends RuleBasedPartitionScanner {
	
	public final static String IDL_AUTOCOMMENT = "_idl_autocomment";
	public final static String IDL_COMMENT = "_idl_comment";
	public final static String IDL_PREPROCESSOR = "_idl_preprocessor";
	
	/**
	 * Constructor defining the rules to use to match the different partitions
	 */
	public UnoidlPartitionScanner() {
		IToken idlComment = new Token(IDL_COMMENT);
		IToken idlAutoComment = new Token(IDL_AUTOCOMMENT);
		IToken idlPreprocessor = new Token(IDL_PREPROCESSOR);
		
		IPredicateRule[] rules = new IPredicateRule[5];
		rules[0] = new MultiLineRule("/**", "*/", idlAutoComment);
		rules[1] = new EndOfLineRule("///", idlAutoComment);
		rules[2] = new MultiLineRule("/*", "*/", idlComment);
		rules[3] = new EndOfLineRule("//", idlComment);
		rules[4] = new EndOfLineRule("#", idlPreprocessor);
		setPredicateRules(rules);
	}
}
