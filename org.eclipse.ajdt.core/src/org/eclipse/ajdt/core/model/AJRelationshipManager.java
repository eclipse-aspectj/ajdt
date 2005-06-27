/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
			"advised_by", AspectJPlugin
					.getResourceString("advised_by.displayName"));

	public static final AJRelationshipType DECLARED_ON = new AJRelationshipType(
			"declared_on", AspectJPlugin
					.getResourceString("declared_on.displayName"));

	public static final AJRelationshipType ASPECT_DECLARATIONS = new AJRelationshipType(
			"aspect_declarations", AspectJPlugin
					.getResourceString("aspect_declarations.displayName"));

	public static final AJRelationshipType MATCHED_BY = new AJRelationshipType(
			"matched_by", AspectJPlugin
					.getResourceString("matched_by.displayName"));

	public static final AJRelationshipType MATCHES_DECLARE = new AJRelationshipType(
			"matches_declare", AspectJPlugin
					.getResourceString("matches_declare.displayName"));

	public static final AJRelationshipType ANNOTATES = new AJRelationshipType(
			"annotates", AspectJPlugin
					.getResourceString("annotates.displayName"));

	public static final AJRelationshipType ANNOTATED_BY = new AJRelationshipType(
			"annotated_by", AspectJPlugin
					.getResourceString("annotated_by.displayName"));

	public static final AJRelationshipType SOFTENS = new AJRelationshipType(
			"softens", AspectJPlugin.getResourceString("softens.displayName"));

	public static final AJRelationshipType SOFTENED_BY = new AJRelationshipType(
			"softened_by", AspectJPlugin
					.getResourceString("softened_by.displayName"));

	/**
	 * Array of all known relationship types
	 */
	public static AJRelationshipType[] allRelationshipTypes = new AJRelationshipType[] {
			ADVISES, ADVISED_BY, DECLARED_ON, ASPECT_DECLARATIONS, MATCHED_BY,
			MATCHES_DECLARE, ANNOTATES, ANNOTATED_BY, SOFTENS, SOFTENED_BY };
}
