/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 *               Helen Hawkins - updated for new ajde interface (bug 148190)
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.exports.AJBuildScriptGenerator;
import org.eclipse.ajdt.internal.ui.ajde.UIComplierConfiguration;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.build.AbstractScriptGenerator;
import org.eclipse.pde.internal.build.IBuildPropertiesConstants;
import org.eclipse.pde.internal.core.ClasspathHelper;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.build.BaseBuildAction;

/**
 * copied from org.eclipse.pde.internal.ui.build.BuildPluginAction
 */
public class BuildPluginAction extends BaseBuildAction {

	protected void makeScripts(IProgressMonitor monitor)
			throws InvocationTargetException, CoreException {

		AJBuildScriptGenerator generator = new AJBuildScriptGenerator();
		AJBuildScriptGenerator.setEmbeddedSource(AbstractScriptGenerator
				.getDefaultEmbeddedSource());
		AJBuildScriptGenerator.setForceUpdateJar(AbstractScriptGenerator
				.getForceUpdateJarFormat());
		AJBuildScriptGenerator.setConfigInfo(AbstractScriptGenerator
				.getDefaultConfigInfos());

		IProject project = fManifestFile.getProject();
		List inpath = getInpath(project);
		List aspectpath = getAspectpath(project);
		generator.setInpath(inpath);
		generator.setAspectpath(aspectpath);

		generator.setWorkingDirectory(project.getLocation().toOSString());
		String url = ClasspathHelper.getDevEntriesProperties(project
				.getLocation().addTrailingSeparator().toString()
				+ "dev.properties", false); //$NON-NLS-1$
		generator.setDevEntries(url);
		generator.setPDEState(TargetPlatformHelper.getState());
		generator.setNextId(TargetPlatformHelper.getPDEState().getNextId());
		generator.setStateExtraData(TargetPlatformHelper.getBundleClasspaths(TargetPlatformHelper.getPDEState()), TargetPlatformHelper.getPatchMap(TargetPlatformHelper.getPDEState()));
		generator.setBuildingOSGi(true);

		/* AJDT 1.7 */
		// allow binary cycles
		Properties properties = new Properties();
		properties.put(IBuildPropertiesConstants.PROPERTY_ALLOW_BINARY_CYCLES, "true"); //$NON-NLS-1$
		generator.setImmutableAntProperties(properties);
		IPluginModelBase model = PluginRegistry.findModel(project);
		if (model != null && model.getPluginBase().getId() != null) {
			generator.setBundles(new BundleDescription[] {model.getBundleDescription()});
			generator.generate();
		} else {
			MessageDialog.openError(null, PDEUIMessages.BuildPluginAction_ErrorDialog_Title, PDEUIMessages.BuildPluginAction_ErrorDialog_Message);
		}
	}

	private List getAspectpath(IProject project) {
		String[] v = AspectJCorePreferences.getResolvedProjectAspectPath(project);

		// need to expand any variables on the path
		UIComplierConfiguration adapter = (UIComplierConfiguration) AspectJUIPlugin.getDefault().getCompilerFactory().getCompilerForProject(project).getCompilerConfiguration();
		String aspectPath = adapter.expandVariables(v[0], v[2]);

		// Ensure that every entry in the list is a fully qualified one.
		aspectPath = adapter.fullyQualifyPathEntries(aspectPath);
	
		if(aspectPath.length() > 0) {
			String[] entries = aspectPath.split(File.pathSeparator);
			List entryList = new ArrayList(Arrays.asList(entries));
			return entryList;
		}
		return null;		
	}

	private List getInpath(IProject project) {
		String[] v = AspectJCorePreferences.getResolvedProjectInpath(project);

		// need to expand any variables on the path
		UIComplierConfiguration adapter = (UIComplierConfiguration) AspectJUIPlugin.getDefault().getCompilerFactory().getCompilerForProject(project).getCompilerConfiguration();
		String inPath = adapter.expandVariables(v[0], v[2]);

		// Ensure that every entry in the list is a fully qualified one.
		inPath = adapter.fullyQualifyPathEntries(inPath);
	
		if(inPath.length() > 0) {
			String[] entries = inPath.split(File.pathSeparator);
			List entryList = new ArrayList(Arrays.asList(entries));
			return entryList;
		}
		return null;	
	}

}
