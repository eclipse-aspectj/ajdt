/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January - copied to AJDT 
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import java.lang.reflect.Field;
import java.util.Map;

import org.eclipse.ajdt.parserbridge.AJCompilationUnitProblemFinder;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaElementDelta;
import org.eclipse.jdt.internal.core.JavaElementDeltaBuilder;
import org.eclipse.jdt.internal.core.JavaModelOperation;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Mostly copied from ReconcileWorkingCopyOperation in order to use the
 * AJCompilationUnitProblemFinder.  Changes marked with "// AspectJ Change".
 */
public class AJReconcileWorkingCopyOperation extends
		JavaModelOperation {

	boolean createAST;
	int astLevel;
	boolean forceProblemDetection;
	WorkingCopyOwner workingCopyOwner;
	org.eclipse.jdt.core.dom.CompilationUnit ast;
	
	/**
	 * @param workingCopy
	 * @param creatAST
	 * @param astLevel
	 * @param forceProblemDetection
	 * @param workingCopyOwner
	 */
	public AJReconcileWorkingCopyOperation(IJavaElement workingCopy,
			boolean creatAST, int astLevel, boolean forceProblemDetection,
			WorkingCopyOwner workingCopyOwner) {
		super(new IJavaElement[] {workingCopy});
		this.createAST = creatAST;
		this.astLevel = astLevel;
		this.forceProblemDetection = forceProblemDetection;
		this.workingCopyOwner = workingCopyOwner;
	}

	/**
	 * @exception JavaModelException if setting the source
	 * 	of the original compilation unit fails
	 */
	protected void executeOperation() throws JavaModelException {
		if (this.progressMonitor != null){
			if (this.progressMonitor.isCanceled()) return;
			this.progressMonitor.beginTask(Util.bind("element.reconciling"), 2); //$NON-NLS-1$
		}
	
		CompilationUnit workingCopy = getWorkingCopy();
		boolean wasConsistent = workingCopy.isConsistent();
		try {
			if (!wasConsistent) {
				// create the delta builder (this remembers the current content of the cu)
				JavaElementDeltaBuilder deltaBuilder = new JavaElementDeltaBuilder(workingCopy);
				
				// update the element infos with the content of the working copy
				this.ast = workingCopy.makeConsistent(this.createAST, this.astLevel, this.progressMonitor);
				deltaBuilder.buildDeltas();
	
				if (progressMonitor != null) progressMonitor.worked(2);
			
				// register the deltas
				// AspectJ Change Begin
				JavaElementDelta delta = getDelta(deltaBuilder);
				if (delta != null) {
					addReconcileDelta(workingCopy, delta);
				}
				// AspectJ Change End
			} else {
				// force problem detection? - if structure was consistent
				if (forceProblemDetection) {
					IProblemRequestor problemRequestor = workingCopy.getPerWorkingCopyInfo();
					if (problemRequestor != null && problemRequestor.isActive()) {
					    CompilationUnitDeclaration unit = null;
					    try {
							problemRequestor.beginReporting();
							char[] contents = workingCopy.getContents();
							// AspectJ Change Begin
							unit = AJCompilationUnitProblemFinder.process(workingCopy, contents, this.workingCopyOwner, problemRequestor, false/*don't cleanup cu*/, this.progressMonitor);
							// AspectJ Change End
							problemRequestor.endReporting();
							if (progressMonitor != null) progressMonitor.worked(1);
							if (this.createAST && unit != null) {
								Map options = workingCopy.getJavaProject().getOptions(true);
								this.ast = AST.convertCompilationUnit(this.astLevel, unit, contents, options, true/*isResolved*/, this.progressMonitor);
								if (progressMonitor != null) progressMonitor.worked(1);
							}
					    } finally {
					        if (unit != null) {
					            unit.cleanUp();
					        }
					    }
					}
				}
			}
		} finally {
			if (progressMonitor != null) progressMonitor.done();
		}
	}
	
    // AspectJ Change Begin
	// nasty hack: the delta field of JavaElementDeltaBuilder is not visible
	// as we're in a different package, so we must access it via reflection
	private JavaElementDelta getDelta(JavaElementDeltaBuilder deltaBuilder) {
		Field deltaField;
		try {
			deltaField = JavaElementDeltaBuilder.class.getDeclaredField("delta"); //$NON-NLS-1$
			deltaField.setAccessible(true);
			Object o = deltaField.get(deltaBuilder);
			return (JavaElementDelta)o;
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		return null;
	}
    // AspectJ Change End
	
	/**
	 * Returns the working copy this operation is working on.
	 */
	protected CompilationUnit getWorkingCopy() {
		return (CompilationUnit)getElementToProcess();
	}
	/**
	 * @see JavaModelOperation#isReadOnly
	 */
	public boolean isReadOnly() {
		return true;
	}
	protected IJavaModelStatus verify() {
		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		CompilationUnit workingCopy = getWorkingCopy();
		if (!workingCopy.isWorkingCopy()) {
			return new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, workingCopy); //was destroyed
		}
		return status;
	}
	

}
