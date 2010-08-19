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

import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.search.SearchMatch;


/**
 * @author Andrew Eisenberg
 * @created Mar 18, 2010
 */
public class ITDAwareSearchTests extends AbstractITDSearchTest {

    public void testITDSearchFieldInTargetType() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { int Java.xxx = 0; }");
        String contents = "class Java { \nvoid foo() { xxx++; } }";
        createCU("Java.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("xxx", contents, matches);
    }
    public void testQualifiedITDSearchFieldInTargetType() throws Exception {
        ICompilationUnit unit = createCU("p", "Aspect.aj", "package p;\naspect Aspect { public int q.Java.xxx = 0; }");
        String contents = "package q;\npublic class Java { \npublic void foo() { xxx++; } }";
        createCU("q", "Java.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("xxx", contents, matches);
    }

    public void testITDSearchMethodInTargetType() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { int Java.xxx() { return 0; } }");
        String contents = "class Java { \nvoid foo() { xxx(); } }";
        createCU("Java.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("xxx()", contents, matches);
    }
    // cannot find matches inside of the target type
    public void testITDSearchConstructorInTargetType() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { Java.new(int x) { this(); } }");
        String contents = "class Java { \nvoid foo() { new Java(7); } }";
        createCU("Java.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("new Java(7)", contents, matches);
    }
    
    
    public void testITDSearchFieldInOtherType() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { int Java.xxx = 0; }");
        String contents = "class Java {  }";
        createCU("Java.java", contents);
        
        contents = "class Other { \nvoid foo() { new Java().xxx++; } }";
        createCU("Other.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("xxx", contents, matches);
    }
    public void testITDSearchMethodInOtherType() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { int Java.xxx() { return  0; } }");
        String contents = "class Java {  }";
        createCU("Java.java", contents);
        
        contents = "class Other { \nvoid foo() { new Java().xxx(); } }";
        createCU("Other.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("xxx()", contents, matches);
    }
    public void testQualifiedITDSearchMethodInOtherType() throws Exception {
        ICompilationUnit unit = createCU("p", "Aspect.aj", "package p;\naspect Aspect { public int q.Java.xxx() { return  0; } }");
        String contents = "package q;\npublic class Java {  }";
        createCU("q", "Java.java", contents);
        
        contents = "package q;\npublic class Other { \nvoid foo() { new Java().xxx(); } }";
        createCU("q", "Other.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("xxx()", contents, matches);
    }
    public void testITDSearchConstructorInOtherType() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { Java.new(int x) { this(); } }");
        String contents = "class Java {  }";
        createCU("Java.java", contents);
        
        contents = "class Other { \nvoid foo() { new Java(7); } }";
        createCU("Other.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("new Java(7)", contents, matches);
    }
    
    public void testITDSearchFieldInDeclaringAspect() throws Exception {
        String contents = "aspect Aspect {\nvoid foo() { new Java().xxx++; }\n int Java.xxx = 0; }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        createCU("Java.java", "class Java {  }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("xxx", contents, matches);
    }
    public void testITDSearchMethodInDeclaringAspect() throws Exception {
        String contents = "aspect Aspect { \nvoid foo() { new Java().xxx(); } \nint Java.xxx() { return  0; } }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        createCU("Java.java", "class Java {  }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("xxx()", contents, matches);
    }
    public void testITDSearchConstructorInDeclaringAspect() throws Exception {
        String contents = "aspect Aspect { \nvoid foo() { new Java(7); }\n Java.new(int x) { this(); } }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        createCU("Java.java", "class Java {  }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("new Java(7)", contents, matches);
    }

    public void testITDSearchFieldInITD() throws Exception {
        String contents = "aspect Aspect { void Java.foo() { xxx++; }\n int Java.xxx = 0; }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        createCU("Java.java", "class Java {  }");
        
        IntertypeElement itd = findLastITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("xxx", contents, matches);
    }
    
    public void testITDSearchMethodInITD() throws Exception {
        String contents = "aspect Aspect { \nvoid Java.foo() { xxx(); }\n int Java.xxx() { return  0; } }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        createCU("Java.java", "class Java {  }");
        
        IntertypeElement itd = findLastITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("xxx()", contents, matches);
    }
    public void testITDSearchRegularMethodInITD() throws Exception {
        String contents = "aspect Aspect { \nvoid Java.foo() { regular(); } }";
        createCU("Aspect.aj", contents);
        ICompilationUnit unit = createCU("Java.java", "class Java { void regular() { } }");
        
        IMember toSearch = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(toSearch, this.getName());
        assertMatch("regular()", contents, matches);
    }
    public void testITDSearchRegularMethodInITD2() throws Exception {
        String contents = "aspect Aspect { \nvoid Java.foo() { regular(); regular(7); } }";
        createCU("Aspect.aj", contents);
        ICompilationUnit unit = createCU("Java.java", "class Java { void regular() { } void regular(int a) { } }");
        
        IMember toSearch = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(toSearch, this.getName());
        assertMatch("regular()", contents, matches);
    }
    public void testITDSearchRegularFieldInITD() throws Exception {
        String contents = "aspect Aspect { \nvoid Java.foo() { regular++; } }";
        createCU("Aspect.aj", contents);
        ICompilationUnit unit = createCU("Java.java", "class Java {\n int regular; }");
        
        IMember toSearch = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(toSearch, this.getName());
        assertMatch("regular", contents, matches);
    }
    public void testITDSearchRegularFieldInITD2() throws Exception {
        String contents = "aspect Aspect { \n void Java.foo() { regular++; } \n int regular; }";
        createCU("Aspect.aj", contents);
        ICompilationUnit unit = createCU("Java.java", "class Java {\n int regular; }");
        
        IMember toSearch = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(toSearch, this.getName());
        assertMatch("regular", contents, matches);
    }
    public void testITDSearchRegularFieldInITD3() throws Exception {
        String contents = "aspect Aspect { int regular;\n void Java.foo(int regular) { regular++; } }";
        createCU("Aspect.aj", contents);
        ICompilationUnit unit = createCU("Java.java", "class Java {\n int regular; }");
        
        IMember toSearch = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(toSearch, this.getName());
        assertNoMatch(contents, matches);
    }
    public void testITDSearchRegularFieldInITD4() throws Exception {
        String contents = "aspect Aspect { int regular;\n void Java.foo() { int regular = 0; regular++; } }";
        createCU("Aspect.aj", contents);
        ICompilationUnit unit = createCU("Java.java", "class Java {\n int regular; }");
        
        IMember toSearch = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(toSearch, this.getName());
        assertNoMatch(contents, matches);
    }
    public void testITDSearchRegularFieldInITD5() throws Exception {
        String contents = "aspect Aspect { int regular;\n void Java.foo() { for(int regular = 0; regular < 5; regular++) regular++; } }";
        createCU("Aspect.aj", contents);
        ICompilationUnit unit = createCU("Java.java", "class Java { int regular; }");
        
        IMember toSearch = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(toSearch, this.getName());
        assertNoMatch(contents, matches);
    }
    public void testITDSearchRegularFieldInITD6() throws Exception {
        String contents = "aspect Aspect { int regular;\n void Java.foo() { int regular = 0, other = 0; regular++; } }";
        createCU("Aspect.aj", contents);
        ICompilationUnit unit = createCU("Java.java", "class Java { int regular; }");
        
        IMember toSearch = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(toSearch, this.getName());
        assertNoMatch(contents, matches);
    }
    public void testITDSearchConstructorInITD() throws Exception {
        String contents = "aspect Aspect { Java.new(int x) { this(); } \nvoid Java.foo() { new Java(7); } }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        createCU("Java.java", "class Java {  }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("new Java(7)", contents, matches);
    }
    
    public void testITDInOtherPackages() throws Exception {
        String contents = "package com.f; import com.bar.Bar; aspect Aspect {   \n  public int Bar.getFoo() { return foo; }  \n   public void Bar.setFoo(int newval) { this.foo = newval; } }";
        createCU("com.f", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("com.bar", "Bar.java", "package com.bar; \n public class Bar {\n public int foo;} ");
        
        IJavaElement elt = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(elt, this.getName());
        assertTwoMatches("foo", contents, matches);
    }
    public void testQualifiedITDInOtherPackages() throws Exception {
        String contents = "package com.f; aspect Aspect {   \n  public int com.bar.Bar.getFoo() { return foo; }  \n   public void com.bar.Bar.setFoo(int newval) { this.foo = newval; } }";
        createCU("com.f", "Aspect.aj", contents);
        ICompilationUnit unit = createCU("com.bar", "Bar.java", "package com.bar; \n public class Bar {\n public int foo;} ");
        
        IJavaElement elt = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(elt, this.getName());
        assertTwoMatches("foo", contents, matches);
    }
}
