/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     George Harley - initial version
 * 	   Helen Hawkins - converting for use with AJDT 1.1.11 codebase  
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.preferences;

import junit.framework.TestCase;

/**
 * @author gharley
 *
 */
public class AspectJPreferencesTest extends TestCase {
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetGetCompilerOptions() {
        AspectJPreferences.setCompilerOptions("foo");
        assertEquals("foo", AspectJPreferences.getCompilerOptions());
    }
    
}
