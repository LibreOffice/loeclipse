/*************************************************************************
 *
 * $RCSfile: ZipContentHelper.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:39 $
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
package org.openoffice.ide.eclipse.java.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.Platform;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.utils.ZipContent;

/**
 * This class is a small structure containing the data to zip for one file.
 * 
 * @author cedricbosdo
 *
 */
public class ZipContentHelper {
    
    /**
     * Get all the ZipContent entries for the given file or directory.
     * 
     * @param pFile the file or directory from which to get all the
     *      contents as {@link ZipContent} objects
     * 
     * @return the {@link ZipContent} object for the file or the directory
     *      and its content.
     */
    public static ZipContent[] getFiles(File pFile) {
        return getInternalFiles(pFile, pFile);
    }
    
    /**
     * Get all the ZipContent entries for the given file or directory.
     * 
     * @param pFile the file or directory from which to get all the
     *      contents as {@link ZipContent} objects
     * @param pRootDir the root directory to use to compute the {@link ZipContent}
     *      relative path.
     * 
     * @return the {@link ZipContent} object for the file or the directory
     *      and its content.
     */
    private static ZipContent[] getInternalFiles(File pFile, File pRootDir) {
        
        ZipContent[] contents = new ZipContent[0];
        
        ArrayList<ZipContent> result = new ArrayList<ZipContent>();
        File[] files = pFile.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File filei = files[i];
                if (filei.isFile() && !filei.getName().endsWith("urd")) { //$NON-NLS-1$
                    
                    String filePath = filei.getAbsolutePath();
                    String relativePath = filePath.substring(
                            pRootDir.getAbsolutePath().length() + 1);
                    if (Platform.getOS().equals(Platform.OS_WIN32)) {
                        relativePath = relativePath.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    ZipContent content = new ZipContent(relativePath, filei);

                    result.add(content);
                } else {
                    if (!filei.getName().equals("urd")) { //$NON-NLS-1$
                        ZipContent[] tmpContents = getInternalFiles(filei, pRootDir);
                        result.addAll(Arrays.asList(tmpContents));
                    }
                }
            }

            contents = new ZipContent[result.size()];
            contents = result.toArray(contents);
        } else {
            PluginLogger.warning(Messages.getString("ZipContentHelper.NotDirectoryError") + pFile); //$NON-NLS-1$
        }
        return contents;
    }
}
