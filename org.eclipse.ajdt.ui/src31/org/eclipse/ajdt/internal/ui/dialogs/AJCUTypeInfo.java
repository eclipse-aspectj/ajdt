package org.eclipse.ajdt.internal.ui.dialogs;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.corext.util.IFileTypeInfo;

public class AJCUTypeInfo extends IFileTypeInfo {

	IJavaElement javaElement;	
	private boolean isAspect;
	
	public AJCUTypeInfo(String pkg, String name, char[][] enclosingTypes,
			int modifiers, boolean isAspect, String project, String sourceFolder, String file,
			String extension, IJavaElement javaElement) {
		super(pkg, name, enclosingTypes, modifiers, project, sourceFolder,
				file, extension);
		this.javaElement = javaElement;
		this.isAspect = isAspect;
	}
	
	protected IJavaElement getJavaElement(IJavaSearchScope scope) {
		return javaElement;
	}
	
	public boolean isAspect() {
		return isAspect;
	}
}
