/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.wizards.export;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.ajdt.internal.ui.wizards.exports.AJPluginExportWizard;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests the Export "Deployable plug-ins with AspectJ support" functionality,
 * which also indirectly tests the create Ant build file support.
 * 
 */
public class ExportPluginTest extends UITestCase {

	// taken from BaseExportWizardPage
	private static final String S_ZIP_FILENAME = "zipFileName"; //$NON-NLS-1$

	private String archivePath;

	protected void setUp() throws Exception {
		super.setUp();
		URL location = Platform.resolve(Platform.getBundle(
				"org.eclipse.ajdt.ui.tests").getEntry("/")); //$NON-NLS-1$ //$NON-NLS-2$
		URL fileURL = new URL(location, "export.zip"); //$NON-NLS-1$
		archivePath = fileURL.getPath();
	}

	public void testExportPlugin() throws Exception {
		IProject project = createPredefinedProject("Hello World Plugin"); //$NON-NLS-1$
		AJPluginExportWizard wiz = new AJPluginExportWizard() {
			public IDialogSettings getDialogSettings() {
				IDialogSettings settings = super.getDialogSettings();
				settings.put(S_ZIP_FILENAME + String.valueOf(0), archivePath);
				return settings;
			}
		};
		wiz.init(JavaPlugin.getDefault().getWorkbench(),
				new StructuredSelection(project));
		File zip = new File(archivePath);
		if (zip.exists()) {
			zip.delete();
		}
		
		Shell shell = JavaPlugin.getActiveWorkbenchShell();
		MyWizardDialog dialog = new MyWizardDialog(shell, wiz);
		dialog.setBlockOnOpen(false);
		dialog.create();
		dialog.open();
		dialog.finishPressed();

		waitForJobsToComplete();

		// now check zip file was created
		if (!zip.exists()) {
			fail("Export of plugin failed to created zip file: " + zip); //$NON-NLS-1$
		}
		assertTrue("Created zip file has a length of 0",zip.length()>0); //$NON-NLS-1$
		ZipFile zf = new ZipFile(zip);
		String jarEntry = "plugins/HelloWorld_1.0.0/HelloWorld.jar"; //$NON-NLS-1$
		ZipEntry entry = zf.getEntry(jarEntry);
		assertNotNull("Couldn't find entry in created zip file for: "+jarEntry,entry); //$NON-NLS-1$
		String xmlEntry = "plugins/HelloWorld_1.0.0/plugin.xml"; //$NON-NLS-1$
		entry = zf.getEntry(xmlEntry);
		assertNotNull("Couldn't find entry in created zip file for: "+xmlEntry,entry); //$NON-NLS-1$
		zf.close();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if ((archivePath != null) && archivePath.length() > 0) {
			File zip = new File(archivePath);
			if (zip.exists()) {
				zip.delete();
				zip.deleteOnExit();
			}
		}
	}

	private class MyWizardDialog extends WizardDialog {

		/**
		 * @param parentShell
		 * @param newWizard
		 */
		public MyWizardDialog(Shell parentShell, IWizard newWizard) {
			super(parentShell, newWizard);
		}

		public void finishPressed() {
			super.finishPressed();
		}

	}
}
