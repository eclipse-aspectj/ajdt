/*********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html 
 * Contributors: 
 * 
 * Sian Whiting -  initial version.
 **********************************************************************/
package org.eclipse.ajdt.ui.visualiser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.contribution.visualiser.core.ProviderManager;
import org.eclipse.contribution.visualiser.core.Stripe;
import org.eclipse.contribution.visualiser.interfaces.IMarkupKind;
import org.eclipse.contribution.visualiser.interfaces.IMember;
import org.eclipse.contribution.visualiser.jdtImpl.JDTMember;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleMarkupKind;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleMarkupProvider;
import org.eclipse.contribution.visualiser.simpleImpl.StealthMarkupKind;
import org.eclipse.contribution.visualiser.utils.JDTUtils;
import org.eclipse.contribution.visualiser.utils.MarkupUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IActionBars;

/**
 * @author Sian
 */
public class AJDTMarkupProvider extends SimpleMarkupProvider {
	
	private IAction hideErrorsAction;
	private Action hideWarningsAction;
	private boolean hideErrors;
	private boolean hideWarnings;
	private Map kindMap;
	protected static final String aspectJErrorKind = "AspectJ Error";
	protected static final String aspectJWarningKind = "AspectJ Warning";
	protected Map savedColours;
	
	private static Color aspectJErrorColor = new Color(null, new RGB(228,5,64));
	private static Color aspectJWarningColor = new Color(null, new RGB(255,206,90));
	private Action resetColorMemoryAction;
	private static String resetColorMemoryID = "ResetColorMemoryUniqueID32dfnio239"; 
	private IPreferenceStore prefs = AspectJUIPlugin.getDefault().getPreferenceStore();
	private static final String allPrefereceKeys = "AJDTVisualiserMarkupProvider.allPrefereceKeys";

	/**
	 * Get a List of Stripes for the given member, which are its markups.
	 */
	public List getMemberMarkups(IMember member) {
		List markupList = super.getMemberMarkups(member);
		if(markupList != null) {
			return checkErrorsAndWarnings(markupList); 
		}
		long stime = System.currentTimeMillis();
		List stripeList = new ArrayList();
		if(ProviderManager.getContentProvider() instanceof AJDTContentProvider) {
			IJavaProject jp = ((AJDTContentProvider)ProviderManager.getContentProvider()).getCurrentProject();
			if( jp != null) {
				List list = AJDTVisualiserUtils.getMarkupInfo(member, jp.getProject(), !hideErrors, !hideWarnings);
				for (Iterator iter = list.iterator(); iter.hasNext();) {
					Map map = (Map)iter.next();
					for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
						Integer lineNum = (Integer) iterator.next();
						List aspects = (List)map.get(lineNum);
						List kinds = new ArrayList();
						for (Iterator it = aspects.iterator(); it
								.hasNext();) {
							String name = (String) it.next();
							IMarkupKind markupKind = null;
							if(kindMap == null) {
								kindMap = new HashMap();
							}
							boolean errorKind = name.startsWith(aspectJErrorKind);
							if(errorKind || name.startsWith(aspectJWarningKind)) {
								if(kindMap.get(name) instanceof IMarkupKind) {
									markupKind = (IMarkupKind)kindMap.get(name);
								} else {
									markupKind = new ErrorOrWarningMarkupKind(name, errorKind);
									kindMap.put(name, markupKind);
								}
							} else {
								if(kindMap.get(name) instanceof IMarkupKind) {
									markupKind = (IMarkupKind)kindMap.get(name);
								} else {
									markupKind = new SimpleMarkupKind(name);
									kindMap.put(name, markupKind);
								}
							} 
							kinds.add(markupKind);
						}
						Stripe stripe = new Stripe(kinds, lineNum.intValue(), 1);
						stripeList.add(stripe);
						addMarkup(member.getFullname(), stripe);
					}
				}
			}
		}
		long mtime = System.currentTimeMillis();
		MarkupUtils.processStripes(stripeList);
		long etime = System.currentTimeMillis();
//		AJDTEventTrace.generalEvent(
//				"AJDTMarkupProvider.getMemberMarkups() executed (didn't hit cache) - took "+(etime-stime)+"ms "+
//				"("+(etime-mtime)+"ms stripe processing)");
		return checkErrorsAndWarnings(stripeList);
	}
	


	/**
	 * @param kinds
	 * @return
	 */
	private List checkErrorsAndWarnings(List stripes) {
		List returningStripes = new ArrayList();
		for (Iterator iter = stripes.iterator(); iter.hasNext();) {
			Stripe stripe = (Stripe) iter.next();
			List kinds = new ArrayList(stripe.getKinds());
			for (Iterator iterator = kinds.iterator(); iterator.hasNext();) {
				IMarkupKind kind = (IMarkupKind) iterator.next();
				if(kind instanceof StealthMarkupKind) {
					String name = kind.getName();
					String[] parts = name.split(":::");
					if(parts.length > 1) {
						String aspectName = parts[1];
						if(VisualiserPlugin.menu != null) {
							if(!VisualiserPlugin.menu.getActive(aspectName)) {
								iterator.remove();
							}
						}
					}
				}
			}
			Stripe newStripe = new Stripe(kinds, stripe.getOffset(), stripe.getDepth());
			returningStripes.add(newStripe);
		}
		return returningStripes;
	}


	/**
	 * Get the colour for a given kind
	 * @param id - the kind
	 * @return the Color for that kind
	 */
	public Color getColorFor(IMarkupKind id){
		if(id.getName().startsWith(aspectJErrorKind)) {
			return aspectJErrorColor;
		} else if (id.getName().startsWith(aspectJWarningKind)) {
			return aspectJWarningColor;
		} else {
			Color savedColor = getSavedColour(id.getName());
			if(savedColor == null) {
				return super.getColorFor(id);
			} else {
				return savedColor;
			}
		}
	}
	
	
	/**
	 * Set the color for a kind.
	 * @param string - the kind
	 * @param color - the Color
	 */
	public void setColorFor(IMarkupKind kind, Color color) {
		super.setColorFor(kind, color);
		saveColourForAspect(kind.getName(), color.getRed(), color.getGreen(), color.getBlue());
	}
	
	/**
	 * Get all the markup kinds.
	 * @return a Set of Strings
	 */
	public SortedSet getAllMarkupKinds() {
		TreeSet kinds = new TreeSet(); 
		if(ProviderManager.getContentProvider() instanceof AJDTContentProvider) {
			IJavaProject jp = ((AJDTContentProvider)ProviderManager.getContentProvider()).getCurrentProject();
			if( jp != null) {
				Set aspects = AJDTVisualiserUtils.getAllAspects(jp,true); 
				for (Iterator iter = aspects.iterator(); iter.hasNext();) {
					String name = (String)iter.next();
					int lastSlash = name.lastIndexOf("/");
					if (lastSlash == -1) lastSlash = name.lastIndexOf("\\");
					name = name.substring(lastSlash+1);
					name = name.substring(0, name.lastIndexOf("."));
					IMarkupKind markupKind;
					if(kindMap == null) {
						kindMap = new HashMap();
					}
					if(kindMap.get(name) instanceof IMarkupKind) {
						markupKind = (IMarkupKind)kindMap.get(name);
					} else {
						markupKind = new SimpleMarkupKind(name);
						kindMap.put(name, markupKind);
					}
					kinds.add(markupKind);					
				}

				if(!hideErrors) {
					if(kindMap.get(aspectJErrorKind) instanceof IMarkupKind) {
						kinds.add((IMarkupKind)kindMap.get(aspectJErrorKind));
					} else {
						IMarkupKind errorKind = new StealthMarkupKind(aspectJErrorKind);
						kinds.add(errorKind);
						kindMap.put(aspectJErrorKind, errorKind);
					}
				}
				if(!hideWarnings) {
					if(kindMap.get(aspectJWarningKind) instanceof IMarkupKind) {
						kinds.add((IMarkupKind)kindMap.get(aspectJWarningKind));
					} else {
						IMarkupKind warningKind = new StealthMarkupKind(aspectJWarningKind);
						kinds.add(warningKind);
						kindMap.put(aspectJWarningKind, warningKind);
					}
				}
			}
		}
		if (kinds.size() > 0) {
			return kinds;
		} else {
			return null;
		}
	}

	
	/**
	 * Process a mouse click on a stripe.  This method opens the editor at the line of the stripe clicked.
	 * @see org.eclipse.contribution.visualiser.interfaces.IMarkupProvider#processMouseclick(java.lang.String, org.eclipse.contribution.visualiser.core.Stripe, int)
	 */
	public boolean processMouseclick(IMember member, Stripe stripe, int buttonClicked) {
		if (buttonClicked == 1) {
			if (member instanceof JDTMember) {
				IJavaElement jEl = ((JDTMember)member).getResource();
				if (jEl != null) {
					JDTUtils.openInEditor(jEl.getResource(), stripe.getOffset());
				}
			}
			return false;
		} else {
			return true;
		}
	}
	
		
	protected void saveColourForAspect(String aspectName, int r, int g, int b) {
		if(ProviderManager.getContentProvider() instanceof AJDTContentProvider) {
			IProject currentProject = ((AJDTContentProvider)ProviderManager.getContentProvider()).getCurrentProject().getProject();
			String key = currentProject + ":::" + aspectName;
			if(prefs.getString(key) == null) {
				prefs.setDefault(key, "");
			}
			
			// Save all keys we add in order that they can be reset.
			String allColourKeys = prefs.getString(allPrefereceKeys);
			if(allColourKeys == null) {
				prefs.putValue(allPrefereceKeys, key);
				prefs.setDefault(allPrefereceKeys, "");
			} else {
				allColourKeys += "," + key;
				prefs.putValue(allPrefereceKeys, allColourKeys);
			}
			
			String value = r + "," + g + "," + b;
			prefs.setValue(key, value);
			if(savedColours == null) {
				savedColours = new HashMap();
			}
			savedColours.remove(key);
			savedColours.put(key, value);
		}
	}
	
	
	protected Color getSavedColour(String aspectName) {
		if(ProviderManager.getContentProvider() instanceof AJDTContentProvider) {
			IProject currentProject = ((AJDTContentProvider)ProviderManager.getContentProvider()).getCurrentProject().getProject();
			String key = currentProject + ":::" + aspectName;
			if(savedColours == null) {
				savedColours = new HashMap();
			}
			String value = (String)savedColours.get(key);
			if(value == null) {
				IPreferenceStore prefs = AspectJUIPlugin.getDefault().getPreferenceStore();
				value = prefs.getString(key); 
				savedColours.put(key, value);
			}
			if(value != null && value != "") {
				String[] rgb = value.split(",");
				if(rgb.length != 3) {
					return null;
				} else {
					int r = Integer.parseInt(rgb[0]);
					int g = Integer.parseInt(rgb[1]);
					int b = Integer.parseInt(rgb[2]);
					return new Color(null, new RGB(r,g,b));
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Activate the markup provider. 
	 */
	public void activate() { 
		if(VisualiserPlugin.menu != null) {
			resetColorMemoryAction = new Action() {
				public void run() {
					resetSavedColours();
					VisualiserPlugin.refresh();
				}

			};
			resetColorMemoryAction.setImageDescriptor(AspectJImages.RESET_COLOURS.getImageDescriptor());
			resetColorMemoryAction.setText(AspectJUIPlugin.getResourceString("ResetColorMemory"));
			resetColorMemoryAction.setId(resetColorMemoryID);
			hideErrorsAction = new Action() {
				public int getStyle() {
					return IAction.AS_CHECK_BOX;
				}
				public void run() {
					hideErrors = hideErrorsAction.isChecked();
					resetMarkupsAndKinds();
					VisualiserPlugin.refresh();
				}
			};
			hideErrorsAction.setImageDescriptor(AspectJImages.HIDE_ERRORS.getImageDescriptor());
			hideErrorsAction.setToolTipText(AspectJUIPlugin.getResourceString("HideErrors"));
			hideWarningsAction = new Action() {
				public int getStyle() {
					return IAction.AS_CHECK_BOX;
				}
				public void run() {
					hideWarnings = hideWarningsAction.isChecked();
					resetMarkupsAndKinds();
					VisualiserPlugin.refresh();
				}
			};
			hideWarningsAction.setImageDescriptor(AspectJImages.HIDE_WARNINGS.getImageDescriptor());
			hideWarningsAction.setToolTipText(AspectJUIPlugin.getResourceString("HideWarnings"));
			IActionBars menuActionBars = VisualiserPlugin.menu.getViewSite().getActionBars();
			IToolBarManager toolBarManager = menuActionBars.getToolBarManager();
			toolBarManager.add(hideErrorsAction);
			toolBarManager.add(hideWarningsAction);
			toolBarManager.update(true);
			IMenuManager menuManager = menuActionBars.getMenuManager();
			menuManager.add(new Separator());
			menuManager.add(resetColorMemoryAction);
		}
	}
	
	/**
	 * Reset all the saved colours
	 */
	protected void resetSavedColours() {
		String colourKeys = prefs.getString(allPrefereceKeys);
		if(colourKeys != null && colourKeys != "") {
			String[] keys = colourKeys.split(",");
			for (int i = 0; i < keys.length; i++) {
				prefs.setToDefault(keys[i]);
			}
			prefs.setToDefault(allPrefereceKeys);
		}
		savedColours = new HashMap();
		super.resetColours();
	}



	/**
	 * Deactivate
	 */
	public void deactivate() {
		super.deactivate();
		if(VisualiserPlugin.menu != null) {
			IActionBars menuActionBars = VisualiserPlugin.menu.getViewSite().getActionBars();
			IToolBarManager toolBarManager = menuActionBars.getToolBarManager();
//			IContributionItem[] contributions = toolBarManager.getItems();
			toolBarManager.removeAll();
			toolBarManager.update(true);
			IMenuManager menuManager = menuActionBars.getMenuManager();
			menuManager.remove(resetColorMemoryID);
			menuManager.update(true);
		}		
	}
	
	private class ErrorOrWarningMarkupKind extends StealthMarkupKind {
		
		private boolean errorKind;
		private String declaringAspect;
		
		/**
		 * @param name
		 */
		public ErrorOrWarningMarkupKind(String name, boolean errorKind) {
			super(name);
			this.errorKind = errorKind;
			String[] nameParts = name.split(":::");
			if(nameParts.length > 1) {
				declaringAspect = nameParts[1];
			}
		}

		public String toString() {
			if (errorKind) {
				return AspectJUIPlugin.getResourceString("AspectJError") + ": " + declaringAspect;
			} else {
				return AspectJUIPlugin.getResourceString("AspectJWarning") + ": " + declaringAspect;
			}
		}
	}
}
