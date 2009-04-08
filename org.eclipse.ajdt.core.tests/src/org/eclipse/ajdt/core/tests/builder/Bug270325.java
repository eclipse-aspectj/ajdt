/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *         Andrew Eisenberg - Initial implementation
 ******************************************************************************/
package org.eclipse.ajdt.core.tests.builder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.testutils.ReaderInputStream;
import org.eclipse.ajdt.core.tests.testutils.TestLogger;
import org.eclipse.ajdt.core.tests.testutils.Utils;
import org.eclipse.ajdt.internal.core.ajde.CoreCompilerConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * 
 * @author Andrew Eisenberg
 * @created Apr 8, 2009
 *
 * Tests to ensure that {@link CoreCompilerConfiguration#setClasspathElementsWithModifiedContents} 
 * does not have duplicated classpath elements
 */
public class Bug270325 extends AJDTCoreTestCase {

    class MockCoreCompilerConfiguration extends CoreCompilerConfiguration {

        List modifiedContents;
        
        public MockCoreCompilerConfiguration(IProject project) {
            super(project);
        }
        public void setClasspathElementsWithModifiedContents(
                List modifiedContents) {
            super.setClasspathElementsWithModifiedContents(modifiedContents);
            this.modifiedContents = modifiedContents;
        }
    }
    
    public void testNoDupsOnClasspath() throws Exception {
        
        Utils.setAutobuilding(false);
        
        try {
            
        } catch (Exception e) {
        } finally {
            Utils.setAutobuilding(true);
        }
    
    }
}
