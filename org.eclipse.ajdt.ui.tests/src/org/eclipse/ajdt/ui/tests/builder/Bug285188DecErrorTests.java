/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *               Helen Hawkins   - updated for new ajde interface (bug 148190)
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Tests bug 285188
 * That declare error will work when it targets more than one project
 * @author Andrew Eisenberg
 * @created Aug 12, 2009
 *
 */
public class Bug285188DecErrorTests extends UITestCase {
    
    public void testDeclareErrorOnMultipleProjects() throws Exception {
        /* IProject aspectProj = */createPredefinedProject("Bug285188");
        IProject javaProjB = createPredefinedProject("Bug285188b");
        IProject javaProjC = createPredefinedProject("Bug285188c");
        waitForJobsToComplete();
        
        IFile class1 = javaProjB.getFile("src/Class.java");
        testMarker(class1);
        IFile class2 = javaProjC.getFile("src/Class2.java");
        testMarker(class2);
        
    }

    private void testMarker(IFile clazz) throws CoreException {
        IMarker[] markers = clazz.findMarkers(IAJModelMarker.AJDT_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        assertEquals("Expecting to find one marker on " + clazz.getFullPath(), 1, markers.length);
        
        int line = ((Integer) markers[0].getAttribute(IMarker.LINE_NUMBER)).intValue();
        assertEquals("Expecting maker to be on line 5", 5, line);
        
        String loc = (String) markers[0].getAttribute(AspectJUIPlugin.RELATED_LOCATIONS_ATTRIBUTE_PREFIX + 0);
        assertTrue("Expecting location string to start with 'Bug285188/src/Aspect.aj:::3:::3:::0' but instead is '" + loc + "'", loc.endsWith("Bug285188/src/Aspect.aj:::3:::3:::0"));
    }

}
