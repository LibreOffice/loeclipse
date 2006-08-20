/*************************************************************************
 *
 * $RCSfile: UnoidlPartitionScanner.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:55:50 $
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
	
	public final static String IDL_AUTOCOMMENT = "_idl_autocomment"; //$NON-NLS-1$
	public final static String IDL_COMMENT = "_idl_comment"; //$NON-NLS-1$
	public final static String IDL_PREPROCESSOR = "_idl_preprocessor"; //$NON-NLS-1$
	
	/**
	 * Constructor defining the rules to use to match the different partitions
	 */
	public UnoidlPartitionScanner() {
		IToken idlComment = new Token(IDL_COMMENT);
		IToken idlAutoComment = new Token(IDL_AUTOCOMMENT);
		IToken idlPreprocessor = new Token(IDL_PREPROCESSOR);
		
		IPredicateRule[] rules = new IPredicateRule[5];
		rules[0] = new MultiLineRule("/**", "*/", idlAutoComment); //$NON-NLS-1$ //$NON-NLS-2$
		rules[1] = new EndOfLineRule("///", idlAutoComment); //$NON-NLS-1$
		rules[2] = new MultiLineRule("/*", "*/", idlComment); //$NON-NLS-1$ //$NON-NLS-2$
		rules[3] = new EndOfLineRule("//", idlComment); //$NON-NLS-1$
		rules[4] = new EndOfLineRule("#", idlPreprocessor); //$NON-NLS-1$
		setPredicateRules(rules);
	}
}
