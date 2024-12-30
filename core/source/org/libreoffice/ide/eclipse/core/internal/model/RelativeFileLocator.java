/*************************************************************************
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright: 2012 by Ludovic Smadja
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.internal.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RelativeFileLocator {

    private File mBaseDir = null;
    private String mRelativePath = null;

    public RelativeFileLocator(File pBaseDir, String pRelativePath) {
        super();
        this.mBaseDir = pBaseDir;
        this.mRelativePath = pRelativePath;
    }

    public List<File> getFiles() {
        List<File> fileList = null;
        if (mBaseDir != null && mBaseDir.isDirectory()) {
            fileList = new ArrayList<File>();
            List<File> scannedDirList = new ArrayList<File>();
            scannedDirList.add(mBaseDir);
            locateRelativeFile(scannedDirList, fileList, mRelativePath);
        }
        return fileList;
    }

    private void locateRelativeFile(List<File> pScannedDirList, List<File> pFileList, String pRelativePath) {
        if (pScannedDirList == null || pScannedDirList.isEmpty()) {
            return;
        }
        List<File> newScannedDirList = new ArrayList<File>();
        for (File scanFile : pScannedDirList) {
            if (scanFile.exists() && scanFile.isDirectory()) {
                File tmpFile = new File(scanFile, pRelativePath);
                if (tmpFile.exists()) {
                    pFileList.add(scanFile);
                }
                File[] children = scanFile.listFiles();
                if (children != null && children.length > 0) {
                    newScannedDirList.addAll(Arrays.asList(children));
                }
            }
        }
        if (!newScannedDirList.isEmpty()) {
            locateRelativeFile(newScannedDirList, pFileList, pRelativePath);
        }
    }

}
