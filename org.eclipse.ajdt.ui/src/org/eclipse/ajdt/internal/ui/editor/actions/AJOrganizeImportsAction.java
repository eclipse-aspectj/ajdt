/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor.actions;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.org.eclipse.jdt.core.compiler.InvalidInputException;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorInput;

/**
 *  
 */
public class AJOrganizeImportsAction extends Action {
	private IAction importsAction;
	private IEditorInput input;

	public AJOrganizeImportsAction(IAction importsAction, IEditorInput input) {
		super();
		this.importsAction = importsAction;
		this.input = input;
	}
	
	public void run() {
		System.out.println("AJ og imports run");
		IWorkingCopyManager manager= JavaPlugin.getDefault().getWorkingCopyManager();
		ICompilationUnit cu= manager.getWorkingCopy(input);
		System.out.println("cu="+cu);
		if (cu != null) {
			IBuffer buffer;
			try {
				buffer = cu.getBuffer();
				if (buffer != null) {
					extractAspectImports(buffer);
				}
			} catch (JavaModelException e) {
			}
		}
		importsAction.run();
		System.out.println("AJ og imports run finished");
	}

	private void extractAspectImports(IBuffer buffer) {
		Scanner scanner = new Scanner();
		scanner.setSource(buffer.getCharacters());
		
		List importList = new ArrayList();
		StringBuffer importBuffer = new StringBuffer();
		String extendsName = null;
		
		int tok;
		boolean insideImport = false;
		boolean insideExtends = false;
		boolean done = false;
		while (!done) {

			try {
				tok = scanner.getNextToken();
			} catch (InvalidInputException e) {
				continue;
			}
			if (tok == TerminalTokens.TokenNameEOF)
				break;

			switch (tok) {
				case TerminalTokens.TokenNameIdentifier:
					char[] name = scanner.getCurrentIdentifierSource();
					if (insideImport) {
						importBuffer.append(name);
					} else if (insideExtends) {
						extendsName = new String(name);
					}
					break;
				case TerminalTokens.TokenNameDOT:
					if (insideImport) {
						importBuffer.append('.');
					}
					break;
				case TerminalTokens.TokenNameMULTIPLY:
					if (insideImport) {
						importBuffer.append('*');
					}
					break;
				case TerminalTokens.TokenNameextends:
					System.out.println("extends");
					insideExtends = true;
					break;
				case TerminalTokens.TokenNameimport:
					System.out.println("import");
					insideImport = true;
					importBuffer = new StringBuffer();
					break;
				case TerminalTokens.TokenNameSEMICOLON:
					if (insideImport) {
						System.out.println("import line="+importBuffer.toString());
						importList.add(importBuffer.toString());
					}
					insideImport = false;
					break;
				case TerminalTokens.TokenNameLBRACE:
					insideExtends = false;
					done = true;
					break;
				default:
					break;
			}
		}
		
		if (extendsName != null) {
			System.out.println("found extends: "+extendsName);
		}
	}

}