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

import java.text.Collator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJProjectModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.ModelComparison;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.xref.internal.ui.utils.XRefUIUtils;
import org.eclipse.contribution.xref.ui.XReferenceUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.DecoratingJavaLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.part.ViewPart;

/**
 * Crosscutting changes view which shows the results of comparing two structure
 * models (one of which might be the one for the current build)
 */
public class ChangesView extends ViewPart {

	public static final String CROSSCUTTING_VIEW_ID = "org.eclipse.ajdt.ui.diff.ChangesView"; //$NON-NLS-1$

	public static final String MAP_FILE_EXT = "ajmap"; //$NON-NLS-1$

	public static final String DOT_MAP_FILE_EXT = "." + MAP_FILE_EXT; //$NON-NLS-1$

	public static final String CURRENT_BUILD = AspectJUIPlugin
			.getResourceString("changesView.currentBuild"); //$NON-NLS-1$

	private Table table;

	// store IJavaElements for navigation (only the string names are put in
	// table)
	private IJavaElement[] sourceElements;

	private IJavaElement[] targetElements;

	private int lastMouseDownTime;

	private IProject currFromProject, currToProject;

	private String currFromName, currToName;

	private static Image incomingImage = null;

	private static Image outgoingImage = null;

	public ChangesView() {
	}

	public static void refresh() {
		IViewPart view = AspectJUIPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().findView(
						ChangesView.CROSSCUTTING_VIEW_ID);
		if (view instanceof ChangesView) {
			ChangesView changesView = (ChangesView) view;
			changesView.refreshIfCurrentBuild();
		}
	}

	private void refreshIfCurrentBuild() {
		if ((currFromName != null) && (currToName != null)) {
			if (currFromName.equals(CURRENT_BUILD)
					|| currToName.equals(CURRENT_BUILD)) {
				performComparison(currFromProject, currFromName, currToProject,
						currToName);
			}
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setLinesVisible(false);
		table.setHeaderVisible(true);
		String[] titles = {
				AspectJUIPlugin.getResourceString("changesView.table.column1"), //$NON-NLS-1$
				AspectJUIPlugin.getResourceString("changesView.table.column2"), //$NON-NLS-1$
				AspectJUIPlugin.getResourceString("changesView.table.column3"), //$NON-NLS-1$
				AspectJUIPlugin.getResourceString("changesView.table.column4") //$NON-NLS-1$
		};
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(titles[i]);
		}
		for (int c = 0; c < titles.length; c++) {
			table.getColumn(c).pack();
			final int col = c;
			// click column headers to sort
			table.getColumn(c).addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					TableItem[] items = table.getItems();
					Collator collator = Collator.getInstance(Locale
							.getDefault());
					for (int i = 1; i < items.length; i++) {
						String value1 = items[i].getText(col);
						for (int j = 0; j < i; j++) {
							String value2 = items[j].getText(col);
							if (collator.compare(value1, value2) < 0) {
								String[] values = { items[i].getText(0),
										items[i].getText(1),
										items[i].getText(2),
										items[i].getText(3) };
								Image[] icons = { items[i].getImage(0),
										items[i].getImage(1),
										items[i].getImage(2),
										items[i].getImage(3) };
								items[i].dispose();
								TableItem item = new TableItem(table, SWT.NONE,
										j);
								item.setText(values);
								item.setImage(icons);
								items = table.getItems();

								// change stored elements to match
								IJavaElement temp = sourceElements[i];
								System.arraycopy(sourceElements, j,
										sourceElements, j + 1, i - j);
								sourceElements[j] = temp;
								temp = targetElements[i];
								System.arraycopy(targetElements, j,
										targetElements, j + 1, i - j);
								targetElements[j] = temp;
								break;
							}
						}
					}
				}
			});
		}

		// keyboard selection
		final TableCursor cursor = new TableCursor(table, SWT.SINGLE);
		cursor.addSelectionListener(new SelectionAdapter() {
			// when the TableEditor is over a cell, select the corresponding row
			// in the table
			public void widgetSelected(SelectionEvent e) {
				table.setSelection(new TableItem[] { cursor.getRow() });
			}

			// when the user hits "ENTER" in the TableCursor, navigate to that
			// element
			public void widgetDefaultSelected(SelectionEvent e) {
				navigateTo(table.indexOf(cursor.getRow()), cursor.getColumn());
			}
		});

		// mouse selection
		table.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				lastMouseDownTime = e.time;
			}
		});
		cursor.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {
				if ((event.time - lastMouseDownTime) < Display.getDefault()
						.getDoubleClickTime()) {
					mouseDoubleClick(event);
				}
			}

			public void mouseDoubleClick(MouseEvent event) {
				Rectangle b = cursor.getBounds();
				Point pt = new Point(b.x + event.x, b.y + event.y);
				TableItem row = table.getItem(pt);
				if (row == null) {
					return;
				}
				for (int i = 0; i < table.getColumnCount(); i++) {
					Rectangle rect = row.getBounds(i);
					if (rect.contains(pt)) {
						navigateTo(table.indexOf(row), i);
					}
				}
			}
		});
	}

	private void navigateTo(int row, int column) {
		// can only navigate to elements (columns 1 and 3)
		if ((column == 1) && (sourceElements != null)
				&& (row < sourceElements.length)) {
			// source element
			XRefUIUtils.revealInEditor(sourceElements[row]);
		} else if ((column == 3) && (targetElements != null)
				&& (row < targetElements.length)) {
			// target element
			XRefUIUtils.revealInEditor(targetElements[row]);
		}
	}

	public void dispose() {
		sourceElements = null;
		targetElements = null;
		currFromProject = null;
		currFromName = null;
		currToProject = null;
		currToName = null;

		if (incomingImage != null) {
			incomingImage.dispose();
			incomingImage = null;
		}
		if (outgoingImage != null) {
			outgoingImage.dispose();
			outgoingImage = null;
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		table.setFocus();
	}

	private void updateDescription(String fromName, String toName) {
		setContentDescription(AspectJUIPlugin.getFormattedResourceString(
				"changesView.description", new String[] { fromName, toName })); //$NON-NLS-1$
	}

	private AJProjectModel getModelFromName(IProject project, String name) {
		if (name.equals(CURRENT_BUILD)) {
			return AJModel.getInstance().getModelForProject(project);
		}
		IPath mapFile = project.getFile(name).getLocation();
		AJProjectModel model = new AJProjectModel(project);
		model.loadModel(mapFile);
		return model;
	}

	private static ILabelProvider labelProvider = new DecoratingJavaLabelProvider(
			new AppearanceAwareLabelProvider());

	private static Image getIncomingImage() {
		if (incomingImage == null) {
			incomingImage = AspectJImages.CHANGES_ADDED.getImageDescriptor()
					.createImage();
		}
		return incomingImage;
	}

	private static Image getOutgoingImage() {
		if (outgoingImage == null) {
			outgoingImage = AspectJImages.CHANGES_REMOVED.getImageDescriptor()
					.createImage();
		}
		return outgoingImage;
	}

	public void performComparison(IProject fromProject, String fromName,
			IProject toProject, String toName) {
		currFromProject = fromProject;
		currFromName = fromName;
		currToProject = toProject;
		currToName = toName;

		updateDescription(fromName, toName);

		AJProjectModel fromModel = getModelFromName(fromProject, fromName);
		AJProjectModel toModel = getModelFromName(toProject, toName);

		List[] ret = ModelComparison.compare(fromModel, toModel);
		List addedList = ret[0];
		List removedList = ret[1];

		int numEntries = addedList.size() + removedList.size();
		sourceElements = new IJavaElement[numEntries];
		targetElements = new IJavaElement[numEntries];
		int rowCount = 0;

		// update table with results
		table.removeAll();
		// added relationships
		for (Iterator iter = addedList.iterator(); iter.hasNext();) {
			AJRelationship rel = (AJRelationship) iter.next();
			String sourceName = toModel.getJavaElementLinkName(rel.getSource());
			String targetName = toModel.getJavaElementLinkName(rel.getTarget());
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, AspectJUIPlugin
					.getResourceString("changesView.table.added")); //$NON-NLS-1$
			item.setImage(0, getIncomingImage());
			item.setText(1, sourceName);
			item.setImage(1, labelProvider.getImage(rel.getSource()));
			item.setText(2, rel.getRelationship().getDisplayName());
			item.setImage(2, XReferenceUIPlugin.getDefault()
					.getXReferenceImage());
			item.setText(3, targetName);
			item.setImage(3, labelProvider.getImage(rel.getTarget()));

			sourceElements[rowCount] = rel.getSource();
			targetElements[rowCount] = rel.getTarget();
			rowCount++;
		}
		// removed relationships
		for (Iterator iter = removedList.iterator(); iter.hasNext();) {
			AJRelationship rel = (AJRelationship) iter.next();
			String sourceName = fromModel.getJavaElementLinkName(rel
					.getSource());
			String targetName = fromModel.getJavaElementLinkName(rel
					.getTarget());
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, AspectJUIPlugin
					.getResourceString("changesView.table.removed")); //$NON-NLS-1$
			item.setImage(0, getOutgoingImage());
			item.setText(1, sourceName);
			item.setImage(1, labelProvider.getImage(rel.getSource()));
			item.setText(2, rel.getRelationship().getDisplayName());
			item.setImage(2, XReferenceUIPlugin.getDefault()
					.getXReferenceImage());
			item.setText(3, targetName);
			item.setImage(3, labelProvider.getImage(rel.getTarget()));

			sourceElements[rowCount] = rel.getSource();
			targetElements[rowCount] = rel.getTarget();
			rowCount++;
		}

		// adjust column sizes as necessary
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).pack();
		}
	}

}
