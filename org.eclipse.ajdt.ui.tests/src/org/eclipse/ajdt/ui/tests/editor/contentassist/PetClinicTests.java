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
package org.eclipse.ajdt.ui.tests.editor.contentassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.model.AJModelChecker;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.parserbridge.AJCompilationUnitProblemFinder;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.ajdt.ui.tests.reconciling.MockProblemRequestor;
import org.eclipse.ajdt.ui.tests.testutils.TestLogger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnitProblemFinder;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;

/**
 * Tests the Pet Clinic project
 * @author Andrew Eisenberg
 *
 */
public class PetClinicTests extends UITestCase {

    IProject petClinicProject;
    List/*ICompilationUnit*/ allCUnits = new ArrayList();
    ICompilationUnit ownerUnit;
    String ownerUnitContents; 
   
    protected void setUp() throws Exception {
        super.setUp();
        petClinicProject = createPredefinedProject("petclinic2");
        
        IFolder src = petClinicProject.getFolder("src");
        
        IResourceVisitor visitor = new IResourceVisitor() {
            public boolean visit(IResource resource) throws CoreException {
                if (resource.getType() == IResource.FILE && 
                        (resource.getName().endsWith("java") ||
                                resource.getName().endsWith("aj"))) {
                    ICompilationUnit unit = createUnit((IFile) resource);
                    allCUnits.add(unit);
                    if (unit.getElementName().equals("Owner.java")) {
                        ownerUnit = unit;
                        ownerUnitContents = new String(((CompilationUnit) ownerUnit).getContents());
                    }
                }
                return true;
            }
        };
        src.accept(visitor);
        
        waitForJobsToComplete();
        setAutobuilding(false);
        
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        setAutobuilding(true);
    }

    
    private ICompilationUnit createUnit(IFile file) {
        return (ICompilationUnit) AspectJCore.create(file);
    }
    
    public void testModelCheck() throws Exception {
        TestLogger logger = new TestLogger();
        AspectJPlugin.getDefault().setAJLogger(logger);
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(petClinicProject);
        AJModelChecker.doModelCheckIfRequired(model);
        boolean success = logger.containsMessage("Crosscutting model sanity checked with no problems");
        if (!success) {
            fail("Model check for petclinic failed\n" + logger.getLogMessages());
        }
        
    }

    public void testContentAssist() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        // check that itds are inserted
        int offset = ownerUnitContents.indexOf("this.getAddres") + "this.getAddres".length();  
        ownerUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        CompletionProposal completionProposal = (CompletionProposal) requestor.accepted.get(0);
        assertEquals("Signature of proposal should have been the 'Owner.getAddress()' method\n" + completionProposal, 
                "getAddress", new String(completionProposal.getName())); 
        assertEquals("Completion start is wrong", offset - "getAddres".length(), completionProposal.getReplaceStart());
    }

    /**
     * Ensure that no files have no errors or warnings
     */
    public void testReconciling() throws Exception {
        StringBuffer sb = new StringBuffer();
        for (Iterator cunitIter = allCUnits.iterator(); cunitIter.hasNext();) {
            sb.append(problemFind((ICompilationUnit) cunitIter.next()));
        }
        if (sb.length() > 0) {
            fail(sb.toString());
        }
    }
    
    private String problemFind(ICompilationUnit unit) throws Exception {
        HashMap problems = doFind(unit);
        if (MockProblemRequestor.countProblems(problems) > 0) {
            return "Should not have any problems in " + unit + " but found:\n" + MockProblemRequestor.printProblems(problems) + "\n"; //$NON-NLS-1$
        } else {
            return "";
        }
    }
    private HashMap doFind(ICompilationUnit unit)
            throws JavaModelException {
        HashMap problems = new HashMap();
        if (unit instanceof AJCompilationUnit) {
            AJCompilationUnitProblemFinder.processAJ((AJCompilationUnit) unit, 
                    AJWorkingCopyOwner.INSTANCE, problems, true, 
                    ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        } else {
            // Requires JDT Weaving
            CompilationUnitProblemFinder.process((CompilationUnit) unit, null,
                    DefaultWorkingCopyOwner.PRIMARY, problems, true, 
                    ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        }
        return problems;
    }
}