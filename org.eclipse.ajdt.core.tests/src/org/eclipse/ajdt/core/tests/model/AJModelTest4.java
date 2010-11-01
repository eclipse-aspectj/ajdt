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
 * @created Sep 18, 2008
 * Stress test the handle identifiers with a particularly nasty project
 *
 */
public class AJModelTest4 extends AbstractModelTest {

    public void testAJHandleIdentifiers() throws Exception {
        IProject project = createPredefinedProject("Handle Testing"); //$NON-NLS-1$
        checkHandles(JavaCore.create(project));
    }
}
