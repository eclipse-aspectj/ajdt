/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - inital version
 *******************************************************************************/
package org.eclipse.contribution.xref.internal.ui.inplace;

//import org.eclipse.contribution.xref.core.IXReferenceAdapter;
//import org.eclipse.contribution.xref.internal.ui.actions.DoubleClickAction;
//import org.eclipse.contribution.xref.internal.ui.providers.TreeObject;
//import org.eclipse.contribution.xref.internal.ui.providers.TreeParent;
//import org.eclipse.contribution.xref.internal.ui.providers.XReferenceContentProvider;
//import org.eclipse.contribution.xref.internal.ui.providers.XReferenceLabelProvider;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.core.runtime.IAdaptable;
//import org.eclipse.jdt.core.IJavaElement;
//import org.eclipse.jdt.internal.ui.actions.OpenActionUtil;
//import org.eclipse.jdt.internal.ui.text.AbstractInplaceInformationControl;
//import org.eclipse.jdt.internal.ui.util.StringMatcher;
//import org.eclipse.jdt.internal.ui.viewsupport.MemberFilter;
//import org.eclipse.jface.action.Action;
//import org.eclipse.jface.viewers.AbstractTreeViewer;
//import org.eclipse.jface.viewers.DoubleClickEvent;
//import org.eclipse.jface.viewers.IDoubleClickListener;
//import org.eclipse.jface.viewers.ILabelProvider;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.jface.viewers.ITreeContentProvider;
//import org.eclipse.jface.viewers.StructuredSelection;
//import org.eclipse.jface.viewers.TreeViewer;
//import org.eclipse.jface.viewers.Viewer;
//import org.eclipse.jface.viewers.ViewerFilter;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.KeyEvent;
//import org.eclipse.swt.events.KeyListener;
//import org.eclipse.swt.events.ModifyEvent;
//import org.eclipse.swt.events.ModifyListener;
//import org.eclipse.swt.graphics.Color;
//import org.eclipse.swt.graphics.FontMetrics;
//import org.eclipse.swt.graphics.GC;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Text;
//import org.eclipse.swt.widgets.Tree;
//import org.eclipse.swt.widgets.TreeItem;

/**
 * Class to populate the inplace Cross Reference view - commented out for the time being
 * 
 * @author hawkinsh
 */
public class XReferenceInformationControl /*extends AbstractInplaceInformationControl*/ {

//	private Action doubleClickAction;
//	private Composite composite;
//	private Shell shell;
//	private StringMatcher stringMatcher;
//	private Text filterText;
//	private TreeViewer viewer;
//	private XReferenceContentProvider contentProvider;
//	
//	/* (non-Javadoc)
//	 * @see org.eclipse.jdt.internal.ui.text.AbstractInplaceInformationControl#setInput(java.lang.Object)
//	 */
//	public void setInput(Object information) {
//		IAdaptable a = null;
//		IXReferenceAdapter xra = null;		
//		if (information == null || information instanceof String) {
//			inputChanged(null, null);
//			return;
//		}
//		if (information instanceof IJavaElement) {
//			IJavaElement je = (IJavaElement)information;
//			a = (IAdaptable) je;
//		} else {
//			// HELEN Argh!?!?!? information is NOT instance of IJavaElement
//			System.out.println("Argh!?!?!? information is NOT instance of IJavaElement");
//		}
//		if (a != null) {
//			xra = (IXReferenceAdapter) a.getAdapter(IXReferenceAdapter.class);
//		}
//		if (xra != null) {
//			inputChanged(xra,information);
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eclipse.jdt.internal.ui.text.AbstractInplaceInformationControl#createViewer(org.eclipse.swt.widgets.Composite, int)
//	 */
//	public Viewer createViewer(Composite parent, int style) {
//		composite = parent;
//		shell = super.getShell();
//		
//		createFilterText(composite);
//		viewer =
//			new TreeViewer(parent, SWT.SINGLE | (style & ~SWT.MULTI));
//		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
//		
//		viewer.addFilter(new NamePatternFilter());
//		viewer.addFilter(new MemberFilter());
//
//		contentProvider = new XReferenceContentProvider();
//		viewer.setContentProvider(contentProvider);
//		viewer.setLabelProvider(new XReferenceLabelProvider());
//		viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
//		
//		doubleClickAction = new DoubleClickAction(shell,viewer,this);
//		hookDoubleClickAction();
//
//		installFilter();
//		return viewer;
//	}
//	
//	/* (non-Javadoc)
//	 * @see org.eclipse.jdt.internal.ui.text.AbstractInplaceInformationControl#setForegroundColor(org.eclipse.swt.graphics.Color)
//	 */
//	public void setForegroundColor(Color foreground) {
//		viewer.getTree().setForeground(foreground);
//		filterText.setForeground(foreground);
//		composite.setForeground(foreground);	
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eclipse.jdt.internal.ui.text.AbstractInplaceInformationControl#setBackgroundColor(org.eclipse.swt.graphics.Color)
//	 */
//	public void setBackgroundColor(Color background) {
//		viewer.getTree().setBackground(background);
//		filterText.setBackground(background);
//		composite.setBackground(background);	
//	}
//	
//	
//	private void gotoSelectedElement() {
//		Object selectedElement= getSelectedElement();
//		if (selectedElement != null) {
//			try {
//				dispose();
//				OpenActionUtil.open(selectedElement, true);
//			} catch (CoreException ex) {
//				ex.printStackTrace();
//			}
//		}
//	}
//
//	/**
//	 * Implementers can modify
//	 */
//	protected Object getSelectedElement() {
//		return ((IStructuredSelection) viewer.getSelection()).getFirstElement();
//	}
//
//// --------------- Added for the text filtering (from AbstractInformationControl) -------------------	
//
//	protected class NamePatternFilter extends ViewerFilter {
//		
//		public NamePatternFilter() {
//		}
//		
//		/* (non-Javadoc)
//		 * Method declared on ViewerFilter.
//		 */
//		public boolean select(Viewer viewer, Object parentElement, Object element) {
//			StringMatcher matcher= getMatcher();
//			if (matcher == null || !(viewer instanceof TreeViewer))
//				return true;
//			TreeViewer treeViewer= (TreeViewer) viewer;
//			
//			String matchName= ((ILabelProvider) treeViewer.getLabelProvider()).getText(element);
//			if (matchName != null && matcher.match(matchName))
//				return true;
//			
//			return hasUnfilteredChild(treeViewer, element);
//		}
//		
//		private boolean hasUnfilteredChild(TreeViewer viewer, Object element) {
//			// HELEN - changed the hasUnfilteredChild method copied from AbstractInformationControl to cope with Cross References
//			if (element instanceof TreeParent) {
//				Object[] children=  ((ITreeContentProvider) viewer.getContentProvider()).getChildren(element);
//				for (int i= 0; i < children.length; i++)
//					if (select(viewer, element, children[i]))
//						return true;
//			}
//			return false;
//		}
//	}
//
//	protected StringMatcher getMatcher() {
//		return stringMatcher;
//	}
//	
//	protected Text createFilterText(Composite parent) {
//		filterText= new Text(parent, SWT.FLAT);
//
//		GridData data= new GridData();
//		GC gc= new GC(parent);
//		gc.setFont(parent.getFont());
//		FontMetrics fontMetrics= gc.getFontMetrics();
//		gc.dispose();
//
//		data.heightHint= org.eclipse.jface.dialogs.Dialog.convertHeightInCharsToPixels(fontMetrics, 1);
//		data.horizontalAlignment= GridData.FILL;
//		data.verticalAlignment= GridData.BEGINNING;
//		filterText.setLayoutData(data);
//		
//		filterText.addKeyListener(new KeyListener() {
//			public void keyPressed(KeyEvent e) {
//				if (e.keyCode == 0x0D) // return
//					gotoSelectedElement();
//				if (e.keyCode == SWT.ARROW_DOWN)
//					viewer.getTree().setFocus();
//				if (e.keyCode == SWT.ARROW_UP)
//					viewer.getTree().setFocus();
//				if (e.character == 0x1B) // ESC
//					dispose();
//			}
//			public void keyReleased(KeyEvent e) {
//				// do nothing
//			}
//		});
//
//		// Horizontal separator line
//		Label separator= new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
//		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//
//		return filterText;
//	}	
//
//	private void installFilter() {
//		filterText.setText(""); //$NON-NLS-1$
//
//		filterText.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				String text= ((Text) e.widget).getText();
//				int length= text.length();
//				if (length > 0 && text.charAt(length -1 ) != '*') {
//					text= text + '*';
//				}
//				setMatcherString(text);
//			}
//		});
//	}
//	
//	protected void setMatcherString(String pattern) {
//		if (pattern.length() == 0) {
//			stringMatcher= null;
//		} else {
//			boolean ignoreCase= pattern.toLowerCase().equals(pattern);
//			stringMatcher= new StringMatcher(pattern, ignoreCase, false);
//		}
//		stringMatcherUpdated();
//	}
//
//	protected void stringMatcherUpdated() {
//		// refresh viewer to refilter
//		viewer.getControl().setRedraw(false);
//		viewer.refresh();
//		viewer.expandAll();
//		selectFirstMatch();
//		viewer.getControl().setRedraw(true);
//	}
//	
//	protected void selectFirstMatch() {
//		Tree tree= viewer.getTree();
//		Object element= findElement(tree.getItems());
//		if (element != null)
//			viewer.setSelection(new StructuredSelection(element), true);
//		else
//			viewer.setSelection(StructuredSelection.EMPTY);
//	}
//
//	private Object findElement(TreeItem[] items) {
//		// HELEN - changed the findElement method copied from AbstractInformationControl to cope with Cross References
//		ILabelProvider labelProvider= (ILabelProvider)viewer.getLabelProvider();
//		for (int i= 0; i < items.length; i++) {
//			// HELEN - in findElement for the text filtering...there must be a better way to do this!?!?!?!?
//			Object o = items[i].getData();
//			TreeParent treeParent = null;
//			TreeObject treeObject = null;
//			if (o instanceof TreeParent) {
//				treeParent = (TreeParent)o;
//			} else if (o instanceof TreeObject) {
//				treeObject = (TreeObject)o;
//			}
//			Object element = null;
//			if (treeParent == null) {
//				element = treeObject;
//			} else {
//				element = treeParent;
//			}
//			if (stringMatcher == null)
//				return element;
//			
//			if (element != null) {
//				String label= labelProvider.getText(element);
//				if (stringMatcher.match(label))
//					return element;
//			}
//
//			element= findElement(items[i].getItems());
//			if (element != null)
//				return element;
//		}
//		return null;
//	}
//	
//// ------------- overriding these methods because want to give focus to the filter
//	
//	public void dispose() {
//		filterText = null;
//		super.dispose();
//	}
//	 
//	public boolean isFocusControl() {
//		return viewer.getControl().isFocusControl() || filterText.isFocusControl();
//	}
//	
//	public void setFocus() {
//		filterText.setFocus();
//		super.setFocus();
//	}
//	
//	protected void inputChanged(Object newInput, Object newSelection) {
//		filterText.setText(""); //$NON-NLS-1$
//		super.inputChanged(newInput,newSelection);
//	}
//
//	
//// ----------- copying the following from XReferenceView to navigate --------------
//		
//	private void hookDoubleClickAction() {
//		viewer.addDoubleClickListener(new IDoubleClickListener() {
//			public void doubleClick(DoubleClickEvent event) {
//				doubleClickAction.run();
//			}
//		});
//	}
	
}
