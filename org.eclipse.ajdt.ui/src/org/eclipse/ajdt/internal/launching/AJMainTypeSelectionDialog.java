/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.launching;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.ajde.ui.AbstractIcon;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.internal.ui.resources.AJDTIcon;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
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

	private static Map imgDescriptorsToImages = new HashMap();

	private static class PackageRenderer extends JavaElementLabelProvider {
		public PackageRenderer() {
			super(JavaElementLabelProvider.SHOW_PARAMETERS
					| JavaElementLabelProvider.SHOW_POST_QUALIFIED
					| JavaElementLabelProvider.SHOW_ROOT);
		}

		public Image getImage(Object element) {
			if (element instanceof IType) {
				return super.getImage(((IType) element).getPackageFragment());
			} else {
				AJDTIcon icon = (AJDTIcon) AspectJImages.registry().getIcon(
						IProgramElement.Kind.PACKAGE);
				return icon.getImageDescriptor().createImage();

			}
		}

		public String getText(Object element) {
			String text = ""; //$NON-NLS-1$
			if (element instanceof IType) {
				text = super.getText(((IType) element).getPackageFragment());
			} else if (element instanceof Object[]) {
				Object[] elements = (Object[]) element;
				text = ((IProgramElement) elements[0]).getPackageName();
				text += " - " + ((IProject) elements[1]).getName(); //$NON-NLS-1$
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
			Image result = super.getImage(element);
			if (result == null && element instanceof Object[]) {
				Object[] elements = (Object[]) element;
				IProgramElement aspectElement = (IProgramElement) elements[0];
				AbstractIcon icon = AspectJImages.registry().getStructureIcon(
						aspectElement.getKind(),
						aspectElement.getAccessibility());
				if (icon instanceof AJDTIcon) {
					ImageDescriptor desc = ((AJDTIcon) icon)
							.getImageDescriptor();
					ImageDescriptor decorated = AJDTUtils.decorate(desc,
							aspectElement);
					if (imgDescriptorsToImages.get(decorated) instanceof Image) {
						result = (Image) imgDescriptorsToImages.get(decorated);
					} else {
						result = decorated.createImage();
						imgDescriptorsToImages.put(decorated, result);
					}
				}
			}
			return result;
		}

		public String getText(Object element) {
			String text = super.getText(element);
			if (text == null || text.trim().equals("")) { //$NON-NLS-1$
				if (element instanceof Object[]) {
					text = ((IProgramElement) ((Object[]) element)[0])
							.getName();
				}
			}
			return text;
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
		setMessage(LauncherMessages
				.getString("MainTypeSelectionDialog.Choose_a_type")); //$NON-NLS-1$		
		setUpperListLabel(LauncherMessages
				.getString("MainTypeSelectionDialog.Matching_types")); //$NON-NLS-1$
		setLowerListLabel(LauncherMessages
				.getString("MainTypeSelectionDialog.Qualifier")); //$NON-NLS-1$

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