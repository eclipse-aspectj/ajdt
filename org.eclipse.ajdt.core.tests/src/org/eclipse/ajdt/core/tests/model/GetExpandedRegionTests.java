/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.core.tests.model;

import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.internal.core.contentassist.ContentAssistProvider;

import junit.framework.TestCase;

/**
 * @author Andrew Eisenberg
 * @created Jun 7, 2009
 * Tests ContentAssistProvider.getExpandedRegion
 */
public class GetExpandedRegionTests extends AJDTCoreTestCase {

    class MockContentAssistProvider extends ContentAssistProvider {
        // make accessible
        protected String getExpandedRegion(int offset, int length,
                char[] contents) {
            return super.getExpandedRegion(offset, length, contents);
        }
    }
    
    protected void setUp() throws Exception {
        // don't instantiate the workspace
    }
    
    protected void tearDown() throws Exception {
        // don't wipe the workspace clean
    }
    
    public void testExpandedRegion() throws Exception {
        validateExpandedRegion("gg ggg gggg", 0, 1, "gg");
    }
    
    public void testExpandedRegion2() throws Exception {
        validateExpandedRegion("gg ggg gggg", 8, 3, "gggg");
    }
    
    public void testExpandedRegion3() throws Exception {
        validateExpandedRegion("gg ggg gggg", 2, 3, "ggg");
    }
    
    public void testExpandedRegion4() throws Exception {
        validateExpandedRegion("java.lang.Object", 4, 10, "Object");
    }

    public void testExpandedRegion5() throws Exception {
        validateExpandedRegion("java lang Object", 4, 10, "Object");
    }

    public void testExpandedRegion6() throws Exception {
        validateExpandedRegion("java.lang.Object", 4, 0, "java");
    }

    public void testExpandedRegion7() throws Exception {
        validateExpandedRegion("java.lang.Object", 5, 0, "lang");
    }

    private void validateExpandedRegion(String fullContents, int offset, int length, String expected) {
        String actual = new MockContentAssistProvider().getExpandedRegion(offset, length, fullContents.toCharArray());
        assertEquals("Error getting expanded region", expected, actual);
    }
}
