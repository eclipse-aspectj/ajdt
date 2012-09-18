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

import org.eclipse.ajdt.pointcutdoctor.core.explain.Reason;
import org.eclipse.contribution.xref.internal.ui.providers.TreeObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;


public class XReferenceTreeSelectionListener implements ISelectionListener {
	
	private PointcutDoctorUIPlugin plugin;
	private ReasonHighlighter reasonHighlighter = new ReasonHighlighter();
	
	public XReferenceTreeSelectionListener(PointcutDoctorUIPlugin plugin) {
		this.plugin = plugin;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IStructuredSelection structuredSel = (IStructuredSelection) selection;
		
		Object selected = structuredSel.getFirstElement();
		if (selected instanceof TreeObject) {
			TreeObject obj = (TreeObject)selected;
			Object data = obj.getData();
			if (data instanceof ShadowNode) {
				ShadowNode sn = (ShadowNode) data;
				Reason reason = plugin.explain(sn);
				System.out.println(reason);
				reasonHighlighter.highlight(reason);
				//viewer.getTree().setToolTipText(reason.getFullTextExplanation());
			}
		}
	}

}
