/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.ui;


import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class UIConstants {

	public static final Font ITALIC = JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);

	public static final Font BOLD = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);

	public static final Font DEFAULT_FONT = JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);

	public static final Color GREEN = new Color(Display.getDefault(), 0x99, 0xff, 0x99);

	public static final Color RED = new Color(Display.getDefault(), 0xff, 0xcc, 0xcc);

	public static final Color YELLOW = new Color(Display.getDefault(), 0xff, 0xff, 0x99);
}
