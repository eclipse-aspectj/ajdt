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

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.junit.launcher.JUnit4TestFinder;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jun 19, 2010
 */
public privileged aspect JUnit4TestFinderAspect {
    
    private ISearchProvider provider = new SearchAdapter().getProvider();

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
                if (isInterestingProject(javaElt.getJavaProject().getProject())) {
                    try {
                        javaElt = this.provider.filterJUnit4TestMatch(javaElt);
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
