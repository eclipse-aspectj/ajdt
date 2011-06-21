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

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.SearchMatch;


/**
 * Test that type references inside of ITDs names are found within searches
 * @author Andrew Eisenberg
 * @created Aug 15, 2010
 */
public class ITDAwareTypeSearchTests extends AbstractITDSearchTest {
    public void testITDMethodType() throws Exception {
        String contents = "aspect Aspect {\n void Java.foo() { } }";
        createCU("Aspect.aj", contents);
        ICompilationUnit unit = createCU("Java.java", "class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("Java", contents, matches);
    }
    public void testITDFieldType() throws Exception {
        String contents = "aspect Aspect {\n int Java.foo; }";
        createCU("Aspect.aj", contents);
        ICompilationUnit unit = createCU("Java.java", "class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("Java", contents, matches);
    }
    public void testITDConstructorType() throws Exception {
        String contents = "aspect Aspect {\n Java.new() {\n this(); } }";
        createCU("Aspect.aj", contents);
        ICompilationUnit unit = createCU("Java.java", "class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("Java", contents, matches);
    }
    public void testITDMethodType2() throws Exception {
        String contents = "package p;\nimport q.Java;\naspect Aspect {\n void Java.foo() { } }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("Java", contents, matches);
    }
    public void testITDMethodType3() throws Exception {
        String contents = "package p;\nimport q.Java;\naspect Aspect {\n void Foo.foo() { new Java(); } \n class Foo { } }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("Java", contents, matches);
    }
    public void testITDFieldType2() throws Exception {
        String contents = "package p;\nimport q.Java;\naspect Aspect {\n int Java.foo; }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("Java", contents, matches);
    }
    public void testITDFieldType3() throws Exception {
        String contents = "package p;\nimport q.Java;\naspect Aspect {\n Object Foo.foo = new Java(); \n class Foo { } }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("Java", contents, matches);
    }
    public void testITDFieldType4() throws Exception {
        String contents = "package p;\nimport q.Java;\naspect Aspect {\n Object Foo.foo2 = Java.class; \n class Foo { } }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("Java", contents, matches);
    }
    public void testITDConstructorType2() throws Exception {
        String contents = "package p;\nimport q.Java;\naspect Aspect {\n Java.new() {\n this(); } }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("Java", contents, matches);
    }
    
    public void testQualifiedITDMethodType() throws Exception {
        String contents = "package p;\n aspect Aspect {\n void p.Java.foo() { } }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("p", "Java.java", "package p;\npublic class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("p.Java", contents, matches);
    }
    public void testQualifiedITDFieldType() throws Exception {
        String contents = "package p;\naspect Aspect {\n int p.Java.foo; }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("p", "Java.java", "package p;\npublic class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("p.Java", contents, matches);
    }
    public void testQualifiedITDConstructorType() throws Exception {
        String contents = "package p;\naspect Aspect {\n p.Java.new() {\n this(); } }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("p", "Java.java", "package p;\npublic class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("p.Java", contents, matches);
    }
    public void testQualifiedITDMethodType2() throws Exception {
        String contents = "package p;\naspect Aspect {\n void q.Java.foo() { } }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("q.Java", contents, matches);
    }
    public void testQualifiedITDFieldType2() throws Exception {
        String contents = "package p;\naspect Aspect {\n int q.Java.foo; }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("q.Java", contents, matches);
    }
    public void testQualifiedITDConstructorType2() throws Exception {
        String contents = "package p;\naspect Aspect {\n q.Java.new() {\n this(); } }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        IType javaType = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(javaType, this.getName());
        assertMatch("q.Java", contents, matches);
    }
}
