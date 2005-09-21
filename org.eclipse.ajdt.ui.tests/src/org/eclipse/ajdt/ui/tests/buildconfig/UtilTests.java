/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.buildconfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.internal.buildconfig.Util;
import org.eclipse.ajdt.ui.tests.AspectJTestPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

//LstSupport
/**
 * @author gharley
 *  
 */
public class UtilTests extends UITestCase {

    private String pwd;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        pwd = AspectJTestPlugin.getPluginDir() + "testdata" + File.separator //$NON-NLS-1$
                + "buildconfigurator"; //$NON-NLS-1$
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    //------------------------------------------------------
    // getRelativePathString tests
    //------------------------------------------------------
    public void testGetRelativePathStringWithCommonRoot() {
        IPath from = new Path("c:/my/dir"); //$NON-NLS-1$
        IPath file = new Path("c:/my/src/foo/bar/MyClass.java"); //$NON-NLS-1$
        String relativePath = Util.getRelativePathString(file, from);
        assertEquals("../src/foo/bar/MyClass.java", relativePath); //$NON-NLS-1$
    }

    public void testGetRelativePathStringNoCommonRoot() {
        IPath from = new Path("c:/my/dir"); //$NON-NLS-1$
        IPath file = new Path("c:/cat/in/the/hat/MyClass.java"); //$NON-NLS-1$
        String relativePath = Util.getRelativePathString(file, from);
        assertEquals("../../cat/in/the/hat/MyClass.java", relativePath); //$NON-NLS-1$
    }

    public void testGetRelativePathStringWithSameHome() {
        IPath from = new Path("c:/my/dir"); //$NON-NLS-1$
        IPath file = new Path("c:/my/dir/MyClass.java"); //$NON-NLS-1$
        String relativePath = Util.getRelativePathString(file, from);
        assertEquals("MyClass.java", relativePath); //$NON-NLS-1$
    }

    public void testGetRelativePathStringWithIdenticalPaths() {
        IPath from = new Path("c:/warren/oates"); //$NON-NLS-1$
        IPath file = new Path("c:/warren/oates"); //$NON-NLS-1$
        String relativePath = Util.getRelativePathString(file, from);
        assertEquals(".", relativePath); //$NON-NLS-1$
    }

    public void testGetRelativePathStringWithNullFile() {
        IPath from = null;
        IPath file = new Path("c:/my/src/foo/bar/MyClass.java"); //$NON-NLS-1$
        String relativePath = Util.getRelativePathString(file, from);
        assertNull(relativePath);
    }

    public void testGetRelativePathStringWithNullFromDir() {
        IPath from = new Path("c:/my/dir"); //$NON-NLS-1$
        IPath file = null;
        String relativePath = Util.getRelativePathString(file, from);
        assertNull(relativePath);
    }

    public void testGetRelativePathStringWithStringCommonRoot() {
        String from = "c:/my/dir"; //$NON-NLS-1$
        String file = "c:/my/src/foo/bar/MyClass.java"; //$NON-NLS-1$
        String relativePath = Util.getRelativePathString(file, from);
        assertEquals("../src/foo/bar/MyClass.java", relativePath); //$NON-NLS-1$
    }

    public void testGetRelativePathStringNoStringCommonRoot() {
        String from = "c:/my/dir"; //$NON-NLS-1$
        String file = "c:/cat/in/the/hat/MyClass.java"; //$NON-NLS-1$
        String relativePath = Util.getRelativePathString(file, from);
        assertEquals("../../cat/in/the/hat/MyClass.java", relativePath); //$NON-NLS-1$
    }

    public void testGetRelativePathStringWithStringNullFile() {
        String from = null;
        String file = "c:/my/src/foo/bar/MyClass.java"; //$NON-NLS-1$
        String relativePath = Util.getRelativePathString(file, from);
        assertNull(relativePath);
    }

    public void testGetRelativePathStringWithStringNullFromDir() {
        String from = "c:/my/dir"; //$NON-NLS-1$
        String file = null;
        String relativePath = Util.getRelativePathString(file, from);
        assertNull(relativePath);
    }

    public void testGetRelativePathStringWithIdenticalStrings() {
        String from = "c:/warren/oates"; //$NON-NLS-1$
        String file = "c:/warren/oates"; //$NON-NLS-1$
        String relativePath = Util.getRelativePathString(file, from);
        assertEquals(".", relativePath); //$NON-NLS-1$
    }

    public void testGetRelativePathStringWithEmptyStrings() {
        String from = ""; //$NON-NLS-1$
        String file = ""; //$NON-NLS-1$
        String relativePath = Util.getRelativePathString(file, from);
        assertEquals(".", relativePath); //$NON-NLS-1$
    }

    //------------------------------------------------------
    // getLstFileContents tests
    //------------------------------------------------------
    public void testGetLstFileContentsNullFile() {
        IPath lstFile = null;
        List files = new ArrayList();
        List options = new ArrayList();
        List links = new ArrayList();
        try {
            // Verify we ought to get back empty lists
            Util.getLstFileContents(lstFile, files, options, links);
            assertEquals(0, files.size());
            assertEquals(0, options.size());
            assertEquals(0, links.size());
        } catch (FileNotFoundException e) {
            fail("Caught unexpected FileNotFoundException!"); //$NON-NLS-1$
        }
    }

    public void testGetLstFileContentsNonExistentFile() {
        IPath lstFile = new Path("c:/made/up/file/name.lst"); //$NON-NLS-1$
        List files = new ArrayList();
        List options = new ArrayList();
        List links = new ArrayList();
        try {
            Util.getLstFileContents(lstFile, files, options, links);
            fail("Preceeding call should have thrown FileNotFoundException"); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            // NO OP
        }
    }

    public void testGetLstFileContentsFilesOnly() {
        IPath lstFile = new Path(new File(pwd, "filesonly.lst") //$NON-NLS-1$
                .getAbsolutePath());
        List files = new ArrayList();
        List options = new ArrayList();
        List links = new ArrayList();
        try {
            // Verify we get expected information back
            Util.getLstFileContents(lstFile, files, options, links);
            assertEquals(18, files.size());
            assertEquals(0, options.size());
            assertEquals(0, links.size());
        } catch (FileNotFoundException e) {
            fail("Caught unexpected FileNotFoundException!"); //$NON-NLS-1$
        }
    }

    public void testGetLstFileContentsFilesAndLinkOnly() {
        IPath lstFile = new Path(new File(pwd, "filesandlinks.lst") //$NON-NLS-1$
                .getAbsolutePath());
        List files = new ArrayList();
        List options = new ArrayList();
        List links = new ArrayList();
        try {
            // Verify we get expected information back
            Util.getLstFileContents(lstFile, files, options, links);
            assertEquals(18, files.size());
            assertEquals(0, options.size());
            assertEquals(1, links.size());
        } catch (FileNotFoundException e) {
            fail("Caught unexpected FileNotFoundException!"); //$NON-NLS-1$
        }
    }

    public void testGetLstFileOptionsOnly() {
        IPath lstFile = new Path(new File(pwd, "optionsonly.lst") //$NON-NLS-1$
                .getAbsolutePath());
        List files = new ArrayList();
        List options = new ArrayList();
        List links = new ArrayList();
        try {
            // Verify we get expected information back
            Util.getLstFileContents(lstFile, files, options, links);
            assertEquals(0, files.size());
            assertEquals(3, options.size());
            assertEquals(0, links.size());
        } catch (FileNotFoundException e) {
            fail("Caught unexpected FileNotFoundException!"); //$NON-NLS-1$
        }
    }

    public void testGetLstFileOptionsAndLinkOnly() {
        IPath lstFile = new Path(new File(pwd, "optionsandlink.lst") //$NON-NLS-1$
                .getAbsolutePath());
        List files = new ArrayList();
        List options = new ArrayList();
        List links = new ArrayList();
        try {
            // Verify we get expected information back
            Util.getLstFileContents(lstFile, files, options, links);
            assertEquals(0, files.size());
            assertEquals(3, options.size());
            assertEquals(1, links.size());
        } catch (FileNotFoundException e) {
            fail("Caught unexpected FileNotFoundException!"); //$NON-NLS-1$
        }
    }

    public void testGetInlinedLstFileWithNoLinksFile() {
        IPath lstFile = new Path(new File(pwd, "filesonly.lst") //$NON-NLS-1$
                .getAbsolutePath());
        try {
            IPath result = Util.getInlinedLstFile(lstFile);
            assertSame("Should get input IPath returned", lstFile, result); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            fail("Caught unexpected FileNotFoundException!"); //$NON-NLS-1$
        }
    }

    public void testGetInlinedLstFileWithLinksFileAndAbsolutePath() {
        IPath lstFile = new Path(new File(pwd, "filesandabsolutelink.lst") //$NON-NLS-1$
                .getAbsolutePath());
        try {
            IPath result = Util.getInlinedLstFile(lstFile);
            assertEquals(Util.getInlinedFileName(lstFile.toString()), result
                    .toString());
			// cleanup
			File f = result.toFile();
			if (f.exists()) {
				f.delete();
			}
        } catch (FileNotFoundException e) {
            fail("Caught unexpected FileNotFoundException!"); //$NON-NLS-1$
        }
    }

    public void testGetInlinedLstFileWithLinksFileAndRelativePath() {
        IPath lstFile = new Path(new File(pwd, "filesandrelativelink.lst") //$NON-NLS-1$
                .getAbsolutePath());
        try {
            IPath result = Util.getInlinedLstFile(lstFile);
            assertEquals(Util.getInlinedFileName(lstFile.toString()), result
                    .toString());
			// cleanup
			File f = result.toFile();
			if (f.exists()) {
				f.delete();
			}
        } catch (FileNotFoundException e) {
            fail("Caught unexpected FileNotFoundException!"); //$NON-NLS-1$
        }
    }

    public void testGetInlinedLstFileWithNestedLinkFiles() {
        IPath lstFile = new Path(new File(pwd, "linkone.lst").getAbsolutePath()); //$NON-NLS-1$
        try {
            IPath result = Util.getInlinedLstFile(lstFile);
            assertEquals(Util.getInlinedFileName(lstFile.toString()), result
                    .toString());
			// cleanup
			File f = result.toFile();
			if (f.exists()) {
				f.delete();
			}
        } catch (FileNotFoundException e) {
            fail("Caught unexpected FileNotFoundException!"); //$NON-NLS-1$
        }
    }

    public void testGetInlinedLstFileWithBadLinkFile() {
        IPath lstFile = new Path(new File(pwd, "badlink.lst").getAbsolutePath()); //$NON-NLS-1$
        try {
            IPath result = Util.getInlinedLstFile(lstFile);
            assertEquals(Util.getInlinedFileName(lstFile.toString()), result
                    .toString());

            // TODO : Check that we get an empty file

			// cleanup
			File f = result.toFile();
			if (f.exists()) {
				f.delete();
			}
        } catch (FileNotFoundException e) {
            fail("Caught unexpected FileNotFoundException!"); //$NON-NLS-1$
        }
    }

    public void testGetInlinedLstFileWithNullInput() {
        try {
            IPath result = Util.getInlinedLstFile(null);
            assertNull(result);
        } catch (FileNotFoundException e) {
            fail("Caught unexpected FileNotFoundException!"); //$NON-NLS-1$
        }
    }

    public void testGetInlinedLstFileWithNonExistentFileInput() {
        IPath lstFile = new Path(new File(pwd, "doesnotexist.lst") //$NON-NLS-1$
                .getAbsolutePath());
        try {
            IPath result = Util.getInlinedLstFile(lstFile);
            fail("Preceeding call should have thrown FileNotFoundException"); //$NON-NLS-1$
        } catch (FileNotFoundException e) {
            // NO OP
        }
    }

    public void testGetInlinedFileName() {
        String result = Util.getInlinedFileName("c:\\made\\up\\name.lst"); //$NON-NLS-1$
        assertEquals("c:\\made\\up\\name.inlined.lst", result); //$NON-NLS-1$
    }
}
// End LstSupport
