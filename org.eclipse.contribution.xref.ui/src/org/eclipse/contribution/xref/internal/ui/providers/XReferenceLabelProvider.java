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
import org.eclipse.contribution.xref.internal.ui.XReferenceUIPlugin;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for the tree of cross references
 */
public class XReferenceLabelProvider extends LabelProvider {

	// any element can be put into the data field of
	// a TreeObject. If it implements IWorkbenchAdapter,
	// it will be displayed correctly in the tree.

	private JavaElementLabelProvider labelProvider =
		new JavaElementLabelProvider(
				JavaElementLabelProvider.SHOW_POST_QUALIFIED
				| JavaElementLabelProvider.SHOW_OVERLAY_ICONS);

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
			} else {
				return (labelProvider.getImage(data));
			}
		} else {
			return XReferenceUIPlugin.getDefault().getXReferenceImage();
		}
	}
}