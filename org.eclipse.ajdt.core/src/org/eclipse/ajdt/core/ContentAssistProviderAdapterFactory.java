package org.eclipse.ajdt.core;

import org.eclipse.ajdt.internal.core.contentassist.ContentAssistProvider;
import org.eclipse.contribution.jdt.itdawareness.ContentAssistAdapter;
import org.eclipse.contribution.jdt.itdawareness.IJavaContentAssistProvider;
import org.eclipse.core.runtime.IAdapterFactory;

@SuppressWarnings("unchecked")
public class ContentAssistProviderAdapterFactory implements IAdapterFactory {

    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (adapterType == IJavaContentAssistProvider.class) {
            return new ContentAssistProvider();
        }
        return null;
    }

    public Class[] getAdapterList() {
        return new Class[] { IJavaContentAssistProvider.class };
    }

}
