/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.model;

import java.util.Comparator;
import java.io.Serializable;

import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.jdt.core.IJavaElement;

/**
 * This class gives an order to AJCodeElements
 */
public class AJComparator implements Comparator, Serializable {

	private static final long serialVersionUID = 8886271915017653329L;

    /* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		if ((o1 instanceof AJCodeElement) && (o2 instanceof AJCodeElement)) {
			return ((AJCodeElement)o1).getNameRange().getOffset() - 
			    ((AJCodeElement)o2).getNameRange().getOffset();
		} else if ((o1 instanceof IJavaElement) && (o2 instanceof IJavaElement)) {
			String o1Name = ((IJavaElement)o1).getElementName();
			String o2Name = ((IJavaElement)o2).getElementName();
			return o1Name.compareTo(o2Name);
		}		
		return 0;
	}

}
