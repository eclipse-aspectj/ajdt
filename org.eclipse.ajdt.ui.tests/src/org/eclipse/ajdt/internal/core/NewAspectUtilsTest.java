/**********************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Matt Chapman - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.core;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.ui.wizards.NewAspectCreationWizardPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class NewAspectUtilsTest extends TestCase {

	private IProject project;
	private IResource resource;


	/**
	 * Constructor for AJDTUtilsTest.
	 * @param name
	 */
	public NewAspectUtilsTest(String name) {
		super(name);
	}

	public void testGetInnerInsertionPoint() {
		String input1 = "/*\n* Created on 23-Oct-2003\n*\n * To change the template for this generated file go to\n * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments\n//embedded comment\n//public class TestClass {}\n */\npackage foo.test;\n\n/**\n * @author mchapman\n *\n * To change the template for this generated type comment go to\n * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments\n */\npublic class TestClass {\n	public static void main(String[] args){\n		System.out.println(\"Hello\");\n	}\n}";
		String clname1 = "TestClass";
		int p = NewAspectCreationWizardPage.NewAspectUtils.getInnerInsertionPoint(input1,clname1);
		String before=input1.substring(0,p);
		String after=input1.substring(p);
		assertTrue("Pre- and post-insertion point sections should together match the entire input",
			input1.equals(before+after));
		assertTrue("Pre-insertion point section should end with {",
			before.trim().endsWith("{"));
		assertTrue("Post-insertion point section should begin with main declaration",
			after.trim().startsWith("public static void main"));
	}
	
	public void testGetInnerImportsInsertionPoint() {
		String[] input = {
			// source with package statement, no imports
			"/*\n* Created on 23-Oct-2003\n*\n * To change the template for this generated file go to\n * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments\n//embedded comment\n//public class TestClass {}\n */\npackage foo.test;\n\n/**\n * @author mchapman\n *\n * To change the template for this generated type comment go to\n * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments\n */\npublic class TestClass {",
			// source with package statement and existing imports
			"/*\n* Created on 23-Oct-2003\n*\n * To change the template for this generated file go to\n * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments\n//embedded comment\n//public class TestClass {}\n */\npackage foo.test;\nimport javax.swing.JComponent;\nimport javax.swing.JFrame;\n\n/**\n * @author mchapman\n *\n * To change the template for this generated type comment go to\n * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments\n */\npublic class TestClass {",
			// source in default package with existing imports
			"/*\n* Created on 23-Oct-2003\n*\n * To change the template for this generated file go to\n * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments\n//embedded comment\n//public class TestClass {}\n */\nimport javax.swing.JComponent;\nimport javax.swing.JFrame;\n\n/**\n * @author mchapman\n *\n * To change the template for this generated type comment go to\n * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments\n */\npublic class TestClass {",
			// source in default package, no imports
			"/*\n* Created on 23-Oct-2003\n*\n * To change the template for this generated file go to\n * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments\n//embedded comment\n//public class TestClass {}\n */\n/**\n * @author mchapman\n *\n * To change the template for this generated type comment go to\n * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments\n */\npublic class TestClass {"
		};

		for (int i=0; i<input.length; i++) {
			int p = NewAspectCreationWizardPage.NewAspectUtils.getInnerImportsInsertionPoint(input[i]);
			String before=input[i].substring(0,p);
			String after=input[i].substring(p);
			assertTrue("Pre- and post-insertion point sections should together match the entire input",
						input[i].equals(before+after));
			if (i==0 || i==1) {
				assertTrue("Pre-insertion point section should contain package statement",
							before.indexOf("package")>=0);
			}
			assertTrue("Post-insertion point section should contain class declaration",
						after.indexOf("public class TestClass")>=0);
		}	
	}
}
