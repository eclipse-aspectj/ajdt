/*******************************************************************************
 * Copyright (c) 2002 - 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sian Whiting - initial version
 *     Andy Clement - refactored for stand-alone visualiser
 *******************************************************************************/
package org.eclipse.contribution.visualiser.views;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.contribution.visualiser.interfaces.IMarkupKind;
import org.eclipse.contribution.visualiser.interfaces.IMarkupProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;


/**
 * The Menu part of the Visualiser.  
 * Displays Markup kinds, colour selection buttons and checkboxes.
 */

public class Menu extends ViewPart {

	private IAction selectNoneAction;
	private IAction selectAllAction;
	private static String markerTypes = "cmesearchmarker";
	Button[] buttons;
	Button[] checkboxes;
	Label[] labels;
	Label[] icons;
	Shell[] shells;
	ColorDialog[] colorDialogs;
	Image[] colorSquares;
	Color[] colors;
	SelectionAdapter selectionListener;
	MouseListener mouseListener;
	Composite canvas;
	ScrolledComposite scrollpane;
	GridLayout layout = new GridLayout(4, false);
	VisualiserPlugin plugin = VisualiserPlugin.getDefault();
    private static IMarkupProvider vmp;
	private static Hashtable kindActive = null;
	private boolean uptodate = false;
	private Map kinds;
//	private String[] relationships =
//		new String[] { "Hide Search Markers", "Hide CME Markers" };

	/**
	 * The constructor.
	 */
	public Menu() {
		reset();
	}


	/**
	 * Set the current IMarkupProvider
	 * @param vmp
	 */
	public void setVisMarkupProvider(IMarkupProvider vmp) {
		if(Menu.vmp != null) {
			kinds = new HashMap();
			Menu.vmp.deactivate();
		}
		Menu.vmp = vmp;
		vmp.activate();
	}
	
	
	/**
	 * Private function used to create square images on colour chooser buttons.
	 */
	private void drawImage(Image image, Color color) {
		GC gc = new GC(image);
		gc.setBackground(color);
		Rectangle bounds = image.getBounds();
		gc.fillRectangle(0, 0, bounds.width, bounds.height);
		gc.drawRectangle(0, 0, bounds.width - 1, bounds.height - 1);
		gc.dispose();
	}

	
	/**
	 * This is a callback that allows us
	 * to create the composite and initialize it.
	 * It also creates listeners for the colour buttons and the checkboxes.
	 */
	public void createPartControl(Composite parent) {
		reset();
		scrollpane =
			new ScrolledComposite(
				parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		canvas = new Composite(scrollpane, SWT.NONE);
		scrollpane.setContent(canvas);
		canvas.setLayout(layout);

// commented out because double clicking was giving NPEs from openAspectSource

//		labelListener = new MouseListener() {
//			public void mouseUp(MouseEvent e) {
//			}
//			public void mouseDown(MouseEvent e) {
//			}
//			public void mouseDoubleClick(MouseEvent e) {
//				for (int i = 0; i < labels.length; i++) {
//					if ((Label) e.getSource() == labels[i]) {
//						VisualiserPlugin.visualiser.openAspectSource(i);
//						return;
//					}
//				}
//			}
//		};

		/* 
		 * Listener for colour buttons - if clicked produces a ColorDialog
		 * then redraws the square image with the chosen colour. 
		 */
		selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() instanceof Button) {
					Button button = (Button) e.getSource();
					int location = 0;
					for (int j = 0; j < buttons.length; j++) {
						if ((buttons[j]).equals(button)) {
							location = j;
						}
					}
					RGB rgb = colorDialogs[location].open();
					if (rgb == null) {
						return;
					}
					colors[location] = new Color(buttons[location].getDisplay(), rgb);
					Image image = buttons[location].getImage();
					drawImage(image, colors[location]);
					buttons[location].setImage(image);
					if (!(VisualiserPlugin.visualiser == null)) {
						vmp.setColorFor((IMarkupKind)labels[location].getData(),colors[location]);
						VisualiserPlugin.visualiser.draw();
					}
				}
			}
		};

		// Listener for checkboxes
		mouseListener = new MouseListener() {
			public void mouseUp(MouseEvent e) {
				if (!(VisualiserPlugin.visualiser == null)) {
					for (int i = 0; i < colors.length; i++) {
						kindActive.put((IMarkupKind)labels[i].getData(),new Boolean(checkboxes[i].getSelection()));	
					}
					VisualiserPlugin.visualiser.updateDisplay(false);
				}
			}
			public void mouseDown(MouseEvent e) {
			}
			public void mouseDoubleClick(MouseEvent e) {
			}
		};
		makePullDownActions();
		contributeToActionBars();
		plugin.setMenu(this);
	}
	

	/**
	 * Create the actions for the view's pull down menu.
	 */
	private void makePullDownActions(){
		selectAllAction = new Action() {
			public int getStyle() {
				return IAction.AS_PUSH_BUTTON;
			}
			public void run() {
				showAll();
			}
		};
		selectAllAction.setText(VisualiserPlugin.getResourceString("Select_All_20")); //$NON-NLS-1$
		selectAllAction.setToolTipText(VisualiserPlugin.getResourceString("Select_All_20")); //$NON-NLS-1$		
		selectNoneAction = new Action() {
			public int getStyle() {
				return IAction.AS_PUSH_BUTTON;
			}
			public void run() {
				showNone();
			}
		};
		selectNoneAction.setText(VisualiserPlugin.getResourceString("Select_None_21")); //$NON-NLS-1$
		selectNoneAction.setToolTipText(VisualiserPlugin.getResourceString("Select_None_21")); //$NON-NLS-1$	
	}


	/**
	 * Adds actions to the action bars.
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
	}


	/**
	 * Adds actions to local pull down menu.
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(selectAllAction);
		manager.add(selectNoneAction);
	}


	/**
	 * Select all the checkboxes in the menu and update the visualiser
	 */
	private void showAll() {
		if(checkboxes!=null){
			for (int i = 0; i < checkboxes.length; i++) {
				checkboxes[i].setSelection(true);						
				kindActive.put((IMarkupKind)labels[i].getData(),new Boolean(checkboxes[i].getSelection()));
			}
			VisualiserPlugin.visualiser.draw();
		}
	}


	/**
	 * Deselect all of the checkboxes in the menu and update the visualiser
	 */
	private void showNone() {
		if(checkboxes!=null){
			for (int i = 0; i < checkboxes.length; i++) {
				checkboxes[i].setSelection(false);						
				kindActive.put((IMarkupKind)labels[i].getData(),new Boolean(false));
			}
			VisualiserPlugin.visualiser.draw();				
		}
	}


	/**
	 * Select the checkboxes whose names are in the given List then 
	 * update the visualiser
	 */
	protected void onlyShow(List names) {
		if(names==null){
			showNone();
		} else if(names.size()==0){
			showNone();
		} else if(names.size() == checkboxes.length){
			showAll();
		} else {
			for(int i=0; i<labels.length; i++){
				if(names.contains(labels[i].getText())){
					checkboxes[i].setSelection(true);
					kindActive.put((IMarkupKind)labels[i].getData(),new Boolean(true));
				} else {
					checkboxes[i].setSelection(false);
					kindActive.put((IMarkupKind)labels[i].getData(),new Boolean(false));
				}
			}
			VisualiserPlugin.visualiser.draw();
		}
	}
	

	/**
	 * Get the active state of a kind (i.e. is the checkbox checked).
	 * @param kind - the kind
	 * @return
	 */	
	public boolean getActive(IMarkupKind kind) {
		if (kindActive==null) return true;
		if(kindActive.get(kind) == null) return true;
		return ((Boolean)kindActive.get(kind)).booleanValue();
	}
	
	
	/**
	 * Get the active state of a kind by name(i.e. is the checkbox checked).
	 * @param kindName - the kind name
	 * @return
	 */
	public boolean getActive(String kindName) {
		if(kinds == null) {
			return true;
		} else {
			IMarkupKind kind = (IMarkupKind)kinds.get(kindName);
			if(kind == null) {
				return true;
			}
			if(kindActive.get(kind) == null) {
				return true;
			}
			return ((Boolean)kindActive.get(kind)).booleanValue();
		}
	}
	
	/**
	 * Reset the up-to-date state of this view
	 */
	public void reset() {
		uptodate = false;
	}


	/**
	 * The main method - adds aspect names to the menu.
	 */
	public void ensureUptodate() {
		if (uptodate) return;
		clear();
		Set markupKinds = vmp.getAllMarkupKinds();
		int numKindsToShow = getNumberToShow(markupKinds);
		if (markupKinds==null) return;
		buttons = new Button[numKindsToShow];
		checkboxes = new Button[numKindsToShow];
		labels = new Label[numKindsToShow];
		icons = new Label[numKindsToShow];
		shells = new Shell[numKindsToShow];
		colorSquares = new Image[numKindsToShow];
		colorDialogs = new ColorDialog[numKindsToShow];
		colors = new Color[numKindsToShow];
		
		
		kindActive = new Hashtable();
		kinds = new HashMap();
		
		int i = 0;
		for (Iterator iter = markupKinds.iterator(); iter.hasNext();) {
			IMarkupKind element = (IMarkupKind) iter.next();
			kinds.put(element.getName(), element);
			if(element.showInMenu()) {
				int imageSize = 12;
				colors[i] = vmp.getColorFor(element);
				buttons[i] = new Button(canvas, SWT.PUSH);
				shells[i] = buttons[i].getShell();
				colorDialogs[i] = new ColorDialog(shells[i]);
				Display display = shells[i].getDisplay();
				colorSquares[i] = new Image(display, imageSize, imageSize);
				buttons[i].setImage(colorSquares[i]);
				buttons[i].addSelectionListener(selectionListener);
				Image image = buttons[i].getImage();
				drawImage(image, colors[i]);
				buttons[i].setImage(image);
	
				checkboxes[i] = new Button(canvas, SWT.CHECK);
				checkboxes[i].addMouseListener(mouseListener);
				checkboxes[i].setSelection(true);

				icons[i] = new Label(canvas, SWT.NONE);
				icons[i].setImage(element.getIcon());
				labels[i] = new Label(canvas, SWT.NONE);
				labels[i].setText(element.getName());
				labels[i].setData(element);
				i++;
			}
			kindActive.put(element, new Boolean(true));
		}
		canvas.layout();
		canvas.setSize(canvas.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
		uptodate = true;
	}


	/**
	 * @param markupCategories
	 * @return
	 */
	private int getNumberToShow(Set markupCategories) {
		int num = 0;
		if (markupCategories == null) return 0;
		for (Iterator iter = markupCategories.iterator(); iter.hasNext();) {
			IMarkupKind element = (IMarkupKind) iter.next();
			if(element.showInMenu()) {
				num++;
			}
		}
		return num;
	}


	/**
	 * Private method to clear the menu.
	 */
	private void clear() {
		if(canvas!=null){
			Control[] children = canvas.getChildren();
			if (children.length > 0) {
				for (int i = 0; i < children.length; i++) {
					children[i].dispose();
				}
			}
		}
	}


	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if(scrollpane != null) {
			scrollpane.setFocus();
		}
	}

	
	/**
	 * Dispose of the menu when closed.
	 */
	public void dispose() {
		plugin.removeMenu();
	}

	
	/**
	 * Create the actions for this view
	 */
	private void makeActions() {

		IActionBars bars = getViewSite().getActionBars();
		IMenuManager manager = bars.getMenuManager();
//		IMenuManager filterSubmenu = new MenuManager("MarkerFilters");
//		manager.add(filterSubmenu);
//
//		for (int i = 0; i < this.relationships.length; i++) {
//			final String currRel = relationships[i];
//			Action action = new Action() {
//				public void run() {
//					/* Code for turning them on and off...
//						ViewerFilter filters[] = plugin.visualiser.getFilters();
//						boolean add = true;
//						for(int i=0;i < filters.length;i++) {
//							RelationshipFilter filter = (RelationshipFilter)filters[i];
//							if (filter.getRelationshipName().equals(currRel)) {
//								add = false;
//								setChecked(false);
//								plugin.visualiser.removeFilter(filter);
//								break;
//							}
//						}
//						
//						if (add) {
//							plugin.visualiser.addFilter(new RelationshipFilter(currRel));
//							setChecked(true);
//						} 				
//					*/
//				}
//			};
//
//			action.setText(relationships[i]);
//			action.setChecked(false);
//			action.setToolTipText(relationships[i]);
//			filterSubmenu.add(action);
//		}

		// Marker input dialog...
		Action Taction = new Action() {
			public void run() {
				markerTypesDialog();
			}
		};
		Taction.setText("Markers...");
		Taction.setToolTipText("Choose displayed markers");
		manager.add(Taction);
		
		groupByRel_Action = new Action() { public void run() {
			if (isChecked()) {
				//groupByRel_Action.setChecked(false);
				groupByQuery_Action.setChecked(false);
				psychedelic_Action.setChecked(false);
			}
			VisualiserPlugin.visualiser.updateDisplay(false);
		}};
		groupByRel_Action.setText("Group by relationship");
		groupByRel_Action.setChecked(true);
		groupByRel_Action.setToolTipText("Have one color per relationship type");
		manager.add(groupByRel_Action);
		
		groupByQuery_Action = new Action() { public void run() {
			if (isChecked()) {
				groupByRel_Action.setChecked(false);
				//groupByQuery_Action.setChecked(false);
				psychedelic_Action.setChecked(false);
			}
			VisualiserPlugin.visualiser.updateDisplay(false);
			
		}};
		groupByQuery_Action.setText("Group by query");
		groupByQuery_Action.setChecked(false);
		groupByQuery_Action.setToolTipText("Have one color per query");
		manager.add(groupByQuery_Action);
		
		psychedelic_Action = new Action() { public void run() {
			if (isChecked()) {
				groupByRel_Action.setChecked(false);
				groupByQuery_Action.setChecked(false);
				//psychedelic_Action.setChecked(false);
			}
			VisualiserPlugin.visualiser.updateDisplay(false);
		}};
		psychedelic_Action.setText("Psychedelic");
		psychedelic_Action.setChecked(false);
		psychedelic_Action.setToolTipText("Press it, you know you want to...");
		manager.add(psychedelic_Action);
	}
	
	private static Action groupByRel_Action;
	private static Action groupByQuery_Action;
	private static Action psychedelic_Action;

	public static boolean isPsychedelic() { return psychedelic_Action.isChecked();}
	
	public static boolean isColorPerRelationship() { return groupByRel_Action.isChecked();}
	
	public static boolean isColorPerQuery() { return groupByQuery_Action.isChecked();}

	public static String getMarkersToDisplay() {
		return markerTypes;
	}

  
    /**
     * Input dialog, allows user to decide which set of markers are displayed
     * in the vis menu and therefore in the vis.
     */
	private void markerTypesDialog() {
		// Which markers to respect?
		InputDialog dlg =
			new InputDialog(
				getSite().getShell(),
				"Displayed Markers",
				"Comma separated list of marker types (e.g. 'cmesearchmarker')",
				markerTypes,
				null);
		dlg.open();
		if (dlg.getReturnCode() == InputDialog.OK) {
			markerTypes = dlg.getValue();
			VisualiserPlugin.visualiser.updateDisplay(false);
		}
	}

}
