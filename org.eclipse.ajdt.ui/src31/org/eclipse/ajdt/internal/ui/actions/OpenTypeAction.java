package org.eclipse.ajdt.internal.ui.actions;

import org.eclipse.ajdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

public class OpenTypeAction extends
		org.eclipse.jdt.internal.ui.actions.OpenTypeAction {

	public OpenTypeAction() {
		super();
	}
	
	public void run() {
		Shell parent= JavaPlugin.getActiveWorkbenchShell();
		OpenTypeSelectionDialog dialog= new OpenTypeSelectionDialog(parent, false, 
			PlatformUI.getWorkbench().getProgressService(),
			null, IJavaSearchConstants.TYPE);
		dialog.setTitle(JavaUIMessages.OpenTypeAction_dialogTitle); 
		dialog.setMessage(JavaUIMessages.OpenTypeAction_dialogMessage); 
		
		int result= dialog.open();
		if (result != IDialogConstants.OK_ID)
			return;
		
		Object[] types= dialog.getResult();
		if (types != null && types.length > 0) {
			IType type= (IType)types[0];
			try {
				IEditorPart part= EditorUtility.openInEditor(type, true);
				EditorUtility.revealInEditor(part, type);
			} catch (CoreException x) {
				String title= JavaUIMessages.OpenTypeAction_errorTitle; 
				String message= JavaUIMessages.OpenTypeAction_errorMessage; 
				ExceptionHandler.handle(x, title, message);
			}
		}
	}

}
