/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.launching;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.internal.debug.ui.launcher.MainMethodSearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;

/**
 * Search Engine to search for main methods and include Aspects in that search
 */
public class AJMainMethodSearchEngine extends MainMethodSearchEngine {

	/**
	 * Searches for all main methods in the given scope. Also searches the AJDE
	 * structure model for Aspects that have main methods.
	 */
	public Object[] searchMainMethodsIncludingAspects(IProgressMonitor pm,
			IJavaSearchScope scope, int style, boolean includeSubtypes)
			throws JavaModelException {
		pm.beginTask(
				LauncherMessages.getString("MainMethodSearchEngine.1"), 200); //$NON-NLS-1$
		IProgressMonitor javaSearchMonitor = new SubProgressMonitor(pm, 100);
		IType[] mainTypes = super.searchMainMethods(javaSearchMonitor, scope,
				style, includeSubtypes);
		IProject[] projects = AspectJPlugin.getWorkspace().getRoot()
				.getProjects();
		List mainList = new ArrayList(Arrays.asList(mainTypes));

		IProgressMonitor ajSearchMonitor = new SubProgressMonitor(pm, 100);
		ajSearchMonitor.beginTask(LauncherMessages
				.getString("MainMethodSearchEngine.1"), 100); //$NON-NLS-1$
		double ticksPerProject = Math.floor(100F / (float) projects.length);
		if (ticksPerProject < 1) {
			ticksPerProject = 1;
		}
		for (int i = 0; i < projects.length; i++) {
			try {
				if(projects[i].hasNature("org.eclipse.ajdt.ui.ajnature")) { 

					IJavaProject jp = JavaCore.create(projects[i]);
					if (jp != null) {
						if (scope.encloses(jp)) {
							Set aspects = getAllAspects(jp);
							mainList.addAll(aspects);
						} else {
							IPath[] enclosingPaths = scope.enclosingProjectsAndJars();
							for (int j = 0; j < enclosingPaths.length; j++) {
								IPath path = enclosingPaths[j];
								if (path.equals(jp.getPath())) {
									IJavaElement[] children = jp.getChildren();
									mainList
											.addAll(searchJavaElements(scope, children));
								}
							}
						}
					}
				}
			} catch (Exception e) {
			}
			ajSearchMonitor.internalWorked(ticksPerProject);
		}
		ajSearchMonitor.done();
		pm.done();
		return mainList.toArray();
	}


	/**
	 * Searches for all main methods in the given scope. Also searches the AJDE
	 * structure model for Aspects that have main methods.
	 */
	public Object[] searchMainMethodsIncludingAspects(IRunnableContext context,
			final IJavaSearchScope scope, final int style,
			final boolean includeSubtypes) throws InvocationTargetException,
			InterruptedException {

		int allFlags = IJavaElementSearchConstants.CONSIDER_EXTERNAL_JARS
				| IJavaElementSearchConstants.CONSIDER_BINARIES;
		Assert.isTrue((style | allFlags) == allFlags);

		final Object[][] res = new Object[1][];

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor pm)
					throws InvocationTargetException {
				try {
					res[0] = searchMainMethodsIncludingAspects(pm, scope,
							style, includeSubtypes);
				} catch (JavaModelException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
		context.run(true, true, runnable);

		return res[0];
	}

	/**
	 * Subsidiary method for 'searchMainMethods...' to search an array of
	 * IJavaElements for runnable aspects.
	 * 
	 * @param scope -
	 *            Java search scope
	 * @param children -
	 *            elements to search
	 * @throws JavaModelException
	 */
	private List searchJavaElements(IJavaSearchScope scope,
			IJavaElement[] children) throws JavaModelException {
		List aspectsFound = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof IPackageFragment) {
				aspectsFound.addAll(searchPackage(scope,
						(IPackageFragment) children[i]));
			} else if (children[i] instanceof IPackageFragmentRoot) {
				IJavaElement[] grandchildren = ((IPackageFragmentRoot) children[i])
						.getChildren();
				aspectsFound.addAll(searchJavaElements(scope, grandchildren));
			}
		}
		return aspectsFound;
	}

	/**
	 * Search a JDT IPackageFragment for runnable aspects if included in the
	 * scope.
	 * 
	 * @param scope -
	 *            search scope
	 * @param packageFragment -
	 *            the IPackageFragment
	 * @throws JavaModelException
	 */
	private List searchPackage(IJavaSearchScope scope,
			IPackageFragment packageFragment) throws JavaModelException {
		List aspectsFound = new ArrayList();
		if (scope.encloses(packageFragment)) {
			Set aspects = getAllAspects(packageFragment);
			aspectsFound.addAll(aspects);
		} else {
			aspectsFound.addAll(searchUnitsInPackage(scope, packageFragment));
		}
		return aspectsFound;
	}

	/**
	 * Search the individual compilation units of an IPackageFragment for
	 * runnable aspects if they are included in the search scope.
	 * 
	 * @param scope -
	 *            the search scope
	 * @param packageFragment -
	 *            the IPackageFragment
	 * @throws JavaModelException
	 */
	private List searchUnitsInPackage(IJavaSearchScope scope,
			IPackageFragment packageFragment) throws JavaModelException {
//		List aspectsFound = new ArrayList();
//		ICompilationUnit[] units = packageFragment.getCompilationUnits();
//		for (int i = 0; i < units.length; i++) {
//			if (scope.encloses(units[i])) {
//				Set aspects = getAllAspects(units[i], packageFragment);
//				aspectsFound.addAll(aspects);
//			}
//		}
//		aspectsFound.addAll(getAllAspects(packageFragment));
//		return aspectsFound;
		return new ArrayList(getAllAspects(packageFragment));
	}

//	/**
//	 * Get any aspect IProgramElements for a CompilationUnit
//	 * 
//	 * @param unit
//	 * @return Set of Object[] containing IProgramElements in location 0 and
//	 *         IProjects in location 1
//	 */
//	private Set getAllAspects(ICompilationUnit unit,
//			IPackageFragment parentPackageFragment) {
//		String unitName = unit.getElementName().substring(0,
//				unit.getElementName().indexOf(".")); //$NON-NLS-1$
//		Set aspects = getAllAspects(parentPackageFragment);
//		for (Iterator iter = aspects.iterator(); iter.hasNext();) {
//			Object[] element = (Object[]) iter.next();
//			IProgramElement aspectElement = (IProgramElement) element[0];
//			if (!(aspectElement.getName().equals(unitName))) {
//				iter.remove();
//			}
//		}
//		return aspects;
//	}

	/**
	 * Iterates through all the packages in a project and returns a Set
	 * containing all the Aspects in a project that are currently active.
	 * 
	 * @param JP
	 *            the project
	 * @return Set of Object[] where element 0 in the array is an
	 *         IProgramElement representing the Aspect and element 1 is an
	 *         IProject, being the project that contains the aspect.
	 */
	private Set getAllAspects(IJavaProject jp) {
//		IProject project = jp.getProject();
//		String configFile = AspectJPlugin.getBuildConfigurationFile(project);
//		if (!(configFile.equals(Ajde.getDefault().getConfigurationManager()
//				.getActiveConfigFile()))) {
//			Ajde.getDefault().getConfigurationManager().setActiveConfigFile(
//					configFile);
//		}
//
//		List structureModelPackages = StructureModelUtil.getPackagesInModel();
//		Set aspects = new HashSet();
//
//		Iterator it = structureModelPackages.iterator();
//		while (it.hasNext()) {
//			Object[] progNodes = (Object[]) it.next();
//
//			IProgramElement packageNode = (IProgramElement) progNodes[0];//it.next();
//
//			Set temp = getAspectsInPackage(packageNode, project);
//
//			aspects.addAll(temp);
//		}
//		return aspects;
		try {
			return new HashSet(getActiveMainTypesFromAJCompilationUnits(AJCompilationUnitManager.INSTANCE.getAJCompilationUnits(jp)));
		} catch (CoreException e) {
			return new HashSet();
		}
	}

	/**
	 * Returns a Set containing all the Aspects in a package that are currently
	 * active.
	 * 
	 * @param packageElement
	 * @return Set of Object[] where element 0 in the array is an
	 *         IProgramElement representing the Aspect and element 1 is an
	 *         IProject, being the project that contains the aspect.
	 */
	private Set getAllAspects(IPackageFragment packageElement) {
//		IProject project = packageElement.getJavaProject().getProject();
//		String configFile = AspectJPlugin.getBuildConfigurationFile(project);
//		if (!(configFile.equals(Ajde.getDefault().getConfigurationManager()
//				.getActiveConfigFile()))) {
//			Ajde.getDefault().getConfigurationManager().setActiveConfigFile(
//					configFile);
//		}
//
//		List packages = StructureModelUtil.getPackagesInModel();
//		Set aspects = new HashSet();
//
//		Iterator it = packages.iterator();
//		while (it.hasNext()) {
//			Object[] progNodes = (Object[]) it.next();
//
//			IProgramElement packageNode = (IProgramElement) progNodes[0];
//			if (packageNode.getName().equals(packageElement.getElementName())) {
//
//				Set temp = getAspectsInPackage(packageNode, project);
//				aspects.addAll(temp);
//				break;
//			}
//		}
//		return aspects;
		List aspects = new ArrayList();
		try {
			aspects = AJCompilationUnitManager.INSTANCE.getAJCompilationUnitsForPackage(packageElement);
		} catch (Exception e) { 
		}				
		
		return new HashSet(getActiveMainTypesFromAJCompilationUnits(aspects));
		
	}

	private List getActiveMainTypesFromAJCompilationUnits(List aspects) {
		List mainTypes = new ArrayList();
		try {
			for (Iterator iter = aspects.iterator(); iter.hasNext();) {
				AJCompilationUnit element = (AJCompilationUnit) iter.next();
				if (BuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(element.getJavaProject()).getActiveBuildConfiguration().isIncluded(element.getCorrespondingResource())) {
					IType[] types = element.getAllTypes();
					for (int i = 0; i < types.length; i++) {
						IType type = types[i];
						IMethod[] methods = type.getMethods();
						for (int j = 0; j < methods.length; j++) {
							if(methods[j].isMainMethod()) {
								mainTypes.add(type);
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return mainTypes;
	}

//	/**
//	 * Get all the Aspects in a given package node of the structure model
//	 * 
//	 * @param packageNode -
//	 *            structure model package
//	 * @param project -
//	 *            associated IProject
//	 * @return Set of Object[] where element 0 in the array is an
//	 *         IProgramElement representing the Aspect and element 1 is an
//	 *         IProject, being the project that contains the aspect.
//	 */
//	private Set getAspectsInPackage(IProgramElement packageNode,
//			IProject project) {
//		List files = StructureModelUtil.getFilesInPackage(packageNode);
//		Set aspects = new HashSet();
//		for (Iterator it = files.iterator(); it.hasNext();) {
//			IProgramElement fileNode = (IProgramElement) it.next();
//			if (fileNode.getKind().equals(IProgramElement.Kind.FILE_JAVA)
//					|| fileNode.getKind().equals(
//							IProgramElement.Kind.FILE_ASPECTJ)) {
//				List children = fileNode.getChildren();
//				for (Iterator iter = children.iterator(); iter.hasNext();) {
//					IProgramElement child = (IProgramElement) iter.next();
//					if (child != null
//							&& child.getKind().equals(
//									IProgramElement.Kind.ASPECT)) {
//						if (child.isRunnable()) {
//							aspects.add(new Object[] { child, project });
//						}
//					}
//				}
//			}
//		}
//		return aspects;
//	}
}