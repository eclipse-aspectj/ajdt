/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.internal.builder.BuilderUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.env.IGenericType;
import org.eclipse.jdt.internal.corext.util.IFileTypeInfo;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.corext.util.TypeInfo;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.util.StringMatcher;
import org.eclipse.jdt.internal.ui.util.TypeInfoLabelProvider;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredList;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

/**
 * Copied from org.eclipse.jdt.internal.ui.dialogs
 * Changes marked with // AspectJ Change
 */
public class TypeSelectionDialog extends TwoPaneElementSelector {

	private static class TypeFilterMatcher implements FilteredList.FilterMatcher {

		private static final char END_SYMBOL= '<';
		private static final char ANY_STRING= '*';

		private StringMatcher fMatcher;
		private StringMatcher fQualifierMatcher;
		
		/*
		 * @see FilteredList.FilterMatcher#setFilter(String, boolean)
		 */
		public void setFilter(String pattern, boolean ignoreCase, boolean igoreWildCards) {
			int qualifierIndex= pattern.lastIndexOf("."); //$NON-NLS-1$

			// type			
			if (qualifierIndex == -1) {
				fQualifierMatcher= null;
				fMatcher= new StringMatcher(adjustPattern(pattern), ignoreCase, igoreWildCards);
				
			// qualified type
			} else {
				fQualifierMatcher= new StringMatcher(pattern.substring(0, qualifierIndex), ignoreCase, igoreWildCards);
				fMatcher= new StringMatcher(adjustPattern(pattern.substring(qualifierIndex + 1)), ignoreCase, igoreWildCards);
			}
		}

		/*
		 * @see FilteredList.FilterMatcher#match(Object)
		 */
		public boolean match(Object element) {
			if (!(element instanceof TypeInfo))
				return false;

			TypeInfo type= (TypeInfo) element;

			if (!fMatcher.match(type.getTypeName()))
				return false;

			if (fQualifierMatcher == null)
				return true;

			return fQualifierMatcher.match(type.getTypeContainerName());
		}
		
		private String adjustPattern(String pattern) {
			int length= pattern.length();
			if (length > 0) {
				switch (pattern.charAt(length - 1)) {
					case END_SYMBOL:
						pattern= pattern.substring(0, length - 1);
						break;
					case ANY_STRING:
						break;
					default:
						pattern= pattern + ANY_STRING;
				}
			}
			return pattern;
		}
	}
	
	/*
	 * A string comparator which is aware of obfuscated code
	 * (type names starting with lower case characters).
	 */
	private static class StringComparator implements Comparator {
	    public int compare(Object left, Object right) {
	     	String leftString= (String) left;
	     	String rightString= (String) right;
	     		     	
	     	if (Strings.isLowerCase(leftString.charAt(0)) &&
	     		!Strings.isLowerCase(rightString.charAt(0)))
	     		return +1;

	     	if (Strings.isLowerCase(rightString.charAt(0)) &&
	     		!Strings.isLowerCase(leftString.charAt(0)))
	     		return -1;
	     	
			int result= leftString.compareToIgnoreCase(rightString);			
			if (result == 0)
				result= leftString.compareTo(rightString);

			return result;
	    }
	}

	private IRunnableContext fRunnableContext;
	private IJavaSearchScope fScope;
	private int fElementKinds;
	
	/**
	 * Constructs a type selection dialog.
	 * @param parent  the parent shell.
	 * @param context the runnable context.
	 * @param elementKinds <code>IJavaSearchConstants.CLASS</code>, <code>IJavaSearchConstants.INTERFACE</code>
	 * or <code>IJavaSearchConstants.TYPE</code>
	 * @param scope   the java search scope.
	 */
	public TypeSelectionDialog(Shell parent, IRunnableContext context, int elementKinds, IJavaSearchScope scope) {
		// AspectJ Change Begin - use an AJTypeInfoLabelProvider
		super(parent, new AJTypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_TYPE_ONLY),
			new AJTypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_TYPE_CONTAINER_ONLY + TypeInfoLabelProvider.SHOW_ROOT_POSTFIX));
		// AspectJ Change End
		Assert.isNotNull(context);
		Assert.isNotNull(scope);

		fRunnableContext= context;
		fScope= scope;
		fElementKinds= elementKinds;
		
		setUpperListLabel(JavaUIMessages.TypeSelectionDialog_upperLabel);
		setLowerListLabel(JavaUIMessages.TypeSelectionDialog_lowerLabel);
	}

	/*
	 * @see AbstractElementListSelectionDialog#createFilteredList(Composite)
	 */
 	protected FilteredList createFilteredList(Composite parent) {
 		FilteredList list= super.createFilteredList(parent);
 		
		fFilteredList.setFilterMatcher(new TypeFilterMatcher());
		fFilteredList.setComparator(new StringComparator());
		
		return list;
	}
	
	/*
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		final ArrayList typeList= new ArrayList();
		try {
			if (isCacheUpToDate()) {
				// run without progress monitor
				// TODO: 3.1M7
				//AllTypesCache.getTypes(fScope, fElementKinds, null, typeList);
			} else {
				IRunnableWithProgress runnable= new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						// TODO: 3.1M7
						// AllTypesCache.getTypes(fScope, fElementKinds, monitor, typeList);
						if (monitor.isCanceled()) {
							throw new InterruptedException();
						}
					}
				};
				fRunnableContext.run(true, true, runnable);
			}
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, JavaUIMessages.TypeSelectionDialog_error3Title, JavaUIMessages.TypeSelectionDialog_error3Message);
			return CANCEL;
		} catch (InterruptedException e) {
			// cancelled by user
			return CANCEL;
		}
		// AspectJ Change Begin - add AspectJ Types
		typeList.addAll(getAspectJTypes(fScope));	
		// AspectJ Change End
		if (typeList.isEmpty()) {
			String title= JavaUIMessages.TypeSelectionDialog_notypes_title;
			String message= JavaUIMessages.TypeSelectionDialog_notypes_message;
			MessageDialog.openInformation(getShell(), title, message);
			return CANCEL;
		}
			
		TypeInfo[] typeRefs= (TypeInfo[])typeList.toArray(new TypeInfo[typeList.size()]);
		setElements(typeRefs);

		return super.open();
	}
	
	// AspectJ Change Begin
	/**
	 * @param scope
	 * @return
	 */
	private List getAspectJTypes(IJavaSearchScope scope) {
		List ajTypes = new ArrayList();
		IProject[] projects = AspectJPlugin.getWorkspace().getRoot()
				.getProjects();
		for (int i = 0; i < projects.length; i++) {
			try {
				if(projects[i].hasNature("org.eclipse.ajdt.ui.ajnature")) { //$NON-NLS-1$ 		
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
										char[][] enclosingTypes = BuilderUtils.getEnclosingTypes(type);
										int kind = 0;
										if (type.isClass()) {
											kind = IGenericType.CLASS_DECL;
										} else if (type.isInterface()) {
											kind = IGenericType.INTERFACE_DECL;
										} else if (type.isEnum()) {
											kind = IGenericType.ENUM_DECL;
										} else /*if (type.isAnnotation())*/ {
											kind = IGenericType.ANNOTATION_TYPE_DECL;
										}
										// TODO: 3.1M7
										/* IFileTypeInfo info = new AJCUTypeInfo(
												type.getPackageFragment().getElementName(),
												type.getElementName(),
												enclosingTypes,
												kind,
												type instanceof AspectElement,
												jp.getElementName(),
												unit.getPackageFragmentRoot().getElementName(),
												unit.getElementName().substring(0, unit.getElementName().lastIndexOf('.')),
												"aj", //$NON-NLS-1$
												unit);							
										ajTypes.add(info); */									
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
//	 AspectJ Change End

	/*
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	protected void computeResult() {
		TypeInfo ref= (TypeInfo) getLowerSelectedElement();

		if (ref == null)
			return;

		try {
			IType type= ref.resolveType(fScope);			
			if (type == null) {
				// not a class file or compilation unit
				String title= JavaUIMessages.TypeSelectionDialog_errorTitle;
				String message= Messages.format(JavaUIMessages.TypeSelectionDialog_dialogMessage, ref.getPath());
				MessageDialog.openError(getShell(), title, message);
				setResult(null);
			} else {
				List result= new ArrayList(1);
				result.add(type);
				setResult(result);
			}

		} catch (JavaModelException e) {
			String title= JavaUIMessages.TypeSelectionDialog_errorTitle;
			String message= JavaUIMessages.TypeSelectionDialog_errorMessage;
			ErrorDialog.openError(getShell(), title, message, e.getStatus());
			setResult(null);
		}
	}
	
	private boolean isCacheUpToDate() throws InvocationTargetException, InterruptedException {
		final boolean result[]= new boolean[1];
		IRunnableWithProgress runnable= new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					// TODO: 3.1M7
					result[0]= true; //AllTypesCache.isCacheUpToDate(monitor);
				} catch (OperationCanceledException e) {
					throw new InterruptedException(e.getMessage());
				}
			}
		};
		PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
		return result[0];
	}
}
