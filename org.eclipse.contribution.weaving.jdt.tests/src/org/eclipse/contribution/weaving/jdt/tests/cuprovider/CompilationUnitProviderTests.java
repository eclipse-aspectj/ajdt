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

package org.eclipse.contribution.weaving.jdt.tests.cuprovider;

import junit.framework.Assert;

import org.eclipse.contribution.weaving.jdt.tests.MockCompilationUnit;
import org.eclipse.contribution.weaving.jdt.tests.WeavingTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.CompilationUnit;

/**
 * @author Andrew Eisenberg
 * @created Jan 5, 2009
 *
 */
public class CompilationUnitProviderTests extends WeavingTestCase {

    public void testCreateCompilationUnit() throws Exception {
        IProject proj = createPredefinedProject("MockCUProject");
        IFile file = proj.getFile("src/nothing/nothing.mock");
        CompilationUnit cu = (CompilationUnit) JavaCore.create(file);
        Assert.assertTrue("Mock compilation unit should exist but doesn't", cu.exists());
        Assert.assertTrue("Mock compilation unit should be of type MockCompilationUnit, but instead is type " + 
                cu.getClass().getCanonicalName(), cu instanceof MockCompilationUnit);
        
    }
}
