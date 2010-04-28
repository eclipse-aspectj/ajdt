package org.eclipse.ajdt.internal.core.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

import org.eclipse.jdt.core.search.SearchMatch;

/**
 * Copied from org.eclipse.jdt.internal.corext.refactoring.base.ReferencesInBinaryContext
 */
public class ReferencesInBinaryContext extends RefactoringStatusContext {

	private List<SearchMatch> fMatches= new ArrayList<SearchMatch>();

	private final String fDescription;

	public ReferencesInBinaryContext(String description) {
		fDescription= description;
	}

	public String getDescription() {
		return fDescription;
	}


	public void add(SearchMatch match) {
		fMatches.add(match);
	}

	public List<SearchMatch> getMatches() {
		return fMatches;
	}

	/*
	 * @see org.eclipse.ltk.core.refactoring.RefactoringStatusContext#getCorrespondingElement()
	 */
	public Object getCorrespondingElement() {
		return null;
	}

	public void addErrorIfNecessary(RefactoringStatus status) {
		if (getMatches().size() != 0) {
			status.addError("Binary references to a refactored element have been found. They will not be updated, which may lead to problems if you proceed.", this);
		}
	}

	public String toString() {
		return fDescription + " (" + fMatches.size() + " matches)"; //$NON-NLS-1$ //$NON-NLS-2$
	}

}
