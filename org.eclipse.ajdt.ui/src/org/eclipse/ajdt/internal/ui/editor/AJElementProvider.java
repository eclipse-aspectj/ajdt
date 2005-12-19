/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sian January - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.IClassFileEditorInput;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.JavaElementProvider;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class AJElementProvider extends JavaElementProvider {

	private JavaEditor fEditor;
	private boolean fUseCodeResolve;

	public AJElementProvider(IEditorPart editor) {
		super(editor);
		this.fEditor = (JavaEditor) editor;
	}

	public AJElementProvider(IEditorPart editor, boolean useCodeResolve) {
		super(editor, useCodeResolve);
		this.fEditor = (JavaEditor) editor;
		this.fUseCodeResolve = useCodeResolve;
	}
	
	/*
	 * @see IInformationProviderExtension#getElement(ITextViewer, IRegion)
	 */
	public Object getInformation2(ITextViewer textViewer, IRegion subject) {
		if (fEditor == null)
			return null;

		try {
			if (fUseCodeResolve) {
				IStructuredSelection sel= SelectionConverter.getStructuredSelection(fEditor);
				if (!sel.isEmpty())
					return sel.getFirstElement();
			}
			IJavaElement element= getElementAtOffset(fEditor);
			if (element != null)
				return element;
			return getInput(fEditor);
		} catch (JavaModelException e) {
			return null;
		}
	}

	/*
	 * Methods below copied from SelectionConverter (changes marked // AspectJ Change)
	 */
	public IJavaElement getElementAtOffset(JavaEditor editor) throws JavaModelException {
		return getElementAtOffset(getInput(editor), (ITextSelection)editor.getSelectionProvider().getSelection());
	}
	
	public static IJavaElement getInput(JavaEditor editor) {
		if (editor == null)
			return null;
		IEditorInput input= editor.getEditorInput();
		if (input instanceof IClassFileEditorInput)
			return ((IClassFileEditorInput)input).getClassFile();
		// AspectJ Change begin - use our WorkingCopyManager
		IWorkingCopyManager manager= AspectJUIPlugin.getDefault().getWorkingCopyManager();				
		// AspectJ Change end
		return manager.getWorkingCopy(input);			
	}
	
	public IJavaElement getElementAtOffset(IJavaElement input, ITextSelection selection) throws JavaModelException {
		if (input instanceof ICompilationUnit) {
			ICompilationUnit cunit= (ICompilationUnit) input;
			JavaModelUtil.reconcile(cunit);
			IJavaElement ref= cunit.getElementAt(selection.getOffset());
			if (ref == null)
				return input;
			else
				return ref;
		} else if (input instanceof IClassFile) {
			IJavaElement ref= ((IClassFile)input).getElementAt(selection.getOffset());
			if (ref == null)
				return input;
			else
				return ref;
		}
		return null;
	}
}
