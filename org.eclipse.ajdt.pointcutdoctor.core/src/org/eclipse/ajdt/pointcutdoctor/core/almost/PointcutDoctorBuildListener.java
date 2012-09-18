/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.core.almost;

import java.util.List;
import java.util.Map;

import org.aspectj.ajde.core.AjCompiler;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.builder.IAJBuildListener;
import org.eclipse.ajdt.core.lazystart.IAdviceChangedListener;
import org.eclipse.ajdt.pointcutdoctor.core.PointcutDoctorCorePlugin;
import org.eclipse.ajdt.pointcutdoctor.core.PointcutRelaxMungerFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.compiler.CategorizedProblem;


public class PointcutDoctorBuildListener implements IAJBuildListener {
	
	private PointcutDoctorCorePlugin plugin;
	
	public PointcutDoctorBuildListener(PointcutDoctorCorePlugin plugin) {
		this.plugin = plugin;
	}

	public void preAJBuild(int kind, IProject project,
			IProject[] requiredProjects) {
		PointcutRelaxMungerFactory factory = 
			plugin
			.getAlmostJPSPluginFacade()
			.getPointcutRelaxMungerFactory(project);
		AjCompiler compiler = AspectJPlugin.getDefault().getCompilerFactory()
			.getCompilerForProject(project);
		compiler.setCustomMungerFactory(factory);
	}

	public void postAJBuild(int kind, IProject project, boolean noSourceChanges) {
		AjCompiler compiler = AspectJPlugin.getDefault().getCompilerFactory()
			.getCompilerForProject(project);
		compiler.setCustomMungerFactory(null);
	}

	public void addAdviceListener(IAdviceChangedListener adviceListener) {
	}

	public void removeAdviceListener(IAdviceChangedListener adviceListener) {
	}

	public void postAJClean(IProject project) {
	}

    public void postAJBuild(int kind, IProject project,
            boolean noSourceChanges,
            Map<IFile, List<CategorizedProblem>> newProblems) {
    }

}
