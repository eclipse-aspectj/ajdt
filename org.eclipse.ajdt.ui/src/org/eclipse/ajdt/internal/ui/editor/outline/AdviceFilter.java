/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor.outline;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filter to allow hiding of advice from package explorer, outline view etc
 */
public class AdviceFilter extends ViewerFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof AdviceElement) {
			return false;
		}
		return true;
	}

}