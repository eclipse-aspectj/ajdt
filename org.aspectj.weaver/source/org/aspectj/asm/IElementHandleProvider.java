/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Mik Kersten     initial implementation
 * ******************************************************************/

package org.aspectj.asm;

import java.io.File;

import org.aspectj.bridge.ISourceLocation;

/**
 * Adapter used to uniquely identify program element handles. Can be implemented and overridden in @see{AsmManager} in order to
 * provide IDE-specific mechanisms of identifying elements. For example, AJDT uses workspace-relative paths that are understood by
 * its JavaCore class.
 *
 * @author Mik Kersten
 */
public interface IElementHandleProvider {

	/**
	 * @return a String uniquely identifying this element
	 */
	String createHandleIdentifier(ISourceLocation location);

	/**
	 * @return a String uniquely identifying this element
	 */
	String createHandleIdentifier(File sourceFile, int line, int column, int offset);

	/**
	 * @return a String uniquely identifying this element
	 */
	String createHandleIdentifier(IProgramElement ipe);

	/**
	 * NOTE: this is necessary for the current implementation to look up nodes, but we may want to consider removing it.
	 *
	 * @return a String corresponding to the
	 */
	String getFileForHandle(String handle);

	/**
	 * NOTE: this is necessary for the current implementation to look up nodes, but we may want to consider removing it.
	 *
	 * @return the line number corresponding to this handel
	 */
	int getLineNumberForHandle(String handle);

	int getOffSetForHandle(String handle);

	/**
	 * Initializes handle provider state.
	 *
	 * The initializer is invoked when a new ASM is created on a full build.
	 */
	void initialize();
}
