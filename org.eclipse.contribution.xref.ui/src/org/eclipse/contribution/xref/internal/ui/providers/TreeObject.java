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
package org.eclipse.contribution.xref.internal.ui.providers;

import org.eclipse.core.runtime.IAdaptable;

/**
 * A class to help navigate the cross references
 */
public class TreeObject implements IAdaptable {

	private String name;
	private TreeParent parent;
	private Object data;

	public TreeObject(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setParent(TreeParent parent) {
		this.parent = parent;
	}
	
	public TreeParent getParent() {
		return parent;
	}
	
	public String toString() {
		return getName();
	}
	
	public Object getAdapter(Class key) {
		return null;
	}
	
	public Object getData() {
		return data;
	}
	
	public void setData(Object o) {
		data = o;
	}
}