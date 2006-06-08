/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.diff;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

public class CompareElementAction implements IObjectActionDelegate {

	private ISelection fSelection;

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		if (fSelection == null) {
			return;
		}
		if (fSelection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) fSelection;
			// compare with each other
			Object[] elements = ss.toArray();
			if (elements.length == 2) {
				if ((elements[0] instanceof IJavaElement)
						&& (elements[1] instanceof IJavaElement)) {
					IJavaElement fromElement = (IJavaElement) elements[0];
					IJavaElement toElement = (IJavaElement) elements[1];
					try {
						IViewPart view = AspectJUIPlugin.getDefault()
								.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage().showView(
										ChangesView.CROSSCUTTING_VIEW_ID);
						if (view instanceof ChangesView) {
							ChangesView changesView = (ChangesView) view;
							changesView.compareElements(fromElement, toElement);
							changesView.setFocus();
						}
					} catch (PartInitException e) {
					}
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = selection;
	}

}
