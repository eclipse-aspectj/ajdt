/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.eclipse.ajdt.internal.core.ajde;

import org.aspectj.ajde.core.AjCompiler;
import org.eclipse.core.resources.IProject;

/**
 * Factory interface used to create and store AjCompiler's for IProject's
 */
public interface ICompilerFactory {
	
	/**
	 * If an AjCompiler has already been created for the given project
	 * then return that one, otherwise create an AjCompiler, store it
	 * and then return it.
	 * 
	 * @param project
	 * @return the AjCompiler for the given project
	 */
	public AjCompiler getCompilerForProject(IProject project);
	
	/**
	 * Stop recording the AjCompiler for the given project
	 * 
	 * @param project
	 */
	public void removeCompilerForProject(IProject project);
	
	/**
	 * @param project
	 * @return true if an AjCompiler exists for the given project and
	 * false otherwise
	 */
	public boolean hasCompilerForProject(IProject project);
	
}
