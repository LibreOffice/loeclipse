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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for the Manifest export page controller, these tests are
 * testing the UI behaviour.
 *
 * @author Cédric Bosdonnat
 *
 */
public class ManifestExportPageControllerTest {

    private static final String PATH_VALUE = "value";
    ManifestExportPageController mTested;

    /**
     * Create the controller to test.
     */
    @Before
    public void setup( ) {
        mTested = new ManifestExportPageController();
    }

    /**
     * Test a selection of the generate manifest check box.
     */
    @Test
    public void testSetGenerateManifest() {
        mTested.setGenerateManifest( true );
        assertFalse( "Load path shouldn't be enabled", mTested.isLoadManifestPathEnabled() );
        assertTrue( "Save manifest box should be enabled", mTested.isSaveManifestEnabled() );
    }

    /**
     * Test a selection of the reuse manifest check box.
     */
    @Test
    public void testSetReuseManifest() {
        mTested.setGenerateManifest( false );
        assertTrue( "Load path should be enabled", mTested.isLoadManifestPathEnabled() );
        assertFalse( "Save manifest box shouldn't be enabled", mTested.isSaveManifestEnabled() );
    }

    /**
     * Test when the save manifest check box is selected.
     */
    @Test
    public void testSetSaveManifest() {
        mTested.setGenerateManifest( true );
        mTested.setSaveManifest( true );
        assertTrue( "save manifest path should be enabled", mTested.isSaveManifestPathEnabled() );
    }

    /**
     * Test when the save manifest check box is unselected.
     */
    @Test
    public void testSetNoSaveManifest() {
        mTested.setGenerateManifest( true );
        mTested.setSaveManifest( false );
        assertFalse( "save manifest path shouldn't be enabled", mTested.isSaveManifestPathEnabled() );
    }

    /**
     * Test setting the manifest save path when the field is enabled.
     */
    @Test
    public void testSetSaveManifestPath() {
        mTested.setGenerateManifest( true );
        mTested.setSaveManifest( true );

        mTested.setSaveManifestPath( PATH_VALUE );
        assertEquals( "the save path should have been set", PATH_VALUE, mTested.getSaveManifestPath() );
    }

    /**
     * Test setting the manifest save path when the field is disabled.
     */
    @Test
    public void testSetManifestPathDisabled() {
        mTested.setGenerateManifest( false );

        mTested.setSaveManifestPath( PATH_VALUE );
        assertNotSame( "the save path shouldn't have been set", PATH_VALUE, mTested.getSaveManifestPath() );
    }

    /**
     * Test setting the manifest load path when the field is enabled.
     */
    @Test
    public void testSetLoadManifestPathEnabled() {
        mTested.setGenerateManifest( false );

        mTested.setLoadManifestPath( PATH_VALUE );
        assertEquals( "the load path should have been set", PATH_VALUE, mTested.getLoadManifestPath() );
    }

    /**
     * Test setting the manifest load path when the field is disabled.
     */
    @Test
    public void testSetLoadManifestPathDisabled() {
        mTested.setGenerateManifest( true );

        mTested.setLoadManifestPath( PATH_VALUE );
        assertNotSame( "the load path shouldn't have been set", PATH_VALUE, mTested.getLoadManifestPath() );
    }
}
