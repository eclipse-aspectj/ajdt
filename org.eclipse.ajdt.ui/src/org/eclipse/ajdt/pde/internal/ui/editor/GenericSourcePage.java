/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.pde.internal.ui.editor;

import org.eclipse.jface.viewers.*;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 *
 */
public class GenericSourcePage extends PDESourcePage {
	/**
	 * @param editor
	 * @param id
	 */
	public GenericSourcePage(PDEFormEditor editor, String id) {
		super(editor, id);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.neweditor.PDESourcePage#createOutlineLabelProvider()
	 */
	protected ILabelProvider createOutlineLabelProvider() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.neweditor.PDESourcePage#createOutlineContentProvider()
	 */
	protected ITreeContentProvider createOutlineContentProvider() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.neweditor.PDESourcePage#outlineSelectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	protected void outlineSelectionChanged(SelectionChangedEvent e) {
	}
	protected IContentOutlinePage createOutlinePage() {
		return null;
	}
}
