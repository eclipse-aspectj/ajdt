/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html .
 * 
 * Contributors:
 *   Matt Chapman
**********************************************************************/
package org.eclipse.contribution.visualiser.interfaces.simpleImpl;

import org.eclipse.core.resources.IResource;

public class ResourceMember extends SimpleMember {

	private IResource resource;

	/**
	 * @param name
	 */
	public ResourceMember(String name, IResource resource) {
		super(name);
		this.resource = resource;
	}

	public IResource getResource() {
		return resource;
	}

	public String toString() {
		return new String(
			"ResourceMember:["
				+ fullname
				+ "] Size:["
				+ size.toString()
				+ "] Resource:["
				+ resource
				+ "]");
	}

}
