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
package org.eclipse.ajdt.buildconfigurator.editor;

import org.eclipse.ajdt.buildconfigurator.editor.model.IBuild;
import org.eclipse.ajdt.buildconfigurator.editor.model.IBuildEntry;
import org.eclipse.ajdt.buildconfigurator.editor.model.IBuildModel;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.pde.internal.ui.elements.DefaultContentProvider;


class BuildOutlineContentProvider extends DefaultContentProvider
		implements
			ITreeContentProvider {
	public Object[] getChildren(Object parent) {
		return new Object[0];
	}
	public boolean hasChildren(Object parent) {
		return false;
	}
	public Object getParent(Object child) {
		if (child instanceof IBuildEntry)
			return ((IBuildEntry) child).getModel();
		return null;
	}
	public Object[] getElements(Object parent) {
		if (parent instanceof IBuildModel) {
			IBuildModel model = (IBuildModel) parent;
			IBuild build = model.getBuild();
			return build.getBuildEntries();
		}
		return new Object[0];
	}
}