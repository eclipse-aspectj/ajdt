/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.internal.ui.providers;

import org.eclipse.contribution.xref.core.IDeferredXReference;
import org.eclipse.contribution.xref.core.IXReferenceNode;
import org.eclipse.contribution.xref.internal.ui.XReferenceUIPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for the tree of cross references
 */
public class XReferenceLabelProvider extends LabelProvider {

	// any element can be put into the data field of
	// a TreeObject. If it implements IWorkbenchAdapter,
	// it will be displayed correctly in the tree.

    private ILabelProvider labelProvider =
		new DecoratingJavaLabelProvider(new AppearanceAwareLabelProvider());

	public String getText(Object obj) {
		String ret = obj.toString();
		Object data = ((TreeObject) obj).getData();
		if ((data != null) && !(data instanceof IDeferredXReference)) {
			ret = labelProvider.getText(data);
		}
		return ret;
	}
	
	public Image getImage(Object obj) {
		Object data = ((TreeObject) obj).getData();
		if (data != null) {
			if (data instanceof IDeferredXReference) {
				return XReferenceUIPlugin.getDefault().getEvaluateImage();
			} else if(data instanceof IXReferenceNode) {
			    return labelProvider.getImage(((IXReferenceNode) data).getJavaElement());
			} else {
				return (labelProvider.getImage(data));
			}
		} else {
			return XReferenceUIPlugin.getDefault().getXReferenceImage();
		}
	}
	
	public void dispose() {
		labelProvider = null;
	}
}