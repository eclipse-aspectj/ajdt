/*******************************************************************************
 * Copyright (c) 2002 - 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sian Whiting - initial version and later features
 *     Andy Clement - refactored for stand-alone visualiser
 *******************************************************************************/
package org.eclipse.contribution.visualiser.views.old;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.contribution.visualiser.core.ProviderManager;
import org.eclipse.contribution.visualiser.core.Stripe;
import org.eclipse.contribution.visualiser.core.resources.VisualiserImages;
import org.eclipse.contribution.visualiser.interfaces.IContentProvider;
import org.eclipse.contribution.visualiser.interfaces.IGroup;
import org.eclipse.contribution.visualiser.interfaces.IMarkupKind;
import org.eclipse.contribution.visualiser.interfaces.IMarkupProvider;
import org.eclipse.contribution.visualiser.interfaces.IMember;
import org.eclipse.contribution.visualiser.internal.preference.VisualiserPreferences;
import org.eclipse.contribution.visualiser.internal.preference.VisualiserPreferencesDialog;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;


/**
 * This class represents the main view of the Visualiser.
 */
public class Visualiser extends ViewPart {

	private Action lockAction;
	private static VisualiserPlugin plugin = VisualiserPlugin.getDefault();
	private static BarDrawing barDrawing = BarDrawing.getBarDrawing();

	private static Action groupViewAction;
	private static Action memberViewAction;
//	private static Action subselectAction;
	private Action absoluteProportionsAction;
	private Action doubleClickAction;
	private Action limitAction;
	private Action zoomInAction;
	private Action zoomOutAction;
	private Action changeStripeModeAction;
	private Action preferencesAction;
	
	private static ImageDescriptor groupViewImage = VisualiserImages.GROUP_VIEW;
	private static ImageDescriptor memberViewImage = VisualiserImages.MEMBER_VIEW;

	public boolean justOpenedFile = false;
	
	private static int viewMode = 0;
	private static final int VIEWMODE_GROUPS = 0;
	private static final int VIEWMODE_GROUP = 1;

	private static final int VIEWMODE_MEMBERS = 2;

	protected static IContentProvider contentP;
	protected static IMarkupProvider markupP;

	private static List data = null;
//	private static List subselection = null;
	private static boolean inGroupView      = false;
//	private static boolean in_sub_select_mode = false;
	private static boolean inLimitMode = false;
	private static boolean absoluteProportions = false;
	private static boolean locked = false;
	private static Canvas canvas;
	private static ScrolledComposite container;

	private Vector selectedElements;
	private Vector annotations;
	private Color[] colors;
	private org.eclipse.swt.graphics.Rectangle viewsize;
	private static Point containerSize;

	/**
	 * The constructor. 
	 */
	public Visualiser() {
		plugin.setVisualiser(this);
		updateSettingsFromPreferences();
	}

	
	/**
	 * This is a callback that will allow us
	 * to create the view and initialize it.
	 */
	public void createPartControl(Composite parent) {
		try {	
			getSite().getPage().showView("org.eclipse.contribution.visualiser.views.Menu");
		} catch (PartInitException pie) {
			System.err.println("Error initialising Visualiser Menu");
		}
		container = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		container.setAlwaysShowScrollBars(false);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		containerSize = container.getSize();
		canvas = new Canvas(container, SWT.NONE);
		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setContent(canvas);
		selectedElements = new Vector();
		annotations = new Vector();
		makeActions();
		memberViewAction.setChecked(true);
		zoomOutAction.setEnabled(false);
		contributeToActionBars();

		/* Resize listener - redraws when resized. */
		container.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				updateVisUI();
			}
		});
		
		String title = ProviderManager.getCurrent().getTitle();
		refreshTitle(title); 			
		data = contentP.getAllMembers();
		
		updateDisplay(true);
	}

	
	/**
	 * Called by Menu when the colour selections or aspects selection
	 * in the menu has changed.
	 */
	public void draw() {
		updateVisUI();
	}
	

	/**
	 * Update drawing options from preference store
	 *
	 */
	public void updateSettingsFromPreferences() {
		setDemarcation(VisualiserPreferences.isDemarcationEnabled());
		setMinStripeSize(VisualiserPreferences.getStripeSize());
		setMaxBarSize(VisualiserPreferences.getMaxBarSize());
		setMinBarSize(VisualiserPreferences.getMinBarSize());
	}
	
	/**
	 * Refresh the title.  Sets the view's title to 'Visualiser - ' plus the argument
	 * @param title
	 */
	public void refreshTitle(String title) {
		//this.setTitle(Messages.getString("Visualiser") + " - " + title);
		this.setContentDescription(VisualiserPlugin.getResourceString("Visualiser") + " - " + title);
	}
	
	
	/**
	 * Set whether or not to show demarcation on bars in group view
	 * @param demarcation
	 */
	public void setDemarcation(boolean demarcation){
		barDrawing.setDemarcation(demarcation, canvas);
	}


	/**
	 * Set the minimum height of stripes in the visualisation
	 * @param size
	 */
	public void setMinStripeSize(int size) {
		barDrawing.highlightDepth = size;
		if(canvas!=null){		
			draw();
		}
	}


	/**
	 * Set the maximum bar width for the view in pixels
	 * @param size
	 */
	public void setMaxBarSize(int size) {
		barDrawing.maxBarWidth = size;
		draw();
	}

	
	/**
	 * Set the minimum bar width for the view in pixels
	 * @param size
	 */
	public void setMinBarSize(int size) {
		barDrawing.minBarWidth = size;
		draw();
	}


	/**
	 * Update the display
	 */
	public void updateDisplay(boolean updateMenu) {
		log(3,"Entering updateDisplay");
//			if (in_sub_select_mode) {
//				data = subselection;
//			} else 
		if(!locked){
			if (inGroupView) {
				//activateGroupView(); // Just to make sure
				if (viewMode == VIEWMODE_GROUP) {
					data =
						(contentP
							.getAllGroups());
				} else if (viewMode == VIEWMODE_GROUPS) {
					data =
						(contentP
							.getAllGroups());
				} else if (viewMode == VIEWMODE_MEMBERS) {
					data =
						(contentP
							.getAllGroups());
				}
			} else if (!(inGroupView)) { // We are in member view
			//	activateMemberView(); // Just to make sure
				if (viewMode == VIEWMODE_GROUP) {
					data =
						(contentP
							.getAllMembers());
				} else if (viewMode == VIEWMODE_GROUPS) {
					data =
						(contentP
							.getAllMembers());
				} else if (viewMode == VIEWMODE_MEMBERS) {
					data =
						(contentP
							.getAllMembers());
				}
			}
		}
		if (VisualiserPlugin.menu != null && updateMenu){
			VisualiserPlugin.menu.reset();
			VisualiserPlugin.menu.ensureUptodate();
		}	
		updateVisUI();
	}

	
	/**
	 * Shorten the input data to only those bars that have active kinds.
	 */
	private static List limitData(List data) {
		if (data==null) return null;
		log(3,"In limit processing: Input size: "+data.size());
		List activeBars = new ArrayList();
		for (Iterator iter = data.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof IGroup) {
				IGroup aGroup = (IGroup) element;
				List stripes = markupP.getGroupMarkups(aGroup);
				if (containsActiveStripe(stripes))
					activeBars.add(element);
			} else {
				IMember aMember = (IMember) element;
				List stripes = markupP.getMemberMarkups(aMember);
				if (containsActiveStripe(stripes)) activeBars.add(element);
			}
		}
		log(3,"Finished limit processing: Output size: "+activeBars.size());
		return activeBars;
	}

	private static boolean containsActiveStripe(List stripes) {
		if (stripes==null) return false;
		// Go through the stripes in the list
		for (Iterator iter = stripes.iterator(); iter.hasNext();) {
			Stripe element = (Stripe) iter.next();
			List kinds = element.getKinds();
			// Go through the kinds in each stripe
			for (Iterator iterator = kinds.iterator(); iterator.hasNext();) {
				IMarkupKind kind = (IMarkupKind) iterator.next();
				// If any kind is active, return true
				if(VisualiserPlugin.menu == null) {
					// If menu is null we assume all kinds are active
					return true;
				} else if (VisualiserPlugin.menu.getActive(kind)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * DEBUG utility method.
	 */
	//	private void dumpMapPackage() {
	//		log("Dumping map...");
	//		for (int i = 0;i<contentToDisplay.length;i++) {
	//			
	//		
	//		Set keys = contentToDisplay[i].keySet();
	//		Iterator ki = keys.iterator();
	//		while (ki.hasNext()) {
	//			Integer line = (Integer)ki.next();
	//			List anns = (List) contentToDisplay[i].get(line);
	//			Iterator ai = anns.iterator();
	//			while (ai.hasNext()) {
	//				Annotation a = (Annotation)ai.next();
	//				log("MENTRY("+i+") LINE("+line+") ANNOTATION("+a.getFile()+","+a.getLine()+")");
	//			}
	//		}
	//		}
	//		
	//	}


	/**
	 * Update the UI with the contents of the List parameter
	 */
	private static void updateVisUI() {
		if (inLimitMode) {
			barDrawing.draw(limitData(data), canvas, absoluteProportions);
		} else {
			barDrawing.draw(data, canvas, absoluteProportions);
		}
	}

    
    /**
     * If the current log-level is greater than or equal to the level given,
     * log the message.
     * @param level - the level
     * @param string - the message
     */
	private static void log(int level, String string) {
		if (level<=VisualiserPlugin.LOGLEVEL) System.err.println(string);
	}


	/**
	 * Private method to get a marker for a source file given it's location
	 * in the vector elements.
	 */
	private IMarker getMarker(int location) {
		IMarker marker = null;
		if (selectedElements.elementAt(location) instanceof IResource) {
			IResource ir = (IResource) selectedElements.elementAt(location);
			if (ir != null) {
				try {
					marker = ir.createMarker(IMarker.MARKER);
				} catch (CoreException coreEx) {
					System.err.println(coreEx);
				}
			}
		}
		return marker;
	}


	/**
	 * Private method to get a marker for a source file given location and required line number.
	 */
	private IMarker getMarker(int location, int lineNum) {

		IMarker marker = null;

		if (selectedElements.elementAt(location) instanceof IResource) {
			IResource ir = (IResource) selectedElements.elementAt(location);
			if (ir != null) {
				try {
					marker = ir.createMarker(IMarker.MARKER);
					marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
				} catch (CoreException coreEx) {
					System.err.println(coreEx);
				}
			}
		}
		return marker;
	}


	/**
	 * Adds actions to the action bar.
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		boolean includeChangeStripeModeAction = false;
		if(markupP != null) {
			includeChangeStripeModeAction = markupP.hasMultipleModes();
		}
		fillLocalToolBar(bars.getToolBarManager(), includeChangeStripeModeAction);
	}


	/**
	 * Adds actions to local pull down menu.
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(preferencesAction);
		manager.add(new Separator());
		manager.add(limitAction);
		manager.add(new Separator());
		manager.add(groupViewAction);
		manager.add(memberViewAction);
	}


	/**
	 * Called by contributeToActionBars to add actions to local tool bar.
	 */
	private void fillLocalToolBar(IToolBarManager manager, boolean includeChangeStripeMode) {
		manager.add(zoomInAction);
		manager.add(zoomOutAction);
		manager.add(new Separator());
		manager.add(lockAction);
		manager.add(new Separator());
		manager.add(absoluteProportionsAction);
//		manager.add(subselectAction);
		manager.add(new Separator());	
		if(includeChangeStripeMode) {
			manager.add(changeStripeModeAction);
			manager.add(new Separator());
		}
		manager.add(limitAction);
		manager.add(new Separator());	
		manager.add(groupViewAction);
		manager.add(memberViewAction);
	}


	/**
	 * Private method called by createPartControl to create and initialise actions.
	 */
	private void makeActions() {
		makeActionPreferences();
		makeActionChangeStripeMode();
		makeActionZoomin();
		makeActionZoomout();
		makeActionLimitvis();
		makeActionGroupView();
		makeActionMemberView();
//		makeActionSubselect();
		makeActionAbsoluteProportions();
		makeActionLock();
	}


	/**
	 * Creates the action which displays the visualiser preferences
	 */
	private void makeActionPreferences() {
		preferencesAction = new Action() {
			public void run() {
				VisualiserPreferencesDialog vpd = new VisualiserPreferencesDialog(new Shell());
				vpd.create();
				vpd.open();
			}
		};
		preferencesAction.setText(VisualiserPlugin.getResourceString("Preferences_24"));
		preferencesAction.setToolTipText(VisualiserPlugin.getResourceString("Preferences_Tip_25"));
		preferencesAction.setImageDescriptor(VisualiserImages.PREFERENCES);
	}


	/**
	 * Creates the actions that specifies whether or not the drawing 
	 * should use absolute proportions
	 */
	private void makeActionAbsoluteProportions() {
		absoluteProportionsAction = new Action() {
			public void run() {
				absoluteProportions = !absoluteProportions;
				updateVisUI();
				zoomInAction.setEnabled(!absoluteProportions);
				zoomOutAction.setEnabled(!absoluteProportions);
			}
			
			public int getStyle() {
				return IAction.AS_CHECK_BOX;
			}
		};
		absoluteProportionsAction.setText(VisualiserPlugin.getResourceString("Absolute_Proportions"));
		absoluteProportionsAction.setToolTipText(VisualiserPlugin.getResourceString("Absolute_Proportions"));
		absoluteProportionsAction.setImageDescriptor(VisualiserImages.ABSOLUTE_PROPORTIONS);
	}
	

	/**
	 * Create the action which changes the markup provider's mode
	 */
	private void makeActionChangeStripeMode() {
		changeStripeModeAction = new Action() {
			public void run() {
				if(markupP.changeMode()){
					updateDisplay(true);
				}
			}
		};
		changeStripeModeAction.setText(VisualiserPlugin.getResourceString("Change_Mode_22")); //$NON-NLS-1$
		changeStripeModeAction.setToolTipText(VisualiserPlugin.getResourceString("Change_Mode_23")); //$NON-NLS-1$
		ImageDescriptor changeID = VisualiserImages.CHANGE_STRIPE_MODE;
		changeStripeModeAction.setImageDescriptor(changeID);
	}


//	/**
//	 * Build the action for the subselect operation.
//	 */
//	private void makeActionSubselect() {
//		subselectAction = new Action() {
//			public int getStyle() {
//				return IAction.AS_CHECK_BOX;
//			}
//			public void run() {
//				subselection = aspectDrawing.subSelect();
//				if (subselection.size() != 0) {
//					activateSubSelectMode();
//				} else {
//					deactivateSubSelectMode();
//				}
//				updateDisplay(false);
//			}
//		};
//
//		subselectAction.setText(Messages.getString("Subselect_18")); //$NON-NLS-1$
//		subselectAction.setToolTipText(Messages.getString("Subselects_chosen_packages/classes_19")); //$NON-NLS-1$
//		ImageDescriptor subselectID;
//		subselectID = ImageDescriptor.createFromFile(this.getClass(), "subselect.gif"); //$NON-NLS-1$
//		subselectAction.setImageDescriptor(subselectID);
//	}


	/**
	 * Make the action which switches the view to member mode
	 */
	private void makeActionMemberView() {
		memberViewAction = new Action() {
			public int getStyle() {
				return IAction.AS_CHECK_BOX;
			}
			public void run() {
				activateMemberView();
				updateDisplay(false);
			}
		};
		memberViewAction.setText(VisualiserPlugin.getResourceString("Class_View_15")); //$NON-NLS-1$
		memberViewAction.setToolTipText(VisualiserPlugin.getResourceString("Changes_to_member_view_16")); //$NON-NLS-1$
		memberViewAction.setImageDescriptor(memberViewImage);
	}
	
	
	/**
	 * Make the action which switches the view to group mode
	 */
	private void makeActionGroupView() {
		groupViewAction = new Action() {
			public int getStyle() {
				return IAction.AS_CHECK_BOX;
			}
			public void run() {
//				if (in_sub_select_mode) {
					if (!(inGroupView)) {

						setChecked(true);
						memberViewAction.setChecked(false);
						inGroupView = true;
					} else {
						setChecked(true);
					}
//				}
				activateGroupView();
				updateDisplay(false);
			}
		};
		groupViewAction.setText(VisualiserPlugin.getResourceString("Package_View_12")); //$NON-NLS-1$
		groupViewAction.setToolTipText(VisualiserPlugin.getResourceString("Changes_to_group_view_13")); //$NON-NLS-1$
		ImageDescriptor packageID;
		groupViewAction.setImageDescriptor(groupViewImage);
	}


	/**
	 * Makes the action which limits (and unlimits) the visualisation to 
	 * affected bars only 
	 */
	private void makeActionLimitvis() {
		limitAction = new Action() {
			public int getStyle() {
				return IAction.AS_CHECK_BOX;
			}
			public void run() {
				if (!inLimitMode)
					inLimitMode = true;
				else
					inLimitMode = false;
				
				setChecked(inLimitMode);
				updateDisplay(false); // aspectDrawing.limit(canvas, in_limit_mode);
			}
		};
		limitAction.setText(VisualiserPlugin.getResourceString("Limit_view_9")); //$NON-NLS-1$
		limitAction.setToolTipText(VisualiserPlugin.getResourceString("Limits_visualisation_to_affected_bars_only_10")); //$NON-NLS-1$
		limitAction.setImageDescriptor(VisualiserImages.LIMIT_MODE);
	}


	/**
	 * Make the zoom-out action
	 *
	 */
	private void makeActionZoomout() {
		zoomOutAction = new Action() {
			public void run() {
				boolean zoom = barDrawing.zoomOut(canvas);
				setEnabled(zoom);
				zoomInAction.setEnabled(true);
			}
		};
		zoomOutAction.setText(VisualiserPlugin.getResourceString("Zoom_Out_6")); //$NON-NLS-1$
		zoomOutAction.setToolTipText(VisualiserPlugin.getResourceString("Zooms_out_7")); //$NON-NLS-1$
		zoomOutAction.setImageDescriptor(VisualiserImages.ZOOM_OUT);
	}
		
	
	/**
	 * Make the zoom-in action
	 */
	private void makeActionZoomin() {
		zoomInAction = new Action() {
			public void run() {
				zoomOutAction.setEnabled(true);
				boolean zoom = barDrawing.zoomIn(canvas);
				setEnabled(zoom);
			}
		};
		zoomInAction.setText(VisualiserPlugin.getResourceString("Zoom_In_3")); //$NON-NLS-1$
		zoomInAction.setToolTipText(VisualiserPlugin.getResourceString("Zooms_in_on_visualisation_4")); //$NON-NLS-1$
		zoomInAction.setImageDescriptor(VisualiserImages.ZOOM_IN);
	}

	
	/**
	 * Make the lock action
	 */
	private void makeActionLock() {
		lockAction = new Action() {
			public void run() {
				locked = !locked;
				groupViewAction.setEnabled(!locked);
				memberViewAction.setEnabled(!locked);
//				subselectAction.setEnabled(!locked);
				if(!locked){
					updateDisplay(true);
				}
			}
			public int getStyle() {
				return IAction.AS_CHECK_BOX;
			}
		};
		lockAction.setText(VisualiserPlugin.getResourceString("Lock")); //$NON-NLS-1$
		lockAction.setToolTipText(VisualiserPlugin.getResourceString("Lock_Tooltip")); //$NON-NLS-1$
		lockAction.setImageDescriptor(VisualiserImages.LOCK);

	}
	
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if(container != null) {
			container.setFocus();
		}
	}

	
	/**
	 * Dispose of the Visualiser view when closed.
	 */
	public void dispose() {
		plugin.removeVisualiser();
		canvas = null;
	}


	/** 
	 * Responder to marker change events.
	 */
	//	public void resourceChanged(IResourceChangeEvent event) {		
	//		
	//		IResource res = event.getResource();
	//		if(event.getType() == IResourceChangeEvent.POST_CHANGE) {								
	//			   try {
	//					event.getDelta().accept(new IResourceDeltaVisitor() {
	//						public boolean visit(IResourceDelta delta) throws CoreException {
	//								IResource res = delta.getResource();
	//								 switch (delta.getKind()) {
	//									case IResourceDelta.CHANGED:
	//										  int flags = delta.getFlags();				 
	//										  if ((flags & IResourceDelta.MARKERS) != 0) {
	//												IMarkerDelta[] markers = delta.getMarkerDeltas();
	//						
	//												// if interested in markers, check these deltas
	//											    updateDisplay();																								
	//										  }
	//
	//										  break;
	//								 }
	//								 return true; 
	//							}
	//					});
	//			   } catch (CoreException ce) {
	//					ce.printStackTrace();			   
	//			   }					   
	//		 }
	//	}


	/**
	 * Set the current content provider
	 * @param vcp - the current IContentProvider
	 */
	public void setVisContentProvider(IContentProvider vcp) {
		contentP = vcp;
		memberViewImage = vcp.getMemberViewIcon() == null ? VisualiserImages.MEMBER_VIEW : vcp.getMemberViewIcon();
		groupViewImage = vcp.getGroupViewIcon() == null ? VisualiserImages.GROUP_VIEW : vcp.getGroupViewIcon();
		if(groupViewAction != null) {
			groupViewAction.setImageDescriptor(groupViewImage);
		}
		if (memberViewAction != null) {
			memberViewAction.setImageDescriptor(memberViewImage);
		}
	}
	
	
	/**
	 * Set the current markup provider
	 * @param vmp - the current IMarkupProvider
	 */
	public void setVisMarkupProvider(IMarkupProvider vmp) {
		markupP = vmp;
		if(markupP.hasMultipleModes()) {
			if(changeStripeModeAction == null) {
				makeActionChangeStripeMode();
				if(getViewSite() != null) {
					IActionBars bars = getViewSite().getActionBars();
					bars.getToolBarManager().removeAll();
					fillLocalToolBar(bars.getToolBarManager(), true);
					bars.updateActionBars();
				}
			}
		} else {
			if(changeStripeModeAction != null){		
				IActionBars bars = getViewSite().getActionBars();
				bars.getToolBarManager().removeAll();
				fillLocalToolBar(bars.getToolBarManager(), false);
				bars.updateActionBars();
				changeStripeModeAction = null;
			}
		}
	}


//	/**
//	 * Activate sub-select mode
//	 */
//	private static void activateSubSelectMode() {
//		in_sub_select_mode = true;
//		subselectAction.setChecked(true);
//	}


//	/**
//	 * De-activate sub-select mode
//	 */
//	private static void deactivateSubSelectMode() {
//		in_sub_select_mode = false;
//		subselectAction.setChecked(false);
//		subselection = null;
//	}


	/**
	 * Activate member mode
	 */
	private static void activateMemberView() {
		inGroupView = false;
//		deactivateSubSelectMode();
		memberViewAction.setChecked(true);
		groupViewAction.setChecked(false);
	}


	/**
	 * Activate group mode 
	 */
	private static void activateGroupView() {
		inGroupView = true;
//		deactivateSubSelectMode();
		memberViewAction.setChecked(false);
		groupViewAction.setChecked(true);
	}


	/**
	 * Only show kinds affecting the member or group with the given name
	 * @param name
	 */
	protected void onlyShowColorsAffecting(String name) {
		List names = new ArrayList();
		List members = contentP.getAllMembers();
		boolean found = false;
		for (Iterator it = members.iterator(); it.hasNext();) {
			IMember member = (IMember) it.next();
			if(member.getToolTip().equals(name)){
				found = true;
				List markups = markupP.getMemberMarkups(member);
				if(markups == null){
					VisualiserPlugin.menu.onlyShow(null);
					return;
				}
				for (Iterator it2 = markups.iterator(); it2.hasNext();) {
					Stripe stripe = (Stripe) it2.next();
					List kinds = stripe.getKinds();
					for (Iterator it3 = kinds.iterator(); it3.hasNext();) {
						IMarkupKind kind = (IMarkupKind) it3.next();
						if(!names.contains(kind.getName())){
							names.add(kind.getName());
						}
					}
				}
			}
		
		}
		if(!found){   // name is name of a group, not a member.
			List groups = contentP.getAllGroups();
			for (Iterator it = groups.iterator(); it.hasNext();) {
				IGroup group = (IGroup) it.next();
				if(group.getToolTip().equals(name)){
					List markups = markupP.getGroupMarkups(group);
					if(markups == null){
						VisualiserPlugin.menu.onlyShow(null);
						return;
					}
					for (Iterator it2 = markups.iterator(); it2.hasNext();) {
						Stripe stripe = (Stripe) it2.next();
						List kinds = stripe.getKinds();
						for (Iterator it3 = kinds.iterator(); it3.hasNext();) {
							IMarkupKind kind = (IMarkupKind) it3.next();
							if(!names.contains(kind.getName())){
								names.add(kind.getName());
							}
						}
					}
				}
			}
		}
		VisualiserPlugin.menu.onlyShow(names);
	}

	
	/**
	 * Handle a click that has occurred on the bar chart.
	 * 
	 * @param member
	 * @param kind
	 * @param stripe
	 * @param buttonClicked
	 */
	protected void handleClick(
		IMember member,
		String kind,
		Stripe stripe,
		int buttonClicked) {

		boolean proceed = contentP.processMouseclick((member!=null?member:null),(stripe!=null?true:false),buttonClicked);
		
		if (stripe!=null) 
		  proceed = markupP.processMouseclick((member!=null?member:null),stripe,kind,buttonClicked) && proceed;
		
		if (proceed) {
		
			if (buttonClicked != 3) { // Left hand or middle mouse button click
				stackContext();
				if (inGroupView) {
					IGroup grp = member.getContainingGroup();
					// IF
					//   someone has clicked on a group
					// THEN
					//   Switch to subselect mode, and to the member view showing all the members of that group
					activateMemberView();
//					activateSubSelectMode();
//					subselection = new ArrayList();
//					subselection.addAll(grp.getMembers());
				} else {
//					activateSubSelectMode();
//					subselection = new ArrayList();
//					subselection.add(member);
				}
				updateDisplay(false);
			} else { // Right hand button clicked
				unstackContext();
			}
		}

	}

	// TODO: Must invalidate the stack when someone changes the content of the view	
	private static Stack context = new Stack();

	/**
	 * Class containing context information for this view
	 */
	private static class ViewContext {
		private boolean groupview;
		private boolean subselect;
		private boolean limitmode;

		private List subselection;
		private List data;

		public boolean equals(ViewContext that) {
			if (this.groupview != that.groupview) return false;
			//if (this.subselect != that.subselect) return false;
			if (this.limitmode != that.limitmode) return false;
			if (this.subselection==null && that.subselection!=null) return false;
			if (that.subselection==null && this.subselection!=null) return false;
			if (this.subselection!=null) {
			  if (this.subselection.size()!=that.subselection.size()) return false;
			  for (int i = 0; i<this.subselection.size();i++) 
			    if (!this.subselection.get(i).equals(that.subselection.get(i))) return false;
			}	
		 	if (this.data==null && that.data!=null) return false;
		 	if (that.data==null && this.data!=null) return false;
		 	if (this.data!=null) {
		 	
			  if (this.data.size()!=that.data.size()) return false;
			  for (int i = 0; i<this.data.size();i++) 
			    if (!this.data.get(i).equals(that.data.get(i))) return false;
		 	}
			return true;
		}

		public void setGroupView(boolean in_group_view) {
			groupview = in_group_view;
		}
		public boolean getGroupView() {
			return groupview;
		}

		public void setSubselect(boolean in_sub_select_mode) {
			subselect = in_sub_select_mode;
		}
		public boolean getSubselect() {
			return subselect;
		}

		public void setSubselection(List subsel) {
			subselection = subsel;
		}
		public List getSubselection() {
			return subselection;
		}

		public void setData(List dta) {
			data = dta;
		}
		public List getData() {
			return data;
		}

		public void setLimitmode(boolean limit_mode) {
			limitmode = limit_mode;
		}
		public boolean getLimitMode() {
			return inLimitMode;
		}

	}

	private static void stackContext() {
		// Push all information we have about the 'current' visview onto the stack
		ViewContext vctx = new ViewContext();
		vctx.setGroupView(inGroupView);
//		vctx.setSubselect(in_sub_select_mode);
		vctx.setLimitmode(inLimitMode);
//		vctx.setSubselection(subselection);
		vctx.setData(data);
		if (context.size()==0) context.push(vctx);
		else if (!vctx.equals((ViewContext)context.peek())) context.push(vctx);
	}

	private static void unstackContext() {
		if (context.size() == 0)
			return;
		ViewContext vctx = (ViewContext) context.pop();
		inGroupView = vctx.getGroupView();
//		in_sub_select_mode = vctx.getSubselect();
		inLimitMode = vctx.getLimitMode();
//		subselection = vctx.getSubselection();
		data = vctx.getData();
	}

	public void resetAbsoluteProportions() {
		absoluteProportions = false;
		absoluteProportionsAction.setChecked(false);
		zoomInAction.setEnabled(true);
		zoomOutAction.setEnabled(true);
	}
	
}