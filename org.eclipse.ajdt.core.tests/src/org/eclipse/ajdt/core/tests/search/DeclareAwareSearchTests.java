/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.search;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.SearchMatch;

/**
 * Test that types and annotations can be appropriately found inside of
 * declare declarations 
 * 
 * @author Andrew Eisenberg
 * @created Aug 18, 2010
 */
public class DeclareAwareSearchTests extends AbstractITDSearchTest {
    public void testDeclareParentsExtends() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare parents : Java extends Other;}\nclass Java { } \nclass Other{ }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType subType = unit.getType("Java");
        IType superType = unit.getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("Java", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("Other", contents, matches);
    }

    public void testDeclareParentsImplements() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare parents : Java implements Other;}\nclass Java { } \ninterface Other{ }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType subType = unit.getType("Java");
        IType superType = unit.getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("Java", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("Other", contents, matches);
    }
    public void testDeclareParentsExtendsInSameAspect() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare parents : Java extends Other;\nclass Java { } \nclass Other{ } }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType subType = unit.getType("Aspect").getType("Java");
        IType superType = unit.getType("Aspect").getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("Java", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("Other", contents, matches);
    }
    
    public void testDeclareParentsImplementsInSameAspect() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare parents : Java implements Other;\nclass Java { } \ninterface Other{ } }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType subType = unit.getType("Aspect").getType("Java");
        IType superType = unit.getType("Aspect").getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("Java", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("Other", contents, matches);
    }
    
    public void testDeclareParentsExtendsInDifferentCU() throws Exception {
        String contents = "package p;\nimport q.Java;\nimport r.Other;\naspect Aspect {\n declare parents : Java extends Other; }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit javaUnit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        ICompilationUnit otherUnit = createCU("r", "Other.java", "package r;\npublic class Other { }");
        IType subType = javaUnit.getType("Java");
        IType superType = otherUnit.getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("Java", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("Other", contents, matches);
    }
    
    public void testDeclareParentsImplementsInDifferentCU() throws Exception {
        String contents = "package p;\nimport q.Java;\nimport r.Other;\naspect Aspect {\n declare parents : Java implements Other; }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit javaUnit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        ICompilationUnit otherUnit = createCU("r", "Other.java", "package r;\npublic interface Other { }");
        IType subType = javaUnit.getType("Java");
        IType superType = otherUnit.getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("Java", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("Other", contents, matches);
    }

    public void testQualifiedDeclareParentsExtendsInDifferentCU() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare parents : q.Java extends r.Other; }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit javaUnit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        ICompilationUnit otherUnit = createCU("r", "Other.java", "package r;\npublic class Other { }");
        IType subType = javaUnit.getType("Java");
        IType superType = otherUnit.getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("q.Java", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("r.Other", contents, matches);
    }
    
    public void testQualifiedDeclareParentsImplementsInDifferentCU() throws Exception {
        String contents = "package p;\nimport q.Java;\naspect Aspect {\n declare parents : q.Java implements r.Other; }";
        createCU("p", "Aspect.aj", contents);
        ICompilationUnit javaUnit = createCU("q", "Java.java", "package q;\npublic class Java { }");
        ICompilationUnit otherUnit = createCU("r", "Other.java", "package r;\npublic interface Other { }");
        IType subType = javaUnit.getType("Java");
        IType superType = otherUnit.getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("q.Java", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("r.Other", contents, matches);
    }
    
    public void testDeclareParentsExtendsMultiple() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare parents : Java || Second extends Other;}\nclass Java { } \nclass Second { } \nclass Other{ }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType subType = unit.getType("Java");
        IType subType2 = unit.getType("Second");
        IType superType = unit.getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("Java", contents, matches);
        matches = findSearchMatches(subType2, this.getName());
        assertMatch("Second", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("Other", contents, matches);
    }
    public void testDeclareParentsExtendsPattern() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare parents : Java+ extends Other;}\nclass Java { } \nclass Other{ }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType subType = unit.getType("Java");
        IType superType = unit.getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("Java", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("Other", contents, matches);
    }
    public void testDeclareParentsExtendsAnnotationPattern() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare parents : (@Ann *) extends Other;}\n @interface Ann { }\n@Ann class Java { } \nclass Other{ }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType subType = unit.getType("Ann");
        IType superType = unit.getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertTwoMatches("Ann", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("Other", contents, matches);
    }
    public void testQualifiedDeclareParentsExtendsMultiple() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare parents : q.Java || q.Second extends q.Other; }";
        createCU("p", "Aspect.aj", contents);
        IType subType = createCU("q", "Java.java", "package q;\npublic class Java { }").getType("Java");
        IType subType2 = createCU("q", "Second.java", "package q;\npublic class Second { }").getType("Second");
        IType superType = createCU("q", "Other.java", "package q;\npublic class Other { }").getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("q.Java", contents, matches);
        matches = findSearchMatches(subType2, this.getName());
        assertMatch("q.Second", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("q.Other", contents, matches);
    }
    public void testQualifiedDeclareParentsExtendsPattern() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare parents : q.Java+ extends q.Other;}";
        createCU("p", "Aspect.aj", contents);
        IType subType = createCU("q", "Java.java", "package q;\npublic class Java { }").getType("Java");
        IType superType = createCU("q", "Other.java", "package q;\npublic class Other { }").getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("q.Java", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("q.Other", contents, matches);
    }
    public void testQualifiedDeclareParentsExtendsAnnotationPattern() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare parents : (@q.Ann *) extends q.Other;}";
        createCU("p", "Aspect.aj", contents);
        createCU("q", "Java.java", "package q;\npublic class Java { }").getType("Java");
        IType subType = createCU("q", "Ann.java", "package q;\n@interface Ann { }").getType("Ann");
        IType superType = createCU("q", "Other.java", "package q;\npublic class Other { }").getType("Other");
        
        List<SearchMatch> matches = findSearchMatches(subType, this.getName());
        assertMatch("q.Ann", contents, matches);
        matches = findSearchMatches(superType, this.getName());
        assertMatch("q.Other", contents, matches);
    }
    
    
    // now look for declare annotation variations
    public void testDeclareAtType() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare @type : Java : @Ann; }\nclass Java { } \n @interface Ann { }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType ann = unit.getType("Ann");
        IType type = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(ann, this.getName());
        assertMatch("Ann", contents, matches);
        matches = findSearchMatches(type, this.getName());
        assertMatch("Java", contents, matches);
    }
    public void testDeclareAtMethod() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare @method : void Java.xxx() : @Ann; }\nclass Java {\n void xxx()  { } } \n @interface Ann { }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType ann = unit.getType("Ann");
        IType type = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(ann, this.getName());
        assertMatch("Ann", contents, matches);
        matches = findSearchMatches(type, this.getName());
        assertMatch("Java", contents, matches);
    }
    public void testDeclareAtConstructor() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare @constructor : Java.new() : @Ann; }\nclass Java {\n Java()  { } } \n @interface Ann { }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType ann = unit.getType("Ann");
        IType type = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(ann, this.getName());
        assertMatch("Ann", contents, matches);
        matches = findSearchMatches(type, this.getName());
        assertMatch("Java", contents, matches);
    }
    public void testDeclareAtField() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare @field : int Java.xxx : @Ann; }\nclass Java {\n int xxx;  { } } \n @interface Ann { }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType ann = unit.getType("Ann");
        IType type = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(ann, this.getName());
        assertMatch("Ann", contents, matches);
        matches = findSearchMatches(type, this.getName());
        assertMatch("Java", contents, matches);
    }
    
    public void testQualifiedDeclareAtType() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare @type : p.Java : @p.Ann; }\nclass Java { } \n @interface Ann { }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType ann = unit.getType("Ann");
        IType type = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(ann, this.getName());
        assertMatch("p.Ann", contents, matches);
        matches = findSearchMatches(type, this.getName());
        assertMatch("p.Java", contents, matches);
    }
    public void testQualifiedDeclareAtMethod() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare @method : void p.Java.xxx() : @p.Ann; }\nclass Java {\n void xxx()  { } } \n @interface Ann { }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType ann = unit.getType("Ann");
        IType type = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(ann, this.getName());
        assertMatch("p.Ann", contents, matches);
        matches = findSearchMatches(type, this.getName());
        assertMatch("p.Java", contents, matches);
    }
    public void testQualifiedDeclareAtConstructor() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare @constructor : p.Java.new() : @p.Ann; }\nclass Java {\n Java()  { } } \n @interface Ann { }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType ann = unit.getType("Ann");
        IType type = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(ann, this.getName());
        assertMatch("p.Ann", contents, matches);
        matches = findSearchMatches(type, this.getName());
        assertMatch("p.Java", contents, matches);
    }
    public void testQualifiedDeclareAtField() throws Exception {
        String contents = "package p;\naspect Aspect {\n declare @field : int p.Java.xxx : @p.Ann; }\nclass Java {\n int xxx;  { } } \n @interface Ann { }";
        ICompilationUnit unit = createCU("p", "Aspect.aj", contents);
        IType ann = unit.getType("Ann");
        IType type = unit.getType("Java");
        
        List<SearchMatch> matches = findSearchMatches(ann, this.getName());
        assertMatch("p.Ann", contents, matches);
        matches = findSearchMatches(type, this.getName());
        assertMatch("p.Java", contents, matches);
    }

    // Can't do patterns
}
