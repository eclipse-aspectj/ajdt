package org.eclipse.contribution.jdt.itdawareness;

import java.util.List;

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
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
@SuppressWarnings("unused")
public privileged aspect MatchLocationManipulatorAspect /*percflow(execution(protected void MatchLocator.process(PossibleMatch, boolean)))*/ {

    ISearchProvider provider = new SearchAdapter().getProvider();
    
    pointcut matchProcessing(MatchLocator locator, PossibleMatch match, boolean bindingsWereCreated) : execution(protected void MatchLocator.process(PossibleMatch, boolean)) 
            && args(match, bindingsWereCreated) && this(locator);
    
    after(MatchLocator locator, PossibleMatch match, boolean bindingsWereCreated) : matchProcessing(locator, match, bindingsWereCreated) {
        try {
            if (locator.requestor != null && match.openable != null && isInterestingProject(match.openable.getJavaProject().getProject())) {
                List<SearchMatch> extraMatches = provider.findExtraMatches(match, locator.pattern);
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
        if (isInterestingProject(project.getProject())) {
            locator.lookupEnvironment = provider.createLookupEnvironment(locator.lookupEnvironment, locator.workingCopies, project);
            locator.nameEnvironment = locator.lookupEnvironment.nameEnvironment;
        } 
    }
    
    private boolean isInterestingProject(IProject proj) {
        return proj != null &&
                WeavableProjectListener.getInstance().isWeavableProject(proj);
    }
}
