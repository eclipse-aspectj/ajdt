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

import org.eclipse.jdt.core.IJavaElement;

public class AJRelationship {

	private IJavaElement source;

	private IJavaElement target;

	private AJRelationshipType relationship;

	public AJRelationship(IJavaElement source, AJRelationshipType relationship,
			IJavaElement target) {
		this.source = source;
		this.target = target;
		this.relationship = relationship;
	}

	public IJavaElement getSource() {
		return source;
	}

	public IJavaElement getTarget() {
		return target;
	}

	public AJRelationshipType getRelationship() {
		return relationship;
	}

	public String toString() {
		return source.getElementName() + " --> " + relationship.getName()
				+ " --> " + target.getElementName();
	}
}
