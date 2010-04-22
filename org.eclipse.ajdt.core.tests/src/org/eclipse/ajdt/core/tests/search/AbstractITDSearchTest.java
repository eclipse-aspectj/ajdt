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

package org.eclipse.ajdt.core.tests.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.AspectJCoreTestPlugin;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
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
 * @author Andrew Eisenberg
 * @created Apr 20, 2010
 *
 */
public class AbstractITDSearchTest extends AJDTCoreTestCase {
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

    protected IntertypeElement findFirstITD(ICompilationUnit unit) throws Exception {
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

    protected IntertypeElement findLastITD(ICompilationUnit unit) throws Exception {
        AJCompilationUnit ajUnit = (AJCompilationUnit) unit;
        IType type = ajUnit.getTypes()[0];
        IJavaElement[] children = type.getChildren();
        for (int i = children.length-1; i >= 0 ; i--) {
            if (children[i] instanceof IntertypeElement) {
                return (IntertypeElement) children[i];
            }
        }
        return null;
    }

    protected IMember findFirstChild(ICompilationUnit unit) throws Exception {
        IType type = unit.getTypes()[0];
        IJavaElement[] children = type.getChildren();
        return (IMember) children[0];
    }

    protected List findSearchMatches(IJavaElement elt, String name) throws Exception {
        javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        waitForManualBuild();
        assertNoProblems(javaProject.getProject());
        
        AspectJCoreTestPlugin.logInfo("About to create Search pattern in " + name);
        SearchPattern pattern = SearchPattern.createPattern(elt, IJavaSearchConstants.REFERENCES);
        SearchEngine engine = new SearchEngine();
        JavaSearchScope scope = new JavaSearchScope();
        scope.add(javaProject);

        AspectJCoreTestPlugin.logInfo("About to perform search in " + name);
        ITDAwareSearchRequestor requestor = new ITDAwareSearchRequestor();
        engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, 
                scope, requestor, new NullProgressMonitor());
        return requestor.getMatches();
    }

    protected ICompilationUnit createCU(String name, String contents) throws CoreException {
        return createCompilationUnitAndPackage("", name, contents, javaProject);
    }

    protected void assertNoMatch(String contents, List matches) {
        assertEquals("Should not have found any matches, but instead found matches in:\n" + contents + "\n\nMatches were:" + printMatches(matches), 0, matches.size());
    }

    private String printMatches(List matches) {
        StringBuffer sb = new StringBuffer();
        for (Iterator matchIter = matches.iterator(); matchIter.hasNext();) {
            SearchMatch match = (SearchMatch) matchIter.next();
            sb.append("\n\n" + match);
            
        }
        return sb.toString();
    }

    protected void assertMatch(String matchName, String contents, List matches) {
            assertEquals("Should have found exactly 1 match, but instead found " + printMatches(matches), 1, matches.size());
            SearchMatch match = (SearchMatch) matches.get(0);
            assertEquals("Wrong match location", contents.indexOf(matchName)+1, match.getOffset());
            assertEquals("Wrong match length", matchName.length(), match.getLength());
            
            // disabled because we can't get this right right now.
    //        assertEquals("Expected exact match, but was potential", SearchMatch.A_ACCURATE, match.getAccuracy());
        }

}
