/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.internal.builder;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;

/**
 * Tests for advice markers
 * 
 * @author Matt Chapman
 */
public class AdviceMarkersTest2 extends TestCase {

	private IProject project;

	// array of marker types to test for
	private static String[] markerTypes = { IAJModelMarker.ADVICE_MARKER,
			IAJModelMarker.BEFORE_ADVICE_MARKER,
			IAJModelMarker.AFTER_ADVICE_MARKER,
			IAJModelMarker.AROUND_ADVICE_MARKER,
			IAJModelMarker.DYNAMIC_ADVICE_MARKER,
			IAJModelMarker.DYNAMIC_BEFORE_ADVICE_MARKER,
			IAJModelMarker.DYNAMIC_AFTER_ADVICE_MARKER,
			IAJModelMarker.DYNAMIC_AROUND_ADVICE_MARKER,
			IAJModelMarker.DECLARATION_MARKER, IAJModelMarker.ITD_MARKER };

	// expected results:
	//   first index of array is the marker type, as above.
	//   second index is expected marker label
	//   third index is expected line number of that marker
	private static Object[][][] results = {
			{ // IAJModelMarker.ADVICE_MARKER
					{ "GetInfo.before(): <anonymous pointcut>", new Integer(36) },
					{ "GetInfo.before(): demoExecs..", new Integer(21) },
					{ "GetInfo.around(): demoExecs().. (runtime test)",
							new Integer(21) },
					{ "GetInfo.after(): fieldSet..", new Integer(28) },
					{ "GetInfo.before(): demoExecs..", new Integer(25) },
					{ "GetInfo.before(): demoExecs..", new Integer(32) },
					{ "GetInfo.around(): demoExecs().. (runtime test)",
							new Integer(32) },
					{ "GetInfo.before(): <anonymous pointcut>.. (runtime test)",
							new Integer(32) },
					{ "GetInfo.before(): demoExecs..", new Integer(41) },
					{ "GetInfo.around(): demoExecs().. (runtime test)",
							new Integer(41) } },
			{ // IAJModelMarker.BEFORE_ADVICE_MARKER
			{ "GetInfo.before(): <anonymous pointcut>", new Integer(36) },
					{ "GetInfo.before(): demoExecs..", new Integer(25) } },
			{ // IAJModelMarker.AFTER_ADVICE_MARKER
			    { "GetInfo.after(): fieldSet..", new Integer(28) }
			},
			{ // IAJModelMarker.AROUND_ADVICE_MARKER
				{}
			},
			{ // IAJModelMarker.DYNAMIC_ADVICE_MARKER
					{ "GetInfo.before(): demoExecs..", new Integer(21) },
					{ "GetInfo.around(): demoExecs().. (runtime test)",
							new Integer(21) },
					{ "GetInfo.before(): demoExecs..", new Integer(32) },
					{ "GetInfo.around(): demoExecs().. (runtime test)",
							new Integer(32) },
					{ "GetInfo.before(): <anonymous pointcut>.. (runtime test)",
							new Integer(32) },
					{ "GetInfo.before(): demoExecs..", new Integer(41) },
					{ "GetInfo.around(): demoExecs().. (runtime test)",
							new Integer(41) }, },
			{ // IAJModelMarker.DYNAMIC_BEFORE_ADVICE_MARKER
			    {}
			},
			{ // IAJModelMarker.DYNAMIC_AFTER_ADVICE_MARKER
				{}
			},
			{ // IAJModelMarker.DYNAMIC_AROUND_ADVICE_MARKER
				{}
			},
			{ // IAJModelMarker.DECLARATION_MARKER
				{ "Demo.itd(int)", new Integer(17) },
				{ "Demo.f", new Integer(17) }
			},
			{ // IAJModelMarker.ITD_MARKER
				{ "Demo.itd(int)", new Integer(17) },
				{ "Demo.f", new Integer(17) }
			}
		};

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = Utils.getPredefinedProject("MarkersTest", true);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testMarkers() throws Exception {
		String filename = "src/tjp/Demo.java";
		IResource file = project.findMember(filename);
		if (file == null)
			fail("Required file not found: " + filename);

		for (int i = 0; i < markerTypes.length; i++) {
			IMarker[] markers = file.findMarkers(markerTypes[i], true,
					IResource.DEPTH_INFINITE);
			//System.out.println("type=" + markerTypes[i]);
			
			// create lists of messages and lines we need to search for
			List tofindMsg = new ArrayList();
			List tofindLine = new ArrayList();
			for (int j = 0; j < results[i].length; j++) {
				if (results[i][j].length == 2) {
					String msg = (String) results[i][j][0];
					tofindMsg.add(msg.intern());
					tofindLine.add(results[i][j][1]);
				}
			}
			
			for (int j = 0; j < markers.length; j++) {
				IMarker m = markers[j];
				int line = m.getAttribute(IMarker.LINE_NUMBER, -1);
				String msg = m.getAttribute(IMarker.MESSAGE, "").intern();
				//System.out.println("msg=" + msg + " line=" + line);
				if (tofindMsg.contains(msg)) {
					// search for matching message and line number
					//  - could be >1 lines with the same message
					//  - we cannot rely on the order of markers
					boolean found = false;
					int foundIndex = 0;
					for (int k = 0; !found && (k < tofindMsg.size()); k++) {
						String msg1 = (String)tofindMsg.get(k);
						if (msg1 == msg) {
							Integer expLine = (Integer)tofindLine.get(k);
							if (expLine.intValue() == line) {
								found = true;
								foundIndex = k;
							}
						}
					}
					if (!found) {
						fail("Expected marker message found, but found at wrong line number: "
								+ line + " Message="+msg);
					}
					// successfully found message and line so remove from lists
					tofindMsg.remove(foundIndex);
					tofindLine.remove(foundIndex);
				} else {
					fail("Found unexpected marker of type " + markerTypes[i]
					    + " with message: " + msg);
				}
			}
			// check that we found everything we were looking for
			if (tofindMsg.size() > 0) {
				String missing = "";
				for (int j = 0; j < tofindMsg.size(); j++) {
					missing += System.getProperty("line.separator");
					missing += (String)tofindMsg.get(j);					
				}
				fail("Did not find all expected markers of type "
						+ markerTypes[i] + ". Missing: " + missing);
			}
		}
	}

}