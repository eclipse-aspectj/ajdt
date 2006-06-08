/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.dialogs;

import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.internal.ui.resources.AJDTIcon;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.jdt.internal.corext.util.TypeInfo;
import org.eclipse.jdt.internal.ui.util.TypeInfoLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Sian
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AJTypeInfoLabelProvider extends TypeInfoLabelProvider {

	private static final Image ASPECT_ICON = ((AJDTIcon)AspectJImages.instance().getIcon(IProgramElement.Kind.ASPECT)).getImageDescriptor().createImage();
	private int flags;
	
	private boolean isSet(int flag) {
		return (flags & flag) != 0;
	}
	/**
	 * @param flags
	 */
	public AJTypeInfoLabelProvider(int flags) {
		super(flags);
		this.flags = flags;
	}
	
	/* non java-doc
	 * @see ILabelProvider#getImage
	 */	
	public Image getImage(Object element) {
		if(element instanceof AJCUTypeInfo) {
			if (((AJCUTypeInfo)element).isAspect()) {
				if (isSet(SHOW_TYPE_CONTAINER_ONLY)) {
					TypeInfo typeRef= (TypeInfo) element;
					if (typeRef.getPackageName().equals(typeRef.getTypeContainerName())) {
						return super.getImage(element);
					}
				} else if (isSet(SHOW_PACKAGE_ONLY)) {
					return super.getImage(element);
				}
				return ASPECT_ICON;
			}
		}
		return super.getImage(element);
	}	

}
