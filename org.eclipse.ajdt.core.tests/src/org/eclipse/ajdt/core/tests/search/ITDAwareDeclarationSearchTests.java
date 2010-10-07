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
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchMatch;


/**
 * Search tests for Declarations of ITDs
 * @author Andrew Eisenberg
 * @created May 26, 2010
 */
public class ITDAwareDeclarationSearchTests extends AbstractITDSearchTest {

    public void testITDSearchFieldDeclaration1() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n int Java.xxx = 0; }");
        createCU("Java.java", "class Java { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }
    public void testITDSearchFieldDeclaration2() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n int xxx = 0;\n int Java.xxx = 0; }");
        createCU("Java.java", "class Java { }");
        createCU("Other.java", "class Other extends Java { int xxx = 0; }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }

    public void testITDSearchFieldDeclaration3() throws Exception {
        createCU("Aspect.aj", "aspect Aspect {\n int xxx = 0;\n int Java.xxx = 0; }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit unit = createCU("Other.java", "class Other extends Java { int xxx = 0; }");
        
        IMember field = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(field, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(field, matches);
    }
    
    public void testITDSearchFieldDeclaration4() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n int xxx = 0;\n int Java.xxx = 0; }");
        createCU("Java.java", "class Java { }");
        createCU("Other.java", "class Other extends Java { int xxx = 0; }");
        
        IMember field = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(field, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(field, matches);
    }
    
    public void testITDSearchMethodDeclaration1() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx() { } }");
        createCU("Java.java", "class Java { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }

    public void testITDSearchMethodDeclaration2() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx() { } }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java { void xxx() { } }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    // Declaration match will only find declarations in the type or sub-types, not the super type
    public void testITDSearchMethodDeclaration3() throws Exception {
        createCU("Other.java", "abstract class Other { abstract void xxx(); }");
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx() { } }");
        createCU("Java.java", "class Java extends Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }
    
    public void testITDSearchMethodDeclaration4() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx() { } }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java { void xxx() { } }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testITDSearchMethodDeclaration5() throws Exception {
        ICompilationUnit other = createCU("Other.java", "abstract class Other { abstract void xxx(); }");
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx() { } }");
        createCU("Java.java", "class Java extends Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testQualifiedITDSearchMethodDeclaration5() throws Exception {
        ICompilationUnit other = createCU("p", "Other.java", "package p;\npublic abstract class Other { public abstract void xxx(); }");
        ICompilationUnit unit = createCU("q", "Aspect.aj", "package q;\naspect Aspect {\n public void r.Java.xxx() { } }");
        createCU("r", "Java.java", "package r;\npublic class Java extends p.Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testITDSearchMethodSimpleArgDeclaration1() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(int x) { } }");
        createCU("Java.java", "class Java { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }

    public void testITDSearchMethodSimpleArgDeclaration2() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(int x) { } }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java { void xxx(int x) { } }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    // Declaration match will only find declarations in the type or sub-types, not the super type
    public void testITDSearchMethodSimpleArgDeclaration3() throws Exception {
        createCU("Other.java", "abstract class Other { abstract void xxx(int x); }");
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(int x) { } }");
        createCU("Java.java", "class Java extends Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }
    
    public void testITDSearchMethodSimpleArgDeclaration4() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(int x) { } }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java { void xxx(int x) { } }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testITDSearchMethodSimpleArgDeclaration5() throws Exception {
        ICompilationUnit other = createCU("Other.java", "abstract class Other { abstract void xxx(int x); }");
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(int x) { } }");
        createCU("Java.java", "class Java extends Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }

    public void testITDSearchMethodStringArgDeclaration1() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(String x) { } }");
        createCU("Java.java", "class Java { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }

    public void testITDSearchMethodStringArgDeclaration2() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(String x) { } }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java { void xxx(String x) { } }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    // Declaration match will only find declarations in the type or sub-types, not the super type
    public void testITDSearchMethodStringArgDeclaration3() throws Exception {
        createCU("Other.java", "abstract class Other { abstract void xxx(String x); }");
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(String x) { } }");
        createCU("Java.java", "class Java extends Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }
    
    public void testITDSearchMethodStringArgDeclaration4() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(String x) { } }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java { void xxx(String x) { } }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testITDSearchMethodStringArgDeclaration5() throws Exception {
        ICompilationUnit other = createCU("Other.java", "abstract class Other { abstract void xxx(String x); }");
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(String x) { } }");
        createCU("Java.java", "class Java extends Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testITDSearchMethodQualifiedArgDeclaration1() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(java.net.URL x) { } }");
        createCU("Java.java", "class Java { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }

    public void testITDSearchMethodQualifiedArgDeclaration2() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(java.net.URL x) { } }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java { void xxx(java.net.URL x) { } }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    // Declaration match will only find declarations in the type or sub-types, not the super type
    public void testITDSearchMethodQualifiedArgDeclaration3() throws Exception {
        createCU("Other.java", "abstract class Other { abstract void xxx(java.net.URL x); }");
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(java.net.URL x) { } }");
        createCU("Java.java", "class Java extends Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }
    
    public void testITDSearchMethodQualifiedArgDeclaration4() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(java.net.URL x) { } }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java { void xxx(java.net.URL x) { } }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testITDSearchMethodQualifiedArgDeclaration5() throws Exception {
        ICompilationUnit other = createCU("Other.java", "abstract class Other { abstract void xxx(java.net.URL x); }");
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(java.net.URL x) { } }");
        createCU("Java.java", "class Java extends Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testITDSearchMethodGenericArgDeclaration1() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(java.util.List<String> x) { } }");
        createCU("Java.java", "class Java { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }

    public void testITDSearchMethodGenericArgDeclaration2() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(java.util.List<String> x) { } }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java { void xxx(java.util.List<String> x) { } }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    // Declaration match will only find declarations in the type or sub-types, not the super type
    public void testITDSearchMethodGenericArgDeclaration3() throws Exception {
        createCU("Other.java", "abstract class Other { abstract void xxx(java.util.List<String> x); }");
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(java.util.List<String> x) { } }");
        createCU("Java.java", "class Java extends Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }
    
    public void testITDSearchMethodGenericArgDeclaration4() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(java.util.List<String> x) { } }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java { void xxx(java.util.List<String> x) { } }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testITDSearchMethodGenericArgDeclaration5() throws Exception {
        ICompilationUnit other = createCU("Other.java", "abstract class Other { abstract void xxx(java.util.List<String> x); }");
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(java.util.List<String> x) { } }");
        createCU("Java.java", "class Java extends Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testITDSearchMethodLocalArgDeclaration1() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(Java x) { } }");
        createCU("Java.java", "class Java { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }

    public void testITDSearchMethodLocalArgDeclaration2() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(Java x) { } }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java { void xxx(Java x) { } }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testITDSearchMethodLocalArgDeclaration3() throws Exception {
        createCU("Other.java", "abstract class Other { abstract void xxx(Java x); }");
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(Java x) { } }");
        createCU("Java.java", "class Java extends Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }
    
    public void testITDSearchMethodLocalArgDeclaration4() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(Java x) { } }");
        createCU("Java.java", "class Java { }");
        ICompilationUnit other = createCU("Other.java", "class Other extends Java { void xxx(Java x) { } }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testITDSearchMethodLocalArgDeclaration5() throws Exception {
        ICompilationUnit other = createCU("Other.java", "abstract class Other { abstract void xxx(Java x); }");
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n void Java.xxx(Java x) { } }");
        createCU("Java.java", "class Java extends Other { }");
        
        IntertypeElement itd = findFirstITD(unit);
        IMember method = findFirstChild(other);
        
        List<SearchMatch> matches = findSearchMatches(method, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(2, matches);
        assertDeclarationMatches(itd, matches);
        assertDeclarationMatches(method, matches);
    }
    
    public void testITDSearchConstructorDeclaration1() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect {\n \nJava.new(int x) { } }");
        createCU("Java.java", "class Java { \nJava() { } }");
        createCU("Other.java", "class Other extends Java { Other(int x) { super(); } }");
        
        IntertypeElement itd = findFirstITD(unit);
        List<SearchMatch> matches = findSearchMatches(itd, this.getName(), IJavaSearchConstants.DECLARATIONS);
        assertExpectedNumberOfMatches(1, matches);
        assertDeclarationMatches(itd, matches);
    }
}