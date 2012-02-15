/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sian January - initial version
 *     Matt Chapman - switch to IVisualiserPalette, and RGB instead of Color 
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.visualiser;

import java.util.Random;

import org.eclipse.contribution.visualiser.interfaces.IVisualiserPalette;
import org.eclipse.swt.graphics.RGB;

/**
 * Utility class used to get colours Ensures no reds or yellows are included to
 * avoid conflict between errors and warnings
 */
public class AJDTPalette implements IVisualiserPalette {

	// Colour constants
	protected final static RGB color1 = new RGB(128, 200, 255);

	protected final static RGB color2 = new RGB(156, 255, 128);

	protected final static RGB color3 = new RGB(192, 128, 255);

	protected final static RGB color4 = new RGB(255, 128, 170);

	protected final static RGB color5 = new RGB(128, 255, 255);

	protected final static RGB color6 = new RGB(255, 128, 255);

	protected final static RGB color7 = new RGB(192, 255, 192);

	protected final static RGB color8 = new RGB(128, 255, 192);

	protected final static RGB color9 = new RGB(255, 203, 128);

	protected final static RGB color10 = new RGB(225, 192, 255);

	protected final static RGB color11 = new RGB(192, 192, 255);

	protected final static RGB color12 = new RGB(255, 192, 192);

	protected final static RGB color13 = new RGB(255, 225, 192);

	protected final static RGB color14 = new RGB(192, 255, 255);

	protected final static RGB color15 = new RGB(255, 192, 255);

	protected final static RGB color16 = new RGB(192, 225, 255);

	protected final static RGB color17 = new RGB(225, 255, 192);

	protected final static RGB color18 = new RGB(255, 64, 170);

	protected final static RGB color19 = new RGB(255, 170, 64);

	protected final static RGB color20 = new RGB(64, 255, 170);

	protected final static RGB color21 = new RGB(64, 170, 255);

	protected final static RGB color22 = new RGB(170, 64, 255);

	protected final static RGB color23 = new RGB(0, 255, 0);

	protected final static RGB color24 = new RGB(0, 255, 255);

	protected final static RGB color25 = new RGB(0, 0, 255);

	protected final static RGB color26 = new RGB(255, 0, 255);

	protected final static RGB[] rgbList = new RGB[] { color1, color2, color3,
			color4, color5, color6, color7, color8, color9, color10, color11,
			color12, color13, color14, color15, color16, color17, color18,
			color19, color20, color21, color22, color23, color24, color25,
			color26 };

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.visualiser.interfaces.IVisualiserPalette#getRGBValues()
	 */
	public RGB[] getRGBValues() {
		return rgbList;
	}

	/**
	 * Returns a colour with random R and G and B values that are between 50 and
	 * 200 and a random B value between 50 and 250, which are also multiples of
	 * 5. This is to avoid anything close to colours used by errors and warnings
	 * and to try and prevent two colours that look the same.
	 * 
	 * @return randomly generated Color
	 */
	public RGB getRandomRGBValue() {
	    Random rand = new Random();
		int r = Math.abs(((int) (rand.nextInt() * 30)) * 5 + 50) % 255;
		int g = Math.abs(((int) (rand.nextInt() * 30)) * 5 + 50) % 255;
		int b = Math.abs(((int) (rand.nextInt() * 40)) * 5 + 50) % 255;
		return new RGB(r, g, b);
	}

}