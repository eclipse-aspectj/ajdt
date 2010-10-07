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
package org.eclipse.contribution.jdt.itdawareness;

import java.util.List;

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;
import org.eclipse.jdt.internal.junit.launcher.JUnit4TestFinder;

/**
 * This aspect maintains match locations for a possible match
 * while doing searches.
 * 
 * When a possible match is discovered, its source must be translated so
 * that ITDs can be inserted into the potential match.
 * 
 * The mapping between source original and translated locations must be 
 * maintained so that when the actual match is reported, the actual source 
 * location is used.
 * 
 * @author Andrew Eisenberg
 * @created Apr 7, 2010
 */
public privileged aspect MatchLocationManipulatorAspect perthis(within(MatchLocator)) {

    /**
     * This will be null if AJDT is not installed (ie- JDT Weaving installed, but no AJDT)
     */
    private SearchAdapter adapter = SearchAdapter.getInstance();
    
    private HierarchyResolver resolver;
    
    /**
     * This pointcut captures the processing of a possible match and is used to allow
     * others to hook in extra search matches that are not found by the match locator
     */
    pointcut matchProcessing(MatchLocator locator, PossibleMatch match, boolean bindingsWereCreated) : execution(protected void MatchLocator.process(PossibleMatch, boolean)) 
            && args(match, bindingsWereCreated) && this(locator);
    
    after(MatchLocator locator, PossibleMatch match, boolean bindingsWereCreated) : matchProcessing(locator, match, bindingsWereCreated) {
        try {
            if (adapter.getProvider() != null && locator.requestor != null && match.openable != null && isInterestingProject(match.openable.getJavaProject().getProject())) {
                try {
                    List<SearchMatch> extraMatches = adapter.getProvider().findExtraMatches(match, locator.pattern, resolver);
                    for (SearchMatch extraMatch : extraMatches) {
                        locator.requestor.acceptSearchMatch(extraMatch);
                    }
                } catch (Exception e) {
                    JDTWeavingPlugin.logException("Exception while search for: " + match, e);
                } finally {
                    adapter.getProvider().matchProcessed(match);
                }
            }
        } catch (Exception e) {
            JDTWeavingPlugin.logException("Exception while search for: " + match, e);
        }
    }
    
    /**
     * This pointcut captures the initialization of a match locator.  It allows a third party 
     * to stuff in a custom lookup environment and name environment.  In the case of AspectJ
     * projects, this allows the creation of ITD-aware type and member lookups.
     */
    pointcut matchLocatorInitialization(MatchLocator locator, JavaProject project) : execution(public void MatchLocator.initialize(JavaProject, int) throws JavaModelException) 
            && this(locator) && args(project,..);
    
    after(MatchLocator locator, JavaProject project) : matchLocatorInitialization(locator, project) {
        if (adapter.getProvider() != null && isInterestingProject(project.getProject())) {
            locator.lookupEnvironment = adapter.getProvider().createLookupEnvironment(locator.lookupEnvironment, locator.workingCopies, project);
            locator.nameEnvironment = locator.lookupEnvironment.nameEnvironment;
            resolver = new HierarchyResolver(locator.lookupEnvironment, null /* not needed for our purposes*/);
        } 
    }
    
    
    /**
     * This pointcut targets a SearchRequestor that accepts potential test matches
     * for the JUnit4 Test finder
     */
    pointcut junit4TestMatchFound(SearchMatch potentialMatch) : within(JUnit4TestFinder.AnnotationSearchRequestor) 
            && execution(public void acceptSearchMatch(SearchMatch) throws CoreException)
            && args(potentialMatch);
    
    before(SearchMatch potentialMatch) : junit4TestMatchFound(potentialMatch) {
        if (potentialMatch instanceof ReferenceMatch) {
            ReferenceMatch refMatch = (ReferenceMatch) potentialMatch;
            Object elt = refMatch.getElement();
            if (elt instanceof IJavaElement) {
                IJavaElement javaElt = (IJavaElement) elt;
                if (adapter.getProvider() != null && isInterestingProject(javaElt.getJavaProject().getProject())) {
                    try {
                        javaElt = this.adapter.getProvider().filterJUnit4TestMatch(javaElt);
                        if (javaElt != null) {
                            refMatch.setElement(javaElt);
                        } else {
                            refMatch.setAccuracy(SearchMatch.A_INACCURATE);
                        }
                    } catch (JavaModelException e) {
                        JDTWeavingPlugin.logException("Exception while search for: " + potentialMatch, e);
                    }
                }
            }
        }
    }
    
    private boolean isInterestingProject(IProject proj) {
        return proj != null &&
                WeavableProjectListener.getInstance().isWeavableProject(proj);
    }
}
