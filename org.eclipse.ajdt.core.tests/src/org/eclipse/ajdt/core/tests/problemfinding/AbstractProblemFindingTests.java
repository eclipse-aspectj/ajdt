/*
 * Copyright 2003-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.eclipse.ajdt.core.tests.problemfinding;

import java.util.HashMap;

import org.eclipse.ajdt.core.parserbridge.AJCompilationUnitProblemFinder;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.core.CompilationUnit;

/**
 * 
 * @author Andrew Eisenberg
 * @created Nov 24, 2010
 */
public abstract class AbstractProblemFindingTests extends AJDTCoreTestCase {

    protected IJavaProject proj;

    public AbstractProblemFindingTests(String name) {
        super(name);
    }

    public AbstractProblemFindingTests() {
        super();
    }

    protected void setUp() throws Exception {
        proj = JavaCore.create(createPredefinedProject("DefaultEmptyProject")); //$NON-NLS-1$
        joinBackgroudActivities();
        setAutobuilding(false);
    }

    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            setAutobuilding(true);
        }
    }

    protected void assertNoProblems(String[] packages, String[] cuNames, String[] cuContents)
            throws CoreException {
                ICompilationUnit[] units = createUnits(packages, cuNames, cuContents, proj);
                buildProject(proj);
                for (ICompilationUnit unit : units) {
                    assertNoProblems((CompilationUnit) unit);
                }
            }

    /**
     * @throws JavaModelException
     */
    private void assertNoProblems(CompilationUnit unit) throws JavaModelException {
        HashMap<String,CategorizedProblem[]> problems = new HashMap<String,CategorizedProblem[]>();
        AJCompilationUnitProblemFinder.processAJ(unit, 
                AJWorkingCopyOwner.INSTANCE, problems, true, 
                ICompilationUnit.ENABLE_BINDINGS_RECOVERY | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY | ICompilationUnit.FORCE_PROBLEM_DETECTION, null);
        
        MockProblemRequestor.filterAllWarningProblems(problems);
        assertEquals("Should not have any problems in " + unit + " but found:\n" + MockProblemRequestor.printProblems(problems), 0, problems.size()); //$NON-NLS-1$
    }

}