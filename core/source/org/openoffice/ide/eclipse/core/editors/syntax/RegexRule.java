/*************************************************************************
 *
 * $RCSfile: RegexRule.java,v $
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

import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * A scanning rule matching a regular expression.
 * 
 * @author cbosdonnat
 */
public class RegexRule implements IRule {

    private IToken mToken;
    private String mRegex;
    
    private int mCharReadNb = 0;
    private char[][] mDelimiters;
    
    /**
     * Constructor, initializing the token to return and the regex to
     * match.
     * 
     * @param aRegex the regular expression to match
     * @param aToken the token to associate
     */
    public RegexRule(String aRegex, IToken aToken){
        mToken = aToken;
        mRegex = aRegex;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner) {
    	mCharReadNb = 0;
        IToken result = Token.UNDEFINED;
        mDelimiters = scanner.getLegalLineDelimiters();
        
        // Reads the characters and test the pattern matching
        String line = new String();
        boolean matchingDone = false;
        boolean matchingBegun = false;
        int c;
        
         do {
        	c = scanner.read();
            line = line + new Character((char)c);
            mCharReadNb++;
            
            if (!isEOL(c)){
                if (Pattern.matches(mRegex, line)){
                    matchingBegun = true;
                } else {
                	if (matchingBegun){
                		matchingDone = true;
                		scanner.unread(); // Unread the bad character
                		result = mToken;
                		line = line.substring(0, line.length()-2);
                	}
                }
            } else {
            	if (matchingBegun){
            		matchingDone = true;
            		result = mToken;
                	//scanner.unread();
            	}
            }
        }while (!isEOL(c) && !matchingDone);
        
        // Unread all the line except the first character if the line is not valid
        if (result != mToken){
	        for (int i=0; i<mCharReadNb; i++){
	            scanner.unread();
	        }
        }
        
        return result;
    }
  
    /**
     * Returns the associated token
     */
    protected IToken getToken(){
    	return mToken;
    }
    
    /**
     * Convenience method to determine if a character corresponds to an end
     * of line.
     */
    protected boolean isEOL (int c){
 
        for (int i= 0; i < mDelimiters.length; i++) {
			if (c == mDelimiters[i][0] || isEOF(c)) {
				return true;
			}
		}
        return false;
    }
    
    /**
     * Convenience method to determine if a character corresponds to an end
     * of file.
     */
    protected boolean isEOF (int c){
        boolean result = false;
        if (ICharacterScanner.EOF == c){
            result = true;
        }
        return result;
    }
}
