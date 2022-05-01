/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui.editor.quickfix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.ContributedProcessorDescriptor;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.IStatusLineProposal;
import org.eclipse.jdt.internal.ui.text.correction.ProblemLocation;
import org.eclipse.jdt.internal.ui.text.correction.proposals.MarkerResolutionProposal;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.eclipse.jdt.ui.text.java.correction.ChangeCorrectionProposal;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerHelpRegistry;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

/**
 * Copied from org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor
 * Any changes marked with // AspectJ Change
 */
public class JavaCorrectionProcessor implements org.eclipse.jface.text.quickassist.IQuickAssistProcessor {

	private static final String QUICKFIX_PROCESSOR_CONTRIBUTION_ID= "quickFixProcessors"; //$NON-NLS-1$
	private static final String QUICKASSIST_PROCESSOR_CONTRIBUTION_ID= "quickAssistProcessors"; //$NON-NLS-1$

	private static ContributedProcessorDescriptor[] fContributedAssistProcessors= null;
	private static ContributedProcessorDescriptor[] fContributedCorrectionProcessors= null;

	private static ContributedProcessorDescriptor[] getProcessorDescriptors(
		String contributionId,
		boolean testMarkerTypes
	) {
		IConfigurationElement[] elements = Platform
			.getExtensionRegistry()
			.getConfigurationElementsFor(JavaUI.ID_PLUGIN, contributionId);
		ArrayList<ContributedProcessorDescriptor> res = new ArrayList<>(elements.length);

		for (IConfigurationElement element : elements) {
			// AspectJ Change Begin
			// skip jdt's processor, as we have registered our own
			if (
				!element.getAttribute("class") //$NON-NLS-1$
					.equals("org.eclipse.jdt.internal.ui.text.correction.QuickFixProcessor") //$NON-NLS-1$
			)
			{
				ContributedProcessorDescriptor desc = new ContributedProcessorDescriptor(element, testMarkerTypes);
				IStatus status = desc.checkSyntax();
				if (status.isOK())
					res.add(desc);
				else
					JavaPlugin.log(status);
			}
			// AspectJ Change End
		}
		return res.toArray(new ContributedProcessorDescriptor[0]);
	}

	private static ContributedProcessorDescriptor[] getCorrectionProcessors() {
		if (fContributedCorrectionProcessors == null) {
			fContributedCorrectionProcessors= getProcessorDescriptors(QUICKFIX_PROCESSOR_CONTRIBUTION_ID, true);
		}
		return fContributedCorrectionProcessors;
	}

	private static ContributedProcessorDescriptor[] getAssistProcessors() {
		if (fContributedAssistProcessors == null) {
			fContributedAssistProcessors= getProcessorDescriptors(QUICKASSIST_PROCESSOR_CONTRIBUTION_ID, false);
		}
		return fContributedAssistProcessors;
	}

	public static boolean hasCorrections(ICompilationUnit cu, int problemId, String markerType) {
		ContributedProcessorDescriptor[] processors= getCorrectionProcessors();
		SafeHasCorrections collector= new SafeHasCorrections(cu, problemId);
    for (ContributedProcessorDescriptor processor : processors) {
      if (processor.canHandleMarkerType(markerType)) {
        collector.process(processor);
        if (collector.hasCorrections()) {
          return true;
        }
      }
    }
		return false;
	}

	public static boolean isQuickFixableType(Annotation annotation) {
		return (annotation instanceof IJavaAnnotation || annotation instanceof SimpleMarkerAnnotation) && !annotation.isMarkedDeleted();
	}


	public static boolean hasCorrections(Annotation annotation) {
		if (annotation instanceof IJavaAnnotation) {
			IJavaAnnotation javaAnnotation= (IJavaAnnotation) annotation;
			int problemId= javaAnnotation.getId();
			if (problemId != -1) {
				ICompilationUnit cu= javaAnnotation.getCompilationUnit();
				if (cu != null) {
					return hasCorrections(cu, problemId, javaAnnotation.getMarkerType());
				}
			}
		}
		if (annotation instanceof SimpleMarkerAnnotation) {
			return hasCorrections(((SimpleMarkerAnnotation) annotation).getMarker());
		}
		return false;
	}

	private static boolean hasCorrections(IMarker marker) {
		if (marker == null || !marker.exists())
			return false;

		IMarkerHelpRegistry registry= IDE.getMarkerHelpRegistry();
		return registry != null && registry.hasResolutions(marker);
	}

	public static boolean hasAssists(IInvocationContext context) {
		ContributedProcessorDescriptor[] processors= getAssistProcessors();
		SafeHasAssist collector= new SafeHasAssist(context);

    for (ContributedProcessorDescriptor processor : processors) {
      collector.process(processor);
      if (collector.hasAssists()) {
        return true;
      }
    }
		return false;
	}

	private final JavaCorrectionAssistant fAssistant;
	private String fErrorMessage;

	/*
	 * Constructor for JavaCorrectionProcessor.
	 */
	public JavaCorrectionProcessor(JavaCorrectionAssistant assistant) {
		fAssistant= assistant;
		fAssistant.addCompletionListener(new ICompletionListener() {

			public void assistSessionEnded(ContentAssistEvent event) {
				fAssistant.setStatusLineVisible(false);
			}

			public void assistSessionStarted(ContentAssistEvent event) {
				fAssistant.setStatusLineVisible(true);
			}

			public void selectionChanged(ICompletionProposal proposal, boolean smartToggle) {
				if (proposal instanceof IStatusLineProposal) {
					IStatusLineProposal statusLineProposal= (IStatusLineProposal)proposal;
					String message= statusLineProposal.getStatusMessage();
					if (message != null) {
						fAssistant.setStatusMessage(message);
					} else {
						fAssistant.setStatusMessage(""); //$NON-NLS-1$
					}
				} else {
					fAssistant.setStatusMessage(""); //$NON-NLS-1$
				}
			}
		});
	}

	/*
	 * @see IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext quickAssistContext) {
		ITextViewer viewer= quickAssistContext.getSourceViewer();
		int documentOffset= quickAssistContext.getOffset();

		IEditorPart part= fAssistant.getEditor();

		ICompilationUnit cu= JavaUI.getWorkingCopyManager().getWorkingCopy(part.getEditorInput());
		IAnnotationModel model= JavaUI.getDocumentProvider().getAnnotationModel(part.getEditorInput());

		int length= viewer != null ? viewer.getSelectedRange().y : 0;
		AssistContext context= new AssistContext(cu, documentOffset, length);

		Annotation[] annotations= fAssistant.getAnnotationsAtOffset();

		fErrorMessage= null;

		ICompletionProposal[] res= null;
		if (model != null && annotations != null) {
			List<ICompletionProposal> proposals= new ArrayList<>(10);
			IStatus status= collectProposals(context, model, annotations, true, !fAssistant.isUpdatedOffset(), proposals);
			res= proposals.toArray(new ICompletionProposal[0]);
			if (!status.isOK()) {
				fErrorMessage= status.getMessage();
				JavaPlugin.log(status);
			}
		}

		if (res == null || res.length == 0) {
			return new ICompletionProposal[] { new ChangeCorrectionProposal(CorrectionMessages.NoCorrectionProposal_description, new NullChange(""), 0, null) }; //$NON-NLS-1$
		}
		if (res.length > 1) {
			Arrays.sort(res, new CompletionProposalComparator());
		}
		return res;
	}

	public static IStatus collectProposals(
		IInvocationContext context,
		IAnnotationModel model,
		Annotation[] annotations,
		boolean addQuickFixes,
		boolean addQuickAssists,
		Collection<ICompletionProposal> proposals
	)
	{
		List<ProblemLocation> problems = new ArrayList<>();

		// collect problem locations and corrections from marker annotations
		for (Annotation curr : annotations) {
			if (curr instanceof IJavaAnnotation) {
				ProblemLocation problemLocation = getProblemLocation((IJavaAnnotation) curr, model);
				if (problemLocation != null)
					problems.add(problemLocation);
			}
			else if (addQuickFixes && curr instanceof SimpleMarkerAnnotation) {
				// don't collect if annotation is already a java annotation
				collectMarkerProposals((SimpleMarkerAnnotation) curr, proposals);
			}
		}
		MultiStatus resStatus = null;

		IProblemLocation[] problemLocations = problems.toArray(new IProblemLocation[0]);
		if (addQuickFixes) {
			IStatus status = collectCorrections(context, problemLocations, proposals);
			if (!status.isOK()) {
				resStatus = new MultiStatus(
					JavaUI.ID_PLUGIN,
					IStatus.ERROR,
					CorrectionMessages.JavaCorrectionProcessor_error_quickfix_message,
					null
				);
				resStatus.add(status);
			}
		}
		if (addQuickAssists) {
			IStatus status = collectAssists(context, problemLocations, proposals);
			if (!status.isOK()) {
				if (resStatus == null) {
					resStatus = new MultiStatus(
						JavaUI.ID_PLUGIN,
						IStatus.ERROR,
						CorrectionMessages.JavaCorrectionProcessor_error_quickassist_message,
						null
					);
				}
				resStatus.add(status);
			}
		}
		if (resStatus != null) {
			return resStatus;
		}
		return Status.OK_STATUS;
	}

	private static ProblemLocation getProblemLocation(IJavaAnnotation javaAnnotation, IAnnotationModel model) {
		int problemId= javaAnnotation.getId();
		if (problemId != -1) {
			Position pos= model.getPosition((Annotation) javaAnnotation);
			if (pos != null) {
				return new ProblemLocation(pos.getOffset(), pos.getLength(), javaAnnotation); // java problems all handled by the quick assist processors
			}
		}
		return null;
	}

	private static void collectMarkerProposals(
		SimpleMarkerAnnotation annotation,
		Collection<ICompletionProposal> proposals
	)
	{
		IMarker marker = annotation.getMarker();
		IMarkerResolution[] res = IDE.getMarkerHelpRegistry().getResolutions(marker);
		if (res.length > 0) {
			for (IMarkerResolution re : res)
				proposals.add(new MarkerResolutionProposal(re, marker));
		}
	}

	private static abstract class SafeCorrectionProcessorAccess implements ISafeRunnable {
		private MultiStatus fMulti= null;
		private ContributedProcessorDescriptor fDescriptor;

		public void process(ContributedProcessorDescriptor[] desc) {
      for (ContributedProcessorDescriptor contributedProcessorDescriptor : desc) {
        fDescriptor = contributedProcessorDescriptor;
        SafeRunner.run(this);
      }
		}

		public void process(ContributedProcessorDescriptor desc) {
			fDescriptor= desc;
			SafeRunner.run(this);
		}

		public void run() throws Exception {
			safeRun(fDescriptor);
		}

		protected abstract void safeRun(ContributedProcessorDescriptor processor) throws Exception;

		public void handleException(Throwable exception) {
			if (fMulti == null) {
				fMulti= new MultiStatus(JavaUI.ID_PLUGIN, IStatus.OK, CorrectionMessages.JavaCorrectionProcessor_error_status, null);
			}
			fMulti.merge(new Status(IStatus.ERROR, JavaUI.ID_PLUGIN, IStatus.ERROR, CorrectionMessages.JavaCorrectionProcessor_error_status, exception));
		}

		public IStatus getStatus() {
			if (fMulti == null) {
				return Status.OK_STATUS;
			}
			return fMulti;
		}

	}

	private static class SafeCorrectionCollector extends SafeCorrectionProcessorAccess {
		private final IInvocationContext fContext;
		private final Collection<ICompletionProposal> fProposals;
		private IProblemLocation[] fLocations;

		public SafeCorrectionCollector(IInvocationContext context, Collection<ICompletionProposal> proposals) {
			fContext= context;
			fProposals= proposals;
		}

		public void setProblemLocations(IProblemLocation[] locations) {
			fLocations= locations;
		}

		public void safeRun(ContributedProcessorDescriptor desc) throws Exception {
			IQuickFixProcessor curr= (IQuickFixProcessor) desc.getProcessor(fContext.getCompilationUnit(), IQuickAssistProcessor.class);
			if (curr != null) {
				IJavaCompletionProposal[] res= curr.getCorrections(fContext, fLocations);
				if (res != null)
					Collections.addAll(fProposals, res);
			}
		}
	}

	private static class SafeAssistCollector extends SafeCorrectionProcessorAccess {
		private final IInvocationContext fContext;
		private final IProblemLocation[] fLocations;
		private final Collection<ICompletionProposal> fProposals;

		public SafeAssistCollector(IInvocationContext context, IProblemLocation[] locations, Collection<ICompletionProposal> proposals) {
			fContext= context;
			fLocations= locations;
			fProposals= proposals;
		}

		public void safeRun(ContributedProcessorDescriptor desc) throws Exception {
			IQuickAssistProcessor curr= (IQuickAssistProcessor) desc.getProcessor(fContext.getCompilationUnit(), IQuickAssistProcessor.class);
			if (curr != null) {
				IJavaCompletionProposal[] res= curr.getAssists(fContext, fLocations);
				if (res != null)
					Collections.addAll(fProposals, res);
			}
		}
	}

	private static class SafeHasAssist extends SafeCorrectionProcessorAccess {
		private final IInvocationContext fContext;
		private boolean fHasAssists;

		public SafeHasAssist(IInvocationContext context) {
			fContext= context;
			fHasAssists= false;
		}

		public boolean hasAssists() {
			return fHasAssists;
		}

		public void safeRun(ContributedProcessorDescriptor desc) throws Exception {
			IQuickAssistProcessor processor= (IQuickAssistProcessor) desc.getProcessor(fContext.getCompilationUnit(), IQuickAssistProcessor.class);
			if (processor != null && processor.hasAssists(fContext)) {
				fHasAssists= true;
			}
		}
	}

	private static class SafeHasCorrections extends SafeCorrectionProcessorAccess {
		private final ICompilationUnit fCu;
		private final int fProblemId;
		private boolean fHasCorrections;

		public SafeHasCorrections(ICompilationUnit cu, int problemId) {
			fCu= cu;
			fProblemId= problemId;
			fHasCorrections= false;
		}

		public boolean hasCorrections() {
			return fHasCorrections;
		}

		public void safeRun(ContributedProcessorDescriptor desc) {
			IQuickFixProcessor processor= (IQuickFixProcessor) desc.getProcessor(fCu, IQuickFixProcessor.class);
			if (processor != null && processor.hasCorrections(fCu, fProblemId)) {
				fHasCorrections= true;
			}
		}
	}


	public static IStatus collectCorrections(
    IInvocationContext context,
                                           IProblemLocation[] locations,
                                           Collection<ICompletionProposal> proposals
  ) {
		ContributedProcessorDescriptor[] processors= getCorrectionProcessors();
		SafeCorrectionCollector collector= new SafeCorrectionCollector(context, proposals);
    for (ContributedProcessorDescriptor curr : processors) {
      IProblemLocation[] handled = getHandledProblems(locations, curr);
      if (handled != null) {
        collector.setProblemLocations(handled);
        collector.process(curr);
      }
    }
		return collector.getStatus();
	}

	private static IProblemLocation[] getHandledProblems(IProblemLocation[] locations, ContributedProcessorDescriptor processor) {
		// implementation tries to avoid creating a new array
		boolean allHandled= true;
		ArrayList res= null;
		for (int i= 0; i < locations.length; i++) {
			IProblemLocation curr= locations[i];
			if (processor.canHandleMarkerType(curr.getMarkerType())) {
				if (!allHandled) { // first handled problem
					if (res == null) {
						res= new ArrayList(locations.length - i);
					}
					res.add(curr);
				}
			} else if (allHandled) {
				if (i > 0) { // first non handled problem
					res= new ArrayList(locations.length - i);
          res.addAll(Arrays.asList(locations).subList(0, i));
				}
				allHandled= false;
			}
		}
		if (allHandled) {
			return locations;
		}
		if (res == null) {
			return null;
		}
		return (IProblemLocation[]) res.toArray(new IProblemLocation[0]);
	}

	public static IStatus collectAssists(IInvocationContext context, IProblemLocation[] locations, Collection proposals) {
		ContributedProcessorDescriptor[] processors= getAssistProcessors();
		SafeAssistCollector collector= new SafeAssistCollector(context, locations, proposals);
		collector.process(processors);

		return collector.getStatus();
	}

	/*
	 * @see IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return fErrorMessage;
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickAssistProcessor#canFix(org.eclipse.jface.text.source.Annotation)
	 * @since 3.2
	 */
	public boolean canFix(Annotation annotation) {
		return hasCorrections(annotation);
	}

	/*
	 * @see org.eclipse.jface.text.quickassist.IQuickAssistProcessor#canAssist(org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext)
	 * @since 3.2
	 */
	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
		if (invocationContext instanceof IInvocationContext)
			return hasAssists((IInvocationContext)invocationContext);
		return false;
	}

}
