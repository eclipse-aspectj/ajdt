/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.core.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author hawkinsh
 *
 */
public class AJProjectModelTest extends TestCase {

	IProject project;
	AJCodeElement[] ajCodeElements;
	AJProjectModel projectModel;
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = Utils.createPredefinedProject("AJProject83082");
		projectModel = new AJProjectModel(project);
		projectModel.createProjectMap();
		
		AJModel model = AJModel.getInstance();
		model.createMap(project);

		IFolder src = project.getFolder("src");
		IFolder com = src.getFolder("com");
		IFolder ibm = com.getFolder("ibm");
		IFolder wpstest = ibm.getFolder("wpstest");
		IFolder aspectjPackage = wpstest.getFolder("aspectj");
		IFile main = aspectjPackage.getFile("Main.java");
		Map annotationsMap = AsmManager.getDefault().getInlineAnnotations(main.getRawLocation().toOSString(),true, true);
		ajCodeElements = createAJCodeElements(model,annotationsMap);  
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        Utils.deleteProject(project);
    }

    public void testIsAdvised() {
        IJavaElement parent = ajCodeElements[0].getParent();
        assertFalse("Parent shouldn't be an AJCodeElement", (parent instanceof AJCodeElement));
        assertTrue("parent is advised because subelement is advised",projectModel.isAdvised(parent));
    }

    public void testGetExtraChildren() {
        IJavaElement parent = ajCodeElements[0].getParent();
        List extraChildren = projectModel.getExtraChildren(parent);
        assertTrue("parent should have two children",extraChildren.size() == 2);
        for (Iterator iter = extraChildren.iterator(); iter.hasNext();) {
            IJavaElement element = (IJavaElement) iter.next();
            assertTrue("child should be an AJCodeElement",(element instanceof AJCodeElement));
            AJCodeElement aj = (AJCodeElement)element;
            assertEquals("the name should be method-call(void java.io.PrintStream.println(java.lang.String))","method-call(void java.io.PrintStream.println(java.lang.String))" ,aj.getName());
            assertTrue("the line number should be 18 or 19",aj.getLine() == 18 || aj.getLine() == 19);
        }
        assertNull("child should have no children",projectModel.getExtraChildren((IJavaElement)extraChildren.get(0)));
        assertNull("child should have no children",projectModel.getExtraChildren((IJavaElement)extraChildren.get(1)));        
    }

	private AJCodeElement[] createAJCodeElements(AJModel model, Map annotationsMap) {
		AJCodeElement[] arrayOfajce = new AJCodeElement[2];
		Set keys = annotationsMap.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			Object key = it.next();
			List annotations = (List) annotationsMap.get(key);
			for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
				IProgramElement node = (IProgramElement) it2.next();
				ISourceLocation sl = node.getSourceLocation();
				if (node.toLinkLabelString()
						.equals("Main: method-call(void java.io.PrintStream.println(java.lang.String))") 
					&& (sl.getLine() == 18) ){
					
					IJavaElement ije = model.getCorrespondingJavaElement(node);
					if (ije instanceof AJCodeElement) {
						arrayOfajce[0] = (AJCodeElement) ije;
					}					
				} else if (node.toLinkLabelString()
						.equals("Main: method-call(void java.io.PrintStream.println(java.lang.String))") 
					&& (sl.getLine() == 19) ){
					
					IJavaElement ije = model.getCorrespondingJavaElement(node);
					if (ije instanceof AJCodeElement) {
						arrayOfajce[1] = (AJCodeElement) ije;
					}					
				}
			}
		}				
		return arrayOfajce;
	}
}
