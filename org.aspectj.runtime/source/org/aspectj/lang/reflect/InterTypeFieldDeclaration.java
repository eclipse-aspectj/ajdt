/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.lang.reflect;

import java.lang.reflect.Type;

/**
 * Represents an inter-type field declaration declared in an aspect.
 */
public interface InterTypeFieldDeclaration extends InterTypeDeclaration {

	/**
	 * @return the field name
	 */
	String getName();

	/**
	 * @return the field type
	 */
	AjType<?> getType();

	/**
	 * @return the generic field type
	 */
	Type getGenericType();

}
