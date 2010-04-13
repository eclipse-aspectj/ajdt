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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;


/**
 * ITD Aware search tests.  Still 2 big problems here:
 * 
 * 1. ITDs references in the target type are not found.
 * Need to insert ITDs into target typebefore match location
 * Can do this with a pointcut inside of MatchLocator.  Must also make sure 
 * to use the correct source locations
 * 
 * 2. ITDs referemnces inside other ITDs targeting the same type are not found
 * Need to translate the ITD method body to insert mock references as in AJCompilationUnit.codeComplete()
 * 
 * In both cases, need to send the converted text to the match locating parser
 * And then need to translate back the source locations
 * 
 * PossibleMatch.getContents() <--- wrap around that and insert ITDs
 * 
 * MatchLocator.report(SearchMatch) <--- translate back slocs here
 * Need to handle both cases: 1. inside of ITD method, and inside of target type
 * 
 * perCFlow(execution(MatchLocator.process()))
 * 
 * @author Andrew Eisenberg
 * @created Mar 18, 2010
 */
public class ITDAwareSearchTests extends AJDTCoreTestCase {

    class ITDAwareSearchRequestor extends SearchRequestor {
        List matches = new ArrayList();
        public void acceptSearchMatch(SearchMatch match) throws CoreException {
            matches.add(match);
        }
        
        public List getMatches() {
            return matches;
        }
    }

    IJavaProject javaProject;
    
    protected void setUp() throws Exception {
        super.setUp();
        javaProject = JavaCore.create(createPredefinedProject("DefaultEmptyProject"));
    }
    
    
    // cannot find matches inside of the target type
    public void testITDSearchFieldInTargetType() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { int Java.xxx = 0; }");
        String contents = "class Java { \nvoid foo() { xxx++; } }";
        createCU("Java.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List matches = findSearchMatches(itd);
        assertMatch("xxx", contents, matches);
    }

    // cannot find matches inside of the target type
    public void testITDSearchMethodInTargetType() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { int Java.xxx() { return 0; } }");
        String contents = "class Java { \nvoid foo() { xxx(); } }";
        createCU("Java.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List matches = findSearchMatches(itd);
        assertMatch("xxx()", contents, matches);
    }
    // cannot find matches inside of the target type
    public void testITDSearchConstructorInTargetType() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { Java.new(int x) { this(); } }");
        String contents = "class Java { \nvoid foo() { new Java(7); } }";
        createCU("Java.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List matches = findSearchMatches(itd);
        assertMatch("new Java(7)", contents, matches);
    }
    
    
    public void testITDSearchFieldInOtherType() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { int Java.xxx = 0; }");
        String contents = "class Java {  }";
        createCU("Java.java", contents);
        
        contents = "class Other { \nvoid foo() { new Java().xxx++; } }";
        createCU("Other.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List matches = findSearchMatches(itd);
        assertMatch("xxx", contents, matches);
    }
    public void testITDSearchMethodInOtherType() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { int Java.xxx() { return  0; } }");
        String contents = "class Java {  }";
        createCU("Java.java", contents);
        
        contents = "class Other { \nvoid foo() { new Java().xxx(); } }";
        createCU("Other.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List matches = findSearchMatches(itd);
        assertMatch("xxx()", contents, matches);
    }
    public void testITDSearchConstructorInOtherType() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { Java.new(int x) { this(); } }");
        String contents = "class Java {  }";
        createCU("Java.java", contents);
        
        contents = "class Other { \nvoid foo() { new Java(7); } }";
        createCU("Other.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List matches = findSearchMatches(itd);
        assertMatch("new Java(7)", contents, matches);
    }
    
    public void testITDSearchFieldInDeclaringAspect() throws Exception {
        String contents = "aspect Aspect {\nvoid foo() { new Java().xxx++; }\n int Java.xxx = 0; }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        createCU("Java.java", "class Java {  }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List matches = findSearchMatches(itd);
        assertMatch("xxx", contents, matches);
    }
    public void testITDSearchMethodInDeclaringAspect() throws Exception {
        String contents = "aspect Aspect { \nvoid foo() { new Java().xxx(); } \nint Java.xxx() { return  0; } }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        createCU("Java.java", "class Java {  }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List matches = findSearchMatches(itd);
        assertMatch("xxx()", contents, matches);
    }
    public void testITDSearchConstructorInDeclaringAspect() throws Exception {
        String contents = "aspect Aspect { \nvoid foo() { new Java(7); }\n Java.new(int x) { this(); } }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        createCU("Java.java", "class Java {  }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List matches = findSearchMatches(itd);
        assertMatch("new Java(7)", contents, matches);
    }

    // disabled because we cannot do matching inside ITDs
    public void _testITDSearchFieldInITD() throws Exception {
        ICompilationUnit unit = createCU("Aspect.aj", "aspect Aspect { int Java.xxx = 0; \nvoid Java.foo() { xxx++; } }");
        String contents = "class Java {  }";
        createCU("Java.java", contents);
        
        IntertypeElement itd = findFirstITD(unit);
        
        List matches = findSearchMatches(itd);
        assertMatch("xxx", contents, matches);
    }
    // disabled because we cannot do matching inside ITDs
    public void _testITDSearchMethodInITD() throws Exception {
        String contents = "aspect Aspect { \nvoid Java.foo() { xxx(); }\n int Java.xxx() { return  0; } }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        createCU("Java.java", "class Java {  }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List matches = findSearchMatches(itd);
        assertMatch("xxx", contents, matches);
    }
    public void testITDSearchConstructorInITD() throws Exception {
        String contents = "aspect Aspect { Java.new(int x) { this(); } \nvoid Java.foo() { new Java(7); } }";
        ICompilationUnit unit = createCU("Aspect.aj", contents);
        createCU("Java.java", "class Java {  }");
        
        IntertypeElement itd = findFirstITD(unit);
        
        List matches = findSearchMatches(itd);
        assertMatch("new Java(7)", contents, matches);
    }

    private IntertypeElement findFirstITD(ICompilationUnit unit) throws Exception {
        AJCompilationUnit ajUnit = (AJCompilationUnit) unit;
        IType type = ajUnit.getTypes()[0];
        IJavaElement[] children = type.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof IntertypeElement) {
                return (IntertypeElement) children[i];
            }
        }
        return null;
    }


    private List findSearchMatches(IJavaElement elt) throws Exception {
        javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        waitForManualBuild();
        assertNoProblems(javaProject.getProject());
        
        SearchPattern pattern = SearchPattern.createPattern(elt, IJavaSearchConstants.REFERENCES);
        SearchEngine engine = new SearchEngine();
        JavaSearchScope scope = new JavaSearchScope();
        scope.add(javaProject);
        
        ITDAwareSearchRequestor requestor = new ITDAwareSearchRequestor();
        engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, 
                scope, requestor, new NullProgressMonitor());
        return requestor.getMatches();
    }

    
    private ICompilationUnit createCU(String name, String contents) throws CoreException {
        return createCompilationUnitAndPackage("", name, contents, javaProject);
    }


    private void assertMatch(String matchName, String contents, List matches) {
        assertEquals("Should have found exactly 1 match", 1, matches.size());
        SearchMatch match = (SearchMatch) matches.get(0);
        assertEquals("Wrong match location", contents.indexOf(matchName)+1, match.getOffset());
        assertEquals("Wrong match length", matchName.length(), match.getLength());
        
        // disabled because we can't get this right right now.
//        assertEquals("Expected exact match, but was potential", SearchMatch.A_ACCURATE, match.getAccuracy());
    }
    
}
