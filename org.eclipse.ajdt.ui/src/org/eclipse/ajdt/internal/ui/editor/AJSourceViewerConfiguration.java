/*
 * Created on 09-Apr-2004
 *
 */
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.ajdt.internal.ui.editor.contentassist.AJCompletionProcessor;
import org.eclipse.jdt.internal.ui.text.CompoundContentAssistProcessor;
import org.eclipse.jdt.internal.ui.text.IJavaPartitions;
import org.eclipse.jdt.internal.ui.text.spelling.WordCompletionProcessor;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;

public class AJSourceViewerConfiguration extends JavaSourceViewerConfiguration {

	AspectJTextTools ajtt = null;
	
	public AJSourceViewerConfiguration(AspectJTextTools textTools, AspectJEditor editor, String string) {
		super(textTools.getColorManager(), textTools.getPreferenceStore(), editor, IJavaPartitions.JAVA_PARTITIONING);
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
			cAssi.setContentAssistProcessor(compoundProcessor, IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
		}
		return assistant;

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

}
