/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.visual.tests;

import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.ui.tests.testutils.TestLogger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.SWT;
import org.eclipse.ui.progress.UIJob;

public class RefactoringParticipationTest extends VisualTestCase {

	private void doRename(IResource file, final String newName,
			final boolean doMain) {
		selectInPackageExplorer(file);

		// Rename type
		postKeyDown(SWT.ALT);
		postKeyDown(SWT.SHIFT);
		postKey('r');
		postKeyUp(SWT.SHIFT);
		postKeyUp(SWT.ALT);

		new UIJob("post key job") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				postString(newName); //$NON-NLS-1$
				postKey(SWT.CR); // finish
				return Status.OK_STATUS;
			}	
		}.schedule(2000);
		
		if (doMain) {
			// need to ok additional dialog about main methods
			new UIJob("post key job") { //$NON-NLS-1$
				public IStatus runInUIThread(IProgressMonitor monitor) {
					postKey(SWT.CR); // finish
					return Status.OK_STATUS;
				}	
			}.schedule(4000);			
		}
		
//		Runnable r = new Runnable() {
//			public void run() {
//				sleep();
//				postString(newName); //$NON-NLS-1$
//				postKey(SWT.CR); // finish
//				if (doMain) {
//					Runnable r2 = new Runnable() {
//						public void run() {
//							sleep();
//							postKey(SWT.CR); // ok dialog about main methods
//						}
//					};
//					new Thread(r2).start();
//				}
//			}
//		};
//		new Thread(r).start();
		waitForJobsToComplete();
	}

	public void testRenameType() throws Exception {
		IProject project = createPredefinedProject("TJP Example"); //$NON-NLS-1$

		IResource demo = project.findMember("src/tjp/Demo.java"); //$NON-NLS-1$
		assertNotNull("Could not find Demo.java", demo); //$NON-NLS-1$

		doRename(demo, "Monkey", true); //$NON-NLS-1$

		demo = project.findMember("src/tjp/Demo.java"); //$NON-NLS-1$
		assertNull("Demo.java should no longer exist", demo); //$NON-NLS-1$

		IResource monkey = project.findMember("src/tjp/Monkey.java"); //$NON-NLS-1$
		assertNotNull(
				"Demo.java should have been renamed to Monkey.java", monkey); //$NON-NLS-1$

		IResource getinfo = project.findMember("src/tjp/GetInfo.aj"); //$NON-NLS-1$
		assertNotNull("Could not find GetInfo.aj", getinfo); //$NON-NLS-1$

		// GetInfo.aj should have been updated accordingly, otherwise
		// there will be compile errors
		IMarker[] problemMarkers = getinfo.findMarkers(IMarker.PROBLEM, true,
				IResource.DEPTH_ONE);
		assertTrue(
				"GetInfo.aj should not have any problems", problemMarkers.length == 0); //$NON-NLS-1$

		// now check contents
		String contents = readFile((IFile) getinfo);
		int ind = contents.indexOf("Demo"); //$NON-NLS-1$
		assertTrue(
				"GetInfo.aj should no longer contain references to Demo", ind == -1); //$NON-NLS-1$
		ind = contents.indexOf("Monkey"); //$NON-NLS-1$
		assertTrue(
				"GetInfo.aj should now contain references to Monkey", ind != -1); //$NON-NLS-1$

		// check advice matches have updated
		AJCompilationUnit ajcu = AJCompilationUnitManager.INSTANCE
				.getAJCompilationUnit((IFile) getinfo);
		assertNotNull("Couldn't find AJCompilationUnit for GetInfo.aj", ajcu); //$NON-NLS-1$
		IType type = ((ICompilationUnit) ajcu).findPrimaryType();
		AdviceElement around = null;
		IJavaElement[] children = type.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof AdviceElement) {
				around = (AdviceElement) children[i];
			}
		}
		assertNotNull("Didn't find around advice element in GetInfo.aj", around); //$NON-NLS-1$

		waitForJobsToComplete();
		//System.out.println("getting related elements");
		List rels = AJModel.getInstance().getRelatedElements(
				AJRelationshipManager.ADVISES, around);
		assertEquals(
				"Around advice in GetInfo.aj advises wrong number of elements", 3, rels.size()); //$NON-NLS-1$
		boolean foundMain = false;
		boolean foundFoo = false;
		boolean foundBar = false;
		String expected = "Monkey.java"; //$NON-NLS-1$
		for (Iterator iter = rels.iterator(); iter.hasNext();) {
			IJavaElement el = (IJavaElement) iter.next();
			//System.out.println("el: "+el);
			String name = el.getElementName();
			String resName = el.getResource().getName();
			if (name.equals("main")) { //$NON-NLS-1$
				foundMain = true;
			} else if (name.equals("foo")) { //$NON-NLS-1$
				foundFoo = true;
			} else if (name.equals("bar")) { //$NON-NLS-1$
				foundBar = true;
			}
			assertEquals(
					"Wrong resource for advised element: " + el, expected, resName); //$NON-NLS-1$
		}
		assertTrue("Didn't find advises main relationship", foundMain); //$NON-NLS-1$
		assertTrue("Didn't find advises foo relationship", foundFoo); //$NON-NLS-1$
		assertTrue("Didn't find advises bar relationship", foundBar); //$NON-NLS-1$
	}

	public void testRenameType2() throws Exception {
		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$

		IResource point = project.findMember("src/bean/Point.java"); //$NON-NLS-1$
		assertNotNull("Could not find Point.java", point); //$NON-NLS-1$

		doRename(point, "Monkey", false); //$NON-NLS-1$

		point = project.findMember("src/bean/Point.java"); //$NON-NLS-1$
		assertNull("Point.java should no longer exist", point); //$NON-NLS-1$

		IResource monkey = project.findMember("src/bean/Monkey.java"); //$NON-NLS-1$
		assertNotNull(
				"Point.java should have been renamed to Monkey.java", monkey); //$NON-NLS-1$

		IResource boundpoint = project.findMember("src/bean/BoundPoint.aj"); //$NON-NLS-1$
		assertNotNull("Could not find BoundPoint.aj", boundpoint); //$NON-NLS-1$

		// GetInfo.aj should have been updated accordingly, otherwise
		// there will be compile errors
		IMarker[] problemMarkers = boundpoint.findMarkers(IMarker.PROBLEM,
				true, IResource.DEPTH_ONE);
		assertTrue(
				"BoundPoint.aj should not have any problems", problemMarkers.length == 0); //$NON-NLS-1$
	}
}
