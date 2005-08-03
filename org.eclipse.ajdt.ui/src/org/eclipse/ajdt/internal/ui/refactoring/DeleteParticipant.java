/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.refactoring;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Delete participant that closes AspectJ editors when the element is deleted
 */
// Partial fix for bug 98261
public class DeleteParticipant extends
		org.eclipse.ltk.core.refactoring.participants.DeleteParticipant {

	private Object element;

	protected boolean initialize(Object element) {
		this.element = element;
		return element instanceof IJavaElement;
	}

	public String getName() {
		return null;
	}

	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return null;
	}

	public Change createChange(IProgressMonitor pm) throws 
			OperationCanceledException {
		
		Set activeEditors = AspectJEditor.getActiveEditorList();
		for (Iterator iter = activeEditors.iterator(); iter.hasNext();) {
			AspectJEditor editor = (AspectJEditor) iter.next();
			if(element instanceof ICompilationUnit) {
				if(((FileEditorInput)editor.getEditorInput()).getFile().equals(((ICompilationUnit)element).getResource())) {
					editor.close(false);
				}
			} else if (element instanceof IPackageFragment) {
				if(((IPackageFragment)element).getResource().contains(((FileEditorInput)editor.getEditorInput()).getFile())) {
					editor.close(false);
				}
			} else if (element instanceof IPackageFragmentRoot) {
				if(((IPackageFragmentRoot)element).getResource().contains(((FileEditorInput)editor.getEditorInput()).getFile())) {
					editor.close(false);
				}
			}
		}
		return null;
	}

}
