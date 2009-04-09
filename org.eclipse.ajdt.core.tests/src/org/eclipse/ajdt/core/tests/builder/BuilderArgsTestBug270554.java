/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SpringSource - initial API and implementation 
 *              Andrew Eisenberg
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.builder;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IProject;

/**
 * Tests bug 270554 that is about builder args
 * specifying extra aspect paths
 * 
 * @author andrew
 *
 */
public class BuilderArgsTestBug270554 extends AJDTCoreTestCase {
    
    public void test1BuilderArg() throws Exception {
        IProject project = createPredefinedProject("Bug270554ClasspathAugmenter1arg");
        
        // project has a builder arg aspectPath->ajar.
        // this means that ajar1.jar and ajar2.jar should be
        // promoted to the aspect path, but jar3.jar should not
        String[] aspectPath = AspectJCorePreferences.getRawProjectAspectPath(project);
        aspectPath = aspectPath[0].split(":");
        assertEquals("Should have 2 elements on the aspect path", 2, aspectPath.length);
        assertTrue("ajar1.jar should be on aspect path",
                aspectPath[0].endsWith("ajar1.jar") || aspectPath[1].endsWith("ajar1.jar"));
        assertTrue("ajar2.jar should be on aspect path",
                aspectPath[0].endsWith("ajar2.jar") || aspectPath[1].endsWith("ajar2.jar"));
    }
    public void test2BuilderArgs() throws Exception {
        IProject project = createPredefinedProject("Bug270554ClasspathAugmenter2args");
        
        // project has a builder arg aspectPath->ajar.
        // this means that ajar1.jar and ajar2.jar should be
        // promoted to the aspect path, but jar3.jar should not
        String[] aspectPath = AspectJCorePreferences.getRawProjectAspectPath(project);
        aspectPath = aspectPath[0].split(":");
        assertEquals("Should have 3 elements on the aspect path", 3, aspectPath.length);
        assertTrue("ajar1.jar should be on aspect path",
                aspectPath[0].endsWith("ajar1.jar") || aspectPath[1].endsWith("ajar1.jar") || aspectPath[2].endsWith("ajar1.jar"));
        assertTrue("ajar2.jar should be on aspect path",
                aspectPath[0].endsWith("ajar2.jar") || aspectPath[1].endsWith("ajar2.jar") || aspectPath[2].endsWith("ajar2.jar"));
        assertTrue("jar3.jar should be on aspect path",
                aspectPath[0].endsWith("jar3.jar") || aspectPath[1].endsWith("jar3.jar") || aspectPath[2].endsWith("jar3.jar"));
    }
}
