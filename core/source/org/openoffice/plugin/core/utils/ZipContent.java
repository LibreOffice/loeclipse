/*************************************************************************
 *
 * $RCSfile: ZipContent.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:31 $
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
package org.openoffice.plugin.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class is a small structure containing the data to ZIP for one file.
 * 
 * @author cedricbosdo
 *
 */
public class ZipContent {

    private static final int BUFFER_SIZE = 2048;

    protected File mResource;
    
    protected String mEntryName;
    
    /**
     * Constructor.
     * 
     * @param pEntryName the name in the ZIP file
     * @param pResource the file to put in the ZIP file.
     */
    public ZipContent( String pEntryName, File pResource ) {
        mResource = pResource;
        mEntryName = pEntryName;
    }
    
    /**
     * @return the file represented by the {@link ZipEntry}
     */
    public File getFile() {
        return mResource;
    }
    
    /**
     * Write the ZIP entry to the given Zip output stream.
     * 
     * @param pOutput the stream where to write the entry data.
     */
    public void writeContentToZip(ZipOutputStream pOutput) {
        
        BufferedInputStream origin = null;
        try {
            FileInputStream fi = new FileInputStream( mResource );
            origin = new BufferedInputStream(fi, BUFFER_SIZE);

            ZipEntry entry = new ZipEntry(mEntryName);
            pOutput.putNextEntry(entry);

            int count;
            byte data[] = new byte[BUFFER_SIZE];

            while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                pOutput.write(data, 0, count);
            }
            
            pOutput.closeEntry();
            
        } catch (IOException e) {
            System.err.println("Problem when writing file to zip: " + mEntryName + " (" + e.getLocalizedMessage() + ")");
        } finally {
            // Close the file entry stream
            try { 
                if (origin != null) {
                    origin.close();
                } 
            } catch (IOException e) {
            }
        }
    }
}
