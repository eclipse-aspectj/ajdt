/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.builder.AJBuilder;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.model.AJModelChecker;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.TestLogger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * @author hawkinsh
 *
 */
public class ModelCheckerTests extends AJDTCoreTestCase {
	
    boolean orig;
    
    protected void setUp() throws Exception {
        super.setUp();
        AJBuilder.removeStateListener();
    }
    
    public void testShouldDoCheck() {
        assertFalse("Without a state listener, should not be checking the model", AJModelChecker.shouldCheckModel());
        
        new TestLogger();
        assertTrue("Should be checking the model because there should be a state listener registered", AJModelChecker.shouldCheckModel());
        
    }
}
