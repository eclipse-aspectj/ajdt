/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import java.util.Set;

/*
 * Sian - Added as part of the fix for bug 78182
 */
/**
 * A listener for problem changes
 */
public interface IProblemChangedListener {

    /**
     * The listener is notified that problems have changed
     * @param changedResources Set of IResources
     */
	void problemsChanged(Set changedResources);
	
}
