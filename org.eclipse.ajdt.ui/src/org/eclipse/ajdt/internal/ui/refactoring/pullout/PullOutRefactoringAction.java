/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.refactoring.pullout;

import java.util.List;

import org.eclipse.ajdt.internal.ui.refactoring.SelectionUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class PullOutRefactoringAction implements IWorkbenchWindowActionDelegate, IEditorActionDelegate, IViewActionDelegate {

	private PullOutRefactoring currSelection;
	private RefactoringStatus  currStatus;
	
	private CompilationUnitEditor editor = null;
	private IWorkbenchWindow window = null;

	public void init(IWorkbenchWindow window) {
		this.window= window;
	}

	public void init(IViewPart view) {
		window = view.getViewSite().getWorkbenchWindow();
	}

	public void dispose() {
		editor = null;
		currSelection = null;
		window = null;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		currStatus = new RefactoringStatus();
		currSelection = new PullOutRefactoring();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection extended = (IStructuredSelection) selection;
			Object[] elements= extended.toArray();
			for (int i = 0; i < elements.length; i++) {
				if (elements[i] instanceof IMethod || elements[i] instanceof IField) {
					currSelection.addMember((IMember) elements[i], currStatus);
				}
			}
		} else if (selection instanceof ITextSelection) {
			if (editor != null) {
				ITextSelection textSel = (ITextSelection) selection;
				CompilationUnitDocumentProvider provider = (CompilationUnitDocumentProvider)
				editor.getDocumentProvider();
				ICompilationUnit unit = provider.getWorkingCopy(editor.getEditorInput());
				if (unit != null) {
					List<IJavaElement> candidates = SelectionUtil.findSelectedElements(unit, textSel);
					for (IJavaElement candidate : candidates) {
						if (candidate != null && (candidate instanceof IMethod || candidate instanceof IField)) {
							currSelection.addMember((IMember) candidate, currStatus);
						}
					}
				}
			}
		}
		action.setEnabled(currSelection.hasMembers());
		if (window == null) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			window = workbench.getActiveWorkbenchWindow();
		}
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof CompilationUnitEditor) {
			editor = (CompilationUnitEditor) targetEditor;
		} else {
			editor = null;
		}
	}

	public void run(IAction action) {
		PullOutRefactoringWizard wizard = new PullOutRefactoringWizard(currSelection);
		wizard.setInitialConditionCheckingStatus(currStatus);
		run(wizard, getShell());
	}

	public void run(RefactoringWizard wizard, Shell parent) {
		try {
			RefactoringWizardOpenOperation operation= new RefactoringWizardOpenOperation(wizard);
			operation.run(parent, wizard.getDefaultPageTitle());
		} catch (InterruptedException exception) {
			// Do nothing
		}
	}

	private Shell getShell() {
		if (window != null) {
			return window.getShell();
		} else if (editor != null) {
			return editor.getEditorSite().getShell();
		} else {
			return null;
		}
	}

}
