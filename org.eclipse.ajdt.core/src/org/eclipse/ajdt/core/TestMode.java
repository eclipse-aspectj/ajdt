/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.core;

/**
 * Placeholder for a boolean flag that is only to be set to true while executing JUnit tests.
 * Some code may alter its behavior slightly to accomodate running in a test environment.
 * <p>
 * For example, avoid error dialog popups that would hang the test execution. 
 */
public class TestMode {

	public static boolean isTesting = false;

}
