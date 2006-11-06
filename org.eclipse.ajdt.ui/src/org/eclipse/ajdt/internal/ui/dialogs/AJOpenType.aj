/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.internal.corext.util.History;
import org.eclipse.ajdt.internal.ui.dialogs.TypeInfoViewer.TypeInfoLabelProvider;
import org.eclipse.ajdt.internal.ui.resources.AJDTIcon;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationPattern;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.dialogs.SearchPattern;

/**
 * Advises code in jdt-src to implement Open AspectJ Type functionality
 */
public privileged aspect AJOpenType {

	private static ImageDescriptor ASPECT_ICON = ((AJDTIcon)AspectJImages.instance().getIcon(IProgramElement.Kind.ASPECT)).getImageDescriptor();

	IPath around() : call(* getStateLocation()) && within(History) {
		return AspectJUIPlugin.getDefault().getStateLocation();
	}
	
	ImageDescriptor around(Object type) : execution(* TypeInfoLabelProvider.getImageDescriptor(..)) && args(type) {
		if(type instanceof AJCUTypeNameMatch) {
			if (((AJCUTypeNameMatch)type).getType() instanceof AspectElement) {
				return ASPECT_ICON;					
			} 
		}
		return proceed(type);
	}
	
	TypeNameMatch[] around() : call(* getResult()) && within(TypeInfoViewer.SearchEngineJob) {
		TypeNameMatch[] result = proceed();
		TypeInfoViewer.SearchEngineJob searchEngineJob = (TypeInfoViewer.SearchEngineJob)thisJoinPoint.getThis();
		String packPattern= searchEngineJob.fFilter.getPackagePattern();
		List types = getAspectJTypes(searchEngineJob.fScope, packPattern == null ? null : packPattern.toCharArray(), 
				searchEngineJob.fFilter.getNamePattern().toCharArray()); 
		TypeNameMatch[] typesIncludingAspects = new TypeNameMatch[result.length + types.size()];			
		System.arraycopy(result, 0, typesIncludingAspects, 0, result.length);
		int index = result.length;
		for (Iterator iter = types.iterator(); iter.hasNext();) {
			TypeNameMatch info = (TypeNameMatch) iter.next();
			typesIncludingAspects[index] = info;
			index ++;
		}
		return typesIncludingAspects;
	}
	
	/**
	 * @param scope
	 * @param string 
	 * @return
	 */
	private static List getAspectJTypes(IJavaSearchScope scope, char[] packagePattern, char[] namePattern) {
		List ajTypes = new ArrayList();
		IProject[] projects = AspectJPlugin.getWorkspace().getRoot()
				.getProjects();
		TypeDeclarationPattern pattern = new TypeDeclarationPattern(packagePattern, null, namePattern, IIndexConstants.TYPE_SUFFIX, SearchPattern.RULE_PREFIX_MATCH);
		for (int i = 0; i < projects.length; i++) {
			try {
				if(AspectJPlugin.isAJProject(projects[i])) { 		
					IJavaProject jp = JavaCore.create(projects[i]);
					if (jp != null) {
						IPath[] paths = scope.enclosingProjectsAndJars();
						for (int a = 0; a < paths.length; a++) {	
							if (paths[a].equals(jp.getPath())) { 
								List ajCus = AJCompilationUnitManager.INSTANCE.getAJCompilationUnits(jp);
								for (Iterator iter = ajCus.iterator(); iter
										.hasNext();) {
									AJCompilationUnit unit = (AJCompilationUnit) iter.next();
									IType[] types = unit.getAllTypes();
									for (int j = 0; j < types.length; j++) {
										IType type = types[j];
										if(pattern.matchesName(namePattern, type.getElementName().toCharArray())) {
											int kind = type.getFlags(); // 103131 - pass in correct flags
											if (type instanceof AspectElement) { // 3.2 - Classes in .aj files are found
												AJCUTypeNameMatch info = new AJCUTypeNameMatch(type,kind);
												ajTypes.add(info);
											}
										}
									}
								}
							} 
						}
					}
				}	
			} catch (JavaModelException e) {
			} catch (CoreException e) {					
			}
		}
		return ajTypes;
	}

}
