/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Luzius Meisser - adjustements for AJDT
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.codeconversion.ConversionOptions;
import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser.Replacement;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * Code copied from JavaFormattingStrategy unless marked with '//AJDT change'
 *
 * @author Luzius Meisser
 */
public class AJFormattingStrategy extends ContextBasedFormattingStrategy {

	/** Documents to be formatted by this strategy */
	private final LinkedList<IDocument> fDocuments = new LinkedList<>();

	/** Partitions to be formatted by this strategy */
	private final LinkedList<TypedPosition> fPartitions = new LinkedList<>();

	/**
	 * Creates a new java formatting strategy.
	 */
	public AJFormattingStrategy() {
		super();
	}

	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#format()
	 */
	public void format() {
		super.format();

		final IDocument document = fDocuments.removeFirst();
		final TypedPosition partition = fPartitions
				.removeFirst();

		if (document != null && partition != null) {
			try {
				// AJDT change start

				//get the document contents and convert them into java syntax
				String content = document.get();
				AspectsConvertingParser pars = new AspectsConvertingParser(
						content.toCharArray());
				ArrayList<Replacement> changes = pars.convert(ConversionOptions.CONSTANT_SIZE);
				content = new String(pars.content);

				final TextEdit edit = CodeFormatterUtil.reformat(
						CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, content, partition
								.getOffset(), partition.getLength(), 0,
						TextUtilities.getDefaultLineDelimiter(document),
						getPreferences());



				if (edit != null) {
					//remove the edits for areas that have been changed by us (the "AspectJ code areas")
					if (changes.size() > 0){
					TextEdit[] edits = edit.getChildren();
            for (TextEdit edit2 : edits) {
              boolean conflict = AspectsConvertingParser.conflictsWithAJEdit(
                edit2.getOffset(), edit2.getLength(), changes);
              if (conflict) {
                edit.removeChild(edit2);
              }
            }
					}
					//apply remaining edits
					edit.apply(document);
				}

				 //AJDT change stop

			} catch (MalformedTreeException exception) {
				JavaPlugin.log(exception);
			} catch (BadLocationException exception) {
				// Can only happen on concurrent document modification - log and
				// bail out
				JavaPlugin.log(exception);
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStarts(org.eclipse.jface.text.formatter.IFormattingContext)
	 */
	public void formatterStarts(final IFormattingContext context) {
		super.formatterStarts(context);

		fPartitions.addLast((TypedPosition) context
				.getProperty(FormattingContextProperties.CONTEXT_PARTITION));
		fDocuments.addLast((IDocument) context
				.getProperty(FormattingContextProperties.CONTEXT_MEDIUM));
	}

	/*
	 * @see org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStops()
	 */
	public void formatterStops() {
		super.formatterStops();

		fPartitions.clear();
		fDocuments.clear();
	}
}
