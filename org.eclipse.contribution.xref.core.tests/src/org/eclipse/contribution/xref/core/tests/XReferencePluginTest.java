/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.core.tests;

import org.eclipse.contribution.xref.core.XReferencePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import junit.framework.TestCase;

/**
 * @author hawkinsh
 *
 */
public class XReferencePluginTest extends TestCase {

	public void testGetDefault() {
		XReferencePlugin xrp = XReferencePlugin.getDefault();
		assertNotNull(xrp);
		XReferencePlugin xrp2 = XReferencePlugin.getDefault();
		assertEquals(xrp,xrp2);
	}

	public void testPluginID() {
		assertEquals("org.eclipse.contribution.xref.core",XReferencePlugin.PLUGIN_ID); //$NON-NLS-1$
	}
		
	public void testLog() {
		LogListener l = new LogListener();
		try {
			XReferencePlugin.getDefault().getLog().addLogListener(l);
			assertFalse(l.hasLogged);
			IStatus status = new Status(IStatus.ERROR,"org.eclipse.contribution.xref.core.tests",1, //$NON-NLS-1$
						"Testcase generated exception as expected",new Throwable()); //$NON-NLS-1$
			XReferencePlugin.log(new CoreException(status));
			assertTrue(l.hasLogged);
		} finally {
			XReferencePlugin.getDefault().getLog().removeLogListener(l);
		}
	}
	
	static class LogListener implements ILogListener {

		public boolean hasLogged = false;
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.ILogListener#logging(org.eclipse.core.runtime.IStatus, java.lang.String)
		 */
		public void logging(IStatus status, String plugin) {
			hasLogged = true;
			
		}
		
	}
	
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
