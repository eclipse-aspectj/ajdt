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

/**
 * 
 * @author mchapman
 */
public class AJRelationshipManager {
	public static final AJRelationshipType ADVISES = new AJRelationshipType("advises");
	public static final AJRelationshipType ADVISED_BY = new AJRelationshipType("advised by");
	public static final AJRelationshipType DECLARED_ON = new AJRelationshipType("declared on");
	public static final AJRelationshipType ASPECT_DECLARATIONS = new AJRelationshipType("aspect declarations");
	public static final AJRelationshipType MATCHED_BY = new AJRelationshipType("matched by");
	public static final AJRelationshipType MATCHES_DECLARE = new AJRelationshipType("matches declare");
	public static final AJRelationshipType ANNOTATES = new AJRelationshipType("annotates");
	public static final AJRelationshipType ANNOTATED_BY = new AJRelationshipType("annotated by");

}
