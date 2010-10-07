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
import org.eclipse.jdt.core.search.SearchMatch;


/**
 * @author Andrew Eisenberg
 * @created Apr 20, 2010
 */
public class ITDAwarePolymorphicSearchTests extends AbstractITDSearchTest {
    public void testITDSearch() throws Exception {
        ICompilationUnit unit = createCU("JavaSuper.java", "class JavaSuper { int foo; }");
        createCU("Java.java", "class Java extends JavaSuper {  }");
        
        String contents = "aspect Aspect { Java.new(int x) { this(); foo = x; }  }";
        createCU("Aspect.aj", contents);
        
        IJavaElement child = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(child, this.getName());
        assertMatch("foo", contents, matches);
    }
    public void testITDSearch2() throws Exception {
        ICompilationUnit unit = createCU("JavaSuper.java", "class JavaSuper { int foo() { return 0; } }");
        createCU("Java.java", "class Java extends JavaSuper {  }");
        
        String contents = "aspect Aspect { Java.new(int x) { this(); foo(); }  }";
        createCU("Aspect.aj", contents);
        
        IJavaElement child = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(child, this.getName());
        assertMatch("foo()", contents, matches);
    }
    public void testITDSearch3() throws Exception {
        ICompilationUnit unit = createCU("JavaSuper.java", "class JavaSuper { int foo; }");
        createCU("Java.java", "class Java extends JavaSuper {  }");
        
        String contents = "aspect Aspect { Java.new(int x) { this(); super.foo = x; }  }";
        createCU("Aspect.aj", contents);
        
        IJavaElement child = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(child, this.getName());
        assertMatch("foo", contents, matches);
    }
    public void testITDSearch4() throws Exception {
        ICompilationUnit unit = createCU("JavaSuper.java", "class JavaSuper { int foo() { return 0; } }");
        createCU("Java.java", "class Java extends JavaSuper {  }");
        
        String contents = "aspect Aspect { Java.new(int x) { this(); super.foo(); }  }";
        createCU("Aspect.aj", contents);
        
        IJavaElement child = findFirstChild(unit);
        
        List<SearchMatch> matches = findSearchMatches(child, this.getName());
        assertMatch("foo()", contents, matches);
    }
    public void testITDSearch5() throws Exception {
        createCU("JavaSuper.java", "class JavaSuper { }");
        createCU("Java.java", "class Java extends JavaSuper {  }");
        
        String contents = "aspect Aspect { Java.new(int x) { this(); foo = x; }\n  int JavaSuper.foo; }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        
        IntertypeElement itd = findLastITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("foo", contents, matches);
    }
    public void testITDSearch6() throws Exception {
        createCU("JavaSuper.java", "class JavaSuper { }");
        createCU("Java.java", "class Java extends JavaSuper {  }");
        
        String contents = "aspect Aspect { Java.new(int x) { this(); foo(); }\n  int JavaSuper.foo() { return 0; } }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        
        IntertypeElement itd = findLastITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("foo()", contents, matches);
    }
    public void testITDSearch7() throws Exception {
        createCU("JavaSuper.java", "class JavaSuper { }");
        createCU("Java.java", "class Java extends JavaSuper {  }");
        
        String contents = "aspect Aspect { Java.new(int x) { this(); super.foo = x; }\n int JavaSuper.foo;  }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        
        IntertypeElement itd = findLastITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("foo", contents, matches);
    }
    public void testITDSearch8() throws Exception {
        createCU("JavaSuper.java", "class JavaSuper {  }");
        createCU("Java.java", "class Java extends JavaSuper {  }");
        
        String contents = "aspect Aspect { Java.new(int x) { this(); super.foo(); }\n int JavaSuper.foo() { return 0; } }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        
        IntertypeElement itd = findLastITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("foo()", contents, matches);
    }
    public void testITDSearch9() throws Exception {
        createCU("JavaSub.java", "class JavaSub extends Java {  }");
        createCU("Java.java", "class Java { int foo() { return 0; } }");
        
        String contents = "aspect Aspect { Java.new(int x) { this(); foo(); }\n int JavaSub.foo() { return 0; } }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        
        IntertypeElement itd = findLastITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("foo()", contents, matches);
    }
    public void testITDSearch10() throws Exception {
        createCU("JavaSub.java", "class JavaSub extends Java {  }");
        createCU("Java.java", "class Java {\n int foo; }");
        
        String contents = "aspect Aspect {\n Java.new(int x) {\n this(); foo = x; }\n int JavaSub.foo; }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        
        IntertypeElement itd = findLastITD(unit);
        
        List<SearchMatch> matches = findSearchMatches(itd, this.getName());
        assertMatch("foo", contents, matches);
    }
}
