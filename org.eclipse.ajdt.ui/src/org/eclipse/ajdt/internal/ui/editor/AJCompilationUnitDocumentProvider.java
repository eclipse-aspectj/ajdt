/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.ajdt.javamodel.AJCompilationUnitManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider;

/**
 * Document Provider for CompilationUnits and AJCompilationUnits
 */
public class AJCompilationUnitDocumentProvider extends
		CompilationUnitDocumentProvider implements
		ICompilationUnitDocumentProvider {

	public AJCompilationUnitDocumentProvider() {
		super();
	}

	/**
	 * Creates a compilation unit from the given file.
	 * 
	 * @param file
	 *            the file from which to create the compilation unit
	 */
	protected ICompilationUnit createCompilationUnit(IFile file) {
		// bug 77917 - return an AJCompilationUnit if the
		// file ends with .aj. This ensures that we still get the annotation
		// model for .aj files.
		if (file.getFileExtension().equals("aj")) { //$NON-NLS-1$
			return AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file);
		} else {
			return super.createCompilationUnit(file);
		}
	}

}
