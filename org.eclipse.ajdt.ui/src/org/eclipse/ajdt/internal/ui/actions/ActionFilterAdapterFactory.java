package org.eclipse.ajdt.internal.ui.actions;

import org.eclipse.ajdt.core.javaelements.AspectJMemberElement;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IActionFilter;

public class ActionFilterAdapterFactory implements IAdapterFactory {

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType == IActionFilter.class && adaptableObject instanceof AspectJMemberElement) {
            return new AspectElementActionFilter();
        }
        return null;
    }

    public Class[] getAdapterList() {
        return new Class[] { IActionFilter.class };
    }


}
