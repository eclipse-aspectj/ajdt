/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui.refactoring;

import org.eclipse.ajdt.core.ReflectionUtils;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.contribution.jdt.refactoring.IRefactoringProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.jdt.internal.ui.refactoring.UserInterfaceManager;
import org.eclipse.jdt.internal.ui.refactoring.reorg.RenameUserInterfaceManager;
import org.eclipse.jdt.internal.ui.refactoring.reorg.RenameUserInterfaceStarter;
import org.eclipse.jdt.ui.refactoring.RenameSupport;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.Workbench;

/**
 * This is a UI class that ensures that the proper Refactoring
 * and wizard are invoked when rename is selected on an ITD.
 * 
 * @author Andrew Eisenberg
 * @created May 21, 2010
 */
public class ITDRenameRefactoringProvider implements IRefactoringProvider {

    static {
        // ensure the user interface manager is properly initialized
        ReflectionUtils.executePrivateMethod(UserInterfaceManager.class, "put", 
                new Class<?>[] { Class.class, Class.class, Class.class },
                RenameUserInterfaceManager.getDefault(), new Object[] { 
                ITDRenameRefactoringProcessor.class, RenameUserInterfaceStarter.class, RenameITDWizard.class });
    }
    
    public boolean isInterestingElement(IJavaElement element) {
        return element instanceof IntertypeElement;
    }

    public void performRefactoring(IJavaElement element, boolean lightweight) throws CoreException {
        RefactoringStatus status = new RefactoringStatus();
        JavaRenameProcessor processor = new ITDRenameRefactoringProcessor((IntertypeElement) element, status);
        
        if (status.isOK()) {
            final RenameSupport support = ReflectionUtils.executePrivateConstructor(RenameSupport.class, new Class[] { JavaRenameProcessor.class, String.class, int.class }, 
                    new Object[] { processor, null, new Integer(RenameSupport.UPDATE_REFERENCES) });
            if (support != null && support.preCheck().isOK()) {
                support.openDialog(getShell());
            }
        } else {
            IStatusLineManager manager = getStatusLineManager();
            if (manager != null) {
                manager.setErrorMessage(status.toString());
            }
        }
    }
    
    /**
     * Lightweight rename refactoring is often broken inside of {@link AJCompilationUnit}s, 
     * so just disable it.
     * @param elt
     * @return true if the element is inside an {@link AJCompilationUnit}
     */
    public boolean belongsToInterestingCompilationUnit(IJavaElement elt) {
        return elt.getAncestor(IJavaElement.COMPILATION_UNIT) instanceof AJCompilationUnit;
    }

    private IStatusLineManager getStatusLineManager() {
        try {
            return Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorSite().getActionBars().getStatusLineManager();
        } catch (Exception e) {
            // null for some reason, maybe workbench not fully initialized
            return null;
        }
    }

    private Shell getShell() {
        try {
            return Workbench.getInstance().getActiveWorkbenchWindow().getShell();
        } catch (Exception e) {
            // null for some reason, maybe workbench not fully initialized
            return null;
        }
    }

    /**
     * Do not check results for problems if this is an {@link AJCompilationUnit}
     * since the result checking uses the actual file contents and will always
     * produce errors.
     */
    public boolean shouldCheckResultForCompileProblems(ICompilationUnit unit) {
        return ! (unit instanceof AJCompilationUnit);
    }

    // can't get this to work, so not used
//    public ITypeRoot convertRoot(ITypeRoot root) {
//        if (root instanceof AJCompilationUnit) {
//            AJCompilationUnit unit = (AJCompilationUnit) root;
//            char[] contents = unit.getContents();
//            AspectsConvertingParser acp = new AspectsConvertingParser(contents);
//            acp.convert(ConversionOptions.CONSTANT_SIZE);
//            AJCompilationUnit cachedUnit = unit.ajCloneCachingContents(acp.content);
//            return cachedUnit;
//        } else {
//            return root;
//        }
//    }
}
