package org.eclipse.contribution.jdt.itdawareness;

import java.util.List;

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.hierarchy.HierarchyResolver;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;

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

    ISearchProvider provider = new SearchAdapter().getProvider();
    
    HierarchyResolver resolver;
    
    pointcut matchProcessing(MatchLocator locator, PossibleMatch match, boolean bindingsWereCreated) : execution(protected void MatchLocator.process(PossibleMatch, boolean)) 
            && args(match, bindingsWereCreated) && this(locator);
    
    after(MatchLocator locator, PossibleMatch match, boolean bindingsWereCreated) : matchProcessing(locator, match, bindingsWereCreated) {
        try {
            if (provider != null && locator.requestor != null && match.openable != null && isInterestingProject(match.openable.getJavaProject().getProject())) {
                List<SearchMatch> extraMatches = provider.findExtraMatches(match, locator.pattern, resolver);
                for (SearchMatch extraMatch : extraMatches) {
                    locator.requestor.acceptSearchMatch(extraMatch);
                }
            }
        } catch (Exception e) {
            JDTWeavingPlugin.logException("Exception while search for: " + match, e);
        }
    }
    
    pointcut matchLocatorInitialization(MatchLocator locator, JavaProject project) : execution(public void MatchLocator.initialize(JavaProject, int) throws JavaModelException) 
            && this(locator) && args(project,..);
    
    after(MatchLocator locator, JavaProject project) : matchLocatorInitialization(locator, project) {
        if (provider != null && isInterestingProject(project.getProject())) {
            locator.lookupEnvironment = provider.createLookupEnvironment(locator.lookupEnvironment, locator.workingCopies, project);
            locator.nameEnvironment = locator.lookupEnvironment.nameEnvironment;
            resolver = new HierarchyResolver(locator.lookupEnvironment, null /* not needed for our purposes*/);
        } 
    }
    
    private boolean isInterestingProject(IProject proj) {
        return proj != null &&
                WeavableProjectListener.getInstance().isWeavableProject(proj);
    }
    
}
