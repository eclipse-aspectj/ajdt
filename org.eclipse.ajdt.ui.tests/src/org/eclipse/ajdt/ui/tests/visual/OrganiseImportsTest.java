/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.SWT;

/**
 * Visual test case for organise imports in a .aj file
 */
public class OrganiseImportsTest extends VisualTestCase {

	
	public void testOrganiseImports() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
		assertTrue("The Bean Example project should have been created", project != null); //$NON-NLS-1$
		IFile boundPoint = (IFile)project.findMember("src/bean/BoundPoint.aj"); //$NON-NLS-1$
		assertTrue("The bean example project should contain a file called 'BoundPoint.aj'", boundPoint != null ); //$NON-NLS-1$
		openFileInDefaultEditor(boundPoint, true);
		final ICompilationUnit cUnit = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(boundPoint);
		assertTrue("BoundPoint.aj should start with two imports", cUnit.getImports().length == 2); //$NON-NLS-1$
		waitForJobsToComplete();			
		
		// Organise imports and test that the file is correct
		organiseImports(cUnit, 3);			
		
		// Add an unused import
		gotoLine(19);
		postString("import java.io.File;"); //$NON-NLS-1$
		postKey(SWT.CR);
		
		// Post Ctrl+S to save the file
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		
		// Wait for the save to be processed
		new DisplayHelper() {
			protected boolean condition() {
				try {					
					IMarker[] problemMarkers = cUnit.getResource().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
					return problemMarkers.length == 1;
				} catch (CoreException e) {
				}
				return false;
			}
		
		}.waitForCondition(display, 5000);

		// Check that there is an unused import in the file
		IMarker[] problemMarkers = cUnit.getResource().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
		assertTrue("BoundPoint.aj should have one problem", problemMarkers.length == 1); //$NON-NLS-1$
		assertTrue("BoundPoint.aj should have four imports", cUnit.getImports().length == 4); //$NON-NLS-1$
		
		organiseImports(cUnit, 3);

		// Add an import requirement - pointcut p2(): call(* File.*(..));
		gotoLine(65);
		postKey(SWT.TAB);
		postString("pointcut p2"); //$NON-NLS-1$
		postKeyDown(SWT.SHIFT);
		postString("90; "); //$NON-NLS-1$
		postKeyUp(SWT.SHIFT);
		postString("call"); //$NON-NLS-1$
		postKeyDown(SWT.SHIFT);
		postString("98 "); //$NON-NLS-1$
		postKeyUp(SWT.SHIFT);
		postString("ByteArrayInputStream."); //$NON-NLS-1$
		postKeyDown(SWT.SHIFT);
		postString("89"); //$NON-NLS-1$
		postKeyUp(SWT.SHIFT);
		postString(".."); //$NON-NLS-1$
		postKeyDown(SWT.SHIFT);
		postString("00"); //$NON-NLS-1$
		postKeyUp(SWT.SHIFT);	
		postString(";");		 //$NON-NLS-1$
		postKey(SWT.DEL); // delete the extra bracket eclipse added
		postKey(SWT.CR);			
		
		// Post Ctrl+S to save the file
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);
		
		// Wait for the save to be processed
		new DisplayHelper() {
			protected boolean condition() {
				try {					
					IMarker[] problemMarkers = cUnit.getResource().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
					return problemMarkers.length == 1;
				} catch (CoreException e) {
				}
				return false;
			}
		
		}.waitForCondition(display, 5000);
		
		organiseImports(cUnit, 4);

	}

	private void organiseImports(final ICompilationUnit cUnit, final int expectedImports) throws JavaModelException, CoreException {
		// Post Ctrl+Shift+O to organise imports
		postKeyDown(SWT.CTRL);
		postKeyDown(SWT.SHIFT);
		postKey('o');
		postKeyUp(SWT.SHIFT);
		postKeyUp(SWT.CTRL);

		// Wait for the save to be processed
		new DisplayHelper() {
			protected boolean condition() {
				try {
					return  cUnit.getImports().length == expectedImports;
				} catch (JavaModelException e) {
				}
				return false;
			}
		
		}.waitForCondition(display, 5000);
		assertTrue("BoundPoint.aj should now have " + expectedImports + " imports, has " + cUnit.getImports().length, cUnit.getImports().length == expectedImports); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Post Ctrl+S to save the file
		postKeyDown(SWT.CTRL);
		postKey('s');
		postKeyUp(SWT.CTRL);

		// Wait for the build to remove any problem markers
		new DisplayHelper() {
			protected boolean condition() {
				try {					
					IMarker[] problemMarkers = cUnit.getResource().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
					return problemMarkers.length == 0;
				} catch (CoreException e) {
				}
				return false;
			}
		
		}.waitForCondition(display, 5000);
		IMarker[] problemMarkers = cUnit.getResource().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
		assertTrue("BoundPoint.aj should not have any problems", problemMarkers.length == 0); //$NON-NLS-1$
	
	}
	
}
