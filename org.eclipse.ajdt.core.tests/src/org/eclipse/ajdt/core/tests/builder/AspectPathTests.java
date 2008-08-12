package org.eclipse.ajdt.core.tests.builder;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class AspectPathTests extends AJDTCoreTestCase {

    // tests bug 243356
    // tests that when a container is added to the aspect path,
    // those classpath entries contained on it that have an asepct path
    // attribute are appropriately added to the aspect path
    public void testContainerOnAspectPath() throws Exception {

        IProject hasLib = createPredefinedProject("HasLib"); //$NON-NLS-1$
        IFile lib = hasLib.getFile("lib.jar"); //$NON-NLS-1$
        IProject makeContainer = createPredefinedProject("MakeContainer"); //$NON-NLS-1$
        IJavaProject jMakeContainer = JavaCore.create(makeContainer);

        final IClasspathEntry[] entries = new IClasspathEntry[2];
        entries[0] = JavaCore.newLibraryEntry(lib.getLocation(), //
                null /*srcPath*/, null /*srcRoot*/, new IAccessRule[0], //
                new IClasspathAttribute[] {AspectJCorePreferences.ASPECTPATH_ATTRIBUTE}, // 
                false /*not exported*/);

        entries[1] = JavaCore.newProjectEntry(hasLib.getFullPath(), 
                new IAccessRule[0] /*accessRules*/,
                true /*combineAccessRules*/,
                new IClasspathAttribute[] {AspectJCorePreferences.ASPECTPATH_ATTRIBUTE},
                false /*isExported*/);

        IClasspathContainer container = new IClasspathContainer() {
            public IClasspathEntry[] getClasspathEntries() {
                return entries;
            }
            public String getDescription() {
                return ""; //$NON-NLS-1$
            }
            public int getKind() {
                  return IClasspathContainer.K_APPLICATION;
            }
            public IPath getPath() {
                return new Path("ajcontainer"); //$NON-NLS-1$
            }
        };

        IClasspathEntry[] cp = jMakeContainer.getRawClasspath();
        if (!cp[cp.length - 1].getPath().equals(container.getPath())) {
            IClasspathEntry[] newCp = new IClasspathEntry[cp.length + 1];
            System.arraycopy(cp, 0, newCp, 0, cp.length);
            newCp[cp.length] = JavaCore.newContainerEntry(container.getPath());
            
            jMakeContainer.setRawClasspath(newCp, null);
        }

        JavaCore.setClasspathContainer(
                container.getPath(), 
                new IJavaProject[] {jMakeContainer},
                new IClasspathContainer[] {container},
                null);

        String[] aspectPath = AspectJCorePreferences.getResolvedProjectAspectPath(makeContainer);
        
        assertTrue("Should have lib.jar on the aspect path", aspectPath[0].endsWith("lib.jar:"));  //$NON-NLS-1$//$NON-NLS-2$
    }
}
