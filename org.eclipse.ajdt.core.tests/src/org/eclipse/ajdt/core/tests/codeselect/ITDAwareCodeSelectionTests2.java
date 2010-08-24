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

package org.eclipse.ajdt.core.tests.codeselect;

import org.eclipse.contribution.jdt.itdawareness.INameEnvironmentProvider;
import org.eclipse.contribution.jdt.itdawareness.NameEnvironmentAdapter;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Andrew Eisenberg
 * @created Jun 6, 2009
 * Tests code selection for ITDs
 * Further testing of ITD hyperlinks.
 * Ensure that hyperlinking works when target aspect is in separate project
 */
public class ITDAwareCodeSelectionTests2 extends AbstractITDAwareCodeSelectionTests {
    
    // need to set a NameEnviromentProvider, since this is typically
    // set by AJDT.UI
    INameEnvironmentProvider origProvider;
    INameEnvironmentProvider mockProvider = new MockNameEnvironmentProvider();

    IProject base;
    IProject depending;
    IFile baseFile;

    ICompilationUnit baseUnit;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        origProvider = NameEnvironmentAdapter.getInstance().getProvider();
        NameEnvironmentAdapter.getInstance().setProvider(mockProvider);
        super.setUp();
        depending = createPredefinedProject("Bug273334depending"); //$NON-NLS-1$
        base = createPredefinedProject("Bug273334base"); //$NON-NLS-1$
        baseFile = base.getFile("src/q/UsesITDs1.java");
        baseUnit = JavaCore.createCompilationUnitFrom(baseFile);
        waitForAutoBuild();
    }

    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            NameEnvironmentAdapter.getInstance().setProvider(origProvider);
        }
    }

    
    /**
     * Test that ITD hyperlinks work when the aspect is in
     * other project and ITDs are declared in this project
     */
    public void testITDTargetFileHyperlink() throws Exception {
        validateCodeSelect(baseUnit, findRegion(baseUnit, "aField", 1), "InterfaceForITD.aField");
        validateCodeSelect(baseUnit, findRegion(baseUnit, "nothing", 1), "InterfaceForITD.nothing");
    }
    /**
     * Test that ITD hyperlinks work when the aspect is in
     * other project and ITDs are declared in other project
     */
    public void testITDTargetFileHyperlinkOtherProject() throws Exception {
        validateCodeSelect(baseUnit, findRegion(baseUnit, "aField", 2), "InterfaceForITD.aField");
        validateCodeSelect(baseUnit, findRegion(baseUnit, "nothing", 2), "InterfaceForITD.nothing");
    }

}
