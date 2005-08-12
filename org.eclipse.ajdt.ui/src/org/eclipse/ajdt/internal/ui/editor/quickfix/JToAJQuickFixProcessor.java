/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor.quickfix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

/**
 * Adapted from org.eclipse.jdt.internal.ui.text.correction.QuickFixProcessor
 */
public class JToAJQuickFixProcessor implements IQuickFixProcessor {

	public boolean hasCorrections(ICompilationUnit cu, int problemId) {
		switch (problemId) {
			case IProblem.UnterminatedString:
			case IProblem.ImportNotFound:
			case IProblem.UndefinedField:
			case IProblem.UndefinedType:
			case IProblem.IncompatibleReturnType:
				return true;
			default:
				return false;
		}
	}	
	
	/* (non-Javadoc)
	 * @see IAssistProcessor#getCorrections(org.eclipse.jdt.internal.ui.text.correction.IAssistContext, org.eclipse.jdt.internal.ui.text.correction.IProblemLocation[])
	 */
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		if (locations == null || locations.length == 0) {
			return null;
		}
		
		// AspectJ Change Begin
		// Only apply to .java files
		IEditorPart ed = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		if ((ed instanceof AspectJEditor)) {
			// we only want to provide corrections for the AspectJ editor
			// otherwise we get double completions in the Java editor
			return null;
		}
		// AspectJ Change End
		
		HashSet handledProblems= new HashSet(locations.length);
		ArrayList resultingCollections= new ArrayList();
		for (int i= 0; i < locations.length; i++) {
			IProblemLocation curr= locations[i];
			Integer id= new Integer(curr.getProblemId());
			if (handledProblems.add(id)) {
				process(context, curr, resultingCollections);
			}
		}
		return (IJavaCompletionProposal[]) resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
	}
	
	private void process(IInvocationContext context, IProblemLocation problem, Collection proposals) throws CoreException {
		int id= problem.getProblemId();
		if (id == 0) { // no proposals for none-problem locations
			return;
		}
		switch (id) {
		case IProblem.UnterminatedString:
//		case IProblem.UnusedImport:
//		case IProblem.DuplicateImport:
//		case IProblem.CannotImportPackage:
//		case IProblem.ConflictingImport:
		case IProblem.ImportNotFound:
//		case IProblem.UndefinedMethod:
//		case IProblem.UndefinedConstructor:
//		case IProblem.ParameterMismatch:
//		case IProblem.MethodButWithConstructorName:
		case IProblem.UndefinedField:
//		case IProblem.UndefinedName:
//		case IProblem.PublicClassMustMatchFileName:
//		case IProblem.PackageIsNotExpectedPackage:
		case IProblem.UndefinedType:
//		case IProblem.TypeMismatch:
//		case IProblem.UnhandledException:
//		case IProblem.UnreachableCatch:
//		case IProblem.InvalidCatchBlockSequence:
//		case IProblem.VoidMethodReturnsValue:
//		case IProblem.ShouldReturnValue:
//		case IProblem.MissingReturnType:
//		case IProblem.NonExternalizedStringLiteral:
//		case IProblem.NonStaticAccessToStaticField:
//		case IProblem.NonStaticAccessToStaticMethod:
//		case IProblem.StaticMethodRequested:
//		case IProblem.NonStaticFieldFromStaticInvocation:
//		case IProblem.InstanceMethodDuringConstructorInvocation:
//		case IProblem.InstanceFieldDuringConstructorInvocation:			
//		case IProblem.NotVisibleMethod:
//		case IProblem.NotVisibleConstructor:
//		case IProblem.NotVisibleType:
//		case IProblem.NotVisibleField:
//		case IProblem.BodyForAbstractMethod:
//		case IProblem.AbstractMethodInAbstractClass:
//		case IProblem.AbstractMethodMustBeImplemented:	
//		case IProblem.BodyForNativeMethod:
//		case IProblem.OuterLocalMustBeFinal:
//		case IProblem.UninitializedLocalVariable:
//		case IProblem.UndefinedConstructorInDefaultConstructor:
//		case IProblem.UnhandledExceptionInDefaultConstructor:
//		case IProblem.NotVisibleConstructorInDefaultConstructor:
//		case IProblem.AmbiguousType:
//		case IProblem.UnusedPrivateMethod:
//		case IProblem.UnusedPrivateConstructor:
//		case IProblem.UnusedPrivateField:
//		case IProblem.UnusedPrivateType:
//		case IProblem.LocalVariableIsNeverUsed:
//		case IProblem.ArgumentIsNeverUsed:
//		case IProblem.MethodRequiresBody:
//		case IProblem.NeedToEmulateFieldReadAccess:
//		case IProblem.NeedToEmulateFieldWriteAccess:
//		case IProblem.NeedToEmulateMethodAccess:
//		case IProblem.NeedToEmulateConstructorAccess:			
//		case IProblem.SuperfluousSemicolon:
//		case IProblem.UnnecessaryCast:
//		case IProblem.UnnecessaryArgumentCast:
//		case IProblem.UnnecessaryInstanceof:
//		case IProblem.IndirectAccessToStaticField:
//		case IProblem.IndirectAccessToStaticMethod:
//		case IProblem.Task:
//		case IProblem.UnusedMethodDeclaredThrownException:
//		case IProblem.UnusedConstructorDeclaredThrownException:
//		case IProblem.UnqualifiedFieldAccess:
//		case IProblem.JavadocMissing:
//		case IProblem.JavadocMissingParamTag:
//		case IProblem.JavadocMissingReturnTag:
//		case IProblem.JavadocMissingThrowsTag:
//		case IProblem.JavadocUndefinedType:
//		case IProblem.JavadocAmbiguousType:
//		case IProblem.JavadocNotVisibleType:
//		case IProblem.JavadocInvalidThrowsClassName:
//		case IProblem.JavadocDuplicateThrowsClassName:
//		case IProblem.JavadocDuplicateReturnTag:
//		case IProblem.JavadocDuplicateParamName:
//		case IProblem.JavadocInvalidParamName:
//		case IProblem.JavadocUnexpectedTag:
//		case IProblem.JavadocInvalidTag:
//		case IProblem.NonBlankFinalLocalAssignment:
//		case IProblem.DuplicateFinalLocalInitialization:
//		case IProblem.FinalFieldAssignment:
//		case IProblem.DuplicateBlankFinalFieldInitialization:
//		case IProblem.AnonymousClassCannotExtendFinalClass:
//		case IProblem.ClassExtendFinalClass:
//		case IProblem.FinalMethodCannotBeOverridden:
//		case IProblem.InheritedMethodReducesVisibility:
//		case IProblem.MethodReducesVisibility:
//		case IProblem.OverridingNonVisibleMethod:
//		case IProblem.CannotOverrideAStaticMethodWithAnInstanceMethod:
//		case IProblem.CannotHideAnInstanceMethodWithAStaticMethod:
//		case IProblem.LocalVariableHidingLocalVariable:
//		case IProblem.LocalVariableHidingField:
//		case IProblem.FieldHidingLocalVariable:
//		case IProblem.FieldHidingField:
//		case IProblem.ArgumentHidingLocalVariable:
//		case IProblem.ArgumentHidingField:
//		case IProblem.IllegalModifierForInterfaceMethod:
//		case IProblem.IllegalModifierForInterface:
//		case IProblem.IllegalModifierForClass:
//		case IProblem.IllegalModifierForInterfaceField:
//		case IProblem.IllegalModifierForMemberInterface:
//		case IProblem.IllegalModifierForMemberClass:
//		case IProblem.IllegalModifierForLocalClass:
//		case IProblem.IllegalModifierForArgument:
//		case IProblem.IllegalModifierForField:
//		case IProblem.IllegalModifierForMethod:
//		case IProblem.IllegalModifierForVariable:
//		case IProblem.IllegalVisibilityModifierForInterfaceMemberType:
		case IProblem.IncompatibleReturnType:
//		case IProblem.IncompatibleExceptionInThrowsClause:
//		case IProblem.NoMessageSendOnArrayType:
//		case IProblem.InvalidOperator:
//		case IProblem.MissingSerialVersion:
//		case IProblem.UnnecessaryElse:
//		case IProblem.SuperclassMustBeAClass:
//		case IProblem.UseAssertAsAnIdentifier:
//		case IProblem.UseEnumAsAnIdentifier:
//		case IProblem.RedefinedLocal:
//		case IProblem.RedefinedArgument:
			SwitchToAspectJEditorProposal.method(context, problem, proposals);
			break;
		default:
		}
	}
}
