/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;

/**
 * Compares two structure models
 */
public class ModelComparison {

	/**
	 * Compares the given two structure models and returns a two element array
	 * of lists as follows: list[0] is a list of relationships added comparing
	 * the first model with the second, and list[1] is a list of relationships
	 * which have been removed.
	 * 
	 * @param fromModel
	 * @param toModel
	 * @return
	 */
	public static List[] compare(AJProjectModel fromModel,
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
				removedList.add(rel);
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
			addedList.add(rel);
		}

		return new List[] { addedList, removedList };
	}

	private static boolean matchJavaElements(IJavaElement je1, IJavaElement je2) {
		return je1.getHandleIdentifier().equals(je2.getHandleIdentifier());
	}
	
	private static boolean removeMatchingRel(List list, AJRelationship match) {
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
