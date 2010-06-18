/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.search;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.junit.launcher.JUnit4TestFinder;

/**
 * @author Andrew Eisenberg
 * @created Jun 18, 2010
 */
public class ITDAwareJUnit4TestFinderTests extends AbstractITDSearchTest{

    public void testFindTests1() throws Exception {
        ICompilationUnit unit = createCU("Target.java", "import org.junit.Test; public class Target {\n @Test public void foo() { \n } }");
        Set<IType> result = findTestsInProject();
        assertTypes(result, unit);
    }
    
    // should find Target as a test
    public void testFindTests2() throws Exception {
        ICompilationUnit unit = createCU("Target.java", "public class Target { \n }");
        createCU("Aspect.aj", "import org.junit.Test; public aspect Aspect {\n @Test public void Target.foo() { \n } }");
        this.buildProject();
        
        Set<IType> result = findTestsInProject();
        assertTypes(result, unit);
    }
    
    // should not find Target as a test since it is not part of the searched for region
    public void testFindTests3() throws Exception {
        createCU("Target.java", "public class Target { \n }");
        ICompilationUnit aspect = createCU("Aspect.aj", "import org.junit.Test; public aspect Aspect {\n @Test public void Target.foo() { \n } }");
        this.buildProject();
        
        Set<IType> result = findTests(aspect);
        assertTypes(result);
    }
    
    // should not find Target as a test since aspect is not in the region
    public void testFindTests4() throws Exception {
        ICompilationUnit unit = createCU("Target.java", "public class Target { \n }");
        createCU("Aspect.aj", "import org.junit.Test; public aspect Aspect {\n @Test public void Target.foo() { \n } }");
        this.buildProject();
        
        Set<IType> result = findTests(unit);
        assertTypes(result);
    }
    
    public void testFindTests5() throws Exception {
        ICompilationUnit unit1 = createCU("Target1.java", "public class Target1 { \n }");
        ICompilationUnit unit2 = createCU("Target2.java", "public class Target2 { \n }");
        ICompilationUnit unit3 = createCU("Target3.java", "public class Target3 { \n }");
        createCU("Aspect.aj", "import org.junit.Test; public aspect Aspect {\n @Test public void Target1.foo() { \n } \n @Test public void Target2.foo() { \n } \n @Test public void Target3.foo() { \n } }");
        this.buildProject();
        
        Set<IType> result = findTestsInProject();
        assertTypes(result, unit1, unit2, unit3);
    }
    
    private void assertTypes(Set<IType> result, ICompilationUnit...units) throws JavaModelException {
        Set<IType> allTypes = new HashSet<IType>();
        for (ICompilationUnit unit : units) {
            for (IType type : unit.getAllTypes()) {
                allTypes.add(type);
            }
        }
        assertEquals(allTypes, result);
    }
    
    private Set<IType> findTestsInProject() throws CoreException {
        return findTests(javaProject);
    }
    private Set<IType> findTests(IJavaElement element) throws CoreException {
        JUnit4TestFinder finder = new JUnit4TestFinder();
        Set<IType> result = new HashSet<IType>();
        finder.findTestsInContainer(element, result, new NullProgressMonitor());
        return result;
    }
}
