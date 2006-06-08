/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.contribution.visualiser.utils;

import org.eclipse.contribution.visualiser.interfaces.IContentProvider;
import org.eclipse.contribution.visualiser.views.VisualiserCanvas;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import java.util.List;

/**
 * Development aspect to collect various graphics-related information, and pass
 * it to the Rendering Monitor view if it is showing (the view needs to be enabled
 * in the plugin.xml).
 * 
 * @author matt
 */
public aspect RenderingMonitorInfo {
	
	pointcut paintCall() : execution(void VisualiserCanvas.paint(..));

	pointcut calcGeomCall() : execution(void VisualiserCanvas.calculateGeom(..));

	pointcut colorCreation() : call(Color.new(..));

	pointcut colorDisposal() : call(void Color.dispose());

	pointcut imageCreation(Display d, int w, int h) : call(Image.new(..)) && args(d,w,h);
	
	pointcut imageCreationFromImageData(Display d, ImageData id) : call(Image.new(..)) && args(d,id);

	pointcut imageDisposal(Image i) : call(void Image.dispose()) && target(i);

	pointcut getProviderData() : call(List IContentProvider.getAll*());
	
	private int numCols;
	
	private long imagePixels;
	
	private long startTime;
	
	private long startGeomTime;
	
	before() : paintCall() {
		startTime = System.currentTimeMillis();
	}
	
	after() : paintCall() {
		long elapsed = System.currentTimeMillis() - startTime;
		RenderingMonitor.logTime(elapsed);
	}

	before() : calcGeomCall() {
		startGeomTime = System.currentTimeMillis();
	}
	
	after() : calcGeomCall() {
		long elapsed = System.currentTimeMillis() - startGeomTime;
		RenderingMonitor.logGeomTime(elapsed);
	}

	before() : execution(void VisualiserCanvas.redraw(..)) {
		RenderingMonitor.resetAverage();
	}
		
	List around() : getProviderData() {
		long start = System.currentTimeMillis();
		List l = proceed();
		long elapsed = System.currentTimeMillis() - start;
		RenderingMonitor.logProvTime(elapsed);
		RenderingMonitor.logDataSize(l.size());
		return l;
	}
	
	before() : colorCreation() {
		numCols++;
		RenderingMonitor.logNumCols(numCols);
	}
	
	before() : colorDisposal() {
		numCols--;
		RenderingMonitor.logNumCols(numCols);
	}
	
	before(Display d, int w, int h) : imageCreation(d,w,h) {
		imagePixels += w * h;
		RenderingMonitor.logImagePixels(imagePixels);
	}

	before(Display d, ImageData id) : imageCreationFromImageData(d,id) {
		imagePixels += id.width * id.height;
		RenderingMonitor.logImagePixels(imagePixels);
	}

	before(Image i) : imageDisposal(i) {
		imagePixels -= i.getBounds().width * i.getBounds().height;
		RenderingMonitor.logImagePixels(imagePixels);
	}
}
