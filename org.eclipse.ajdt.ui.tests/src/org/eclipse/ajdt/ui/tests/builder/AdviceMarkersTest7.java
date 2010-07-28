/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Andrew Eisenberg - Initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;


/**
 * Tests for markers contributed by aspects on aspect paths
 * 
 * Test that when advice is added, the marker appears and when advice
 * is removed, the markers disappear.
 * 
 * Note that we cannot put markers on aspects on the aspect path, so
 * these files should have no markers
 */
public class AdviceMarkersTest7 extends UITestCase {
    private IProject project;
    private IProject projectOnAspectPath;
    private IFile javaFile;
    private IFile ajFile;
    private IFile aspectPathFile;
    
    protected void setUp() throws Exception {
        super.setUp();
        projectOnAspectPath = createPredefinedProject("MarkerTestingAspectPath");
        project = createPredefinedProject("MarkerTestingProject");
        
        javaFile = project.getFile("src/a/TargetClass.java");
        ajFile = project.getFile("src/a/AdvisesFromProject.aj");
        aspectPathFile = projectOnAspectPath.getFile("src/b/AdvisesFromAspectPath.aj");
    }
    
    /**
     * test that a change in aspect path aspect will affect the markers properly
     * @throws Exception
     */
    public void testChangeOnAspectPath() throws Exception {
        checkAllMarkersExist();
        changeAspectOnAspectPath();
        checkOnlyProjectMarkersExist();
        unChangeAspectOnAspectPath();
        checkAllMarkersExist();
    }

    /**
     * test that a change in of an aspect in the project will affect the markers properly
     * 
     * note that this aspect does not advise the class directly, but rather implements an 
     * abstract aspect on the aspect path.  The advice marker in the java class has a relationship
     * to the advice declared in the aspect path paroject
     * @throws Exception
     */
    public void testChangeInProject() throws Exception {
        checkAllMarkersExist();
        changeAspectInProject();
        checkOnlyAspectPathMarkersExist();
        unChangeAspectInProject();
        checkAllMarkersExist();
    }

    
    
    private void checkAllMarkersExist() throws Exception {
        waitForJobsToComplete();
        IMarker[] javaMarkers = javaFile.findMarkers(IAJModelMarker.ADVICE_MARKER, true, 0);
        assertEquals("markers on java file in project should be 2", 2, javaMarkers.length);
        int first = ((Integer) javaMarkers[0].getAttribute(IMarker.LINE_NUMBER)).intValue();
        int second = ((Integer) javaMarkers[1].getAttribute(IMarker.LINE_NUMBER)).intValue();
        assertTrue("marker in java file should have a marker on line 4", 
                first == 4 || second == 4);
        assertTrue("marker in java file should have a marker on line 4", 
                first == 7 || second == 7);

        // shouldn't have any markers here because this aspect contains only a pointcut
        IMarker[] ajMarkers = ajFile.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true, 0);
        assertEquals("markers on aspect in project should be 0", 0, ajMarkers.length);

        IMarker[] aspectPathMarkers = aspectPathFile.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true, 0);
        assertEquals("markers on aspect on aspect path should be 0", 0, aspectPathMarkers.length);
    }
    
    private void checkOnlyAspectPathMarkersExist() throws Exception {
        waitForJobsToComplete();
        IMarker[] javaMarkers = javaFile.findMarkers(IAJModelMarker.ADVICE_MARKER, true, 0);
        assertEquals("markers on java file in project should be 1", 1, javaMarkers.length);
        assertEquals("marker in aspect should be on line 4", new Integer(4), javaMarkers[0].getAttribute(IMarker.LINE_NUMBER));

        IMarker[] ajMarkers = ajFile.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true, 0);
        assertEquals("markers on aspect in project should be 0", 0, ajMarkers.length);

        IMarker[] aspectPathMarkers = aspectPathFile.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true, 0);
        assertEquals("markers on aspect on aspect path should be 0", 0, aspectPathMarkers.length);
    }

    private void checkOnlyProjectMarkersExist() throws Exception {
        waitForJobsToComplete();
        IMarker[] javaMarkers = javaFile.findMarkers(IAJModelMarker.ADVICE_MARKER, true, 0);
        assertEquals("markers on java file in project should be 1", 1, javaMarkers.length);
        assertEquals("marker in aspect should be on line 7", new Integer(7), javaMarkers[0].getAttribute(IMarker.LINE_NUMBER));

        // shouldn't have any markers here because this aspect contains only a pointcut
        IMarker[] ajMarkers = ajFile.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true, 0);
        assertEquals("markers on aspect in project should be 0", 0, ajMarkers.length);

        IMarker[] aspectPathMarkers = aspectPathFile.findMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER, true, 0);
        assertEquals("markers on aspect on aspect path should be 0", 0, aspectPathMarkers.length);
    }
    
    // add a misspelling in the pointcut
    private void changeAspectOnAspectPath() throws Exception {
        String newContents = "package b;\n" + 
        "public abstract aspect AdvisesFromAspectPath {\n" + 
        "public abstract pointcut amain();\n" + 
        "before() : amain() {\n" + 
        "System.out.println(\"Advised from aspect path!\");\n" + 
        "}\n" + 
        "before() : execution(public static void a.TargetClass.maiiiiiiiiiiiiiiiiin(String[])) {\n" + 
        "System.out.println(\"Advised from project!\");\n" + 
        "}\n" + 
        "}";
        StringReader reader = new StringReader(newContents);
        aspectPathFile.setContents(new ReaderInputStream(reader), true, true, null);
        waitForJobsToComplete();
    }
    
    // back to original contents
    private void unChangeAspectOnAspectPath() throws Exception {
        String newContents = "package b;\n" + 
            "public abstract aspect AdvisesFromAspectPath {\n" + 
            "public abstract pointcut amain();\n" + 
            "before() : amain() {\n" + 
            "System.out.println(\"Advised from aspect path!\");\n" + 
            "}\n" + 
            "before() : execution(public static void a.TargetClass.main(String[])) {\n" + 
            "System.out.println(\"Advised from project!\");\n" + 
            "}\n" + 
            "}";
        StringReader reader = new StringReader(newContents);
        aspectPathFile.setContents(new ReaderInputStream(reader), true, true, null);
        waitForJobsToComplete();
    }
    
    // add misspelling in pointcut
    private void changeAspectInProject() throws Exception {
        String newContents = "package a;\n" +
        "import b.AdvisesFromAspectPath;\n" +
        "public aspect AdvisesFromProject extends AdvisesFromAspectPath {\n" +
        "public pointcut amain() : execution(public static void a.TargetClass.maiiiiiiiiiiiiiiiiin2(String[]));\n" +
        "}";
        StringReader reader = new StringReader(newContents);
        ajFile.setContents(new ReaderInputStream(reader), true, true, null);
        waitForJobsToComplete();
    }

    // back to original contents
    private void unChangeAspectInProject() throws Exception {
        String newContents = "package a;\n" +
        "import b.AdvisesFromAspectPath;\n" +
        "public aspect AdvisesFromProject extends AdvisesFromAspectPath {\n" +
        "public pointcut amain() : execution(public static void a.TargetClass.main2(String[]));\n" +
        "}";
        StringReader reader = new StringReader(newContents);
        ajFile.setContents(new ReaderInputStream(reader), true, true, null);
        waitForJobsToComplete();
    }
}

class ReaderInputStream extends InputStream {

    private Reader reader;
    
    public ReaderInputStream(Reader reader){
        this.reader = reader;
    }
    
    /* (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        return reader.read();
    }

    
    public void close() throws IOException {
        reader.close();
    } 
    
}
