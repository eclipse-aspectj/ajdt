/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;


import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.ProblemsLabelDecorator;
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

	private final Image baseImage = AspectJImages.ASPECTJ_FILE.getImageDescriptor().createImage();
	private final ProblemsLabelDecorator problemsDecorator;
	
	private AspectJEditor editor;
	

	public AspectJEditorTitleImageUpdater(AspectJEditor editor) {		
		Assert.isNotNull(editor);
		this.editor = editor;
		problemsDecorator = new ProblemsLabelDecorator(JavaPlugin.getImageDescriptorRegistry());
	}

			
	public void updateEditorImage(IJavaElement jelement) {
		Image titleImage= editor.getTitleImage();
		if (titleImage == null) {
			return;
		}
		Image newImage = problemsDecorator.decorateImage(baseImage, jelement);
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
		problemsDecorator.dispose();
		baseImage.dispose();
	}

}
