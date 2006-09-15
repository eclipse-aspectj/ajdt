/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

import org.aspectj.ajdt.internal.core.builder.AsmHierarchyBuilder;
import org.eclipse.ajdt.core.AspectJPlugin;

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
			"advises", AspectJPlugin.getResourceString("advises.displayName")); //$NON-NLS-1$ //$NON-NLS-2$

	public static final AJRelationshipType ADVISED_BY = new AJRelationshipType(
			"advised_by", AspectJPlugin //$NON-NLS-1$
					.getResourceString("advised_by.displayName")); //$NON-NLS-1$

	public static final AJRelationshipType DECLARED_ON = new AJRelationshipType(
			"declared_on", AspectJPlugin //$NON-NLS-1$
					.getResourceString("declared_on.displayName")); //$NON-NLS-1$

	public static final AJRelationshipType ASPECT_DECLARATIONS = new AJRelationshipType(
			"aspect_declarations", AspectJPlugin //$NON-NLS-1$
					.getResourceString("aspect_declarations.displayName")); //$NON-NLS-1$

	public static final AJRelationshipType MATCHED_BY = new AJRelationshipType(
			"matched_by", AspectJPlugin //$NON-NLS-1$
					.getResourceString("matched_by.displayName")); //$NON-NLS-1$

	public static final AJRelationshipType MATCHES_DECLARE = new AJRelationshipType(
			"matches_declare", AspectJPlugin //$NON-NLS-1$
					.getResourceString("matches_declare.displayName")); //$NON-NLS-1$

	public static final AJRelationshipType ANNOTATES = new AJRelationshipType(
			"annotates", AspectJPlugin //$NON-NLS-1$
					.getResourceString("annotates.displayName")); //$NON-NLS-1$

	public static final AJRelationshipType ANNOTATED_BY = new AJRelationshipType(
			"annotated_by", AspectJPlugin //$NON-NLS-1$
					.getResourceString("annotated_by.displayName")); //$NON-NLS-1$

	public static final AJRelationshipType SOFTENS = new AJRelationshipType(
			"softens", AspectJPlugin.getResourceString("softens.displayName")); //$NON-NLS-1$ //$NON-NLS-2$

	public static final AJRelationshipType SOFTENED_BY = new AJRelationshipType(
			"softened_by", AspectJPlugin //$NON-NLS-1$
					.getResourceString("softened_by.displayName")); //$NON-NLS-1$

	public static final AJRelationshipType USES_POINTCUT = new AJRelationshipType(
			"uses_pointcut", AspectJPlugin.getResourceString("uses_pointcut.displayName")); //$NON-NLS-1$

	public static final AJRelationshipType POINTCUT_USED_BY = new AJRelationshipType(
			"pointcut_used_by", AspectJPlugin.getResourceString("pointcut_used_by.displayName")); //$NON-NLS-1$
	
	/**
	 * Array of all known relationship types
	 */
	private static AJRelationshipType[] allRelationshipTypes = null;
	
	public static AJRelationshipType[] getAllRelatinshipTypes() {
		if (allRelationshipTypes == null) {
			if (AsmHierarchyBuilder.shouldAddUsesPointcut) {
				allRelationshipTypes = new AJRelationshipType[] {
						ADVISES, ADVISED_BY, DECLARED_ON, ASPECT_DECLARATIONS, MATCHED_BY,
						MATCHES_DECLARE, ANNOTATES, ANNOTATED_BY, SOFTENS, SOFTENED_BY,
						USES_POINTCUT, POINTCUT_USED_BY };
			} else {
				allRelationshipTypes = new AJRelationshipType[] {
						ADVISES, ADVISED_BY, DECLARED_ON, ASPECT_DECLARATIONS, MATCHED_BY,
						MATCHES_DECLARE, ANNOTATES, ANNOTATED_BY, SOFTENS, SOFTENED_BY};	
			}		
		}
		return allRelationshipTypes;
	}
}
