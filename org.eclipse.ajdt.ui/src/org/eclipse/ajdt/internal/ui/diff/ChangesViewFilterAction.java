/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ben Dalziel     - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.diff;

import java.util.List;

import org.eclipse.contribution.xref.ui.XReferenceUIPlugin;
import org.eclipse.contribution.xref.ui.filters.CustomFilterDialog;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.filters.FilterMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;

public class ChangesViewFilterAction extends Action {

	private static List populatingList;
	private static List checkedList;
	private static List defaultCheckedList;
	private static String dialogTitle;
	private static String dialogMessage;

	private Shell parentShell;

	public ChangesViewFilterAction(Shell shell, List items,
			List checkedItems, List defaultItems, String dlogTitle,
			String dlogMessage) {
		populatingList = items;
		checkedList = checkedItems;
		defaultCheckedList = defaultItems;
		dialogTitle = dlogTitle;
		dialogMessage = dlogMessage;
		parentShell = shell;
	}

	public void fillActionBars(IActionBars actionBars) {
		fillToolBar(actionBars.getToolBarManager());
		fillViewMenu(actionBars.getMenuManager());
	}

	/**
	 * Fills the given view menu with the entries managed by the group.
	 * 
	 * @param viewMenu
	 *            the menu to fill
	 */
	public void fillViewMenu(IMenuManager viewMenu) {
	}
	
	protected List getCheckedList() {
		return checkedList;
	}

	private void fillToolBar(IToolBarManager tooBar) {
		tooBar.add(new ShowFilterDialogAction());

	}

	class ShowFilterDialogAction extends Action {
		ShowFilterDialogAction() {
			setText(XReferenceUIPlugin.getResourceString("OpenCustomFiltersDialogAction.text"));
			setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
			setDisabledImageDescriptor(JavaPluginImages.DESC_DLCL_FILTER);
		}

		public void run() {
			checkedList = CustomFilterDialog.showDialog(parentShell,
					populatingList, checkedList, defaultCheckedList,
					dialogTitle, dialogMessage);
			
			ChangesView.refresh(true);
		}
	}
}
