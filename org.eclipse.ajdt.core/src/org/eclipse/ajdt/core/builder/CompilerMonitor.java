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
package org.eclipse.ajdt.core.builder;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 */
public class CompilerMonitor implements IAJCompilerMonitor {

    private IProgressMonitor monitor = null;

    private long compileStartTime;

    /**
     * Is this CompilerMonitor instance currently 'in use' ?
     */
    private boolean compilationInProgress = false;

	public boolean finished() {
		return !compilationInProgress;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajde.BuildProgressMonitor#start(java.lang.String)
	 */
	public void start(String configFile) {
		System.out.println("start core compiler monitor");
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajde.BuildProgressMonitor#setProgressText(java.lang.String)
	 */
	public void setProgressText(String text) {
		System.out.println("set progress: "+text);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajde.BuildProgressMonitor#setProgressBarVal(int)
	 */
	public void setProgressBarVal(int newVal) {
		System.out.println("set progress bar:"+newVal);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajde.BuildProgressMonitor#incrementProgressBarVal()
	 */
	public void incrementProgressBarVal() {
		System.out.println("increment progress bar");
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajde.BuildProgressMonitor#setProgressBarMax(int)
	 */
	public void setProgressBarMax(int maxVal) {
		System.out.println("set progress bar max: "+maxVal);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajde.BuildProgressMonitor#getProgressBarMax()
	 */
	public int getProgressBarMax() {
		System.out.println("get progress bar max");
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.ajde.BuildProgressMonitor#finish()
	 */
	public void finish() {
		compilationInProgress = false;
		System.out.println("finish called");
	}
	
    /**
     * Called from the Builder to set up the compiler for a new build.
     */
    public void prepare(IProject project, List buildList,
            IProgressMonitor eclipseMonitor) {
        monitor = eclipseMonitor;
        if (monitor != null) {
//            monitor.beginTask(AspectJUIPlugin.getResourceString("ajCompilation"),
//                    AspectJUIPlugin.PROGRESS_MONITOR_MAX);
        	monitor.beginTask("new compile",10);
        }

        compileStartTime = System.currentTimeMillis();
        System.out.println("compilation now in progress");
        compilationInProgress = true;
    }
    
    public static void clearOtherProjectMarkers(IProject p) {
    
    }
    
    public static void showOutstandingProblems() {
    	
    }
}
