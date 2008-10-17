/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Tests for source advice markers with aspects in .java files
 * Duplicate of AdviceMarkersTest3, except that the project used contains
 * an aspect in a .java file.
 */
public class AdviceMarkersTest5 extends UITestCase {

	private IProject project;

	// array of marker types to test for
	private static String[] markerTypes = { 
		IAJModelMarker.SOURCE_ADVICE_MARKER,
		IAJModelMarker.SOURCE_BEFORE_ADVICE_MARKER,
		IAJModelMarker.SOURCE_AFTER_ADVICE_MARKER,
		IAJModelMarker.SOURCE_AROUND_ADVICE_MARKER,
		IAJModelMarker.SOURCE_DYNAMIC_ADVICE_MARKER,
		IAJModelMarker.SOURCE_DYNAMIC_BEFORE_ADVICE_MARKER,
		IAJModelMarker.SOURCE_DYNAMIC_AFTER_ADVICE_MARKER,
		IAJModelMarker.SOURCE_DYNAMIC_AROUND_ADVICE_MARKER,
		IAJModelMarker.SOURCE_ITD_MARKER
	};

	// expected results:
	//   first index of array is the marker type, as above.
	//   second index is expected marker label
	//   third index is expected line number of that marker
	private static Object[][][] results = {
			{ // IAJModelMarker.SOURCE_ADVICE_MARKER
					// Temporary - declare soft should have a different type of marker...
					{ "advises Demo: exception-handler(void tjp.Demo.<catch>(tjp.DemoException))", new Integer(39)}, //$NON-NLS-1$
					{ "4 AspectJ markers at this line", new Integer(49) }, //$NON-NLS-1$
					{ "advises Demo.foo(int,Object) (runtime test)", new Integer(53) }, //$NON-NLS-1$
					{ "advises Demo: field-set(int tjp.Demo.x)", new Integer(57) }, //$NON-NLS-1$
					{ "3 AspectJ markers at this line", new Integer(61) }, //$NON-NLS-1$
					{ "advises GetInfo.printParameters(JoinPoint)", new Integer(74) } //$NON-NLS-1$
			},
			{ // IAJModelMarker.SOURCE_BEFORE_ADVICE_MARKER
					{ "advises Demo: exception-handler(void tjp.Demo.<catch>(tjp.DemoException))", new Integer(39)}, //$NON-NLS-1$
					{ "4 AspectJ markers at this line", new Integer(49) } //$NON-NLS-1$
			},
			{ // IAJModelMarker.SOURCE_AFTER_ADVICE_MARKER
			    	{ "advises Demo: field-set(int tjp.Demo.x)", new Integer(57) }, //$NON-NLS-1$
			    	{ "advises GetInfo.printParameters(JoinPoint)", new Integer(74) } //$NON-NLS-1$
			},
			{ // IAJModelMarker.SOURCE_AROUND_ADVICE_MARKER
				{}
			},
			{ // IAJModelMarker.SOURCE_DYNAMIC_ADVICE_MARKER
				{}
			},
			{ // IAJModelMarker.SOURCE_DYNAMIC_BEFORE_ADVICE_MARKER
				{ "advises Demo.foo(int,Object) (runtime test)", new Integer(53) } //$NON-NLS-1$
			},
			{ // IAJModelMarker.SOURCE_DYNAMIC_AFTER_ADVICE_MARKER
				{}
			},
			{ // IAJModelMarker.SOURCE_DYNAMIC_AROUND_ADVICE_MARKER
				{ "3 AspectJ markers at this line", new Integer(61) } //$NON-NLS-1$
			},
			{ // IAJModelMarker.SOURCE_ITD_MARKER
				{ "declared on Demo", new Integer(25)}, //$NON-NLS-1$
				{ "softens Demo.go()", new Integer(27)}, //$NON-NLS-1$
				{ "declared on Demo", new Integer(29)}, //$NON-NLS-1$
				{ "declared on Demo", new Integer(33)}, //$NON-NLS-1$
				{ "matched by Demo: field-set(int tjp.Demo.x)", new Integer(24)} //$NON-NLS-1$ 
			}
		};

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("MarkersTestWithAspectsInJavaFiles"); //$NON-NLS-1$
	}

	public void testMarkers() throws Exception {
		String filename = "src/tjp/GetInfo.java"; //$NON-NLS-1$
		IResource file = project.findMember(filename);
		if (file == null)
			fail("Required file not found: " + filename); //$NON-NLS-1$

		IMarker[] allMarkers = file.findMarkers(IMarker.MARKER, true, IResource.DEPTH_INFINITE);
		for (int x = 0; x < allMarkers.length; x++) {
			IMarker marker = allMarkers[x];
			System.out.println(marker.getType());
		}
		for (int i = 0; i < markerTypes.length; i++) {
			IMarker[] markers = file.findMarkers(markerTypes[i], true,
					IResource.DEPTH_INFINITE);
			
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
				String msg = m.getAttribute(IMarker.MESSAGE, "").intern(); //$NON-NLS-1$
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
						fail("Expected marker message found, but found at wrong line number: " //$NON-NLS-1$
								+ line + " Message="+msg); //$NON-NLS-1$
					}
					// successfully found message and line so remove from lists
					tofindMsg.remove(foundIndex);
					tofindLine.remove(foundIndex);
				} else {
					fail("Found unexpected marker of type " + markerTypes[i] //$NON-NLS-1$
					    + " with message: " + msg); //$NON-NLS-1$
				}
			}
			// check that we found everything we were looking for
			if (tofindMsg.size() > 0) {
				String missing = ""; //$NON-NLS-1$
				for (int j = 0; j < tofindMsg.size(); j++) {
					missing += System.getProperty("line.separator"); //$NON-NLS-1$
					missing += (String)tofindMsg.get(j);					
				}
				fail("Did not find all expected markers of type " //$NON-NLS-1$
						+ markerTypes[i] + ". Missing: " + missing); //$NON-NLS-1$
			}
		}
	}
}