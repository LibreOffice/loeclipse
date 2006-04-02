/*************************************************************************
 *
 * $RCSfile: RegexRule.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/02 20:13:05 $
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

import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * TODOC
 * 
 * @author cbosdonnat
 * 
 */
public class RegexRule implements IRule {

    private IToken token;
    private String regex;
    
    private int charReadNb = 0;
    private char[][] delimiters;
    
    public RegexRule(String aRegex, IToken aToken){
        token = aToken;
        regex = aRegex;
    }
    
    /**
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner) {
    	charReadNb = 0;
        IToken result = Token.UNDEFINED;
        delimiters = scanner.getLegalLineDelimiters();
        
        // Reads the characters and test the pattern matching
        String line = new String();
        boolean matchingDone = false;
        boolean matchingBegun = false;
        int c;
        
         do {
        	c = scanner.read();
            line = line + new Character((char)c);
            charReadNb++;
            
            if (!isEOL(c)){
                if (Pattern.matches(regex, line)){
                    matchingBegun = true;
                } else {
                	if (matchingBegun){
                		matchingDone = true;
                		scanner.unread(); // Unread the bad character
                		result = token;
                		line = line.substring(0, line.length()-2);
                	}
                }
            } else {
            	if (matchingBegun){
            		matchingDone = true;
            		result = token;
                	//scanner.unread();
            	}
            }
        }while (!isEOL(c) && !matchingDone);
        
        // Unread all the line except the first character if the line is not valid
        if (result != token){
	        for (int i=0; i<charReadNb; i++){
	            scanner.unread();
	        }
        }
        
        return result;
    }
  
    protected IToken getToken(){
    	return token;
    }
    
    protected boolean isEOL (int c){
 
        for (int i= 0; i < delimiters.length; i++) {
			if (c == delimiters[i][0] || isEOF(c)) {
				return true;
			}
		}
        return false;
    }
    
    protected boolean isEOF (int c){
        boolean result = false;
        if (ICharacterScanner.EOF == c){
            result = true;
        }
        return result;
    }
   
}
