/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.core.tests;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Helen Hawkins
 * Bug 115828
 */
public class ProjectDeletionTest extends AJDTCoreTestCase {

    public void testProjectDeletion() throws Exception {
        
        for (int i = 0; i < 3; i++) {
            System.out.println(">>>>>>>>>>>>>>>>> count = " + i); //$NON-NLS-1$
    		IProject libProject = createPredefinedProject14("MyAspectLibrary"); //$NON-NLS-1$
    		IProject weaveMeProject = createPredefinedProject("WeaveMe"); //$NON-NLS-1$
    		try {
    		    deleteProject(weaveMeProject, true);
    			deleteProject(libProject, true);
    		} catch (CoreException ce) {
    		    fail("failed to delete project"); //$NON-NLS-1$
    		}
        }
        
    }
}
