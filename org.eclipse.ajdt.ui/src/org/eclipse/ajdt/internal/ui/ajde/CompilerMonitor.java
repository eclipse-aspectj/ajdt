/**********************************************************************
 Copyright (c) 2002, 2004 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made  available under the terms of the Eclipse Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/epl-v10.html
 Contributors:
 Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 Matt Chapman - add support for Go To Related Location entries
 ...
 **********************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import java.util.List;

import org.aspectj.weaver.WeaverMetrics;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.TimerLogEvent;
import org.eclipse.ajdt.core.builder.IAJCompilerMonitor;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

/**
 * The compiler monitor interface is used by the AspectJ builder. Just before
 * delegating the compilation of the source files to the 'external' ajc
 * compiler, the builder prepares a compiler monitor for this perspective. The
 * compiler monitor is then called back through the CompilerMessages and
 * CompileProgressMonitor interfaces. When the final message comes from the AJC
 * compiler (it should be finishProgress()) then the compiler monitor sets the
 * finished flag which is used by the builder to determine when it is safe to
 * exit the build function. This syncrhonization appears to be necessary because
 * the compilation is done asynchronously once the AJC compiler is started.
 */
public class CompilerMonitor implements IAJCompilerMonitor {

    private boolean reportedCompiledMessages;

    private boolean reportedWovenMessages;

    /**
     * Which Eclipse IProgressMonitor should this CompilerMonitor keep updating?
     */
    private IProgressMonitor monitor = null;

    /**
     * Is this CompilerMonitor instance currently 'in use' ?
     */
    private boolean compilationInProgress = false;

    /**
     * Monitor progress against Ajde max
     */
    private int currentAjdeProgress;

    /**
     * Determine if a linked folder exists in the buildList
     */
    private boolean linked;

    /**
     * Project being built
     */
    private IProject project;
    
	/**
	 * Indicates whether the build is for one particular AspectJ project only
	 * (i.e. caused by the build action button being clicked) or else is part of
	 * a build of all projects in workspace (i.e. caused by a rebuild-all
	 * action).
	 */
	public static boolean isLocalBuild = false;
	
    /**
     * Called from the Builder to set up the compiler for a new build.
     */
    public void prepare(IProject project, List buildList,
            IProgressMonitor eclipseMonitor) {
    	this.project = project;
    	
        //check if the folder contains linked resources
        linked = false;
        IResource[] res = null;

        try {
            res = project.members();
        } catch (CoreException e) {
            //should not occur but for some reason one of the following it
            // true:
            //1. This resource does not exist.
            //2. includePhantoms is false and resource does not exist.
            //3. includePhantoms is false and this resource is a project that
            // is not open.
        }

        for (int i = 0; (linked == false) && (i < res.length); i++) {
            if (res[i].getType() == IResource.FOLDER) {
                linked = res[i].isLinked();
            }
        }

        monitor = eclipseMonitor;
        if (monitor != null) {
            monitor.beginTask(UIMessages.ajCompilation,
                    AspectJUIPlugin.PROGRESS_MONITOR_MAX);
        }

        AJLog.logStart(TimerLogEvent.FIRST_COMPILED);
        AJLog.logStart(TimerLogEvent.FIRST_WOVEN);
        reportedCompiledMessages = false;
        reportedWovenMessages = false;
        compilationInProgress = true;

    }

    /** ******* BuildProgressMonitor interface implementation ************* START */

    /*
     * Methods are called from Ajde in the following sequence: - start( ) -
     * clearTasks( ) - setProgressBarMax( max ) - followed by any number of
     * setProgressBarVal(), incrementProgressBarVal( ) and setProgressText( )
     * calls in any order - finish( )
     */

    /**
     * Ajde has started a compile
     */
    public void start(final String configFile) {
        currentAjdeProgress = 0;
        if (monitor != null) {
            AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (monitor != null) {
                        if (isLocalBuild) {
                        	monitor.setTaskName(NLS.bind(UIMessages.CompilerMonitor_building_Project,project.getName()));
                        }
                    }// end if
                }// end run method
            });
        }
    }

    /**
	 * Ajde wishes to display information about the progress of the compilation.
	 */
    public void setProgressText(String text) {
        if (!reportedCompiledMessages && text.startsWith("compiled: ")) { //$NON-NLS-1$
            reportedCompiledMessages = true;
            AJLog.logEnd(AJLog.COMPILER, TimerLogEvent.FIRST_COMPILED);
        }
        if (!reportedWovenMessages && text.startsWith("woven ")) { //$NON-NLS-1$
            reportedWovenMessages = true;
            AJLog.logEnd(AJLog.COMPILER, TimerLogEvent.FIRST_WOVEN);
        }

        // Three messages are caught here:
        //   compiled:
        //   woven class
        //   woven aspect
        // Each indicates that something has been processed and so will be
        // reported on later.  For this reason we remember that it has been
        // processed so that we can remove markers for it before adding
        // any new ones.  
        // FIXME ASC18022005 this isnt the nicest way to do this, it would be better
        // to ask the state what changed...
        if (text.startsWith("compiled: ") || text.startsWith("woven ")) { //$NON-NLS-1$ //$NON-NLS-2$
            // If a project contains a 'srclink' and that link is to a directory
            // that isn't defined in another eclipse project, then we may get 
        	// resource paths here that cannot be found in eclipse. So the 
        	// entry added to affectedResources will be null. However, as
            // this code is only used to ensure we tidy up markers, that does
            // not matter - if it does not exist, it cannot have 
        	// outstanding markers.
            IPath resourcePath = null;
            if (text.startsWith("compiled: ")) { //$NON-NLS-1$
            	resourcePath = new Path(text.substring(10));
            } else {
            	// woven messages look like this: 'woven class XXXX (from c:\fullpathhere)'
            	int fromLoc = text.indexOf("from "); //$NON-NLS-1$
            	int endLoc = text.lastIndexOf(")"); //$NON-NLS-1$
            	if (fromLoc!=-1 && endLoc>fromLoc) { // guards guards
            		resourcePath = new Path(text.substring(fromLoc+5,endLoc));
            	}
            }
            IWorkspaceRoot workspaceRoot = AspectJPlugin.getWorkspace()
                    .getRoot();

            if (linked) {
                IFile[] files = workspaceRoot
                        .findFilesForLocation(resourcePath);
                for (int i = 0; i < files.length; i++) {
                	CompilerTaskListManager.getInstance().addAffectedResource(files[i]);
                }
            } else {
                IFile file = workspaceRoot.getFileForLocation(resourcePath);
                if (file == null) {
                	AJLog.log(AJLog.COMPILER,"Processing progress message: Can't find eclipse resource for file with path " //$NON-NLS-1$
                                    + text);
                } else {
                    CompilerTaskListManager.getInstance().addAffectedResource(file);
                }
            }
        }

        final String amendedText = removePrefix(text);
        AJLog.log(AJLog.COMPILER_PROGRESS,"AJC: " + text); //$NON-NLS-1$ //$NON-NLS-2$
        if (monitor != null) {
            AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (monitor != null)
                        monitor.subTask(amendedText);
                }
            });
        }

    }

    /**
     * Ajde wishes to advance the progress bar to this absolute value
     */
    public void setProgressBarVal(int newVal) {
        if (newVal >= currentAjdeProgress) {
            incrementProgressBarVal("setProgressBarVal() delegating to "); //$NON-NLS-1$
        }
    }

    /**
     * Ajde informs us that the compiler is assuming a maximum progress bar
     * value of <code>maxVal</code>
     */
    public void setProgressBarMax(int maxVal) {
    }

    public void incrementProgressBarVal() {
        incrementProgressBarVal("AJDE Callback:"); //$NON-NLS-1$
    }

    /**
     * Ajde asks us to increment the progress bar by one
     */
    public void incrementProgressBarVal(String caller) {
        currentAjdeProgress++;
        // Bug 22258 - Reworked internals of run() method, it used to be
        // responsible for
        // increasing currentProgress but I've changed it since if anything
        // causes the execution
        // of the thread to hang then currentProgress doesn't increase and
        // setProgressBarVal()
        // just loops calling this routine.
        if (monitor != null) {
            AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (monitor != null)
                        monitor.worked(1);
                }
            });
        }
    }

    /**
     * Ajde asks our preference for the maximum value of the progress bar
     */
    public int getProgressBarMax() {
        return AspectJUIPlugin.PROGRESS_MONITOR_MAX;
    }

    /**
     * Ajde informs us that a compilation has finished. We may under some
     * circumstances get multiple calls to finish. This method is marked
     * synchronized to let one finish finish before another finish gets in!
     */
    public synchronized void finish(boolean wasFullBuild) {
        AJLog.log(AJLog.COMPILER,"AJDE Callback: finish()"); //$NON-NLS-1$
        // AMC - moved this next monitor var set outside of thread -
        // this status change must be instantly visible
        compilationInProgress = false;
        WeaverMetrics.reset();

        if (AspectJUIPlugin.getDefault().getDisplay().isDisposed())
        	AJLog.log("Not finishing with bpm, display is disposed!"); //$NON-NLS-1$
        else
            AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
                public void run() {

                    if (monitor != null) {
                        // ask the project to perform a refresh to pick up the
                        // newly generated classfiles - bug 30462
                        // It think during the processing in refreshOutputDir -
                        // monitor can go null...
                        //                    if (monitor!=null) monitor.setTaskName("");
                        if (monitor != null)
                            monitor.worked(AspectJUIPlugin.PROGRESS_MONITOR_MAX);
                        if (monitor != null)
                            monitor.done();
                        monitor = null;
                    }
                }
            });
    }

    /**
     * Has the most recent compile finished?
     */
    public boolean finished() {
        return !compilationInProgress;
    }

    /** ******* CompileProgressMonitor interface implementation ************* END */

    /**
     * A compiler message has been emitted, which potentially includes the fully
     * qualifed path of a resource. Chop it down to just the project-relative
     * portion.
     */
    private String removePrefix(String msg) {
        String ret = msg;
        IProject p = AspectJPlugin.getDefault().getCurrentProject();
        
        //Bug 150936                   
        if (p == null || p.getLocation() == null) {
        	AJLog.log("Could not find project location, " + p); //$NON-NLS-1$
        	return ret;
        };
        	
        String projectLocation = p.getLocation().toOSString() + "\\"; //$NON-NLS-1$
        
        if (msg.indexOf(projectLocation) != -1) {
            ret = msg.substring(0, msg.indexOf(projectLocation))
                    + msg.substring(msg.indexOf(projectLocation)
                            + projectLocation.length());
        } else {
            projectLocation = projectLocation.replace('\\', '/');
            if (msg.indexOf(projectLocation) != -1) {
                ret = msg.substring(0, msg.indexOf(projectLocation))
                        + msg.substring(msg.indexOf(projectLocation)
                                + projectLocation.length());
            }
        }

        // special cases...
        // this message is too long to be meaningful
        if (ret.startsWith("might need to weave")) { //$NON-NLS-1$
            ret = UIMessages.CompilerMonitor_weaving;
        }
        // we always get this next one, and it seems to be nonsense
        if (ret.startsWith("directory classpath entry does not exist: null")) { //$NON-NLS-1$
            ret = ""; //$NON-NLS-1$
        }

        // chop (from x\y\z\a\b\C.java) to just (C.java)
        if (ret.startsWith("woven") && ret.indexOf("(from") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
            int loc = ret.indexOf("(from"); //$NON-NLS-1$
            if (loc != -1) {
                String fromPiece = ret.substring(loc);
                int lastSlash = fromPiece.lastIndexOf("/"); //$NON-NLS-1$
                if (lastSlash == -1)
                    lastSlash = fromPiece.lastIndexOf("\\"); //$NON-NLS-1$
                if (lastSlash != -1) {
                    fromPiece = fromPiece.substring(lastSlash + 1);
                    ret = ret.substring(0, loc) + " (" + fromPiece; //$NON-NLS-1$
                } else {
                    int space = fromPiece.indexOf(" "); //$NON-NLS-1$
                    if (space != -1)
                        ret = ret.substring(0, loc) + " (" //$NON-NLS-1$
                                + fromPiece.substring(space + 1);
                }
            }
        }
        return ret;
    }
 
}