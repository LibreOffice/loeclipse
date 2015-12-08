/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Cédric Bosdonnat.
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2009 by Cédric Bosdonnat.
 *
 * All Rights Reserved.
 * 
 ************************************************************************/
package org.openoffice.plugin.core.model;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Class representing the file types for the entries of the manifest.xml file.
 * 
 * @author Cédric Bosdonnat
 *
 */
public class FileType {
    
    public static final String MIME_XCU = "application/vnd.sun.star.configuration-data";
    public static final String MIME_XCS = "application/vnd.sun.star.configuration-schema";
    public static final String MIME_DIALOG_LIB = "application/vnd.sun.star.dialog-library";
    public static final String MIME_BASIC_LIB = "application/vnd.sun.star.basic-library";
    public static final String MIME_UNO_TYPES = "application/vnd.sun.star.uno-typelibrary";
    public static final String MIME_UNO_COMPONENT = "application/vnd.sun.star.uno-component";
    public static final String MIME_DESCRIPTION = "application/vnd.sun.star.package-bundle-description";
    
    public static final String PARAM_LOCALE = "locale";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_PLATFORM = "platform";

    String mMimeType;
    HashMap< String, String > mParams = new HashMap<String, String>();
    
    /**
     * Constructor.
     * 
     * @param pMime the mime type of the file.
     */
    public FileType( String pMime ) {
        mMimeType = pMime;
    }
    
    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mMimeType;
    }
    
    /**
     * @param pName the name of the parameter to add
     * @param pValue the value of the parameter to add
     */
    public void addParam( String pName, String pValue ) { 
        mParams.put( pName, pValue );
    }
    
    /**
     * @param pName the name of the parameter to remove
     */
    public void removeParam( String pName ) {
        mParams.remove( pName );
    }
    
    /**
     * @return a copy of the parameters map. Changing this object will have no effect.
     */
    public HashMap< String, String > getParams( ) {
        HashMap<String, String> copy = new HashMap<String, String>( );
        copy.putAll( mParams );
        return copy;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String type = mMimeType;
        
        // Output the params if any
        Iterator< Entry< String, String > > iter = mParams.entrySet().iterator();
        String paramPattern = ";{0}={1}"; 
        while ( iter.hasNext() ) {
            Entry< String, String > entry = iter.next();
            type += MessageFormat.format( paramPattern, entry.getKey(), entry.getValue() );
        }
        
        return type;
    }
}
