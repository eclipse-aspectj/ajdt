/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Compare two structure models or elements
 */
public class ModelComparison {

	private boolean propagateUp;
	
	public ModelComparison(boolean propagateUp) {
		this.propagateUp = propagateUp;
	}
	
	/**
	 * Compares the given two structure models and returns a two element array
	 * of lists as follows: list[0] is a list of relationships added comparing
	 * the first model with the second, and list[1] is a list of relationships
	 * which have been removed.
	 * 
	 * @param fromModel
	 * @param toModel
	 * @return a two element List array, containing the added list and removed
	 *         list respectively
	 */
	public List[] compareProjects(AJProjectModel fromModel,
			AJProjectModel toModel) {
		List fromRels = fromModel
				.getAllRelationships(AJRelationshipManager.allRelationshipTypes);
		List toRels = toModel
				.getAllRelationships(AJRelationshipManager.allRelationshipTypes);

		// lists to return
		List addedList = new ArrayList();
		List removedList = new ArrayList();

		//System.out.println("from model:");
		for (Iterator iter = fromRels.iterator(); iter.hasNext();) {
			AJRelationship rel = (AJRelationship) iter.next();
			if (!removeMatchingRel(toRels, rel)) {
				//String sourceName = fromModel.getJavaElementLinkName(rel
				//		.getSource());
				//String targetName = fromModel.getJavaElementLinkName(rel
				//		.getTarget());
				//System.out.println("---"+sourceName+"
				// "+rel.getRelationship().getDisplayName()
				//		+" "+targetName);
				removedList.add(propagateRel(rel));
			}
		}

		// anything left in list must have been added
		//System.out.println("to model:");
		for (Iterator iter = toRels.iterator(); iter.hasNext();) {
			AJRelationship rel = (AJRelationship) iter.next();
			//String sourceName = toModel.getJavaElementLinkName(rel.getSource());
			//String targetName = toModel.getJavaElementLinkName(rel.getTarget());
			//System.out.println("+++"+sourceName+"
			// "+rel.getRelationship().getDisplayName()
			//		+" "+targetName);
			addedList.add(propagateRel(rel));
		}
		
		return new List[] { addedList, removedList };
	}

	/**
	 * Compare the crosscutting of two Java elements, for example to see whether
	 * two advice or declare statements advise the same places.
	 * @param fromModel
	 * @param toModel
	 * @param fromEl
	 * @param toEl
	 * @return a two element List array, containing the added list and removed
	 *         list respectively
	 */
	public List[] compareElements(AJProjectModel fromModel,
			AJProjectModel toModel, IJavaElement fromEl,
			IJavaElement toEl) {
		List fromRels = fromModel
			.getAllRelationships(AJRelationshipManager.allRelationshipTypes);
		List toRels = toModel
			.getAllRelationships(AJRelationshipManager.allRelationshipTypes);

		// lists to return
		List addedList = new ArrayList();
		List removedList = new ArrayList();

		Set fromTargets = new HashSet();
		Set toTargets = new HashSet();
		
		for (Iterator iter = fromRels.iterator(); iter.hasNext();) {
			AJRelationship rel = (AJRelationship) iter.next();
			if (matchJavaElements(fromEl,rel.getSource())) {
				fromTargets.add(propagate(rel.getTarget()).getHandleIdentifier());
				removedList.add(propagateRel(rel));
			}
		}
		for (Iterator iter = toRels.iterator(); iter.hasNext();) {
			AJRelationship rel = (AJRelationship) iter.next();
			if (matchJavaElements(toEl,rel.getSource())) {
				String target = propagate(rel.getTarget()).getHandleIdentifier(); 
				toTargets.add(target);
				// only add to addedList if target is different
				if (!fromTargets.contains(target)) {
					addedList.add(propagateRel(rel));
				}
			}
		}
		
		// remove matching targets from the removedList
		for (Iterator iter = removedList.iterator(); iter.hasNext();) {
			AJRelationship rel = (AJRelationship) iter.next();
			if (toTargets.contains(propagate(rel.getTarget()).getHandleIdentifier())) {
				iter.remove();
			}
		}
		
		return new List[] { addedList, removedList };
	}
	
	private IJavaElement propagate(IJavaElement el) {
		if (propagateUp && (el instanceof AJCodeElement)) {
			return el.getParent();
		}
		return el;
	}
	
	private AJRelationship propagateRel(AJRelationship rel) {
		rel.setSource(propagate(rel.getSource()));
		rel.setTarget(propagate(rel.getTarget()));
		return rel;
	}
	
	private boolean matchJavaElements(IJavaElement je1, IJavaElement je2) {
		return propagate(je1).getHandleIdentifier().equals(
				propagate(je2).getHandleIdentifier());
	}
	
	private boolean removeMatchingRel(List list, AJRelationship match) {
		for (Iterator iter = list.iterator(); iter.hasNext();) {
			AJRelationship rel = (AJRelationship) iter.next();
			if (rel.getRelationship().equals(match.getRelationship())
					&& matchJavaElements(rel.getSource(), match.getSource())
					&& matchJavaElements(rel.getTarget(), match.getTarget())) {
				list.remove(rel);
				return true;
			}
		}
		return false;
	}
}
