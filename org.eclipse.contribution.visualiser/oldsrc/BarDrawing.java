/*******************************************************************************
 * Copyright (c) 2002 - 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sian Whiting - initial version and later features
 * 	   Andy Clement - refactored for stand-alone visualiser
 *     Matt Chapman - added support for keyboard traversal
 *******************************************************************************/
package org.eclipse.contribution.visualiser.views.old;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.contribution.visualiser.core.Stripe;
import org.eclipse.contribution.visualiser.interfaces.IGroup;
import org.eclipse.contribution.visualiser.interfaces.IMarkupKind;
import org.eclipse.contribution.visualiser.interfaces.IMarkupProvider;
import org.eclipse.contribution.visualiser.interfaces.IMember;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;

/**
 * When given a canvas and various data BarDrawing displays the data on the
 * canvas using draw2d. Designed for general purpose visualisation.
 */
public class BarDrawing {

	private boolean absolute_proportions;
	private boolean continueWithAbsoluteProportions;
	private static int LOGLEVEL = 0;
	private MouseMotionListener locationListener;
	private boolean mouseInARectangle = false;
	private Shape mouseInThisRectangle;
	private Action onlyShowAction;
	private org.eclipse.swt.widgets.Menu rCMenu;
	private int spacing = 3;
	int maxBarWidth = 60;
	int minBarWidth = 20;
	int highlightDepth = 1;
	private int selectedColumn = -1;
	private int classNum = -1;
	private int lineNum = -1;
	private float zoom = 1;
	private float max_zoom = 5;
	private float scale;
	private boolean demarcation = true;
	private Shape[] rectangles;
	private Vector[] lineNums;
	private IFigure panel;
	private MouseListener mouseListener;
	private BarButton[] columnButtons;
	private boolean limit_mode = false;
	public org.eclipse.swt.graphics.Rectangle viewsize;
	private List whatToDisplay;
	public static Control canvas;
	private static BarDrawing singleton;

	// boolean used to prevent recursion of draw method
	boolean alreadyInDrawMethod = false;

	// Map on screen rectangles to RectangleData - this gives the member and
	// markup for a rectangle
	Hashtable rectangleTable;

	// A map of line numbers to Annotation instances.
	Map[] markups;
//	private LightweightSystem lws;

	/**
	 * Get the single instance of this class
	 * @return
	 */
	public static BarDrawing getBarDrawing() {
		if (singleton == null) {
			singleton = new BarDrawing();
		}
		return singleton;
	}

	/**
	 * The Constructor. Instantiates the panel and mouse listener and
	 * implements the mouse listener event handlers.
	 */
	private BarDrawing() {
		panel = new Figure();

		// Instantiate the mouse listener
		mouseListener = new MouseListener() {
			public void mousePressed(MouseEvent e) {
				if (e.button == 1) { // single click on button1 means selection so action is not passed to the providers
					if (e.getSource() instanceof TraversableRectangleFigure) {
						TraversableRectangleFigure rectangle = (TraversableRectangleFigure) e.getSource();
						rectangle.requestFocus();
					}
				} else if ((e.getSource() instanceof Figure)
					&& !(e.getSource() instanceof RectangleFigure)
					//&& (e.button == 3)
					) {
					log(3, "Processing button3 click on Figure");
					VisualiserPlugin.visualiser.handleClick(null, null, null, e.button);
				} else if (e.getSource() instanceof RectangleFigure) {
						log(3, "Processing button click on a rectangle");
						selectedColumn = -1;
						// Check if a rectangle was clicked.
						RectangleFigure rectangle = (RectangleFigure) e.getSource();
						RectangleData rd =
							(RectangleData) rectangleTable.get(rectangle);
						log(3, "You clicked: " + rd.toString());
						VisualiserPlugin.visualiser.handleClick(
							rd.mem,
							rd.kind,
							rd.stripe,
							e.button);
				}
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseDoubleClicked(MouseEvent e) {
				if(e.button == 1) { // pass double clicks with button1 to the provider
					if (e.getSource() instanceof RectangleFigure) {
						log(3, "Processing button click on a rectangle");
						selectedColumn = -1;
						// Check if a rectangle was clicked.
						RectangleFigure rectangle = (RectangleFigure) e.getSource();
						RectangleData rd =
							(RectangleData) rectangleTable.get(rectangle);
						log(3, "You clicked: " + rd.toString());
						VisualiserPlugin.visualiser.handleClick(
							rd.mem,
							rd.kind,
							rd.stripe,
							e.button);
					} else {
						VisualiserPlugin.visualiser.handleClick(null, null, null, e.button);
					}
				}
			}
		};

		locationListener = new MouseMotionListener() {

			public void mouseEntered(MouseEvent e) {
				mouseInARectangle = true;
				mouseInThisRectangle = (RectangleFigure) e.getSource();
			}

			public void mouseExited(MouseEvent arg0) {
				mouseInARectangle = false;
			}

			public void mouseHover(MouseEvent arg0) {
			}

			public void mouseMoved(MouseEvent arg0) {
			}

			public void mouseDragged(MouseEvent arg0) {
			}
		};

	}

	/**
	 * Fill the context menu for the Visualiser
	 * @param menu
	 */
	private void fillRightClickMenu(MenuManager menu) {
		onlyShowAction = new Action() {
			public void run() {
				VisualiserPlugin.visualiser.onlyShowColorsAffecting(
					((Label) mouseInThisRectangle.getToolTip()).getText());
			}
		};
		onlyShowAction.setText(VisualiserPlugin.getResourceString("OnlyShow"));
		// add the actions to the menu
		menu.add(onlyShowAction);
	}

	/**
	 * Draw is the main method. It is passed a canvas and some data and it
	 * calls subsidiary methods to display the data graphically on the canvas.
	 */
	public void draw(List whatToDraw, Canvas canvas, boolean absolute_proportions) {
		
		if (alreadyInDrawMethod)
			return;
		if (canvas == null)
			return;
		Control[] children = canvas.getChildren();
		for (int i = 0; i < children.length; i++) {
			children[i].dispose();
		}
		this.setAbsoluteProportions(absolute_proportions);
		alreadyInDrawMethod = true;
		BarDrawing.canvas = canvas;
		clearPanel();
		this.whatToDisplay = whatToDraw;
		if (whatToDisplay == null) {
			whatToDisplay = Visualiser.contentP.getAllGroups();
		}
		int size = (whatToDraw == null ? 0 : whatToDraw.size());
		rectangles = new Shape[size];
		if(rectangleTable != null) {
			rectangleTable.clear();
		} else {
			rectangleTable = new Hashtable();
		}
		lineNums = new Vector[size];
		if (panel != null) {
			panel.removeMouseListener(mouseListener);
		}
		panel = new Figure();
		panel.addMouseListener(mouseListener);
		FlowLayout layout = new FlowLayout();
		layout.setMinorSpacing(spacing);
		panel.setLayoutManager(layout);
		panel.setBorder(new LineBorder(ColorConstants.menuBackground, 5));
		viewsize = canvas.getParent().getClientArea();
		if (zoom > 1) {
			viewsize.width = (int) ((float) viewsize.width * zoom);
			viewsize.height = (int) ((float) viewsize.height * zoom);
		}
		LightweightSystem lws = new LightweightSystem(canvas);
		lws.setContents(panel);
		if (size > 0) {
			if(absolute_proportions) {
				addShapesAndKeepProportions(size, viewsize, canvas.getParent().getClientArea());
			} else {
				addShapes(size, viewsize);
			}
		} else {
			Label label = new Label(Visualiser.contentP.getEmptyMessage());
			panel.add(label);
		}

		org.eclipse.swt.graphics.Point canvasSize = canvas.getSize();
		org.eclipse.swt.graphics.Point preferredSize;
		if (zoom > 1 || this.absolute_proportions) {
			preferredSize =
				new org.eclipse.swt.graphics.Point(
					Math.max(
						panel.getPreferredSize().width,
						canvas.getParent().getClientArea().width),
					Math.max(
						canvas.getParent().getClientArea().height,
						panel.getPreferredSize().height));
		} else {
			preferredSize =
				new org.eclipse.swt.graphics.Point(
					Math.max(
						panel.getPreferredSize().width,
						canvas.getParent().getClientArea().width),
					Math.min(
						canvas.getParent().getClientArea().height,
						panel.getPreferredSize().height));
		}
		if (!canvasSize.equals(preferredSize)) {
			canvas.setSize(preferredSize);				
		}
		
		// Configure the right click menu.
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		fillRightClickMenu(menuMgr);
		rCMenu = menuMgr.createContextMenu(canvas);
		canvas.setMenu(rCMenu);
		rCMenu.addMenuListener(new MenuListener() {
			public void menuHidden(MenuEvent e) {
			}
			public void menuShown(MenuEvent e) {
				onlyShowAction.setEnabled(mouseInARectangle);
			}
		});
		alreadyInDrawMethod = false;
	}

	/**
	 * AddShapes is called by draw to add the shapes to the panel.
	 */
	private void addShapes(
		int size,
		org.eclipse.swt.graphics.Rectangle viewsize) {

		columnButtons = new BarButton[size];
		boolean hScrollBar = false;
		int width = 0;
		int maximum = 0;
		int num = 0;
		int max_depth = viewsize.height - 30;

		// Calculates the maximum rectangle depth from all rectangles being
		// drawn
		for (int i = 0; i < size; i++) {
			boolean beingShown = true;
			Object o = whatToDisplay.get(i);
			if (beingShown) {
				num++;
				int sze = ((IMember) o).getSize().intValue();
				if (sze > maximum)
					maximum = sze;
			}
		}

		// Calculates width and scale based on number of rectangles
		// and maximum rectangle depth respectively, and viewsize.
		if (!limit_mode)
			num = size;

		if (num * (minBarWidth + spacing) > (viewsize.width) - 10) {
			width = minBarWidth;
			hScrollBar = true;
		} else if (num * (maxBarWidth + spacing) < (viewsize.width) - 10) {
			width = maxBarWidth;
		} else
			width = (int) ((float) viewsize.width - 10) / num - spacing;

		if (hScrollBar) {
			max_depth = max_depth - 20;
		}

		scale = (float) max_depth / (float) maximum;
		if (scale > highlightDepth) {
			scale = (float) Math.floor(scale);
		}

		BarButton prevButton = null;
		// Add shapes to the panel
		for (int i = 0; i < size; i++) {
			IMember member = (IMember) whatToDisplay.get(i);

			rectangles[i] = new RectangleFigure();
			rectangleTable.put(
				rectangles[i],
				new RectangleData(member, null, null));
			rectangles[i].addMouseListener(mouseListener);
			rectangles[i].addMouseMotionListener(locationListener);
			lineNums[i] = new Vector();

			int barHeight =
				((int) (((float) member.getSize().intValue()) * scale + 0.5f))
					+ 20;

			rectangles[i].setSize(width, barHeight);
			rectangles[i].setToolTip(new Label(member.getToolTip()));

			// A button is the 'label box' at the top of a member
			columnButtons[i] =
				createButton(member.getName(), rectangles[i].getSize().width);
			if (prevButton!=null) {
				prevButton.setRightComponent(columnButtons[i]);
				columnButtons[i].setLeftComponent(prevButton);
			}
			prevButton = columnButtons[i];

			rectangles[i].add(columnButtons[i]);
			rectangles[i].setBackgroundColor(ColorConstants.buttonDarkest);
			IMarkupProvider vmp = Visualiser.markupP;

			// Lets add subrectangles to rectangles to represent the members of
			// a package
			if (whatToDisplay.get(i) instanceof IGroup && demarcation) {
				IGroup ig = (IGroup) whatToDisplay.get(i);
				List mems = (List) ig.getMembers();
				IMember[] memsArray = new IMember[mems.size()];
				float offset = 20.0f;

				mems.toArray(memsArray);
				for (int j = 0; j < memsArray.length; j++) {
					IMember member2 = memsArray[j];

					RectangleFigure rec = new RectangleFigure();
					rec.addMouseListener(mouseListener);
					rec.addMouseMotionListener(locationListener);
					rectangleTable.put(
						rec,
						new RectangleData(member2, null, null));

					float barheight =
						((float) member2.getSize().intValue()) * scale + 1.5f;
					if (j == (memsArray.length - 1))
						barheight = rectangles[i].getSize().height - offset;
					rec.setSize(width, (int) (barheight));
					rec.setLocation(new Point(0, (int) offset));
					rec.setToolTip(
						new org.eclipse.draw2d.Label(member2.getToolTip()));
					offset += (float) (int) (barheight - 1);
					rec.setBackgroundColor(ColorConstants.buttonDarkest);
					rectangles[i].add(rec);
					if (attachStripes(rec,
						vmp.getMemberMarkups(member2),
						vmp,
						width,
						true,
						member2,
						columnButtons[i])) {
						rec.setBackgroundColor(ColorConstants.white);
					}
				}
			} else if (whatToDisplay.get(i) instanceof IGroup) {
				log(3, "Striping " + member.getFullname());
				attachStripes(
					(RectangleFigure) rectangles[i],
					vmp.getGroupMarkups((IGroup) member),
					vmp,
					width,
					false,
					member,
					columnButtons[i]);
			} else {
				log(3, "Striping " + member.getFullname());
				attachStripes(
					(RectangleFigure) rectangles[i],
					vmp.getMemberMarkups(member),
					vmp,
					width,
					false,
					member,
					columnButtons[i]);
			}

			if (limit_mode) {
				if (rectangles[i].getBackgroundColor() == ColorConstants.white)
					panel.add(rectangles[i]);
			} else {
				panel.add(rectangles[i]);
			}
		}
	}
	

	/**
	 * Add shapes to the canvas but ensure that minimum stripe height is honoured 
	 * as minimum line height, stripes do not overlap vertically and white space 
	 * representing lines without stripes is not compressed
	 * @param size
	 * @param viewsize
	 */
	private void addShapesAndKeepProportions(int size, org.eclipse.swt.graphics.Rectangle viewsize, org.eclipse.swt.graphics.Rectangle originalViewsize) {
		int max_member_size = 0;
		
		for (int i = 0; i < size; i++) {
			IMember member = (IMember) whatToDisplay.get(i);
			if(member.getSize().intValue() > max_member_size) {
				max_member_size = member.getSize().intValue();
			}
		}
		int max_bar_height = max_member_size * highlightDepth + 20;
		int viewsize_height_required = max_bar_height + 30;
		if(viewsize.height < viewsize_height_required) {		
			viewsize.height = viewsize_height_required;			
		} 

		int widthRequired = size * (minBarWidth + spacing);
		int area = widthRequired * viewsize.height;
		boolean cont = true;
		// The following is a work around for the "No more Handles" exception which occurs if the
		// canvas size requested is too big.
		// WORKS if area = 5,078,745, FAILS if area = 5,523,105
		if (area > 5078745) {
			String title = "Absolute proportions view would consume too much area";
			String message = 
				"If the visualise attempts to render your chosen resources in"+
				" absolute proportions mode, you are likely to see a NoMoreHandles"+
				" exception from SWT.  Do you want to continue?  (As a workaround,"+
				" you could try reducing the stripe height in the visualiser configuration"+
				" and trying again)";
			cont = MessageDialog.openQuestion(canvas.getParent().getShell(),title,message);
		}

		if (cont) {
			continueWithAbsoluteProportions = true;
			addShapes(size, viewsize);	
		} else {
			continueWithAbsoluteProportions = false;
			this.absolute_proportions = false;
			VisualiserPlugin.visualiser.resetAbsoluteProportions();
			addShapes(size, originalViewsize);
		}		
	}


	/**
	 * Attach stripes to a rectangle
	 * @param rec
	 * @param markups
	 * @param vmp
	 * @param width
	 * @param ingroupview
	 * @param mem
	 * @param button
	 */
	private boolean attachStripes(
		RectangleFigure rec,
		List markups,
		IMarkupProvider vmp,
		int width,
		boolean ingroupview,
		IMember mem,
		Button button) {

		Stripe s = null;

		boolean striped = false;
		int numStripes = 0;
		if (markups != null && markups.size() > 0) {
			// Sort Stripes by offset so we get a logical traversal order
			Set sortedSet = new TreeSet();
			for (int j = 0; j < markups.size(); j++) {
				s = (Stripe) markups.get(j);
				if (s != null) {
					sortedSet.add(s);
				}
			}

			List aboveRowList = null;
			for (Iterator iter = sortedSet.iterator(); iter.hasNext();) {
				numStripes++;
				s = (Stripe) iter.next();

				// Rectangle representing the stripe
				int activeKinds = 0;
				for (int i = 0; i < s.getKinds().size(); i++) {
					if (VisualiserPlugin.menu == null)
						activeKinds++;
					else if (
						VisualiserPlugin.menu.getActive(
							(IMarkupKind) s.getKinds().get(i)))
						activeKinds++;
				}
				int across = 0;

				TraversableRectangleFigure leftF = null;
				List thisRowList = new ArrayList();
				for (int i = 0; i < s.getKinds().size(); i++) {
					if (VisualiserPlugin.menu == null
						|| VisualiserPlugin.menu.getActive(
							(IMarkupKind) s.getKinds().get(i))) {
						striped = true;
						TraversableRectangleFigure f =
							new TraversableRectangleFigure(rectangleTable);
						
						if (leftF != null) {
							f.setLeftComponent(leftF);
							leftF.setRightComponent(f);
						}
						leftF = f;
						thisRowList.add(f);

						rectangleTable.put(
							f,
							new RectangleData(
								mem,
								s,
								((IMarkupKind) s.getKinds().get(i)).getName()));

						addStripe(
							f,
							rec,
							s,
							width,
							across++,
							activeKinds,
							ingroupview,
							vmp,
							i);
					}
					// Before we add it to the 'super' rectangle - lets check
					// if we are adding it on top of an existing
				}
				if (thisRowList.size() > 0) {
					if (aboveRowList != null) {
						for (int i = 0; i < aboveRowList.size(); i++) {
							TraversableRectangleFigure lf =
								(TraversableRectangleFigure) aboveRowList.get(
									i);
							TraversableRectangleFigure ref =
								(TraversableRectangleFigure) thisRowList.get(
									(i * thisRowList.size())
										/ aboveRowList.size());
							lf.setDownComponent(ref);
						}
						for (int i = 0; i < thisRowList.size(); i++) {
							TraversableRectangleFigure lf =
								(TraversableRectangleFigure) thisRowList.get(i);
							TraversableRectangleFigure ref =
								(TraversableRectangleFigure) aboveRowList.get(
									(i * aboveRowList.size())
										/ thisRowList.size());
							lf.setUpComponent(ref);
						}
					} else {
						for (int i = 0; i < thisRowList.size(); i++) {
							TraversableRectangleFigure lf =
								(TraversableRectangleFigure) thisRowList.get(i);
							lf.setUpComponent(button);
							if (i == 0 && (button instanceof BarButton)) {
								((BarButton) button).setDownComponent(lf);
							}
						}
					}
					aboveRowList = thisRowList;
				}
			}
		}
		return striped;
	}

	private void addStripe(
		Figure f,
		RectangleFigure rec,
		Stripe s,
		int width,
		int across,
		int activeKinds,
		boolean ingroupview,
		IMarkupProvider vmp,
		int i) {
		if (rec.getBackgroundColor() != ColorConstants.white)
			rec.setBackgroundColor(ColorConstants.white);

		// Minus one because the offset for line 1 should be 0
		// etc..
		int offsetY = s.getOffset() - 1;

		// Should not get line numbers of <1 but just in case..
		if (offsetY < 0) {
			offsetY = 0;
		}
		int depth = s.getDepth();
		log(3, "Adding stripe to resource " + offsetY + " depth " + depth);
		// Set it to the position down the bar
		float xloc = /* point.x + */
		1.5f
			+ (((((float) width) - 1) / ((float) activeKinds))
				* ((float) across));

		float yloc = ((float) (offsetY) * scale) /* + point.y */;
		if (ingroupview) {
			Point point = rec.getLocation();
			yloc += point.y;
		} else {
			yloc += 20;
		}
		// If offset is 0 add 1 so as not to cover the
		// border at the top of the bar.
		if (offsetY == 0) {
			yloc++;
		}
		f.setLocation(new Point(xloc, yloc));

		int drawheight = 0;
		if (depth == 1) {
			drawheight = (int) (Math.max(highlightDepth, Math.floor(scale)));
		} else {
			drawheight =
				(int) (Math.max((float) highlightDepth, scale) * (float) depth);
		}
		int drawwidth = (int) ((width - 2) / activeKinds + 1.5f);

		int widthwidth = rec.getBounds().width;
		int heightheight = rec.getLocation().y + rec.getBounds().height;
		while ((yloc + drawheight) >= heightheight) {
			drawheight--;
		}
		while ((xloc + drawwidth) >= widthwidth) {
			drawwidth--;
		}
		if (drawheight > 0) {
			f.setSize(drawwidth, drawheight);
			//f.setOutline(false);
			f.setBackgroundColor(vmp.getColorFor((IMarkupKind) s.getKinds().get(i)));
			f.addMouseListener(mouseListener);
			f.setToolTip(
				new org.eclipse.draw2d.Label(" " + s.getKinds().get(i) + " "));
			rec.add(f);
		}

	}

//	/**
//	 * Called in subselect mode so that only selected bars are redrawn.
//	 */
//	public List subSelect() {
//		List subselectedData = new ArrayList();
//		if (markups != null) {
//			// Change the selection to match those bars with markup
//			int count = 0;
//			if (whatToDisplay != null) {
//
//				Map[] mapSelection = new Map[count];
//				for (int i = 0; i < whatToDisplay.size(); i++) {
//					if (columnButtons[i].isSelected()) {
//						subselectedData.add(whatToDisplay.get(i));
//						mapSelection[count] = markups[i];
//						count++;
//					}
//				}
//			}
//		} else {
//			// TODO: Show everything??
//			int count = 0;
//			if (whatToDisplay != null) {
//				for (int i = 0; i < whatToDisplay.size(); i++) {
//					if (columnButtons[i].isSelected()) {
//						subselectedData.add(whatToDisplay.get(i));
//						count++;
//					}
//				}
//			}
//		}
//		return subselectedData;
//	}

	/**
	 * Used to clear the panel
	 */
	private void clearPanel() {
		java.util.List children = panel.getChildren();
		if (children.size() > 0) {
			for (int i = 0; i < children.size();) {
				IFigure figure = (IFigure) children.get(i);
				panel.remove(figure);
				figure.erase();
			}
		}
	}

	/**
	 * @param absolute_proportions The absolute_proportions to set.
	 */
	private void setAbsoluteProportions(boolean absolute_proportions) {
		this.absolute_proportions = absolute_proportions;
		if(absolute_proportions) {
			zoom = 1;
		}
	}

	
	/**
	 * Increases magnification by 0.5 (up to max_zoom) and redraws. Returns
	 * true if further zoom in is possible.
	 */
	public boolean zoomIn(Canvas canvas) {
		if (zoom + (float) 0.5 <= max_zoom) {
			zoom = zoom + (float) 0.5;
			draw(whatToDisplay, canvas, absolute_proportions);			
		}
		return (zoom + (float) 0.5 <= max_zoom);
	}

	/**
	 * Decreases magnification by 0.5 (down to 1) and redraws. Returns true if
	 * further zoom out is possible.
	 */
	public boolean zoomOut(Canvas canvas) {
		if (zoom - (float) 0.5 <= 1) {
			zoom = 1;
		} else {
			zoom = zoom - (float) 0.5;
		}
		draw(whatToDisplay, canvas, absolute_proportions);
		return (zoom > 1);
	}

	/**
	 * Get the data currently being displayed
	 * @return
	 */
	protected List getDisplayedData() {
		return whatToDisplay;
	}

	/**
	 * Set demarcation to be on (true) or off (false) when in group view.
	 * 
	 * @param set - demarcation on or off
	 */
	public void setDemarcation(boolean set, Canvas canvas) {
		if (demarcation != set) {
			demarcation = set;
			draw(whatToDisplay, canvas, absolute_proportions);
		}
	}

	/**
	 * Create a button and return it
	 * @param n
	 * @param width
	 * @return
	 */
	public BarButton createButton(String n, int width) {
		BarButton b = new BarButton(n);
		b.setBackgroundColor(ColorConstants.buttonDarker);
		b.setSize(width, 20);
		b.setStyle(org.eclipse.draw2d.Clickable.STYLE_TOGGLE);
		return b;
	}

	/**
	 * Log the given message if the level is lower than or equal to the current level.
	 * @param level
	 * @param msg
	 */
	private void log(int level, String msg) {
		if (level <= LOGLEVEL) {
			System.err.println(msg);
		}
	}

}
