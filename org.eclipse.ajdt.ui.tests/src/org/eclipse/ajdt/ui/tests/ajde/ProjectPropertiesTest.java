/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.ajde;

import java.io.File;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ErrorHandler;
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

		ErrorHandler eh = Ajde.getDefault().getErrorHandler();
		TestErrorHandler teh = new TestErrorHandler();
		Ajde.getDefault().setErrorHandler(teh);
		project.build(IncrementalProjectBuilder.FULL_BUILD,
				new NullProgressMonitor());
		assertFalse(
				"Regression of bug 148055. The compiler threw an error with message: " //$NON-NLS-1$
						+ teh.getLastMessage(), teh.errorOccurred());
		Ajde.getDefault().setErrorHandler(eh);
	}

	private class TestErrorHandler implements ErrorHandler {

		private boolean gotError = false;

		private String lastMessage;

		public void handleError(String message) {
			lastMessage = message;
			gotError = true;
		}

		public void handleError(String message, Throwable t) {
			String newline = System.getProperty("line.separator"); //$NON-NLS-1$
			StringBuffer sb = new StringBuffer();
			if (t != null) {
				StackTraceElement[] ste = t.getStackTrace();
				sb.append(t.getClass().getName());
				sb.append(newline);
				for (int i = 0; i < ste.length; i++) {
					sb.append("at "); //$NON-NLS-1$
					sb.append(ste[i].toString());
					sb.append(newline);
				}
			}
			lastMessage = message + newline + sb.toString();	
			gotError = true;
		}

		public void handleWarning(String message) {
		}

		public boolean errorOccurred() {
			return gotError;
		}

		public String getLastMessage() {
			return lastMessage;
		}
	}
}