/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;


import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.ui.ProblemsLabelDecorator;
import org.eclipse.swt.graphics.Image;

/*
 * Sian - Added as part of the fix for bug 78182
 */
/**
 * The <code>AspectJEditorErrorTickUpdater</code> will register as a IProblemChangedListener
 * to listen on problem changes of the editor's input. It updates the title image when the annotation
 * model changed.
 */
public class AspectJEditorTitleImageUpdater {

	private ImageDescriptorRegistry registry = JavaPlugin.getImageDescriptorRegistry();
	private final Image baseImage = registry.get(AspectJImages.ASPECTJ_FILE.getImageDescriptor());
	private final ProblemsLabelDecorator problemsDecorator;
	
	private AspectJEditor editor;
	

	public AspectJEditorTitleImageUpdater(AspectJEditor editor) {		
		Assert.isNotNull(editor);
		this.editor = editor;
		problemsDecorator = new ProblemsLabelDecorator(registry);
	}

			
	public boolean updateEditorImage(IJavaElement jelement) {
		Image titleImage= editor.getTitleImage();
		if (titleImage == null) {
			return false;
		}
		Image newImage = problemsDecorator.decorateImage(baseImage, jelement);
		if (titleImage != newImage) {
			editor.customUpdatedTitleImage(newImage);
			return true;
		}
		return false;
	}

	
	public void dispose() {
		problemsDecorator.dispose();
	}

}
