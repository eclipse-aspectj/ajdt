/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.lazystart;

import org.eclipse.ajdt.core.builder.AJBuilder;
import org.eclipse.ajdt.core.javaelements.AspectJMemberElement;
import org.eclipse.ajdt.core.lazystart.IAdviceChangedListener;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

/*
 * Loading classes in this lazystart package does not immediately cause the
 * plugin to active (as specified in MANIFEST.MF). This is done to avoid early
 * activation of AJDT plugins. Once AJDT classes outside this package are
 * referred to, the plugins are then activated.
 */
public class AdviceImageDecorator implements ILightweightLabelDecorator {

	private ListenerList fListeners;
	
	private IAdviceChangedListener fAdviceChangedListener;

	public void decorate(Object element, IDecoration decoration) {
		// add the orange triangle to the icon if this method, 
		// class or aspect is advised
		if ((element instanceof IMethod || element instanceof SourceType)) {
			IJavaElement je = (IJavaElement) element;
			IJavaProject jp = je.getJavaProject();
			// only query the model if the element is in an AJ project
			if ((jp != null) && Utils.isAJProject(jp.getProject()) && 
			        !(je instanceof AspectJMemberElement)) {
				if (AJProjectModelFactory.getInstance().getModelForJavaElement(je)
				        .isAdvised(je)) {
					ensureAdviceListenerIsRegistered();
					// causes bundle activation
					AspectJUIPlugin.getDefault();
					decoration.addOverlay(AspectJImages.ADVICE_OVERLAY
							.getImageDescriptor(), IDecoration.TOP_LEFT);
				}
			}
		}
	}
	
	private void ensureAdviceListenerIsRegistered() {
		if (fAdviceChangedListener == null) {
			fAdviceChangedListener= new IAdviceChangedListener() {
				public void adviceChanged() {
					fireAdviceChanged();
				}
			};
			AJBuilder.addAdviceListener(fAdviceChangedListener);
		}
	}

	public void addListener(ILabelProviderListener listener) {
		if (fListeners == null) {
			fListeners= new ListenerList();
		}
		fListeners.add(listener);
	}
	

	private void fireAdviceChanged() {
		if (fListeners != null && !fListeners.isEmpty()) {
			LabelProviderChangedEvent event= new LabelProviderChangedEvent(this);
			Object[] listeners= fListeners.getListeners();
			for (int i= 0; i < listeners.length; i++) {
				((ILabelProviderListener) listeners[i]).labelProviderChanged(event);
			}
		}
	}
	
	public void dispose() {
		if (fAdviceChangedListener != null) {
			AJBuilder.removeAdviceListener(fAdviceChangedListener);
			fAdviceChangedListener= null;
		}
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		if (fListeners != null) {
			fListeners.remove(listener);
			if (fListeners.isEmpty() && fAdviceChangedListener != null) {
				AJBuilder.removeAdviceListener(fAdviceChangedListener);
				fAdviceChangedListener= null;
			}
		}
	}
}
