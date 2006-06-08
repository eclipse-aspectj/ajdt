/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.launching;

import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

/**
 * Dialog allowing the user to select a runnable type. Adapted from
 * MainTypeSelectionDialog to allow runnable Aspects to be listed.
 */
public class AJMainTypeSelectionDialog extends TwoPaneElementSelector {

	private Object[] types;

	private static class PackageRenderer extends JavaElementLabelProvider {
		public PackageRenderer() {
			super(JavaElementLabelProvider.SHOW_PARAMETERS
					| JavaElementLabelProvider.SHOW_POST_QUALIFIED
					| JavaElementLabelProvider.SHOW_ROOT);
		}

		public Image getImage(Object element) {
			if (element instanceof IType) {
				return super.getImage(((IType) element).getPackageFragment());
			} return null;
		}

		public String getText(Object element) {
			String text = ""; //$NON-NLS-1$
			if (element instanceof IType) {
				text = super.getText(((IType) element).getPackageFragment());
			} 
			return text;
		}
	}

	private static class AJElementLabelProvider extends
			JavaElementLabelProvider {

		 private ILabelProvider labelProvider =
				new DecoratingJavaLabelProvider(new AppearanceAwareLabelProvider());
		
		public AJElementLabelProvider(int i) {
			super(i);
		}

		public Image getImage(Object element) {
			if(element instanceof AspectElement) {
				return labelProvider.getImage(element);
			}
			return super.getImage(element);
		}

		public String getText(Object element) {
			if(element instanceof AspectElement) {
				return labelProvider.getText(element);
			}
			return super.getText(element);
		}
	}

	/**
	 * @param shell
	 * @param types
	 */
	public AJMainTypeSelectionDialog(Shell shell, Object[] types) {
		super(shell, new AJElementLabelProvider(
				JavaElementLabelProvider.SHOW_BASICS
						| JavaElementLabelProvider.SHOW_OVERLAY_ICONS),
				new PackageRenderer());
		this.types = types;
	}

	/**
	 * Returns the main types.
	 */
	public Object[] getTypes() {
		return types;
	}

	/*
	 * @see Windows#configureShell
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
	}

	/*
	 * @see Window#open()
	 */
	public int open() {

		if (types == null)
			return CANCEL;

		setElements(types);
		return super.open();
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	public Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		applyDialogFont(control);
		return control;
	}

}