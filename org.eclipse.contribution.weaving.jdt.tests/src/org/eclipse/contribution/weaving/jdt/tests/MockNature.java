package org.eclipse.contribution.weaving.jdt.tests;

import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class MockNature implements IProjectNature {
    public static final String ID_NATURE = "org.eclipse.contribution.weaving.jdt.tests.mock";
    private IProject project;

    public void configure() throws CoreException {
        WeavableProjectListener.weavableNatureAdded(project);
    }

    public void deconfigure() throws CoreException {

    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

}
