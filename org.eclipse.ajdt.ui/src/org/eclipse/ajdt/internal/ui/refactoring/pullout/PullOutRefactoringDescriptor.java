///*******************************************************************************
// * Copyright (c) 2003, 2005, 2010 IBM Corporation and others.
// * All rights reserved. This program and the accompanying materials 
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// * 
// * Contributors:
// *     Kris De Volder - initial version
// *******************************************************************************/
//package org.eclipse.ajdt.internal.ui.refactoring.pullout;
//
//import java.util.Map;
//
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.ltk.core.refactoring.Refactoring;
//import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
//import org.eclipse.ltk.core.refactoring.RefactoringStatus;
//
///**
// * This implementation is bogus/outdated! Will need to be replaced with something
// * that actually describes what current implementation of a PullOutRefactoring can do.
// */
//public class PullOutRefactoringDescriptor extends RefactoringDescriptor {
//
//	public static final String REFACTORING_ID = PullOutRefactoring.class.getCanonicalName();
//
//	private final Map<String,String> fArguments;
//
//	public PullOutRefactoringDescriptor(String project, String description, String comment, Map<String,String> arguments) {
//		super(REFACTORING_ID, project, description, comment, RefactoringDescriptor.STRUCTURAL_CHANGE | RefactoringDescriptor.MULTI_CHANGE);
//		fArguments= arguments;
//	}
//	
//	@Override
//	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
//		PullOutRefactoring refactoring= new PullOutRefactoring();
//		status.merge(refactoring.initialize(fArguments));
//		return refactoring;
//	}
//
//	public Map<String,String> getArguments() {
//		return fArguments;
//	}
//}