/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

public class InPlaceHover implements IJavaEditorTextHover {
	
	private PointcutDoctorUIPlugin plugin;
	
	public InPlaceHover() {
		this.plugin = PointcutDoctorUIPlugin.getDefault();
	}

	public void setEditor(IEditorPart editor) {
		// TODO Auto-generated method stub
		
	}

	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (!plugin.isEnabled()) return null;

		final List<String> filenames = new ArrayList<String>();
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow activeWindow = JavaPlugin.getActiveWorkbenchWindow();
				if (activeWindow != null) {
					IWorkbenchPage activePage = activeWindow.getActivePage();
					if (activePage != null) {
						//TODO is this a good way?
						JavaEditor editor = (JavaEditor)activePage.getActiveEditor();
						IJavaElement je = EditorUtility.getEditorInputJavaElement(editor, false);
						String fileName =  je.getPath().toOSString();
						filenames.add(fileName);
					}
				}
			}
		});
		if (filenames.size()>0) {
			//TODO the offset here might be the offset in the editor, rather than that of the document
			String info = plugin.getTexualReasonByFileAndOffset(filenames.get(0), 
					hoverRegion.getOffset()); 
			return info;
		} else return null;
	}

	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		//TODO do we really need to create a hoverRegion here?
		return null;
	}

}
