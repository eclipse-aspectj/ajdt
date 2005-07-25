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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.ajdt.internal.ui.dialogs.AJTypeInfoLabelProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.internal.corext.util.TypeInfo;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.jdt.internal.ui.dialogs.MultiElementListSelectionDialog;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.jdt.internal.ui.util.ElementValidator;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.util.TypeInfoLabelProvider;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.actions.OrganizeImportsAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorActionBarContributor;

/**
 *  AspectJ organise imports action.  Some copied from OrganizeImportsAction
 */
public class AJOrganizeImportsAction extends OrganizeImportsAction {

	private JavaEditor fEditor;

	public AJOrganizeImportsAction(JavaEditor editor) {
		super(editor);
		this.fEditor = editor;
	}
	
	/**
	 * Note: This method is for internal use only. Clients should not call this method.
	 */
	public void run(ICompilationUnit cu) {
		if (!ElementValidator.check(cu, getShell(), ActionMessages.getString("OrganizeImportsAction.error.title"), fEditor != null)) //$NON-NLS-1$ 
			return;
		if (!ActionUtil.isProcessable(getShell(), cu))
			return;
			
		try {
			IPreferenceStore store= PreferenceConstants.getPreferenceStore();
			String[] prefOrder= JavaPreferencesSettings.getImportOrderPreference(store);
			int threshold= JavaPreferencesSettings.getImportNumberThreshold(store);
			boolean ignoreLowerCaseNames= store.getBoolean(PreferenceConstants.ORGIMPORTS_IGNORELOWERCASE);
			
			if (fEditor == null && EditorUtility.isOpenInEditor(cu) == null) {
				IEditorPart editor= EditorUtility.openInEditor(cu);
				if (editor instanceof JavaEditor) {
					fEditor= (JavaEditor) editor;
				}			
			}
			AJOrganizeImportsOperation op= new AJOrganizeImportsOperation(cu, prefOrder, threshold, ignoreLowerCaseNames, !cu.isWorkingCopy(), true, createChooseImportQuery());
		
			IRewriteTarget target= null;
			if (fEditor != null) {
				target= (IRewriteTarget) fEditor.getAdapter(IRewriteTarget.class);
				if (target != null) {
					target.beginCompoundChange();
				}
			}
			
			BusyIndicatorRunnableContext context= new BusyIndicatorRunnableContext();
			try {
				PlatformUI.getWorkbench().getProgressService().runInUI(context,
					new WorkbenchRunnableAdapter(op, op.getScheduleRule()),
					op.getScheduleRule());
				IProblem parseError= op.getParseError();
				if (parseError != null) {
					String message= ActionMessages.getFormattedString("OrganizeImportsAction.single.error.parse", parseError.getMessage()); //$NON-NLS-1$
					MessageDialog.openInformation(getShell(), ActionMessages.getString("OrganizeImportsAction.error.title"), message); //$NON-NLS-1$ 
					if (fEditor != null && parseError.getSourceStart() != -1) {
						fEditor.selectAndReveal(parseError.getSourceStart(), parseError.getSourceEnd() - parseError.getSourceStart() + 1);
					}
				} else {
					if (fEditor != null) {
						setStatusBarMessage(getOrganizeInfo(op));
					}
				}
			} catch (InvocationTargetException e) {
				ExceptionHandler.handle(e, getShell(), ActionMessages.getString("OrganizeImportsAction.error.title"), ActionMessages.getString("OrganizeImportsAction.error.message")); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (InterruptedException e) {
			} finally {
				if (target != null) {
					target.endCompoundChange();
				}
			}
		} catch (CoreException e) {	
			ExceptionHandler.handle(e, getShell(), ActionMessages.getString("OrganizeImportsAction.error.title"), ActionMessages.getString("OrganizeImportsAction.error.message")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private IChooseImportQuery createChooseImportQuery() {
		return new IChooseImportQuery() {
			public TypeInfo[] chooseImports(TypeInfo[][] openChoices, ISourceRange[] ranges) {
				return doChooseImports(openChoices, ranges);
			}
		};
	}
	
	private TypeInfo[] doChooseImports(TypeInfo[][] openChoices, final ISourceRange[] ranges) {
		// remember selection
		ISelection sel= fEditor != null ? fEditor.getSelectionProvider().getSelection() : null;
		TypeInfo[] result= null;
		ILabelProvider labelProvider= new AJTypeInfoLabelProvider(TypeInfoLabelProvider.SHOW_FULLYQUALIFIED);
		
		MultiElementListSelectionDialog dialog= new MultiElementListSelectionDialog(getShell(), labelProvider) {
			protected void handleSelectionChanged() {
				super.handleSelectionChanged();
				// show choices in editor
				doListSelectionChanged(getCurrentPage(), ranges);
			}
		};
		dialog.setTitle(ActionMessages.getString("OrganizeImportsAction.selectiondialog.title")); //$NON-NLS-1$
		dialog.setMessage(ActionMessages.getString("OrganizeImportsAction.selectiondialog.message")); //$NON-NLS-1$
		dialog.setElements(openChoices);
		if (dialog.open() == Window.OK) {
			Object[] res= dialog.getResult();			
			result= new TypeInfo[res.length];
			for (int i= 0; i < res.length; i++) {
				Object[] array= (Object[]) res[i];
				if (array.length > 0)
					result[i]= (TypeInfo) array[0];
			}
		}
		// restore selection
		if (sel instanceof ITextSelection) {
			ITextSelection textSelection= (ITextSelection) sel;
			fEditor.selectAndReveal(textSelection.getOffset(), textSelection.getLength());
		}
		return result;
	}

	
	private void doListSelectionChanged(int page, ISourceRange[] ranges) {
		if (fEditor != null && ranges != null && page >= 0 && page < ranges.length) {
			ISourceRange range= ranges[page];
			fEditor.selectAndReveal(range.getOffset(), range.getLength());
		}
	}

	private void setStatusBarMessage(String message) {
		IEditorActionBarContributor contributor= fEditor.getEditorSite().getActionBarContributor();
		if (contributor instanceof EditorActionBarContributor) {
			IStatusLineManager manager= ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
			manager.setMessage(message);
		}
	}
	
	
	private String getOrganizeInfo(AJOrganizeImportsOperation op) {
		int nImportsAdded= op.getNumberOfImportsAdded();
		if (nImportsAdded >= 0) {
			return ActionMessages.getFormattedString("OrganizeImportsAction.summary_added", String.valueOf(nImportsAdded)); //$NON-NLS-1$
		} else {
			return ActionMessages.getFormattedString("OrganizeImportsAction.summary_removed", String.valueOf(-nImportsAdded)); //$NON-NLS-1$
		}
	}
}