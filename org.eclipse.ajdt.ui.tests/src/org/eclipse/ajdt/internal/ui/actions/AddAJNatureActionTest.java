/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     George Harley - initial version
 * 	   Helen Hawkins - converting for use with AJDT 1.1.11 codebase  
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.actions;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.test.utils.JavaTestProject;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;


/**
 * @author gharley
 */
public class AddAJNatureActionTest extends TestCase {

    // Lets create a new Java project, check its nature and then
    // convert it to an AspectJ project. Another check of its nature should
    // reveal it to be AspectJ.

    private JavaTestProject testProject = null;
    private IType helloType;
    private IFile goodbyeAJFile;

    protected void setUp() throws Exception {
        super.setUp();

        // create a Java project that contains all the various Java elements...
        // create a project
        testProject = new JavaTestProject("Test Java Project");

        // create a package
        IPackageFragment testPackage = testProject.createPackage("mypackage");

        // add a couple of Java files in the package
        helloType =
            testProject.createType(
                testPackage,
                "Hello.java",
                "public class Hello {\n"
                    + "  public static void main(String[] args) {\n"
                    + "    System.out.println(\"Hello\");\n"
                    + "  }\n"
                    + "}");

        goodbyeAJFile =
            testProject.createFile(
                (IFolder) helloType
                    .getPackageFragment()
                    .getUnderlyingResource(),
                "Goodbye.aj",
                "public class Goodbye {\n"
                    + "  public Goodbye() {};\n"
                    + "  public static void main(String[] args) {\n"
                    + "    System.out.println(message);\n"
                    + "  }\n"
                    + "  public String message = \"Goodbye\";\n"
                    + "}");
    }

    /*
	 * @see TestCase#tearDown()
	 */
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            testProject.dispose();        	
        } catch (CoreException e) {
        	// don't care about the exception here.....
        }
    }

    public void testAddsAJNature() throws CoreException {
        // Ensure that we are starting with a Java project.
		AspectJPreferences.setAJDTPrefConfigDone(true);
        IProject proj = testProject.getProject();
        assertTrue(proj.hasNature("org.eclipse.jdt.core.javanature"));
        
        // GCH Put us into the Java perspective and arrange for the pop up 
        // to appear. How do I close it ???
        
        
        // Attempt to add the AspectJ nature to it.

        // First, ensure that we avoid getting prompted about
        // perspective switching when AspectJ projects come into being.
//        boolean originalAskVal =
//            AspectJPreferences.askAspectJPerspectiveSwitch();
//        AspectJPreferences.setAskAspectJPerspectiveSwitch(false);

        // Next, create the necessary arguments for the nature addition.
        ISelection sel = new StructuredSelection(testProject.getProject());
        IAction action = new Action() {
            public void run() {
                    // NO OP
            }
        };
        AddAJNatureAction aja = new AddAJNatureAction();
        aja.selectionChanged(action, sel);
        
        // Attempt to add the nature
        aja.run(action);
        assertTrue(proj.hasNature(AspectJUIPlugin.ID_NATURE));
		AspectJPreferences.setAJDTPrefConfigDone(false);

        // Restore altered preference value.
        //AspectJPreferences.setAskAspectJPerspectiveSwitch(originalAskVal);
    }
}
