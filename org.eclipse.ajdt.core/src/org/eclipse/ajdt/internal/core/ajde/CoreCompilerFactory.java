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

import java.util.HashMap;
import java.util.Map;

import org.aspectj.ajde.core.AjCompiler;
import org.eclipse.core.resources.IProject;

/**
 * ICompilerFactory implementation which returns AjCompilers with
 * core implementations of the required interfaces.
 * 
 * This class is only used if ajdt.ui plugin is not available
 * 
 */ 
public class CoreCompilerFactory implements ICompilerFactory {

	private Map<IProject, AjCompiler> compilerMap = new HashMap<IProject, AjCompiler>();
	
	/**
	 * If have already created an AjCompiler for the given project
	 * return that one, otherwise create a new one.
	 */
	public AjCompiler getCompilerForProject(IProject project) {
		if (compilerMap.get(project) != null) {
			return (AjCompiler) compilerMap.get(project);
		}
		AjCompiler compiler = createCompiler(project);
		compilerMap.put(project,compiler);
		return compiler;
	}

    protected AjCompiler createCompiler(IProject project) {
        AjCompiler compiler = new AjCompiler(
				project.getName(),
				new CoreCompilerConfiguration(project),
				new CoreBuildProgressMonitor(project),
				new CoreBuildMessageHandler());
        return compiler;
    }

	/**
	 * No longer record the AjCompiler for the given project.
	 */
	public void removeCompilerForProject(IProject project) {
        // firstly clean up any state associated with the compiler
	    AjCompiler compiler = (AjCompiler) compilerMap.get(project);
	    if (compiler != null) {
            compiler.clearLastState();
            // remove compiler from the map
            compilerMap.remove(project);
        }
	}
	
	/**
	 * Return true if have already created an AjCompiler for the
	 * given project, false otherwise.
	 */
	public boolean hasCompilerForProject(IProject project) {
		return (compilerMap.get(project) != null);
	}

}
