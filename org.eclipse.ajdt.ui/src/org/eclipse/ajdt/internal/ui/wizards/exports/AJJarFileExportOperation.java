/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui.wizards.exports;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ISourceAttribute;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.IJavaStatusConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jdt.ui.jarpackager.IJarDescriptionWriter;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.jdt.ui.jarpackager.JarWriter3;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Copied from org.eclipse.jdt.internal.ui.jarpackager.JarFileExportOperation.
 * Changes marked with // AspectJ Change.
 */
public class AJJarFileExportOperation extends WorkspaceModifyOperation implements IJarExportRunnable {

	private static class MessageMultiStatus extends MultiStatus {
		MessageMultiStatus(String pluginId, int code, String message, Throwable exception) {
			super(pluginId, code, message, exception);
		}
		/*
		 * allows to change the message
		 */
		protected void setMessage(String message) {
			super.setMessage(message);
		}
	}

	private JarWriter3 fJarWriter;
	private JarPackageData fJarPackage;
	private JarPackageData[] fJarPackages;
	private final Shell fParentShell;
	private Map fJavaNameToClassFilesMap;
	private IContainer fClassFilesMapContainer;
	private Set<IContainer> fExportedClassContainers;
	private final MessageMultiStatus fStatus;
	private final StandardJavaElementContentProvider fJavaElementContentProvider;
	private boolean fFilesSaved;

	/**
	 * Creates an instance of this class.
	 *
	 * @param	jarPackage	the JAR package specification
	 * @param	parent	the parent for the dialog,
	 * 			or <code>null</code> if no dialog should be presented
	 */
	public AJJarFileExportOperation(JarPackageData jarPackage, Shell parent) {
		this(new JarPackageData[] {jarPackage}, parent);
	}

	/**
	 * Creates an instance of this class.
	 *
	 * @param	jarPackages		an array with JAR package data objects
	 * @param	parent			the parent for the dialog,
	 * 			or <code>null</code> if no dialog should be presented
	 */
	public AJJarFileExportOperation(JarPackageData[] jarPackages, Shell parent) {
		this(parent);
		fJarPackages= jarPackages;
	}

	private AJJarFileExportOperation(Shell parent) {
		fParentShell= parent;
		fStatus= new MessageMultiStatus(JavaPlugin.getPluginId(), IStatus.OK, "", null); //$NON-NLS-1$
		fJavaElementContentProvider= new StandardJavaElementContentProvider();
	}

	protected void addToStatus(CoreException ex) {
		IStatus status= ex.getStatus();
		String message= ex.getLocalizedMessage();
		if (message == null || message.length() < 1) {
			message= JarPackagerMessages.JarFileExportOperation_coreErrorDuringExport;
			status= new Status(status.getSeverity(), status.getPlugin(), status.getCode(), message, ex);
		}
		fStatus.add(status);
	}

	/**
	 * Adds a new info to the list with the passed information.
	 * Normally the export operation continues after a warning.
	 * @param	message		the message
	 * @param	error 	the throwable that caused the warning, or <code>null</code>
	 */
	protected void addInfo(String message, Throwable error) {
		fStatus.add(new Status(IStatus.INFO, JavaPlugin.getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, message, error));
	}

	/**
	 * Adds a new warning to the list with the passed information.
	 * Normally the export operation continues after a warning.
	 * @param	message		the message
	 * @param	error	the throwable that caused the warning, or <code>null</code>
	 */
	protected void addWarning(String message, Throwable error) {
		fStatus.add(new Status(IStatus.WARNING, JavaPlugin.getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, message, error));
	}

	/**
	 * Adds a new error to the list with the passed information.
	 * Normally an error terminates the export operation.
	 * @param	message		the message
	 * @param	error 	the throwable that caused the error, or <code>null</code>
	 */
	protected void addError(String message, Throwable error) {
		fStatus.add(new Status(IStatus.ERROR, JavaPlugin.getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, message, error));
	}

	/**
	 * Answers the number of file resources specified by the JAR package.
	 *
	 * @return int
	 */
	protected int countSelectedElements() {
		Set<IJavaProject> enclosingJavaProjects= new HashSet<>(10);
		int count= 0;

		int n= fJarPackage.getElements().length;
		for (int i= 0; i < n; i++) {
			Object element= fJarPackage.getElements()[i];

			IJavaProject javaProject= getEnclosingJavaProject(element);
			if (javaProject != null)
				enclosingJavaProjects.add(javaProject);

			IResource resource;
			if (element instanceof IJavaElement) {
				IJavaElement je= (IJavaElement)element;
				try {
					resource= je.getUnderlyingResource();
				} catch (JavaModelException ex) {
					continue;
				}

				// Should not happen since we only export source files
				if (resource == null)
					continue;
			}
			else
				resource= (IResource)element;

			if (resource.getType() == IResource.FILE)
				count++;
			else
				count += getTotalChildCount((IContainer)resource);
		}

		if (fJarPackage.areOutputFoldersExported()) {
			if (!fJarPackage.areJavaFilesExported())
				count= 0;
      for (IJavaProject javaProject : enclosingJavaProjects) {
        IContainer[] outputContainers;
        try {
          outputContainers = getOutputContainers(javaProject);
        }
        catch (CoreException ex) {
          addToStatus(ex);
          continue;
        }
        for (IContainer outputContainer : outputContainers)
          count += getTotalChildCount(outputContainer);

      }
		}

		return count;
	}

	private int getTotalChildCount(IContainer container) {
		IResource[] members;
		try {
			members= container.members();
		} catch (CoreException ex) {
			return 0;
		}
		int count= 0;
    for (IResource member : members) {
      if (member.getType() == IResource.FILE)
        count++;
      else
        count += getTotalChildCount((IContainer) member);
    }
		return count;
	}

	/**
	 * Exports the passed resource to the JAR file
	 *
	 * @param element the resource or JavaElement to export
	 */
	protected void exportElement(Object element, IProgressMonitor progressMonitor) throws InterruptedException {
        // AspectJ Change Begin
        if (!AspectJPlugin.USING_CU_PROVIDER) {
            // Don't export AJCompilationUnits because they are duplicates of files that we also export.
            if (element instanceof AJCompilationUnit) {
                return;
            }
        }
        // AspectJ Change End
		int leadSegmentsToRemove= 1;
		IPackageFragmentRoot pkgRoot= null;
		boolean isInJavaProject= false;
		IResource resource;
		IJavaProject jProject= null;
		if (element instanceof IJavaElement) {
			isInJavaProject= true;
			IJavaElement je= (IJavaElement)element;
			int type= je.getElementType();
			if (type != IJavaElement.CLASS_FILE && type != IJavaElement.COMPILATION_UNIT) {
				exportJavaElement(progressMonitor, je);
				return;
			}
			try {
				resource= je.getUnderlyingResource();
			} catch (JavaModelException ex) {
				addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_resourceNotFound, je.getElementName()), ex);
				return;
			}
			jProject= je.getJavaProject();
			pkgRoot= JavaModelUtil.getPackageFragmentRoot(je);
		}
		else
			resource= (IResource)element;

		if (!resource.isAccessible()) {
			addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_resourceNotFound, resource.getFullPath()), null);
			return;
		}

		if (resource.getType() == IResource.FILE) {
			if (!isInJavaProject) {
				// check if it's a Java resource
				try {
					isInJavaProject= resource.getProject().hasNature(JavaCore.NATURE_ID);
				} catch (CoreException ex) {
					addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_projectNatureNotDeterminable, resource.getFullPath()), ex);
					return;
				}
				if (isInJavaProject) {
					jProject= JavaCore.create(resource.getProject());
					try {
						IPackageFragment pkgFragment= jProject.findPackageFragment(resource.getFullPath().removeLastSegments(1));
						if (pkgFragment != null)
							pkgRoot= JavaModelUtil.getPackageFragmentRoot(pkgFragment);
						else
							pkgRoot= findPackageFragmentRoot(jProject, resource.getFullPath().removeLastSegments(1));
					} catch (JavaModelException ex) {
						addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_javaPackageNotDeterminable, resource.getFullPath()), ex);
						return;
					}
				}
			}

			if (pkgRoot != null && jProject != null) {
				leadSegmentsToRemove= pkgRoot.getPath().segmentCount();
				boolean isOnBuildPath;
				isOnBuildPath= jProject.isOnClasspath(resource);
				if (!isOnBuildPath || (mustUseSourceFolderHierarchy() && !pkgRoot.getElementName().equals(IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH)))
					leadSegmentsToRemove--;
			}

			IPath destinationPath= resource.getFullPath().removeFirstSegments(leadSegmentsToRemove);

			boolean isInOutputFolder= false;
			if (isInJavaProject && jProject != null) {
				try {
					isInOutputFolder= jProject.getOutputLocation().isPrefixOf(resource.getFullPath());
				} catch (JavaModelException ex) {
					isInOutputFolder= false;
				}
			}

			exportClassFiles(progressMonitor, pkgRoot, resource, jProject, destinationPath);
			exportResource(progressMonitor, pkgRoot, isInJavaProject, resource, destinationPath, isInOutputFolder);

			progressMonitor.worked(1);
			ModalContext.checkCanceled(progressMonitor);

		} else
			exportContainer(progressMonitor, (IContainer)resource);
	}

	private void exportJavaElement(IProgressMonitor progressMonitor, IJavaElement je) throws InterruptedException {
		if (je.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT && ((IPackageFragmentRoot)je).isArchive())
			return;

		Object[] children= fJavaElementContentProvider.getChildren(je);
    for (Object child : children)
      exportElement(child, progressMonitor);
	}

	private void exportResource(IProgressMonitor progressMonitor, IResource resource, int leadingSegmentsToRemove) throws InterruptedException {
		if (resource instanceof IContainer) {
			IContainer container= (IContainer)resource;
			IResource[] children;
			try {
				children= container.members();
			} catch (CoreException e) {
				// this should never happen because an #isAccessible check is done before #members is invoked
				addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_errorDuringExport, container.getFullPath()), e);
				return;
			}
      for (IResource child : children)
        exportResource(progressMonitor, child, leadingSegmentsToRemove);
		} else if (resource instanceof IFile) {
			try {
				IPath destinationPath= resource.getFullPath().removeFirstSegments(leadingSegmentsToRemove);
				progressMonitor.subTask(Messages.format(JarPackagerMessages.JarFileExportOperation_exporting, destinationPath.toString()));
				fJarWriter.write((IFile)resource, destinationPath);
			} catch (CoreException ex) {
				Throwable realEx= ex.getStatus().getException();
				if (realEx instanceof ZipException && realEx.getMessage() != null && realEx.getMessage().startsWith("duplicate entry:")) //$NON-NLS-1$
					addWarning(ex.getMessage(), realEx);
				else
					addToStatus(ex);
			} finally {
				progressMonitor.worked(1);
				ModalContext.checkCanceled(progressMonitor);
			}
		}
	}

	private void exportContainer(IProgressMonitor progressMonitor, IContainer container) throws InterruptedException {
		if (container.getType() == IResource.FOLDER && isOutputFolder((IFolder)container))
			return;

		IResource[] children;
		try {
			children= container.members();
      for (IResource child : children)
        exportElement(child, progressMonitor);
		} catch (CoreException e) {
			// this should never happen because an #isAccessible check is done before #members is invoked
			addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_errorDuringExport, container.getFullPath()), e);
		}
	}

	private IPackageFragmentRoot findPackageFragmentRoot(IJavaProject jProject, IPath path) throws JavaModelException {
		if (jProject == null || path == null || path.segmentCount() <= 0)
			return null;
		IPackageFragmentRoot pkgRoot= jProject.findPackageFragmentRoot(path);
		if (pkgRoot != null)
			return pkgRoot;
		else
			return findPackageFragmentRoot(jProject, path.removeLastSegments(1));
	}

	private void exportResource(IProgressMonitor progressMonitor, IPackageFragmentRoot pkgRoot, boolean isInJavaProject, IResource resource, IPath destinationPath, boolean isInOutputFolder) {

		// Handle case where META-INF/MANIFEST.MF is part of the exported files
		if (fJarPackage.areClassFilesExported() && destinationPath.toString().equals("META-INF/MANIFEST.MF")) {//$NON-NLS-1$
			if (fJarPackage.isManifestGenerated())
				addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_didNotAddManifestToJar, resource.getFullPath()), null);
			return;
		}

		boolean isNonJavaResource= !isInJavaProject || pkgRoot == null;
		boolean isInClassFolder= false;
		try {
			isInClassFolder= pkgRoot != null && !pkgRoot.isArchive() && pkgRoot.getKind() == IPackageFragmentRoot.K_BINARY;
		} catch (JavaModelException ex) {
			addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_cantGetRootKind, resource.getFullPath()), ex);
		}
		if ((fJarPackage.areClassFilesExported() &&
					((isNonJavaResource || (pkgRoot != null && !isJavaFile(resource) && !isClassFile(resource)))
					|| isInClassFolder && isClassFile(resource)))
			|| (fJarPackage.areJavaFilesExported() && (isNonJavaResource || (pkgRoot != null && !isClassFile(resource)) || (isInClassFolder && isClassFile(resource) && !fJarPackage.areClassFilesExported())))) {
			try {
				progressMonitor.subTask(Messages.format(JarPackagerMessages.JarFileExportOperation_exporting, destinationPath.toString()));
				fJarWriter.write((IFile) resource, destinationPath);
			} catch (CoreException ex) {
				Throwable realEx= ex.getStatus().getException();
				if (realEx instanceof ZipException && realEx.getMessage() != null && realEx.getMessage().startsWith("duplicate entry:")) //$NON-NLS-1$
					addWarning(ex.getMessage(), realEx);
				else
					addToStatus(ex);
			}
		}
	}

	private boolean isOutputFolder(IFolder folder) {
		try {
			IJavaProject javaProject= JavaCore.create(folder.getProject());
			IPath outputFolderPath= javaProject.getOutputLocation();
			return folder.getFullPath().equals(outputFolderPath);
		} catch (JavaModelException ex) {
			return false;
		}
	}

	private void exportClassFiles(IProgressMonitor progressMonitor, IPackageFragmentRoot pkgRoot, IResource resource, IJavaProject jProject, IPath destinationPath) {
		if (fJarPackage.areClassFilesExported() && isJavaFile(resource) && pkgRoot != null) {
			try {
				if (!jProject.isOnClasspath(resource))
					return;

				// find corresponding file(s) on classpath and export
				Iterator<IFile> iter= filesOnClasspath((IFile)resource, destinationPath, jProject, pkgRoot, progressMonitor);
				IPath baseDestinationPath= destinationPath.removeLastSegments(1);
				while (iter.hasNext()) {
					IFile file= iter.next();
					IPath classFilePath= baseDestinationPath.append(file.getName());
					progressMonitor.subTask(Messages.format(JarPackagerMessages.JarFileExportOperation_exporting, classFilePath.toString()));
					fJarWriter.write(file, classFilePath);
				}
			} catch (CoreException ex) {
				addToStatus(ex);
			}
		}
	}

	/**
	 * Exports the resources as specified by the JAR package.
	 */
	protected void exportSelectedElements(IProgressMonitor progressMonitor) throws InterruptedException {
		fExportedClassContainers= new HashSet<>(10);
		Set<IJavaProject> enclosingJavaProjects= new HashSet<>(10);
		int n= fJarPackage.getElements().length;
		for (int i= 0; i < n; i++) {
			Object element= fJarPackage.getElements()[i];
			exportElement(element, progressMonitor);
			if (fJarPackage.areOutputFoldersExported()) {
				IJavaProject javaProject= getEnclosingJavaProject(element);
				if (javaProject != null)
					enclosingJavaProjects.add(javaProject);
			}
		}
		if (fJarPackage.areOutputFoldersExported())
			exportOutputFolders(progressMonitor, enclosingJavaProjects);
	}

	private IJavaProject getEnclosingJavaProject(Object element) {
		if (element instanceof IJavaElement) {
			return ((IJavaElement)element).getJavaProject();
		} else if (element instanceof IResource) {
			IProject project= ((IResource)element).getProject();
			try {
				if (project.hasNature(JavaCore.NATURE_ID))
					return JavaCore.create(project);
			} catch (CoreException ex) {
				addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_projectNatureNotDeterminable, project.getFullPath()), ex);
			}
		}
		return null;
	}

	private void exportOutputFolders(IProgressMonitor progressMonitor, Set<IJavaProject> javaProjects) throws InterruptedException {
		if (javaProjects == null)
			return;

    for (IJavaProject javaProject : javaProjects) {
      IContainer[] outputContainers;
      try {
        outputContainers = getOutputContainers(javaProject);
      }
      catch (CoreException ex) {
        addToStatus(ex);
        continue;
      }
      for (IContainer outputContainer : outputContainers)
        exportResource(progressMonitor, outputContainer, outputContainer.getFullPath().segmentCount());

    }
	}

	private IContainer[] getOutputContainers(IJavaProject javaProject) throws CoreException {
		Set<IPath> outputPaths= new HashSet<>();
		boolean includeDefaultOutputPath= false;
		IPackageFragmentRoot[] roots= javaProject.getPackageFragmentRoots();
    for (IPackageFragmentRoot root : roots) {
      if (root != null) {
        IClasspathEntry cpEntry = root.getRawClasspathEntry();
        if (cpEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
          IPath location = cpEntry.getOutputLocation();
          if (location != null)
            outputPaths.add(location);
          else
            includeDefaultOutputPath = true;
        }
      }
    }

		if (includeDefaultOutputPath) {
			// Use default output location
			outputPaths.add(javaProject.getOutputLocation());
		}

		// Convert paths to containers
		Set<IContainer> outputContainers= new HashSet<>(outputPaths.size());
    for (IPath path : outputPaths) {
      if (javaProject.getProject().getFullPath().equals(path))
        outputContainers.add(javaProject.getProject());
      else {
        IFolder outputFolder = createFolderHandle(path);
        if (outputFolder == null || !outputFolder.isAccessible()) {
          String msg = JarPackagerMessages.JarFileExportOperation_outputContainerNotAccessible;
          addToStatus(new CoreException(new Status(IStatus.ERROR, JavaPlugin.getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, msg, null)));
        }
        else
          outputContainers.add(outputFolder);
      }
    }
		return outputContainers.toArray(new IContainer[0]);
	}

	/**
	 * Returns an iterator on a list with files that correspond to the
	 * passed file and that are on the classpath of its project.
	 *
	 * @param	file			the file for which to find the corresponding classpath resources
	 * @param	pathInJar		the path that the file has in the JAR (i.e. project and source folder segments removed)
	 * @param	javaProject		the javaProject that contains the file
	 * @return	the iterator over the corresponding classpath files for the given file
	 * @deprecated As of 2.1 use the method with additional IPackageFragmentRoot paramter
	 */
	protected Iterator filesOnClasspath(IFile file, IPath pathInJar, IJavaProject javaProject, IProgressMonitor progressMonitor) throws CoreException {
		return filesOnClasspath(file, pathInJar, javaProject, null, progressMonitor);
	}

	/**
   * Returns an iterator on a list with files that correspond to the
   * passed file and that are on the classpath of its project.
   *
   * @param  file      the file for which to find the corresponding classpath resources
   * @param  pathInJar    the path that the file has in the JAR (i.e. project and source folder segments removed)
   * @param  javaProject    the javaProject that contains the file
   * @param  pkgRoot      the package fragment root that contains the file
   * @return the iterator over the corresponding classpath files for the given file
   */
	protected Iterator filesOnClasspath(IFile file, IPath pathInJar, IJavaProject javaProject, IPackageFragmentRoot pkgRoot, IProgressMonitor progressMonitor) throws CoreException {
		// Allow JAR Package to provide its own strategy
		IFile[] classFiles= fJarPackage.findClassfilesFor(file);
		if (classFiles != null)
			return Arrays.asList(classFiles).iterator();

		if (!isJavaFile(file))
			return Collections.emptyIterator();

		IPath outputPath= null;
		if (pkgRoot != null) {
			IClasspathEntry cpEntry= pkgRoot.getRawClasspathEntry();
			if (cpEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
				outputPath= cpEntry.getOutputLocation();
		}
		if (outputPath == null)
			// Use default output location
			outputPath= javaProject.getOutputLocation();

		IContainer outputContainer;
		if (javaProject.getProject().getFullPath().equals(outputPath))
			outputContainer= javaProject.getProject();
		else {
			outputContainer= createFolderHandle(outputPath);
			if (outputContainer == null || !outputContainer.isAccessible()) {
				String msg= JarPackagerMessages.JarFileExportOperation_outputContainerNotAccessible;
				throw new CoreException(new Status(IStatus.ERROR, JavaPlugin.getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, msg, null));
			}
		}

		// Java CU - search files with .class ending
		boolean hasErrors= hasCompileErrors(file);
		boolean hasWarnings= hasCompileWarnings(file);
		boolean canBeExported= canBeExported(hasErrors, hasWarnings);
		if (!canBeExported)
			return Collections.emptyIterator();
		reportPossibleCompileProblems(file, hasErrors, hasWarnings, canBeExported);
		IContainer classContainer= outputContainer;
		if (pathInJar.segmentCount() > 1)
			classContainer= outputContainer.getFolder(pathInJar.removeLastSegments(1));

		if (fExportedClassContainers.contains(classContainer))
			return Collections.emptyIterator();

		if (fClassFilesMapContainer == null || !fClassFilesMapContainer.equals(classContainer)) {
			fJavaNameToClassFilesMap= buildJavaToClassMap(classContainer);
			if (fJavaNameToClassFilesMap == null) {
				// Could not fully build map. fallback is to export whole directory
				IPath location= classContainer.getLocation();
				String containerName= "";  //$NON-NLS-1$
				if (location != null)
					containerName= location.toFile().toString();
				String msg= Messages.format(JarPackagerMessages.JarFileExportOperation_missingSourceFileAttributeExportedAll, containerName);
				addInfo(msg, null);
				fExportedClassContainers.add(classContainer);
				return getClassesIn(classContainer);
			}
			fClassFilesMapContainer= classContainer;
		}
		ArrayList classFileList= (ArrayList)fJavaNameToClassFilesMap.get(file.getName());
		if (classFileList == null || classFileList.isEmpty()) {
			String msg= Messages.format(JarPackagerMessages.JarFileExportOperation_classFileOnClasspathNotAccessible, file.getFullPath());
			throw new CoreException(new Status(IStatus.ERROR, JavaPlugin.getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, msg, null));
		}
		return classFileList.iterator();
	}

	private Iterator<IFile> getClassesIn(IContainer classContainer) throws CoreException {
		IResource[] resources= classContainer.members();
		List<IFile> files= new ArrayList<>(resources.length);
    for (IResource resource : resources)
      if (resource.getType() == IResource.FILE && isClassFile(resource))
        files.add((IFile) resource);
		return files.iterator();
	}

	/**
	 * Answers whether the given resource is a Java file.
	 * The resource must be a file whose file name ends with ".java".
	 *
	 * @return a <code>true<code> if the given resource is a Java file
	 */
	boolean isJavaFile(IResource file) {
		return file != null
			&& file.getType() == IResource.FILE
			&& file.getFileExtension() != null
			// AspectJ Change Begin
			&& (file.getFileExtension().equalsIgnoreCase("java") //$NON-NLS-1$
					|| file.getFileExtension().equalsIgnoreCase("aj")); //$NON-NLS-1$
			// AspectJ Change End
	}

	/**
	 * Answers whether the given resource is a class file.
	 * The resource must be a file whose file name ends with ".class".
	 *
	 * @return a <code>true<code> if the given resource is a class file
	 */
	boolean isClassFile(IResource file) {
		return file != null
			&& file.getType() == IResource.FILE
			&& file.getFileExtension() != null
			&& file.getFileExtension().equalsIgnoreCase("class"); //$NON-NLS-1$
	}

	/*
	 * Builds and returns a map that has the class files
	 * for each java file in a given directory
	 */
	private Map<String, ArrayList<IFile>> buildJavaToClassMap(IContainer container) throws CoreException {
		if (container == null || !container.isAccessible())
			return new HashMap<>(0);
		/*
		 * XXX: Bug 6584: Need a way to get class files for a java file (or CU)
		 */
		IClassFileReader cfReader;
		IResource[] members= container.members();
		Map<String, ArrayList<IFile>> map= new HashMap<>(members.length);
    for (IResource member : members) {
      if (isClassFile(member)) {
        IFile classFile = (IFile) member;
        IPath location = classFile.getLocation();
        if (location != null) {
          File file = location.toFile();
          cfReader = ToolFactory.createDefaultClassFileReader(location.toOSString(), IClassFileReader.CLASSFILE_ATTRIBUTES);
          if (cfReader != null) {
            ISourceAttribute sourceAttribute = cfReader.getSourceFileAttribute();
            if (sourceAttribute == null) {
              /*
               * Can't fully build the map because one or more
               * class file does not contain the name of its
               * source file.
               */
              addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_classFileWithoutSourceFileAttribute, file), null);
              return null;
            }
            String javaName = new String(sourceAttribute.getSourceFileName());
            ArrayList<IFile> classFiles = map.computeIfAbsent(javaName, k -> new ArrayList<>(3));
            classFiles.add(classFile);
          }
        }
      }
    }
		return map;
	}

	/**
	 * Creates a file resource handle for the file with the given workspace path.
	 * This method does not create the file resource; this is the responsibility
	 * of <code>createFile</code>.
	 *
	 * @param filePath the path of the file resource to create a handle for
	 * @return the new file resource handle
	 */
	protected IFile createFileHandle(IPath filePath) {
		if (filePath.isValidPath(filePath.toString()) && filePath.segmentCount() >= 2)
			return JavaPlugin.getWorkspace().getRoot().getFile(filePath);
		else
			return null;
	}

	/**
	 * Creates a folder resource handle for the folder with the given workspace path.
	 *
	 * @param folderPath the path of the folder to create a handle for
	 * @return the new folder resource handle
	 */
	protected IFolder createFolderHandle(IPath folderPath) {
		if (folderPath.isValidPath(folderPath.toString()) && folderPath.segmentCount() >= 2)
			return JavaPlugin.getWorkspace().getRoot().getFolder(folderPath);
		else
			return null;
	}

	/**
	 * Returns the status of this operation.
	 * The result is a status object containing individual
	 * status objects.
	 *
	 * @return the status of this operation
	 */
	public IStatus getStatus() {
		String message;
		switch (fStatus.getSeverity()) {
			case IStatus.OK:
				message= ""; //$NON-NLS-1$
				break;
			case IStatus.INFO:
				message= JarPackagerMessages.JarFileExportOperation_exportFinishedWithInfo;
				break;
			case IStatus.WARNING:
				message= JarPackagerMessages.JarFileExportOperation_exportFinishedWithWarnings;
				break;
			case IStatus.ERROR:
				if (fJarPackages.length > 1)
					message= JarPackagerMessages.JarFileExportOperation_creationOfSomeJARsFailed;
				else
					message= JarPackagerMessages.JarFileExportOperation_jarCreationFailed;
				break;
			default:
				// defensive code in case new severity is defined
				message= ""; //$NON-NLS-1$
				break;
		}
		fStatus.setMessage(message);
		return fStatus;
	}

	/**
	 * Answer a boolean indicating whether the passed child is a descendant
	 * of one or more members of the passed resources collection
	 *
	 * @param	resources	a List contain potential parents
	 * @param	child		the resource to test
	 * @return	a <code>boolean</code> indicating if the child is a descendant
	 */
	protected boolean isDescendant(List<IResource> resources, IResource child) {
		if (child.getType() == IResource.PROJECT)
			return false;

		IResource parent= child.getParent();
		if (resources.contains(parent))
			return true;

		return isDescendant(resources, parent);
	}

	protected boolean canBeExported(boolean hasErrors, boolean hasWarnings) throws CoreException {
		return (!hasErrors && !hasWarnings)
			|| (hasErrors && fJarPackage.areErrorsExported())
			|| (hasWarnings && fJarPackage.exportWarnings());
	}

	protected void reportPossibleCompileProblems(IFile file, boolean hasErrors, boolean hasWarnings, boolean canBeExported) {
		if (hasErrors) {
			if (canBeExported)
				addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_exportedWithCompileErrors, file.getFullPath()), null);
			else
				addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_notExportedDueToCompileErrors, file.getFullPath()), null);
		}
		if (hasWarnings) {
			if (canBeExported)
				addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_exportedWithCompileWarnings, file.getFullPath()), null);
			else
				addWarning(Messages.format(JarPackagerMessages.JarFileExportOperation_notExportedDueToCompileWarnings, file.getFullPath()), null);
		}
	}

	/**
	 * Exports the resources as specified by the JAR package.
	 *
	 * @param	progressMonitor	the progress monitor that displays the progress
	 * @see	#getStatus()
	 */
	protected void execute(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
		int count= fJarPackages.length;
		progressMonitor.beginTask("", count); //$NON-NLS-1$
		try {
      for (JarPackageData jarPackage : fJarPackages) {
        SubProgressMonitor subProgressMonitor = new SubProgressMonitor(progressMonitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
        fJarPackage = jarPackage;
        if (fJarPackage != null)
          singleRun(subProgressMonitor);
      }
		} finally {
			progressMonitor.done();
		}
	}

	@SuppressWarnings("deprecation")
    public void singleRun(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
		try {
			if (!preconditionsOK())
				throw new InvocationTargetException(null, JarPackagerMessages.JarFileExportOperation_jarCreationFailedSeeDetails);
			int totalWork= countSelectedElements();
			if (fJarPackage.areGeneratedFilesExported()
				&& ((!isAutoBuilding() && fJarPackage.isBuildingIfNeeded())
					|| (isAutoBuilding() && fFilesSaved))) {
				int subMonitorTicks= totalWork/10;
				totalWork += subMonitorTicks;
				progressMonitor.beginTask("", totalWork); //$NON-NLS-1$
				SubProgressMonitor subProgressMonitor= new SubProgressMonitor(progressMonitor, subMonitorTicks, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
				buildProjects(subProgressMonitor);
			} else
				progressMonitor.beginTask("", totalWork); //$NON-NLS-1$

			fJarWriter= fJarPackage.createJarWriter3(fParentShell);
			exportSelectedElements(progressMonitor);
			if (getStatus().getSeverity() != IStatus.ERROR) {
				progressMonitor.subTask(JarPackagerMessages.JarFileExportOperation_savingFiles);
				saveFiles();
			}
		} catch (CoreException ex) {
			addToStatus(ex);
		} finally {
			try {
				if (fJarWriter != null)
					fJarWriter.close();
			} catch (CoreException ex) {
				addToStatus(ex);
			}
			progressMonitor.done();
		}
	}

	protected boolean preconditionsOK() {
		if (!fJarPackage.areGeneratedFilesExported() && !fJarPackage.areJavaFilesExported()) {
			addError(JarPackagerMessages.JarFileExportOperation_noExportTypeChosen, null);
			return false;
		}
		if (fJarPackage.getElements() == null || fJarPackage.getElements().length == 0) {
			addError(JarPackagerMessages.JarFileExportOperation_noResourcesSelected, null);
			return false;
		}
		if (fJarPackage.getAbsoluteJarLocation() == null) {
			addError(JarPackagerMessages.JarFileExportOperation_invalidJarLocation, null);
			return false;
		}
		File targetFile= fJarPackage.getAbsoluteJarLocation().toFile();
		if (targetFile.exists() && !targetFile.canWrite()) {
			addError(JarPackagerMessages.JarFileExportOperation_jarFileExistsAndNotWritable, null);
			return false;
		}
		if (!fJarPackage.isManifestAccessible()) {
			addError(JarPackagerMessages.JarFileExportOperation_manifestDoesNotExist, null);
			return false;
		}
		if (!fJarPackage.isMainClassValid(new BusyIndicatorRunnableContext())) {
			addError(JarPackagerMessages.JarFileExportOperation_invalidMainClass, null);
			return false;
		}

		if (fParentShell != null) {
			final boolean[] res= { false };
			fParentShell.getDisplay().syncExec(() -> {
        RefactoringSaveHelper refactoringSaveHelper= new RefactoringSaveHelper(RefactoringSaveHelper.SAVE_ALL_ALWAYS_ASK);
        res[0]= refactoringSaveHelper.saveEditors(fParentShell);
        fFilesSaved= refactoringSaveHelper.didSaveFiles(); /* AJDT 1.7 */
      });
			if (!res[0]) {
				addError(JarPackagerMessages.JarFileExportOperation_fileUnsaved, null);
				return false;
			}
		}

		return true;
	}

	protected void saveFiles() {
		// Save the manifest
		if (fJarPackage.areGeneratedFilesExported() && fJarPackage.isManifestGenerated() && fJarPackage.isManifestSaved()) {
			try {
				saveManifest();
			} catch (CoreException | IOException ex) {
				addError(JarPackagerMessages.JarFileExportOperation_errorSavingManifest, ex);
			}
    }

		// Save the description
		if (fJarPackage.isDescriptionSaved()) {
			try {
				saveDescription();
			} catch (CoreException | IOException ex) {
				addError(JarPackagerMessages.JarFileExportOperation_errorSavingDescription, ex);
			}
    }
	}

	/* AJDT 1.7 remove getDirtyEditors() */

	protected void saveDescription() throws CoreException, IOException {
		// Adjust JAR package attributes
		if (fJarPackage.isManifestReused())
			fJarPackage.setGenerateManifest(false);
		ByteArrayOutputStream objectStreamOutput= new ByteArrayOutputStream();
		IJarDescriptionWriter writer= fJarPackage.createJarDescriptionWriter(objectStreamOutput, "UTF-8"); //$NON-NLS-1$
		ByteArrayInputStream fileInput= null;
		try {
			writer.write(fJarPackage);
			fileInput= new ByteArrayInputStream(objectStreamOutput.toByteArray());
			IFile descriptionFile= fJarPackage.getDescriptionFile();
			if (descriptionFile.isAccessible()) {
				// AspectJ Change Begin
				if (fJarPackage.allowOverwrite() || AJJarPackagerUtil.askForOverwritePermission(fParentShell, descriptionFile.getFullPath().toString()))
				// AspectJ Change End
					descriptionFile.setContents(fileInput, true, true, null);
			} else
				descriptionFile.create(fileInput, true, null);
		} finally {
			if (fileInput != null)
				fileInput.close();
			if (writer != null)
				writer.close();
		}
	}

	protected void saveManifest() throws CoreException, IOException {
		ByteArrayOutputStream manifestOutput= new ByteArrayOutputStream();
		ByteArrayInputStream fileInput= null;
		try {
			Manifest manifest= fJarPackage.getManifestProvider().create(fJarPackage);
			manifest.write(manifestOutput);
			fileInput= new ByteArrayInputStream(manifestOutput.toByteArray());
			IFile manifestFile= fJarPackage.getManifestFile();
			if (manifestFile.isAccessible()) {
				// AspectJ Change Begin
				if (fJarPackage.allowOverwrite() || AJJarPackagerUtil.askForOverwritePermission(fParentShell, manifestFile.getFullPath().toString()))
				// AspectJ Change End
					manifestFile.setContents(fileInput, true, true, null);
			} else
				manifestFile.create(fileInput, true, null);
		} finally {
			if (manifestOutput != null)
				manifestOutput.close();
			if (fileInput != null)
				fileInput.close();
		}
	}

	private boolean isAutoBuilding() {
		return ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding();
	}

	private void buildProjects(IProgressMonitor progressMonitor) {
		Set<IProject> builtProjects= new HashSet<>(10);
		Object[] elements= fJarPackage.getElements();
    for (Object o : elements) {
      IProject project = null;
      Object element = o;
      if (element instanceof IResource)
        project = ((IResource) element).getProject();
      else if (element instanceof IJavaElement)
        project = ((IJavaElement) element).getJavaProject().getProject();
      if (project != null && !builtProjects.contains(project)) {
        try {
          project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, progressMonitor);
        }
        catch (CoreException ex) {
          String message = Messages.format(JarPackagerMessages.JarFileExportOperation_errorDuringProjectBuild, project.getFullPath());
          addError(message, ex);
        }
        finally {
          // don't try to build same project a second time even if it failed
          builtProjects.add(project);
        }
      }
    }
	}

	/**
	 * Tells whether the given resource (or its children) have compile errors.
	 * The method acts on the current build state and does not recompile.
	 *
	 * @param resource the resource to check for errors
	 * @return <code>true</code> if the resource (and its children) are error free
	 * @throws CoreException import org.eclipse.core.runtime.CoreException if there's a marker problem
	 */
	private boolean hasCompileErrors(IResource resource) throws CoreException {
		IMarker[] problemMarkers= resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
    for (IMarker problemMarker : problemMarkers) {
      if (problemMarker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_ERROR)
        return true;
    }
		return false;
	}

	/**
	 * Tells whether the given resource (or its children) have compile errors.
	 * The method acts on the current build state and does not recompile.
	 *
	 * @param resource the resource to check for errors
	 * @return <code>true</code> if the resource (and its children) are error free
	 * @throws CoreException import org.eclipse.core.runtime.CoreException if there's a marker problem
	 */
	private boolean hasCompileWarnings(IResource resource) throws CoreException {
		IMarker[] problemMarkers= resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
    for (IMarker problemMarker : problemMarkers) {
      if (problemMarker.getAttribute(IMarker.SEVERITY, -1) == IMarker.SEVERITY_WARNING)
        return true;
    }
		return false;
	}

	private boolean mustUseSourceFolderHierarchy() {
		return fJarPackage.useSourceFolderHierarchy() && fJarPackage.areJavaFilesExported() && !fJarPackage.areGeneratedFilesExported();
	}
}
