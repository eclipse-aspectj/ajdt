/**********************************************************************
 Copyright (c) 2002, 2004 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made  available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 Contributors:
 Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 Matt Chapman - add support for Go To Related Location entries
 ...
 **********************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import java.util.List;

import org.aspectj.weaver.WeaverMetrics;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.builder.IAJCompilerMonitor;
import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

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

    private long compileStartTime;

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
     * Ratio of max value used by Ajde to MONITOR_MAX
     */
    private float ajdeMonitorMaxRatio = 1.0f;

    /**
     * Monitor progress against Ajde max
     */
    private int currentAjdeProgress;

    /**
     * Determine if a linked folder exists in the buildList
     */
    private boolean linked;

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

        if (AspectJUIPlugin.DEBUG_COMPILER) {
            System.out.println("CompilerMonitor.prepare called: IPM is "
                    + (eclipseMonitor == null ? "Null" : "Not-Null"));
        }

        monitor = eclipseMonitor;
        if (monitor != null) {
            monitor.beginTask(AspectJUIPlugin.getResourceString("ajCompilation"),
                    AspectJUIPlugin.PROGRESS_MONITOR_MAX);
        }

        compileStartTime = System.currentTimeMillis();
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
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err
                    .println("AJDE Callback: CompileProgressMonitor.start() called");
        
        currentAjdeProgress = 0;
        if (monitor != null) {
            AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (monitor != null) {
                        if (isLocalBuild) {
                            ProjectBuildConfigurator pbc = BuildConfigurator
                                    .getBuildConfigurator()
                                    .getActiveProjectBuildConfigurator();
                            if (pbc != null) {
                                String configName = pbc
                                        .getActiveBuildConfiguration()
                                        .getName();
                                monitor.setTaskName("Building project with \'"
                                        + configName + "\' configuration");
                            }
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
        if (!reportedCompiledMessages && text.startsWith("compiled: ")) {
            reportedCompiledMessages = true;
            AJDTEventTrace.generalEvent("Time to first 'compiled:' message: "
                    + (System.currentTimeMillis() - compileStartTime) + "ms");
        }
        if (!reportedWovenMessages && text.startsWith("woven ")) {
            reportedWovenMessages = true;
            AJDTEventTrace.generalEvent("Time to first 'woven' message: "
                    + (System.currentTimeMillis() - compileStartTime) + "ms");
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
        if (text.startsWith("compiled: ") || text.startsWith("woven ")) {
            // If a project contains a 'srclink' and that link is to a directory
            // that isn't defined in another eclipse project, then we may get 
        	// resource paths here that cannot be found in eclipse. So the 
        	// entry added to affectedResources will be null. However, as
            // this code is only used to ensure we tidy up markers, that does
            // not matter - if it does not exist, it cannot have 
        	// outstanding markers.
            IPath resourcePath = null;
            if (text.startsWith("compiled: ")) {
            	resourcePath = new Path(text.substring(10));
            } else {
            	// woven messages look like this: 'woven class XXXX (from c:\fullpathhere)'
            	int fromLoc = text.indexOf("from ");
            	int endLoc = text.lastIndexOf(")");
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
                    AJDTEventTrace
                            .generalEvent("Processing progress message: Can't find eclipse resource for file with path "
                                    + text);
                } else {
                    CompilerTaskListManager.getInstance().addAffectedResource(file);
                }
            }
        }

        final String amendedText = removePrefix(text);
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("AJDE Callback: setProgressText(" + text + ")");
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
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("AJDE Callback: setProgressBarVal(" + newVal
                    + ")");

        if (newVal >= currentAjdeProgress) {
            incrementProgressBarVal("setProgressBarVal() delegating to ");
        }
    }

    /**
     * Ajde informs us that the compiler is assuming a maximum progress bar
     * value of <code>maxVal</code>
     */
    public void setProgressBarMax(int maxVal) {
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("AJDE Callback: setProgressBarMax(" + maxVal
                    + ")");
        ajdeMonitorMaxRatio = ((float) maxVal)
                / AspectJUIPlugin.PROGRESS_MONITOR_MAX;
    }

    public void incrementProgressBarVal() {
        incrementProgressBarVal("AJDE Callback:");
    }

    /**
     * Ajde asks us to increment the progress bar by one
     */
    public void incrementProgressBarVal(String caller) {
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println(caller + " incrementProgressBarVal():"
                    + currentAjdeProgress);
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
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("AJDE Callback: getProgressBarMax()");
        return AspectJUIPlugin.PROGRESS_MONITOR_MAX;
    }

    /**
     * Ajde informs us that a compilation has finished. We may under some
     * circumstances get multiple calls to finish. This method is marked
     * synchronized to let one finish finish before another finish gets in!
     */
    public synchronized void finish() {
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("AJDE Callback: finish()");
        // AMC - moved this next monitor var set outside of thread -
        // this status change must be instantly visible
        compilationInProgress = false;

        // Summarize what happened during weaving... ASC170205 - commented out for now - not that useful!
//        AJDTEventTrace.generalEvent("Weaver stress level: ");
//        int fastMatchOnTypeMaybe = (WeaverMetrics.fastMatchOnTypeAttempted
//                - WeaverMetrics.fastMatchOnTypeTrue - WeaverMetrics.fastMatchOnTypeFalse);
//        AJDTEventTrace.generalEvent("Fast fast matching (type level) of #"
//                + WeaverMetrics.fastMatchOnTypeAttempted + " types "
//                + "resulting in us dismissing "
//                + WeaverMetrics.fastMatchOnTypeFalse);
//        //System.err.println(" YES/NO/MAYBE =
//        // "+Metrics.fastMatchOnTypeTrue+"/"+Metrics.fastMatchOnTypeFalse+"/"+Metrics.fastMatchOnTypeMaybe);
//        //		int fastMatchMaybe = (WeaverMetrics.fastMatchOnShadowsAttempted
//        //				- WeaverMetrics.fastMatchOnShadowsFalse -
//        // WeaverMetrics.fastMatchOnShadowsTrue);
//        AJDTEventTrace.generalEvent("Fast matching within the remaining #"
//                + (WeaverMetrics.fastMatchOnTypeTrue + fastMatchOnTypeMaybe)
//                + " types, " + "we fast matched on #"
//                + WeaverMetrics.fastMatchOnShadowsAttempted
//                + " shadows and dismissed #"
//                + WeaverMetrics.fastMatchOnShadowsFalse);
//        // System.err.println(" YES/NO/MAYBE =
//        // "+Metrics.fastMatchTrue+"/"+Metrics.fastMatchFalse+"/"+fastMatchMaybe);
//        AJDTEventTrace.generalEvent("Slow match then attempted on #"
//                + WeaverMetrics.matchAttempted + " shadows of which "
//                + WeaverMetrics.matchTrue + " successful");
        WeaverMetrics.reset();

        if (AspectJUIPlugin.getDefault().getDisplay().isDisposed())
            System.err.println("Not finishing with bpm, display is disposed!");
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
                    //monitoringInProgress = false;
                    // Attempting this here will lock the GUI up if there is
                    // another
                    // compile about to
                    // be done. So it has been moved to an equivalent call
                    // (showOutstandingProblems())
                    // in the Builder.java file.
                    //showMessages();

                    // this next piece should really be handled by an aspect
                    //				AspectVisualiserPlugin.getDefault().refreshView();

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
        String projectLocation = p.getLocation().toOSString() + "\\";
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
        if (ret.startsWith("might need to weave")) {
            ret = "weaving ...";
        }
        // we always get this next one, and it seems to be nonsense
        if (ret.startsWith("directory classpath entry does not exist: null")) {
            ret = "";
        }

        // chop (from x\y\z\a\b\C.java) to just (C.java)
        if (ret.startsWith("woven") && ret.indexOf("(from") != -1) {
            int loc = ret.indexOf("(from");
            if (loc != -1) {
                String fromPiece = ret.substring(loc);
                int lastSlash = fromPiece.lastIndexOf("/");
                if (lastSlash == -1)
                    lastSlash = fromPiece.lastIndexOf("\\");
                if (lastSlash != -1) {
                    fromPiece = fromPiece.substring(lastSlash + 1);
                    ret = ret.substring(0, loc) + " (" + fromPiece;
                } else {
                    int space = fromPiece.indexOf(" ");
                    if (space != -1)
                        ret = ret.substring(0, loc) + " ("
                                + fromPiece.substring(space + 1);
                }
            }
        }
        return ret;
    }

//    /**
//     * Inner class used to ensure marker creation and attribute setting occur as
//     * an atomic unit (otherwise tick marks do not appear correctly in editor
//     * margin.
//     */
//    class ProblemAdder implements IWorkspaceRunnable {
//        private Iterator problemIterator;
//
//        public ProblemAdder(Iterator it) {
//            this.problemIterator = it;
//        }
//
//        public void run(IProgressMonitor monitor) {
//            while (problemIterator.hasNext()) {
//                ProblemTracker p = (ProblemTracker) problemIterator.next();
//                IResource ir = null;
//                if (p.location != null) {
//                    String loc = p.location.getSourceFile().getAbsolutePath();
//                    ir = AspectJUIPlugin.getDefault().getAjdtProjectProperties()
//                            .findResource(loc);
//                    if (ir == null) {
//                        ir = AspectJUIPlugin.getDefault().getCurrentProject();
//                    }
//                    try {
//                        IMarker im = ir.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
//                        if (p.location.getLine() > 0) {
//                            im.setAttribute(IMarker.LINE_NUMBER, p.location
//                                    .getLine());
//                        }
//                        if (p.location.getColumn() > 0) {
//                            //im.setAttribute(IMarker.CHAR_START ,
//                            // p.location.getColumnNumber());
//                            //im.setAttribute(IMarker.CHAR_END,...);
//                        }
//
//                        if (p.kind == IMessage.ERROR) {
//                            im.setAttribute(IMarker.SEVERITY,
//                                    IMarker.SEVERITY_ERROR);
//                        } else if (p.kind == IMessage.WARNING) {
//                            im.setAttribute(IMarker.SEVERITY,
//                                    IMarker.SEVERITY_WARNING);
//                        } else {
//                            im.setAttribute(IMarker.SEVERITY,
//                                    IMarker.SEVERITY_INFO);
//                        }
//
//                        // FIXME: Remove this horrid hack.
//                        // Hack the filename off the front and the line number
//                        // off the end
//                        if (p.message.indexOf("\":") != -1
//                                && p.message.indexOf(", at line") != -1) {
//                            String hackedMessage = p.message
//                                    .substring(p.message.indexOf("\":") + 2);
//                            hackedMessage = hackedMessage.substring(0,
//                                    hackedMessage.indexOf(", at line"));
//                            im.setAttribute(IMarker.MESSAGE, hackedMessage);
//                        } else {
//                            im.setAttribute(IMarker.MESSAGE, p.message);
//                        }
//                    } catch (Exception e) {
//                        AspectJUIPlugin.getDefault().getErrorHandler()
//                                .handleError("Error creating marker", e);
//                    }
//                }
//            }
//            clearTasks();
//        } // end of run method
//
//    }; // end of ProblemAdder class
//


 
}