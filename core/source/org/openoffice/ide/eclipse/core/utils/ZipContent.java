/*************************************************************************
 *
 * $RCSfile: ZipContent.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/12/06 07:49:21 $
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
package org.openoffice.ide.eclipse.core.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
			PluginLogger.warning("Problem when writing file to zip: " + mEntryName); //$NON-NLS-1$
		} finally {
			// Close the file entry stream
			try { if (origin != null) origin.close(); } catch (IOException e) {}
		}
	}
}
