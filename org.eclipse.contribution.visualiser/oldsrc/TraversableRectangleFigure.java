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

import java.util.Hashtable;

import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FocusEvent;
import org.eclipse.draw2d.KeyEvent;
import org.eclipse.draw2d.KeyListener;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolTipHelper;
import org.eclipse.swt.SWT;


/**
 * Extended draw2d's RectangleFigure to add keyboard traversal capability
 */
public class TraversableRectangleFigure extends RectangleFigure {

	private Hashtable rectangleTable;
	private Figure upComponent;
	private Figure downComponent;
	private Figure leftComponent;
	private Figure rightComponent;
	private ToolTipHelper helper;

	/**
	 * The constructor
	 * @param rectangleTable
	 */
	public TraversableRectangleFigure(Hashtable rectangleTable) {
		super();
		this.rectangleTable = rectangleTable;
		setForegroundColor(ColorConstants.black);
		addKeyListener(new KeyHandler());
		setFocusTraversable(true);
		setRequestFocusEnabled(true);
		setOutline(false);
	}


	/**
	 * Focus is given to this figure - outlines the figure in the view
	 */	
	public void handleFocusGained(FocusEvent event) {
		super.handleFocusGained(event);
		setOutline(true);
		repaint();
	}
	
	
	/**
	 * Focus is lost by the figure - removes the outline.
	 */
	public void handleFocusLost(FocusEvent event) {
		super.handleFocusLost(event);
		setOutline(false);
		removeToolTip();
		repaint();
	}
		
		
	/**
	 * Listener for keyboard actions affecting this figure
	 */	
	class KeyHandler implements KeyListener {

		/**
		 * The figure is traversed TO by the user
		 * @param source
		 * @param dest
		 */
		private void traverseToFigure(TraversableRectangleFigure source, Figure dest) {
			if (dest instanceof TraversableRectangleFigure) {
				TraversableRectangleFigure f =
					(TraversableRectangleFigure)dest;
				if (source.isToolTipShowing()) {
					f.showToolTip();
				}
			}
			dest.requestFocus();
		}
		
		
		/**
		 * A key is pressed - traverse if it is an arrow key, show the tooltip
		 * if F2 is pressed, otherwise do nothing.
		 */
		public void keyPressed(KeyEvent ke) {
			Object obj = ke.getSource();
			if (!(obj instanceof TraversableRectangleFigure)) {
				return;
			}
			TraversableRectangleFigure fig = (TraversableRectangleFigure)obj;
			
			if (ke.character == ' ') {
				RectangleFigure rectangle = (RectangleFigure)ke.getSource();
				RectangleData rd = (RectangleData)fig.rectangleTable.get(rectangle);
				//log(3,"You pressed: " + rd.toString());
				VisualiserPlugin.visualiser.handleClick(rd.mem,rd.kind,rd.stripe,1);
			} else if (ke.keycode==SWT.ARROW_UP) {
				if (fig.getUpComponent()!=null) {
					traverseToFigure(fig, fig.getUpComponent());
				}						
			} else if (ke.keycode==SWT.ARROW_DOWN) {
				if (fig.getDownComponent()!=null) {
					traverseToFigure(fig, fig.getDownComponent());
				}										
			} else if (ke.keycode==SWT.ARROW_LEFT) {
				if (fig.leftComponent!=null) {
					traverseToFigure(fig, fig.getLeftComponent());
				}						
			} else if (ke.keycode==SWT.ARROW_RIGHT) {
				if (fig.rightComponent!=null) {
					traverseToFigure(fig, fig.getRightComponent());
				}						
			} else if (ke.keycode==SWT.F2) {
				fig.showToolTip();
			}
		}

		
		/**
		 * A key is released - no action
		 */
		public void keyReleased(KeyEvent ke) {}
	
	}
	
	
	/**
	 * Returns true if the tooltip is showing for this figure
	 * @return
	 */
	public boolean isToolTipShowing() {
		if (helper!=null && helper.isShowing()) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Show the tooltip for this figure
	 */
	public void showToolTip() {
		if (helper!=null && helper.isShowing()) {
			removeToolTip();
		}
		org.eclipse.swt.graphics.Point absolute =
			BarDrawing.canvas.toDisplay(new org.eclipse.swt.graphics.Point(
						getLocation().x,getLocation().y));
		helper = new ToolTipHelper(BarDrawing.canvas);
		helper.displayToolTipNear(this, getToolTip(),
					absolute.x,
					absolute.y);
	}
	
	
	/**
	 * Hide the tooltip for this figure
	 */
	private void removeToolTip() {
		if (helper!=null) {
			helper.dispose();
			helper=null;
		}
	}
	

	/**
	 * Returns the upComponent.
	 * @return 
	 */
	public Figure getUpComponent() {
		return upComponent;
	}


	/**
	 * @param upComponent The upComponent to set.
	 */
	public void setUpComponent(Figure upComponent) {
		this.upComponent = upComponent;
	}


	/**
	 * Returns the downComponent.
	 * @return 
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
	 * Returns the leftComponent.
	 * @return 
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
	 * Returns the rightComponent.
	 * @return 
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
