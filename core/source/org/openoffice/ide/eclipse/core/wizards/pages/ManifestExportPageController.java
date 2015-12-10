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
package org.openoffice.ide.eclipse.core.wizards.pages;

/**
 * Testable controller class for the Manifest export page, this class
 * implements all the logic ruling the dialog controls.
 *
 * @author Cédric Bosdonnat
 *
 */
public class ManifestExportPageController {

    /**
     * Stores the state of the Generate / Use manifest option.
     */
    private boolean mGenerateManifest;

    /**
     * Stores the state of the Save manifest check box.
     */
    private boolean mSaveManifest;

    /**
     * Stores the workspace-relative path to store the manifest to.
     */
    private String mSaveManifestPath;

    /**
     * Stores the workspace-relative path to read the manifest from.
     */
    private String mLoadManifestPath;

    /**
     * Default constructor.
     */
    public ManifestExportPageController( ) {
        setGenerateManifest( true );
        setSaveManifest( false );
        mSaveManifestPath = new String( );
        mLoadManifestPath = new String( );
    }

    /**
     * Sets whether to use an existing manifest file or generate a new one (note that
     * the generated manifest file may or may not be saved).
     *
     * @param pGenerate <code>true</code> to generate a new manifest, <code>false</code>
     *      to use it.
     */
    public void setGenerateManifest( boolean pGenerate ) {
        mGenerateManifest = pGenerate;
    }

    /**
     * Setting this value has no effect unless the manifest is generated, but setting it to
     * <code>true</code> will save the manifest to the path returned by {@link #getSaveManifestPath()}.
     *
     * @param pSave <code>true</code> to save the manifest in the workspace,
     *              <code>false</code> otherwise.
     */
    public void setSaveManifest( boolean pSave ) {
        if ( isSaveManifestEnabled() ) {
            mSaveManifest = pSave;
        }
    }

    /**
     * Setting this value has no effect unless {@link #isSaveManifestPathEnabled()}
     * returns <code>true</code>.
     *
     * @param pPath the workspace-relative path to the manifest file to create.
     */
    public void setSaveManifestPath( String pPath ) {
        if ( isSaveManifestPathEnabled() ) {
            mSaveManifestPath = pPath;
        }
    }

    /**
     * Setting this value has no effect unless {@link #isLoadManifestPathEnabled()}
     * returns <code>true</code>.
     *
     * @param pPath the workspace-relative path to the manifest file to load.
     */
    public void setLoadManifestPath( String pPath ) {
        if ( isLoadManifestPathEnabled() ) {
            mLoadManifestPath = pPath;
        }
    }

    /**
     * @return <code>true</code> is the manifest has to be saved, <code>false</code> if it needs
     *      to be loaded from a file in the workspace.
     */
    public boolean getGenerateManifest( ) {
        return mGenerateManifest;
    }

    /**
     * Note that this value has no effect unless {@link #getGenerateManifest()} returns
     * <code>true</code>.
     *
     * @return <code>true</code> is the manifest has to be saved, <code>false</code> otherwise.
     */
    public boolean getSaveManifest( ) {
        return mSaveManifest;
    }

    /**
     * Note that this value has no effect unless {@link #getSaveManifest()} returns <code>true</code>.
     *
     * @return the workspace-relative path to store the manifest to.
     */
    public String getSaveManifestPath( ) {
        return mSaveManifestPath;
    }

    /**
     * Note that this value has no effect unless {@link #getGenerateManifest()}
     * returns <code>false</code>.
     *
     * @return the workspace-relative path of the manifest file to load.
     */
    public String getLoadManifestPath( ) {
        return mLoadManifestPath;
    }

    /**
     * @return the state of the save manifest check box.
     */
    public boolean isSaveManifestEnabled( ) {
        return mGenerateManifest;
    }

    /**
     * @return the state of the save path row.
     */
    public boolean isSaveManifestPathEnabled( ) {
        return mGenerateManifest && mSaveManifest;
    }

    /**
     * @return the state of the load path row.
     */
    public boolean isLoadManifestPathEnabled( ) {
        return !mGenerateManifest;
    }
}
