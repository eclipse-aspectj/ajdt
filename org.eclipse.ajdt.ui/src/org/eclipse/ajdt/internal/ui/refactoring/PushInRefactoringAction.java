package org.eclipse.ajdt.internal.ui.refactoring;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.aspectj.asm.IProgramElement.Kind;
import org.eclipse.ajdt.core.javaelements.DeclareElement;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class PushInRefactoringAction implements IWorkbenchWindowActionDelegate, IEditorActionDelegate, IViewActionDelegate {

    private IJavaElement[] currSelection;
                         
	private IWorkbenchWindow window= null;
	
	private CompilationUnitEditor editor = null;
	
	public PushInRefactoringAction() {
    }

	public void dispose() {
	    window = null;
	}

	public void run(IAction action) {
	    if (currSelection != null) {
	        try {
                List<IAspectJElement> itds = findAllITDs(currSelection);
                if (itds.size() > 0) {
                	PushInRefactoring refactoring= new PushInRefactoring();
                	refactoring.setITDs(itds);
                	run(new PushInRefactoringWizard(refactoring, "Push In Intertype Declaration", 
                	        refactoring.createDescriptor()), getShell(), "Push In Intertype Declaration");
                } else {
                    MessageDialog.openInformation(getShell(), "No ITDs", 
                            "No intertype declarations selected");
                }
            } catch (JavaModelException e) {
            }
		}
	}

    private Shell getShell() {
        if (window != null) {
            return window.getShell();
        } else if (editor != null) {
            return editor.getEditorSite().getShell();
        } else {
            return null;
        }
    }

	public void run(RefactoringWizard wizard, Shell parent, String dialogTitle) {
		try {
			RefactoringWizardOpenOperation operation= new RefactoringWizardOpenOperation(wizard);
			operation.run(parent, dialogTitle);
		} catch (InterruptedException exception) {
			// Do nothing
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	    currSelection = null;
	    if (selection instanceof IStructuredSelection) {
            IStructuredSelection extended= (IStructuredSelection) selection;
            Object[] elements= extended.toArray();
            IJavaElement[] candidates = new IJavaElement[elements.length];
            for (int i = 0; i < elements.length; i++) {
                if (elements[i] instanceof IJavaElement) {
                    candidates[i] = (IJavaElement) elements[i];
                    if (candidates[i].isReadOnly()) {
                        // can't refactor binary elements
                        candidates = null;
                        break;
                    }
                } else {
                    // invalid selection
                    candidates = null;
                    break;
                }
            }
            currSelection = candidates;
	    } else if (selection instanceof ITextSelection) {
            if (editor != null) {
                ITextSelection textSel = (ITextSelection) selection;
                CompilationUnitDocumentProvider provider = (CompilationUnitDocumentProvider)
                    editor.getDocumentProvider();
                ICompilationUnit unit = provider.getWorkingCopy(editor.getEditorInput());
                if (unit != null) {
                    try {
                        IJavaElement candidate = unit.getElementAt(textSel.getOffset());
                        if (candidate != null) {
                            currSelection = new IJavaElement[1];
                            currSelection[0] = candidate;
                        }
                    } catch (JavaModelException e) {
                    }
                }
            }
	    }
	    action.setEnabled(currSelection != null && currSelection.length > 0);
	    if (window == null) {
	        IWorkbench workbench = PlatformUI.getWorkbench();
	        window = workbench.getActiveWorkbenchWindow();
	    }
	}
	
	protected List<IAspectJElement> findAllITDs(IJavaElement[] selection) throws JavaModelException {
	    List<IAspectJElement> itds = new LinkedList<IAspectJElement>();
	    for (int i = 0; i < selection.length; i++) {
            IJavaElement element = selection[i];
            itds.addAll(findITDsInChildren(element));
        }
	    return itds;
	}

    private Collection<IAspectJElement> findITDsInChildren(IJavaElement element) throws JavaModelException {
        List<IAspectJElement> itds = new LinkedList<IAspectJElement>();
        if (element.isReadOnly()) {
            return Collections.emptyList();
        }
        if (element instanceof IntertypeElement) {
            itds.add((IAspectJElement) element);
        } else if (element instanceof DeclareElement && 
                (((DeclareElement) element).getAJKind().isDeclareAnnotation() ||
                ((DeclareElement) element).getAJKind() == Kind.DECLARE_PARENTS)) {
            itds.add((IAspectJElement) element);
        } else if (element instanceof IParent) {
            IParent parent = (IParent) element;
            try {
                IJavaElement[] children = parent.getChildren();
                for (int i = 0; i < children.length; i++) {
                    itds.addAll(findITDsInChildren(children[i]));
                }
            } catch (JavaModelException e) {
            }
        }
        return itds;
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if (targetEditor instanceof CompilationUnitEditor) {
            editor = (CompilationUnitEditor) targetEditor;
        } else {
            editor = null;
        }
    }

    public void init(IWorkbenchWindow window) {
    	this.window= window;
    }

    public void init(IViewPart view) {
        window = view.getViewSite().getWorkbenchWindow();
    }
}