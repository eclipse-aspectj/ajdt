/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.buildconfig.editor;
import org.eclipse.ajdt.internal.buildconfig.editor.model.IBuild;
import org.eclipse.ajdt.internal.buildconfig.editor.model.IBuildEntry;
import org.eclipse.ajdt.internal.buildconfig.editor.model.IBuildModel;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ajdt.pde.internal.ui.editor.FormOutlinePage;
import org.eclipse.ajdt.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.ajdt.pde.internal.ui.editor.PDEFormPage;
/**
 * @author dejan
 * 
 */
public class BuildOutlinePage extends FormOutlinePage {
	/**
	 * @param editor
	 */
	public BuildOutlinePage(PDEFormEditor editor) {
		super(editor);
	}
	
	protected ILabelProvider createLabelProvider() {
		return new BuildLabelProvider();
	}
		
	protected Object[] getChildren(Object parent) {
		if (parent instanceof PDEFormPage) {
			PDEFormPage page = (PDEFormPage) parent;
			IBuildModel model = (IBuildModel) page.getModel();
			if (model.isValid()) {
				IBuild build = model.getBuild();
				if (page.getId().equals(BuildPage.PAGE_ID))
					return build.getBuildEntries();
			}
		}
		return new Object[0];
	}
	protected String getParentPageId(Object item) {
		String pageId = null;
		if (item instanceof IBuildEntry)
			pageId = BuildPage.PAGE_ID;
		if (pageId != null)
			return pageId;
		return super.getParentPageId(item);
	}
}