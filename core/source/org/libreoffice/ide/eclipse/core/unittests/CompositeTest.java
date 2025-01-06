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
package org.libreoffice.ide.eclipse.core.unittests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;

import org.libreoffice.ide.eclipse.core.internal.model.UnoComposite;
import org.libreoffice.ide.eclipse.core.model.IUnoComposite;

import junit.framework.TestCase;

/**
 * JUnit tests for the UNO-IDL composite structure.
 *
 */
public class CompositeTest extends TestCase {

    /**
     * Constructor.
     *
     * @param name
     *            the test case name
     */
    public CompositeTest(String name) {
        super(name);
    }

    /**
     * Test method for {@link UnoComposite#getChildren()} and {@link UnoComposite#addChild(IUnoComposite)}.
     */
    public void testGetChildren() {

        // Create composites
        IUnoComposite parent = new UnoComposite();
        IUnoComposite child1 = new UnoComposite();
        IUnoComposite child2 = new UnoComposite();

        // Add children
        parent.addChild(child1);
        parent.addChild(child2);

        // get the children
        IUnoComposite[] children = parent.getChildren();
        assertEquals("2 children to find", 2, children.length); //$NON-NLS-1$
    }

    /**
     * Test method for {@link UnoComposite#removeAll()}.
     */
    public void testRemoveAll() {

        // Create composites
        IUnoComposite parent = new UnoComposite();
        IUnoComposite child1 = new UnoComposite();
        IUnoComposite child2 = new UnoComposite();

        // Add children
        parent.addChild(child1);
        parent.addChild(child2);

        // get the children
        IUnoComposite[] children = parent.getChildren();
        assertEquals("2 children to find", 2, children.length); //$NON-NLS-1$

        // Removes all composites
        parent.removeAll();
        children = parent.getChildren();
        assertEquals("There should be no more child", 0, children.length); //$NON-NLS-1$
    }

    /**
     * Test method for {@link UnoComposite#setType(int)}.
     */
    public void testSetType() {

        // Create a composite
        IUnoComposite node = new UnoComposite();
        assertEquals("The type should be COMPOSITE_TYPE_NOTSET", //$NON-NLS-1$
            IUnoComposite.COMPOSITE_TYPE_NOTSET, node.getType());

        // Sets the type FILE
        node.setType(IUnoComposite.COMPOSITE_TYPE_FILE);
        assertEquals("The type should be COMPOSITE_TYPE_FILE", //$NON-NLS-1$
            IUnoComposite.COMPOSITE_TYPE_FILE, node.getType());

        // Sets the type FOLDER, no changes of the type
        node.setType(IUnoComposite.COMPOSITE_TYPE_FOLDER);
        assertEquals("The type should be COMPOSITE_TYPE_FILE", //$NON-NLS-1$
            IUnoComposite.COMPOSITE_TYPE_FILE, node.getType());
    }

    /**
     * Test method for {@link UnoComposite#configure(String)} and for {@link UnoComposite#toString()}.
     */
    public void testConfigureText() {

        // test with text only
        IUnoComposite parent = new UnoComposite();
        parent.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
        parent.configure(new Hashtable<String, Object>(), "test1 text"); //$NON-NLS-1$
        assertEquals("Test 1 failed", "test1 text", parent.toString()); //$NON-NLS-1$ //$NON-NLS-2$

        // test with one user variable
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("text", "test2 text"); //$NON-NLS-1$ //$NON-NLS-2$
        parent.configure(properties, "${text}"); //$NON-NLS-1$
        assertEquals("Test 2 failed", "test2 text", parent.toString()); //$NON-NLS-1$ //$NON-NLS-2$

        // test with one user variable and text
        properties = new Hashtable<String, Object>();
        properties.put("text", "3"); //$NON-NLS-1$ //$NON-NLS-2$
        parent.configure(properties, "test${text} text"); //$NON-NLS-1$
        assertEquals("Test 3 failed", "test3 text", parent.toString()); //$NON-NLS-1$ //$NON-NLS-2$

        // text with one user variable, text and the children variable
        IUnoComposite child1 = new UnoComposite();
        child1.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
        Hashtable<String, Object> properties1 = new Hashtable<String, Object>();
        properties1.put("id", "1"); //$NON-NLS-1$ //$NON-NLS-2$
        child1.configure(properties1, "child${id} ${children}"); //$NON-NLS-1$

        IUnoComposite child2 = new UnoComposite();
        child2.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
        child2.configure(new Hashtable<String, Object>(), "child2 "); //$NON-NLS-1$

        IUnoComposite child3 = new UnoComposite();
        child3.setType(IUnoComposite.COMPOSITE_TYPE_FILE);
        child3.configure("test.txt"); //$NON-NLS-1$

        IUnoComposite grandchild1 = new UnoComposite();
        grandchild1.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
        Hashtable<String, Object> properties2 = new Hashtable<String, Object>();
        properties2.put("text", "grandchild1\n"); //$NON-NLS-1$ //$NON-NLS-2$
        grandchild1.configure(properties2, "${text}"); //$NON-NLS-1$
        child1.addChild(grandchild1);

        parent.configure(new Hashtable<String, Object>(), "text before \n${children} after"); //$NON-NLS-1$
        String expected = "text before \nchild1 grandchild1\nchild2  after"; //$NON-NLS-1$
        parent.addChild(child1);
        parent.addChild(child2);
        parent.addChild(child3);
        assertEquals("Test 4 failed", expected, parent.toString()); //$NON-NLS-1$
    }

    /**
     * Test for {@link UnoComposite#setIndented(boolean)} and {@link UnoComposite#toString()}.
     */
    public void testIndentation() {

        // text with one user variable, text and the children variable
        IUnoComposite child1 = new UnoComposite();
        child1.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
        child1.setIndented(true);
        Hashtable<String, Object> properties1 = new Hashtable<String, Object>();
        properties1.put("id", "1"); //$NON-NLS-1$ //$NON-NLS-2$
        child1.configure(properties1, "child${id} ${children}"); //$NON-NLS-1$

        IUnoComposite child2 = new UnoComposite();
        child2.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
        child2.configure(new Hashtable<String, Object>(), "child2\n"); //$NON-NLS-1$

        IUnoComposite child3 = new UnoComposite();
        child3.setType(IUnoComposite.COMPOSITE_TYPE_FILE);
        child3.configure("test.txt"); //$NON-NLS-1$

        IUnoComposite grandchild1 = new UnoComposite();
        grandchild1.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
        grandchild1.setIndented(true);
        Hashtable<String, Object> properties2 = new Hashtable<String, Object>();
        properties2.put("text", "grandchild1\n"); //$NON-NLS-1$ //$NON-NLS-2$
        grandchild1.configure(properties2, "${text}"); //$NON-NLS-1$
        child1.addChild(grandchild1);

        IUnoComposite parent = new UnoComposite();
        parent.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
        parent.configure(new Hashtable<String, Object>(), "text before \n${children} after"); //$NON-NLS-1$
        String expected = "text before \n\tchild1 \tgrandchild1\nchild2\n after"; //$NON-NLS-1$
        parent.addChild(child1);
        parent.addChild(child2);
        parent.addChild(child3);
        assertEquals("Indentation test failed", expected, parent.toString()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link UnoComposite#configure(String)} and for {@link UnoComposite#create(boolean)}.
     */
    public void testConfigureFile() {

        // create a new folder toto
        try {
            IUnoComposite folder = new UnoComposite();
            folder.setType(IUnoComposite.COMPOSITE_TYPE_FOLDER);
            folder.configure("toto"); //$NON-NLS-1$
            folder.create(true);

            File folderTest = new File("toto"); //$NON-NLS-1$
            assertTrue("toto doesn't exist", folderTest.exists()); //$NON-NLS-1$
            assertTrue("toto is not a folder", folderTest.isDirectory()); //$NON-NLS-1$
            assertTrue("toto is writable and readable", //$NON-NLS-1$
                folderTest.canWrite() && folderTest.canRead());

            IUnoComposite file = new UnoComposite();
            file.setType(IUnoComposite.COMPOSITE_TYPE_FILE);
            file.configure("toto/toto.txt"); //$NON-NLS-1$

            IUnoComposite parent = new UnoComposite();
            parent.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            properties.put("text", "3"); //$NON-NLS-1$ //$NON-NLS-2$
            parent.configure(properties, "test${text} text"); //$NON-NLS-1$

            file.addChild(parent);
            file.create(true);

            File fileTest = new File("toto/toto.txt"); //$NON-NLS-1$
            assertTrue("toto file doesn't exists", fileTest.exists()); //$NON-NLS-1$
            assertTrue("toto file isn't a file", fileTest.isFile()); //$NON-NLS-1$
            assertTrue("toto file can't be read", fileTest.canRead()); //$NON-NLS-1$

            BufferedReader reader = new BufferedReader(new FileReader(fileTest));
            assertEquals("Wrong content of the file", "test3 text", reader.readLine()); //$NON-NLS-1$ //$NON-NLS-2$
            reader.close();

            // Cleaning after the tests
            fileTest.delete();
            folderTest.delete();

        } catch (Exception e) {
            e.printStackTrace();
            assertFalse("Exception thrown", true); //$NON-NLS-1$
        }
    }
}
