package org.eclipse.ajdt.internal.ui.refactoring;

import org.eclipse.contribution.jdt.refactoring.IRefactoringProvider;
import org.eclipse.core.runtime.IAdapterFactory;

public class RenameProviderAdapterFactory implements IAdapterFactory {
  @Override
  public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
    if (adapterType == IRefactoringProvider.class)
      return adapterType.cast(new ITDRenameRefactoringProvider());
    return null;
  }

  @Override
  public Class<?>[] getAdapterList() {
    return new Class<?>[] { ITDRenameRefactoringProvider.class };
  }
}
