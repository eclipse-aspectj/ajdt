/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.ajdt.exports.AJModelBuildScriptGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.build.AbstractScriptGenerator;
import org.eclipse.pde.internal.build.builder.DevClassPathHelper;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.TargetPlatform;
import org.eclipse.pde.internal.ui.build.BaseBuildAction;
import org.eclipse.pde.internal.ui.build.ClasspathHelper;

/**
 * 
 */
public class BuildPluginAction extends BaseBuildAction {

	protected void makeScripts(IProgressMonitor monitor)
		throws InvocationTargetException, CoreException {
	
		AJModelBuildScriptGenerator generator = new AJModelBuildScriptGenerator();
		AJModelBuildScriptGenerator.setOutputFormat(AbstractScriptGenerator.getDefaultOutputFormat());
		AJModelBuildScriptGenerator.setEmbeddedSource(AbstractScriptGenerator.getDefaultEmbeddedSource());
		AJModelBuildScriptGenerator.setForceUpdateJar(AbstractScriptGenerator.getForceUpdateJarFormat());
		AJModelBuildScriptGenerator.setConfigInfo(AbstractScriptGenerator.getDefaultConfigInfos());
		
		IProject project = fManifestFile.getProject();
		generator.setWorkingDirectory(project.getLocation().toOSString());
		String url = ClasspathHelper.getDevEntriesProperties(project.getLocation().addTrailingSeparator().toString() + "dev.properties", false); //$NON-NLS-1$
		generator.setDevEntries(new DevClassPathHelper(url));
		generator.setPluginPath(TargetPlatform.createPluginPath());
		generator.setBuildingOSGi(PDECore.getDefault().getModelManager().isOSGiRuntime());
		try {
			IPluginModelBase model = PDECore.getDefault().getModelManager().findModel(project);
			if (model != null) {
				generator.setModelId(model.getPluginBase().getId());
				generator.generate();
			}
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

}
