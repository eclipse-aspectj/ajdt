/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.contribution.weaving.jdt.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.After;

/**
 * Mainly copied from AbstractJavaModelTests in org.eclipse.jdt.core.tests.model
 */
public class WeavingTestCase {
    
    protected IProgressMonitor monitor = new DumbProgressMonitor();

    @After()
	public void tearDown() throws Exception {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < allProjects.length; i++) {
			IProject project = allProjects[i];
			deleteProject(project,false);
		}
		allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < allProjects.length; i++) {
			IProject project = allProjects[i];
			deleteProject(project,true);
		}
	}

	/**
	 * Returns the IWorkspace this test suite is running on.
	 */
	public IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	public IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
	}
	
	
	protected IProject createPredefinedProject(final String projectName) throws CoreException, IOException {
		IJavaProject jp = setUpJavaProject(projectName);
		jp.setOption("org.eclipse.jdt.core.compiler.problem.missingSerialVersion", "ignore"); //$NON-NLS-1$ //$NON-NLS-2$
		jp.getProject().build(IncrementalProjectBuilder.FULL_BUILD,null);
		return jp.getProject();
	}
	
	/**
	 * Create a named project, optionally turn of some irritating options that can clog up the output and then build it
	 */
	protected IProject createPredefinedProject(final String projectName,boolean turnOffIrritatingOptions) throws CoreException, IOException {
		IJavaProject jp = setUpJavaProject(projectName);
		if (turnOffIrritatingOptions) {
			jp.setOption("org.eclipse.jdt.core.compiler.problem.missingSerialVersion", "ignore");//$NON-NLS-1$ //$NON-NLS-2$ // $NON-NLS-2$
			jp.setOption("org.eclipse.jdt.core.compiler.problem.rawTypeReference","ignore");//$NON-NLS-1$ //$NON-NLS-2$ // $NON-NLS-2$
			jp.setOption("org.eclipse.jdt.core.compiler.taskTags","");//$NON-NLS-1$ //$NON-NLS-2$ // $NON-NLS-2$
		}
		jp.getProject().build(IncrementalProjectBuilder.FULL_BUILD,null);
		return jp.getProject();		
	}
	
	
	protected IJavaProject setUpJavaProject(final String projectName) throws CoreException, IOException {
		return setUpJavaProject(projectName, "1.5"); //$NON-NLS-1$
	}
	
	protected IJavaProject setUpJavaProject(final String projectName, String compliance) throws CoreException, IOException {
		// copy files in project from source workspace to target workspace
		String sourceWorkspacePath = getSourceWorkspacePath();
		String targetWorkspacePath = getWorkspaceRoot().getLocation().toFile().getCanonicalPath();
		copyDirectory(new File(sourceWorkspacePath, projectName), new File(targetWorkspacePath, projectName));
		
		// create project
		final IProject project = getWorkspaceRoot().getProject(projectName);
		IWorkspaceRunnable populate = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null);
				project.open(null);
			}
		};
		getWorkspace().run(populate, null);
		
		IJavaProject javaProject = JavaCore.create(project);
		return javaProject;
	}
	
	   /**
     * Returns the OS path to the directory that contains this plugin.
     */
    protected String getPluginDirectoryPath() {
        try {
            URL platformURL = Platform.getBundle("org.eclipse.contribution.weaving.jdt.tests").getEntry("/"); //$NON-NLS-1$ //$NON-NLS-2$
            return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String getSourceWorkspacePath() {
        return getPluginDirectoryPath() +  java.io.File.separator + "workspace"; //$NON-NLS-1$
    }

	
	
	// A dumb progressmonitor we can use - if we dont pass one it may create a UI one...
	static class DumbProgressMonitor implements IProgressMonitor {

		public void beginTask(String name, int totalWork) {/*dontcare*/}

		public void done() {/*dontcare*/}

		public void internalWorked(double work) {/*dontcare*/}

		public boolean isCanceled() {/*dontcare*/return false;}

		public void setCanceled(boolean value) {/*dontcare*/}

		public void setTaskName(String name) {/*dontcare*/}

		public void subTask(String name) {/*dontcare*/}

		public void worked(int work) {/*dontcare*/}
		
	}

	/**
	 * Wait for autobuild notification to occur
	 */
	public static void waitForAutoBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, new DumbProgressMonitor());
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);
	}
	
	public static void waitForManualBuild() {
		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, new DumbProgressMonitor());
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);		
	}
	
	/**
	 * Copy the given source directory (and all its contents) to the given target directory.
	 */
	protected void copyDirectory(File source, File target) throws IOException {
		if (!target.exists()) {
			target.mkdirs();
		}
		File[] files = source.listFiles();
		if (files == null) return;
		for (int i = 0; i < files.length; i++) {
			File sourceChild = files[i];
			String name =  sourceChild.getName();
			if (name.equals("CVS")) continue; //$NON-NLS-1$
			File targetChild = new File(target, name);
			if (sourceChild.isDirectory()) {
				copyDirectory(sourceChild, targetChild);
			} else {
				copy(sourceChild, targetChild);
			}
		}
	}
	
	/**
	 * Copy file from src (path to the original file) to dest (path to the destination file).
	 */
	public void copy(File src, File dest) throws IOException {
		// read source bytes
		byte[] srcBytes = this.read(src);
		
		// write bytes to dest
		FileOutputStream out = new FileOutputStream(dest);
		out.write(srcBytes);
		out.close();
	}
	
	public byte[] read(java.io.File file) throws java.io.IOException {
		int fileLength;
		byte[] fileBytes = new byte[fileLength = (int) file.length()];
		java.io.FileInputStream stream = new java.io.FileInputStream(file);
		int bytesRead = 0;
		int lastReadSize = 0;
		while ((lastReadSize != -1) && (bytesRead != fileLength)) {
			lastReadSize = stream.read(fileBytes, bytesRead, fileLength - bytesRead);
			bytesRead += lastReadSize;
		}
		stream.close();
		return fileBytes;
	}
	
	
	protected IProject getProject(String project) {
		return getWorkspaceRoot().getProject(project);
	}

	protected void deleteProject(IProject project, boolean force) throws CoreException {
		if (project.exists() && !project.isOpen()) { // force opening so that project can be deleted without logging (see bug 23629)
			project.open(null);
		}
		deleteResource(project,force);
	}
	
	protected void deleteProject(String projectName) throws CoreException {
		deleteProject(this.getProject(projectName),true);
	}
	
	/**
	 * Delete this resource.
	 */
	public void deleteResource(IResource resource, boolean force) throws CoreException {
		waitForManualBuild();
		waitForAutoBuild();
		CoreException lastException = null;
		try {
			resource.delete(false, null);
		} catch (CoreException e) {
			lastException = e;
			// just print for info
			System.out.println("(CoreException): " + e.getMessage() + " Resource " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$
			e.printStackTrace();
		} catch (IllegalArgumentException iae) {
			// just print for info
			System.out.println("(IllegalArgumentException): " + iae.getMessage() + ", resource " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!force) {
			return;
		}
		int retryCount = 10; // wait 1 minute at most
		while (resource.isAccessible() && --retryCount >= 0) {
			waitForAutoBuild();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			try {
				resource.delete(true, null);
			} catch (CoreException e) {
				lastException = e;
				// just print for info
				System.out.println("(CoreException) Retry "+retryCount+": "+ e.getMessage() + ", resource " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} catch (IllegalArgumentException iae) {
				// just print for info
				System.out.println("(IllegalArgumentException) Retry "+retryCount+": "+ iae.getMessage() + ", resource " + resource.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		if (!resource.isAccessible()) return;
		System.err.println("Failed to delete " + resource.getFullPath()); //$NON-NLS-1$
		if (lastException != null) {
			throw lastException;
		}
	}
	
   protected void waitForJobsToComplete(){
        SynchronizationUtils.joinBackgroudActivities();
    }

}

//Adapted from org.eclipse.jdt.ui.tests.performance.JdtPerformanceTestCase
class SynchronizationUtils {

        
    public static void joinBackgroudActivities()  {
        // Join Building
        boolean interrupted= true;
        while (interrupted) {
            try {
                Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);
                interrupted= false;
            } catch (InterruptedException e) {
                interrupted= true;
            }
        }
        boolean wasInterrupted = false;
        do {
            try {
                Job.getJobManager().join(ResourcesPlugin.FAMILY_MANUAL_BUILD, null);
                wasInterrupted = false;
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                wasInterrupted = true;
            }
        } while (wasInterrupted);   
        // Join jobs
        joinJobs(100, 0, 500);
    }


    private static boolean joinJobs(long minTime, long maxTime, long intervalTime) {
        long startTime= System.currentTimeMillis() + minTime;
        runEventQueue();
        while (System.currentTimeMillis() < startTime)
            runEventQueue(intervalTime);
        
        long endTime= maxTime > 0  && maxTime < Long.MAX_VALUE ? System.currentTimeMillis() + maxTime : Long.MAX_VALUE;
        boolean calm= allJobsQuiet();
        while (!calm && System.currentTimeMillis() < endTime) {
            runEventQueue(intervalTime);
            calm= allJobsQuiet();
        }
        return calm;
    }
    
    private static void sleep(int intervalTime) {
        try {
            Thread.sleep(intervalTime);
        } catch (InterruptedException e) {
        }
    }
    
    private static boolean allJobsQuiet() {
        IJobManager jobManager= Job.getJobManager();
        Job[] jobs= jobManager.find(null);
        for (int i= 0; i < jobs.length; i++) {
            Job job= jobs[i];
            int state= job.getState();
            //ignore jobs we don't care about
            if (!job.getName().equals("Flush Cache Job") &&  //$NON-NLS-1$
                    !job.getName().equals("Usage Data Event consumer") &&  //$NON-NLS-1$
                    (state == Job.RUNNING || state == Job.WAITING)) {
                return false;
            }
        }
        return true;
    }
    
    private static void runEventQueue() {
        IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null)
            runEventQueue(window.getShell());
    }
    
    private static void runEventQueue(Shell shell) {
        try {
            while (shell.getDisplay().readAndDispatch()) {
                // do nothing
            }
        } catch (SWTException e) {
            System.err.println(e);
        }
    }
    
    private static void runEventQueue(long minTime) {
        long nextCheck= System.currentTimeMillis() + minTime;
        while (System.currentTimeMillis() < nextCheck) {
            runEventQueue();
            sleep(1);
        }
    }
}
