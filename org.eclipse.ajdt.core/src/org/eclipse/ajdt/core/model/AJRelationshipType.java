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

/**
 * 
 * @author mchapman
 */
public class AJRelationshipType {
	
	private String displayName;
	private String internalName;
	private String menuName;

	public AJRelationshipType(String internalName, String displayName, String menuName) {
		this.displayName = displayName;
		this.internalName = internalName;
		this.menuName = menuName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public String getInternalName() {
		return internalName;
	}
	
	public String getMenuName() {
		return menuName;
	}
	
	@Override
	public String toString() {
		return "AJRelationshipType("+getDisplayName()+")";
	}
	
}