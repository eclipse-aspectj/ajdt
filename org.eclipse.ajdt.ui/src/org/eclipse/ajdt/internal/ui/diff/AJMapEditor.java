/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.diff;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * Editor for .ajmap files, which contain the serialized form of a
 * project's crosscutting structure. The editor doesn't do much at
 * the moment, it just shows a message. It could in future allow
 * for editing of associated metadata, such as notes etc.
 */
public class AJMapEditor extends EditorPart implements IPropertyChangeListener {

	private ScrolledComposite fScrolledComposite;
	private Color fBackgroundColor;
	private Color fForegroundColor;
	private Color fSeparatorColor;
	private List fBannerLabels= new ArrayList();
	private List fHeaderLabels= new ArrayList();
	private Font fFont;

	/** The horizontal scroll increment. */
	private static final int HORIZONTAL_SCROLL_INCREMENT= 10;
	/** The vertical scroll increment. */
	private static final int VERTICAL_SCROLL_INCREMENT= 10;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	public void doSaveAs() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite( site );
		setInput( input );
		setPartName(input.getName());
		setContentDescription(input.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		// appearance taken from ClassFileEditor.SourceAttachmentForm
		Display display= parent.getDisplay();
		fBackgroundColor= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		fForegroundColor= display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		fSeparatorColor= new Color(display, 152, 170, 203);

		JFaceResources.getFontRegistry().addListener(this);

		fScrolledComposite= new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		fScrolledComposite.setAlwaysShowScrollBars(false);
		fScrolledComposite.setExpandHorizontal(true);
		fScrolledComposite.setExpandVertical(true);
		fScrolledComposite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				JFaceResources.getFontRegistry().removeListener(AJMapEditor.this);
				fScrolledComposite= null;
				fSeparatorColor.dispose();
				fSeparatorColor= null;				
				fBannerLabels.clear();
				fHeaderLabels.clear();
				if (fFont != null) {
					fFont.dispose();
					fFont= null;
				}
			}
		});
		
		fScrolledComposite.addControlListener(new ControlListener() {
			public void controlMoved(ControlEvent e) {}

			public void controlResized(ControlEvent e) {
				Rectangle clientArea = fScrolledComposite.getClientArea();
				
				ScrollBar verticalBar= fScrolledComposite.getVerticalBar();
				verticalBar.setIncrement(VERTICAL_SCROLL_INCREMENT);
				verticalBar.setPageIncrement(clientArea.height - verticalBar.getIncrement());

				ScrollBar horizontalBar= fScrolledComposite.getHorizontalBar();
				horizontalBar.setIncrement(HORIZONTAL_SCROLL_INCREMENT);
				horizontalBar.setPageIncrement(clientArea.width - horizontalBar.getIncrement());
			}
		});
		
		Composite composite= createComposite(fScrolledComposite);
		composite.setLayout(new GridLayout());

		createTitleLabel(composite, AspectJUIPlugin.getResourceString("ajmapEditor.title")); //$NON-NLS-1$
		createLabel(composite, null);
		createLabel(composite, null);

		createHeadingLabel(composite, AspectJUIPlugin.getResourceString("ajmapEditor.heading")); //$NON-NLS-1$

		Composite separator= createCompositeSeparator(composite);
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint= 2;
		separator.setLayoutData(data);
		
		createLabel(composite, AspectJUIPlugin.getResourceString("ajmapEditor.description")); //$NON-NLS-1$
		
		separator= createCompositeSeparator(composite);
		data= new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint= 2;
		separator.setLayoutData(data);
		
		fScrolledComposite.setContent(composite);
		fScrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

	}

	private Composite createComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(fBackgroundColor);
		return composite;
	}
	
	private Composite createCompositeSeparator(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(fSeparatorColor);
		return composite;
	}
	
	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		if (text != null)
			label.setText(text);
		label.setBackground(fBackgroundColor);
		label.setForeground(fForegroundColor);
		return label;
	}

	private Label createTitleLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		if (text != null)
			label.setText(text);
		label.setBackground(fBackgroundColor);
		label.setForeground(fForegroundColor);
		label.setFont(JFaceResources.getHeaderFont());
		fHeaderLabels.add(label);
		return label;
	}

	private Label createHeadingLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		if (text != null)
			label.setText(text);
		label.setBackground(fBackgroundColor);
		label.setForeground(fForegroundColor);
		label.setFont(JFaceResources.getBannerFont());
		fBannerLabels.add(label);
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		fScrolledComposite.setFocus();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		for (Iterator iterator = fBannerLabels.iterator(); iterator.hasNext();) {
			Label label = (Label) iterator.next();
			label.setFont(JFaceResources.getBannerFont());
		}

		for (Iterator iterator = fHeaderLabels.iterator(); iterator.hasNext();) {
			Label label = (Label) iterator.next();
			label.setFont(JFaceResources.getHeaderFont());
		}

		Control control= fScrolledComposite.getContent();
		fScrolledComposite.setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		fScrolledComposite.setContent(control);
		
		fScrolledComposite.layout(true);
		fScrolledComposite.redraw();
	}


}
