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
public class Bug358024ProblemFinderTests extends AbstractProblemFindingTests {
    public void testGenericDeclareParents1() throws Exception {
        assertNoProblems(
                new String[] { "p" }, 
                new String[] { "C.aj" }, 
                new String[] {
                        "package p;\n" +
                        "interface InterfaceB<T> {}\n" + 
                        "class B<T> implements InterfaceB<T> {}\n" + 
                        "class A {}\n" + 
                        "aspect AspectForA {\n" + 
                        "    declare parents: A extends B<String>;\n" + 
                        "}" + 
                        "class Main {\n" + 
                        "    void phantomError(){\n" + 
                        "        InterfaceB<String> b = new A();  // should not have an error here\n" + 
                        "        System.out.println(b);\n" + 
                        "    }\n" + 
                        "}"});
    }
    public void testGenericDeclareParents2() throws Exception {
        assertNoProblems(
                new String[] { "ajdterror", "ajdterror", "ajdterror", "ajdterror", "ajdterror" }, 
                new String[] { "A.java", "AspectForA.aj", "B.java", "InterfaceB.java", "Main.java" }, 
                new String[] {
                        "package ajdterror;\n" + 
                        "public class A {}",
                        
                        "package ajdterror;\n" + 
                        "\n" + 
                        "public aspect AspectForA {\n" + 
                        "    declare parents: A extends B<String>; \n" + 
                        "}",
                        
                        "package ajdterror;\n" + 
                        "public class B<T> implements InterfaceB<T> {}",
                        
                        "package ajdterror;\n" + 
                        "public interface InterfaceB<T> {}",
                        
                        "package ajdterror;\n" + 
                        "public class Main {\n" + 
                        "    void phantomError(){\n" + 
                        "        InterfaceB<String> b = new A(); //Here, the phantom error occurs.\n" + 
                        "    }\n" + 
                        "}"
                });
    }
}