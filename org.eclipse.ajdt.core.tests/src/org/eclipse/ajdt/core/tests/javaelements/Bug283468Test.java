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

package org.eclipse.ajdt.core.tests.javaelements;

import org.eclipse.ajdt.core.codeconversion.ITDAwareNameEnvironment;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaProject;

/**
 * @author Andrew Eisenberg
 * @created Jul 17, 2009
 * 
 * Tests that package-info.java is handled correctly
 *
 */
public class Bug283468Test extends AJDTCoreTestCase {

    public void testPackageInfo() throws Exception {
        IProject proj = createPredefinedProject("Bug283468");
        IJavaProject jProj = JavaCore.create(proj);
        ITDAwareNameEnvironment environment = new ITDAwareNameEnvironment((JavaProject) jProj, DefaultWorkingCopyOwner.PRIMARY, null);
        try {
            NameEnvironmentAnswer answer = environment.findType("package-info".toCharArray(), new char[][] { "f".toCharArray()} );
            assertNull("Name environment should not have found package-info.", answer);
        } catch (Exception e) {
            fail("Name environment threw an error when trying to look for package-info.");
        }
        
    }
}
