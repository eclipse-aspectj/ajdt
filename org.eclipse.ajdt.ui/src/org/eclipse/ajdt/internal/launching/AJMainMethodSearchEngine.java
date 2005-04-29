/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
	 * Searches for all main methods in the given scope. Also searches 
	 * for Aspects that have main methods.
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
				if(projects[i].hasNature("org.eclipse.ajdt.ui.ajnature")) { //$NON-NLS-1$ 

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
	 * Searches for all main methods in the given scope. Also searches 
     * for Aspects that have main methods.
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
		List units = new ArrayList();
		Set allAspects = getAllAspects(packageFragment);
		for (Iterator iter = allAspects.iterator(); iter.hasNext();) {
			IType type = (IType) iter.next();
			if(scope.encloses(type)) {
				units.add(type);
			}
		}
		return units;
	}

	/**
	 * Iterates through all the packages in a project and returns a Set
	 * containing all the Aspects in a project that are currently active.
	 * 
	 * @param JP
	 *            the project
	 * @return Set of AJCompilationUnits
	 */
	private Set getAllAspects(IJavaProject jp) {
		try {
			return new HashSet(getActiveMainTypesFromAJCompilationUnits(AJCompilationUnitManager.INSTANCE.getAJCompilationUnits(jp)));
		} catch (CoreException e) {
			return new HashSet();
		}
	}

	/**
	 * Returns a Set containing all the Aspects in a package that are currently
	 * active and contain main types.
	 * 
	 * @param packageElement
	 * @return Set of AJCompilationUnits
	 */
	private Set getAllAspects(IPackageFragment packageElement) {
		List aspects = new ArrayList();
		try {
			aspects = AJCompilationUnitManager.INSTANCE.getAJCompilationUnitsForPackage(packageElement);
		} catch (Exception e) { 
		}				
		
		return new HashSet(getActiveMainTypesFromAJCompilationUnits(aspects));
		
	}

	/**
	 * Get a List of the active (included in current build configuration) main types (IType)
	 * from the List of AJCompilationUnits passed in
	 * @param aspects
	 * @return
	 */
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

}