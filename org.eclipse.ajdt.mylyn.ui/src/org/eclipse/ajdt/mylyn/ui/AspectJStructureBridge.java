package org.eclipse.ajdt.mylyn.ui;

import java.util.List;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.IAJCodeElement;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.core.internal.resources.Marker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.ui.packageview.ClassPathContainer;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.mylyn.internal.java.ui.JavaStructureBridge;
import org.eclipse.mylyn.internal.java.ui.JavaUiBridgePlugin;
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.eclipse.ui.internal.WorkingSet;
import org.eclipse.ui.views.markers.internal.ConcreteMarker;

/**
 * Structure bridge so that AspectJ program elements can be a part of a mylyn 
 * task context. 
 * @author andrew eisenberg
 *
 */
public class AspectJStructureBridge extends JavaStructureBridge {

	public final static String CONTENT_TYPE = "aspectj";
	
	public String getContentType() {
		return CONTENT_TYPE;
	}



	@Override
	public String getHandleForOffsetInObject(Object object, int offset) {
		IMarker marker;
		int charStart = 0;
		if (object instanceof ConcreteMarker) {
			marker = ((ConcreteMarker)object).getMarker();
		} else if (object instanceof Marker) {
			marker = (Marker)object;
		} else {
			return null;
		}
		
		Object attribute = marker.getAttribute(IMarker.CHAR_START, 0);
		if (attribute instanceof Integer) {
			charStart = ((Integer)attribute).intValue();
		}
		
		try {
			ICompilationUnit compilationUnit = null;
			IResource resource = marker.getResource();
			if (resource instanceof IFile) {
				IFile file = (IFile)resource;
				// TODO: get rid of file extension check
				if (file.getFileExtension().equals("aj")) {
					compilationUnit = JavaCore.createCompilationUnitFrom(file);
				} else {
					return null;
				}
			}
			if (compilationUnit != null) {
				IJavaElement javaElement = compilationUnit.getElementAt(charStart);
				if (javaElement != null) {
					if (javaElement instanceof IImportDeclaration)
						javaElement = javaElement.getParent().getParent();
					return javaElement.getHandleIdentifier();
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (JavaModelException ex) {
			if (!ex.isDoesNotExist())
				ExceptionHandler.handle(ex, "error", "could not find java element"); //$NON-NLS-2$ //$NON-NLS-1$
			return null;
		} catch (Throwable t) {
			StatusHandler.log(new Status(IStatus.ERROR, JavaUiBridgePlugin.PLUGIN_ID, "Could not find element for: " + marker, t));
			return null;
		}
	}
	
	@Override
	public Object getObjectForHandle(String handle) {
		try {
			return AspectJCore.create(handle);
		} catch (Throwable t) {
			StatusHandler.log(new Status(IStatus.WARNING, JavaUiBridgePlugin.PLUGIN_ID, "Could not create java element for handle: " + handle, t));
			return null;
		}
	}

	@Override
	public boolean acceptsObject(Object object) {
		return object instanceof IAJCodeElement || 
 			   object instanceof IAspectJElement;

	}

}
