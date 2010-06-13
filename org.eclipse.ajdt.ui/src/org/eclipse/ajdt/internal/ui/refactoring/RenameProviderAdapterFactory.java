package org.eclipse.ajdt.internal.ui.refactoring;

import org.eclipse.ajdt.ui.AJDTNameEnvironmentProvider;
import org.eclipse.contribution.jdt.itdawareness.INameEnvironmentProvider;
import org.eclipse.contribution.jdt.itdawareness.IRenameRefactoringProvider;
import org.eclipse.core.runtime.IAdapterFactory;

public class RenameProviderAdapterFactory implements IAdapterFactory {

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType == IRenameRefactoringProvider.class) {
            return new ITDRenameRefactoringProvider();
        }
        return null;
    }
 
    public Class[] getAdapterList() {
        return new Class[] { ITDRenameRefactoringProvider.class };
    }

}
