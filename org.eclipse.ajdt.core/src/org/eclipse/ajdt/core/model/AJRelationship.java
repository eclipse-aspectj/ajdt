/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

import org.eclipse.jdt.core.IJavaElement;


/**
 * See bug 253245
 * This class is a facade and only used for backwards compatibility for the SpringIDE
 * 
 * @deprecated
 */
public class AJRelationship {

	private IJavaElement source;

	private IJavaElement target;

	private AJRelationshipType relationship;

	private boolean runtimeTest;
	
	/**
	 * @deprecated 
	 */
	public AJRelationship(IJavaElement source, AJRelationshipType relationship,
			IJavaElement target, boolean runtimeTest) {
		this.source = source;
		this.target = target;
		this.relationship = relationship;
		this.runtimeTest = runtimeTest;
	}

    /**
     * @deprecated 
     */
	public IJavaElement getSource() {
		return source;
	}

    /**
     * @deprecated 
     */
	public IJavaElement getTarget() {
		return target;
	}

	public void setSource(IJavaElement source) {
		this.source = source;
	}

	public void setTarget(IJavaElement target) {
		this.target = target;
	}

    /**
     * @deprecated 
     */
	public AJRelationshipType getRelationship() {
		return relationship;
	}

	public String toString() {
		return source.getElementName() + " --> " + relationship.getDisplayName() //$NON-NLS-1$
				+ " --> " + target.getElementName(); //$NON-NLS-1$
	}

    /**
     * @deprecated 
     */
	public boolean hasRuntimeTest() {
		return runtimeTest;
	}
}
