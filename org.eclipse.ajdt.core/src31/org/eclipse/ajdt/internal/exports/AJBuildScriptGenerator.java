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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.build.AssembleScriptGenerator;
import org.eclipse.pde.internal.build.AssemblyInformation;
import org.eclipse.pde.internal.build.BuildScriptGenerator;
import org.eclipse.pde.internal.build.SourceFeatureInformation;
import org.eclipse.pde.internal.build.builder.FeatureBuildScriptGenerator;
import org.eclipse.pde.internal.build.packager.PackageScriptGenerator;

/**
 * Mostly copied from BuildScriptGenerator.
 * Enables AspectJ plugins to be correctly exported.
 */
public class AJBuildScriptGenerator extends BuildScriptGenerator {

	private boolean generateAssembleScript = true;
	/**
	 * flag indicating if the errors detected when the state is resolved must be reported or not.
	 * For example in releng mode we are interested in reporting the errors. It is the default. 
	 */
	private boolean reportResolutionErrors = true;

	/** flag indicating if missing properties file should be logged */
	private boolean ignoreMissingPropertiesFile = false;

	
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

	protected void generateFeatures(List features) throws CoreException {
		AssemblyInformation assemblageInformation = null;
		assemblageInformation = new AssemblyInformation();

		for (Iterator i = features.iterator(); i.hasNext();) {
			String[] featureInfo = getNameAndVersion((String) i.next());
			// AspectJ Change Begin
			FeatureBuildScriptGenerator generator = new AJFeatureBuildScriptGenerator(featureInfo[0], featureInfo[1], assemblageInformation);
			// AspectJ Change End
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
			generator.includePlatformIndependent(true);
			generator.setReportResolutionErrors(reportResolutionErrors);
			generator.setIgnoreMissingPropertiesFile(ignoreMissingPropertiesFile);
			generator.setSignJars(signJars);
			generator.setGenerateJnlp(generateJnlp);
			generator.generate();
		}

		if (generateAssembleScript == true) {
			String[] featureInfo = null;
			if (features.size() == 1)
				featureInfo = getNameAndVersion((String) features.get(0));
			else
				featureInfo = new String[] {"all"};

			generateAssembleScripts(assemblageInformation, featureInfo);

			if (features.size() == 1)
				featureInfo = getNameAndVersion((String) features.get(0));
			else
				featureInfo = new String[] {""};

			generatePackageScripts(assemblageInformation, featureInfo);
		}
	}

	private void generatePackageScripts(AssemblyInformation assemblageInformation, String[] featureInfo) throws CoreException {
		PackageScriptGenerator assembler = new PackageScriptGenerator(workingDirectory, assemblageInformation, featureInfo[0]);
		assembler.setSignJars(signJars);
		assembler.setGenerateJnlp(generateJnlp);
		assembler.setArchivesFormat(archivesFormat); //TODO Check 
		assembler.generate();
	}

	private void generateAssembleScripts(AssemblyInformation assemblageInformation, String[] featureInfo) throws CoreException {
		AssembleScriptGenerator assembler = new AssembleScriptGenerator(workingDirectory, assemblageInformation, featureInfo[0]);
		assembler.setSignJars(signJars);
		assembler.setGenerateJnlp(generateJnlp);
		assembler.setArchivesFormat(archivesFormat);
		assembler.generate();
	}
	private String[] getNameAndVersion(String id) {
		int versionPosition = id.indexOf(":"); //$NON-NLS-1$
		String[] result = new String[2];
		if (versionPosition != -1) {
			result[1] = id.substring(versionPosition + 1);
			result[0] = id.substring(0, versionPosition);
		} else
			result[0] = id;
		return result;
	}
	
	
	/**
	 * @param generateAssembleScript
	 *            The generateAssembleScript to set.
	 */
	public void setGenerateAssembleScript(boolean generateAssembleScript) {
		this.generateAssembleScript = generateAssembleScript;
	}
	
	/**
	 * @param value The reportResolutionErrors to set.
	 */
	public void setReportResolutionErrors(boolean value) {
		this.reportResolutionErrors = value;
		// AspectJ Change Begin - duplicated private variable so update both
		super.setReportResolutionErrors(value);
		// AspectJ Change End
	}

	/**
	 * @param value The ignoreMissingPropertiesFile to set.
	 */
	public void setIgnoreMissingPropertiesFile(boolean value) {
		ignoreMissingPropertiesFile = value;
		// AspectJ Change Begin - duplicated private variable so update both
		super.setIgnoreMissingPropertiesFile(value);
		// AspectJ Change End
	}
}
