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
package org.eclipse.ajdt.internal.builder;

/**
 * 
 * @author mchapman
 */
public class AJRelationshipManager {
	public static final AJRelationship ADVISES = new AJRelationship("advises");
	public static final AJRelationship ADVISED_BY = new AJRelationship("advised by");
	public static final AJRelationship DECLARED_ON = new AJRelationship("declared on");
	public static final AJRelationship ASPECT_DECLARATIONS = new AJRelationship("aspect declarations");
	public static final AJRelationship MATCHED_BY = new AJRelationship("matched by");
	public static final AJRelationship MATCHES_DECLARE = new AJRelationship("matches declare");
}
