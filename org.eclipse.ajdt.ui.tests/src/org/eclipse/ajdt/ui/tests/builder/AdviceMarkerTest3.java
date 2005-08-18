/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
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
 * Tests for source advice markers
 */
public class AdviceMarkerTest3 extends UITestCase {

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
		IAJModelMarker.SOURCE_ITD_MARKER//,
//		IAJModelMarker.ADVICE_MARKER
	};

	// expected results:
	//   first index of array is the marker type, as above.
	//   second index is expected marker label
	//   third index is expected line number of that marker
	private static Object[][][] results = {
			{ // IAJModelMarker.SOURCE_ADVICE_MARKER
					// Temporary - declare soft should have a different type of marker...
//					{ "advises Demo.go()", new Integer(28) },
					{ "advises Demo: exception-handler(void tjp.Demo.<catch>(tjp.DemoException))", new Integer(39)},
//					{ "advises Demo.bar(Integer)", new Integer(49) },
//					{ "advises Demo.foo(int, Object)", new Integer(49) },
//					{ "advises Demo.go()", new Integer(49) },
//					{ "advises Demo.main(String[])", new Integer(49) },
					{ "4 AspectJ markers at this line", new Integer(49) },
					{ "advises Demo.foo(int, Object) (runtime test)", new Integer(53) },
					{ "advises Demo: field-set(int tjp.Demo.x)", new Integer(57) },
//					{ "advises Demo.main(String[]) (runtime test)", new Integer(61) },
//					{ "advises Demo.foo(int, Object) (runtime test)", new Integer(61) },
//					{ "advises Demo.bar(Integer) (runtime test)", new Integer(61) },
					{ "3 AspectJ markers at this line", new Integer(61) },
					{ "advises GetInfo.printParameters(JoinPoint)", new Integer(74) }
			},
			{ // IAJModelMarker.SOURCE_BEFORE_ADVICE_MARKER
					{ "advises Demo: exception-handler(void tjp.Demo.<catch>(tjp.DemoException))", new Integer(39)},
					{ "4 AspectJ markers at this line", new Integer(49) }
//					{ "advises Demo.bar(Integer)", new Integer(49) },
//					{ "advises Demo.foo(int, Object)", new Integer(49) },
//					{ "advises Demo.go()", new Integer(49) },
//					{ "advises Demo.main(String[])", new Integer(49) }
					},
			{ // IAJModelMarker.SOURCE_AFTER_ADVICE_MARKER
			    	{ "advises Demo: field-set(int tjp.Demo.x)", new Integer(57) },
			    	{ "advises GetInfo.printParameters(JoinPoint)", new Integer(74) }
			},
			{ // IAJModelMarker.SOURCE_AROUND_ADVICE_MARKER
				{}
			},
			{ // IAJModelMarker.SOURCE_DYNAMIC_ADVICE_MARKER
				{}
			},
			{ // IAJModelMarker.SOURCE_DYNAMIC_BEFORE_ADVICE_MARKER
				{ "advises Demo.foo(int, Object) (runtime test)", new Integer(53) }
			},
			{ // IAJModelMarker.SOURCE_DYNAMIC_AFTER_ADVICE_MARKER
				{}
			},
			{ // IAJModelMarker.SOURCE_DYNAMIC_AROUND_ADVICE_MARKER
				{ "3 AspectJ markers at this line", new Integer(61) }
//				{ "advises Demo.main(String[]) (runtime test)", new Integer(61) },
//				{ "advises Demo.foo(int, Object) (runtime test)", new Integer(61) },
//				{ "advises Demo.bar(Integer) (runtime test)", new Integer(61) }
			},
			{ // IAJModelMarker.SOURCE_ITD_MARKER
				{ "declared on Demo", new Integer(25)},
				{ "softens Demo.go()", new Integer(27)},
				{ "declared on Demo", new Integer(29)},
				{ "declared on Demo", new Integer(33)}
			}//, // IAJModelMarker.ADVICE_MARKER
//			{
//				{ "advised by GetInfo.after(): <anonymous pointcut>", new Integer(79)}
//				{ "advises
//			}
		};

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		project = createPredefinedProject("MarkersTest");
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		deleteProject(project);
	}

	public void testMarkers() throws Exception {
		String filename = "src/tjp/GetInfo.aj";
		IResource file = project.findMember(filename);
		if (file == null)
			fail("Required file not found: " + filename);

		IMarker[] allMarkers = file.findMarkers(IMarker.MARKER, true, IResource.DEPTH_INFINITE);
		for (int x = 0; x < allMarkers.length; x++) {
			IMarker marker = allMarkers[x];
			System.out.println(marker.getType());
		}
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
