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
package org.eclipse.ajdt.internal.core.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.PointcutUtilities;
import org.eclipse.ajdt.core.text.CoreMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * When types are renamed this participant updates any references in to
 * that type in aspects (in the same project)
 */
public class AspectRenameParticipant extends RenameParticipant {

	private IType fType;

	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		CompositeChange changes = new CompositeChange(CoreMessages.renameTypeReferences);
		final String oldName = fType.getElementName();
		final String newName = getArguments().getNewName();
		IProject project = fType.getResource().getProject();
		AJLog.log("Rename type references in aspects from "+oldName+" to "+newName); //$NON-NLS-1$ //$NON-NLS-2$
		AJLog.log("qualified name: "+fType.getFullyQualifiedName());
		List ajs = AJCompilationUnitManager.INSTANCE.getCachedCUs(project);
		pm.beginTask(CoreMessages.renameTypeReferences, ajs.size());
		for (Iterator iter = ajs.iterator(); iter.hasNext();) {
			AJCompilationUnit cu = (AJCompilationUnit) iter.next();
			IResource res = cu.getUnderlyingResource();
			if (res.getType() == IResource.FILE) {
				IFile file = (IFile)res;
				AJLog.log("Looking for type references for "+oldName+" in "+file); //$NON-NLS-1$ //$NON-NLS-2$
				TextFileChange change = new TextFileChange(((IJavaElement)cu).getElementName(),
						file);
				TextEdit te = renameJavaSpecificReferences(cu,fType,newName);
				TextEdit[] te2 = renameAspectSpecificReferences(cu,fType,newName);
				if ((te2 != null) && (te2.length > 0)) {					
					if (te==null) {
						te = new MultiTextEdit();
					}
					for (int i = 0; i < te2.length; i++) {
						te.addChild(te2[i]);
					}	
				}
				if (te!=null) {
					change.setEdit(te);
					changes.add(change);
				}
			}
			pm.worked(1);
		}
		
		pm.done();
		if (changes.getChildren().length == 0) {
			return null;
		}
		return changes;
	}

	class AspectChange {
		IAspectJElement element;
		List offsets;
	}
	
	
	private TextEdit[] renameAspectSpecificReferences(AJCompilationUnit ajcu,
			IType type, final String newName) throws JavaModelException {
		List editList = new ArrayList();
		String name = type.getElementName();
		IType[] types = ((ICompilationUnit)ajcu).getTypes();
		for (int i = 0; i < types.length; i++) {
			if (types[i] instanceof AspectElement) {
				AspectChange[] aspectChanges = searchForReferenceInPointcut(
						ajcu, (AspectElement) types[i], name, type.getFullyQualifiedName());
				if (aspectChanges.length > 0) {
					for (int j = 0; j < aspectChanges.length; j++) {
						if (aspectChanges[j].element instanceof ISourceReference) {
							ISourceRange range = ((ISourceReference) aspectChanges[j].element)
									.getSourceRange();
							List offsets = aspectChanges[j].offsets;
							for (Iterator iterator = offsets.iterator(); iterator
									.hasNext();) {
								Integer o = (Integer) iterator.next();
								int offset = range.getOffset() + o.intValue()
										- name.length();
								ReplaceEdit edit = new ReplaceEdit(offset, name
										.length(), newName);
								editList.add(edit);
							}
						}
					}
				}
			}
		}
		return (TextEdit[]) editList.toArray(new TextEdit[editList.size()]);
	}
	
	private static TextEdit renameJavaSpecificReferences(AJCompilationUnit ajcu, IType type, final String newName) throws JavaModelException {
		final String name = type.getElementName();
		final String fqn = type.getFullyQualifiedName();

		ajcu.requestOriginalContentMode();
		final int origLen = ((ISourceReference)ajcu).getSource().length();
		ajcu.discardOriginalContentMode();
		
		ASTParser parser = ASTParser.newParser(AST.JLS3);
	    parser.setSource(ajcu);
		parser.setResolveBindings(true);
	    //parser.setProject(ajcu.getJavaProject());
	    //parser.setUnitName(ajcu.getElementName());
		
	    final CompilationUnit cu = (CompilationUnit) parser.createAST(null);	    
	    final ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
	    cu.accept(new org.eclipse.jdt.core.dom.ASTVisitor() {
			public boolean visit(SimpleName node) {
				String id = node.getIdentifier();
				if (id.equals(name)) {
					final ITypeBinding binding= node.resolveTypeBinding();
					if (binding != null) {
						String qual = binding.getQualifiedName();
						if (qual.equals(fqn)) {
							int endPos = node.getStartPosition()+node.getLength();
							if (endPos < origLen) {
								SimpleName replacement = cu.getAST().newSimpleName(newName);
								rewrite.replace(node, replacement, null);
							}
						}
					}
				}
				return true;
			}
	    });
	    TextEdit edits = rewrite.rewriteAST();
	    if (edits.getLength()==0) {
	    	return null;
	    }
	    return edits;
	}
		
	private AspectChange[] searchForReferenceInPointcut(AJCompilationUnit ajcu,
			AspectElement aspect, String name, String qualifiedName) throws JavaModelException {
		boolean samePackage = removeTypeName(
				((IType) aspect).getFullyQualifiedName()).equals(
				removeTypeName(qualifiedName));
		List elementsToChange = new ArrayList();
		List elementsToSearch = new ArrayList();
		elementsToSearch.addAll(Arrays.asList(aspect.getAdvice()));
		elementsToSearch.addAll(Arrays.asList(aspect.getPointcuts()));
		elementsToSearch.addAll(Arrays.asList(aspect.getDeclares()));
		elementsToSearch.addAll(Arrays.asList(aspect.getITDs()));
		for (Iterator iter = elementsToSearch.iterator(); iter.hasNext();) {
			IAspectJElement element = (IAspectJElement) iter.next();
			if (element instanceof ISourceReference) {
				ajcu.requestOriginalContentMode();
				String src = ((ISourceReference) element).getSource();
				ajcu.discardOriginalContentMode();
				Map map = PointcutUtilities.findAllIdentifiers(src);
				if (map != null) {
					for (Iterator iter2 = map.keySet().iterator(); iter2
							.hasNext();) {
						String id = (String) iter2.next();
						if (id.equals(name)) {
							IImportDeclaration imp = ((ICompilationUnit) ajcu)
									.getImport(qualifiedName);
							if (samePackage || imp.exists()) {
								AJLog.log("found reference"); //$NON-NLS-1$
								AspectChange ac = new AspectChange();
								ac.element = element;
								ac.offsets = (List) map.get(id);
								elementsToChange.add(ac);
							}
						}
					}
				}
			}
		}
		return (AspectChange[]) elementsToChange.toArray(new AspectChange[] {});
	}
	
	private String removeTypeName(String qualifiedName) {
		int ind = qualifiedName.lastIndexOf('.');
		if (ind == -1) {
			return qualifiedName;
		}
		return qualifiedName.substring(0,ind);
	}
	
	public String getName() {
		return ""; //$NON-NLS-1$
	}

	protected boolean initialize(Object element) {
		if (element instanceof IType) {
			fType = (IType) element;
			return true;
		}
		return false;
	}

}
