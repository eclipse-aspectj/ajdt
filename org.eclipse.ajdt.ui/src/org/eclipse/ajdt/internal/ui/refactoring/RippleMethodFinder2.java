// Copied from org.eclipse.jdt.internal.corext.refactoring.rename.RippleMethodFinder2
// adapted for AJDT.  If target is an ITD, then don't use getDeclaringType, but rather
// use the target type
// Changes marked with // AspectJ change
/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.refactoring.RefactoringScopeFactory;
import org.eclipse.jdt.internal.corext.refactoring.base.ReferencesInBinaryContext;
import org.eclipse.jdt.internal.corext.refactoring.rename.MethodChecks;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.SearchUtils;

public class RippleMethodFinder2 {

	private final IMethod fMethod;
	private List<IMethod> fDeclarations;
	private ITypeHierarchy fHierarchy;
	private Map<IType, IMethod> fTypeToMethod;
	private Set<IType> fRootTypes;
	private MultiMap/*<IType, IType>*/ fRootReps;
	private Map<IType, ITypeHierarchy> fRootHierarchies;
	private UnionFind fUnionFind;

	private final boolean fExcludeBinaries;
	private final ReferencesInBinaryContext fBinaryRefs;
	private Map<IMethod, SearchMatch> fDeclarationToMatch;

	private static class MultiMap {
		HashMap<IType, Collection<IType>> fImplementation= new HashMap<>();

		public void put(IType key, IType value) {
			Collection<IType> collection = fImplementation.computeIfAbsent(key, k -> new HashSet<>());
			collection.add(value);
		}

		public Collection<IType> get(IType key) {
			return fImplementation.get(key);
		}
	}

	private static class UnionFind {
		HashMap<IType, IType> fElementToRepresentative= new HashMap<>();

		public void init(IType type) {
			fElementToRepresentative.put(type, type);
		}

		//path compression:
		public IType find(IType element) {
			IType root= element;
			IType rep= fElementToRepresentative.get(root);
			while (rep != null && ! rep.equals(root)) {
				root= rep;
				rep= fElementToRepresentative.get(root);
			}
			if (rep == null)
				return null;

			rep= fElementToRepresentative.get(element);
			while (! rep.equals(root)) {
				IType temp= element;
				element= rep;
				fElementToRepresentative.put(temp, root);
				rep= fElementToRepresentative.get(element);
			}
			return root;
		}

//		//straightforward:
//		public IType find(IType element) {
//			IType current= element;
//			IType rep= fElementToRepresentative.get(current);
//			while (rep != null && ! rep.equals(current)) {
//				current= rep;
//				rep= fElementToRepresentative.get(current);
//			}
//			if (rep == null)
//				return null;
//			else
//				return current;
//		}

		public void union(IType rep1, IType rep2) {
			fElementToRepresentative.put(rep1, rep2);
		}
	}


	private RippleMethodFinder2(IMethod method, boolean excludeBinaries){
		fMethod= method;
		fExcludeBinaries= excludeBinaries;
		fBinaryRefs= null;
	}

	private RippleMethodFinder2(IMethod method, ReferencesInBinaryContext binaryRefs) {
		fMethod= method;
		fExcludeBinaries= true;
		fDeclarationToMatch= new HashMap<>();
		fBinaryRefs= binaryRefs;
	}

	public static IMethod[] getRelatedMethods(IMethod method, boolean excludeBinaries, IProgressMonitor pm, WorkingCopyOwner owner) throws CoreException {
		try{
			if (! MethodChecks.isVirtual(method))
				return new IMethod[]{ method };

			return new RippleMethodFinder2(method, excludeBinaries).getAllRippleMethods(pm, owner);
		} finally{
			pm.done();
		}
	}

	public static IMethod[] getRelatedMethods(IMethod method, IProgressMonitor pm, WorkingCopyOwner owner) throws CoreException {
		return getRelatedMethods(method, true, pm, owner);
	}

	public static IMethod[] getRelatedMethods(IMethod method, ReferencesInBinaryContext binaryRefs, IProgressMonitor pm, WorkingCopyOwner owner) throws CoreException {
		try {
			if (! MethodChecks.isVirtual(method))
				return new IMethod[]{ method };

			return new RippleMethodFinder2(method, binaryRefs).getAllRippleMethods(pm, owner);
		} finally{
			pm.done();
		}
	}

	private IMethod[] getAllRippleMethods(IProgressMonitor pm, WorkingCopyOwner owner) throws CoreException {
		IMethod[] rippleMethods= findAllRippleMethods(pm, owner);
		if (fDeclarationToMatch == null)
			return rippleMethods;

		List<IMethod> rippleMethodsList= new ArrayList<>(Arrays.asList(rippleMethods));
		for (Iterator<IMethod> iter= rippleMethodsList.iterator(); iter.hasNext(); ) {
			SearchMatch match= fDeclarationToMatch.get(iter.next());
			if (match != null) {
				iter.remove();
				fBinaryRefs.add(match);
			}
		}
		fDeclarationToMatch= null;
		return rippleMethodsList.toArray(new IMethod[0]);
	}

	private IMethod[] findAllRippleMethods(IProgressMonitor pm, WorkingCopyOwner owner) throws CoreException {
		pm.beginTask("", 4); //$NON-NLS-1$

		findAllDeclarations(new SubProgressMonitor(pm, 1), owner);

		//TODO: report assertion as error status and fall back to only return fMethod
		//check for bug 81058:
		if (! fDeclarations.contains(fMethod))
			Assert.isTrue(false, "Search for method declaration did not find original element: " + fMethod.toString()); //$NON-NLS-1$

		createHierarchyOfDeclarations(new SubProgressMonitor(pm, 1), owner);
		createTypeToMethod();
		createUnionFind();
		if (pm.isCanceled())
			throw new OperationCanceledException();

		fHierarchy= null;
		fRootTypes= null;

		Map<IType, List<IType>> partitioning= new HashMap<>();
		for (IType type : fTypeToMethod.keySet()) {
			IType rep = fUnionFind.find(type);
			List<IType> types = partitioning.get(rep);
			if (types == null)
				types = new ArrayList<>();
			types.add(type);
			partitioning.put(rep, types);
		}
		Assert.isTrue(partitioning.size() > 0);
		if (partitioning.size() == 1)
			return fDeclarations.toArray(new IMethod[0]);

		//Multiple partitions; must look out for nasty marriage cases
		//(types inheriting method from two ancestors, but without redeclaring it).
		// AspectJ Change
		// original
//		IType methodTypeRep= fUnionFind.find(fMethod.getDeclaringType());
		// new
		IType methodTypeRep= fUnionFind.find(getType(fMethod));
		// AspectJ end
		List<IType> relatedTypes= partitioning.get(methodTypeRep);
		boolean hasRelatedInterfaces= false;
		List<IMethod> relatedMethods= new ArrayList<>();
		for (IType relatedType : relatedTypes) {
			relatedMethods.add(fTypeToMethod.get(relatedType));
			if (relatedType.isInterface())
				hasRelatedInterfaces = true;
		}

		//Definition: An alien type is a type that is not a related type. The set of
		// alien types diminishes as new types become related (a.k.a marry a relatedType).

		List<IMethod> alienDeclarations= new ArrayList<>(fDeclarations);
		fDeclarations= null;
		alienDeclarations.removeAll(relatedMethods);
		List<IType> alienTypes= new ArrayList<>();
		boolean hasAlienInterfaces= false;
		for (IMethod alienDeclaration : alienDeclarations) {
			// AspectJ Change
			// original
//			IType alienType= alienDeclaration.getDeclaringType();
			// new
			IType alienType = getType(alienDeclaration);
			// AspectJ end
			alienTypes.add(alienType);
			if (alienType.isInterface())
				hasAlienInterfaces = true;
		}
		if (alienTypes.size() == 0) //no nasty marriage scenarios without types to marry with...
			return relatedMethods.toArray(new IMethod[0]);
		if (! hasRelatedInterfaces && ! hasAlienInterfaces) //no nasty marriage scenarios without interfaces...
			return relatedMethods.toArray(new IMethod[0]);

		//find all subtypes of related types:
		HashSet<IType> relatedSubTypes= new HashSet<>();
		List<IType> relatedTypesToProcess= new ArrayList<>(relatedTypes);
		while (relatedTypesToProcess.size() > 0) {
			//TODO: would only need subtype hierarchies of all top-of-ripple relatedTypesToProcess
			for (IType relatedType : relatedTypesToProcess) {
				if (pm.isCanceled())
					throw new OperationCanceledException();
				ITypeHierarchy hierarchy = getCachedHierarchy(relatedType, owner, new SubProgressMonitor(pm, 1));
				if (hierarchy == null)
					hierarchy = relatedType.newTypeHierarchy(owner, new SubProgressMonitor(pm, 1));
				IType[] allSubTypes = hierarchy.getAllSubtypes(relatedType);
				Collections.addAll(relatedSubTypes, allSubTypes);
			}
			relatedTypesToProcess.clear(); //processed; make sure loop terminates

			HashSet<IType> marriedAlienTypeReps = new HashSet<>();
			for (IType alienType : alienTypes) {
				if (pm.isCanceled())
					throw new OperationCanceledException();
				IMethod alienMethod = fTypeToMethod.get(alienType);
				ITypeHierarchy hierarchy = getCachedHierarchy(alienType, owner, new SubProgressMonitor(pm, 1));
				if (hierarchy == null)
					hierarchy = alienType.newTypeHierarchy(owner, new SubProgressMonitor(pm, 1));
				IType[] allSubtypes = hierarchy.getAllSubtypes(alienType);
				for (IType subtype : allSubtypes) {
					if (relatedSubTypes.contains(subtype)) {
						if (JavaModelUtil.isVisibleInHierarchy(alienMethod, subtype.getPackageFragment())) {
							marriedAlienTypeReps.add(fUnionFind.find(alienType));
						} else {
							// not overridden
						}
					}
				}
			}

			if (marriedAlienTypeReps.size() == 0)
				return relatedMethods.toArray(new IMethod[0]);

			for (IType marriedAlienTypeRep : marriedAlienTypeReps) {
				List<IType> marriedAlienTypes = partitioning.get(marriedAlienTypeRep);
				for (IType marriedAlienInterfaceType : marriedAlienTypes) {
					relatedMethods.add(fTypeToMethod.get(marriedAlienInterfaceType));
				}
				alienTypes.removeAll(marriedAlienTypes); //not alien any more
				relatedTypesToProcess.addAll(marriedAlienTypes); //process freshly married types again
			}
		}

		fRootReps= null;
		fRootHierarchies= null;
		fTypeToMethod= null;
		fUnionFind= null;

		return relatedMethods.toArray(new IMethod[0]);
	}

	private ITypeHierarchy getCachedHierarchy(IType type, WorkingCopyOwner owner, IProgressMonitor monitor) throws JavaModelException {
		IType rep= fUnionFind.find(type);
		if (rep != null) {
			Collection<IType> collection = fRootReps.get(rep);
			for (IType root : collection) {
				ITypeHierarchy hierarchy = fRootHierarchies.get(root);
				if (hierarchy == null) {
					hierarchy = root.newTypeHierarchy(owner, new SubProgressMonitor(monitor, 1));
					fRootHierarchies.put(root, hierarchy);
				}
				if (hierarchy.contains(type))
					return hierarchy;
			}
		}
		return null;
	}

	private void findAllDeclarations(IProgressMonitor monitor, WorkingCopyOwner owner) throws CoreException {
		fDeclarations= new ArrayList<>();

		class MethodRequestor extends SearchRequestor {
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				IMethod method= (IMethod) match.getElement();
				boolean isBinary= method.isBinary();
				if (fBinaryRefs != null || ! (fExcludeBinaries && isBinary)) {
					fDeclarations.add(method);
				}
				if (isBinary && fBinaryRefs != null) {
					fDeclarationToMatch.put(method, match);
				}
			}
		}

		int limitTo = IJavaSearchConstants.DECLARATIONS | IJavaSearchConstants.IGNORE_DECLARING_TYPE | IJavaSearchConstants.IGNORE_RETURN_TYPE;
		int matchRule= SearchPattern.R_ERASURE_MATCH | SearchPattern.R_CASE_SENSITIVE;
		SearchPattern pattern= SearchPattern.createPattern(fMethod, limitTo, matchRule);
		SearchParticipant[] participants= SearchUtils.getDefaultSearchParticipants();
		IJavaSearchScope scope= RefactoringScopeFactory.createRelatedProjectsScope(fMethod.getJavaProject(), IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SYSTEM_LIBRARIES);
		MethodRequestor requestor= new MethodRequestor();
		SearchEngine searchEngine= owner != null ? new SearchEngine(owner) : new SearchEngine();

		searchEngine.search(pattern, participants, scope, requestor, monitor);
	}

	private void createHierarchyOfDeclarations(IProgressMonitor pm, WorkingCopyOwner owner) throws JavaModelException {
		IRegion region = JavaCore.newRegion();
		for (IMethod declaringType : fDeclarations) {
			// AspectJ Change
			// original
			// IType declaringType= ((IMethod) iter.next()).getDeclaringType();
			// new
			// AspectJ end
			region.add(declaringType);
		}
		fHierarchy = JavaCore.newTypeHierarchy(region, owner, pm);
	}

	private void createTypeToMethod() {
		fTypeToMethod = new HashMap<>();
		for (IMethod declaration : fDeclarations) {
			// AspectJ Change
			// original
			//fTypeToMethod.put(declaration.getDeclaringType(), declaration);
			// new
			fTypeToMethod.put(getType(declaration), declaration);
			// AspectJ end
		}
	}

	private void createUnionFind() throws JavaModelException {
		fRootTypes = new HashSet<>(fTypeToMethod.keySet());
		fUnionFind = new UnionFind();
		for (IType type : fTypeToMethod.keySet()) {
			fUnionFind.init(type);
		}
		for (IType type : fTypeToMethod.keySet()) {
			uniteWithSupertypes(type, type);
		}
		fRootReps = new MultiMap();
		for (IType type : fRootTypes) {
			IType rep = fUnionFind.find(type);
			if (rep != null)
				fRootReps.put(rep, type);
		}
		fRootHierarchies = new HashMap<>();
	}

	private void uniteWithSupertypes(IType anchor, IType type) throws JavaModelException {
		IType[] supertypes = fHierarchy.getSupertypes(type);
		for (IType supertype : supertypes) {
			IType superRep = fUnionFind.find(supertype);
			if (superRep == null) {
				//Type doesn't declare method, but maybe supertypes?
				uniteWithSupertypes(anchor, supertype);
			} else {
				//check whether method in supertype is really overridden:
				IMember superMethod = (IMember) fTypeToMethod.get(supertype);
				if (JavaModelUtil.isVisibleInHierarchy(superMethod, anchor.getPackageFragment())) {
					IType rep = fUnionFind.find(anchor);
					fUnionFind.union(rep, superRep);
					// current type is no root anymore
					fRootTypes.remove(anchor);
					uniteWithSupertypes(supertype, supertype);
				} else {
					//Not overridden -> overriding chain ends here.
				}
			}
		}
	}

	// AspectJ change
	private IType getType(IMethod method) {
		if (method instanceof IntertypeElement) {
			IType candidate = ((IntertypeElement) method).findTargetType();
			if (candidate != null) {
				return candidate;
			}
		}
		return method.getDeclaringType();
	}
	// AspectJ end

}
