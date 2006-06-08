/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.ajdt.core.AJLog;
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
		AJLog.log("Building AJ code scanner"); //$NON-NLS-1$
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