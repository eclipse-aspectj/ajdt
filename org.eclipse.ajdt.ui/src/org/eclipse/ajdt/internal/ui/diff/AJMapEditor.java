/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.diff;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.model.AJProjectModel;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.icu.text.DateFormat;

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

		createTitleLabel(composite, UIMessages.ajmapEditor_title);
		createLabel(composite, null);
		createLabel(composite, null);

		createHeadingLabel(composite, UIMessages.ajmapEditor_heading);

		createSeparator(composite);
		
		createText(composite, UIMessages.ajmapEditor_description);
		
		createSeparator(composite);
		
		createLabel(composite, null);
		createLabel(composite, null);

		createHeadingLabel(composite, UIMessages.ajmapEditor_info_heading);
		
		createSeparator(composite);

		String info = getMapInfo();
		if (info != null) {
			createText(composite, info);
			createSeparator(composite);
		}
		
		fScrolledComposite.setContent(composite);
		fScrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

	}

	private void createSeparator(Composite parent) {
		Composite separator= createCompositeSeparator(parent);
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint= 2;
		separator.setLayoutData(data);
	}
	
	private String getMapInfo() {
		IEditorInput input = getEditorInput();
		if ((input != null) && (input instanceof FileEditorInput)) {
			IFile file = ((FileEditorInput)input).getFile();
			return getInfoForModelFile(file.getProject(), file.getLocation());
		}
		return null;
	}
	
	private String getInfoForModelFile(IProject project, IPath path) {
		AJProjectModel model = new AJProjectModel(project);
		boolean worked = model.loadModel(path);
		if (!worked) {
			return null;
		}
		if (!path.toFile().exists()) {
			return null;
		}
		StringBuffer info = new StringBuffer();
		long lastMod = path.toFile().lastModified();
		if (lastMod > 0) {
			String lastModStr = DateFormat.getDateTimeInstance().format(
					new Date(lastMod));
			info.append(NLS.bind(UIMessages.ajmapEditor_last_mod_date,
					lastModStr));
			info.append("\n"); //$NON-NLS-1$
		}
		try {
			FileInputStream fis = new FileInputStream(path.toFile());
			ObjectInputStream ois = new ObjectInputStream(fis);
			int version = ois.readInt();
			ois.close();
			fis.close();
			info.append(NLS.bind(UIMessages.ajmapEditor_file_version, ""
					+ version));
			info.append("\n"); //$NON-NLS-1$
		} catch (IOException e) {
		}
		info.append("\n"); //$NON-NLS-1$
		info.append(UIMessages.ajmapEditor_rel_heading);
		info.append("\n"); //$NON-NLS-1$
		AJRelationshipType[] relTypes = AJRelationshipManager
				.getAllRelationshipTypes();
		int total = 0;
		for (int i = 0; i < relTypes.length; i++) {
			int n = model.getAllRelationships(
					new AJRelationshipType[] { relTypes[i] }).size();
			if (n > 0) {
				info.append(n + " " + relTypes[i].getDisplayName() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				total += n;
			}
		}
		info.append(NLS.bind(UIMessages.ajmapEditor_rel_total, "" + total)); //$NON-NLS-1$
		return info.toString();
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

	private Text createText(Composite parent, String text) {
		Text label = new Text(parent, SWT.READ_ONLY | SWT.MULTI);
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
