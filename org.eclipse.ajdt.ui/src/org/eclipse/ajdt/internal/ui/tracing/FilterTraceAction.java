/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman    - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.tracing;

import java.util.List;

import org.eclipse.contribution.xref.internal.ui.text.XRefMessages;
import org.eclipse.contribution.xref.ui.filters.CustomFilterDialog;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;

/**
 * Configures the debug trace categories used by the EventTraceView
 */
public class FilterTraceAction extends Action {

	private static List populatingList;
	private static List checkedList;
	private static List defaultCheckedList;
	private static String dialogTitle;
	private static String dialogMessage;

	private Shell parentShell;
	private String tooltipText;

	public FilterTraceAction(Shell shell, List items,
			List checkedItems, List defaultItems, String dlogTitle,
			String dlogMessage, String tooltip) {
	    FilterTraceAction.populatingList = items;
	    FilterTraceAction.checkedList = checkedItems;
	    FilterTraceAction.defaultCheckedList = defaultItems;
	    FilterTraceAction.dialogTitle = dlogTitle;
	    FilterTraceAction.dialogMessage = dlogMessage;
		parentShell = shell;
		tooltipText = tooltip;
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
		Action showDialogAction = new ShowFilterDialogAction();
		showDialogAction.setToolTipText(tooltipText);
		tooBar.add(showDialogAction);

	}

	class ShowFilterDialogAction extends Action {
		ShowFilterDialogAction() {
			setText(XRefMessages.OpenCustomFiltersDialogAction_text);
			setImageDescriptor(JavaPluginImages.DESC_ELCL_FILTER);
			setDisabledImageDescriptor(JavaPluginImages.DESC_DLCL_FILTER);
		}

		public void run() {
			checkedList = CustomFilterDialog.showDialog(parentShell,
					populatingList, checkedList, defaultCheckedList,
					dialogTitle, dialogMessage);
			DebugTracing.setDebugCategories(checkedList);
		}
	}
}
