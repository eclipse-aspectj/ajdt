/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *  Andrew Eisenberg - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.model;

import org.eclipse.ajdt.core.builder.AJBuilder;
import org.eclipse.ajdt.core.model.AJModelChecker;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.TestLogger;

/**
 * @author Andrew Eisenberg
 *
 */
public class ModelCheckerTests extends AJDTCoreTestCase {
	
    boolean orig;
    
    protected void setUp() throws Exception {
        super.setUp();
        AJBuilder.removeStateListener();
    }
    protected void tearDown() throws Exception {
        AJBuilder.addStateListener();
        super.tearDown();
    }
    
    public void testShouldDoCheck() {
        assertFalse("Without a state listener, should not be checking the model", AJModelChecker.shouldCheckModel());
        
        new TestLogger();
        assertTrue("Should be checking the model because there should be a state listener registered", AJModelChecker.shouldCheckModel());
        
    }
}
