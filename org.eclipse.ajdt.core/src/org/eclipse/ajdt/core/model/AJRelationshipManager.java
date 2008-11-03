/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

import org.eclipse.ajdt.core.text.CoreMessages;

/**
 * Defines the different relationships which can be present in the structure
 * model
 */
public class AJRelationshipManager {

	/*
	 * Please note - if you want to add another relationship type here (e.g.
	 * type1) you will need to add two entries to the properties file for this
	 * plug-in - type1.displayName and type1.menuName. In addition the name of
	 * the new relationship type (e.g. type1) must not contain spaces.
	 *  
	 */

	public static final AJRelationshipType ADVISES = new AJRelationshipType(
			"advises", CoreMessages.advises_displayName, CoreMessages.advises_menuName); //$NON-NLS-1$

	public static final AJRelationshipType ADVISED_BY = new AJRelationshipType(
			"advised_by", CoreMessages.advised_by_displayName, CoreMessages.advised_by_menuName); //$NON-NLS-1$

	public static final AJRelationshipType DECLARED_ON = new AJRelationshipType(
			"declared_on", CoreMessages.declared_on_displayName, CoreMessages.declared_on_menuName); //$NON-NLS-1$

	public static final AJRelationshipType ASPECT_DECLARATIONS = new AJRelationshipType(
			"aspect_declarations", CoreMessages.aspect_declarations_displayName, CoreMessages.aspect_declarations_menuName); //$NON-NLS-1$

	public static final AJRelationshipType MATCHED_BY = new AJRelationshipType(
			"matched_by", CoreMessages.matched_by_displayName, CoreMessages.matched_by_menuName); //$NON-NLS-1$

	public static final AJRelationshipType MATCHES_DECLARE = new AJRelationshipType(
			"matches_declare", CoreMessages.matches_declare_displayName, CoreMessages.matches_declare_menuName); //$NON-NLS-1$

	public static final AJRelationshipType ANNOTATES = new AJRelationshipType(
			"annotates", CoreMessages.annotates_displayName, CoreMessages.annotates_menuName); //$NON-NLS-1$

	public static final AJRelationshipType ANNOTATED_BY = new AJRelationshipType(
			"annotated_by", CoreMessages.annotated_by_displayName, CoreMessages.annotated_by_menuName); //$NON-NLS-1$

	public static final AJRelationshipType SOFTENS = new AJRelationshipType(
			"softens", CoreMessages.softens_displayName, CoreMessages.softens_menuName); //$NON-NLS-1$ 

	public static final AJRelationshipType SOFTENED_BY = new AJRelationshipType(
			"softened_by", CoreMessages.softened_by_displayName, CoreMessages.softened_by_menuName); //$NON-NLS-1$

	public static final AJRelationshipType USES_POINTCUT = new AJRelationshipType(
			"uses_pointcut", CoreMessages.uses_pointcut_displayName, CoreMessages.uses_pointcut_menuName); //$NON-NLS-1$

	public static final AJRelationshipType POINTCUT_USED_BY = new AJRelationshipType(
			"pointcut_used_by", CoreMessages.pointcut_used_by_displayName, CoreMessages.uses_pointcut_menuName); //$NON-NLS-1$

	/**
	 * Array of all known relationship types
	 */
	private static AJRelationshipType[] allRelationshipTypes = null;
	
	public static AJRelationshipType[] getAllRelationshipTypes() {
		if (allRelationshipTypes == null) {
//			if (AsmHierarchyBuilder.shouldAddUsesPointcut) {
//				allRelationshipTypes = new AJRelationshipType[] {
//						ADVISES, ADVISED_BY, DECLARED_ON, ASPECT_DECLARATIONS, MATCHED_BY,
//						MATCHES_DECLARE, ANNOTATES, ANNOTATED_BY, SOFTENS, SOFTENED_BY,
//						USES_POINTCUT, POINTCUT_USED_BY };
//			} else {
				allRelationshipTypes = new AJRelationshipType[] {
						ADVISES, ADVISED_BY, DECLARED_ON, ASPECT_DECLARATIONS, MATCHED_BY,
						MATCHES_DECLARE, ANNOTATES, ANNOTATED_BY, SOFTENS, SOFTENED_BY};	
//			}		
		}
		return allRelationshipTypes;
	}
	
	public static AJRelationshipType getInverseRelationship(AJRelationshipType type) {
		if (type == ADVISES) {
			return ADVISED_BY;
		}
		if (type == ADVISED_BY) {
			return ADVISES;
		}
		if (type == DECLARED_ON) {
			return ASPECT_DECLARATIONS;
		}
		if (type == ASPECT_DECLARATIONS) {
			return DECLARED_ON;
		}
		if (type == MATCHED_BY) {
			return MATCHES_DECLARE;
		}
		if (type == MATCHES_DECLARE) {
			return MATCHED_BY;
		}
		if (type == ANNOTATES) {
			return ANNOTATED_BY;
		}
		if (type == ANNOTATED_BY) {
			return ANNOTATES;
		}
		if (type == SOFTENS) {
			return SOFTENED_BY;
		}
		if (type == SOFTENED_BY) {
			return SOFTENS;
		}
		if (type == USES_POINTCUT) {
			return POINTCUT_USED_BY;
		}
		if (type == POINTCUT_USED_BY) {
			return USES_POINTCUT;
		}
		return null;
	}
	
	public static AJRelationshipType toRelationshipType(String displayName) {
	    if (displayName.equals(DECLARED_ON.getDisplayName())) {
	        return DECLARED_ON;
	    }
	    if (displayName.equals(MATCHES_DECLARE.getDisplayName())) {
	        return MATCHES_DECLARE;
	    }
        if (displayName.equals(ADVISED_BY.getDisplayName())) {
            return ADVISED_BY;
        }
        if (displayName.equals(ADVISES.getDisplayName())) {
            return ADVISES;
        }
        if (displayName.equals(ASPECT_DECLARATIONS.getDisplayName())) {
            return ASPECT_DECLARATIONS;
        }
        if (displayName.equals(MATCHED_BY.getDisplayName())) {
            return MATCHED_BY;
        }
        if (displayName.equals(ANNOTATED_BY.getDisplayName())) {
            return ANNOTATED_BY;
        }
        if (displayName.equals(ANNOTATES.getDisplayName())) {
            return ANNOTATES;
        }
        if (displayName.equals(SOFTENED_BY.getDisplayName())) {
            return SOFTENED_BY;
        }
        if (displayName.equals(SOFTENS.getDisplayName())) {
            return SOFTENS;
        }
        if (displayName.equals(POINTCUT_USED_BY.getDisplayName())) {
            return POINTCUT_USED_BY;
        }
        if (displayName.equals(USES_POINTCUT.getDisplayName())) {
            return USES_POINTCUT;
        }
        return null;
	}
}
