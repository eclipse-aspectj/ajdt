/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;


import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jface.text.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

/*
 * Sian - Added as part of the fix for bug 78182
 */
/**
 * The <code>AspectJEditorErrorTickUpdater</code> will register as a IProblemChangedListener
 * to listen on problem changes of the editor's input. It updates the title image when the annotation
 * model changed.
 */
public class AspectJEditorTitleImageUpdater {

	
	private AppearanceAwareLabelProvider labelProvider;
	private AspectJEditor editor;
	

	public AspectJEditorTitleImageUpdater(AspectJEditor editor) {
		Assert.isNotNull(editor);
		this.editor = editor;
		labelProvider=  new AppearanceAwareLabelProvider(0, JavaElementImageProvider.SMALL_ICONS);
//		labelProvider.addLabelDecorator(new ImageDecorator());
	}

			
	public void updateEditorImage(IJavaElement jelement) {
		Image titleImage= editor.getTitleImage();
		if (titleImage == null) {
			return;
		}
		Image newImage= labelProvider.getImage(jelement);
		if (titleImage != newImage) {
			postImageChange(newImage);
		}
	}

	
	private void postImageChange(final Image newImage) {
		Shell shell= editor.getEditorSite().getShell();
		if (shell != null && !shell.isDisposed()) {
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					editor.updatedTitleImage(newImage);
				}
			});
		}
	}	
	
	public void dispose() {
		labelProvider.dispose();
	}

}
