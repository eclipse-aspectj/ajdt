/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - updated for Eclipse 3.1 (bug 109484)
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.ajdocexport;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Copied from org.eclipse.jdt.internal.ui.javadocexport.JavadocWriter
 * updated for Eclipse 3.1 (bug 109484)
 * Changes marked with // AspectJ Extension
 */
public class AJdocWriter {

	private static final char PATH_SEPARATOR= '/'; // use forward slash for all platforms

	private final OutputStream fOutputStream;
	private final IJavaProject[] fJavaProjects;
	private final IPath fBasePath;

	/**
	 * Create a AJdocWriter on the given output stream.
	 * It is the client's responsibility to close the output stream.
	 * @param basePath The base path to which all path will be made relative (if
	 * possible). If <code>null</code>, paths are not made relative.
	 */
	public AJdocWriter(OutputStream outputStream, IPath basePath, IJavaProject[] projects) {
		Assert.isNotNull(outputStream);
		fOutputStream= new BufferedOutputStream(outputStream);
		fBasePath= basePath;
		fJavaProjects= projects;
	}

	// AspectJ Extension - passing an AJdocOptionsManager as an argument
	public void writeXML(AJdocOptionsManager store) throws ParserConfigurationException, TransformerException {

		DocumentBuilder docBuilder;
		DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		docBuilder= factory.newDocumentBuilder();
		Document document= docBuilder.newDocument();

		// Create the document
		Element project= document.createElement("project"); //$NON-NLS-1$
		document.appendChild(project);

		project.setAttribute("default", "javadoc"); //$NON-NLS-1$ //$NON-NLS-2$

		Element javadocTarget= document.createElement("target"); //$NON-NLS-1$
		project.appendChild(javadocTarget);
		javadocTarget.setAttribute("name", "javadoc"); //$NON-NLS-1$ //$NON-NLS-2$

		Element xmlJavadocDesc= document.createElement("javadoc"); //$NON-NLS-1$
		javadocTarget.appendChild(xmlJavadocDesc);

		if (!store.isFromStandard())
			xmlWriteDoclet(store, document, xmlJavadocDesc);
		else
			xmlWriteJavadocStandardParams(store, document, xmlJavadocDesc);


		// Write the document to the stream
		Transformer transformer=TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4"); //$NON-NLS-1$ //$NON-NLS-2$
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(fOutputStream);
		transformer.transform(source, result);

	}

	//writes ant file, for now only worry about one project
	// AspectJ Extension - passing an AJdocOptionsManager as an argument
	private void xmlWriteJavadocStandardParams(AJdocOptionsManager store, Document document, Element xmlJavadocDesc) throws DOMException {

		String destination= getPathString(Path.fromOSString(store.getDestination()));

		xmlJavadocDesc.setAttribute(store.DESTINATION, destination);
		xmlJavadocDesc.setAttribute(store.VISIBILITY, store.getAccess());
		String source= store.getSource();
		if (source.length() > 0 && !source.equals("-")) { //$NON-NLS-1$
			xmlJavadocDesc.setAttribute(store.SOURCE, store.getSource());
		}
		xmlJavadocDesc.setAttribute(store.USE, booleanToString(store.getBoolean("use"))); //$NON-NLS-1$
		xmlJavadocDesc.setAttribute(store.NOTREE, booleanToString(store.getBoolean("notree"))); //$NON-NLS-1$
		xmlJavadocDesc.setAttribute(store.NONAVBAR, booleanToString(store.getBoolean("nonavbar"))); //$NON-NLS-1$
		xmlJavadocDesc.setAttribute(store.NOINDEX, booleanToString(store.getBoolean("noindex"))); //$NON-NLS-1$
		xmlJavadocDesc.setAttribute(store.SPLITINDEX, booleanToString(store.getBoolean("splitindex"))); //$NON-NLS-1$
		xmlJavadocDesc.setAttribute(store.AUTHOR, booleanToString(store.getBoolean("author"))); //$NON-NLS-1$
		xmlJavadocDesc.setAttribute(store.VERSION, booleanToString(store.getBoolean("version"))); //$NON-NLS-1$
		xmlJavadocDesc.setAttribute(store.NODEPRECATEDLIST, booleanToString(store.getBoolean("nodeprecatedlist"))); //$NON-NLS-1$
		xmlJavadocDesc.setAttribute(store.NODEPRECATED, booleanToString(store.getBoolean("nodeprecated"))); //$NON-NLS-1$


		//set the packages and source files
		List packages= new ArrayList();
		List sourcefiles= new ArrayList();
		sortSourceElement(store.getSourceElements(), sourcefiles, packages);
		if (!packages.isEmpty())
			xmlJavadocDesc.setAttribute(store.PACKAGENAMES, toSeparatedList(packages));

		if (!sourcefiles.isEmpty())
			xmlJavadocDesc.setAttribute(store.SOURCEFILES, toSeparatedList(sourcefiles));

		xmlJavadocDesc.setAttribute(store.SOURCEPATH, getPathString(store.getSourcepath()));
		xmlJavadocDesc.setAttribute(store.CLASSPATH, getPathString(store.getClasspath()));

		String overview= store.getOverview();
		if (overview.length() > 0)
			xmlJavadocDesc.setAttribute(store.OVERVIEW, overview);

		String styleSheet= store.getStyleSheet();
		if (styleSheet.length() > 0)
			xmlJavadocDesc.setAttribute(store.STYLESHEETFILE, styleSheet);

		String title= store.getTitle();
		if (title.length() > 0)
			xmlJavadocDesc.setAttribute(store.TITLE, title);


		String vmArgs= store.getVMParams();
		String additionalArgs= store.getAdditionalParams();
		if (vmArgs.length() + additionalArgs.length() > 0) {
			String str= vmArgs + ' ' + additionalArgs;
			xmlJavadocDesc.setAttribute(store.EXTRAOPTIONS, str);
		}

		String[] hrefs= store.getHRefs();
    for (String href : hrefs) {
      Element links = document.createElement("link"); //$NON-NLS-1$
      xmlJavadocDesc.appendChild(links);
      links.setAttribute(store.HREF, href);
    }
	}

	private void sortSourceElement(IJavaElement[] iJavaElements, List sourcefiles, List packages) {
    for (IJavaElement element : iJavaElements) {
      IPath p = element.getResource().getLocation();
      if (p == null)
        continue;

      if (element instanceof ICompilationUnit) {
        String relative = getPathString(p);
        sourcefiles.add(relative);
      }
      else if (element instanceof IPackageFragment) {
        packages.add(element.getElementName());
      }
    }
	}

	private String getPathString(IPath[] paths) {
		StringBuilder buf= new StringBuilder();

    for (IPath path : paths) {
      if (buf.length() != 0) {
        buf.append(File.pathSeparatorChar);
      }
      buf.append(getPathString(path));
    }

		if (buf.length() == 0) {
			buf.append('.');
		}
		return buf.toString();
	}

	private boolean hasSameDevice(IPath p1, IPath p2) {
		String dev= p1.getDevice();
		if (dev == null) {
			return p2.getDevice() == null;
		}
		return dev.equals(p2.getDevice());
	}

	//make the path relative to the base path
	private String getPathString(IPath fullPath) {
		if (fBasePath == null || !hasSameDevice(fullPath, fBasePath)) {
			return fullPath.toOSString();
		}
		int matchingSegments= fBasePath.matchingFirstSegments(fullPath);
		if (fBasePath.segmentCount() == matchingSegments) {
			return getRelativePath(fullPath, matchingSegments);
		}
    for (IJavaProject fJavaProject : fJavaProjects) {
      IProject proj = fJavaProject.getProject();
      IPath projLoc = proj.getLocation();
      if (projLoc.segmentCount() <= matchingSegments && projLoc.isPrefixOf(fullPath)) {
        return getRelativePath(fullPath, matchingSegments);
      }
    }
		IPath workspaceLoc= ResourcesPlugin.getWorkspace().getRoot().getLocation();
		if (workspaceLoc.segmentCount() <= matchingSegments && workspaceLoc.isPrefixOf(fullPath)) {
			return getRelativePath(fullPath, matchingSegments);
		}
		return fullPath.toOSString();
	}

	private String getRelativePath(IPath fullPath, int matchingSegments) {
		StringBuilder res= new StringBuilder();
		int backSegments= fBasePath.segmentCount() - matchingSegments;
		while (backSegments > 0) {
			res.append(".."); //$NON-NLS-1$
			res.append(PATH_SEPARATOR);
			backSegments--;
		}
		int segCount= fullPath.segmentCount();
		for (int i= matchingSegments; i < segCount; i++) {
			if (i > matchingSegments) {
				res.append(PATH_SEPARATOR);
			}
			res.append(fullPath.segment(i));
		}
		return res.toString();
	}

    // AspectJ Extension - passing an AJdocOptionsManager as an argument
	private void xmlWriteDoclet(AJdocOptionsManager store, Document document, Element xmlJavadocDesc) throws DOMException {

		//set the packages and source files
		List packages= new ArrayList();
		List sourcefiles= new ArrayList();
		sortSourceElement(store.getSourceElements(), sourcefiles, packages);
		if (!packages.isEmpty())
			xmlJavadocDesc.setAttribute(store.PACKAGENAMES, toSeparatedList(packages));

		if (!sourcefiles.isEmpty())
			xmlJavadocDesc.setAttribute(store.SOURCEFILES, toSeparatedList(sourcefiles));

		xmlJavadocDesc.setAttribute(store.SOURCEPATH, getPathString(store.getSourcepath()));
		xmlJavadocDesc.setAttribute(store.CLASSPATH, getPathString(store.getClasspath()));
		xmlJavadocDesc.setAttribute(store.VISIBILITY, store.getAccess());

		Element doclet= document.createElement("doclet"); //$NON-NLS-1$
		xmlJavadocDesc.appendChild(doclet);
		doclet.setAttribute(store.NAME, store.getDocletName());
		doclet.setAttribute(store.PATH, store.getDocletPath());

		String str= store.getOverview();
		if (str.length() > 0)
			xmlJavadocDesc.setAttribute(store.OVERVIEW, str);

		str= store.getAdditionalParams();
		if (str.length() > 0)
			xmlJavadocDesc.setAttribute(store.EXTRAOPTIONS, str);

	}

	private String toSeparatedList(List packages) {
		StringBuilder buf= new StringBuilder();
		Iterator iter= packages.iterator();
		int nAdded= 0;
		while (iter.hasNext()) {
			if (nAdded > 0) {
				buf.append(',');
			}
			nAdded++;
			String curr= (String) iter.next();
			buf.append(curr);
		}
		return buf.toString();
	}

	private String booleanToString(boolean bool) {
		if (bool)
			return "true"; //$NON-NLS-1$
		else
			return "false"; //$NON-NLS-1$
	}

	public void close() throws IOException {
		if (fOutputStream != null) {
			fOutputStream.close();
		}
	}


}
