/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sian January - initial version
 *     ...
 **********************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.debug.internal.ui.InstructionPointerAnnotation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
	
/**
 * Listens to InstructionPointerAnnotations being added and removed from
 * one IAnnotationModel and duplicates the events in another IAnnotationModel.
 */
public class InstructionPointerAnnotationListener implements IAnnotationModelListener, IAnnotationModelListenerExtension {

	private IAnnotationModel annotationModel;
	
	
	public InstructionPointerAnnotationListener(IAnnotationModel delegateModel) {
		annotationModel = delegateModel;
	}

	public void modelChanged(IAnnotationModel model) {
		// Don't need to fill in this method because we implement IAnnotationModelListenerExtension
	}

	public void modelChanged(AnnotationModelEvent event) {
		Annotation[] added = event.getAddedAnnotations();
		for (int i = 0; i < added.length; i++) {
			if(added[i] instanceof InstructionPointerAnnotation) {
				annotationModel.addAnnotation(added[i], event.getAnnotationModel().getPosition(added[i]));
			}
		}
		Annotation[] removed = event.getRemovedAnnotations();
		for (int i = 0; i < removed.length; i++) {
			if(removed[i] instanceof InstructionPointerAnnotation) {
				annotationModel.removeAnnotation(removed[i]);
			}
		}
	}


} 