package org.eclipse.ajdt.build.tasks;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.internal.build.AssemblyInformation;
import org.eclipse.pde.internal.build.SourceFeatureInformation;
import org.eclipse.pde.internal.build.Utils;
import org.eclipse.pde.internal.build.builder.BuildDirector;
import org.eclipse.pde.internal.build.builder.FeatureBuildScriptGenerator;
import org.eclipse.pde.internal.build.site.BuildTimeFeature;
import org.eclipse.pde.internal.build.site.BuildTimeSiteFactory;
import org.eclipse.pde.internal.build.site.compatibility.FeatureEntry;
import org.osgi.framework.Version;

public class AJBuildDirector extends BuildDirector {

	private boolean scriptGeneration = true;

	public AJBuildDirector() {
		super();
	}

	public AJBuildDirector(AssemblyInformation assemblageInformation) {
		super(assemblageInformation);
	}

	public AJBuildDirector(String featureId, String versionId,
			AssemblyInformation informationGathering) throws CoreException {
		super(featureId, versionId, informationGathering);
	}


	
	protected void generate(BuildTimeFeature feature, boolean generateProductFiles) throws CoreException {
		if (analyseIncludedFeatures)
			generateIncludedFeatureBuildFile(feature);
		if (analysePlugins)
			generateChildrenScripts(feature);

		collectElementToAssemble(feature);

		if (scriptGeneration) {
			FeatureBuildScriptGenerator featureScriptGenerator = 
				new FeatureBuildScriptGenerator(feature);  //AspectJ Change
			featureScriptGenerator.setDirector(this);
			featureScriptGenerator.setBuildSiteFactory(siteFactory);
			featureScriptGenerator.setGenerateProductFiles(generateProductFiles);
			featureScriptGenerator.generate();
		}
	}
	
	BuildTimeSiteFactory getSiteFactory() {
		return super.siteFactory;
	}

	/**
	 * @param models
	 * @throws CoreException
	 */
	private void generateModels(List models) throws CoreException {
		if (scriptGeneration == false)
			return;
		if (binaryFeature == false || models.isEmpty())
			return;

		Set generatedScripts = new HashSet(models.size());
		for (Iterator iterator = models.iterator(); iterator.hasNext();) {
			BundleDescription model = (BundleDescription) iterator.next();
			if (generatedScripts.contains(model))
				continue;
			generatedScripts.add(model);

			//Get the corresponding plug-in entries (from a feature object) associated with the model
			//and generate the script if one the configuration is being built. The generated scripts
			//are configuration agnostic so we only generate once.
			Set matchingEntries = (Set) ((Properties) model.getUserObject()).get(PLUGIN_ENTRY);
			if (matchingEntries.isEmpty())
				return;

			Iterator entryIter = matchingEntries.iterator();
			FeatureEntry correspondingEntry = (FeatureEntry) entryIter.next();
			List list = selectConfigs(correspondingEntry);
			if (list.size() == 0)
				continue;

			AJModelBuildScriptGenerator generator = 
				new AJModelBuildScriptGenerator();  // AspectJ Change
			generator.setBuildSiteFactory(siteFactory);
			generator.setCompiledElements(getCompiledElements());
			generator.setIgnoreMissingPropertiesFile(isIgnoreMissingPropertiesFile());
			generator.setModel(model); // setModel has to be called before configurePersistentProperties because it reads the model's properties
			generator.setFeatureGenerator(this);
			generator.setPluginPath(getPluginPath());
			generator.setBuildingOSGi(isBuildingOSGi());
			generator.setDevEntries(devEntries);
			generator.includePlatformIndependent(isPlatformIndependentIncluded());
			generator.setSignJars(signJars);
			generator.setAssociatedEntry(correspondingEntry);
			generator.generate();
		}

	}


	
	private void generateChildrenScripts(BuildTimeFeature feature) throws CoreException {
		/* AJDT 1.7 */
		Set plugins = computeElements(feature);
		String suffix = generateFeatureVersionSuffix(feature);
		if (suffix != null) {
			Version versionId = new Version(feature.getVersion());
			String qualifier = versionId.getQualifier();
			qualifier = qualifier.substring(0, feature.getContextQualifierLength());
			qualifier = qualifier + '-' + suffix;
			versionId = new Version(versionId.getMajor(), versionId.getMinor(), versionId.getMicro(), qualifier);
			String newVersion = versionId.toString();
			feature.setVersion(newVersion);
			//initializeFeatureNames(); //reset our variables
		}
		generateModels(Utils.extractPlugins(getSite(false).getRegistry().getSortedBundles(), plugins));
	}

	
	
	/**
	 * Sets the scriptGeneration.
	 * 
	 * @param scriptGeneration
	 *                   The scriptGeneration to set
	 */
	public void setScriptGeneration(boolean scriptGeneration) {
		super.setScriptGeneration(scriptGeneration);
		this.scriptGeneration = scriptGeneration;
	}

	// AspectJ Change---make accessible to classes in this package.
	SourceFeatureInformation getSourceToGather() {
		return sourceToGather;
	}
}
