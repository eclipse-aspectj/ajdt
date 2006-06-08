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
package org.eclipse.contribution.xref.ui;

import org.eclipse.contribution.xref.core.IXReference;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * A cross reference where calculating the set of associates is an
 * expensive operation. The <code>IRunnableWithProgress.run()</code> 
 * operation will be called at a client's (often end-user's) request.
 * <p>
 * The set of associates for this cross reference should be evaluated in
 * the body of the run method. After run has been called, getAssociates() 
 * should return the results of the evaluation. These results should be retained
 * (made available through subsequent calls to getAssociates)
 * until such time as evaluate is called again, whereupon the
 * result set should be updated once more. 
 * <p>Implementors may return a non-null empty iterator from getAssociates()
 * before the first call to run.</p>
 * 
 * </p> 
 */
public interface IDeferredXReference
	extends IXReference, IRunnableWithProgress {
	// this section deliberately left blank
}
