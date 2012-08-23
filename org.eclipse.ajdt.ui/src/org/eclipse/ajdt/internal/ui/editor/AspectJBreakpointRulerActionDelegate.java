/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class AspectJBreakpointRulerActionDelegate extends RulerToggleBreakpointActionDelegate {

	private IEditorPart fEditorPart;
    private AspectJBreakpointRulerAction fDelegate;
	/**
	 * @see IEditorActionDelegate#setActiveEditor(bIAction, IEditorPart)
	 */
	public void setActiveEditor(IAction callerAction, IEditorPart targetEditor) {
		// only care about compilation unit and class file editors
		fEditorPart = targetEditor;
		if (targetEditor != null) {
			String id= targetEditor.getSite().getId();

            if (!id.equals(JavaUI.ID_CU_EDITOR)
                    && !id.equals(JavaUI.ID_CF_EDITOR)
                    && !id.equals(AspectJEditor.ASPECTJ_EDITOR_ID)) {
				targetEditor= null;
			}
		}
		super.setActiveEditor(callerAction, targetEditor);
	}

	
	/**
	 * @see AbstractRulerActionDelegate#createAction()
	 */
	protected IAction createAction(ITextEditor editor,
			IVerticalRulerInfo rulerInfo) {
		IResource resource;
		IEditorInput editorInput = editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			resource = ((IFileEditorInput) editorInput).getFile();
			if (AspectJPlugin.isAJProject(resource.getProject())) {
				//it's an AspectJ Project, use our action
				return fDelegate = new AspectJBreakpointRulerAction(rulerInfo, editor,
						fEditorPart);
			}
		}
		//	else: use jdts action
		return super.createAction(editor, rulerInfo);
	}

	   /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#dispose()
     */
    public void dispose() {
        if (fDelegate != null) {
            fDelegate.dispose();
        }
        fDelegate = null;
        fEditorPart = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
     */
    public void runWithEvent(IAction action, Event event) {
        if(fDelegate != null) {
            fDelegate.runWithEvent(event);
        } else {
            super.runWithEvent(action, event);
        }
    }
}
