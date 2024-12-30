/*************************************************************************
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
package org.libreoffice.plugin.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class for file handling.
 */
public class FileHelper {

    /**
     * Converts all separators to the Unix separator of forward slash.
     *
     * @param pPath
     *            the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToUnix(String pPath) {
        String path;
        if (pPath == null || pPath.indexOf('/') == -1) {
            path = pPath;
        } else {
            path = pPath.replace('\\', '/');
        }
        return path;
    }

    /**
     * Moves the src directory or file to the dst container.
     *
     * @param pSrc
     *            the file or directory to move
     * @param pDst
     *            the destination directory
     * @param pForce
     *            if set to <code>true</code>, overwrites the existing files
     *
     * @throws IOException
     *             is thrown when one of the parameters is null or the underlying file doesn't exists. This exception
     *             can also be thrown if the writing rights are missing on dst
     */
    public static void move(File pSrc, File pDst, boolean pForce) throws IOException {

        // Check for invalid arguments
        if (pSrc == null || !pSrc.canRead()) {
            String path = "";
            if (pSrc != null) {
                path = pSrc.getAbsolutePath();
            }
            throw new IOException("FileHelper.ReadError" + path);
        }

        if (pDst == null || !pDst.canWrite()) {
            String path = "";
            if (pDst != null) {
                path = pDst.getAbsolutePath();
            }
            throw new IOException("FileHelper.WriteError" + path);
        }

        // Now really move the content
        if (pSrc.isFile()) {
            copyFile(pSrc, new File(pDst, pSrc.getName()), pForce);
            pSrc.delete();
        } else {

            File dstdir = new File(pDst, pSrc.getName());

            // if the new dir doesn't exists, then create it
            if (!dstdir.exists()) {
                dstdir.mkdir();
            }

            // copy each contained file
            File[] files = pSrc.listFiles();
            for (int i = 0; i < files.length; i++) {
                File filei = files[i];
                move(filei, dstdir, pForce);
                filei.delete();
            }

            pSrc.delete();
        }
    }

    /**
     * Copy the file src into the file dst. If the dst file already exists, it will be deleted before to start copying
     * if force is set to <code>true</code>, otherwise nothing will be done.
     *
     * @param pSrc
     *            the original file
     * @param pDst
     *            the file to create
     * @param pForce
     *            overwrite the existing destination if any
     *
     * @throws IOException
     *             is thrown if
     *             <ul>
     *             <li>the src file is <code>null</code> or isn't readable,</li>
     *             <li>dst is <code>null</code></li>
     *             <li>the writing process fails</li>
     *             </ul>
     */
    public static void copyFile(File pSrc, File pDst, boolean pForce) throws IOException {

        // Check for invalid arguments
        if (pSrc == null || !pSrc.canRead()) {
            String msg = "FileHelper.ReadError";
            if (pSrc != null) {
                msg += " " + pSrc.getAbsolutePath();
            }
            throw new IOException(msg);
        }
        if (pDst == null) {
            throw new IOException("FileHelper.NullDestinationError");
        }

        // clean the existing file if any and force
        if (pForce && pDst.exists() && pDst.isFile()) {
            pDst.delete();
        }

        // now copy the file
        if (!pDst.exists()) {
            copyFile(pSrc, pDst);
        }
    }

    private static void copyFile(File pSrc, File pDst) throws IOException {
        FileInputStream in = new FileInputStream(pSrc);
        FileOutputStream out = new FileOutputStream(pDst);

        try {
            int c = in.read();
            while (c != -1) {
                out.write(c);
                c = in.read();
            }
        } finally {
            try {
                in.close();
                out.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Removes a file or directory.
     *
     * @param pFile
     *            the file or directory to remove
     */
    public static void remove(File pFile) {
        if (pFile.isFile()) {
            pFile.delete();
        } else {
            String[] children = pFile.list();
            if (null != children) {
                for (String child : children) {
                    if (!child.equals(".") && !child.equals("..")) { //$NON-NLS-2$
                        File childFile = new File(pFile, child);
                        remove(childFile);
                    }
                }
            }
            pFile.delete();
        }
    }
}
