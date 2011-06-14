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
public class Bug347021ProblemFinderTests extends AbstractProblemFindingTests {
    public void testForLoopOnThis() throws Exception {
        assertNoProblems(
                new String[] { "p", "p" }, 
                new String[] { "C.java", "IterableAspect.aj" }, 
                new String[] {
                        "package p;\n" +
                        "import java.util.Iterator;\n" + 
                		"class C implements Iterable<String> {\n" + 
                		"  public Iterator<String> iterator() {\n" + 
                		"    return null;\n" + 
                		"  }\n" + 
                		"}",

                		"package p;\n" +
                		"abstract aspect IterableAspect {\n" + 
                		"  public void C.map() {\n" + 
                		"    for (String value : this) { value.toString(); }\n" + 
                		"  }\n" + 
                		"}"});
    }
    public void testCastOnThis() throws Exception {
        assertNoProblems(
                new String[] { "p", "p" }, 
                new String[] { "C.java", "IterableAspect.aj" }, 
                new String[] {
                        "package p;\n" +
                        "import java.util.Iterator;\n" + 
                        "class C implements Iterable<String> {\n" + 
                        "  public Iterator<String> iterator() {\n" + 
                        "    return null;\n" + 
                        "  }\n" + 
                        "}",
                        
                        "package p;\n" +
                        "abstract aspect IterableAspect {\n" + 
                        "  public void C.map() {\n" + 
                        "    ((C) this).map();\n" + 
                        "  }\n" + 
                "}"});
    }
    public void testBooleanExpressionOnThis() throws Exception {
        assertNoProblems(
                new String[] { "p", "p" }, 
                new String[] { "C.java", "IterableAspect.aj" }, 
                new String[] {
                        "package p;\n" +
                        "import java.util.Iterator;\n" + 
                        "class C implements Iterable<String> {\n" + 
                        "  public Iterator<String> iterator() {\n" + 
                        "    return null;\n" + 
                        "  }\n" + 
                        "}",
                        
                        "package p;\n" +
                        "abstract aspect IterableAspect {\n" + 
                        "  public void C.map() {\n" +
                        "    if (this instanceof C) { }\n" + 
                        "  }\n" + 
                "}"});
    }
}