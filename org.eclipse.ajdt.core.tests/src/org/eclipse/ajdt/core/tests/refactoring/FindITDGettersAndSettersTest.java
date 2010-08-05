/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.core.tests.refactoring;

import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.contribution.jdt.itdawareness.ISearchProvider;
import org.eclipse.contribution.jdt.itdawareness.SearchAdapter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author Andrew Eisenberg
 * @created Apr 27, 2010
 *
 */
public class FindITDGettersAndSettersTest extends AbstractAJDTRefactoringTest {

    
    private ISearchProvider searchProvider = SearchAdapter.getInstance().getProvider();

    public void testFindGetter1() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { \n private int foo; }",
                              "package bar;\nimport foo.Java; privileged aspect Aspect { \n int Java.getFoo() { \n return foo; } }"}
                );

        IField field = getFirstField(units);
        IJavaElement getter = searchProvider.findITDGetter(field);
        assertEquals("Should have found an ITD", getFirstIntertypeElement(units[1]), getter);
    }
    public void testFindSetter1() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { \n private int foo; }",
                              "package bar;\nimport foo.Java; privileged aspect Aspect { \n void Java.setFoo(int foo) { \nthis.foo = foo; } }"}
                );

        IField field = getFirstField(units);

        IJavaElement getter = searchProvider.findITDSetter(field);
        assertEquals("Should have found an ITD", getFirstIntertypeElement(units[1]), getter);
    }
    // disabled because ajc doesn't produce the right handle ids for fully qualified itds
    public void _testFindGetter2() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { \n private int foo; }",
                "privileged aspect Aspect { \n int foo.Java.getFoo() { \n return foo; } }"}
        );
        
        IField field = getFirstField(units);

        IJavaElement getter = searchProvider.findITDGetter(field);
        assertEquals("Should have found an ITD", getFirstIntertypeElement(units[1]), getter);
    }
    // disabled because ajc doesn't produce the right handle ids for fully qualified itds
    public void _testFindSetter2() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { \n private int foo; }",
                "privileged aspect Aspect { \n void foo.Java.setFoo(int foo) { \nthis.foo = foo; } }"}
        );
        
        IField field = getFirstField(units);

        IJavaElement getter = searchProvider.findITDSetter(field);
        assertEquals("Should have found an ITD", getFirstIntertypeElement(units[1]), getter);
    }
    public void testFindGetter3() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { \n private int foo; }",
                              "package bar;\nimport foo.Java; privileged aspect Aspect { \n int Java.getFoo() { \n return foo; } \n void Java.setFoo(int foo) { \nthis.foo = foo; } }"}
                );

        IField field = getFirstField(units);

        IJavaElement getter = searchProvider.findITDGetter(field);
        assertEquals("Should have found an ITD", getFirstIntertypeElement(units[1]), getter);
    }
    public void testFindSetter3() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { \n private int foo; }",
                              "package bar;\nimport foo.Java; privileged aspect Aspect { \n void Java.setFoo(int foo) { \nthis.foo = foo; }\n int Java.getFoo() { \n return foo; } }"}
                );

        IField field = getFirstField(units);

        IJavaElement getter = searchProvider.findITDSetter(field);
        assertEquals("Should have found an ITD", getFirstIntertypeElement(units[1]), getter);
    }
    public void testFindGetter4() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { \n private String foo; }",
                              "package bar;\nimport foo.Java; privileged aspect Aspect { \n String Java.getFoo() { \n return foo; } }"}
                );

        IField field = getFirstField(units);

        IJavaElement getter = searchProvider.findITDGetter(field);
        assertEquals("Should have found an ITD", getFirstIntertypeElement(units[1]), getter);
    }
    public void testFindSetter4() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { \n private String foo; }",
                              "package bar;\nimport foo.Java; privileged aspect Aspect { \n void Java.setFoo(String foo) { \nthis.foo = foo; } }"}
                );

        IField field = getFirstField(units);

        IJavaElement getter = searchProvider.findITDSetter(field);
        assertEquals("Should have found an ITD", getFirstIntertypeElement(units[1]), getter);
    }
    public void testFindGetter5() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { }",
                "package bar;\nimport foo.Java; privileged aspect Aspect { \n String Java.getFoo() { \n return foo; } \n String Java.foo; }"}
        );
        IntertypeElement itd = getLastIntertypeElement(units[1]);
        IField field = (IField) itd.createMockDeclaration(units[0].getTypes()[0]);
        

        IJavaElement getter = searchProvider.findITDGetter(field);
        assertEquals("Should have found an ITD", getFirstIntertypeElement(units[1]), getter);
    }
    public void testFindSetter5() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { }",
                "package bar;\nimport foo.Java; privileged aspect Aspect { \n void Java.setFoo(String foo) { \nthis.foo = foo; }\n String Java.foo;  }"}
        );
        IntertypeElement itd = getLastIntertypeElement(units[1]);
        IField field = (IField) itd.createMockDeclaration(units[0].getTypes()[0]);
        

        IJavaElement setter = searchProvider.findITDSetter(field);
        assertEquals("Should have found an ITD", getFirstIntertypeElement(units[1]), setter);
    }
    public void testFindGetter6() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { }",
                "package bar;\nimport foo.Java; privileged aspect Aspect { \n String[] Java.getFoo() { \n return foo; } \n String[] Java.foo; }"}
        );
        IntertypeElement itd = getLastIntertypeElement(units[1]);
        IField field = (IField) itd.createMockDeclaration(units[0].getTypes()[0]);
        

        IJavaElement getter = searchProvider.findITDGetter(field);
        assertEquals("Should have found an ITD", getFirstIntertypeElement(units[1]), getter);
    }
    public void testFindSetter6() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { }",
                "package bar;\nimport foo.Java; privileged aspect Aspect { \n void Java.setFoo(String[] foo) { \nthis.foo = foo; }\n String[] Java.foo;  }"}
        );
        IntertypeElement itd = getLastIntertypeElement(units[1]);
        IField field = (IField) itd.createMockDeclaration(units[0].getTypes()[0]);
        

        IJavaElement setter = searchProvider.findITDSetter(field);
        assertEquals("Should have found an ITD", getFirstIntertypeElement(units[1]), setter);
    }



    public void testNoFindGetter1() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { \n private int foo; }",
                "package bar;\nimport foo.Java; privileged aspect Aspect { \n int Java.getFoo(int x) { \n return foo; } }"}
        );
        
        IField field = getFirstField(units);

        IJavaElement getter = searchProvider.findITDGetter(field);
        assertNull("Should have found an ITD", getter);
    }
    public void testNoFindSetter1() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { \n private int foo; }",
                              "package bar;\nimport foo.Java; privileged aspect Aspect { \n int Java.setFoo(int foo) { \nthis.foo = foo; return foo;} }"}
                );

        IField field = getFirstField(units);

        IJavaElement setter = searchProvider.findITDSetter(field);
        assertNull("Should have found an ITD", setter);
    }
    public void testNoFindGetter2() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { \n private int foo; }",
                "package bar;\nimport foo.Java; privileged aspect Aspect { \n void Java.getFoo() { \n  } }"}
        );
        
        IField field = getFirstField(units);

        IJavaElement getter = searchProvider.findITDGetter(field);
        assertNull("Should have found an ITD", getter);
    }
    public void testNoFindSetter2() throws Exception {
        ICompilationUnit[] units = createUnits(
                new String[] {"foo", "bar"}, 
                new String[] {"Java.java", "Aspect.aj"}, 
                new String[] {"package foo;\npublic class Java { \n private int foo; }",
                "package bar;\nimport foo.Java; privileged aspect Aspect { \n void Java.setFoo() { \n } }"}
        );
        
        IField field = getFirstField(units);
        
        IJavaElement setter = searchProvider.findITDSetter(field);
        assertNull("Should have found an ITD", setter);
    }


}
