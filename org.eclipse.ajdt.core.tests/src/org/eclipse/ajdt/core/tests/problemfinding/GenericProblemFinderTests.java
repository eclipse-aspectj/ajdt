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
public class GenericProblemFinderTests extends AbstractProblemFindingTests {
    public void testNoProblemsAbstractAspectDeclareParents() throws Exception {
        assertNoProblems(
                new String[] { "p" }, 
                new String[] { "AbstractAspect.aj" }, 
                new String[] {
                        "package p;\n" +
                        "public abstract aspect AbstractAspect {\n" +
                        "declare parents : Class extends X;\n" +
                        "declare parents : Class extends Y;\n" +
                        "}\n" +
                        "aspect Aspect extends AbstractAspect {\n" + 
                        "    void something(X x) {\n" +
                        "       something(new Class());\n" +
                        "    }\n" +
                        "    void something2(Y y) {\n" +
                        "        something2(new Class());\n" +
                        "    }\n" +
                        "}\n" +
                        "interface X { }\n" +
                        "interface Y { }\n" +
                        "class Class { }" });
    }
    public void testNoProblemsGenericAbstractAspectDeclareParents() throws Exception {
        assertNoProblems(
                new String[] { "p" }, 
                new String[] { "AbstractAspect.aj" }, 
                new String[] {
                        "package p;\n" +
                        "public abstract aspect AbstractAspect<S, T> {\n" +
                        "declare parents : Class extends S;\n" +
                        "declare parents : Class extends T;\n" +
                        "}\n" +
                        "aspect Aspect extends AbstractAspect<X, Y> {\n" + 
                        "    void something(X x) {\n" +
                        "       something(new Class());\n" +
                        "    }\n" +
                        "    void something2(Y y) {\n" +
                        "        something2(new Class());\n" +
                        "    }\n" +
                        "}\n" +
                        "interface X { }\n" +
                        "interface Y { }\n" +
                        "class Class { }" });
    }
    
    public void testNoProblemsGenericAbstractAspectDeclareParents2() throws Exception {
        assertNoProblems(
                new String[] { "p", "p", "p", "p", "p" }, 
                new String[] { "AbstractAspect.aj", "Aspect.aj", "X.java", "Y.java", "Class.java" }, 
                new String[] {
                        "package p;\n" +
                        "public abstract aspect AbstractAspect<S, T> {\n" +
                        "declare parents : Class extends S;\n" +
                        "declare parents : Class extends T;\n" +
                        "}",

                        "package p;\n" +
                        "aspect Aspect extends AbstractAspect<X, Y> {\n\n\n" + 
                        "    void something(X x) {\n" +
                        "       something(new Class());\n" +
                        "    }\n" +
                        "    void something2(Y y) {\n" +
                        "        something2(new Class());\n" +
                        "    }\n" +
                        "}",
                        
                        "package p;\n" +
                        "interface X { }\n",
                        
                        "package p;\n" +
                        "interface Y { }\n",
                        
                        "package p;\n" +
                        "class Class { }" });
    }
    
    // test type parameters on method
    // aspect and class in same file
    public void testBug343001a() throws Exception {
        assertNoProblems(
                new String[] { "p" }, 
                new String[] { "BadClass.aj" }, 
                new String[] {
                        "package p;\n" +
                		"import java.util.List;\n" + 
                		"import java.util.ArrayList;\n" + 
                		"public class BadClass {\n" + 
                		"    void g() {\n" + 
                		"        List<Comparable<?>> v1 = persist();\n" + 
                		"        ArrayList<Comparable<?>> v2 = this.persist();\n" + 
                		"        String v3 = persist2();\n" + 
                		"        BadClass v4 = this.persist2();\n" + 
                		"        System.out.println(\"\"+v1+v2+v3+v4);\n" + 
                		"    } \n" + 
                		"}\n" + 
                		"aspect Foo {\n" + 
                		"    public <T extends List<Comparable<?>>>  T BadClass.persist() {\n" + 
                		"        return null;\n" + 
                		"    }\n" + 
                		"    public <T> T BadClass.persist2() {\n" + 
                		"        return null;\n" + 
                		"    }\n" + 
                		"}" });
    }
    
    // test type parameters on method
    // aspect and class in different file
    public void testBug343001b() throws Exception {
        assertNoProblems(
                new String[] { "p", "q" }, 
                new String[] { "BadClass.java", "Foo.aj" }, 
                new String[] {
                        "package p;\n" +
                        "import java.util.List;\n" + 
                        "import java.util.ArrayList;\n" + 
                        "public class BadClass {\n" + 
                        "    void g() {\n" + 
                        "        List<Comparable<?>> v1 = persist();\n" + 
                        "        ArrayList<Comparable<?>> v2 = this.persist();\n" + 
                        "        String v3 = persist2();\n" + 
                        "        BadClass v4 = this.persist2();\n" + 
                        "        System.out.println(\"\"+v1+v2+v3+v4);\n" + 
                        "    } \n" + 
                        "}",
                        
                        "package q;\n" +
                        "import java.util.List;\n" + 
                        "import p.BadClass;" +
                        "aspect Foo {\n" + 
                        "    public <T extends List<Comparable<?>>>  T BadClass.persist() {\n" + 
                        "        return null;\n" + 
                        "    }\n" + 
                        "    public <T> T BadClass.persist2() {\n" + 
                        "        return null;\n" + 
                        "    }\n" + 
                        "}" });
    }
    
    // test type parameters on method
    // aspect and class in different file and references in different file
    public void testBug343001c() throws Exception {
        assertNoProblems(
                new String[] { "p", "q", "r" }, 
                new String[] { "BadClass.java", "Foo.aj", "Other.java" }, 
                new String[] {
                        "package p;\n" +
                        "public class BadClass {\n" + 
                        "}",
                        
                        "package q;\n" +
                        "import java.util.List;\n" + 
                        "import p.BadClass;\n" +
                        "aspect Foo {\n" + 
                        "    public <T extends List<Comparable<?>>> T BadClass.persist() {\n" + 
                        "        return null;\n" + 
                        "    }\n" + 
                        "    public <T> T BadClass.persist2() {\n" + 
                        "        return null;\n" + 
                        "    }\n" + 
                        "}",
                        
                        "package r;\n" +
                        "import p.BadClass;\n" +
                        "import java.util.List;\n" + 
                        "import java.util.ArrayList;\n" + 
                        "public class Other {\n" +
                        "    void g() { \n" + 
                        "        List<Comparable<?>> v1 = new BadClass().persist(); \n" + 
                        "        ArrayList<Comparable<?>> v2 = new BadClass().persist(); \n" + 
                        "        String v3 = new BadClass().persist2(); \n" + 
                        "        BadClass v4 = new BadClass().persist2(); \n" + 
                        "        System.out.println(\"\"+v1+v2+v3+v4); \n" + 
                        "    }\n" + 
                        "}"});
    }
    
    // test type parameters on method
    // aspect and class in different file and references in different file
    // now look at type parameters as arguments, not just return types
    public void testBug343001d() throws Exception {
        assertNoProblems(
                new String[] { "p", "q", "r" }, 
                new String[] { "BadClass.java", "Foo.aj", "Other.java" }, 
                new String[] {
                        "package p;\n" +
                        "import java.util.List;\n" + 
                        "import java.util.ArrayList;\n" + 
                        "public class BadClass {\n" + 
                        "    void g() { \n" + 
                        "        List<Comparable<?>> v1 = new BadClass().persist(new ArrayList<Comparable<?>>()); \n" + 
                        "        ArrayList<Comparable<?>> v2 = new BadClass().persist(new ArrayList<Comparable<?>>()); \n" + 
                        "        String v3 = new BadClass().persist2(\"\"); \n" + 
                        "        BadClass v4 = new BadClass().persist2(new BadClass()); \n" + 
                        "        System.out.println(\"\"+v1+v2+v3+v4); \n" + 
                        "    }\n" + 
                        "}",
                        
                        "package q;\n" +
                        "import java.util.List;\n" + 
                        "import p.BadClass;\n" +
                        "aspect Foo {\n" + 
                        "    public <T extends List<Comparable<?>>> T BadClass.persist(T a) {\n" + 
                        "        return null;\n" + 
                        "    }\n" + 
                        "    public <T> T BadClass.persist2(T a) {\n" + 
                        "        return null;\n" + 
                        "    }\n" + 
                        "}",
                        
                        "package r;\n" +
                        "import p.BadClass;\n" +
                        "import java.util.List;\n" + 
                        "import java.util.ArrayList;\n" + 
                        "public class Other {\n" +
                        "    void g() { \n" + 
                        "        List<Comparable<?>> v1 = new BadClass().persist(new ArrayList<Comparable<?>>()); \n" + 
                        "        ArrayList<Comparable<?>> v2 = new BadClass().persist(new ArrayList<Comparable<?>>()); \n" + 
                        "        String v3 = new BadClass().persist2(\"\"); \n" + 
                        "        BadClass v4 = new BadClass().persist2(new BadClass()); \n" + 
                        "        System.out.println(\"\"+v1+v2+v3+v4); \n" + 
                        "    }\n" + 
                        "}"});
    }
}