/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

import org.eclipse.ajdt.internal.ui.wizards.exports.AJPluginExportWizard;
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
  /* AJDT 1.7 */
  /* lots of changes for AJDT 1.7  do not sync with 1.6 */
public class ExportPluginTest extends UITestCase {

	// taken from BaseExportWizardPage / ExportDestinationTab / ExportOptionsTab
    protected static final String S_EXPORT_TYPE = "exportType"; //$NON-NLS-1$
    protected static final String S_DESTINATION = "destination"; //$NON-NLS-1$
    protected static final String S_ZIP_FILENAME = "zipFileName"; //$NON-NLS-1$
    protected static final String S_INSTALL_DESTINATION = "installDestination"; //$NON-NLS-1$
    protected static final String S_JAR_FORMAT = "exportUpdate"; //$NON-NLS-1$

    protected static final int TYPE_DIR = 1;
    protected static final int TYPE_ARCHIVE = 2;
    protected static final int TYPE_INSTALL = 3;


	private String archivePath;
	private String exportFolder;

	protected void setUp() throws Exception {
		super.setUp();
		IPath state = AspectJTestPlugin.getDefault().getStateLocation();
		archivePath = state.append("export.zip").toOSString(); //$NON-NLS-1$
		exportFolder = state.append("exportDir/").toOSString(); //$NON-NLS-1$
	}

	public void _testExportPluginAsZip() throws Exception {
	    
	    // Ignore these tests on Linux because not passing
//	    if (System.getProperty("os.name").equals("Linux")) {
//	        return;
//	    }
	    
	    
		IProject project = createPredefinedProject("Hello World Plugin"); //$NON-NLS-1$
		AJPluginExportWizard wiz = new AJPluginExportWizard() {
			public IDialogSettings getDialogSettings() {
				IDialogSettings settings = super.getDialogSettings();
				settings.put(S_EXPORT_TYPE, String.valueOf(TYPE_ARCHIVE));
				settings.put(S_ZIP_FILENAME + String.valueOf(0), archivePath);
                settings.put(S_JAR_FORMAT, false);
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
			fail("Export of plugin failed to create zip file: " + zip); //$NON-NLS-1$
		}
		assertTrue("Created zip file has a length of 0",zip.length()>0); //$NON-NLS-1$
		ZipFile zf = new ZipFile(zip);

		String jarEntry = "plugins/HelloWorld_1.0.0/HelloWorld.jar"; //$NON-NLS-1$
        ZipEntry entry = zf.getEntry(jarEntry);
        // Macs seem to be generating zip files slightly differently
        if (entry == null) {
            entry = zf.getEntry("./" + jarEntry); //$NON-NLS-1$
        }
        
		assertNotNull("Couldn't find entry in created zip file for: "+jarEntry,entry); //$NON-NLS-1$
		String xmlEntry = "plugins/HelloWorld_1.0.0/plugin.xml"; //$NON-NLS-1$
		entry = zf.getEntry(xmlEntry);
        // Macs seem to be generating zip files slightly differently
        if (entry == null) {
            entry = zf.getEntry("./" + xmlEntry); //$NON-NLS-1$
        }
		assertNotNull("Couldn't find entry in created zip file for: "+xmlEntry,entry); //$NON-NLS-1$
		zf.close();
	}

	public void _testExportMinimalBundleAsZip() throws Exception {
        // Ignore these tests on Linux because not passing
//        if (System.getProperty("os.name").equals("Linux")) {
//            return;
//        }
        
		IProject project = createPredefinedProject("Minimal Plugin"); //$NON-NLS-1$
		AJPluginExportWizard wiz = new AJPluginExportWizard() {
			public IDialogSettings getDialogSettings() {
				IDialogSettings settings = super.getDialogSettings();
                settings.put(S_EXPORT_TYPE, String.valueOf(TYPE_ARCHIVE));
				settings.put(S_ZIP_FILENAME + String.valueOf(0), archivePath);
                settings.put(S_JAR_FORMAT, false);
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
			fail("Export of plugin failed to create zip file: " + zip); //$NON-NLS-1$
		}
		assertTrue("Created zip file has a length of 0",zip.length()>0); //$NON-NLS-1$
		ZipFile zf = new ZipFile(zip);
		String jarEntry = "plugins/MyPlugin_1.0.0/helloWorld/HelloAspect.class"; //$NON-NLS-1$
		ZipEntry entry = zf.getEntry(jarEntry);
        // Macs seem to be generating zip files slightly differently
        if (entry == null) {
            entry = zf.getEntry("./" + jarEntry); //$NON-NLS-1$
        }
        assertNotNull("Couldn't find entry in created zip file for: "+jarEntry,entry); //$NON-NLS-1$
		zf.close();
	}

	public void _testExportJavaBundleAsZip() throws Exception {
        // Ignore these tests on Linux because not passing
//        if (System.getProperty("os.name").equals("Linux")) {
//            return;
//        }
        
		IProject project = createPredefinedProject("Hello World Java Bundle"); //$NON-NLS-1$
		AJPluginExportWizard wiz = new AJPluginExportWizard() {
			public IDialogSettings getDialogSettings() {
				IDialogSettings settings = super.getDialogSettings();
                settings.put(S_EXPORT_TYPE, String.valueOf(TYPE_ARCHIVE));
				settings.put(S_ZIP_FILENAME + String.valueOf(0), archivePath);
                settings.put(S_JAR_FORMAT, false);
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
			fail("Export of plugin failed to create zip file: " + zip); //$NON-NLS-1$
		}
		assertTrue("Created zip file has a length of 0",zip.length()>0); //$NON-NLS-1$
		ZipFile zf = new ZipFile(zip);
		String jarEntry = "plugins/MyPlugin3_1.0.0/myplugin3/Activator.class"; //$NON-NLS-1$
		ZipEntry entry = zf.getEntry(jarEntry);
        // Macs seem to be generating zip files slightly differently
        if (entry == null) {
            entry = zf.getEntry("./" + jarEntry); //$NON-NLS-1$
        }
        assertNotNull("Couldn't find entry in created zip file for: "+jarEntry,entry); //$NON-NLS-1$
		String xmlEntry = "plugins/MyPlugin3_1.0.0/META-INF/MANIFEST.MF"; //$NON-NLS-1$
		entry = zf.getEntry(xmlEntry);
        // Macs seem to be generating zip files slightly differently
        if (entry == null) {
            entry = zf.getEntry("./" + xmlEntry); //$NON-NLS-1$
        }
		assertNotNull("Couldn't find entry in created zip file for: "+xmlEntry,entry); //$NON-NLS-1$
		zf.close();
	}

    public void _testExportPluginAsDir() throws Exception {
        // Ignore these tests on Linux because not passing
//        if (System.getProperty("os.name").equals("Linux")) {
//            return;
//        }
        
        IProject project = createPredefinedProject("Hello World Plugin"); //$NON-NLS-1$
        AJPluginExportWizard wiz = new AJPluginExportWizard() {
            public IDialogSettings getDialogSettings() {
                IDialogSettings settings = super.getDialogSettings();
                settings.put(S_EXPORT_TYPE, String.valueOf(TYPE_DIR));
                settings.put(S_DESTINATION + String.valueOf(0), exportFolder);
                settings.put(S_JAR_FORMAT, false);
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
            fail("Export of plugin failed to create export folder: " + folder); //$NON-NLS-1$
        }
        File pluginsFolder = new File(folder, "plugins"); //$NON-NLS-1$
        if (!pluginsFolder.exists()) {
            fail("Export of plugin failed to create plugins sub-folder: " + pluginsFolder); //$NON-NLS-1$
        }
        File hwFolder = new File(pluginsFolder, "HelloWorld_1.0.0"); //$NON-NLS-1$
        if (!hwFolder.exists()) {
            fail("Export of plugin failed to create HelloWorld_1.0.0 sub-folder: " + hwFolder); //$NON-NLS-1$
        }
        File jar = new File(hwFolder, "HelloWorld.jar"); //$NON-NLS-1$
        if (!jar.exists()) {
            fail("Export of plugin failed to create HelloWorld.jar: " + jar); //$NON-NLS-1$
        }
        ZipFile zf = new ZipFile(jar);
        String jarEntry = "helloWorld/HelloWorldPlugin.class"; //$NON-NLS-1$
        ZipEntry entry = zf.getEntry(jarEntry);
        // Macs seem to be generating zip files slightly differently
        if (entry == null) {
            entry = zf.getEntry("./" + jarEntry); //$NON-NLS-1$
        }
        assertNotNull("Couldn't find entry in created jar file for: "+jarEntry,entry); //$NON-NLS-1$
        jarEntry = "helloWorld/HelloAspect.class"; //$NON-NLS-1$
        entry = zf.getEntry(jarEntry);
        assertNotNull("Couldn't find entry in created jar file for: "+jarEntry,entry); //$NON-NLS-1$
    }
    
    
	// not supported yet
//    public void testExportPluginAsInstallableUnit() throws Exception {
//        // Ignore these tests on Linux because not passing
//        if (System.getProperty("os.name").equals("Linux")) {
//            return;
//        }
//        
//        IProject project = createPredefinedProject("Hello World Plugin"); //$NON-NLS-1$
//        AJPluginExportWizard wiz = new AJPluginExportWizard() {
//            public IDialogSettings getDialogSettings() {
//                IDialogSettings settings = super.getDialogSettings();
//                settings.put(S_EXPORT_TYPE, String.valueOf(TYPE_INSTALL));
//                settings.put(S_INSTALL_DESTINATION + String.valueOf(0), exportFolder);
//                settings.put(S_JAR_FORMAT, false);
//                return settings;
//            }
//        };
//        wiz.init(JavaPlugin.getDefault().getWorkbench(),
//                new StructuredSelection(project));
//        File folder = new File(exportFolder);
//        if (folder.exists()) {
//            deleteDir(folder);
//            if (folder.exists()) {
//                fail("Couldn't delete export Folder"); //$NON-NLS-1$
//            }
//        }
//        
//        Shell shell = JavaPlugin.getActiveWorkbenchShell();
//        MyWizardDialog dialog = new MyWizardDialog(shell, wiz);
//        dialog.setBlockOnOpen(false);
//        dialog.create();
//        dialog.open();
//        dialog.finishPressed();
//
//        waitForJobsToComplete();
//
//        // now check export folder was created
//        if (!folder.exists()) {
//            fail("Export of plugin failed to create export folder: " + folder); //$NON-NLS-1$
//        }
//        File pluginsFolder = new File(folder, "plugins"); //$NON-NLS-1$
//        if (!pluginsFolder.exists()) {
//            fail("Export of plugin failed to create plugins sub-folder: " + pluginsFolder); //$NON-NLS-1$
//        }
//        File hwFolder = new File(pluginsFolder, "HelloWorld_1.0.0"); //$NON-NLS-1$
//        if (!hwFolder.exists()) {
//            fail("Export of plugin failed to create HelloWorld_1.0.0 sub-folder: " + hwFolder); //$NON-NLS-1$
//        }
//        File jar = new File(hwFolder, "HelloWorld.jar"); //$NON-NLS-1$
//        if (!jar.exists()) {
//            fail("Export of plugin failed to create HelloWorld.jar: " + jar); //$NON-NLS-1$
//        }
//        ZipFile zf = new ZipFile(jar);
//        String jarEntry = "helloWorld/HelloWorldPlugin.class"; //$NON-NLS-1$
//        ZipEntry entry = zf.getEntry(jarEntry);
//        // Macs seem to be generating zip files slightly differently
//        if (entry == null) {
//            entry = zf.getEntry("./" + jarEntry); //$NON-NLS-1$
//        }
//        assertNotNull("Couldn't find entry in created jar file for: "+jarEntry,entry); //$NON-NLS-1$
//        jarEntry = "helloWorld/HelloAspect.class"; //$NON-NLS-1$
//        entry = zf.getEntry(jarEntry);
//        assertNotNull("Couldn't find entry in created jar file for: "+jarEntry,entry); //$NON-NLS-1$
//        
//        if (! new File(folder, "artifacts.xml").exists()) {
//            fail("Could not find artifacts.xml");
//        }
//        if (! new File(folder, "content.xml").exists()) {
//            fail("Could not find content.xml");
//        }
//    }

    protected void tearDown() throws Exception {
        // Ignore these tests on Linux because not passing
//        if (System.getProperty("os.name").equals("Linux")) {
//            return;
//        }
        
		super.tearDown();
		if ((archivePath != null) && archivePath.length() > 0) {
			File zip = new File(archivePath);
			if (zip.exists()) {
				zip.delete();
				zip.deleteOnExit();
			}
		}
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
