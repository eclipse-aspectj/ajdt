/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.diff;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

public class ComparisonReferenceDropDownAction extends Action implements IMenuCreator {

	private Menu fMenu;
	
	public ComparisonReferenceDropDownAction() {
		setText(UIMessages.changesView_ComparisonReference); 
		setToolTipText(UIMessages.changesView_ComparisonReference);
		setImageDescriptor(AspectJImages.COMPARISON.getImageDescriptor());
		setMenuCreator(this);
	}
		
	public void dispose() {
		disposeMenu();
	}

	void disposeMenu() {
		if (fMenu != null)
			fMenu.dispose();
	}

	public Menu getMenu(Control parent) {
		disposeMenu();
		fMenu = new Menu(parent);

		IProject project = getProjectForActiveEditor();
		if (project != null) {
			SetComparisonReferenceAction action = new SetComparisonReferenceAction(project,
					UIMessages.changesView_ComparisonReference_last_inc);
			addActionToMenu(fMenu, action, project);
			action = new SetComparisonReferenceAction(project,
					UIMessages.changesView_ComparisonReference_last_full);
			addActionToMenu(fMenu, action, project);

			new MenuItem(fMenu, SWT.SEPARATOR);
			String[] maps = getMapFileNames();
			for (int i = 0; i < maps.length; i++) {
				action = new SetComparisonReferenceAction(project, maps[i]);
				addActionToMenu(fMenu, action, project);
			}
		} else {
			MenuItem m = new MenuItem(fMenu, SWT.RADIO);
			m.setText(UIMessages.changesView_ComparisonReference_no_project);
		}
		return fMenu;
	}

	public Menu getMenu(Menu parent) {
		return null;
	}
	
	protected void addActionToMenu(Menu parent, Action action, IProject project) {
		String txt = action.getText();
		if (txt.equals(UIMessages.changesView_ComparisonReference_last_full)) {
			txt = ChangesView.REF_LAST_FULL;
		} else if (txt.equals(UIMessages.changesView_ComparisonReference_last_inc)) {
			txt = ChangesView.REF_LAST_INC;
		}
		
		action.setChecked(txt.equals(ChangesView.getReferencePoint(project)));
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}
	
	private IProject getProjectForActiveEditor() {
		IWorkbenchWindow window = AspectJUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			return null;
		}
		IEditorPart editor = page.getActiveEditor();
		if (editor == null) {
			return null;
		}
		IEditorInput input = editor.getEditorInput();
		if (input == null) {
			return null;
		}
		IResource resource = (IResource)input.getAdapter(IFile.class);
		if (resource == null) {
			return null;
		}
		return resource.getProject();
	}
	
	private String[] getMapFileNames() {
		List mapNames = new ArrayList();
		IProject project = getProjectForActiveEditor();
		if (project != null) {
			try {
				IResource[] files = project.members();
				for (int i = 0; i < files.length; i++) {
					if ((files[i].getType() == IResource.FILE)
							&& ChangesView.MAP_FILE_EXT.equals(files[i]
									.getFileExtension()) && files[i].exists()) {
						mapNames.add(files[i].getName());
					}
				}
			} catch (CoreException e) {
			}
		}
		return (String[]) mapNames.toArray(new String[] {});
	}
	
	private class SetComparisonReferenceAction extends Action {

		private IProject fProject;
		
		public SetComparisonReferenceAction(IProject project, String label) {
	        super("", AS_RADIO_BUTTON); //$NON-NLS-1$

			setText(label);
			fProject = project;
		}
				
		public void run() {
			String txt = getText();
			if (txt.equals(UIMessages.changesView_ComparisonReference_last_full)) {
				txt = ChangesView.REF_LAST_FULL;
			} else if (txt.equals(UIMessages.changesView_ComparisonReference_last_inc)) {
				txt = ChangesView.REF_LAST_INC;
			}
			ChangesView.setReferencePoint(fProject, txt);
			ChangesView.refresh(false, fProject);
		}
	}
}
