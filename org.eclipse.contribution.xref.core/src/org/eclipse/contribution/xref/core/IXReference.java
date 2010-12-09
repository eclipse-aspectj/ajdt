/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.core;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;

/**
 * A base interface to capture references between elements in the workspace
 */
public interface IXReference {

	/**
	 * The name of the reference type (e.g. "implements")
	 * @return the reference type name, suitable for display in a user interface
	 */
	public String getName();
	
	/**
	 * The list of related items. Typically these would be 
	 * IResource and IJavaElements, but any object can be
	 * added to the list and will be displayed correctly in
	 * the ui as long as implements the IAdaptable interface
	 * and returns an IWorkbenchAdapter on request.
	 * @return a non-null iterator over the related items
	 */
	public Iterator<IAdaptable> getAssociates();
	
}
