/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.tracing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.internal.ui.help.AspectJUIHelp;
import org.eclipse.ajdt.internal.ui.help.IAJHelpContextIds;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 * Displays and configures debug tracing for AJDT
 */
public class EventTraceView extends ViewPart 
				implements EventTrace.EventListener {

	StyledText text;
	
	private ClearEventTraceAction clearEventTraceAction;
	
	private PrintCrossCuttingModelAction printModelAction;
	
	private FilterTraceAction filterAction;
	
	private Font font = JFaceResources.getFont(JFaceResources.TEXT_FONT);
	
	/**
	 * Constructor for AJDTEventTraceView.
	 */
	public EventTraceView() {
		super();
	}

	public void dispose( ) {		
		AspectJPreferences.setEventTraceList(
					filterAction.getCheckedList());		
		DebugTracing.setDebug(false);
		EventTrace.removeListener( this );
	}

	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		text = new StyledText( parent, SWT.MULTI | SWT.READ_ONLY | SWT.VERTICAL | SWT.HORIZONTAL );
        text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

		DebugTracing.setDebug(true);
		startup();
		EventTrace.addListener( this );

		makeActions();
		contributeToActionBars();
		
		// Add an empty ISelectionProvider so that this view works with dynamic help (bug 104331)
		getSite().setSelectionProvider(new ISelectionProvider() {
			public void addSelectionChangedListener(ISelectionChangedListener listener) {
			}
			public ISelection getSelection() {
				return null;
			}
			public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			}
			public void setSelection(ISelection selection) {
			}
		});
	}

	/**
	 * record version information & content of the preference store
	 */
	private void startup() {
		ajdtEvent(DebugTracing.startupInfo(),AJLog.DEFAULT, new Date());
	}
	
	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		text.setFocus();
	}

	public Object getAdapter(Class key) {
    	if (key.equals(IContextProvider.class)) {
    		return AspectJUIHelp.getHelpContextProvider(this, IAJHelpContextIds.EVENT_TRACE_VIEW);
    	}
    	return super.getAdapter(key);
    }

    public void ajdtEvent(String msg, final int category, Date time) {
		/*
		 * This code no longer dependent on either java.util.DateFormat, nor its ICU4J 
		 * version, while avoiding the deprecated methods in java.util.Date, hence the 
		 * slightly convoluted manner of extracting the time from the given date.
		 * 
		 * -spyoung
		 */
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(time);
		
		final String txt = calendar.get(Calendar.HOUR_OF_DAY) + ":"  //$NON-NLS-1$
			+ calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + " " + msg + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
			public void run() {
				appendEventText(txt, category);
			}
		});
	}

	private void appendEventText(String msg, int category) {
		IViewSite site = getViewSite();
		if (site == null) {
			return;
		}
		Shell shell = site.getShell();
		if (shell == null) {
			return;
		}
		Display display = shell.getDisplay();
		if (display == null) {
			return;
		}
		
		StyleRange styleRange = new StyleRange();
		styleRange.font = font;
		styleRange.start = text.getText().length();
		styleRange.length = msg.length();
		if (category==AJLog.BUILDER) {
			styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_BLUE);
		} else if (category==AJLog.BUILDER_CLASSPATH) {
			styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_RED);
		} else if ((category==AJLog.COMPILER) || (category==AJLog.COMPILER_PROGRESS)
				|| (category==AJLog.COMPILER_MESSAGES)) {
			styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_GREEN);
        } else if (category==AJLog.PARSER) {
            styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_CYAN);
        } else if (category==AJLog.MODEL) {
            styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_YELLOW);
		} else {
			styleRange.foreground = display.getSystemColor(SWT.COLOR_BLACK);
		}
		
		text.append( msg );
		text.setStyleRange(styleRange);	
        text.setTopIndex(text.getLineCount() - 1);
	}
	
	private void makeActions() {
        clearEventTraceAction = new ClearEventTraceAction(text);
        printModelAction = new PrintCrossCuttingModelAction();
		
		String dlogTitle = UIMessages.eventTrace_filter_dialog_title;
		String dlogMessage = UIMessages.eventTrace_filter_dialog_message;		
		List populatingList = Arrays.asList(DebugTracing.categoryNames);		

		List checkedList = AspectJPreferences.getEventTraceCheckedList();
		
		List defaultList = new ArrayList();
		defaultList.add(DebugTracing.categoryNames[0]);
	    defaultList.add(DebugTracing.categoryNames[3]);
		
	    if (checkedList == null){
	    	checkedList = new ArrayList(defaultList);
	    }
	    DebugTracing.setDebugCategories(checkedList);
	    
		filterAction = new FilterTraceAction(getSite().getShell(),
				populatingList, checkedList, defaultList, dlogTitle,
				dlogMessage, UIMessages.eventTrace_filter_action_tooltip);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
	    manager.add(clearEventTraceAction);
	    manager.add(printModelAction);
        filterAction.fillActionBars(getViewSite().getActionBars());
	}
}
