package org.eclipse.ajdt.internal.ui.actions;

import org.eclipse.ajdt.core.javaelements.AspectJMemberElement;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IActionFilter;

public class ActionFilterAdapterFactory implements IAdapterFactory {
  @Override
  public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
    if (adapterType == IActionFilter.class && adaptableObject instanceof AspectJMemberElement)
      return adapterType.cast(new AspectElementActionFilter());
    return null;
  }

  @Override
  public Class<?>[] getAdapterList() {
    return new Class<?>[] { IActionFilter.class };
  }
}
