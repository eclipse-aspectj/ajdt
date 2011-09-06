/*******************************************************************************
 * Copyright (c) 2000, 2006, 2008 IBM Corporation, SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Eisenberg (SpringSource) - Adapted for AJDT
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElementAttribute;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


public class PathBlockWorkbookPage extends BuildPathBasePage {
	
	private TreeListDialogField fClassPathList;
	
	public PathBlockWorkbookPage(TreeListDialogField classPathList) {
		fClassPathList= classPathList;
	}
	
	public Control getControl(Composite parent) {
		PixelConverter converter= new PixelConverter(parent);
		
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		
		LayoutUtil.doDefaultLayout(composite, new DialogField[] { fClassPathList }, true, SWT.DEFAULT, SWT.DEFAULT);
		LayoutUtil.setHorizontalGrabbing(fClassPathList.getTreeControl(null));

		int buttonBarWidth= converter.convertWidthInCharsToPixels(24);
		fClassPathList.setButtonsMinWidth(buttonBarWidth);
			
		return composite;
	}
	
	/*
	 * @see BuildPathBasePage#getSelection
	 */
	public List getSelection() {
		return fClassPathList.getSelectedElements();
	}

	/*
	 * @see BuildPathBasePage#setSelection
	 */	
	public void setSelection(List selElements, boolean expand) {
		fClassPathList.selectElements(new StructuredSelection(selElements));
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage#isEntryKind(int)
	 */
	public boolean isEntryKind(int kind) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage#init(org.eclipse.jdt.core.IJavaProject)
	 */
	public void init(IJavaProject javaProject) {
	}

	/**
     * {@inheritDoc}
     */
    public void setFocus() {
    	fClassPathList.setFocus();
    }

    boolean editCustomEntry(CPListElementAttribute attr) {
        return editCustomAttribute(fClassPathList.getTreeViewer().getControl().getShell(), attr);
    }
    
}
