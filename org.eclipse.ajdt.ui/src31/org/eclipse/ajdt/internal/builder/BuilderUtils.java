/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 ******************************************************************************/
package org.eclipse.ajdt.internal.builder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.internal.core.AJLog;
import org.eclipse.ajdt.internal.core.TimerLogEvent;
import org.eclipse.ajdt.internal.ui.dialogs.AJCUTypeInfo;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.internal.jobs.JobStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.IGenericType;
import org.eclipse.jdt.internal.corext.util.IFileTypeInfo;
import org.eclipse.jdt.internal.corext.util.TypeInfo;
import org.eclipse.swt.widgets.Display;

/**
 * Builder Utilities
 */
public class BuilderUtils {

	/**
	 * Starts a job that updates JDT's all types cache with the 
	 * types (but not aspects) contained in .aj files in the given 
	 * java project (jp).
	 * @param jp
	 */
	public static void updateTypesCache(final IJavaProject jp) {
		/*
		Job updateJob = new Job(AspectJUIPlugin.getResourceString("AllTypesUpdateJob")) { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				AJLog.logStart(TimerLogEvent.UPDATE_TYPES_CACHE_FOR_PROJECT + jp.getElementName());
				try {
					List types = getTypeInfosForProject(jp);
					TypeInfo[] type = AllTypesCache.getAllTypes(new NullProgressMonitor());
					List typeList = new ArrayList(Arrays.asList(type));
					for (Iterator iter = typeList.iterator(); iter.hasNext();) {
						TypeInfo info = (TypeInfo) iter.next();
						if(info instanceof AJCUTypeInfo) {
							if(((AJCUTypeInfo)info).getProject().equals(jp.getElementName())) {
								iter.remove();
							}
						}				
					}
					TypeInfo[] typesIncludingAspects = new TypeInfo[typeList.size() + types.size()];			
					System.arraycopy(typeList.toArray(), 0, typesIncludingAspects, 0, typeList.size());
					int index = typeList.size();
					for (Iterator iter = types.iterator(); iter.hasNext();) {
						TypeInfo info = (TypeInfo) iter.next();
						typesIncludingAspects[index] = info;
						index ++;
					}
					Arrays.sort(typesIncludingAspects, new Comparator() {
						public int compare(Object o1, Object o2) {
							return ((TypeInfo)o1).getTypeName().compareTo(((TypeInfo)o2).getTypeName());
						}
					});
					Method setTypes = AllTypesCache.class.getDeclaredMethod("setCache", new Class[] {TypeInfo[].class});
					setTypes.setAccessible(true);
					setTypes.invoke(null, new Object[] {typesIncludingAspects});
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				} catch (CoreException e) {			
				} catch (InvocationTargetException e) {			
				} catch (IllegalAccessException e) {			
				}
				AJLog.logEnd(TimerLogEvent.UPDATE_TYPES_CACHE_FOR_PROJECT + jp.getElementName());
				return new JobStatus(IStatus.OK, this, AspectJUIPlugin.getResourceString("UpdatedTypesCache")); //$NON-NLS-1$
			}
		};
//		updateJob.setSystem(true);
		updateJob.schedule();		
		*/
	}
	
	
	/**
	 * Starts a job that updates JDT's all types cache with the 
	 * types (but not aspects) contained in .aj files in all of the 
	 * java projects in the workspace.
	 * @param workspace - the current workspace
	 */
	public static void updateTypesCache(IWorkspace workspace) {
		/*
		IProject[] projectArray = workspace.getRoot().getProjects();
		final List projects = new ArrayList();
		for (int i = 0; i < projectArray.length; i++) {
			IJavaProject jp = (IJavaProject)JavaCore.create(projectArray[i]);
			if(jp != null) {
				projects.add(jp);
			}
		}
		Job updateJob = new Job(AspectJUIPlugin.getResourceString("AllTypesUpdateJob")) { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				AJLog.logStart(TimerLogEvent.UPDATE_TYPES_CACHE_FOR_WORKSPACE);
				List allTypes = new ArrayList();
				try {
					TypeInfo[] type = AllTypesCache.getAllTypes(monitor);
					List typeList = new ArrayList(Arrays.asList(type));
					for (Iterator iterator = projects.iterator(); iterator.hasNext();) {
					IJavaProject jp = (IJavaProject) iterator.next();
						List types = getTypeInfosForProject(jp);
						allTypes.addAll(types);
						for (Iterator iter = typeList.iterator(); iter.hasNext();) {
							TypeInfo info = (TypeInfo) iter.next();
							if(info instanceof AJCUTypeInfo) {
								if(((AJCUTypeInfo)info).getProject().equals(jp.getElementName())) {
									iter.remove();
								}
							}				
						}
					}
				
					TypeInfo[] typesIncludingAspects = new TypeInfo[typeList.size() + allTypes.size()];			
					System.arraycopy(typeList.toArray(), 0, typesIncludingAspects, 0, typeList.size());
					int index = typeList.size();
					for (Iterator iter = allTypes.iterator(); iter.hasNext();) {
						TypeInfo info = (TypeInfo) iter.next();
						typesIncludingAspects[index] = info;
						index ++;
					}
					Arrays.sort(typesIncludingAspects, new Comparator() {
						public int compare(Object o1, Object o2) {
							return ((TypeInfo)o1).getTypeName().compareTo(((TypeInfo)o2).getTypeName());
						}
					});
					Method setTypes = AllTypesCache.class.getDeclaredMethod("setCache", new Class[] {TypeInfo[].class});
					setTypes.setAccessible(true);
					setTypes.invoke(null, new Object[] {typesIncludingAspects});
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				} catch (CoreException e) {			
				} catch (InvocationTargetException e) {			
				} catch (IllegalAccessException e) {			
				}
				AJLog.logEnd(TimerLogEvent.UPDATE_TYPES_CACHE_FOR_WORKSPACE);
				return new JobStatus(IStatus.OK, this, AspectJUIPlugin.getResourceString("UpdatedTypesCache")); //$NON-NLS-1$
			}			
			
		};
		if(projects.size() > 0) {
			IJavaProject jp = (IJavaProject)projects.get(0);
//			updateJob.setSystem(true);
			updateJob.schedule();
		}
		*/
	}
	
	/**
	 * @param types
	 * @param j
	 * @return
	 */
	public static char[][] getEnclosingTypes(IType startType) {
		char[][] enclosingTypes = null;
		IType type = startType.getDeclaringType();
		List enclosingTypeList = new ArrayList();
		while(type != null) {
			char[] typeName = type.getElementName().toCharArray();
			enclosingTypeList.add(0, typeName);
			type = type.getDeclaringType();
		}
		if(enclosingTypeList.size() > 0) {
			enclosingTypes = new char[enclosingTypeList.size()][];
			for (int k = 0; k < enclosingTypeList.size(); k++) {
				char[] typeName = (char[]) enclosingTypeList.get(k);
				enclosingTypes[k] = typeName;
			}
		}
		return enclosingTypes;
	}
	
	/**
	 * Get a list of ITypeInfos for a project for all types in .aj files
	 * except aspects
	 */
	private static List getTypeInfosForProject(final IJavaProject jp) throws CoreException, JavaModelException {
		List types = new ArrayList();
		List cus = AJCompilationUnitManager.INSTANCE.getAJCompilationUnits(jp);
		for (Iterator iter = cus.iterator(); iter.hasNext();) {
			AJCompilationUnit unit = (AJCompilationUnit) iter.next();
			IType[] itypes = unit.getAllTypes();
			for (int i = 0; i < itypes.length; i++) {
				// Don't add aspects...
				if(!(itypes[i] instanceof AspectElement)) {
					char[][] enclosingTypes = getEnclosingTypes(itypes[i]);
					int kind = 0;
					if (itypes[i].isClass()) {
						kind = IGenericType.CLASS_DECL;
					} else if (itypes[i].isInterface()) {
						kind = IGenericType.INTERFACE_DECL;
					} else if (itypes[i].isEnum()) {
						kind = IGenericType.ENUM_DECL;
					} else /*if (type.isAnnotation())*/ {
						kind = IGenericType.ANNOTATION_TYPE_DECL;
					}
					/*
					IFileTypeInfo info = new AJCUTypeInfo(
							itypes[i].getPackageFragment().getElementName(),
							itypes[i].getElementName(),
							enclosingTypes,
							kind,
							itypes[i] instanceof AspectElement,
							jp.getElementName(),
							unit.getPackageFragmentRoot().getElementName(),
							unit.getElementName().substring(0, unit.getElementName().lastIndexOf('.')),
							"aj",
							unit);						
					types.add(info);
					*/
				}
			}
		}
		return types;
	}


	/**
	 * Initialise the types cache
	 */
	public static void initTypesCache() {
		/*
		Job job = new Job("") {
			public IStatus run(IProgressMonitor monitor) {
				Display d = Display.getDefault();
				d.syncExec(new Runnable() {
					public void run() {
						try {
							Method startBackgroundMode = AllTypesCache.class.getDeclaredMethod("startBackgroundMode", new Class[0]);
							startBackgroundMode.setAccessible(true);
							startBackgroundMode.invoke(null, new Object[0]);
						} catch (SecurityException e) {
						} catch (NoSuchMethodException e) {
						} catch (IllegalArgumentException e) {
						} catch (IllegalAccessException e) {
						} catch (InvocationTargetException e) {
						}						
					}
				}); 
				updateTypesCache(AspectJPlugin.getWorkspace());
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		*/
	}
}
