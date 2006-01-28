/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.cuprovider;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ICompilationUnitProvider;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.core.PackageFragment;

public class AspectJCompilationUnitProvider implements ICompilationUnitProvider {

	public ICompilationUnit createCompilationUnit(IPackageFragment parent, String name, WorkingCopyOwner owner) {
		return new AJCompilationUnit((PackageFragment)parent, name, owner);
	}

}
