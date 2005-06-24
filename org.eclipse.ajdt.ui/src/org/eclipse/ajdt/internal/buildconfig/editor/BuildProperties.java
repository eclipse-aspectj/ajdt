/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.buildconfig.editor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.internal.buildconfig.editor.model.Build;
import org.eclipse.ajdt.internal.buildconfig.editor.model.BuildEntry;
import org.eclipse.ajdt.internal.buildconfig.editor.model.BuildModel;
import org.eclipse.ajdt.internal.buildconfig.editor.model.IBuild;
import org.eclipse.ajdt.internal.buildconfig.editor.model.IBuildEntry;
import org.eclipse.ajdt.internal.buildconfig.editor.model.IBuildModel;
import org.eclipse.ajdt.internal.utils.AJDTEventTrace;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.pde.core.IBaseModel;

/**
 * This class is used by the build configuration functionality in the UI to
 * support the including and excluding of resources in .ajproperties files.
 * 
 * @author Matt Chapman
 *  
 */
public class BuildProperties {

	private IFile myPropertiesFile;

	private IBuild build;

	private IBuildModel buildModel;

	public BuildProperties(IFile file) {
		myPropertiesFile = file;
	}

	/**
	 * Create a new properties file with the given source folders included
	 * @param file the properties file
	 * @param sourceFolder the list of source filders to include
	 */
	public BuildProperties(IFile file, List sourceFolders) {
		myPropertiesFile = file;
		
		initBuild(false);
		
		IBuildEntry srcIncl = buildModel.getFactory().createEntry(
					IBuildPropertiesConstants.PROPERTY_SRC_INCLUDES);
		build.addWithoutNotify(srcIncl);
		
		IBuildEntry srcExcl = build.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_EXCLUDES);
		if (srcExcl != null){
			((Build)build).removeWithoutNotify(srcExcl);
		}

		for (Iterator iter = sourceFolders.iterator(); iter.hasNext();) {
			Object next = iter.next();
			IPath path;
			String entry;
			if (next instanceof IResource) {
				entry = getNameFromResource(((IResource) next));
			} else {
				path = ((IPath) next).addTrailingSeparator();
				entry = path.toString();
			}

			addTokenToBuildEntry((BuildEntry) srcIncl, entry);
		}

		writeFile();
	}

	/**
	 * Returns a list of all files included by the current configuration.
	 * 
	 * @param forceReadingFile if set ensures properties file is always read
	 * @return a list of file resources
	 */
	public List getFiles(boolean forceReadingFile) {
		if (forceReadingFile){
			//ensure latest contents are read
			initBuild(true);
		} else {
			initBuild(false);
		}

		IBuildEntry srcIncl = build
				.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_INCLUDES);
		IBuildEntry srcExcl = build
				.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_EXCLUDES);
		IProject fProject = myPropertiesFile.getProject();
		List included = determineIncludedFiles(srcIncl, srcExcl, fProject);

		return included;
	}

	/**
	 * Includes the given resource from the active configuration, and writes the
	 * updated configuration file.
	 * 
	 * @param res
	 *            the resource to include
	 */
	public void include(IResource res) {
		String name;
		if (res.getType() == IResource.FOLDER) {
			name = res.getProjectRelativePath().addTrailingSeparator()
					.toString();
		} else {
			name = res.getProjectRelativePath().toString();
		}
		name = handleResourceFolder(res, name);
		initBuild(false);

		IBuildEntry srcIncl = build
				.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_INCLUDES);
		IBuildEntry srcExcl = build
				.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_EXCLUDES);
		handleCheck(srcIncl, srcExcl, name, res);

		if (srcIncl != null) {
			build.addWithoutNotify(srcIncl);
		}
		if (srcExcl != null) {
			build.addWithoutNotify(srcExcl);
		}
		deleteEmptyEntries();
		//writeFile();
	}

	/**
	 * Excludes the given resource from the active configuration, and writes the
	 * updated configuration file.
	 * 
	 * @param res
	 *            the resource to exclude
	 * @throws CoreException
	 */
	public void exclude(IResource res) throws CoreException {
		String name;
		if (res.getType() == IResource.FOLDER) {
			name = res.getProjectRelativePath().addTrailingSeparator()
					.toString();
		} else {
			name = res.getProjectRelativePath().toString();
		}
		name = handleResourceFolder(res, name);

		initBuild(false);

		IBuildEntry srcIncl = build
				.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_INCLUDES);
		IBuildEntry srcExcl = build
				.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_EXCLUDES);
		handleUncheck(srcIncl, srcExcl, name, res);

		if (srcIncl != null) {
			build.addWithoutNotify(srcIncl);
		}
		if (srcExcl != null) {
			build.addWithoutNotify(srcExcl);
		}
		deleteEmptyEntries();
	}

	//once build was initialized, it must never be set to null again
	//to force reinitialization, use forceInit paramter instead
	//(prevents concurrent access trouble)
	private void initBuild(boolean forceInit) {
		if (build == null || forceInit) {
			BuildEditor be = new BuildEditor();
			AJDTEventTrace.buildConfigRead(myPropertiesFile);
			be.monitoredFileAdded(myPropertiesFile);
			IBaseModel ibm = be.getContextManager().findContext(
					myPropertiesFile).getModel();
			if (ibm instanceof BuildModel) {
				buildModel = (BuildModel) ibm;
				build = buildModel.getBuild();
			}
		}
	}

	//return a list of all file resources in the given folder, including all
	// sub-folders
	private List allFiles(IContainer folder) {
		final List contents = new ArrayList();
		try {
			folder.accept(new IResourceVisitor() {
				public boolean visit(IResource res) {
					if (res.getType() == IResource.FILE
							&& CoreUtils.ASPECTJ_SOURCE_FILTER
									.accept(res.getName())) {
						contents.add(res);
					}
					return true;
				}
			});
		} catch (CoreException e) {
		}
		return contents;
	}

	private List determineIncludedFiles(final IBuildEntry includes,
			final IBuildEntry excludes, final IProject fProject) {
		List includedFiles = new ArrayList();
		Vector fileExt = new Vector();
		String[] inclTokens, exclTokens = new String[0];
		if (fProject == null || includes == null)
			return includedFiles;
		inclTokens = includes.getTokens();
		if (excludes != null)
			exclTokens = excludes.getTokens();
		Set temp = new TreeSet();
		for (int i = 0; i < inclTokens.length; i++)
			temp.add(inclTokens[i]);
		for (int i = 0; i < exclTokens.length; i++)
			temp.add(exclTokens[i]);
		Iterator iter = temp.iterator();
		while (iter.hasNext()) {
			String resource = iter.next().toString();
			boolean isIncluded = includes.contains(resource);
			if (resource.lastIndexOf(Path.SEPARATOR) == resource.length() - 1) {
				IContainer folder;
				if (resource.length() == 1) {
					folder = fProject;
				} else {
					folder = fProject.getFolder(resource);
				}
				if (isIncluded) {
					includedFiles.addAll(allFiles(folder));
				} else {
					includedFiles.removeAll(allFiles(folder));
				}
			} else if (resource.startsWith("*.")) { //$NON-NLS-1$
				if (isIncluded)
					fileExt.add(resource.substring(2));
			} else {
				IFile file = fProject.getFile(resource);
				if (isIncluded
						&& CoreUtils.ASPECTJ_SOURCE_FILTER.accept(file
								.getName())) {
					includedFiles.add(file);
				} else {
					includedFiles.remove(file);
				}
			}
		}
		if (fileExt.size() == 0)
			return includedFiles;
		return includedFiles;
	}

	public void writeFile() {
		AJDTEventTrace.buildConfigWrite(myPropertiesFile);
		IBuildEntry[] entries = build.getBuildEntries();
		final StringBuffer buff = new StringBuffer();
		for (int i = 0; i < entries.length; i++) {
			if (entries[i] instanceof BuildEntry) {
				String s = ((BuildEntry) entries[i]).write();
				buff.append(s);
			}
		}
		
		if (!myPropertiesFile.getWorkspace().isTreeLocked()) {
			InputStream inputStream = new ByteArrayInputStream(buff
					.toString().getBytes());
			try {
				if (!myPropertiesFile.exists()) {
					myPropertiesFile.create(inputStream, false,
							null);
				} else {
					myPropertiesFile.setContents(inputStream,
							false, true, null);
				}
			} catch (CoreException e) {
			}
		} else {
			AJDTEventTrace.generalEvent("BuildProperties: resource tree locked");
		}
	}

	private void addTokenToBuildEntry(BuildEntry entry, String token) {
		if (!entry.contains(token)) {
			entry.addTokenWithoutNotify(token);
		}
	}

	private boolean contains(Object[] array, Object o){
		if (array == null)
			return false;
		for (int i=0; i<array.length; i++){
			if (array[i].equals(o))
				return true;
		}
		return false;
	}
	
	private String getNameFromResource(IResource res){
		IPath p = res.getProjectRelativePath();
		if ((res.getType() == IResource.FOLDER) || (res.getType() == IResource.PROJECT))
			p = p.addTrailingSeparator();
		return p.toString();
	}
	
	private boolean isIncluded(IResource res, IBuildEntry includes, IBuildEntry excludes) {
		if (res.getParent() == null)
			return false;
		String name = getNameFromResource(res);
		if (includes != null){
			if (contains(includes.getTokens(), name))
				return true;
		}
		if (excludes != null){
			if (contains(excludes.getTokens(), name))
				return false;
		}
		return isIncluded(res.getParent(), includes, excludes);
	}
	

	// from BuildContentsSection
	/**
	 * @param resource -
	 *            file/folder being modified in tree
	 * @param resourceName -
	 *            name file/folder
	 * @return relative path of folder if resource is folder, otherwise, return
	 *         resourceName
	 */
	protected String handleResourceFolder(IResource resource,
			String resourceName) {
		if (resource instanceof IFolder) {
			deleteFolderChildrenFromEntries((IFolder) resource);
			return getResourceFolderName(resourceName);
		}
		return resourceName;
	}

	protected void deleteFolderChildrenFromEntries(IFolder folder) {
		IBuild build = buildModel.getBuild();
		IBuildEntry srcIncl = build
				.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_INCLUDES);
		IBuildEntry srcExcl = build
				.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_EXCLUDES);
		String parentFolder = getResourceFolderName(folder
				.getProjectRelativePath().toString());

		removeChildren(srcIncl, parentFolder);
		removeChildren(srcExcl, parentFolder);
	}

	protected String getResourceFolderName(String resourceName) {
		return resourceName;
	}

	protected void removeChildren(IBuildEntry entry, String parentFolder) {
		if (entry != null) {
			String[] tokens = entry.getTokens();
			for (int i = 0; i < tokens.length; i++) {
				if (tokens[i].indexOf(Path.SEPARATOR) != -1
						&& tokens[i].startsWith(parentFolder)
						&& !tokens[i].equals(parentFolder)) {
					((BuildEntry) entry).removeTokenWithoutNotify(tokens[i]);
				}
			}
		}
	}

	// similar to BuildContentsSection.handleCheck
	protected void handleCheck(IBuildEntry includes, IBuildEntry excludes,
			String resourceName, IResource resource) {
		if (includes == null) {
			includes = buildModel.getFactory().createEntry(
					IBuildPropertiesConstants.PROPERTY_SRC_INCLUDES);
			IBuild build = buildModel.getBuild();
			build.addWithoutNotify(includes);
		}
		if ((excludes != null) && excludes.contains(resourceName)) {
			excludes.removeTokenWithoutNotify(resourceName);
		}
		if (!this.isIncluded(resource, includes, excludes)){
			includes.addTokenWithoutNotify(resourceName);
		}
	}

	// similar to BuildContentsSection.handleUnCheck
	protected void handleUncheck(IBuildEntry includes, IBuildEntry excludes,
			String resourceName, IResource resource) throws CoreException {
		if (isIncluded(resource.getParent(), includes, excludes)) {
			if (excludes == null) {
				excludes = buildModel.getFactory().createEntry(
						IBuildPropertiesConstants.PROPERTY_SRC_EXCLUDES);
				IBuild build = buildModel.getBuild();
				build.addWithoutNotify(excludes);
			}
			if (!excludes.contains(resourceName)
					&& (includes != null ? !includes.contains(resourceName)
							: true))
				excludes.addTokenWithoutNotify(resourceName);
		}
		if (includes != null) {
			if (includes.contains(resourceName))
				((BuildEntry) includes).removeTokenWithoutNotify(resourceName);
			if (includes.contains("*." + resource.getFileExtension())) { //$NON-NLS-1$
				IResource[] members = myPropertiesFile.getProject().members();
				for (int i = 0; i < members.length; i++) {
					if (!(members[i] instanceof IFolder)
							&& !members[i].getName().equals(resource.getName())
							&& (resource.getFileExtension().equals(members[i]
									.getFileExtension()))) {
						includes.addTokenWithoutNotify(members[i].getName());
					}
				}
				includes
						.removeTokenWithoutNotify("*." + resource.getFileExtension()); //$NON-NLS-1$
			}
		}
	}

	protected void deleteEmptyEntries() {
		IBuild build = buildModel.getBuild();
		IBuildEntry[] entries = {
				build.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_EXCLUDES),
				build.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_INCLUDES) };
		for (int i = 0; i < entries.length; i++) {
			if (entries[i] != null && entries[i].getTokens().length == 0)
				((Build) build).removeWithoutNotify(entries[i]);
		}
	}

	/**
	 * @param sourcePathes
	 */
	public void updateSourceFolders(List sourcePathes) {
		boolean changed = false;
		
		initBuild(false);
		
		IBuildEntry srcIncl = build
				.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_INCLUDES);
		if (srcIncl != null) {
			changed |= removeOldSourceFolders(sourcePathes, srcIncl);
		} else {
			srcIncl = buildModel.getFactory().createEntry(
					IBuildPropertiesConstants.PROPERTY_SRC_INCLUDES);
			build.addWithoutNotify(srcIncl);
		}
		IBuildEntry srcExcl = build
				.getEntry(IBuildPropertiesConstants.PROPERTY_SRC_EXCLUDES);
		if (srcExcl != null) {
			changed |= removeOldSourceFolders(sourcePathes, srcExcl);
		}
		changed |= addNewSourceFolders(sourcePathes, srcIncl);

		if (changed) {
			writeFile();
		}
	}

	private boolean removeOldSourceFolders(List sourcePathes,
			IBuildEntry srcIncl) {
		boolean changed = false;
		String[] entries = srcIncl.getTokens();
		Iterator iter;
		for (int i = 0; i < entries.length; i++) {
			iter = sourcePathes.iterator();
			boolean toBeRemoved = true;
			while (iter.hasNext()) {
				IPath p = (IPath) iter.next();
				if (p.isPrefixOf(new Path(entries[i]))) {
					toBeRemoved = false;
					break;
				}
			}
			if (toBeRemoved) {
				srcIncl.removeTokenWithoutNotify(entries[i]);
				changed = true;
			}
		}
		return changed;
	}

	private boolean addNewSourceFolders(List sourcePathes, IBuildEntry srcIncl) {
		boolean changed = false;
		String[] entries = srcIncl.getTokens();
		Iterator iter;

		iter = sourcePathes.iterator();
		while (iter.hasNext()) {
			boolean isNewSrcPath = true;
			IPath path = (IPath) iter.next();
			for (int i = 0; i < entries.length; i++) {
				if (path.isPrefixOf(new Path(entries[i]))) {
					isNewSrcPath = false;
					break;
				}
			}

			if (isNewSrcPath) {
				srcIncl.addTokenWithoutNotify(path.addTrailingSeparator()
						.toString());
				changed = true;
			}

		}
		return changed;
	}
}