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
package org.openoffice.ide.eclipse.core.internal.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RelativeFileLocator {

    private File mBaseDir = null;
    private String mRelativePath = null;
        
    public RelativeFileLocator(File baseDir, String pRelativePath) {
        super();
        this.mBaseDir = baseDir;
        this.mRelativePath = pRelativePath;
    }

    public List<File> getFiles(){
        if(mBaseDir == null || !mBaseDir.isDirectory()){
            return null;
        }
        List<File> fileList = new ArrayList<File>();
        List<File> scannedDirList = new ArrayList<File>();
        scannedDirList.add(mBaseDir);
        locateRelativeFile(scannedDirList, fileList, mRelativePath);
        return fileList;
    }

    private void locateRelativeFile(List<File> scannedDirList, List<File> fileList, String relativePath) {
        if( scannedDirList == null || scannedDirList.isEmpty()){
            return;
        }
        List<File> newScannedDirList = new ArrayList<File>();
        for(File scanFile : scannedDirList){
            if( scanFile.exists() && scanFile.isDirectory()){
                File tmpFile = new File(scanFile, relativePath);
                if(tmpFile.exists()){
                    fileList.add(scanFile);
                }                
                File[] children = scanFile.listFiles();
                if(children != null && children.length > 0){
                    newScannedDirList.addAll(Arrays.asList(children));
                }
            }            
        }    
        if(!newScannedDirList.isEmpty()){
            locateRelativeFile(newScannedDirList,fileList,relativePath);
        }
    }

}
