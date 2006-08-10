/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.lazystart;

/*
 * Loading classes in this lazystart package does not immediately cause the
 * plugin to active (as specified in MANIFEST.MF). This is done to avoid early
 * activation of AJDT plugins. Once AJDT classes outside this package are
 * referred to, the plugins are then activated.
 */

public interface IAdviceChangedListener {

	/**
	 * Indicate that there has been a change in the set of advised elements
	 * 
	 */
	public void adviceChanged();
}
