/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January - copied for use in AJDT
 *     ...
 **********************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitAnnotationModelEvent;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jdt.internal.ui.javaeditor.JavaMarkerAnnotation;
import org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor;
import org.eclipse.jdt.internal.ui.text.java.IProblemRequestorExtension;
import org.eclipse.jdt.internal.ui.text.spelling.JavaSpellingReconcileStrategy;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.jface.text.source.ImageUtilities;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;

/**
 * Annotation model dealing with java marker annotations and temporary problems.
 * Also acts as problem requester for its compilation unit. Initially inactive. Must explicitly be
 * activated.
 * 
 * Sian - Mostly copied from CompilationUnitDocumentProvider
 */
public class AJCompilationUnitAnnotationModel extends ResourceMarkerAnnotationModel implements IProblemRequestor, IProblemRequestorExtension {
		
	private static class ProblemRequestorState {
		boolean fInsideReportingSequence= false;
		List fReportedProblems;
	}
	
	private ThreadLocal fProblemRequestorState= new ThreadLocal();
	private int fStateCount= 0;
	
	private ICompilationUnit fCompilationUnit;
	private List fGeneratedAnnotations;
	private IProgressMonitor fProgressMonitor;
	private boolean fIsActive= false;
	private boolean fIsHandlingTemporaryProblems;	
	
	private ReverseMap fReverseMap= new ReverseMap();
	private List fPreviouslyOverlaid= null; 
	private List fCurrentlyOverlaid= new ArrayList();

	
	public AJCompilationUnitAnnotationModel(IResource resource) {
		super(resource);
	}
	
	public void setCompilationUnit(ICompilationUnit unit)  {
		fCompilationUnit= unit;
	}
	
	protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
		String markerType= MarkerUtilities.getMarkerType(marker);
		if (markerType != null && markerType.startsWith(JavaMarkerAnnotation.JAVA_MARKER_TYPE_PREFIX))
			return new JavaMarkerAnnotation(marker);
		return super.createMarkerAnnotation(marker);
	}
	
	/*
	 * @see org.eclipse.jface.text.source.AnnotationModel#createAnnotationModelEvent()
	 */
	protected AnnotationModelEvent createAnnotationModelEvent() {
		return new CompilationUnitAnnotationModelEvent(this, getResource());
	}
	
	protected Position createPositionFromProblem(IProblem problem) {
		int start= problem.getSourceStart();
		if (start < 0)
			return null;
			
		int length= problem.getSourceEnd() - problem.getSourceStart() + 1;
		if (length < 0)
			return null;
			
		return new Position(start, length);
	}
	
	/*
	 * @see IProblemRequestor#beginReporting()
	 */
	public void beginReporting() {
		ProblemRequestorState state= (ProblemRequestorState) fProblemRequestorState.get();
		if (state == null)
			internalBeginReporting(false);				
	}
	
	/*
	 * @see org.eclipse.jdt.internal.ui.text.java.IProblemRequestorExtension#beginReportingSequence()
	 */
	public void beginReportingSequence() {
		ProblemRequestorState state= (ProblemRequestorState) fProblemRequestorState.get();
		if (state == null)
			internalBeginReporting(true);
	}
	
	/**
	 * Sets up the infrastructure necessary for problem reporting.
	 * 
	 * @param insideReportingSequence <code>true</code> if this method
	 *            call is issued from inside a reporting sequence
	 */
	private void internalBeginReporting(boolean insideReportingSequence) {
		if (fCompilationUnit != null && fCompilationUnit.getJavaProject().isOnClasspath(fCompilationUnit)) {
			ProblemRequestorState state= new ProblemRequestorState();
			state.fInsideReportingSequence= insideReportingSequence;
			state.fReportedProblems= new ArrayList();
			synchronized (getLockObject()) {
				fProblemRequestorState.set(state);
				++fStateCount;
			}
		}
	}

	/*
	 * @see IProblemRequestor#acceptProblem(IProblem)
	 */
	public void acceptProblem(IProblem problem) {
		if (fIsHandlingTemporaryProblems) {
			ProblemRequestorState state= (ProblemRequestorState) fProblemRequestorState.get();
			if (state != null)
				state.fReportedProblems.add(problem);
		}
	}
	
	/*
	 * @see IProblemRequestor#endReporting()
	 */
	public void endReporting() {
		ProblemRequestorState state= (ProblemRequestorState) fProblemRequestorState.get();
		if (state != null && !state.fInsideReportingSequence)
			internalEndReporting(state);
	}
	
	/*
	 * @see org.eclipse.jdt.internal.ui.text.java.IProblemRequestorExtension#endReportingSequence()
	 */
	public void endReportingSequence() {
		ProblemRequestorState state= (ProblemRequestorState) fProblemRequestorState.get();
		if (state != null && state.fInsideReportingSequence)
			internalEndReporting(state);
	}
	
	private void internalEndReporting(ProblemRequestorState state) {
		int stateCount= 0;
		synchronized(getLockObject()) {
			-- fStateCount;
			stateCount= fStateCount;
			fProblemRequestorState.set(null);
		}
		
		if (stateCount == 0 && fIsHandlingTemporaryProblems)
			reportProblems(state.fReportedProblems);
	}
	
	/**
	 * Signals the end of problem reporting.
	 */
	private void reportProblems(List reportedProblems) {
		if (fProgressMonitor != null && fProgressMonitor.isCanceled())
			return;
			
		boolean temporaryProblemsChanged= false;
		
		synchronized (getLockObject()) {
			
			boolean isCanceled= false;

			fPreviouslyOverlaid= fCurrentlyOverlaid;
			fCurrentlyOverlaid= new ArrayList();

			if (fGeneratedAnnotations.size() > 0) {
				temporaryProblemsChanged= true;	
				removeAnnotations(fGeneratedAnnotations, false, true);
				fGeneratedAnnotations.clear();
			}
			
			if (reportedProblems != null && reportedProblems.size() > 0) {
										
				Iterator e= reportedProblems.iterator();
				while (e.hasNext()) {
					
					if (fProgressMonitor != null && fProgressMonitor.isCanceled()) {
						isCanceled= true;
						break;
					}
						
					IProblem problem= (IProblem) e.next();
					Position position= createPositionFromProblem(problem);
					if (position != null) {
						
						try {
							ProblemAnnotation annotation= new ProblemAnnotation(problem, fCompilationUnit);
							overlayMarkers(position, annotation);								
							addAnnotation(annotation, position, false);
							fGeneratedAnnotations.add(annotation);
						
							temporaryProblemsChanged= true;
						} catch (BadLocationException x) {
							// ignore invalid position
						}
					}
				}
			}
			
			removeMarkerOverlays(isCanceled);
			fPreviouslyOverlaid= null;
		}
			
		if (temporaryProblemsChanged)
			fireModelChanged();
	}

	private void removeMarkerOverlays(boolean isCanceled) {
		if (isCanceled) {
			fCurrentlyOverlaid.addAll(fPreviouslyOverlaid);
		} else if (fPreviouslyOverlaid != null) {
			Iterator e= fPreviouslyOverlaid.iterator();
			while (e.hasNext()) {
				JavaMarkerAnnotation annotation= (JavaMarkerAnnotation) e.next();
				annotation.setOverlay(null);
			}
		}			
	}
	
	/**
	 * Overlays value with problem annotation.
	 * @param problemAnnotation
	 */
	private void setOverlay(Object value, ProblemAnnotation problemAnnotation) {
		if (value instanceof  JavaMarkerAnnotation) {
			JavaMarkerAnnotation annotation= (JavaMarkerAnnotation) value;
			if (annotation.isProblem()) {
				annotation.setOverlay(problemAnnotation);
				fPreviouslyOverlaid.remove(annotation);
				fCurrentlyOverlaid.add(annotation);
			}
		} else {
		}
	}
	
	private void  overlayMarkers(Position position, ProblemAnnotation problemAnnotation) {
		Object value= getAnnotations(position);
		if (value instanceof List) {
			List list= (List) value;
			for (Iterator e = list.iterator(); e.hasNext();)
				setOverlay(e.next(), problemAnnotation);
		} else {
			setOverlay(value, problemAnnotation);
		}
	}
	
	/**
	 * Tells this annotation model to collect temporary problems from now on.
	 */
	private void startCollectingProblems() {
		fGeneratedAnnotations= new ArrayList();  
	}
	
	/**
	 * Tells this annotation model to no longer collect temporary problems.
	 */
	private void stopCollectingProblems() {
		if (fGeneratedAnnotations != null)
			removeAnnotations(fGeneratedAnnotations, true, true);
		fGeneratedAnnotations= null;
	}
	
	/*
	 * @see IProblemRequestor#isActive()
	 */
	public boolean isActive() {
		return fIsActive;
	}
	
	/*
	 * @see IProblemRequestorExtension#setProgressMonitor(IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor= monitor;
	}
	
	/*
	 * @see IProblemRequestorExtension#setIsActive(boolean)
	 */
	public void setIsActive(boolean isActive) {
		fIsActive= isActive;
		// Sian: added this call so that we can keep the same AspectJEditor in 3.0 and 3.1
		setIsHandlingTemporaryProblems(isActive);
	}
	
	/*
	 * @see IProblemRequestorExtension#setIsHandlingTemporaryProblems(boolean)
	 * @since 3.1
	 */
	public void setIsHandlingTemporaryProblems(boolean enable) {
		if (fIsHandlingTemporaryProblems != enable) {
			fIsHandlingTemporaryProblems= enable;
			if (fIsHandlingTemporaryProblems)
				startCollectingProblems();
			else
				stopCollectingProblems();
		}
		
	}
	
	private Object getAnnotations(Position position) {
		synchronized (getLockObject()) {
			return fReverseMap.get(position);
		}
	}
				
	/*
	 * @see AnnotationModel#addAnnotation(Annotation, Position, boolean)
	 */
	protected void addAnnotation(Annotation annotation, Position position, boolean fireModelChanged) throws BadLocationException {				
		super.addAnnotation(annotation, position, fireModelChanged);

		synchronized (getLockObject()) {
			Object cached= fReverseMap.get(position);
			if (cached == null)
				fReverseMap.put(position, annotation);
			else if (cached instanceof List) {
				List list= (List) cached;
				list.add(annotation);
			} else if (cached instanceof Annotation) {
				List list= new ArrayList(2);
				list.add(cached);
				list.add(annotation);
				fReverseMap.put(position, list);
			}
		}
	}
	
	/*
	 * @see AnnotationModel#removeAllAnnotations(boolean)
	 */
	protected void removeAllAnnotations(boolean fireModelChanged) {
		super.removeAllAnnotations(fireModelChanged);
		synchronized (getLockObject()) {
			fReverseMap.clear();
		}
	}
	
	/*
	 * @see AnnotationModel#removeAnnotation(Annotation, boolean)
	 */
	protected void removeAnnotation(Annotation annotation, boolean fireModelChanged) {
		Position position= getPosition(annotation);
		synchronized (getLockObject()) {
			Object cached= fReverseMap.get(position);
			if (cached instanceof List) {
				List list= (List) cached;
				list.remove(annotation);
				if (list.size() == 1) {
					fReverseMap.put(position, list.get(0));
					list.clear();
				}
			} else if (cached instanceof Annotation) {
				fReverseMap.remove(position);
			}
		}
		super.removeAnnotation(annotation, fireModelChanged);
	}



	protected static class GlobalAnnotationModelListener implements IAnnotationModelListener, IAnnotationModelListenerExtension {
		
		private ListenerList fListenerList;
		
		public GlobalAnnotationModelListener() {
			fListenerList= new ListenerList();
		}
		
		/**
		 * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
		 */
		public void modelChanged(IAnnotationModel model) {
			Object[] listeners= fListenerList.getListeners();
			for (int i= 0; i < listeners.length; i++) {
				((IAnnotationModelListener) listeners[i]).modelChanged(model);
			}
		}
	
		/**
		 * @see IAnnotationModelListenerExtension#modelChanged(AnnotationModelEvent)
		 */
		public void modelChanged(AnnotationModelEvent event) {
			Object[] listeners= fListenerList.getListeners();
			for (int i= 0; i < listeners.length; i++) {
				Object curr= listeners[i];
				if (curr instanceof IAnnotationModelListenerExtension) {
					((IAnnotationModelListenerExtension) curr).modelChanged(event);
				}
			}
		}
		
		public void addListener(IAnnotationModelListener listener) {
			fListenerList.add(listener);
		}
		
		public void removeListener(IAnnotationModelListener listener) {
			fListenerList.remove(listener);
		}			
	}		



	/**
	 * Annotation representing an <code>IProblem</code>.
	 */
	static public class ProblemAnnotation extends Annotation implements IJavaAnnotation, IAnnotationPresentation {
	
		public static final String SPELLING_ANNOTATION_TYPE= "org.eclipse.ui.workbench.texteditor.spelling"; //$NON-NLS-1$
		
		//XXX: To be fully correct these constants should be non-static
		/** 
		 * The layer in which task problem annotations are located.
		 */
		private static final int TASK_LAYER;
		/** 
		 * The layer in which info problem annotations are located.
		 */
		private static final int INFO_LAYER;
		/** 
		 * The layer in which warning problem annotations representing are located.
		 */
		private static final int WARNING_LAYER;
		/** 
		 * The layer in which error problem annotations representing are located.
		 */
		private static final int ERROR_LAYER;
		
		static {
			AnnotationPreferenceLookup lookup= EditorsUI.getAnnotationPreferenceLookup();
			TASK_LAYER= computeLayer("org.eclipse.ui.workbench.texteditor.task", lookup); //$NON-NLS-1$
			INFO_LAYER= computeLayer("org.eclipse.jdt.ui.info", lookup); //$NON-NLS-1$
			WARNING_LAYER= computeLayer("org.eclipse.jdt.ui.warning", lookup); //$NON-NLS-1$
			ERROR_LAYER= computeLayer("org.eclipse.jdt.ui.error", lookup); //$NON-NLS-1$
		}
		
		private static int computeLayer(String annotationType, AnnotationPreferenceLookup lookup) {
			Annotation annotation= new Annotation(annotationType, false, null);
			AnnotationPreference preference= lookup.getAnnotationPreference(annotation);
			if (preference != null)
				return preference.getPresentationLayer() + 1;
			else
				return IAnnotationAccessExtension.DEFAULT_LAYER + 1;
		}
		
		private static Image fgQuickFixImage;
		private static Image fgQuickFixErrorImage;
		private static boolean fgQuickFixImagesInitialized= false;
		
		private ICompilationUnit fCompilationUnit;
		private List fOverlaids;
		private IProblem fProblem;
		private Image fImage;
		private boolean fQuickFixImagesInitialized= false;
		private int fLayer= IAnnotationAccessExtension.DEFAULT_LAYER;
		
		
		public ProblemAnnotation(IProblem problem, ICompilationUnit cu) {
			
			fProblem= problem;
			fCompilationUnit= cu;
			
			if (JavaSpellingReconcileStrategy.SPELLING_PROBLEM_ID == fProblem.getID()) {
				setType(SPELLING_ANNOTATION_TYPE);
				fLayer= WARNING_LAYER;
			} else if (IProblem.Task == fProblem.getID()) {
				setType(JavaMarkerAnnotation.TASK_ANNOTATION_TYPE);
				fLayer= TASK_LAYER;
			} else if (fProblem.isWarning()) {
				setType(JavaMarkerAnnotation.WARNING_ANNOTATION_TYPE);
				fLayer= WARNING_LAYER;
			} else if (fProblem.isError()) {
				setType(JavaMarkerAnnotation.ERROR_ANNOTATION_TYPE);
				fLayer= ERROR_LAYER;
			} else {
				setType(JavaMarkerAnnotation.INFO_ANNOTATION_TYPE);
				fLayer= INFO_LAYER;
			}
		}
		
		/*
		 * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
		 */
		public int getLayer() {
			return fLayer;
		}
		
		private void initializeImages() {
			// http://bugs.eclipse.org/bugs/show_bug.cgi?id=18936
			if (!fQuickFixImagesInitialized) {
				if (isProblem() && indicateQuixFixableProblems() && JavaCorrectionProcessor.hasCorrections(this)) { // no light bulb for tasks
					if (!fgQuickFixImagesInitialized) {
						fgQuickFixImage= JavaPluginImages.get(JavaPluginImages.IMG_OBJS_FIXABLE_PROBLEM);
						fgQuickFixErrorImage= JavaPluginImages.get(JavaPluginImages.IMG_OBJS_FIXABLE_ERROR);
						fgQuickFixImagesInitialized= true;
					}
					if (JavaMarkerAnnotation.ERROR_ANNOTATION_TYPE.equals(getType()))
						fImage= fgQuickFixErrorImage;
					else
						fImage= fgQuickFixImage;
				}
				fQuickFixImagesInitialized= true;
			}
		}
	
		private boolean indicateQuixFixableProblems() {
			return PreferenceConstants.getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_CORRECTION_INDICATION);
		}
					
		/*
		 * @see Annotation#paint
		 */
		public void paint(GC gc, Canvas canvas, Rectangle r) {
			initializeImages();
			if (fImage != null)
				ImageUtilities.drawImage(fImage, gc, canvas, r, SWT.CENTER, SWT.TOP);
		}
		
		/*
		 * @see IJavaAnnotation#getImage(Display)
		 */
		public Image getImage(Display display) {
			initializeImages();
			return fImage;
		}
		
		/*
		 * @see IJavaAnnotation#getMessage()
		 */
		public String getText() {
			return fProblem.getMessage();
		}
		
		/*
		 * @see IJavaAnnotation#getArguments()
		 */
		public String[] getArguments() {
			return isProblem() ? fProblem.getArguments() : null;
		}
	
		/*
		 * @see IJavaAnnotation#getId()
		 */
		public int getId() {
			return fProblem.getID();
		}
	
		/*
		 * @see IJavaAnnotation#isProblem()
		 */
		public boolean isProblem() {
			String type= getType();
			return  JavaMarkerAnnotation.WARNING_ANNOTATION_TYPE.equals(type)  || 
						JavaMarkerAnnotation.ERROR_ANNOTATION_TYPE.equals(type) ||
						SPELLING_ANNOTATION_TYPE.equals(type);
		}
		
		/*
		 * @see IJavaAnnotation#hasOverlay()
		 */
		public boolean hasOverlay() {
			return false;
		}
		
		/*
		 * @see org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation#getOverlay()
		 */
		public IJavaAnnotation getOverlay() {
			return null;
		}
		
		/*
		 * @see IJavaAnnotation#addOverlaid(IJavaAnnotation)
		 */
		public void addOverlaid(IJavaAnnotation annotation) {
			if (fOverlaids == null)
				fOverlaids= new ArrayList(1);
			fOverlaids.add(annotation);
		}
	
		/*
		 * @see IJavaAnnotation#removeOverlaid(IJavaAnnotation)
		 */
		public void removeOverlaid(IJavaAnnotation annotation) {
			if (fOverlaids != null) {
				fOverlaids.remove(annotation);
				if (fOverlaids.size() == 0)
					fOverlaids= null;
			}
		}
		
		/*
		 * @see IJavaAnnotation#getOverlaidIterator()
		 */
		public Iterator getOverlaidIterator() {
			if (fOverlaids != null)
				return fOverlaids.iterator();
			return null;
		}
				
		/*
		 * @see org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation#getCompilationUnit()
		 */
		public ICompilationUnit getCompilationUnit() {
			return fCompilationUnit;
		}
	}
	
	/**
	 * Internal structure for mapping positions to some value. 
	 * The reason for this specific structure is that positions can
	 * change over time. Thus a lookup is based on value and not
	 * on hash value.
	 */
	protected static class ReverseMap {
		
		static class Entry {
			Position fPosition;
			Object fValue;
		}
		
		private List fList= new ArrayList(2);
		private int fAnchor= 0;
		
		public ReverseMap() {
		}
		
		public Object get(Position position) {
			
			Entry entry;
			
			// behind anchor
			int length= fList.size();
			for (int i= fAnchor; i < length; i++) {
				entry= (Entry) fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor= i;
					return entry.fValue;
				}
			}
			
			// before anchor
			for (int i= 0; i < fAnchor; i++) {
				entry= (Entry) fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor= i;
					return entry.fValue;
				}
			}
			
			return null;
		}
		
		private int getIndex(Position position) {
			Entry entry;
			int length= fList.size();
			for (int i= 0; i < length; i++) {
				entry= (Entry) fList.get(i);
				if (entry.fPosition.equals(position))
					return i;
			}
			return -1;
		}
		
		public void put(Position position,  Object value) {
			int index= getIndex(position);
			if (index == -1) {
				Entry entry= new Entry();
				entry.fPosition= position;
				entry.fValue= value;
				fList.add(entry);
			} else {
				Entry entry= (Entry) fList.get(index);
				entry.fValue= value;
			}
		}
		
		public void remove(Position position) {
			int index= getIndex(position);
			if (index > -1)
				fList.remove(index);
		}
		
		public void clear() {
			fList.clear();
		}
	}

}
