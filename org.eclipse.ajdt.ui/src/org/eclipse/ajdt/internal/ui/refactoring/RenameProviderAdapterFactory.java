package org.eclipse.ajdt.internal.ui.refactoring;

import org.eclipse.contribution.jdt.refactoring.IRefactoringProvider;
import org.eclipse.core.runtime.IAdapterFactory;

public class RenameProviderAdapterFactory implements IAdapterFactory {

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType == IRefactoringProvider.class) {
            return new ITDRenameRefactoringProvider();
        }
        return null;
    }
 
    public Class[] getAdapterList() {
        return new Class[] { ITDRenameRefactoringProvider.class };
    }

}
