/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sian January - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.core;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;

/**
 * A working copy owner that creates internal buffers.
 * Based on org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner. 
 * Part of the fix for 117412.
 */
public class AJWorkingCopyOwner extends WorkingCopyOwner {
		
	public static final WorkingCopyOwner INSTANCE = 
		AspectJPlugin.USING_CU_PROVIDER ? (WorkingCopyOwner)DefaultWorkingCopyOwner.PRIMARY :
				(WorkingCopyOwner)new AJWorkingCopyOwner();

	private AJWorkingCopyOwner() {
		// singleton - so use a private constructor
	}

	public IBuffer createBuffer(ICompilationUnit workingCopy) {
		// AspectJ Change begin - use DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider because that has been initialized, wheras a local version wouldn't be
		if (DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider != null) return DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider.createBuffer(workingCopy);
		return super.createBuffer(workingCopy);
	}
	
	public String toString() {
		return "AJDT working copy owner"; //$NON-NLS-1$
	}
}
