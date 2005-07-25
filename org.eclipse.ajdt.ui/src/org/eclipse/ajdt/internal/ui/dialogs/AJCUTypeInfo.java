package org.eclipse.ajdt.internal.ui.dialogs;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.internal.builder.BuilderUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.corext.util.IFileTypeInfo;

public class AJCUTypeInfo extends IFileTypeInfo {

	public static final char SEPARATOR= '/';
	public static final char EXTENSION_SEPARATOR= '.';
	public static final char PACKAGE_PART_SEPARATOR='.';
	
	IJavaElement javaElement;	
	private boolean isAspect;
	private char[][] enclosingTypes;
	
	public AJCUTypeInfo(String pkg, String name, char[][] enclosingTypes,
			int modifiers, boolean isAspect, String project, String sourceFolder, String file,
			String extension, IJavaElement javaElement) {
		super(pkg, name, enclosingTypes, modifiers, project, sourceFolder,
				file, extension);
		this.enclosingTypes = enclosingTypes;
		this.javaElement = javaElement;
		this.isAspect = isAspect;
	}

	public AJCUTypeInfo(String pkg, String name, char[][] enclosingTypes,
			int modifiers, String project, String sourceFolder, String file,
			String extension) {
		super(pkg, name, enclosingTypes, modifiers, project, sourceFolder,
				file, extension);
		this.enclosingTypes = enclosingTypes;
		this.javaElement = findJavaElement();
		this.isAspect = findIsAspect();
	}
	
	private boolean findIsAspect() {
		if(javaElement != null) {
			try {
				IType[] types = ((ICompilationUnit)javaElement).getTypes();

				for (int i = 0; i < types.length; i++) {
					if(types[i].getElementName().equals(getTypeName())) {
						char[][] typesEnclosingTypes = BuilderUtils.getEnclosingTypes(types[i]);
						if(typesEnclosingTypes.length == enclosingTypes.length) {
							return types[i] instanceof AspectElement;
						}
					}
				}
			} catch (JavaModelException e) {
			}
		}
		return false;
	}

	private IJavaElement findJavaElement() {
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		IPath path= new Path(getPath());
		IResource resource= root.findMember(path);
		if (resource != null) {
			IJavaElement elem= AJCompilationUnitManager.INSTANCE.getAJCompilationUnit((IFile)resource);
			if (elem.exists()) {
				return elem;
			}
		}
		return null;
	}

	protected IJavaElement getContainer(IJavaSearchScope scope) {
		return javaElement;
	}
	
	public boolean isAspect() {
		return isAspect;
	}
}
