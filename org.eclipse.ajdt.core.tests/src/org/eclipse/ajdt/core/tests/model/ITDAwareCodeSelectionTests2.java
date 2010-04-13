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

import java.util.HashMap;

import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.contribution.jdt.itdawareness.INameEnvironmentProvider;
import org.eclipse.contribution.jdt.itdawareness.ITDAwarenessAspect;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * @author Andrew Eisenberg
 * @created Jun 6, 2009
 * Tests code selection for ITDs
 * Further testing of ITD hyperlinks.
 * Ensure that hyperlinking works when target aspect is in separate project
 */
public class ITDAwareCodeSelectionTests2 extends AJDTCoreTestCase {
    
    // need to set a NameEnviromentProvider, since this is typically
    // set by AJDT.UI
    // BAD!!!
    INameEnvironmentProvider origProvider;
    INameEnvironmentProvider mockProvider = new INameEnvironmentProvider() {
    
        public ISourceType transformSourceTypeInfo(ISourceType info) {
            // don't need
            return null;
        }
    
        public boolean shouldFindProblems(CompilationUnit unitElement) {
            return true;
        }
    
        public CompilationUnitDeclaration problemFind(CompilationUnit unitElement,
                SourceElementParser parer, WorkingCopyOwner workingCopyOwner,
                HashMap problems, boolean creatingAST, int reconcileFlags,
                IProgressMonitor monitor) throws JavaModelException {
            // don't need
            return null;
        }
    
        public SearchableEnvironment getNameEnvironment(JavaProject project,
                ICompilationUnit[] workingCopies) {
            // don't need
            return null;
        }
    
        public SearchableEnvironment getNameEnvironment(JavaProject project,
                WorkingCopyOwner owner) {
            // don't need
            return null;
        }
    };

    IProject base;
    IProject depending;
    IFile baseFile;

    ICompilationUnit baseUnit;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        origProvider = ITDAwarenessAspect.aspectOf().nameEnvironmentAdapter.getProvider();
        ITDAwarenessAspect.aspectOf().nameEnvironmentAdapter.setProvider(mockProvider);
        super.setUp();
        base = createPredefinedProject("Bug273334base"); //$NON-NLS-1$
        depending = createPredefinedProject("Bug273334depending"); //$NON-NLS-1$
        baseFile = base.getFile("src/q/UsesITDs1.java");
        baseUnit = JavaCore.createCompilationUnitFrom(baseFile);
        waitForAutoBuild();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        ITDAwarenessAspect.aspectOf().nameEnvironmentAdapter.setProvider(origProvider);
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
   private void validateCodeSelect(ICompilationUnit unit,
            IRegion region, String expected) throws Exception {
        IJavaElement[] result = unit.codeSelect(region.getOffset(), region.getLength());
        assertEquals("Should have found exactly one hyperlink", 1, result.length);
        IJavaElement elt = result[0];
        assertTrue("Java element " + elt.getHandleIdentifier() + " should exist", elt.exists());
        assertEquals(expected, elt.getElementName());
    }

    private IRegion findRegion(ICompilationUnit unit, String string, int occurrence) {
        String contents = new String(((CompilationUnit) unit).getContents());
        int start = 0;
        while (occurrence-- > 0) {
            start = contents.indexOf(string, start);
        }
        return new Region(start, string.length());
    }


}
