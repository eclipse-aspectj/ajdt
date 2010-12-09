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

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;

/**
 * This class gives an order to AJCodeElements
 */
public class AJComparator implements Comparator<IAdaptable>, Serializable {

	private static final long serialVersionUID = 8886271915017653329L;

    /* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(IAdaptable o1, IAdaptable o2) {
		if ((o1 instanceof AJCodeElement) && (o2 instanceof AJCodeElement)) {
			return ((AJCodeElement)o1).getNameRange().getOffset() - 
			    ((AJCodeElement)o2).getNameRange().getOffset();
		} else if ((o1 instanceof IJavaElement) && (o2 instanceof IJavaElement)) {
			return compareJavaElements((IJavaElement) o1, (IJavaElement) o2);
		} else if ((o1 instanceof IAdaptable) && (o2 instanceof IAdaptable)) {
		    return compareJavaElements((IJavaElement) o1.getAdapter(IJavaElement.class), (IJavaElement) o2.getAdapter(IJavaElement.class));
		}		
		return 0;
	}

    /**
     * @param o1
     * @param o2
     * @return
     */
    public int compareJavaElements(IJavaElement o1, IJavaElement o2) {
        if (o1 == null || o2 == null) {
            return 0;
        }
        String o1Name = o1.getElementName();
        String o2Name = o2.getElementName();
        return o1Name.compareTo(o2Name);
    }

}
