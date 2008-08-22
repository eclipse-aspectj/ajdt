/*******************************************************************************
 * Copyright (c) 2008 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * Andrew Eisenberg  (SpringSource)
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2008 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * Andrew Eisenberg  (SpringSource)
 *******************************************************************************/
package org.eclipse.ajdt.mylyn.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.internal.builder.AJNode;
import org.eclipse.contribution.xref.internal.ui.providers.TreeObject;
import org.eclipse.contribution.xref.ui.views.XReferenceView;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.context.ui.AbstractAutoFocusViewAction;
import org.eclipse.mylyn.context.ui.InterestFilter;
import org.eclipse.ui.IViewPart;

/**
 * This class adds context filtering to the AspectJ cross references view
 * @author andrew eisenberg
 *
 */
public class FocusXRefViewAction extends AbstractAutoFocusViewAction {
	
	static class XRefInterestFilter extends InterestFilter {
		
		/**
		 * Determines if label is one of the text strings that can
		 * appear in the XRef view
		 * @param label
		 * @return
		 */
		private boolean isXRefLabel(String label) {
			return label != null && 
					(label.equals("advised by") || 
					label.equals("advises") ||
					label.equals("declared on") ||
					label.equals("aspect declarations") ||
					label.equals("matched by") ||
					label.equals("matches declare") ||
					label.equals("annotates") ||
					label.equals("annotated by") ||
					label.equals("softens") ||
					label.equals("softened by") ||
					label.equals("uses pointcut") ||
					label.equals("pointcut used by"));
		}

		
		@Override
		protected boolean isInteresting(IInteractionElement element) {
			return super.isInteresting(element);
		}
		
		@Override
		public boolean select(Viewer viewer, Object parent, Object object) {
			if (object instanceof TreeObject) {
				TreeObject node = (TreeObject) object;
				Object data = node.getData();
				if (data != null) {
					if (data instanceof AJNode) {
						data = ((AJNode) data).getJavaElement();
					}
					return super.select(viewer, parent, data);
				} else if (isXRefLabel(node.getName())) {
					// always select the XRef labels
					return true;
				}
			}
			return false;
		}
	}
	

	public FocusXRefViewAction() {
		super(new XRefInterestFilter(), true, true, false);
	}

	@Override
	public List<StructuredViewer> getViewers() {
		List<StructuredViewer> viewers = new ArrayList<StructuredViewer>();
		IViewPart part = super.getPartForAction();
		if (part instanceof XReferenceView) {
			viewers.add(((XReferenceView) part).getTreeViewer());
		}
		return viewers;
	}

}
