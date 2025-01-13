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
     * @param src
     *            the file or directory to move
     * @param dst
     *            the destination directory
     * @param force
     *            if set to <code>true</code>, overwrites the existing files
     *
     * @throws IOException
     *             is thrown when one of the parameters is null or the underlying file doesn't exists. This exception
     *             can also be thrown if the writing rights are missing on dst
     */
    public static void move(File src, File dst, boolean force) throws IOException {

        // Check for invalid arguments
        if (src == null || !src.canRead()) {
            String path = "";
            if (src != null) {
                path = src.getAbsolutePath();
            }
            throw new IOException("FileHelper.ReadError" + path);
        }

        if (dst == null || !dst.canWrite()) {
            String path = "";
            if (dst != null) {
                path = dst.getAbsolutePath();
            }
            throw new IOException("FileHelper.WriteError" + path);
        }

        // Now really move the content
        if (src.isFile()) {
            copyFile(src, new File(dst, src.getName()), force);
            src.delete();
        } else {

            File dstdir = new File(dst, src.getName());

            // if the new dir doesn't exists, then create it
            if (!dstdir.exists()) {
                dstdir.mkdir();
            }

            // copy each contained file
            File[] files = src.listFiles();
            for (int i = 0; i < files.length; i++) {
                File filei = files[i];
                move(filei, dstdir, force);
                filei.delete();
            }

            src.delete();
        }
    }

    /**
     * Copy the file src into the file dst. If the dst file already exists, it will be deleted before to start copying
     * if force is set to <code>true</code>, otherwise nothing will be done.
     *
     * @param src
     *            the original file
     * @param dst
     *            the file to create
     * @param force
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
    public static void copyFile(File src, File dst, boolean force) throws IOException {

        // Check for invalid arguments
        if (src == null || !src.canRead()) {
            String msg = "FileHelper.ReadError";
            if (src != null) {
                msg += " " + src.getAbsolutePath();
            }
            throw new IOException(msg);
        }
        if (dst == null) {
            throw new IOException("FileHelper.NullDestinationError");
        }

        // clean the existing file if any and force
        if (force && dst.exists() && dst.isFile()) {
            dst.delete();
        }

        // now copy the file
        if (!dst.exists()) {
            copyFile(src, dst);
        }
    }

    private static void copyFile(File src, File dst) throws IOException {
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dst);

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
     * @param file
     *            the file or directory to remove
     */
    public static void remove(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            String[] children = file.list();
            if (null != children) {
                for (String child : children) {
                    if (!child.equals(".") && !child.equals("..")) { //$NON-NLS-2$
                        File childFile = new File(file, child);
                        remove(childFile);
                    }
                }
            }
            file.delete();
        }
    }
}
