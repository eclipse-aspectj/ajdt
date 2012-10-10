/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.contribution.jdt.debug;

import org.eclipse.contribution.jdt.JDTWeavingPlugin;
import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdi.internal.request.EventRequestImpl;
import org.eclipse.jdi.internal.request.StepRequestImpl;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.internal.debug.core.breakpoints.ConditionalBreakpointHandler;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import com.sun.jdi.Location;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.InternalException;
/**
 * The pointcuts and advice that provides hooks for other plugins into the JDT debug infrastructure
 * 
 * @author Andrew Eisenberg
 * @created Oct 28, 2010
 */
public privileged aspect DebugHooksAspect {

    private static final IWorkspaceRoot WORKSPACE_ROOT = ResourcesPlugin
            .getWorkspace().getRoot();

    /**
     * will be null when outside of STS (ie- Groovy support not installed)
     */
    private DebugAdapter adapter = DebugAdapter.getInstance();

    /**
     * This pointcut is reached when the debugged application stops at a new location.
     * 
     * This provides client plugins the capability to further step through the application. this
     * allows client plugins to provide more precise step filters than what is normally available.
     */
    pointcut arrivedAtNewLocation(Location location, JDIThread.StepHandler handler, JDIDebugTarget target) : 
        execution(protected boolean JDIThread.StepHandler.locationShouldBeFiltered(Location) throws DebugException) &&
        args(location) && this(handler) && cflowbelow(eventHandling(target));
    
    /**
     * In order to get the current thread for the extra step filtering, we need to use a bit
     * of a wormhole pattern to grab the {@link JDIDebugTarget}
     */
    pointcut eventHandling(JDIDebugTarget target) : execution(public boolean JDIThread.StepHandler.handleEvent(Event, JDIDebugTarget, boolean, EventSet))
      && args(*, target, *, *);

    boolean around(Location location, JDIThread.StepHandler handler, JDIDebugTarget target) : arrivedAtNewLocation(location, handler, target) {
        // if we have the provider and the current thread is launched from an interesting project
        // and the provider
        // determines that the step should be performed, then doit.
        // otherwise, proceed as usual
        try {
            
            StepRequestImpl request = (StepRequestImpl) ((JDIThread.StepHandler) handler).getStepRequest();
            if (request != null) {
                IThread thread = target.findThread(request.thread());
                IDebugProvider provider = adapter.getProvider();
                if ((provider != null && isInterestingLaunch(thread) && thread.isStepping() && provider
                        .shouldPerformExtraStep(location))) {
                    return true;  // do not proceed
                }
            }
        } catch (DebugException e) {
            JDTWeavingPlugin.logException(e);
        }
        return proceed(location, handler, target);
    }

    /**
     * This pointcut is reached when an evaluation is performed during debugging/
     * 
     * Note that there is a second method that may need to be advised
     * @param snippet
     * @param object
     * @param frame
     * @param listener
     */
    pointcut performEvaluation(String snippet, IJavaObject object,
            IJavaThread thread, IEvaluationListener listener,
            int evaluationDetail, boolean hitBreakpoints, ASTEvaluationEngine engine) 
                : execution(public void ASTEvaluationEngine.evaluate(String, IJavaThread, IEvaluationListener, int, boolean) throws DebugException) && 
                  args(snippet, object, thread, listener, evaluationDetail, hitBreakpoints) && this(engine);

    void around(String snippet, IJavaObject object, IJavaThread thread,
            IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints, ASTEvaluationEngine engine) : performEvaluation(snippet, object,
                    thread, listener, evaluationDetail, hitBreakpoints, engine) {
        try {
            if (maybePerformEvaluation(snippet, object, (IJavaStackFrame) thread.getStackFrames()[0], listener, evaluationDetail, hitBreakpoints, engine)) {
                return; // do not proceed
            }
        } catch (DebugException e) {
            JDTWeavingPlugin.logException(e);
        }
        proceed(snippet, object, thread, listener, evaluationDetail, hitBreakpoints, engine);
    }
    
    /**
     * This pointcut is reached when an evaluation is performed during debugging/
     * 
     * Note that there is a second method that may need to be advised
     * @param snippet
     * @param object
     * @param frame
     * @param listener
     */
    pointcut performEvaluationWithThread(String snippet, IJavaStackFrame frame, IEvaluationListener listener,
            int evaluationDetail, boolean hitBreakpoints, ASTEvaluationEngine engine) 
                : execution(public void ASTEvaluationEngine.evaluate(String, IJavaStackFrame, IEvaluationListener, int, boolean) throws DebugException) && 
                  args(snippet, frame, listener, evaluationDetail, hitBreakpoints) && this(engine);

    void around(String snippet, IJavaStackFrame frame,
            IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints, ASTEvaluationEngine engine) :
                performEvaluationWithThread(snippet, frame, listener, evaluationDetail, hitBreakpoints, engine) {
        try {
            IJavaObject object = frame.getThis();
            if (maybePerformEvaluation(snippet, object, frame, listener, evaluationDetail, hitBreakpoints, engine)) {
                return; // do not proceed
            }
        } catch (DebugException e) {
            JDTWeavingPlugin.logException(e);
        }
        proceed(snippet, frame, listener, evaluationDetail, hitBreakpoints, engine);
    }
    
    /**
     * Capture enabling of step requests
     */
    pointcut stepRequestEnabled(StepRequestImpl stepRequest) : execution(public void EventRequestImpl.enable()) && this(stepRequest);
    
    private static final int MAX_RETRY = 50;
    /**
     * There is a problem in that when performing extra step filters, occasionally
     * an exception is thrown, but on a retry to perform the step request, 
     * then the extra step is successful.
     * 
     * Try 50 times before failing.  This seems to work
     * This is the same problem as described in:
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=297071
     * And it may be mac-only
     * @param stepRequest
     */
    void around(StepRequestImpl stepRequest) : stepRequestEnabled(stepRequest) {
        
        for (int attemptNumber = 1; attemptNumber < MAX_RETRY; attemptNumber++) {
            try {
                proceed(stepRequest);
                return;  // success.  we are done
            } catch(InternalException e) {
                if (e.errorCode() == 13) {
                    // swallow exception and retry
                } else {
                    // fail
                    throw e;
                }
            }
        }
        
        // try one more time, but do not swallow
        proceed(stepRequest);
    }
    
    pointcut gettingStepFilters(JDIDebugTarget target) : execution(public String[] JDIDebugTarget.getStepFilters()) &&
            this(target);

    /**
     * Allow the {@link IDebugProvider} the capability to supply its own
     * step filters.
     */
    String[] around(JDIDebugTarget target) : gettingStepFilters(target) {
        String[] initialFilters = proceed(target);
        try {
            IDebugProvider provider = adapter.getProvider();
            if (provider != null && isInterestingLaunch(target)) {
                return provider.augmentStepFilters(initialFilters);
            } 
        } catch(Throwable t) {
            JDTWeavingPlugin.logException(t);
        }
        return initialFilters;
    }
    
    pointcut conditionalBreakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint, ConditionalBreakpointHandler handler) : execution(public int ConditionalBreakpointHandler.breakpointHit(IJavaThread, IJavaBreakpoint))
                && args(thread, breakpoint) && this(handler);
    
    int around(IJavaThread thread, IJavaBreakpoint breakpoint, ConditionalBreakpointHandler handler) :
            conditionalBreakpointHit(thread, breakpoint, handler) {
        try {
            IDebugProvider provider = adapter.getProvider();
            if (provider != null && 
                    isInterestingLaunch(thread) && provider.shouldPerformEvaluation((IJavaStackFrame) thread.getTopStackFrame())) {
                return provider.conditionalBreakpointHit(thread, breakpoint, handler);
            }
        } catch (Throwable t) {
            JDTWeavingPlugin.logException(t);
        }
        
        return proceed(thread, breakpoint, handler);
    }

    /**
     * @param snippet
     * @param object
     * @param frame
     * @param listener
     * @param engine
     * @return true iff the provider performed the evaluation
     */
    protected boolean maybePerformEvaluation(String snippet, IJavaObject object,
            IJavaStackFrame frame, IEvaluationListener listener, int evaluationDetail, boolean hitBreakpoints,
            ASTEvaluationEngine engine) {
        try {
            IDebugProvider provider = adapter.getProvider();
            if (provider != null && isInterestingLaunch(frame)
                    && provider.shouldPerformEvaluation(frame)) {
                provider.performEvaluation(snippet, object, frame, listener,
                        engine.getJavaProject(), evaluationDetail, hitBreakpoints);
                return true;
            }
        } catch (Exception e) {
            JDTWeavingPlugin.logException(e);
        }
        return false;
    }

    /**
     * return true iff the current thread is part of a launch that is associated with an interesting
     * project
     */
    protected boolean isInterestingLaunch(IDebugElement thread) {
        try {
            if (thread == null) return false;
            
            IDebugProvider provider = adapter.getProvider();
            if (provider != null && provider.isAlwaysInteretingLaunch()) {
                return true;
            }
            
            ILaunchConfiguration launchConfig = thread
                    .getLaunch().getLaunchConfiguration();
            
            return isInterestingLaunch(launchConfig);
        } catch (CoreException e) {
            JDTWeavingPlugin.logException(e);
        }
        return false;
    }

    /**
     * Made public, static for testing
     * @param launchConfig
     * @return
     * @throws CoreException
     */
    public static boolean isInterestingLaunch(ILaunchConfiguration launchConfig)
            throws CoreException {
        String projectName = launchConfig
                .getAttribute(
                        IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                        "");
        
        if (!projectName.equals("")) {
            IProject project = WORKSPACE_ROOT.getProject(projectName);
            return (WeavableProjectListener.getInstance()
                    .isWeavableProject(project));
        } else {
            // most likely a server launch
            // return true iff we are running a SpringSource server
            String pluginId = launchConfig.getType().getPluginIdentifier();
            return pluginId != null && (pluginId.contains("springsource") || pluginId.contains("vmware")); 
        }
    }
}
