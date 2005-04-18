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
public class AJRelationshipType {
	
	private String displayName;
	private String internalName;

	public AJRelationshipType(String internalName, String displayName) {
		this.displayName = displayName;
		this.internalName = internalName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public String getInternalName() {
		return internalName;
	}
}