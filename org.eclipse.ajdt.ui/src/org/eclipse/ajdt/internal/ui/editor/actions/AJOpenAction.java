/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.PointcutElement;
import org.eclipse.ajdt.core.javaelements.PointcutUtilities;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/*
 * copied from org.eclipse.jdt.ui.actions.OpenAction changes marked // AspectJ
 * change
 */
public class AJOpenAction extends SelectionDispatchAction {

	private JavaEditor fEditor;

	/**
	 * Creates a new <code>OpenAction</code>. The action requires that the
	 * selection provided by the site's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param site
	 *            the site providing context information for this action
	 */
	public AJOpenAction(IWorkbenchSite site) {
		super(site);
		setText(ActionMessages.OpenAction_label);
		setToolTipText(ActionMessages.OpenAction_tooltip);
		setDescription(ActionMessages.OpenAction_description);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IJavaHelpContextIds.OPEN_ACTION);
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call
	 * this constructor.
	 * 
	 * @param editor
	 *            the Java editor
	 */
	public AJOpenAction(JavaEditor editor) {
		this(editor.getEditorSite());
		fEditor = editor;
		setText(ActionMessages.OpenAction_declaration_label);
		setEnabled(SelectionConverter.canOperateOn(fEditor));
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	public void selectionChanged(ITextSelection selection) {
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(checkEnabled(selection));
	}

	private boolean checkEnabled(IStructuredSelection selection) {
		if (selection.isEmpty())
			return false;
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof ISourceReference)
				continue;
			if (element instanceof IFile)
				continue;
			if (element instanceof IStorage)
				continue;
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	public void run(ITextSelection selection) {
		if (!ActionUtil.isProcessable(fEditor))
			return;
		try {
			IJavaElement[] elements= SelectionConverter.codeResolveForked(fEditor, false);
			// begin AspectJ change
			IJavaElement element = null;	
			if (elements == null || elements.length == 0) {
				// it might be a pointcut
				IJavaElement input = SelectionConverter.getInput(fEditor);
				if (input instanceof ICompilationUnit) {
					input = AJCompilationUnitManager.mapToAJCompilationUnit((ICompilationUnit)input);
				}
				if (input instanceof AJCompilationUnit) {
					AJCompilationUnit ajcu = (AJCompilationUnit) input;
					String sel = selection.getText();
					if (selection.getLength() == 0) {
						ajcu.requestOriginalContentMode();
						String source = ajcu.getSource();
						ajcu.discardOriginalContentMode();
						sel = PointcutUtilities.findIdentifier(source, selection.getOffset());
					}
					if ((sel != null) && (sel.length() > 0)) {
						IJavaElement el = ajcu.getElementAt(selection
								.getOffset());
						if ((el instanceof AdviceElement)
								|| (el instanceof PointcutElement)) {
							element = PointcutUtilities.findPointcut(el, sel);
						}
					}
				}
			} else {
				element = elements[0];
				if (elements.length > 1)
					element= SelectionConverter.selectJavaElement(elements, getShell(), getDialogTitle(), ActionMessages.OpenAction_select_element);
			}
			// end AspectJ change
			if (element == null) {
				IEditorStatusLine statusLine = (IEditorStatusLine) fEditor
						.getAdapter(IEditorStatusLine.class);
				if (statusLine != null)
					statusLine
							.setMessage(
									true,
									ActionMessages.OpenAction_error_messageBadSelection,
									null);
				getShell().getDisplay().beep();
				return;
			}
			IJavaElement input = SelectionConverter.getInput(fEditor);
			int type = element.getElementType();
			if (type == IJavaElement.JAVA_PROJECT
					|| type == IJavaElement.PACKAGE_FRAGMENT_ROOT
					|| type == IJavaElement.PACKAGE_FRAGMENT)
				element = input;
			run(new Object[] { element });
		} catch (InvocationTargetException e) {
			showError(e);
		} catch (JavaModelException e) {
			showError(e);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	public void run(IStructuredSelection selection) {
		if (!checkEnabled(selection))
			return;
		run(selection.toArray());
	}

	/**
	 * Note: this method is for internal use only. Clients should not call this
	 * method.
	 * 
	 * @param elements
	 *            the elements to process
	 */
	public void run(Object[] elements) {
		if (elements == null)
			return;
		
		MultiStatus status= new MultiStatus(JavaUI.ID_PLUGIN, IStatus.OK, ActionMessages.OpenAction_multistatus_message, null);
		
		for (int i= 0; i < elements.length; i++) {
			Object element= elements[i];
			try {
				element= getElementToOpen(element);
				boolean activateOnOpen= fEditor != null ? true : OpenStrategy.activateOnOpen();
				IEditorPart part= EditorUtility.openInEditor(element, activateOnOpen);
				if (part != null && element instanceof IJavaElement)
					JavaUI.revealInEditor(part, (IJavaElement)element);
			} catch (PartInitException e) {
				String message= Messages.format(ActionMessages.OpenAction_error_problem_opening_editor, new String[] { new JavaUILabelProvider().getText(element), e.getStatus().getMessage() });
				status.add(new Status(IStatus.ERROR, JavaUI.ID_PLUGIN, IStatus.ERROR, message, null));
			} catch (CoreException e) {
				String message= Messages.format(ActionMessages.OpenAction_error_problem_opening_editor, new String[] { new JavaUILabelProvider().getText(element), e.getStatus().getMessage() });
				status.add(new Status(IStatus.ERROR, JavaUI.ID_PLUGIN, IStatus.ERROR, message, null));
				JavaPlugin.log(e);
			}
		}
		if (!status.isOK()) {
			IStatus[] children= status.getChildren();
			ErrorDialog.openError(getShell(), getDialogTitle(), ActionMessages.OpenAction_error_message, children.length == 1 ? children[0] : status);
		}
	}

	/**
	 * Note: this method is for internal use only. Clients should not call this
	 * method.
	 * 
	 * @param object
	 *            the element to open
	 * @return the real element to open
	 * @throws JavaModelException
	 *             if an error occurs while accessing the Java model
	 */
	public Object getElementToOpen(Object object) throws JavaModelException {
		return object;
	}

	private String getDialogTitle() {
		return ActionMessages.OpenAction_error_title;
	}

	private void showError(CoreException e) {
		ExceptionHandler.handle(e, getShell(), getDialogTitle(),
				ActionMessages.OpenAction_error_message);
	}
	
	private void showError(InvocationTargetException e) {
		ExceptionHandler.handle(e, getShell(), getDialogTitle(), ActionMessages.OpenAction_error_message); 
	}
}
