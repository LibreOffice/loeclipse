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
 * Copyright: 2010 by Cédric Bosdonnat
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.java.export;

/**
 * Controller object for the fields of the {@link JavaExportPart} view.
*/
public class JavaExportPageControl {

    public static final String DEFAULT_ANT_FILENAME = "build.xml"; //$NON-NLS-1$

    private boolean mSaveAntScript;
    private String mSavePath;

    /**
     * Default constructor.
     */
    public JavaExportPageControl() {
        setSaveAntScript(false);
        setSavePath(DEFAULT_ANT_FILENAME);
    }

    /**
     * @param pSave the state of the save ant script box.
     */
    public void setSaveAntScript(boolean pSave) {
        mSaveAntScript = pSave;
    }

    /**
     * @param pPath the path to set in the save field
     */
    public void setSavePath(String pPath) {
        mSavePath = pPath;
    }

    /**
     * @return the state of the save ant script box
     */
    public boolean getSaveAntScript() {
        return mSaveAntScript;
    }

    /**
     * @return the value of the save path text field
     */
    public String getSavePath() {
        return mSavePath;
    }

    /**
     * @return whether the save path field is enabled
     */
    public boolean isSavePathEnabled() {
        return mSaveAntScript;
    }
}
