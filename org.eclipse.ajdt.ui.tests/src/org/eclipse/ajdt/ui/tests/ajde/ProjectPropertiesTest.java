/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial version
 *     Helen Hawkins - updated for new ajde interface (bug 148190)
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.ajde;

import java.io.File;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.ajde.UIMessageHandler;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * 
 * 
 * @author mchapman
 */
public class ProjectPropertiesTest extends UITestCase {

	public void testBug148055() throws Exception {
		IProject project = createPredefinedProject("project.with.aop-ajc.xml.file"); //$NON-NLS-1$

		IResource xml = project.findMember("src/META-INF/aop-ajc.xml"); //$NON-NLS-1$
		assertNotNull("Couldn't find aop-ajc.xml file in project", xml); //$NON-NLS-1$
		assertTrue("aop-ajc.xml file doesn't exist: " + xml, xml.exists()); //$NON-NLS-1$
		File file = xml.getRawLocation().toFile();
		assertNotNull("Couldn't find aop-ajc.xml as a java.io.File", file); //$NON-NLS-1$
		assertTrue("aop-ajc.xml file doesn't exist: " + file, file.exists()); //$NON-NLS-1$

		boolean deleted = file.delete();
		assertTrue("Delete failed for file: " + file, deleted); //$NON-NLS-1$

		project.build(IncrementalProjectBuilder.FULL_BUILD,
				new NullProgressMonitor());
		assertTrue("Regression of bug 148055: Should be no errors, but got " //$NON-NLS-1$
				+((UIMessageHandler)AspectJPlugin.getDefault().getCompilerFactory()
        				.getCompilerForProject(project).getMessageHandler()).getErrors(),
        				((UIMessageHandler)AspectJPlugin.getDefault().getCompilerFactory()
                				.getCompilerForProject(project).getMessageHandler()).getErrors().size()==0);	
	}
}