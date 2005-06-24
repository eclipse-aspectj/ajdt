/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * 
 * @author mchapman
 */
public class AJModelUtils {

	private static Job refreshJob;

	private static int previousExecutionTime;
	
	/**
	 * Goes through all the open editors and updates the outline page for each
	 * (if they are using the standard Java outline page)
	 */
	public static void refreshOutlineViews() {
		int delay = 5*previousExecutionTime;
		if (delay < 100) {
			delay = 100;
		} else if (delay > 5000) {
			delay = 5000;
		}
		getRefreshOutlinePagesJob().schedule(delay);
	}

	// reuse the same Job to avoid excessive updates
	private static Job getRefreshOutlinePagesJob() {
		if (refreshJob == null) {
			refreshJob = new RefreshOutlinePagesJob();
		}
		return refreshJob;
	}

	private static class RefreshOutlinePagesJob extends UIJob {
		RefreshOutlinePagesJob() {
			super(AspectJUIPlugin
					.getResourceString("utils.refresh.outline.job")); //$NON-NLS-1$
		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			long start = System.currentTimeMillis();
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
					.getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
				IWorkbenchPage[] pages = windows[i].getPages();
				for (int x = 0; x < pages.length; x++) {
					IEditorReference[] editors = pages[x].getEditorReferences();
					for (int z = 0; z < editors.length; z++) {
						IEditorPart editor = editors[z].getEditor(true);
						if (editor != null) {
							Object out = editor
									.getAdapter(IContentOutlinePage.class);
							if (out instanceof JavaOutlinePage) {
								refreshOutline((JavaOutlinePage) out);
							}
						}
					}
				}
			}
			previousExecutionTime = (int)(System.currentTimeMillis() - start);
			//System.out.println("refresh outline: elapsed="+previousExecutionTime);
			return Status.OK_STATUS;
		}
	}

	private static void refreshOutline(JavaOutlinePage page) {
		try {
			// Here be dragons
			Class clazz = page.getClass();
			Field field = clazz.getDeclaredField("fOutlineViewer"); //$NON-NLS-1$
			field.setAccessible(true); // cough cough
			Class viewer = StructuredViewer.class;
			Method method = viewer.getMethod("refresh", //$NON-NLS-1$
					new Class[] { boolean.class });
			Object outlineViewer = field.get(page);
			if (outlineViewer != null) {
				method.invoke(outlineViewer, new Object[] { Boolean.TRUE });
			}
		} catch (Exception e) {
		}
	}

}