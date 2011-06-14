/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Eisenberg - factored out common code with AspectPathBlock 
 *                        into PathBlok
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.internal.launching.LaunchConfigurationManagementUtils;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;

/**
 * @author gharley
 */
public class AspectPathBlock extends PathBlock {

    private List existingAspectPath;

    public AspectPathBlock(IStatusChangeListener context, int pageToShow) {
        super(context, pageToShow);
        fPathList.setLabelText(UIMessages.AspectPathBlock_aspectpath_label);
    }



    /*
     * @see BuildPathBasePage#getSelection
     */
    public List getSelection() {
        return fPathList.getSelectedElements();
    }

    public void init(IJavaProject jproject, IClasspathEntry[] aspectpathEntries) {
        setJavaProject(jproject);
        
        existingAspectPath = null;
        if (aspectpathEntries != null) {
            existingAspectPath = getExistingEntries(aspectpathEntries);
        }

        if (existingAspectPath == null) {
            existingAspectPath = new ArrayList();
        }

        fPathList.setElements(existingAspectPath);

        super.init();
    }




    @Override
    protected void internalSetProjectPath(List<CPListElement> pathEntries,
            StringBuffer pathBuffer, StringBuffer contentKindBuffer,
            StringBuffer entryKindBuffer) {
        AspectJCorePreferences.setProjectAspectPath(getJavaProject().getProject(),
                pathBuffer.toString(), contentKindBuffer.toString(),
                entryKindBuffer.toString());

        LaunchConfigurationManagementUtils.updateAspectPaths(getJavaProject(),
                existingAspectPath, pathEntries);
    }

    
    protected String getBlockNote() {
        return UIMessages.AspectPathBlock_note;
    }
    
    public String getBlockTitle() {
        return UIMessages.AspectPathBlock_tab_libraries;
    }

    protected String getRestrictionPathAttrName() {
//        return AspectJCorePreferences.ASPECTPATH_RESTRICTION_ATTRIBUTE_NAME;
        return "org.eclipse.ajdt.aspectpath.restriction";
    }

    protected String getPathAttributeName() {
        return AspectJCorePreferences.ASPECTPATH_ATTRIBUTE_NAME;
    }
}
