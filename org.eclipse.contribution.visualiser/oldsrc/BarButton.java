/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.contribution.visualiser.views.old;

import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.KeyEvent;
import org.eclipse.draw2d.KeyListener;
import org.eclipse.swt.SWT;


/**
 * Extends the draw2d Button class to allow buttos used in the Visualise drawing
 * to be navigated via the arrow keys on the keyboard
 */
public class BarButton extends Button {
	
	private Figure downComponent;
	private Figure leftComponent;
	private Figure rightComponent;
	
	/**
	 * Constructor - creates the button and sets up navigation components
	 * @param n
	 */
	public BarButton(String n) {
		super(n);
		addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent ke) {
				if ((downComponent != null) && ke.keycode==SWT.ARROW_DOWN) {
					downComponent.requestFocus();
				} else if ((leftComponent != null) && ke.keycode==SWT.ARROW_LEFT) {
					leftComponent.requestFocus();
				} if ((rightComponent != null) && ke.keycode==SWT.ARROW_RIGHT) {
					rightComponent.requestFocus();
				}
			}
			public void keyReleased(KeyEvent ke) {
			}
		});
	}

	
	/**
	 * @return Returns the downComponent.
	 */
	public Figure getDownComponent() {
		return downComponent;
	}

	
	/**
	 * @param downComponent The downComponent to set.
	 */
	public void setDownComponent(Figure downComponent) {
		this.downComponent = downComponent;
	}

	
	/**
	 * @return Returns the leftComponent.
	 */
	public Figure getLeftComponent() {
		return leftComponent;
	}

	
	/**
	 * @param leftComponent The leftComponent to set.
	 */
	public void setLeftComponent(Figure leftComponent) {
		this.leftComponent = leftComponent;
	}

	
	/**
	 * @return Returns the rightComponent.
	 */
	public Figure getRightComponent() {
		return rightComponent;
	}

	
	/**
	 * @param rightComponent The rightComponent to set.
	 */
	public void setRightComponent(Figure rightComponent) {
		this.rightComponent = rightComponent;
	}

}