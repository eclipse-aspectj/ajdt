/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import java.util.Set;

import org.eclipse.ajdt.buildconfigurator.ImageDecorator;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.internal.ui.ajde.CompilerTaskListManager;
import org.eclipse.ajdt.internal.ui.ajde.IProblemChangedListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jface.text.Assert;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;

/*
 * Sian - Added as part of the fix for bug 78182
 */
/**
 * The <code>AspectJEditorErrorTickUpdater</code> will register as a IProblemChangedListener
 * to listen on problem changes of the editor's input. It updates the title image when the annotation
 * model changed.
 */
public class AspectJEditorErrorTickUpdater implements IProblemChangedListener {

	
	private JavaUILabelProvider labelProvider;
	private AspectJEditor editor;

	public AspectJEditorErrorTickUpdater(AspectJEditor editor) {
		Assert.isNotNull(editor);
		this.editor = editor;
		labelProvider=  new JavaUILabelProvider(0, JavaElementImageProvider.SMALL_ICONS);
		labelProvider.addLabelDecorator(new ImageDecorator());
		CompilerTaskListManager.getInstance().addListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.internal.ui.ajde.IProblemChangedListener#problemsChanged(java.util.List)
	 */
	public void problemsChanged(Set changedResources) {
		IEditorInput input= editor.getEditorInput();
		if (input != null) {
			IJavaElement jElement = (IJavaElement)input.getAdapter(IJavaElement.class);
			if(jElement != null) {
				IResource resource = jElement.getResource();
				if(changedResources.contains(resource)) {
					updateEditorImage(jElement);
				}
			} else {
				IFile file = (IFile) input.getAdapter(IFile.class);
				if(changedResources.contains(file)) {
					updateEditorImage(AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(file));				
				}
			}
		}
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
		CompilerTaskListManager.getInstance().removeListener(this);
	}

}
