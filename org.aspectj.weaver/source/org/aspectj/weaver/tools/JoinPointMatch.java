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
package org.aspectj.weaver.tools;

/**
 * @author colyer
 * The result of asking a ShadowMatch to match at a given join point.
 */
public interface JoinPointMatch {

	/**
	 * True if the pointcut expression has matched at this join point, and false
	 * otherwise
	 */
	boolean matches();

	/**
	 * Get the parameter bindings at the matched join point.
	 * If the join point was not matched an empty array is returned.
	 */
	PointcutParameter[] getParameterBindings();
}
