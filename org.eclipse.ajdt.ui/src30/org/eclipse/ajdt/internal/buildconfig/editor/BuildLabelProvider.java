/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.buildconfig.editor;

import org.eclipse.ajdt.internal.buildconfig.editor.model.IBuildEntry;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;


class BuildLabelProvider extends LabelProvider {
	public String getText(Object obj) {
		if (obj instanceof IBuildEntry) {
			return ((IBuildEntry) obj).getName();
		}
		if (obj instanceof IFormPage) {
			return ((IFormPage)obj).getTitle();
		}
		return super.getText(obj);
	}
	public Image getImage(Object obj) {
		if (obj instanceof IBuildEntry) {
			return PDEPlugin.getDefault().getLabelProvider().get(
				PDEPluginImages.DESC_BUILD_VAR_OBJ);
		}
		if (obj instanceof IFormPage) {
			return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_PAGE_OBJ);
		}
		return null;
	}
}