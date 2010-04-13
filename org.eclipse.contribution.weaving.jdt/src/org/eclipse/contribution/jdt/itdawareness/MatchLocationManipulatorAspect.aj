package org.eclipse.contribution.jdt.itdawareness;

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
    
    pointcut possibleMatchContents(PossibleMatch possible) : execution(public char[] PossibleMatch.getContents()) &&
        this(possible);
    
    char[] around(PossibleMatch possible) : possibleMatchContents(possible) {
        char[] orig = proceed(possible);
        if (possible.openable instanceof CompilationUnit) {
            char[] translated = provider.translateForMatchProcessing(orig, (CompilationUnit) possible.openable);
            possible.source = translated;
            return translated;
        } else {
            return orig;
        }
    }
    
    pointcut matchReported(SearchMatch match) : execution(protected void MatchLocator.report(SearchMatch)) 
            && args(match);
    
//    void around(SearchMatch match) : matchReported(match) {
//        match.setOffset(provider.translateLocationToOriginal(match.getOffset()));
//        proceed(match);
//    }
    
    pointcut matchLocatorInitialization(MatchLocator locator, JavaProject project) : execution(public void MatchLocator.initialize(JavaProject, int) throws JavaModelException) 
            && this(locator) && args(project,..);
    
    after(MatchLocator locator, JavaProject project) : matchLocatorInitialization(locator, project) {
        locator.lookupEnvironment = provider.createLookupEnvironment(locator.lookupEnvironment, locator.workingCopies, project);
        locator.nameEnvironment = locator.lookupEnvironment.nameEnvironment;
    }
    
}
