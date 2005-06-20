/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 * 	   Sian January - updated for new build configuration wizard
 *******************************************************************************/
package org.eclipse.ajdt.internal.buildconfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.internal.buildconfig.editor.BuildProperties;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * @author Luzius Meisser
 * 
 * This class represents a Build Configuration. It stores all the Classpath data
 * and offers functionality to include/exclude files, commit changes and to
 * create new Build Configurations using sophisticated constructors.
 */
public class BuildConfiguration implements Cloneable,
		org.eclipse.jdt.internal.core.util.Util.Comparable {
	public static final String EXTENSION = "ajproperties"; //$NON-NLS-1$

	public static final String STANDARD_BUILD_CONFIGURATION_NAME = AspectJUIPlugin.getResourceString("buildConfig.standardFileName"); //$NON-NLS-1$

	public static final String STANDARD_BUILD_CONFIGURATION_FILE = STANDARD_BUILD_CONFIGURATION_NAME
		+ "." + EXTENSION;

	//because we are using jdts classpath file reader/writer, we cannot
	//define new entry kinds and have to recycle one we are not using
	public static final int LST_FILE_LINK = IClasspathEntry.CPE_CONTAINER;

	private String name;

	private BuildProperties propertiesFile;

	private HashSet fileList;

	private ProjectBuildConfigurator pbc;

	private boolean listenToFileChanges = true;

	//creates BuildConfiguration from data and writes according file
	public BuildConfiguration(String name, IJavaProject jp,
			ProjectBuildConfigurator pbc) {
		this.pbc = pbc;
		this.name = name;

		//check if buildconfigurator has a filelist we should use
		//(when converting to an aj project, this file list gets
		//populated using jdts exclusion/inclusion patterns)
		fileList = BuildConfigurator.getBuildConfigurator()
				.getInitialFileList();
		if (fileList != null) {
			propertiesFile = new BuildProperties(getFileFromName(name),
					minimizeIncludes(fileList));
		} else {
			try {
				IClasspathEntry[] cpes2 = jp.getRawClasspath();
				if (BuildConfigurator.getBuildConfigurator()
						.haveExclusionPatterns(cpes2)) {
					fileList = BuildConfigurator.getBuildConfigurator()
							.getFileSetFromCPE(cpes2, jp);
					propertiesFile = new BuildProperties(getFileFromName(name),
							minimizeIncludes(fileList));
				} else {
					fileList = BuildConfigurator.getBuildConfigurator()
							.getInitialFileList();
					if (fileList != null) {
						//maybe filelist is written now
						//(this check could be prevented using synchronization
						//but we risk creating deadlock opportunities when
						// doing so)
						propertiesFile = new BuildProperties(
								getFileFromName(name),
								minimizeIncludes(fileList));
					} else {
						List pathes = new ArrayList();
						for (int i = 0; i < cpes2.length; i++) {
							if (cpes2[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
								IPath p = cpes2[i].getPath();
								IResource res = jp.getProject().getParent()
										.findMember(p);
								if ((res != null) && res.exists())
									pathes.add(res.getProjectRelativePath());
							}
						}
						propertiesFile = new BuildProperties(
								getFileFromName(name), pathes);
					}
				}
			} catch (JavaModelException e) {
			}
		}
	}

	public IFile getFile() {
		return getFileFromName(name);
	}

	//creates BuildConfiguration from file
	public BuildConfiguration(IFile existingFile, ProjectBuildConfigurator pbc) {
		this.pbc = pbc;
		loadDataFromFile(existingFile);
	}

	private HashSet getFileList() {
		if (fileList == null) {
			HashSet newList = new HashSet(20);
			newList.addAll(propertiesFile.getFiles(false));
			fileList = newList;
			return newList;
		}
		return fileList;
	}

	/**
	 * Creates BuildConfiguration from lst file
	 * 
	 * @param lstFile2
	 * @param shell
	 * @param pbc
	 *  
	 */
	public BuildConfiguration(IFile lstFile2, Shell shell,
			ProjectBuildConfigurator pbc) {
		this.pbc = pbc;
		name = lstFile2.getName();
		name = name.substring(0, name.length() - 4);

		String title = AspectJUIPlugin
				.getResourceString("BCDialog.SaveLstAsAJProp.title"); //$NON-NLS-1$
		String msg = AspectJUIPlugin.getResourceString(
				"BCDialog.SaveLstAsAJProp.message").replaceAll("%src", name); //$NON-NLS-1$ //$NON-NLS-2$
		InputDialogWithCheck md = new InputDialogWithCheck(shell, title, msg, name + "." //$NON-NLS-1$
				+ BuildConfiguration.EXTENSION, null);
		md.setBlockOnOpen(true);
		if ((md.open() == Window.OK) && (md.getValue() != null)) {
			String fileName = md.getValue();
			if (!fileName.endsWith("." + BuildConfiguration.EXTENSION)) { //$NON-NLS-1$
				fileName = fileName + "." + BuildConfiguration.EXTENSION; //$NON-NLS-1$
			}
			IFile file = pbc.getJavaProject().getProject().getFile(fileName);
			name = BuildConfiguration.getNameFromFile(file);

			if (file.exists()) {
				if (!askUserOverwrite(fileName)) {
					return;
				}
			}

			fileList = new HashSet();
			addLStFileContentsToFileList(lstFile2);
			IFile newFile = getFileFromName(name);
			this.propertiesFile = new BuildProperties(newFile, minimizeIncludes(fileList));
			pbc.addBuildConfiguration(this);
			if (md.isActivateChecked()){
				pbc.setActiveBuildConfiguration(this);
			}
			if (md.isOpenInEditorChecked()) {
				IWorkbenchWindow dwindow = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow();
				IWorkbenchPage page = dwindow.getActivePage();
				if (page != null) 
					try {
						IDE.openEditor(page, newFile, true);
					}
					catch (PartInitException pie) {}
			}
		}
	}

	private List minimizeIncludes(HashSet fileList) {
		//if all files of a folder are included, include folder instead of
		// files seperately
		HashSet temp1 = (HashSet) fileList.clone();
		HashSet temp2 = new HashSet();
		HashSet temp3;
		boolean hasChanged = true;
		Iterator iter = temp1.iterator();

		while (hasChanged) {

			hasChanged = false;
			iter = temp1.iterator();
			while (iter.hasNext()) {
				IResource f = (IResource) iter.next();
				IContainer cont = f.getParent();
				if (cont.getType() != IResource.PROJECT) {
					try {
						IResource[] mems = cont.members();
						boolean containsAll = true;
						for (int i = 0; i < mems.length; i++) {
							if (!temp1.contains(mems[i])
									&& CoreUtils.ASPECTJ_SOURCE_FILTER
											.accept(mems[i].getName())) {
								containsAll = false;
								break;
							} else if (!temp1.contains(mems[i])
									&& (mems[i].getType() == IResource.FOLDER)) {
								containsAll = false;
								break;
							}
						}
						if (!containsAll) {
							for (int i = 0; i < mems.length; i++) {
								if (temp1.remove(mems[i]))
									temp2.add(mems[i]);
							}
						} else {
							for (int i = 0; i < mems.length; i++) {
								temp1.remove(mems[i]);
							}
							hasChanged = true;
							temp2.add(cont);
						}
					} catch (CoreException e1) {
					}
				} else {
					temp1.remove(f);
					temp2.add(f);
				}
				iter = temp1.iterator();
			}

			temp3 = temp2;
			temp2 = temp1;
			temp1 = temp3;
		}

		return new ArrayList(temp1);
	}

	private void addLStFileContentsToFileList(IFile lstFile) {
		ArrayList files = new ArrayList();
		ArrayList options = new ArrayList();
		ArrayList links = new ArrayList();

		try {
			org.eclipse.ajdt.internal.buildconfig.Util.getLstFileContents(lstFile
					.getLocation(), files, options, links);
		} catch (FileNotFoundException e) {
			return;
		}

		IProject pro = pbc.getJavaProject().getProject();
		IPath lstFileFolder = lstFile.getFullPath().removeLastSegments(1);
		int segs = lstFileFolder.matchingFirstSegments(pro.getFullPath());
		IPath relPath = lstFileFolder.removeFirstSegments(segs);

		Iterator iter;
		iter = files.iterator();
		while (iter.hasNext()) {
			String filename = (String) iter.next();
			IFile f = pro.getFile(relPath.append(filename));
			if (f.exists()) {
				getFileList().add(f);
			}
		}

		//read linked lst files
		iter = links.iterator();
		while (iter.hasNext()) {
			String filename = ((String) iter.next()).substring(1);
			IFile f = pro.getFile(relPath.append(filename));
			if (f.exists()) {
				addLStFileContentsToFileList(f);
			}
		}
	}

	//creates new BuildConfiguration and askes user for name
	public BuildConfiguration(final ProjectBuildConfigurator pbc, Shell parentShell)
			throws BuildConfigurationCreationException {
		this.pbc = pbc;

		String title = AspectJUIPlugin
				.getResourceString("BCDialog.SaveBuildConfigurationAs.title"); //$NON-NLS-1$
		String msg = AspectJUIPlugin
				.getResourceString("BCDialog.SaveBuildConfigurationAs.message"); //$NON-NLS-1$
		String fileName = BuildConfigurator.getFreeFileName(pbc.getJavaProject().getProject());

		BuildConfiguration origBC = pbc.getActiveBuildConfiguration();
//
//		final String forbiddenName = origBC.getName();
		IInputValidator validator = new IInputValidator() {
			public String isValid(String input) {
				IFile[] files = pbc.getConfigurationFiles();
				for (int i = 0; i < files.length; i++) {
					IFile file = files[i];
					String name = files[i].getName();
					if (file.getName().equals(input)
							|| file.getName()  //$NON-NLS-1$
									.equals(input + "." + BuildConfiguration.EXTENSION)) {
						return AspectJUIPlugin
								.getResourceString("BCDialog.NameValidator.ExistsError"); //$NON-NLS-1$
					}
				}
				return null;				
			}
		};

		InputDialogWithCheck md = new InputDialogWithCheck(parentShell, title, msg, fileName,
				validator);
		md.setBlockOnOpen(true);
		if (md.open() == Window.OK) {
			if (md.getValue() != null) {
			
				String newName = md.getValue();
				this.name = newName;
	
				IFile origFile = origBC.getFile();
				IFile newFile = this.getFile();
				if (newFile.exists()) {
					if (!askUserOverwrite(newFile.getName())) {
						throw new BuildConfigurationCreationException();
					} else {
						try {
							newFile.delete(true, null);
						} catch (CoreException e1) {
						}
					}
				}
				try {
					origFile.copy(newFile.getFullPath(), true, null);
					this.propertiesFile = new BuildProperties(newFile);
				} catch (CoreException e) {
					//could not copy file, try another way to create it
					this.propertiesFile = new BuildProperties(
							getFileFromName(name), new ArrayList(getFileList()));
				}
				fileList = new HashSet(propertiesFile.getFiles(false));
				pbc.addBuildConfiguration(this);
				if (md.isActivateChecked()) {
					pbc.setActiveBuildConfiguration(this);
				}
				if (md.isOpenInEditorChecked()) {
					IWorkbenchWindow dwindow = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow();
					IWorkbenchPage page = dwindow.getActivePage();
					if (page != null) 
						try {
							IDE.openEditor(page,newFile,true);
						}
						catch (PartInitException pie) {}
				}
			} else {
				throw new BuildConfigurationCreationException();
			}
		}
	}
	
	/**
	 * Creates a build configuration with the given name
	 * @param name
	 * @param pbc
	 */
	public BuildConfiguration (IFile fileToUse, ProjectBuildConfigurator pbc, boolean makeActive) {
		this.pbc = pbc;
		name = getNameFromFile(fileToUse);
		IProject project = fileToUse.getProject();
		fileList = BuildConfigurator.getBuildConfigurator()
				.getInitialFileList();
		if(fileList != null) {
			propertiesFile = new BuildProperties(
				fileToUse, new ArrayList(getFileList()));
		} else {
			try {
				IJavaProject jp = JavaCore.create(project);		
				IClasspathEntry[] cpes2 = jp.getRawClasspath();
				if (BuildConfigurator.getBuildConfigurator()
						.haveExclusionPatterns(cpes2)) {
					fileList = BuildConfigurator.getBuildConfigurator()
							.getFileSetFromCPE(cpes2, jp);
					propertiesFile = new BuildProperties(fileToUse,
							minimizeIncludes(fileList));
				} else {
					List pathes = new ArrayList();
					for (int i = 0; i < cpes2.length; i++) {
						if (cpes2[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
							IPath p = cpes2[i].getPath();
							IResource res = jp.getProject().getParent()
									.findMember(p);
							if ((res != null) && res.exists())
								pathes.add(res.getProjectRelativePath());
						}
					}
					propertiesFile = new BuildProperties(
							fileToUse, pathes);
				}
			} catch (JavaModelException jme) {}
		}
		
		pbc.addBuildConfiguration(this);
		if (makeActive)
			pbc.setActiveBuildConfiguration(this);		

	}
	
	IFile getFileFromName(String name) {
		return pbc.getJavaProject().getProject().getFile(
				name + "." + BuildConfiguration.EXTENSION); //$NON-NLS-1$
	}

	static String getNameFromFile(IFile file) {
		String n = file.getName();
		return n.substring(0, n.indexOf("." + EXTENSION)); //$NON-NLS-1$
	}

	public void loadDataFromFile(IFile existingFile) {
		if (existingFile.exists()) {
			name = getNameFromFile(existingFile);
			propertiesFile = new BuildProperties(existingFile);
		}
	}

	private String getLstContentFromData() {
		String files = ""; //$NON-NLS-1$
		Iterator iter = getFileList().iterator();
		while (iter.hasNext()) {
			files = files.concat(((IFile) iter.next()).getProjectRelativePath()
					.toOSString().concat(System.getProperty("line.separator", "\n"))); //$NON-NLS-1$ //$NON-NLS-2$
			;
		}
		return files;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	//adds an exclusion filter for a given file to a given classpathentry[]
	private boolean excludeFile(IResource file) throws CoreException {
		listenToFileChanges = false;
		propertiesFile.exclude(file);
		listenToFileChanges = true;
		if (file.getType() == IResource.FILE) {
			getFileList().remove(file);
			return true;
		}
		return false;
	}

	public void excludeFiles(List exFiles) throws CoreException {
		Iterator iter = exFiles.iterator();
		boolean needsupdate = false;
		IResource res = null;
		while (iter.hasNext()) {
			res = (IResource) iter.next();
			if (!excludeFile(res))
				needsupdate = true;
		}
		if (needsupdate) {
			update(false);
		}
		
		propertiesFile.writeFile();
	}

	//adds an exclusion filter for a given file to a given classpathentry[]
	private boolean includeFile(IResource file) {
		listenToFileChanges = false;
		propertiesFile.include(file);
		listenToFileChanges = true;
		if (file.getType() == IResource.FILE) {
			getFileList().add(file);
			return true;
		}
		return false;
	}

	public void includeFiles(List exFiles) {
		Iterator iter = exFiles.iterator();
		boolean needsupdate = false;
		IResource res = null;
		while (iter.hasNext()) {
			res = (IResource) iter.next();
			if (!includeFile(res))
				needsupdate = true;
		}
		if (needsupdate) {
			update(false);
		}
		
		propertiesFile.writeFile();
	}

	public boolean isIncluded(IResource file) {
		//System.out.println("include check");
		boolean isIncl = getFileList().contains(file);
		return isIncl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(org.eclipse.jdt.internal.core.util.Util.Comparable o) {
		if (o instanceof BuildConfiguration) {
			return getName().compareToIgnoreCase(
					((BuildConfiguration) o).getName());
		}
		return 0;
	}

	/**
	 *  
	 */
	public void update(boolean forceReadingFile) {
		// mark file list as dirty as it is (probably) about to change
		AspectJUIPlugin.getDefault().getAjdtProjectProperties()
				.setProjectSourceFileListKnown(
						pbc.getJavaProject().getProject(), false);

		if (listenToFileChanges == true) {
			if (fileList == null) {
				fileList = new HashSet();
			} else {
				fileList.clear();
			}
			List l = propertiesFile.getFiles(forceReadingFile);
			fileList.addAll(l);
		}
	}

	//	returns a list of java.io.Files containing the selected files
	public List getIncludedJavaFiles(CoreUtils.FilenameFilter filter) {
		Iterator iter = getFileList().iterator();
		ArrayList list = new ArrayList(getFileList().size());
		while (iter.hasNext()) {
			IResource res = (IResource) iter.next();
			if (res.exists() && filter.accept(res.getName()))
				list.add(new File(res.getLocation().toOSString()));
		}
		return list;
	}

	//returns a list of strings containing the file names
	public List getIncludedJavaFileNames(CoreUtils.FilenameFilter filter) {
		Iterator iter = getFileList().iterator();
		ArrayList list = new ArrayList(getFileList().size());
		while (iter.hasNext()) {
			IResource res = (IResource) iter.next();
			if (res.exists() && filter.accept(res.getName()))
				list.add(res.getLocation().toOSString());
		}
		return list;
	}

	/**
	 *  
	 */
	public void writeLstFile() {
		update(false);
		String lstFileContent = this.getLstContentFromData();

		String title = AspectJUIPlugin
				.getResourceString("BCDialog.SaveAJPropAsLst.title"); //$NON-NLS-1$
		String msg = AspectJUIPlugin.getResourceString(
				"BCDialog.SaveAJPropAsLst.message").replaceAll("%name", name); //$NON-NLS-1$ //$NON-NLS-2$

		InputDialog md = new InputDialog(null, title, msg, name + ".lst", null); //$NON-NLS-1$
		md.setBlockOnOpen(true);
		if ((md.open() == Window.OK) && (md.getValue() != null)) {
			String fileName = md.getValue();
			if (!fileName.endsWith(".lst")) { //$NON-NLS-1$
				fileName = fileName + ".lst"; //$NON-NLS-1$
			}
			try {
				IFile file = pbc.getJavaProject().getProject()
						.getFile(fileName);
				if (file.exists()) {
					if (askUserOverwrite(fileName)) {
						InputStream inputStream = new ByteArrayInputStream(
								lstFileContent.getBytes());
						if (file.isReadOnly()) {
							ResourcesPlugin.getWorkspace().validateEdit(
									new IFile[] { file }, null);
						}
						file.setContents(inputStream, IResource.FORCE, null);
					}
				} else {
					InputStream inputStream = new ByteArrayInputStream(
							lstFileContent.getBytes());
					file.create(inputStream, IResource.FORCE, null);
				}
			} catch (CoreException e) {
				AspectJUIPlugin.getDefault().getErrorHandler().handleError(
						AspectJUIPlugin.getResourceString(
								"buildConfig.exceptionWriting").replaceAll( //$NON-NLS-1$
								"%fileName", fileName), e); //$NON-NLS-1$
			}

		}

	}

	private boolean askUserOverwrite(String fileName) {
		String[] options = {
				AspectJUIPlugin.getResourceString("BCDialog.Overwrite.yes"), //$NON-NLS-1$
				AspectJUIPlugin.getResourceString("BCDialog.Overwrite.no") }; //$NON-NLS-1$
		String title = AspectJUIPlugin
				.getResourceString("BCDialog.Overwrite.title"); //$NON-NLS-1$
		String msg = AspectJUIPlugin.getResourceString(
				"BCDialog.Overwrite.message").replaceAll("%fileName", fileName); //$NON-NLS-1$ //$NON-NLS-2$

		MessageDialog mdiag = new MessageDialog(null, title, null, msg,
				MessageDialog.QUESTION, options, 1);
		return (mdiag.open() == 0);
	}

	/**
	 * @return
	 */
	public boolean areFilesActive() {
		return (getFileList().size() != 0);
	}

	/**
	 *  
	 */
	public void updateSourceFolders(List sourcePathes) {
		propertiesFile.updateSourceFolders(sourcePathes);
		fileList = null;
		//pbc.configurationChanged(this);
	}
	
	private class InputDialogWithCheck extends InputDialog {
		private Button activateCheckbox;
		private Button openInEditorCheckbox;
		private boolean activateSelected;
		private boolean openInEditorSelected;
		
		/**
		 * @param parentShell
		 * @param dialogTitle
		 * @param dialogMessage
		 * @param initialValue
		 * @param validator
		 */
		public InputDialogWithCheck(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator) {
			super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
		}
		
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			openInEditorCheckbox = new Button(composite, SWT.CHECK);
			openInEditorCheckbox.setSelection(true);
			openInEditorCheckbox.setText(AspectJUIPlugin
					.getResourceString("BuildConfig.openForEdit")); //$NON-NLS-1$	
			activateCheckbox = new Button(composite, SWT.CHECK);
			activateCheckbox.setSelection(true);
			activateCheckbox.setText(AspectJUIPlugin
					.getResourceString("BuildConfig.activate")); //$NON-NLS-1$
			return composite;
		}
		
		protected void okPressed() {
			activateSelected = activateCheckbox.getSelection();
			openInEditorSelected = openInEditorCheckbox.getSelection();
			super.okPressed();
		}
		/**
		 * Is the checkbox selected?
		 * @return
		 */
		public boolean isActivateChecked() {
			return activateSelected;
		}
		
		public boolean isOpenInEditorChecked() {
			return openInEditorSelected;
		}
	}
}