/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/
package org.eclipse.ajdt.internal.exports;

import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.exports.AJFeatureBuildScriptGenerator;
import org.eclipse.ajdt.exports.AJModelBuildScriptGenerator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.build.AssembleScriptGenerator;
import org.eclipse.pde.internal.build.AssemblyInformation;
import org.eclipse.pde.internal.build.BuildScriptGenerator;
import org.eclipse.pde.internal.build.SourceFeatureInformation;
import org.eclipse.pde.internal.build.builder.FeatureBuildScriptGenerator;

/**
 * Mostly copied from BuildScriptGenerator.
 * Enables AspectJ plugins to be correctly exported.
 */
public class AJBuildScriptGenerator extends BuildScriptGenerator {

	private boolean generateAssembleScript = true;

	
	/**
	 * 
	 * @param models
	 * @throws CoreException
	 */
	protected void generateModels(List models) throws CoreException {
		for (Iterator iterator = models.iterator(); iterator.hasNext();) {
			AJModelBuildScriptGenerator generator = new AJModelBuildScriptGenerator();
			//Filtering is not required here, since we are only generating the
			// build for a plugin or a fragment
			String model = (String) iterator.next();
			generator.setModelId(model);
			generator.generate();
		}
	}

	/**
	 * 
	 * @param features
	 * @throws CoreException
	 */
	protected void generateFeatures(List features) throws CoreException {
		for (Iterator i = features.iterator(); i.hasNext();) {
			AssemblyInformation assemblageInformation = null;
			assemblageInformation = new AssemblyInformation();

			String featureId = (String) i.next();
			String versionId = null;
			int versionPosition = featureId.indexOf(":"); //$NON-NLS-1$
			if (versionPosition != -1) {
				versionId = featureId.substring(versionPosition + 1);
				featureId = featureId.substring(0, versionPosition);
			}
			FeatureBuildScriptGenerator generator = new AJFeatureBuildScriptGenerator(featureId, versionId, assemblageInformation);
			generator.setGenerateIncludedFeatures(this.recursiveGeneration);
			generator.setAnalyseChildren(this.children);
			generator.setSourceFeatureGeneration(false);
			generator.setBinaryFeatureGeneration(true);
			generator.setScriptGeneration(true);
			generator.setPluginPath(pluginPath);
			generator.setBuildSiteFactory(null);
			generator.setDevEntries(devEntries);
			generator.setSourceToGather(new SourceFeatureInformation());
			generator.setCompiledElements(generator.getCompiledElements());
			generator.setBuildingOSGi(isBuildingOSGi());
			generator.generate();

			if (generateAssembleScript == true) {
				AssembleScriptGenerator assembler = new AssembleScriptGenerator(workingDirectory, assemblageInformation, featureId, null);
				assembler.generate();
			}
		}
	}
	
	/**
	 * @param generateAssembleScript
	 *            The generateAssembleScript to set.
	 */
	public void setGenerateAssembleScript(boolean generateAssembleScript) {
		this.generateAssembleScript = generateAssembleScript;
	}
}
