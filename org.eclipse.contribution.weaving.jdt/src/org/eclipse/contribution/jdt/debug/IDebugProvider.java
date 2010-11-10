/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.contribution.jdt.debug;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.eval.IEvaluationListener;

import com.sun.jdi.Location;

/**
 * @author Andrew Eisenberg
 * @created Oct 28, 2010
 * Provides hooks for debug support 
 */
public interface IDebugProvider {


    /**
     * Perform an evaluation in the debug provider 
     * @param snippet code snippet to evaluate
     * @param object 'this' object
     * @param frame current stack frame
     * @param listener the listener that requests the evaluation results
     * @param javaProject 
     * @throws DebugException
     */
    void performEvaluation(String snippet, IJavaObject object,
            IJavaStackFrame frame, IEvaluationListener listener, IJavaProject javaProject,
            int evaluationDetail, boolean hitBreakpoints) throws DebugException;
    
    /**
     * @param frame current stack fram where evaluation is occurring
     * @return true iff this stack frame should be evaluated by the debug provider
     * rather than the regular evaluator 
     * @throws DebugException 
     */
    boolean shouldPerformEvaluation(IJavaStackFrame frame) throws DebugException;
    
    /**
     * @param location current debug location
     * @return true iff the debug provider determines that an extra step into should be performed
     * @throws DebugException
     */
    boolean shouldPerformExtraStep(Location location) throws DebugException;
 
    
    /**
     * @return the extra step filters to be added to any step request
     * @return
     */
    String[] augmentStepFilters(String[] origStepFilters);
}
