
/* *******************************************************************
 * Copyright (c) 2004 Mik Kersten
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/

package org.eclipse.ajdt.internal.ui.navigator;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.HierarchyWalker;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IHierarchyListener;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.internal.ui.resources.AJDTIcon;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * @author Mik Kersten
 */
public class PointcutNavigatorView extends ViewPart {
	
    private HashMap targetHistory = new HashMap();
    private boolean structureModelLocked = false;
    
    public final IHierarchyListener VIEW_LISTENER = new IHierarchyListener() {
		public void elementsUpdated(IHierarchy model) {   
		    Workbench.getInstance().getDisplay().asyncExec(new Runnable() {
				public void run() {
				    try {
				        if (viewer != null) {
				            viewer.refresh();
				            viewer.expandAll();
				        }
				    } catch (Throwable t) {
			        	//MPC: I've commented this out, as our enforcement aspect complains about it
			        	//     - any thrown exceptions will be logged by the FFDC aspect anyway
				        //t.printStackTrace();
				    }
				}
			});
		}
	}; 
	
    private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action pinStructureModel;
	private Action action2;
	
	private final class TogglePushpinAction extends Action {
	    public TogglePushpinAction() {
	        setChecked(false); 
	    }
	    
	    public void run() {
            structureModelLocked = !structureModelLocked;
            setChecked(structureModelLocked);
        }
    }

    class TreeObject implements IAdaptable {
	    private IProgramElement element;
		private TreeParent parent;
		
		public TreeObject(IProgramElement element) {
			this.element = element;
		}
		public String getName() {
			return element.toLabelString();
		}
		public void setParent(TreeParent parent) {
			this.parent = parent;
		}
		public TreeParent getParent() {
			return parent;
		}
		public String toString() {
			return getName();
		}
		public Object getAdapter(Class key) {
			return null;
		}
        public IProgramElement getElement() {
            return element;
        }
	}
	
	class TreeParent extends TreeObject {
		private ArrayList children;
		private String relationshipName;
		boolean isLink = false;
		boolean isMissing = false;
		boolean isNew = false;
	
		public TreeParent(String relationshipName) {
		    super(null);
		    this.relationshipName = relationshipName;
		    children = new ArrayList();
		}

		public String toString() {
		    if (super.element == null) return relationshipName;
		    else return getName();
		}
		
		public TreeParent(IProgramElement element) {
			super(element);
			children = new ArrayList();
		}
		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}
		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}
		public TreeObject [] getChildren() {
			return (TreeObject [])children.toArray(new TreeObject[children.size()]);
		}
		public boolean hasChildren() {
			return children.size() > 0;
		}
        public boolean isLink() {
            return isLink;
        }
        public void setLink(boolean isLink) {
            this.isLink = isLink;
        }
        public boolean isMissing() {
            return isMissing;
        }
        public void setMissing(boolean isMissing) {
            this.isMissing = isMissing;
        }
        public boolean isNew() {
            return isNew;
        }
        public void setNew(boolean isNew) {
            this.isNew = isNew;
        }
	} 

	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {	}
		
		public void dispose() { }
		
		public Object[] getElements(Object parent) {
		    if (AsmManager.getDefault().getHierarchy().getRoot() == null) return new TreeParent[] {new TreeParent(UIMessages.PointcutNavigatorView_rebuild_to_view_structure)};
		    final List pointcutsAndAdvice = new ArrayList();
		    AsmManager.getDefault().getHierarchy().getRoot().walk(new HierarchyWalker() {
                public void preProcess(IProgramElement node) {
                    if (node.getKind().equals(IProgramElement.Kind.POINTCUT) ||
                        node.getKind().equals(IProgramElement.Kind.ADVICE)) {
                        pointcutsAndAdvice.add(node);
                    }
                } 
            });
		    return buildPointcutTree(pointcutsAndAdvice);    
		}

        private Object[] buildPointcutTree(List pointcutsAndAdvice) {
            List topLevel = new ArrayList();
            for (Iterator it = pointcutsAndAdvice.iterator(); it.hasNext();) {
                IProgramElement element = (IProgramElement) it.next();
                
                IRelationship relationship = AsmManager.getDefault().getRelationshipMap().get(element, IRelationship.Kind.USES_POINTCUT, "uses pointcut"); //$NON-NLS-1$
                if (relationship == null || relationship.getTargets().size() == 0) {
                    TreeParent adviceOrPointcutNode = new TreeParent(element);
                    topLevel.add(adviceOrPointcutNode);
                    addAllReferencingAsChildren(adviceOrPointcutNode);
                    addTargets(adviceOrPointcutNode);
                } else {
//                    System.err.println(">>>> got: " + element + ", " + );
                }
            }
            return topLevel.toArray();
        }

        private void addTargets(TreeParent node) {
            try {
	            if (node.getElement() == null) return;
	            if (node.getElement().getKind() == IProgramElement.Kind.ADVICE) {
	                IRelationship advice = AsmManager.getDefault().getRelationshipMap().get(node.getElement(), IRelationship.Kind.ADVICE, "advises"); //$NON-NLS-1$
	//                System.err.println("> " + advice.getTargets());
	                if (advice.getTargets().size() != 0) {
	                    TreeParent adviceNode = new TreeParent(UIMessages.PointcutNavigatorView_advises);
	                    List newTargets = advice.getTargets();
	                    List oldTargets = (List)targetHistory.get(node.getElement().getHandleIdentifier());
	                    
	                    List allTargets = new ArrayList();
	                    allTargets.addAll(newTargets);
	                    if (oldTargets != null) {
	                        for (Iterator it3 = oldTargets.iterator(); it3.hasNext();) {
	                            Object next = it3.next();
	                            if (!allTargets.contains(next)) allTargets.add(next);
                            }
	                    }
	                    for (Iterator it2 = allTargets.iterator(); it2.hasNext();) {
	                        String handle = (String) it2.next();
	                        IProgramElement target = AsmManager.getDefault().getHierarchy().getElement(handle);
	                        if (target != null) {
	                            TreeParent targetNode = new TreeParent(target);
		                        targetNode.setLink(true);
		                        if (oldTargets != null) {	
//		                            System.err.println(">> " + target.getHandleIdentifier() +  ">>>> old: " + oldTargets);
		                            if (!newTargets.contains(target.getHandleIdentifier())) {
		                                targetNode.setMissing(true);
		                            } 
		                            if (!oldTargets.contains(target.getHandleIdentifier())) {
		                                targetNode.setNew(true);
		                            } 
		                        }
		                        adviceNode.addChild(targetNode);
	                        }
	                    }  
	                    node.addChild(adviceNode);
	                    if (!structureModelLocked) targetHistory.put(node.getElement().getHandleIdentifier(), newTargets);
	                }
	            }
            } catch (Throwable t) {
            	//MPC: I've commented this out, as our enforcement aspect complains about it
            	//     - any thrown exceptions will be logged by the FFDC aspect anyway
                //t.printStackTrace();
            }
        }

        private void addAllReferencingAsChildren(TreeParent node) {
            IRelationship relationship = AsmManager.getDefault().getRelationshipMap().get(node.getElement(), IRelationship.Kind.USES_POINTCUT, "pointcut used by"); //$NON-NLS-1$
            if (relationship != null && relationship.getTargets().size() != 0) {
                for (Iterator it = relationship.getTargets().iterator(); it.hasNext();) {
                    String handle = (String) it.next();
                    IProgramElement child = AsmManager.getDefault().getHierarchy().getElement(handle);
//                    System.err.println(">>>> " + child);
                    TreeParent childNode = new TreeParent(child);
                    node.addChild(childNode);
                    addAllReferencingAsChildren(childNode);
                    addTargets(childNode);
                }
            }
        }

        public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}

//		private void initialize() {
//			invisibleRoot.addChild(new TreeParent(AsmManager.getDefault().getHierarchy().getRoot()));
//		}
	}
	class ViewLabelProvider extends LabelProvider implements IColorProvider, IFontProvider {

	    public Font getFont(Object element) {
	        if (!(element instanceof TreeParent)) return null;
			TreeParent node = (TreeParent)element;
			if (node.isNew() || node.isMissing()) {
			    return new Font(null, "Tahoma", 8, SWT.BOLD); //$NON-NLS-1$
			} else {
			    return null;
			} 
	    }
	    
		public String getText(Object obj) {
			return obj.toString();
		}
		
		// TODO: don't create each time
		public Image getImage(Object obj) {
//			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof TreeParent) {
			    IProgramElement element = ((TreeParent)obj).getElement();
			    if (element == null) {
			        return AspectJImages.ADVICE.getImageDescriptor().createImage();
			    } else if (element.getKind() == IProgramElement.Kind.ADVICE) {
			        // TODO: implement proper advice icons 
				    AJDTIcon icon = (AJDTIcon)AspectJImages.instance().getIcon(element.getKind());
				    return icon.getImageDescriptor().createImage();
			    } else {
			        AJDTIcon icon = (AJDTIcon)AspectJImages.instance().getStructureIcon(element.getKind(), element.getAccessibility());
				    return icon.getImageDescriptor().createImage();
			    }
			}
			return null;
		}
		
		public Color getBackground(Object element) { 
			return null;
		}

		public Color getForeground(Object element) {
			if (!(element instanceof TreeParent)) return null;
			TreeParent node = (TreeParent)element;
			if (node.isMissing()) {
			    return new Color(Workbench.getInstance().getDisplay(), 120, 120, 120);
			} else if(node.isLink()) {
			    return new Color(Workbench.getInstance().getDisplay(), 0, 0, 255);
			} else {
			    return new Color(Workbench.getInstance().getDisplay(), 0, 0, 0);
			}
		} 
	}
	class NameSorter extends ViewerSorter {
	    
//	        public int compare(Viewer viewer, Object e1, Object e2) {
//	            if (e1 instanceof TreeParent && e2 instanceof TreeParent) {
//	                TreeParent p1 = (TreeParent)e1;
//	                if (p1.getElement().getKind() == IProgramElement.Kind.ADVICE) return 1;
//	            }
//            return super.compare(viewer, e1, e2);
//        }
}

	public PointcutNavigatorView() { }

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		try {
		    viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			drillDownAdapter = new DrillDownAdapter(viewer);
			viewer.setContentProvider(new ViewContentProvider());
			viewer.setLabelProvider(new ViewLabelProvider());
			viewer.setSorter(new NameSorter());
			viewer.setInput(getViewSite());
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
	            public void selectionChanged(SelectionChangedEvent event) {
	                StructuredSelection selection = (StructuredSelection)viewer.getSelection();
	                if (selection.getFirstElement() != null && selection.getFirstElement() instanceof TreeParent) {
	                    TreeParent parent = (TreeParent)selection.getFirstElement();
	                    if (parent.getElement() == null) return;
	                    IResource resource = AspectJUIPlugin.getDefault().getAjdtProjectProperties().findResource(
	                            parent.getElement().getSourceLocation().getSourceFile().getAbsolutePath(), 
	                            AspectJPlugin.getDefault().getCurrentProject());
	                    
	                    IEditorPart part;
	                    try {
	                        part = IDE.openEditor(
	                            AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage( ), 
	                            (IFile)resource, 
	                            true);
	                        if (part instanceof AspectJEditor) {
	                            AspectJEditor editor = (AspectJEditor)part;
	                            IDocumentProvider provider= editor.getDocumentProvider();
	                    		IDocument document= provider.getDocument(editor.getEditorInput());
	                    		try {
	                    			int start= document.getLineOffset(parent.getElement().getSourceLocation().getLine());			
	                    			editor.selectAndReveal(start-2, 0);
	                    			IWorkbenchPage page= editor.getSite().getPage();
	                    			page.activate(editor);
	                    		} catch (BadLocationException x) {
	                    			// ignore
	                    		} 
	                        }
	                    } catch (PartInitException e) {
	                    	//MPC: I've commented this out, as our enforcement aspect complains about it
	                    	//     - any thrown exceptions will be logged by the FFDC aspect anyway
	                        //e.printStackTrace();
	                    }
	                }
	            }
	        });
			makeActions();
			hookContextMenu();
			hookDoubleClickAction();
			contributeToActionBars();
			
			AsmManager.getDefault().addListener(VIEW_LISTENER);
        } catch (Throwable t) {
        	//MPC: I've commented this out, as our enforcement aspect complains about it
        	//     - any thrown exceptions will be logged by the FFDC aspect anyway
            //t.printStackTrace();
        }
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				PointcutNavigatorView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(pinStructureModel);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(pinStructureModel);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(pinStructureModel);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		pinStructureModel = new TogglePushpinAction();
		pinStructureModel.setText(UIMessages.PointcutNavigatorView_pin_structure_model);
		pinStructureModel.setToolTipText(UIMessages.PointcutNavigatorView_pin_structure_model_tooltip);
		
		// MPC: I've commented this out temporarily, to fix the build
		//pinStructureModel.setImageDescriptor(AspectJImages.PUSHPIN.getImageDescriptor( ) );
		
		action2 = new Action() {
			public void run() {
//				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2"); //$NON-NLS-1$
		action2.setToolTipText("Action 2 tooltip"); //$NON-NLS-1$
		action2.setImageDescriptor(AspectJImages.HIDE_DECLARATIONS.getImageDescriptor( ) );
//		action2.setImageDescriptor(MylarImages.AUTO_EXPAND);
//		doubleClickAction = new Action() {
//			public void run() {
//				ISelection selection = viewer.getSelection();
//				Object obj = ((IStructuredSelection)selection).getFirstElement();
//				showMessage("Double-click detected on "+obj.toString());
//			}
//		};
	}

	private void hookDoubleClickAction() {
//		viewer.addDoubleClickListener(new IDoubleClickListener() {
//			public void doubleClick(DoubleClickEvent event) {
//				doubleClickAction.run();
//			}
//		});
	}
//	private void showMessage(String message) {
//		MessageDialog.openInformation(
//			viewer.getControl().getShell(),
//			"Sample View", //$NON-NLS-1$
//			message);
//	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
