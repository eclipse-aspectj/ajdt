/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.RuleBasedScanner;

/**
 * Identical to the Java text tools except that we use our own scanner.
 */
public class AspectJTextTools extends JavaTextTools {

	private AspectJCodeScanner aspectjCodeScanner;

	/**
	 * Constructor for AspectJTextTools
	 */
	public AspectJTextTools(IPreferenceStore ips) {
		super(ips);
		AJDTEventTrace.generalEvent("Building AJ code scanner");
		aspectjCodeScanner = new AspectJCodeScanner(getColorManager(), ips);
	} 
	
   /**
	* Returns a scanner which is configured to scan AspectJ source code.
	*
	* @return an AspectJsource code scanner
	*/
	public RuleBasedScanner getCodeScanner() {
		return aspectjCodeScanner;
	}
	/**
	 * Disposes all the individual tools of this tools collection.
	 */
	public void dispose() {

		aspectjCodeScanner = null;

		super.dispose();
	}

	// In 3.0 this has been replaced by 
	// JavaSourceViewerConfiguration.affectsTextPresentation(PropertyChangeEvent event).
	// Therefore, following the Java model, have moved this to
	// AJSourceViewerConfiguration.affectsTextPresentation(PropertyChangeEvent event)
//	public boolean affectsBehavior(PropertyChangeEvent event) {
//		return  aspectjCodeScanner.affectsBehavior(event) ||
//		  super.affectsBehavior(event);
//	}
	
	/**
	 * Adapts the behavior of the contained components to the change
	 * encoded in the given event.
	 * 
	 * @param event the event to whch to adapt
	 */
	// This method is deprecated in 3.0 with no replacement
//	protected void adaptToPreferenceChange(PropertyChangeEvent event) {
//		if (aspectjCodeScanner.affectsBehavior(event))
//			aspectjCodeScanner.adaptToPreferenceChange(event);
//			
//			super.adaptToPreferenceChange(event);
//	}
	
	public IPreferenceStore getPreferenceStore() {
		return super.getPreferenceStore();
	}
	
	// (Luzius) Override to solve 67281
	public void setupJavaDocumentPartitioner(IDocument document, String partitioning) {
		IDocumentPartitioner partitioner= createDocumentPartitioner();
		
		//only change in function:
		//connect document before setting up partitioner
		//(otherwise we get a NPE when the partitioner tries to access
		//its document)
		partitioner.connect(document);
		
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3= (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(partitioning, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
		//partitioner.connect(document);
	}

	/**
	 * Added for 3.0 compatibility
	 * 
	 * @return Returns the aspectjCodeScanner.
	 */
	public AspectJCodeScanner getAspectjCodeScanner() {
		return aspectjCodeScanner;
	}
}