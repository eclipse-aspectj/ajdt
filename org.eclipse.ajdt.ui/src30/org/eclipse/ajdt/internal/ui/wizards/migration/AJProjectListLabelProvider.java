/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards.migration;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;

/**
 * Label Provider which returns the project name as text and 
 * the standard project image as the image
 */
public class AJProjectListLabelProvider extends LabelProvider {

    public String getText(Object element) {
        if (element instanceof IProject) {
            return ((IProject)element).getName();
        }
    	return element.toString();
    }
    
    public Image getImage(Object element) {
        IWorkbench workbench = AspectJUIPlugin.getDefault().getWorkbench();
        Image projectImage =
            workbench.getSharedImages().getImage(
                IDE.SharedImages.IMG_OBJ_PROJECT);
        return projectImage;
    }
    
}
