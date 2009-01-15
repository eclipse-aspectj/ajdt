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

package org.eclipse.contribution.weaving.jdt.tests.sourceprovider;

import junit.framework.Assert;

import org.eclipse.contribution.weaving.jdt.tests.MockCompilationUnit;
import org.eclipse.contribution.weaving.jdt.tests.MockSourceTransformer;
import org.eclipse.contribution.weaving.jdt.tests.WeavingTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Test;

/**
 * @author Andrew Eisenberg
 * @created Jan 5, 2009
 *
 */
public class SourceTransformerTests extends WeavingTestCase {

    @Test
    public void transformCompilationUnit() throws Exception {
        IProject proj = createPredefinedProject("MockCUProject");
        IFile file = proj.getFile("src/nothing/nothing.mock");
        MockCompilationUnit cu = (MockCompilationUnit) JavaCore.create(file);
        cu.becomeWorkingCopy(monitor);
        Assert.assertEquals("Wrong number of children", 1, cu.getChildren().length);
        Assert.assertEquals("Wrong name for mock class", 
                MockSourceTransformer.MOCK_CLASS_NAME, cu.getChildren()[0].getElementName());
    } 
}
