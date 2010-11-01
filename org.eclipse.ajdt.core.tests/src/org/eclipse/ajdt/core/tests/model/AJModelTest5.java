/*******************************************************************************
 * Copyright (c) 2008 SpringSource Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Andrew Eisenberg = Initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * 
 * @author andrew
 * Tests  Bug 265553
 * Ensure that binary handles can be traversed
 * Also check that we can get the correct start and end location for binary handles
 */
public class AJModelTest5 extends AbstractModelTest {

    public void testBug265553AJHandleIdentifiers() throws Exception {
        createPredefinedProject("Bug265553AspectPath"); //$NON-NLS-1$
        IProject base = createPredefinedProject("Bug265553Base"); //$NON-NLS-1$
        checkHandles(JavaCore.create(base));
    }
    public void testBug265553AJHandleIdentifiers2() throws Exception {
        IProject onAspectPath = createPredefinedProject("Bug265553AspectPath"); //$NON-NLS-1$
        checkHandles(JavaCore.create(onAspectPath));
    }
}
