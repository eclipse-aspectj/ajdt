/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Andrew Eisenberg - Initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.problemfinding;



/**
 * Tests AJCompilationUnitProblemFinder
 * @author andrew
 *
 */
public class Bug361170ProblemFinderTests extends AbstractProblemFindingTests {
    public void testITDReturnsThis1() throws Exception {
        assertNoProblems(
                new String[] { "p" }, 
                new String[] { "MyExtendingAspect.aj" }, 
                new String[] {
                        "package p;\n" +
                        "class B {}\n" +
                        "class A extends B { }\n" +
                        "public aspect MyExtendingAspect {\n" + 
                        "    public B A.getRef() {\n" + 
                        "        return this;\n" + 
                        "    }\n" + 
                        "}"});
    }
    public void testITDReturnsThis2() throws Exception {
        assertNoProblems(
                new String[] { "p", "p", "p" }, 
                new String[] { "B.java", "A.java", "MyExtendingAspect.aj" }, 
                new String[] {
                        "package p;\n" +
                        "class B {}",
                        
                        "package p;\n" +
                        "class A extends B { }",
                        
                        "package p;\n" +
                        "public aspect MyExtendingAspect {\n" + 
                        "    public B A.getRef() {\n" + 
                        "        return this;\n" + 
                        "    }\n" + 
                        "}"});
    }

}