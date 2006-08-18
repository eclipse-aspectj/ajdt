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

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;

public class ChangeFileExtensionTest extends VisualTestCase {

	public void testRenameSingleFiles() throws Exception {
		IProject project = createPredefinedProject("Spacewar Example"); //$NON-NLS-1$

		// convert .java to .aj
		IResource player = project.findMember("src/spacewar/Player.java"); //$NON-NLS-1$
		assertNotNull("Could not find Player.java", player); //$NON-NLS-1$

		selectInPackageExplorer(player);

		// bring up context menu
		postKeyDown(SWT.SHIFT);
		postKey(SWT.F10);
		postKeyUp(SWT.SHIFT);

		sleep();

		postKey('j'); // AspectJ Tools
		postKey('a'); // Convert to .aj

		waitForJobsToComplete();

		IResource ajplayer = project.findMember("src/spacewar/Player.aj"); //$NON-NLS-1$
		assertNotNull("Did not convert Player.java to Player.aj", ajplayer); //$NON-NLS-1$

		player = project.findMember("src/spacewar/Player.java"); //$NON-NLS-1$
		assertNull("Should not have still found Player.java", player); //$NON-NLS-1$

		// now convert .aj to .java
		IResource ship = project.findMember("src/spacewar/Ship.aj"); //$NON-NLS-1$
		assertNotNull("Could not find Ship.aj", ship); //$NON-NLS-1$
		AJCompilationUnit ajcu = AJCompilationUnitManager.INSTANCE
				.getAJCompilationUnit((IFile) ship);
		selectInPackageExplorer(ajcu);

		// bring up context menu
		postKeyDown(SWT.SHIFT);
		postKey(SWT.F10);
		postKeyUp(SWT.SHIFT);

		sleep();

		postKey('j'); // AspectJ Tools
		postKey('j'); // Convert to .java

		waitForJobsToComplete();

		IResource javaship = project.findMember("src/spacewar/Ship.java"); //$NON-NLS-1$
		assertNotNull("Did not convert Ship.aj to Ship.java", javaship); //$NON-NLS-1$

		ship = project.findMember("src/spacewar/Ship.aj"); //$NON-NLS-1$
		assertNull("Should not have still found Ship.aj", ship); //$NON-NLS-1$
	}

	public void testRenameMultipleFiles() throws Exception {
		IProject project = createPredefinedProject("Spacewar Example"); //$NON-NLS-1$

		// convert two .java files to .aj
		IResource spaceobject = project
				.findMember("src/spacewar/SpaceObject.java"); //$NON-NLS-1$
		assertNotNull("Could not find SpaceObject.java", spaceobject); //$NON-NLS-1$
		selectInPackageExplorer(spaceobject);
		
		// multi-select following file (SWFrame.java)
		postKeyDown(SWT.SHIFT);
		postKey(SWT.ARROW_DOWN);
		postKeyUp(SWT.SHIFT);

		// bring up context menu
		postKeyDown(SWT.SHIFT);
		postKey(SWT.F10);
		postKeyUp(SWT.SHIFT);

		sleep();

		postKey('j'); // AspectJ Tools
		postKey('a'); // Convert to .aj

		waitForJobsToComplete();

		IResource ajspaceobject = project
				.findMember("src/spacewar/SpaceObject.aj"); //$NON-NLS-1$
		assertNotNull(
				"Did not convert SpaceObject.java to SpaceObject.aj", ajspaceobject); //$NON-NLS-1$

		spaceobject = project.findMember("src/spacewar/SpaceObject.java"); //$NON-NLS-1$
		assertNull("Should not have still found SpaceObject.java", spaceobject); //$NON-NLS-1$

		IResource ajswframe = project.findMember("src/spacewar/SWFrame.aj"); //$NON-NLS-1$
		assertNotNull("Did not convert SWFrame.java to SWFrame.aj", ajswframe); //$NON-NLS-1$

		IResource swframe = project.findMember("src/spacewar/SWFrame.java"); //$NON-NLS-1$
		assertNull("Should not have still found SWFrame.java", swframe); //$NON-NLS-1$

		// now convert .aj to .java
		IResource debug = project.findMember("src/spacewar/Debug.aj"); //$NON-NLS-1$
		assertNotNull("Could not find Debug.aj", debug); //$NON-NLS-1$
		AJCompilationUnit ajcu = AJCompilationUnitManager.INSTANCE
				.getAJCompilationUnit((IFile) debug);
		selectInPackageExplorer(ajcu);

		// multi-select following file (Display.aj)
		postKeyDown(SWT.SHIFT);
		postKey(SWT.ARROW_DOWN);
		postKeyUp(SWT.SHIFT);

		// bring up context menu
		postKeyDown(SWT.SHIFT);
		postKey(SWT.F10);
		postKeyUp(SWT.SHIFT);

		sleep();

		postKey('j'); // AspectJ Tools
		postKey('j'); // Convert to .java

		waitForJobsToComplete();

		IResource javadebug = project.findMember("src/spacewar/Debug.java"); //$NON-NLS-1$
		assertNotNull("Did not convert Debug.aj to Debug.java", javadebug); //$NON-NLS-1$

		debug = project.findMember("src/spacewar/Debug.aj"); //$NON-NLS-1$
		assertNull("Should not have still found Debug.aj", debug); //$NON-NLS-1$

		IResource javadisplay = project.findMember("src/spacewar/Display.java"); //$NON-NLS-1$
		assertNotNull("Did not convert Display.aj to Display.java", javadisplay); //$NON-NLS-1$

		IResource display = project.findMember("src/spacewar/Display.aj"); //$NON-NLS-1$
		assertNull("Should not have still found Display.aj", display); //$NON-NLS-1$		
	}
}
