/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.ui.tests.visual;

import java.util.Iterator;

import org.eclipse.contribution.xref.core.XReferenceProviderDefinition;
import org.eclipse.contribution.xref.internal.ui.actions.XReferenceCustomFilterActionInplace;
import org.eclipse.contribution.xref.internal.ui.inplace.XReferenceInplaceDialog;
import org.eclipse.contribution.xref.ui.XReferenceUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

public class XReferenceInplaceDialogTest extends VisualTestCase {

	private IProject project;
	private int viewSize;
	private ITextEditor editor;
	
	protected void setUp() throws Exception {	
		super.setUp();
		project = createPredefinedProject("bug102865"); //$NON-NLS-1$
	}

	protected void tearDown() throws Exception {
		project.refreshLocal(5, null);
		super.tearDown();
	}

	public void testKeyDrivenMenuPopUp() throws CoreException {
		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile ajFile = (IFile)res;

		// open Aspect.aj and select the pointcut
		final ITextEditor editorPart = (ITextEditor)openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(8);
		moveCursorRight(8);
		waitForJobsToComplete();

		// open inplace xref view
		final XReferenceInplaceDialog dialog = openInplaceXRef(null);
		waitForJobsToComplete();

		//Opens the inplace view menu
		postKeyDown(SWT.CTRL);
		postKey(SWT.F10);
		postKeyUp(SWT.CTRL);
		
		postKey(SWT.ESC);
		
		assertTrue("Menu has not been presented, as a result the ESC key did not close it it, and the dialog has been closed in it's place", dialog.isOpen()); //$NON-NLS-1$
		editorPart.close(false);
	}
	
	public XReferenceCustomFilterActionInplace setupDialog() {
		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile ajFile = (IFile)res;

		editor = (ITextEditor)openFileInAspectJEditor(ajFile, false);
		editor.setFocus();
		gotoLine(8);
		moveCursorRight(8);
		waitForJobsToComplete();

		// open inplace xref view
		final XReferenceInplaceDialog dialog = openInplaceXRef(null);
		waitForJobsToComplete();
		// get the filter action
		XReferenceCustomFilterActionInplace xrefAction = getFilterAction(dialog);
		waitForJobsToComplete();	
		
		checkProvidersAgree(xrefAction);

		//Opens the inplace view menu
		postKeyDown(SWT.CTRL);
		postKey(SWT.F10);
		postKeyUp(SWT.CTRL);

		// Highlights the 'Filters...' menu item and selects it
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.CR);
		
		return xrefAction;
	}
	
	public void testSelectAll() throws CoreException {
		XReferenceCustomFilterActionInplace xrefAction = setupDialog();

		// In the filter dialog
		postKey(SWT.TAB);
		postKey(SWT.CR);
		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.CR);
		
		waitForJobsToComplete();

		checkProvidersAgree(xrefAction);
		
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				// Comparing the number of selected items with the populating list at this point is ok because repeated entries
				// in the populating list are removed in the constructor of the action
				assertTrue("The number of checked Filtes should equal the number of items in the list", xrefAction.getPopulatingList().size() == provider.getCheckedInplaceFilters().size()); //$NON-NLS-1$
			}
		}
	}
	
	public void testDeselectAll() throws CoreException {
		XReferenceCustomFilterActionInplace xrefAction = setupDialog();

		// In the filter dialog
		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.CR);
		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.CR);
				
		waitForJobsToComplete();

		checkProvidersAgree(xrefAction);
				
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				// Comparing the number of selected items with the populating list at this point is ok because repeated entries
				// in the populating list are removed in the constructor of the action
				assertTrue("The number of checked Filtes be zero", provider.getCheckedInplaceFilters().size() == 0); //$NON-NLS-1$
			}
		}
		// Reset to have all filters selected
	}
	
	public void testRestoreDefaults() throws CoreException {
		XReferenceCustomFilterActionInplace xrefAction = setupDialog();

		// In the filter dialog
		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.CR);
		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.CR);
				
		waitForJobsToComplete();

		checkProvidersAgree(xrefAction);
						
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				assertTrue("provider.getCheckedFilters() should be of size() == 0", provider.getCheckedInplaceFilters().size() == 0); //$NON-NLS-1$
			}
		}		
	}

	// CheckedList should now be empty
	public void testChecking() throws CoreException {
		XReferenceCustomFilterActionInplace xrefAction = setupDialog();

		// In the filter dialog
		postKey(' ');
		postKey(SWT.ARROW_DOWN);
		postKey(' ');
		postKey(SWT.ARROW_DOWN);
		postKey(' ');
		postKey(SWT.CR);
				
		waitForJobsToComplete();

		checkProvidersAgree(xrefAction);
						
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				assertTrue("provider.getCheckedFilters() should be of size() == 3", provider.getCheckedInplaceFilters().size() == 3); //$NON-NLS-1$
			}
		}
	}
	
	// CheckedList should now have first three items checked.  Uncheck these...
	public void testUnChecking() throws CoreException {
		XReferenceCustomFilterActionInplace xrefAction = setupDialog();

		// In the filter dialog
		postKey(' ');
		postKey(SWT.ARROW_DOWN);
		postKey(' ');
		postKey(SWT.ARROW_DOWN);
		postKey(' ');
		postKey(SWT.CR);
				
		waitForJobsToComplete();

		checkProvidersAgree(xrefAction);
						
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				assertTrue("provider.getCheckedFilters() should be of size() == 0", provider.getCheckedInplaceFilters().size() == 0); //$NON-NLS-1$
			}
		}
	}

	// CheckedList should now be empty
	public void testCancelDoesNotUpdate() throws CoreException {
		XReferenceCustomFilterActionInplace xrefAction = setupDialog();

		// In the filter dialog
		postKey(' ');
		postKey(SWT.ARROW_DOWN);
		postKey(' ');
		postKey(SWT.ARROW_DOWN);
		postKey(' ');

		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.CR);
				
		waitForJobsToComplete();

		checkProvidersAgree(xrefAction);
						
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			// Only concern ourselves with those providers dealing with the setting and checking of filters
			if (provider.getAllFilters() != null){
				assertTrue("provider.getCheckedFilters() should be of size() == 0", provider.getCheckedInplaceFilters().size() == 0); //$NON-NLS-1$
			}
		}
	}
	
	public void testEscape() {
		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile ajFile = (IFile)res;

		// open Aspect.aj and select the pointcut
		final ITextEditor editorPart = (ITextEditor)openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(8);
		moveCursorRight(8);
		waitForJobsToComplete();
		
		// open inplace xref view
		XReferenceInplaceDialog dialog = openInplaceXRef(null);
		
		shutdownViewWithEscape(dialog);
		
		editorPart.close(false);
		waitForJobsToComplete();
		
	}
	
	public void testMoveAndResize() {
		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile ajFile = (IFile)res;

		// open Aspect.aj and select the pointcut
		final ITextEditor editorPart = (ITextEditor)openFileInAspectJEditor(ajFile, false);
		editorPart.setFocus();
		gotoLine(8);
		moveCursorRight(8);
		waitForJobsToComplete();
		
		// open inplace xref view
		final XReferenceInplaceDialog dialog = openInplaceXRef(null);

		// check that "remember size and location is checked"
		IDialogSettings settings = XReferenceUIPlugin.getDefault()
			.getDialogSettings().getSection("org.eclipse.contribution.internal.xref.QuickXRef"); //$NON-NLS-1$
		boolean disableRestoreLocation = settings.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_LOCATION);
		boolean disableRestoreSize = settings.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_SIZE);
		assertFalse("default setting should be to restore the location",disableRestoreLocation); //$NON-NLS-1$
		assertFalse("default setting should be to restore the size",disableRestoreSize); //$NON-NLS-1$
		
		// wait for the shell to be created
		new DisplayHelper() {

			protected boolean condition() {
				Shell s = dialog.getShell();
				boolean ret = (s != null);
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		final Shell shell = dialog.getShell();
		assertNotNull("the inplace xref view shell shouldn't be null",shell); //$NON-NLS-1$
		final Rectangle r = shell.getBounds();
		
		moveShell(shell,r.x + 50,r.y + 50,r.width + 100,r.height + 100);

		// wait for the bounds to be set and it to move
		new DisplayHelper() {

			protected boolean condition() {
				Rectangle rect = shell.getBounds();
				boolean ret = (rect != null) && !(rect.equals(r));
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		Rectangle r1 = shell.getBounds();
		assertTrue("the inplace xref view should have changed it's height", r.height != r1.height); //$NON-NLS-1$
		assertTrue("the inplace xref view should have changed it's width", r.width != r1.width); //$NON-NLS-1$
		assertTrue("the inplace xref view should have changed it's x coordinate", r.x != r1.x); //$NON-NLS-1$
		assertTrue("the inplace xref view should have changed it's y coordinate", r.y != r1.y); //$NON-NLS-1$
				
		shutdownViewWithEscape(dialog);
		
		// open inplace xref view
		final XReferenceInplaceDialog dialog2 = openInplaceXRef(dialog);
		
		new DisplayHelper() {

			protected boolean condition() {
				Shell s = dialog2.getShell();
				boolean ret = s != null;
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		assertNotNull("the inplace xref dialog shell shouldn't be null",dialog2.getShell()); //$NON-NLS-1$
		
		Rectangle r2 = dialog2.getShell().getBounds();
		assertEquals("the inplace xref view should have remembered the changed height", r1.height, r2.height); //$NON-NLS-1$
		assertEquals("the inplace xref view should have remembered the changed width", r1.width, r2.width); //$NON-NLS-1$
		assertEquals("the inplace xref view should have remembered the changed x coordinate", r1.x, r2.x); //$NON-NLS-1$
		assertEquals("the inplace xref view should have remembered the changed y coordinate", r1.y, r2.y); //$NON-NLS-1$

		// set the "disable restore" settings to be true (which means
		// that next time the inplace xref view is brought up, the size
		// and position will be the defaults
		changeDisableRestoreSettings(settings,true);
		
		shutdownViewWithEscape(dialog2);
		
		editorPart.setFocus();
		gotoLine(8);
		moveCursorRight(8);
		waitForJobsToComplete();
		// open and get hold of the new inplace xref view
		final XReferenceInplaceDialog dialog3 = openInplaceXRef(dialog2);
		
		new DisplayHelper() {

			protected boolean condition() {
				Shell s = dialog3.getShell();
				boolean ret = s != null;
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		assertNotNull("the inplace xref dialog shell shouldn't be null",dialog3.getShell()); //$NON-NLS-1$

		
		new DisplayHelper() {

			protected boolean condition() {
				Rectangle r = dialog3.getDefaultBounds();
				boolean ret = (r != null);
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		Rectangle defaults = dialog3.getDefaultBounds();
		assertNotNull("the inplace views default bounds should not be null", defaults); //$NON-NLS-1$
		
		Rectangle r3 = dialog3.getShell().getBounds();
		// for some bizarre reason on windows, 100 is added to the height and width,
		// and 50 is added to the x and y coordinates, on linux it is as expected,
		// therefore check equality for one or the other.
		assertTrue("the inplace xref view should have the default height",  //$NON-NLS-1$
				r3.height == defaults.height || r3.height == defaults.height + 100 );
		assertTrue("the inplace xref view should have the default width",  //$NON-NLS-1$
				r3.width == defaults.width || r3.width == defaults.width + 100 );
		assertTrue("he inplace xref view should have the default x coordinate",  //$NON-NLS-1$
				r3.x == defaults.x || r3.x == defaults.x + 50 );
		assertTrue("the inplace xref view should have the default y coordinate",  //$NON-NLS-1$
				r3.y == defaults.y || r3.y == defaults.y + 50 );

		// revert the disable restoring the location setting to it's default,
		// namely not to disable the restoring of the location - this
		// is just putting the settings back to their defaults.
		changeDisableRestoreSettings(settings,false);
	
		shutdownViewWithEscape(dialog3);
		editorPart.close(false);
	}
	
	public void testBug102140() {
		IResource res = project.findMember("src/pack/A.aj"); //$NON-NLS-1$
		if (res == null || !(res instanceof IFile)) {
			fail("src/pack/A.aj file not found."); //$NON-NLS-1$
		} 
		IFile ajFile = (IFile)res;

		editor = (ITextEditor)openFileInAspectJEditor(ajFile, false);
		editor.setFocus();
		gotoLine(8);
		moveCursorRight(8);
		waitForJobsToComplete();

		// open inplace xref view
		final XReferenceInplaceDialog dialog = openInplaceXRef(null);
		waitForJobsToComplete();
		// get the filter action
		XReferenceCustomFilterActionInplace xrefAction = getFilterAction(dialog);
		waitForJobsToComplete();	
		
		checkProvidersAgree(xrefAction);

		//Opens the inplace view menu
		postKeyDown(SWT.CTRL);
		postKey(SWT.F10);
		postKeyUp(SWT.CTRL);

		// Highlights the 'Filters...' menu item and selects it
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.ARROW_DOWN);
		postKey(SWT.CR);

		// In the filter dialog navigate to the
		// ok button and press return
		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.TAB);
		postKey(SWT.CR);
				
		waitForJobsToComplete();

		// if the inplace dialog has the focus after the filter has been
		// shutdown then posting an 'f' will filter out the contents of
		// the inplace view and the number of items in the tree viewer
		// will be zero. If the dialog doesn't have the focus (a regression
		// of bug 102140 then the character will be posted to the editor.
		assertEquals("inplace dialog should have one main tree node",1,dialog.getTreeViewer().getTree().getItemCount()); //$NON-NLS-1$
		
		postKey('f');
		waitForJobsToComplete();

		assertEquals("the contents of the inplace dialog should have been filtered out",0,dialog.getTreeViewer().getTree().getItemCount()); //$NON-NLS-1$
		
		shutdownViewWithEscape(dialog);
	}
	
	
	private void moveShell(Shell s, int xCoord, int yCoord, int width, int height) {
		Rectangle r1 = new Rectangle(xCoord,yCoord,width,height);
		s.setBounds(r1);
	}
	
	private void changeDisableRestoreSettings(IDialogSettings settings, boolean disable) {
		final boolean dis = disable;
		settings.put(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_LOCATION, dis);
		settings.put(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_SIZE, dis);
		
		new DisplayHelper() {

			protected boolean condition() {
				IDialogSettings s = XReferenceUIPlugin.getDefault()
					.getDialogSettings().getSection("org.eclipse.contribution.internal.xref.QuickXRef"); //$NON-NLS-1$
				boolean ret;
				if (dis) {
					ret = s.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_LOCATION)
						&& s.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_SIZE);
				} else {
					ret = !(s.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_LOCATION))
						&& !(s.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_SIZE));
				}
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);

		if (dis) {
			assertTrue("setting should be to disable restoring the location",settings.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_LOCATION)); //$NON-NLS-1$
			assertTrue("setting should be to disable restoring the size",settings.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_SIZE));		 //$NON-NLS-1$
		} else {
			assertFalse("setting should be to enable restoring the location",settings.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_LOCATION)); //$NON-NLS-1$
			assertFalse("setting should be to enable restoring the size",settings.getBoolean(XReferenceInplaceDialog.STORE_DISABLE_RESTORE_SIZE)); //$NON-NLS-1$
		}

	}
	
	private void shutdownViewWithEscape(XReferenceInplaceDialog xrefDialog) {
		final XReferenceInplaceDialog dialog = xrefDialog;
		// press esc
		postKey(SWT.ESC);
		
		// wait a few secs
		new DisplayHelper() {

			protected boolean condition() {
				boolean ret = !dialog.isOpen();
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		assertFalse("xref inplace view should not be open",dialog.isOpen()); //$NON-NLS-1$

	}
	
	private XReferenceInplaceDialog openInplaceXRef(XReferenceInplaceDialog previousDialog) {
		
		openDialog(previousDialog);
		
		XReferenceInplaceDialog dialog = XReferenceInplaceDialog.getInplaceDialog();
		
		// try again, in case the posted key events didn't
		// get posted correctly the first time
		if (dialog == null) {
			openDialog(previousDialog);
		}
		
		final XReferenceInplaceDialog newDialog = XReferenceInplaceDialog.getInplaceDialog();
		assertNotNull("the inplace dialog shouldn't be null",newDialog); //$NON-NLS-1$
		assertFalse("should have the new inplace dialog",newDialog.equals(previousDialog)); //$NON-NLS-1$
	
		new DisplayHelper() {

			protected boolean condition() {
				boolean ret = newDialog.isOpen();
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		
		assertTrue("xref inplace view should be open",newDialog.isOpen()); //$NON-NLS-1$
		
		return newDialog;
	}
	
	private void openDialog(XReferenceInplaceDialog previousDialog) {
		final XReferenceInplaceDialog dialog = previousDialog;
		
		postKeyDown(SWT.CTRL);
		postKeyDown(SWT.ALT);
		postKey('x');
		postKeyUp(SWT.ALT);
		postKeyUp(SWT.CTRL);
		
		new DisplayHelper() {

			protected boolean condition() {
				XReferenceInplaceDialog i = XReferenceInplaceDialog.getInplaceDialog();
				boolean ret = (i != null) && !(i.equals(dialog));
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
	}
	
	private void checkProvidersAgree(XReferenceCustomFilterActionInplace xrefAction) {
		// If any providers return Lists from getCheckedFilters(), they should all agree on the stored Lists
		XReferenceProviderDefinition contributingProviderDefinition = null;
		for (Iterator iter = xrefAction.getProviderDefns().iterator(); iter.hasNext();) {
			XReferenceProviderDefinition provider = (XReferenceProviderDefinition) iter.next();
			if (provider.getCheckedFilters() != null || provider.getCheckedInplaceFilters() != null) {
				if (contributingProviderDefinition == null){
					contributingProviderDefinition = provider;
					viewSize = contributingProviderDefinition.getCheckedFilters().size();
				} else {
					assertTrue("Provider 'checked' Lists do not match", //$NON-NLS-1$
							provider.getCheckedFilters().equals(contributingProviderDefinition.getCheckedFilters()) && provider.getCheckedFilters().size() == viewSize);
					assertTrue("Provider 'checkedInplace' Lists do not match", //$NON-NLS-1$
							provider.getCheckedInplaceFilters().equals(contributingProviderDefinition.getCheckedInplaceFilters()));
				}
			} else {
				contributingProviderDefinition = provider;
			}
		}		
	}
	
	private XReferenceCustomFilterActionInplace getFilterAction(XReferenceInplaceDialog inplaceDialog) {
		final XReferenceInplaceDialog dialog = inplaceDialog;
		
		new DisplayHelper() {

			protected boolean condition() {
				Action a = dialog.getCustomFilterActionInplace();
				boolean ret = (a != null);
				return ret;
			}
		
		}.waitForCondition(Display.getCurrent(), 5000);
		assertNotNull("Should have custom filter dialog action in inplace xref view",dialog.getCustomFilterActionInplace()); //$NON-NLS-1$
		return (XReferenceCustomFilterActionInplace)dialog.getCustomFilterActionInplace();
	}
}
