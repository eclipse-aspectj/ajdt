/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.wizards.export;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.ajdt.internal.ui.wizards.exports.ProductExportWizard;
import org.eclipse.ajdt.ui.tests.AspectJTestPlugin;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
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
public class ExportProductTest extends UITestCase {

	// taken from ExportDestinationTab
	private static final String S_EXPORT_DIRECTORY = "exportDirectory"; //$NON-NLS-1$
	private static final String S_DESTINATION = "destination"; //$NON-NLS-1$

	// taken from ProductConfigurationSection
	private static final String S_PRODUCT_CONFIG = "productConfig"; //$NON-NLS-1$

	private String exportFolder;

	protected void setUp() throws Exception {
		super.setUp();
		IPath state = AspectJTestPlugin.getDefault().getStateLocation();
		exportFolder = state.append("exportProductDir/").toOSString(); //$NON-NLS-1$
	}

	public void _testExportProduct() throws Exception {
        // Ignore these tests on Linux because not passing
        if (System.getProperty("os.name").equals("Linux")) {
            return;
        }
        
		IProject project = createPredefinedProject("com.example.xzy"); //$NON-NLS-1$
		ProductExportWizard wiz = new ProductExportWizard() {
			public IDialogSettings getDialogSettings() {
				IDialogSettings settings = super.getDialogSettings();
				settings.put(S_PRODUCT_CONFIG + String.valueOf(0), "/com.example.xzy/sample.product"); //$NON-NLS-1$
				settings.put(S_EXPORT_DIRECTORY, true);
				settings.put(S_DESTINATION + String.valueOf(0), exportFolder);
				return settings;
			}
		};
		wiz.init(JavaPlugin.getDefault().getWorkbench(),
				new StructuredSelection(project));
		File folder = new File(exportFolder);
		if (folder.exists()) {
			deleteDir(folder);
			if (folder.exists()) {
				fail("Couldn't delete export Folder"); //$NON-NLS-1$
			}
		}
		
		Shell shell = JavaPlugin.getActiveWorkbenchShell();
		MyWizardDialog dialog = new MyWizardDialog(shell, wiz);
		dialog.setBlockOnOpen(false);
		dialog.create();
		dialog.open();
		dialog.finishPressed();

		waitForJobsToComplete();

		// now check export folder was created
		if (!folder.exists()) {
			fail("Export of product failed to create export folder: " + folder); //$NON-NLS-1$
		}
		File eclipseFolder = new File(folder, "eclipse"); //$NON-NLS-1$
		if (!eclipseFolder.exists()) {
			fail("Export of product failed to create eclipse sub-folder: " + eclipseFolder); //$NON-NLS-1$
		}
		File pluginsFolder = new File(eclipseFolder, "plugins"); //$NON-NLS-1$
		if (!pluginsFolder.exists()) {
			fail("Export of product failed to create plugins sub-folder: " + pluginsFolder); //$NON-NLS-1$
		}
		boolean foundAjRuntime = false;
		boolean foundXyzPlugin = false;
		File[] plugins = pluginsFolder.listFiles();
		for (int i = 0; i < plugins.length; i++) {
			String name = plugins[i].getName();
			if (name.indexOf("org.aspectj.runtime") != -1) { //$NON-NLS-1$
				foundAjRuntime = true;
			} else if (name.indexOf("com.example.xzy") != -1) { //$NON-NLS-1$
				foundXyzPlugin = true;
				File jar = plugins[i];
				assertTrue("Created plugin is not a file",jar.isFile()); //$NON-NLS-1$
				assertTrue("Created plugin has a file length of 0",jar.length()>0); //$NON-NLS-1$
				// now check that it contains an aspect
				ZipFile zf = new ZipFile(jar);
				String jarEntry = "com/example/xzy/MyAspect.class"; //$NON-NLS-1$
				ZipEntry entry = zf.getEntry(jarEntry);
				assertNotNull("Couldn't find entry in created jar file for: "+jarEntry,entry); //$NON-NLS-1$
				jarEntry = "com/example/xzy/MyAspect.aj"; //$NON-NLS-1$
				entry = zf.getEntry(jarEntry);
				assertNull("Should NOT have found entry in created jar file for: "+jarEntry,entry); //$NON-NLS-1$	
			}
		}
		assertTrue("Product Export failed to include AspectJ runtime plugin",foundAjRuntime); //$NON-NLS-1$
		assertTrue("Product Export failed to include com.example.xyz plugin",foundXyzPlugin); //$NON-NLS-1$
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		if ((exportFolder != null) && exportFolder.length() > 0) {
			final File folder = new File(exportFolder);
			if (folder.exists()) {
				Runnable r = new Runnable() {
					public void run() {
						// allow time for file handles to be released
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
						}
						deleteDir(folder);						
					}
				};
				new Thread(r).start();
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
