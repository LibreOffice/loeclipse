/*************************************************************************
 *
 * $RCSfile: ZipContent.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/11 18:39:35 $
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
package org.openoffice.ide.eclipse.java.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.Platform;
import org.openoffice.ide.eclipse.core.PluginLogger;

/**
 * This class is a small structure containing the data to zip for one file
 * 
 * @author cedricbosdo
 *
 */
public class ZipContent {

	protected File mFile;
	
	protected String mEntryName;
	
	public ZipContent(String entryName, File file) {
		mFile = file;
		mEntryName = entryName;
	}
	
	public void writeContentToZip(ZipOutputStream out) {
		
		BufferedInputStream origin = null;
		try {
			FileInputStream fi = new FileInputStream(mFile);
			 origin = new BufferedInputStream(fi, 2048);

			ZipEntry entry = new ZipEntry(mEntryName);
			out.putNextEntry(entry);

			int count;
			byte data[] = new byte[2048];

			while((count = origin.read(data, 0, 2048)) != -1) {
				out.write(data, 0, count);
			}
			
			out.closeEntry();
			
		} catch (IOException e) {
			PluginLogger.warning(Messages.getString("ZipContent.AddToJarError") + mEntryName); //$NON-NLS-1$
		} finally {
			// Close the file entry stream
			try { if (origin != null) origin.close(); } catch (IOException e) {}
		}
	}
	
	public static ZipContent[] getFiles(File file) {
		return getInternalFiles(file, file);
	}
	
	private static ZipContent[] getInternalFiles(File file, File rootDir) {
		
		ZipContent[] contents = new ZipContent[0];
		
		ArrayList result = new ArrayList();
		File[] files = file.listFiles();
		if (files != null) {
			for (int i=0; i<files.length; i++) {
				File filei = files[i];
				if (filei.isFile() && !filei.getName().endsWith("urd")) { //$NON-NLS-1$
					
					String filePath = filei.getAbsolutePath();
					String relativePath = filePath.substring(
							rootDir.getAbsolutePath().length() + 1);
					if (Platform.getOS().equals(Platform.OS_WIN32)) {
						relativePath = relativePath.replace("\\", "/");
					}
					ZipContent content = new ZipContent(relativePath, filei);

					result.add(content);
				} else {
					if (!filei.getName().equals("urd")) { //$NON-NLS-1$
						ZipContent[] tmpContents = getInternalFiles(filei, rootDir);
						result.addAll(Arrays.asList(tmpContents));
					}
				}
			}

			contents = new ZipContent[result.size()];
			for (int i=0, length=result.size(); i<length; i++) {
				contents[i] = (ZipContent)result.get(i);
			}
		} else {
			PluginLogger.warning(Messages.getString("ZipContent.NotDirectoryError") + file); //$NON-NLS-1$
		}
		return contents;
	}
}
