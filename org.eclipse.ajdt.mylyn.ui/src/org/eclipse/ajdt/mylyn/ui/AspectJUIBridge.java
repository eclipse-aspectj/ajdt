package org.eclipse.ajdt.mylyn.ui;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.java.ui.JavaUiBridge;
import org.eclipse.mylyn.internal.java.ui.JavaUiBridgePlugin;
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.eclipse.ui.IEditorPart;

/**
 * UI Bridge for AspectJ elements in a Mylyn task context.
 * @author andrew
 *
 */
public class AspectJUIBridge extends JavaUiBridge {

	public AspectJUIBridge() {
	} 

	@Override
	public boolean acceptsEditor(IEditorPart editorPart) {
		return editorPart instanceof AspectJEditor;
	}


	@Override
	public String getContentType() {
		return AspectJStructureBridge.CONTENT_TYPE;
	}


	@Override
	public void open(IInteractionElement node) {
		IJavaElement javaElement = AspectJCore.create(node.getHandleIdentifier());
		if (javaElement == null || !javaElement.exists())
			return;
		try {
			IEditorPart part = JavaUI.openInEditor(javaElement);
			JavaUI.revealInEditor(part, javaElement);
		} catch (Throwable t) {
			StatusHandler.fail(new Status(IStatus.ERROR, JavaUiBridgePlugin.PLUGIN_ID, "Could not open editor for: " + node, t));
		}
	}

	@Override
	public Object getObjectForTextSelection(TextSelection selection,
			IEditorPart editor) {
		if (editor instanceof AspectJEditor) {
			TextSelection textSelection = selection;
			try {
				if (selection != null) {
					return SelectionConverter.resolveEnclosingElement((AspectJEditor) editor, textSelection);
				} else {
					Object element = editor.getEditorInput().getAdapter(IJavaElement.class);
					if (element instanceof IJavaElement)
						return element;
				}
			} catch (JavaModelException e) {
				// ignore
			}
		}
		return null;
	}

}
