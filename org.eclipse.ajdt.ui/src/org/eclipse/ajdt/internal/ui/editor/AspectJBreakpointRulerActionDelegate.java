/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.debug.ui.actions.ManageBreakpointRulerAction;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class AspectJBreakpointRulerActionDelegate extends AbstractRulerActionDelegate {

	private IEditorPart fEditorPart;
	/**
	 * @see IEditorActionDelegate#setActiveEditor(bIAction, IEditorPart)
	 */
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		// only care about compilation unit and class file editors
		fEditorPart = targetEditor;
		if (targetEditor != null) {
			String id= targetEditor.getSite().getId();
//			System.err.println("ID="+id);
//			System.err.println("JavaUI.ID_CU_EDITOR="+JavaUI.ID_CU_EDITOR);
//			System.err.println("JavaUI.ID_CF_EDITOR="+JavaUI.ID_CF_EDITOR);

// ASC - This is a copy of the JDT internal class, but the if() has been
// extended to allow for our AJDT CompilationUnitEditor - this CUE is
// the same as the ID_CU_EDITOR but for Aspects, to ensure breakpoints
// are handled the same by the AJDT editor as they are by the ID_CU_EDITOR
// we have to ensure the targetEditor below is not nulled out.
			if (!id.equals(JavaUI.ID_CU_EDITOR) && !id.equals(JavaUI.ID_CF_EDITOR) 
			&& !id.equals(AspectJEditor.ASPECTJ_EDITOR_ID))
				targetEditor= null;
		}
		super.setActiveEditor(callerAction, targetEditor);
	}

	
	/**
	 * @see AbstractRulerActionDelegate#createAction()
	 */
	protected IAction createAction(ITextEditor editor, IVerticalRulerInfo rulerInfo) {
		try {
			IResource  resource;
			IEditorInput editorInput = editor.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				resource= ((IFileEditorInput)editorInput).getFile();
				if (resource.getProject().hasNature(AspectJUIPlugin.ID_NATURE)){
					//it's an AspectJ Project, use our action
					return new AspectJBreakpointRulerAction(rulerInfo, editor, fEditorPart);
				}
			}
		} catch (CoreException e) {
		}
		//else: use jdts action
		return new ManageBreakpointRulerAction(rulerInfo, editor);
	}
}
