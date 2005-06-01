/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.internal.builder.BuilderUtils;
import org.eclipse.ajdt.internal.ui.dialogs.AJCUTypeInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.SourceRange;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationMessages;
import org.eclipse.jdt.internal.corext.codemanipulation.ImportReferencesCollector;
import org.eclipse.jdt.internal.corext.codemanipulation.ImportsStructure;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;
import org.eclipse.jdt.internal.corext.util.AllTypesCache;
import org.eclipse.jdt.internal.corext.util.IFileTypeInfo;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.internal.corext.util.TypeInfo;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jface.text.Region;

/**
 * @author Sian
 * Mostly copied from OrganizeImportsOperation - changes marked with // AspectJ Change
 */
public class AJOrganizeImportsOperation implements IWorkspaceRunnable {
	
	
	private static class TypeReferenceProcessor {
		
		private ArrayList fOldSingleImports;
		private ArrayList fOldDemandImports;
		
		private HashSet fImportsAdded;
		
		private ImportsStructure fImpStructure;
				
		private ArrayList fTypeRefsFound; // cached array list for reuse
		
		private boolean fDoIgnoreLowerCaseNames;
		
		private IJavaSearchScope fSearchScope;
		private IPackageFragment fCurrPackage;
		
		private ScopeAnalyzer fAnalyzer;
		
		public TypeReferenceProcessor(ArrayList oldSingleImports, ArrayList oldDemandImports, CompilationUnit root, ImportsStructure impStructure, boolean ignoreLowerCaseNames) {
			fOldSingleImports= oldSingleImports;
			fOldDemandImports= oldDemandImports;
			fImpStructure= impStructure;
			fDoIgnoreLowerCaseNames= ignoreLowerCaseNames;
			fAnalyzer= new ScopeAnalyzer(root);

			ICompilationUnit cu= fImpStructure.getCompilationUnit();
			fSearchScope= SearchEngine.createJavaSearchScope(new IJavaElement[] { cu.getJavaProject() });
			fCurrPackage= (IPackageFragment) cu.getParent();
					
			fTypeRefsFound= new ArrayList();  	// cached array list for reuse
			fImportsAdded= new HashSet();		
		}
		
		private boolean needsImport(ITypeBinding typeBinding, SimpleName ref) {
			if (!typeBinding.isTopLevel() && !typeBinding.isMember()) {
				return false; // no imports for anonymous, local, primitive types
			}
			int modifiers= typeBinding.getModifiers();
			if (Modifier.isPrivate(modifiers)) {
				return false; // imports for privates are not required
			}
			ITypeBinding currTypeBinding= Bindings.getBindingOfParentType(ref);
			if (currTypeBinding == null) {
				return false; // not in a type
			}
			if (!Modifier.isPublic(modifiers)) {
				if (!currTypeBinding.getPackage().getName().equals(typeBinding.getPackage().getName())) {
					return false; // not visible
				}
			}
			
			ASTNode parent= ref.getParent();
			if (parent instanceof Type) {
				parent= parent.getParent();
			}
			if (parent instanceof TypeDeclaration && parent.getParent() instanceof CompilationUnit) {
				return true;
			}
			
			if (typeBinding.isMember()) {
				IBinding[] visibleTypes= fAnalyzer.getDeclarationsInScope(ref, ScopeAnalyzer.TYPES);
				for (int i= 0; i < visibleTypes.length; i++) {
					if (visibleTypes[i] == typeBinding) {
						return false;
					}
				}
			}
			return true;				
		}
			
		
		/**
		 * Tries to find the given type name and add it to the import structure.
		 * Returns array of coices if user needs to select a type.
		 */
		public TypeInfo[] process(SimpleName ref, IProgressMonitor monitor) throws CoreException {
			String typeName= ref.getIdentifier();
			
			if (fImportsAdded.contains(typeName)) {
				return null;
			}
			
			try {
				IBinding binding= ref.resolveBinding();
				if (binding != null) {
					if (binding.getKind() == IBinding.TYPE) {
						ITypeBinding typeBinding= (ITypeBinding) binding;
						if (typeBinding.isArray()) {
							typeBinding= typeBinding.getElementType();
						}
						if (needsImport(typeBinding, ref)) {
							fImpStructure.addImport(typeBinding);
							fImportsAdded.add(typeName);
						}
					}	
					return null;
				} 
				
				fImportsAdded.add(typeName);
						
				ArrayList typeRefsFound= fTypeRefsFound; // reuse
				
				findTypeRefs(typeName, typeRefsFound, monitor);				
				int nFound= typeRefsFound.size();
				if (nFound == 0) {
					// nothing found
					return null;
				} else if (nFound == 1) {
					TypeInfo typeRef= (TypeInfo) typeRefsFound.get(0);
					fImpStructure.addImport(typeRef.getFullyQualifiedName());
					return null;
				} else {
					String containerToImport= null;
					boolean ambiguousImports= false;
									
					// multiple found, use old import structure to find an entry
					for (int i= 0; i < nFound; i++) {
						TypeInfo typeRef= (TypeInfo) typeRefsFound.get(i);
						String fullName= typeRef.getFullyQualifiedName();
						String containerName= typeRef.getTypeContainerName();
						if (fOldSingleImports.contains(fullName)) {
							// was single-imported
							fImpStructure.addImport(fullName);
							return null;
						} else if (fOldDemandImports.contains(containerName)) {
							if (containerToImport == null) {
								containerToImport= containerName;
							} else {  // more than one import-on-demand
								ambiguousImports= true;
							}
						}
					}
					
					if (containerToImport != null && !ambiguousImports) {
						fImpStructure.addImport(containerToImport, typeName);
					} else {
						// return the open choices
						return (TypeInfo[]) typeRefsFound.toArray(new TypeInfo[nFound]);
					}
				}
			} finally {
				fTypeRefsFound.clear();
			}
			return null;
		}
		
		private void findTypeRefs(String simpleTypeName, Collection typeRefsFound, IProgressMonitor monitor) throws JavaModelException {
			if (fDoIgnoreLowerCaseNames && simpleTypeName.length() > 0) {
				char ch= simpleTypeName.charAt(0);
				if (Strings.isLowerCase(ch) && Character.isLetter(ch)) {
					return;
				}
			}
			TypeInfo[] infos= AllTypesCache.getTypesForName(simpleTypeName, fSearchScope, monitor);
			for (int i= 0; i < infos.length; i++) {
				TypeInfo curr= infos[i];
				IType type= curr.resolveType(fSearchScope);
				if (type != null && JavaModelUtil.isVisible(type, fCurrPackage)) {
					typeRefsFound.add(curr);
				}
			}
			// AspectJ Change Begin
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {fImpStructure.getCompilationUnit().getJavaProject()}, 
					IJavaSearchScope.APPLICATION_LIBRARIES |
					IJavaSearchScope.REFERENCED_PROJECTS |
					IJavaSearchScope.SOURCES); 
//				JavaSearchScopeFactory.getInstance().createJavaProjectSearchScope(fImpStructure.getCompilationUnit().getJavaProject(), false);
			List ajTypes = getAspectJTypes(scope);
			for (Iterator iter = ajTypes.iterator(); iter.hasNext();) {
				AJCUTypeInfo curr = (AJCUTypeInfo) iter.next();
				if(curr.getTypeName().equals(simpleTypeName)) {
					IType type= curr.resolveType(fSearchScope);
					if (type != null && JavaModelUtil.isVisible(type, fCurrPackage)) {
						typeRefsFound.add(curr);
					}
				}
			}	
			// AspectJ Change End	
		}
	}	

	// AspectJ Change Begin
	/**
	 * Get a list of TypeInfos for the aspects found in the given scope
	 * @param scope
	 * @return
	 */
	private static List getAspectJTypes(IJavaSearchScope scope) {
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
										// Only get aspects because we have already added everything else to the AllTypesCache
										if(types[j] instanceof AspectElement) {
											char[][] enclosingTypes = BuilderUtils.getEnclosingTypes(types[j]);
											IFileTypeInfo info = new AJCUTypeInfo(
														types[j].getPackageFragment().getElementName(),
														types[j].getElementName(),
														enclosingTypes,
														types[j].isInterface(),
														types[j] instanceof AspectElement,
														jp.getElementName(),
														unit.getPackageFragmentRoot().getElementName(),
														unit.getElementName().substring(0, unit.getElementName().lastIndexOf('.')),
														"aj", //$NON-NLS-1$
														unit);						
											ajTypes.add(info);
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
//	 AspectJ Change End


	private Region fRange;
	private ImportsStructure fImportsStructure;	
	private boolean fDoSave;
	
	private boolean fIgnoreLowerCaseNames;
	
	private IChooseImportQuery fChooseImportQuery;
	
	private int fNumberOfImportsAdded;

	private IProblem fParsingError;
	private CompilationUnit fASTRoot;

	public AJOrganizeImportsOperation(ImportsStructure impStructure, Region range, boolean ignoreLowerCaseNames, boolean save, IChooseImportQuery chooseImportQuery) {
		super();
		fImportsStructure= impStructure;
		fRange= range;
		fDoSave= save;
		fIgnoreLowerCaseNames= ignoreLowerCaseNames;
		fChooseImportQuery= chooseImportQuery;

		fNumberOfImportsAdded= 0;
		
		fParsingError= null;
		ASTParser parser= ASTParser.newParser(AST.JLS2);
		parser.setSource(impStructure.getCompilationUnit());
		parser.setResolveBindings(true);
		fASTRoot= (CompilationUnit) parser.createAST(null);
	}
	
	public AJOrganizeImportsOperation(ICompilationUnit cu, String[] importOrder, int importThreshold, boolean ignoreLowerCaseNames, boolean save, boolean doResolve, IChooseImportQuery chooseImportQuery) throws CoreException {
		this(new ImportsStructure(cu, importOrder, importThreshold, false), null, ignoreLowerCaseNames, save, chooseImportQuery);
	}
	
	/**
	 * Runs the operation.
	 * @throws OperationCanceledException Runtime error thrown when operation is cancelled.
	 */	
	public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		try {

			if (monitor == null) {
				monitor= new NullProgressMonitor();
			}
			ICompilationUnit cu= fImportsStructure.getCompilationUnit();
			fNumberOfImportsAdded= 0;
			
			monitor.beginTask(CodeGenerationMessages.getFormattedString("OrganizeImportsOperation.description", cu.getElementName()), 4); //$NON-NLS-1$

			ArrayList oldSingleImports= new ArrayList();
			ArrayList oldDemandImports= new ArrayList();
			
			Collection references= findTypeReferences(oldSingleImports, oldDemandImports);
			if (references == null) {
				return;
			}
			
			int nOldImports= oldDemandImports.size() + oldSingleImports.size();
						
			oldDemandImports.add(""); //$NON-NLS-1$
			oldDemandImports.add("java.lang"); //$NON-NLS-1$
			oldDemandImports.add(cu.getParent().getElementName());			
			
			monitor.worked(1);
		
			TypeReferenceProcessor processor= new TypeReferenceProcessor(oldSingleImports, oldDemandImports, fASTRoot, fImportsStructure, fIgnoreLowerCaseNames);
			ArrayList openChoices= new ArrayList();
			ArrayList sourceRanges= new ArrayList();
			
			SubProgressMonitor subMonitor= new SubProgressMonitor(monitor, 2);
			try {
				subMonitor.beginTask("", references.size()); //$NON-NLS-1$
				
				Iterator refIterator= references.iterator();
				while (refIterator.hasNext()) {
					SimpleName typeRef= (SimpleName) refIterator.next();
					TypeInfo[] openChoice= processor.process(typeRef, new SubProgressMonitor(subMonitor, 1));
					if (openChoice != null) {
						openChoices.add(openChoice);
						sourceRanges.add(new SourceRange(typeRef.getStartPosition(), typeRef.getLength()));
					}	
				}
			} finally {
				subMonitor.done();
			}
			
			processor= null;
			
			if (openChoices.size() > 0 && fChooseImportQuery != null) {
				TypeInfo[][] choices= (TypeInfo[][]) openChoices.toArray(new TypeInfo[openChoices.size()][]);
				ISourceRange[] ranges= (ISourceRange[]) sourceRanges.toArray(new ISourceRange[sourceRanges.size()]);
				TypeInfo[] chosen= fChooseImportQuery.chooseImports(choices, ranges);
				if (chosen == null) {
					// cancel pressed by the user
					throw new OperationCanceledException();
				}
				for (int i= 0; i < chosen.length; i++) {
					TypeInfo typeInfo= chosen[i];
					fImportsStructure.addImport(typeInfo.getFullyQualifiedName());
				}				
			}
			fImportsStructure.create(fDoSave, new SubProgressMonitor(monitor, 1));
			
			fNumberOfImportsAdded= fImportsStructure.getNumberOfImportsCreated() - nOldImports;
		} finally {
			monitor.done();
		}
	}
	
	private boolean isAffected(IProblem problem) {
		return fRange == null || (fRange.getOffset() <= problem.getSourceEnd() && (fRange.getOffset() + fRange.getLength()) > problem.getSourceStart());
	}

	
	// find type references in a compilation unit
	private Collection findTypeReferences(ArrayList oldSingleImports, ArrayList oldDemandImports) {
		IProblem[] problems= fASTRoot.getProblems();
		for (int i= 0; i < problems.length; i++) {
			IProblem curr= problems[i];
			if (curr.isError() && (curr.getID() & IProblem.Syntax) != 0 && isAffected(curr)) {
				fParsingError= problems[i];
				return null;
			}
		}
		List imports= fASTRoot.imports();
		for (int i= 0; i < imports.size(); i++) {
			ImportDeclaration curr= (ImportDeclaration) imports.get(i);
			String id= ASTResolving.getFullName(curr.getName());
			if (curr.isOnDemand()) {
				oldDemandImports.add(id);
			} else {
				oldSingleImports.add(id);
			}
		}
		
		ArrayList result= new ArrayList();
		ImportReferencesCollector visitor = new ImportReferencesCollector(fRange, result);
		fASTRoot.accept(visitor);

		return result;
	}	
	
	/**
	 * After executing the operation, returns <code>null</code> if the operation has been executed successfully or
	 * the range where parsing failed. 
	 */
	public IProblem getParseError() {
		return fParsingError;
	}
	
	public int getNumberOfImportsAdded() {
		return fNumberOfImportsAdded;
	}

	/**
	 * @return Returns the scheduling rule for this operation
	 */
	public ISchedulingRule getScheduleRule() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	
}
