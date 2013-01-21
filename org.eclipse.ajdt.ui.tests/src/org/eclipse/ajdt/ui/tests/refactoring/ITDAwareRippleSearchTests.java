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
package org.eclipse.ajdt.ui.tests.refactoring;

import java.util.Arrays;

import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.tests.search.AbstractITDSearchTest;
import org.eclipse.ajdt.internal.ui.refactoring.RippleMethodFinder2;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;



/**
 * Tests that the {@link RippleMethodFinder2} works on ITDs where appropriate
 * @author Andrew Eisenberg
 * @created Mar 18, 2010
 */
public class ITDAwareRippleSearchTests extends AbstractITDSearchTest {

    public void testRipple1() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\nvoid Java.foo() { } }");
        createCU("Java.java", "class Java { }");
        buildProject();
        IntertypeElement itd = findFirstITD(unit);
        IMethod[] methods = RippleMethodFinder2.getRelatedMethods(itd, new NullProgressMonitor(), DefaultWorkingCopyOwner.PRIMARY);
        assertEquals("Should have found 1 ripple method", 1, methods.length);
        assertContains(methods, itd);
    }
    public void testRipple2() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\nvoid Java.foo() { } }");
        createCU("Java.java", "class Java {\n void foo(int x) { } }");
        buildProject();
        IntertypeElement itd = findFirstITD(unit);
        IMethod[] methods = RippleMethodFinder2.getRelatedMethods(itd, new NullProgressMonitor(), DefaultWorkingCopyOwner.PRIMARY);
        assertEquals("Should have found 1 ripple method", 1, methods.length);
        assertContains(methods, itd);
    }
    
    public void testRipple3() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\nvoid Java.foo() { } }");
        createCU("Java.java", "class Java {\n void foo(int x) { } }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java {\n void foo() { }\n void foo(int x) { } }");
        buildProject();
        IntertypeElement itd = findFirstITD(unit);
        IMethod method = (IMethod) findFirstChild(other);
        IMethod[] methods = RippleMethodFinder2.getRelatedMethods(itd, new NullProgressMonitor(), DefaultWorkingCopyOwner.PRIMARY);
        assertEquals("Should have found 2 ripple method", 2, methods.length);
        assertContains(methods, itd);
        assertContains(methods, method);
    }
    
    public void testRipple4() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\nvoid Java.foo() { } }");
        createCU("Java.java", "class Java extends Other {\n void foo(int x) { } }");
        ICompilationUnit other = createCU("Other.java", "class Other {\n void foo() { }\n void foo(int x) { } }");
        buildProject();
        IntertypeElement itd = findFirstITD(unit);
        IMethod method = (IMethod) findFirstChild(other);
        IMethod[] methods = RippleMethodFinder2.getRelatedMethods(itd, new NullProgressMonitor(), DefaultWorkingCopyOwner.PRIMARY);
        assertEquals("Should have found 2 ripple method", 2, methods.length);
        assertContains(methods, itd);
        assertContains(methods, method);
    }
    
    public void testRipple5() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\nvoid Java.foo() { } }");
        createCU("Java.java", "class Java {\n void foo(int x) { } }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java {\n void foo() { }\n void foo(int x) { } }");
        buildProject();
        IntertypeElement itd = findFirstITD(unit);
        IMethod method = (IMethod) findFirstChild(other);
        IMethod[] methods = RippleMethodFinder2.getRelatedMethods(method, new NullProgressMonitor(), DefaultWorkingCopyOwner.PRIMARY);
        assertEquals("Should have found 2 ripple method", 2, methods.length);
        assertContains(methods, itd);
        assertContains(methods, method);
    }
    
    public void testRipple6() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\nvoid Java.foo() { } }");
        createCU("Java.java", "class Java extends Other {\n void foo(int x) { } }");
        ICompilationUnit other = createCU("Other.java", "class Other {\n void foo() { }\n void foo(int x) { } }");
        buildProject();
        IntertypeElement itd = findFirstITD(unit);
        IMethod method = (IMethod) findFirstChild(other);
        IMethod[] methods = RippleMethodFinder2.getRelatedMethods(method, new NullProgressMonitor(), DefaultWorkingCopyOwner.PRIMARY);
        assertEquals("Should have found 2 ripple method", 2, methods.length);
        assertContains(methods, itd);
        assertContains(methods, method);
    }
    
    
    public void testRippleInterface1() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\npublic void Java.foo() { } }");
        createCU("Java.java", "interface Java extends Other {\n void foo(int x); }");
        ICompilationUnit other = createCU("Other.java", "interface Other {\n void foo();\n void foo(int x); }");
        buildProject();
        IntertypeElement itd = findFirstITD(unit);
        IMethod method = (IMethod) findFirstChild(other);
        IMethod[] methods = RippleMethodFinder2.getRelatedMethods(method, new NullProgressMonitor(), DefaultWorkingCopyOwner.PRIMARY);
        assertEquals("Should have found 2 ripple method", 2, methods.length);
        assertContains(methods, itd);
        assertContains(methods, method);
    }
    
    public void testRippleInterface2() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\npublic void Java.foo() { } }");
        createCU("Java.java", "interface Java {\n void foo(int x); }");
        ICompilationUnit other = createCU("Other.java", "class Other implements Java {\n public void foo() { }\n public void foo(int x) { } }");
        buildProject();
        IntertypeElement itd = findFirstITD(unit);
        IMethod method = (IMethod) findFirstChild(other);
        IMethod[] methods = RippleMethodFinder2.getRelatedMethods(method, new NullProgressMonitor(), DefaultWorkingCopyOwner.PRIMARY);
        assertEquals("Should have found 2 ripple method", 2, methods.length);
        assertContains(methods, itd);
        assertContains(methods, method);
    }
    
    public void testRippleInterface3() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\npublic void Java.foo() { } }");
        createCU("Java.java", "interface Java {\n void foo(int x); }");
        ICompilationUnit other = createCU("Other.java", "interface Other extends Java {\n void foo();\n void foo(int x); }");
        buildProject();
        IntertypeElement itd = findFirstITD(unit);
        IMethod method = (IMethod) findFirstChild(other);
        IMethod[] methods = RippleMethodFinder2.getRelatedMethods(method, new NullProgressMonitor(), DefaultWorkingCopyOwner.PRIMARY);
        assertEquals("Should have found 2 ripple method", 2, methods.length);
        assertContains(methods, itd);
        assertContains(methods, method);
    }
    
    public void testRippleInterface4() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\npublic void Java.foo() { } }");
        createCU("Java.java", "class Java implements Other {\n public void foo(int x) { } }");
        ICompilationUnit other = createCU("Other.java", "interface Other {\n void foo();\n void foo(int x); }");
        buildProject();
        IntertypeElement itd = findFirstITD(unit);
        IMethod method = (IMethod) findFirstChild(other);
        IMethod[] methods = RippleMethodFinder2.getRelatedMethods(method, new NullProgressMonitor(), DefaultWorkingCopyOwner.PRIMARY);
        assertEquals("Should have found 2 ripple method", 2, methods.length);
        assertContains(methods, itd);
        assertContains(methods, method);
    }
    
    public void testRippleInterface5() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\npublic void Java.foo() { } }");
        createCU("Java.java", "interface Java {\n void foo(int x); }");
        ICompilationUnit other = createCU("Other.java", "class Other implements Java {\n public void foo() { }\n public void foo(int x) { } }");
        buildProject();
        IntertypeElement itd = findFirstITD(unit);
        IMethod method = (IMethod) findFirstChild(other);
        IMethod[] methods = RippleMethodFinder2.getRelatedMethods(method, new NullProgressMonitor(), DefaultWorkingCopyOwner.PRIMARY);
        assertEquals("Should have found 2 ripple method", 2, methods.length);
        assertContains(methods, itd);
        assertContains(methods, method);
    }
    
    public void testRippleInterface6() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\npublic void Java.foo() { } }");
        createCU("Java.java", "class Java implements Other, Other2 {\n public void foo(int x) { } }");
        ICompilationUnit other = createCU("Other.java", "interface Other {\n void foo();\n void foo(int x); }");
        ICompilationUnit other2 = createCU("Other2.java", "interface Other2 {\n void foo();\n void foo(int x); }");
        buildProject();
        IntertypeElement itd = findFirstITD(unit);
        IMethod method = (IMethod) findFirstChild(other);
        IMethod method2 = (IMethod) findFirstChild(other2);
        IMethod[] methods = RippleMethodFinder2.getRelatedMethods(method, new NullProgressMonitor(), DefaultWorkingCopyOwner.PRIMARY);
        assertEquals("Should have found 3 ripple method", 3, methods.length);
        assertContains(methods, itd);
        assertContains(methods, method);
        assertContains(methods, method2);
    }
    

    
    void assertContains(IMethod[] methods, IMethod toContain) {
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].equals(toContain)) {
                return;
            }
        }
        fail(Arrays.toString(methods) + " \nshould contain\n" + toContain);
    }
}
