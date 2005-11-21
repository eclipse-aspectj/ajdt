/*
 * Created on 09-Apr-2004
 *
 */
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.ajdt.internal.ui.editor.contentassist.AJCompletionProcessor;
import org.eclipse.ajdt.internal.ui.editor.outline.AJOutlineInformationControl;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.CompoundContentAssistProcessor;
import org.eclipse.jdt.internal.ui.text.JavaElementProvider;
import org.eclipse.jdt.internal.ui.text.spelling.WordCompletionProcessor;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;

public class AJSourceViewerConfiguration extends JavaSourceViewerConfiguration {

	AspectJTextTools ajtt = null;
	
	private IPreferenceStore prefs;
	
	public AJSourceViewerConfiguration(AspectJTextTools textTools, AspectJEditor editor) {
		super(textTools.getColorManager(), textTools.getPreferenceStore(), editor, EclipseEditorIsolation.JAVA_PARTITIONING);
		ajtt = textTools;
	}
	
	protected RuleBasedScanner getCodeScanner() {
		return ajtt.getCodeScanner();
	}
	
	/*
	 * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		IContentAssistant assistant = super.getContentAssistant(sourceViewer);
		if ((assistant != null) && (assistant instanceof ContentAssistant)) {
			ContentAssistant cAssi = (ContentAssistant)assistant;
			IContentAssistProcessor ajProcessor= new AJCompletionProcessor(getEditor());
			cAssi.setContentAssistProcessor(ajProcessor, IDocument.DEFAULT_CONTENT_TYPE);
			// Register the java processor for single line comments to get the NLS template working inside comments
			IContentAssistProcessor wordProcessor= new WordCompletionProcessor();
			CompoundContentAssistProcessor compoundProcessor= new CompoundContentAssistProcessor();
			compoundProcessor.add(ajProcessor);
			compoundProcessor.add(wordProcessor);
			cAssi.setContentAssistProcessor(compoundProcessor, EclipseEditorIsolation.JAVA_SINGLE_LINE_COMMENT);
			if (prefs == null) {
				prefs = createPreferenceStore();
			}
			configureAJProcessor(prefs, (AJCompletionProcessor)ajProcessor);
		}
		return assistant;

	}
	
	
	// Copied from super as no access to super's preference store
	private IPreferenceStore createPreferenceStore() {
		IPreferenceStore generalTextStore= EditorsUI.getPreferenceStore();
		return new ChainedPreferenceStore(new IPreferenceStore[] { ajtt.getPreferenceStore(), generalTextStore});
	}

	
	// Fix for bug 111971 - our completion processor is not configured properly by ContentAssistPreferences
	// so copied ContentAssistPreferences.configureJavaProcessor to here.
	private void configureAJProcessor(IPreferenceStore store, AJCompletionProcessor jcp) {
		
		String triggers= store.getString(PreferenceConstants.CODEASSIST_AUTOACTIVATION_TRIGGERS_JAVA);
		if (triggers != null)
			jcp.setCompletionProposalAutoActivationCharacters(triggers.toCharArray());

		boolean enabled= store.getBoolean(PreferenceConstants.CODEASSIST_SHOW_VISIBLE_PROPOSALS);
		jcp.restrictProposalsToVisibility(enabled);

		enabled= store.getBoolean(PreferenceConstants.CODEASSIST_CASE_SENSITIVITY);
		jcp.restrictProposalsToMatchingCases(enabled);

		enabled= store.getBoolean(PreferenceConstants.CODEASSIST_ORDER_PROPOSALS);
		jcp.orderProposalsAlphabetically(enabled);
	}
	
	/**
	 * Determines whether the preference change encoded by the given event
	 * changes the behavior of one of its contained components
	 * 
	 * @param event the event to be investigated
	 * @return <code>true</code> if event causes a behavioral change
	 * @since 3.0
	 */
	public boolean affectsTextPresentation(PropertyChangeEvent event) {
		return  ajtt.getAspectjCodeScanner().affectsBehavior(event) ||
		  super.affectsTextPresentation(event);
	}
	
	
	
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		IContentFormatter formatter = super.getContentFormatter(sourceViewer);
		if (formatter instanceof MultiPassContentFormatter)
			((MultiPassContentFormatter)formatter).setMasterStrategy(new AJFormattingStrategy());
		return formatter;
	}

	// creates an AJOutlineInformationControl instead of a
	// JavaOutlineInformationControl
	// not needed if/when eclipse bug 79489 is fixed
	/**
	 * Returns the outline presenter control creator. The creator is a factory creating outline
	 * presenter controls for the given source viewer. This implementation always returns a creator
	 * for <code>JavaOutlineInformationControl</code> instances.
	 * @param commandId the ID of the command that opens this control
	 * 
	 * @return an information control creator
	 * @since 2.1
	 */
	private IInformationControlCreator getOutlinePresenterControlCreator(final String commandId) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				int shellStyle= SWT.RESIZE;
				int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;
				return new AJOutlineInformationControl(parent, shellStyle, treeStyle, commandId);
			}
		};
	}

	// copied from superclass so that we can call our own version of
	// getOutlinePresenterControlCreator()
	// not needed if/when eclipse bug 79489 is fixed
	/**
	 * Returns the outline presenter which will determine and shown
	 * information requested for the current cursor position.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param doCodeResolve a boolean which specifies whether code resolve should be used to compute the Java element 
	 * @return an information presenter
	 * @since 2.1
	 */
	public IInformationPresenter getOutlinePresenter(ISourceViewer sourceViewer, boolean doCodeResolve) {
		InformationPresenter presenter;
		if (doCodeResolve)
			presenter= new InformationPresenter(getOutlinePresenterControlCreator(IJavaEditorActionDefinitionIds.OPEN_STRUCTURE));
		else
			presenter= new InformationPresenter(getOutlinePresenterControlCreator(IJavaEditorActionDefinitionIds.SHOW_OUTLINE));
		presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		presenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);
		IInformationProvider provider= new JavaElementProvider(getEditor(), doCodeResolve);
		presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
		presenter.setInformationProvider(provider, EclipseEditorIsolation.JAVA_DOC);
		presenter.setInformationProvider(provider, EclipseEditorIsolation.JAVA_MULTI_LINE_COMMENT);
		presenter.setInformationProvider(provider, EclipseEditorIsolation.JAVA_SINGLE_LINE_COMMENT);
		presenter.setInformationProvider(provider, EclipseEditorIsolation.JAVA_STRING);
		presenter.setInformationProvider(provider, EclipseEditorIsolation.JAVA_CHARACTER);
		presenter.setSizeConstraints(20, 20, true, false);
		// bug 80239 - the following line was missing
		presenter.setRestoreInformationControlBounds(getSettings("outline_presenter_bounds"), true, true); //$NON-NLS-1$
		return presenter;
	}
	
	
	// copied from superclass for fix to bug 80239
	/**
	 * Returns the settings for the given section.
	 *
	 * @param sectionName the section name
	 * @return the settings
	 * @since 3.0
	 */
	private IDialogSettings getSettings(String sectionName) {
		IDialogSettings settings= JavaPlugin.getDefault().getDialogSettings().getSection(sectionName);
		if (settings == null)
			settings= JavaPlugin.getDefault().getDialogSettings().addNewSection(sectionName);

		return settings;
	}

}
