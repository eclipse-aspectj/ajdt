/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.ajdt.ui.tests;

import java.io.StringReader;
import java.net.URI;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * 
 * @author Andrew Eisenberg
 * @created Nov 19, 2012
 */
public class FindFilesForLocationURITests extends UITestCase {
    class StringInputStream extends ReaderInputStream {

        public StringInputStream(String s) {
            super(new StringReader(s));
        }

    }
    
    private IFile file;
    private URI uri;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final IProject project = getRoot().getProject("Proj");
        if (! project.exists()) {
            project.create(null);
            project.open(null);
        }       
        file = project.getFile("foo.txt");
        if (!file.exists()) {
            file.create(new StringInputStream("foo"), true, null);
        }
        uri = file.getLocation().toFile().toURI();
    }
    private IWorkspaceRoot getRoot() {
        return ResourcesPlugin.getWorkspace().getRoot();
    }
    public void testFindFilesForLocationURI() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            getRoot().findFilesForLocationURI(uri);
        }
        long end = System.currentTimeMillis();
        System.out.println("testFindFilesForLocationURI took " + (end - start) );
    }
    public void testGetFileForLocation() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            getRoot().getFileForLocation(file.getLocation());
        }
        long end = System.currentTimeMillis();
        System.out.println("testGetFileForLocation took " + (end - start) );
    }
}
