/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ben Dalziel     - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.contribution.xref.ui.filters.CustomFilterDialog;
import org.eclipse.swt.SWT;

public class CustomFilterDialogTest extends VisualTestCase {

	public void testShowDialogWithoutCheckedList() {

		// Works with checkedList.size() == 0. Assignes the values of
		// defaultCheckedList to the checkedList

		List populatingList = new ArrayList();
		populatingList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		List checkedList = new ArrayList();

		List defaultCheckedList = new ArrayList();
		defaultCheckedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				postKey(SWT.CR);
			}
		};

		new Thread(r).start();

		List returnedList = CustomFilterDialog.showDialog(null, populatingList,
				checkedList, defaultCheckedList, "TITLE", "MESSAGE"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Returned list should not have any elements", //$NON-NLS-1$
				returnedList.size() == 0);
	}

	public void testShowDialogWithCheckedList() {

		List populatingList = new ArrayList();
		populatingList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		List checkedList = new ArrayList();
		checkedList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$

		List defaultCheckedList = new ArrayList();
		defaultCheckedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				postKey(SWT.CR);
			}
		};

		new Thread(r).start();

		List returnedList = CustomFilterDialog.showDialog(null, populatingList,
				checkedList, defaultCheckedList, "TITLE", "MESSAGE"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Returned list should have one element", returnedList //$NON-NLS-1$
				.size() == 1);
		assertEquals(
				"Returned list (0) item should be the same as the checkedList", //$NON-NLS-1$
				returnedList.get(0), checkedList.get(0));
	}
	

	public void testShowDialogWithBadCheckedList() {

		List populatingList = new ArrayList();
		populatingList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		List checkedList = new ArrayList();
		checkedList.add("NOT IN populatingList"); //$NON-NLS-1$

		List defaultCheckedList = new ArrayList();
		defaultCheckedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				postKey(SWT.CR);
			}
		};

		new Thread(r).start();

		List returnedList = CustomFilterDialog.showDialog(null, populatingList,
				checkedList, defaultCheckedList, "TITLE", "MESSAGE"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Returned list should be empty", returnedList //$NON-NLS-1$
				.size() == 0);
	}
	

	public void testShowDialogWithNoneStringCheckedList() {

		List populatingList = new ArrayList();
		populatingList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		List checkedList = new ArrayList();
		checkedList.add(new ArrayList());

		List defaultCheckedList = new ArrayList();
		defaultCheckedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		//  Shouldn't need because the dialog will never run
//		Runnable r = new Runnable() {
//			public void run() {
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				postCharacterKey(SWT.CR);
//			}
//		};
//
//		new Thread(r).start();

		List returnedList = CustomFilterDialog.showDialog(null, populatingList,
				checkedList, defaultCheckedList, "TITLE", "MESSAGE"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Returned list should be empty", returnedList //$NON-NLS-1$
				.size() == 0);
	}
	

	public void testShowDialogWithTooBigCheckedList() {

		List populatingList = new ArrayList();
		populatingList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		List checkedList = new ArrayList();
		checkedList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		checkedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$
		checkedList.add("ITEM FROM filterDialogTest, 3"); //$NON-NLS-1$

		List defaultCheckedList = new ArrayList();
		defaultCheckedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		//  Shouldn't need because the dialog will never run
//		Runnable r = new Runnable() {
//			public void run() {
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				postCharacterKey(SWT.CR);
//			}
//		};
//
//		new Thread(r).start();

		List returnedList = CustomFilterDialog.showDialog(null, populatingList,
				checkedList, defaultCheckedList, "TITLE", "MESSAGE"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Returned list should be empty", returnedList //$NON-NLS-1$
				.size() == 0);
	}
	

	public void testShowDialogWithTooBigDefaultList() {

		List populatingList = new ArrayList();
		populatingList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		List checkedList = new ArrayList();
		checkedList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$

		List defaultCheckedList = new ArrayList();
		defaultCheckedList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		defaultCheckedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$
		defaultCheckedList.add("ITEM FROM filterDialogTest, 3"); //$NON-NLS-1$

		//  Shouldn't need because the dialog will never run
//		Runnable r = new Runnable() {
//			public void run() {
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				postCharacterKey(SWT.CR);
//			}
//		};
//
//		new Thread(r).start();

		List returnedList = CustomFilterDialog.showDialog(null, populatingList,
				checkedList, defaultCheckedList, "TITLE", "MESSAGE"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Returned list should be empty", returnedList //$NON-NLS-1$
				.size() == 0);
	}

	public void testCheckEntry() {

		List populatingList = new ArrayList();
		populatingList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		List checkedList = new ArrayList();
		checkedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		List defaultCheckedList = new ArrayList();
		defaultCheckedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				postKey(' ');
				postKey(SWT.CR);
			}
		};

		new Thread(r).start();

		List returnedList = CustomFilterDialog.showDialog(null, populatingList,
				checkedList, defaultCheckedList, "TITLE", "MESSAGE"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Returned list should have two entries, the set checked item and the newly checked item", returnedList //$NON-NLS-1$
				.size() == 2);
		assertEquals(
				"returnedList.get(0) item should be the same as the populatingList.get(0)", //$NON-NLS-1$
				returnedList.get(0), populatingList.get(0));
		assertEquals(
				"returnedList.get(1) item should be the same as the populatingList.get(1)", //$NON-NLS-1$
				returnedList.get(1), populatingList.get(1));
	}
	

	public void testUnCheckEntry() {

		List populatingList = new ArrayList();
		populatingList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		List checkedList = new ArrayList();
		checkedList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$

		List defaultCheckedList = new ArrayList();
		defaultCheckedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				postKey(' ');
				postKey(SWT.CR);
			}
		};

		new Thread(r).start();

		List returnedList = CustomFilterDialog.showDialog(null, populatingList,
				checkedList, defaultCheckedList, "TITLE", "MESSAGE"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Returned list should not have any entries, the checked item should have been unchecked", returnedList //$NON-NLS-1$
				.size() == 0);
	}
	
	public void testSelectAll() {

		List populatingList = new ArrayList();
		populatingList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 3"); //$NON-NLS-1$

		List checkedList = new ArrayList();
		checkedList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$

		List defaultCheckedList = new ArrayList();
		defaultCheckedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
//				postCharacterKey((char) SWT.ARROW_DOWN);
				postKey(SWT.TAB);
				postKey(SWT.CR);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.CR);
			}
		};

		new Thread(r).start();

		List returnedList = CustomFilterDialog.showDialog(null, populatingList,
				checkedList, defaultCheckedList, "TITLE", "MESSAGE"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Returned list should have 3 entries", returnedList //$NON-NLS-1$
				.size() == 3);
		assertEquals(
				"returnedList.get(0) item should be the same as the populatingList.get(0)", //$NON-NLS-1$
				returnedList.get(0), populatingList.get(0));
		assertEquals(
				"returnedList.get(1) item should be the same as the populatingList.get(1)", //$NON-NLS-1$
				returnedList.get(1), populatingList.get(1));
		assertEquals(
				"returnedList.get(2) item should be the same as the populatingList.get(2)", //$NON-NLS-1$
				returnedList.get(2), populatingList.get(2));
	}
	
	public void testDeSelectAll() {

		List populatingList = new ArrayList();
		populatingList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 3"); //$NON-NLS-1$

		List checkedList = new ArrayList();
		checkedList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		checkedList.add("ITEM FROM filterDialogTest, 3"); //$NON-NLS-1$

		List defaultCheckedList = new ArrayList();
		defaultCheckedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
//				postCharacterKey((char) SWT.ARROW_DOWN);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.CR);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.CR);
			}
		};

		new Thread(r).start();

		List returnedList = CustomFilterDialog.showDialog(null, populatingList,
				checkedList, defaultCheckedList, "TITLE", "MESSAGE"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Returned list should not have any entries", returnedList //$NON-NLS-1$
				.size() == 0);
	}
	
	public void testRestoreDefaults() {

		List populatingList = new ArrayList();
		populatingList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$
		populatingList.add("ITEM FROM filterDialogTest, 3"); //$NON-NLS-1$

		List checkedList = new ArrayList();
		checkedList.add("ITEM FROM filterDialogTest, 1"); //$NON-NLS-1$
		checkedList.add("ITEM FROM filterDialogTest, 3"); //$NON-NLS-1$

		List defaultCheckedList = new ArrayList();
		defaultCheckedList.add("ITEM FROM filterDialogTest, 2"); //$NON-NLS-1$

		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
//				postCharacterKey((char) SWT.ARROW_DOWN);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.TAB);
				postKey(SWT.CR);
				postKey(SWT.TAB);
				postKey(SWT.CR);
			}
		};

		new Thread(r).start();

		List returnedList = CustomFilterDialog.showDialog(null, populatingList,
				checkedList, defaultCheckedList, "TITLE", "MESSAGE"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Returned list should have 1 entry", returnedList //$NON-NLS-1$
				.size() == 1);
		assertEquals(
				"returnedList.get(0) item should be the same as the defaultCheckedList.get(0)", //$NON-NLS-1$
				returnedList.get(0), defaultCheckedList.get(0));
	}
	
}
