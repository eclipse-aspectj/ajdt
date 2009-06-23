package org.eclipse.ajdt.internal.ui.text;

import java.util.ArrayList;

import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.codeconversion.ConversionOptions;
import org.eclipse.ajdt.core.codeconversion.ITDAwareNameEnvironment;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.text.ITDAwareSelectionRequestor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.SelectionEngine;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlink;
import org.eclipse.jdt.internal.ui.text.JavaWordFinder;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Andrew Eisenberg
 * @created Jun 6, 2009
 *
 * This class is now unused since ITDHyperlinks are found via code select now (through the ITDAwareness aspect)
 */
public class ITDHyperlinkDetector extends AbstractHyperlinkDetector {

    public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
            IRegion region, boolean canShowMultipleHyperlinks) {
        
        ITextEditor textEditor= (ITextEditor)getAdapter(ITextEditor.class);
        if (region == null || !(textEditor instanceof JavaEditor))
            return null;

        IAction openAction= textEditor.getAction("OpenEditor"); //$NON-NLS-1$
        if (!(openAction instanceof SelectionDispatchAction))
            return null;

        int offset= region.getOffset();

        IJavaElement input= EditorUtility.getEditorInputJavaElement(textEditor, false);
        if (input == null) {
            return null;
        }
        IOpenable openable = input.getOpenable();
        if (! (openable instanceof ICompilationUnit)) {
            return null;
        }
        ICompilationUnit unit = (ICompilationUnit) openable;

        try {
            IDocument document= textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
            IRegion wordRegion= JavaWordFinder.findWord(document, offset);
            if (wordRegion == null || wordRegion.getLength() == 0)
                return null;
            
            
            IJavaElement[] elements = findJavaElement(unit, wordRegion);
            if (elements.length == 0) {
                return null;
            }
            IHyperlink[] result= new IHyperlink[elements.length];
            for (int i= 0; i < elements.length; i++) {
                result[i]= new JavaElementHyperlink(wordRegion, (SelectionDispatchAction) openAction, elements[i], elements.length > 1);
            }
            return result;

        } catch (JavaModelException e) {
        }
        return null;
    }

    private IJavaElement[] findJavaElement(ICompilationUnit unit,
            IRegion wordRegion) throws JavaModelException {
        JavaProject javaProject = (JavaProject) unit.getJavaProject();
        SearchableEnvironment environment = new ITDAwareNameEnvironment(javaProject, unit.getOwner(), null);

        ITDAwareSelectionRequestor requestor = new ITDAwareSelectionRequestor(AJProjectModelFactory.getInstance().getModelForJavaElement(javaProject), unit);
        SelectionEngine engine = new SelectionEngine(environment, requestor, javaProject.getOptions(true), unit.getOwner()); /* AJDT 1.7 */
        
        final AspectsConvertingParser converter = new AspectsConvertingParser(((CompilationUnit) unit).getContents());
        converter.setUnit(unit);
        ArrayList replacements = converter.convert(ConversionOptions.CODE_COMPLETION);
        
        org.eclipse.jdt.internal.compiler.env.ICompilationUnit wrappedUnit = 
                new CompilationUnit((PackageFragment) unit.getParent(), unit.getElementName(), unit.getOwner()){
            public char[] getContents() {
                return converter.content;
            }
        };
        int transformedStart = AspectsConvertingParser.translatePositionToAfterChanges(wordRegion.getOffset(), replacements);
        int transformedEnd = AspectsConvertingParser.translatePositionToAfterChanges(wordRegion.getOffset() + wordRegion.getLength(), replacements)-1;
        
        engine.select(wrappedUnit, transformedStart, transformedEnd);
        IJavaElement[] elements = requestor.getElements();
        return elements;
    }
}
