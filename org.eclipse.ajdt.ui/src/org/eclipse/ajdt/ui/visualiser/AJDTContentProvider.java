/*********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html 
 * Contributors: 
 * 
 * 	Sian Whiting -  initial version.
 * 
 **********************************************************************/
package org.eclipse.ajdt.ui.visualiser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.javamodel.AJCompilationUnitManager;
import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.contribution.visualiser.core.ProviderManager;
import org.eclipse.contribution.visualiser.interfaces.IGroup;
import org.eclipse.contribution.visualiser.interfaces.IMember;
import org.eclipse.contribution.visualiser.jdtImpl.JDTContentProvider;
import org.eclipse.contribution.visualiser.jdtImpl.JDTGroup;
import org.eclipse.contribution.visualiser.jdtImpl.JDTMember;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleMember;
import org.eclipse.contribution.visualiser.utils.JDTUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * AJDT Content Provider for the Visualiser
 */
public class AJDTContentProvider extends JDTContentProvider {

	List currentGroups;	
	List currentMembers;
	
	/**
	 * Get all the groups to display.
	 * @see org.eclipse.contribution.visualiser.interfaces.IContentProvider#getAllGroups()
	 */
	public List getAllGroups() {
		if(currentGroups != null) {
			return currentGroups;
		} else {
			updateData();
			return currentGroups;
		}
	}
	
	
	/**
	 * Get all the members to display.
	 * @see org.eclipse.contribution.visualiser.interfaces.IContentProvider#getAllMembers()
	 */
	public List getAllMembers() {
		if(currentMembers != null) {
			return currentMembers;
		} else {
			updateData();
			return currentMembers;
		}
	}

	
	/** 
	 * Keeps the currentJavaElement and currentProject information up to date
	 * in this class, as this method is called whenever a user changes
	 * their selection in the workspace.
	 */
	public void selectionChanged(IWorkbenchPart iwp, ISelection is) {
		if(!(ProviderManager.getContentProvider().equals(this))){
			return;
		}
		boolean updateRequired = false;
		if (is instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection =
				(IStructuredSelection) is;
			Object o = structuredSelection.getFirstElement();

			if (o != null) {
				if (o instanceof IJavaElement) {
					IJavaElement je = (IJavaElement) o;
					if (currentlySelectedJE == je) {
						return;
					}
					currentlySelectedJE = je;
					updateRequired = true;
					if (je.getJavaProject() != null) {
						setCurrentProject(je.getJavaProject());
					}
				}
			}
		}
		if (updateRequired) {
			AJDTEventTrace.generalEvent("AJDTContentProvider.selectionChanged(): Marking visualiser content as out of date");
			currentGroups = null;
			currentMembers = null;
			VisualiserPlugin.refresh();
		}
	}
	
	
	/**
	 * Get the data for the current selection
	 * @param members - add members to the List being returned if true, otherwise add groups
	 * @return data
	 */
	private void updateData() {
		if(ProviderManager.getMarkupProvider() instanceof AJDTMarkupProvider) {
			((AJDTMarkupProvider)ProviderManager.getMarkupProvider()).resetMarkupsAndKinds();
		}
		long stime = System.currentTimeMillis();
		currentGroups = new ArrayList();
		currentMembers = new ArrayList();
		try {
			if (currentlySelectedJE instanceof IJavaProject) {
				IPackageFragment[] packageFragments = ((IJavaProject)currentlySelectedJE).getPackageFragments();
				for (int i = 0; i < packageFragments.length; i++) {
					if (!(packageFragments[i].isReadOnly())) {
						boolean defaultPackage = packageFragments[i].isDefaultPackage();
						IGroup group = new JDTGroup(packageFragments[i].getElementName());
						if(defaultPackage) {
							group.setName("(default package)");
							group.setTooltip("(default package)");
						}
						List classes = AJDTVisualiserUtils.getAllClasses(packageFragments[i]);
						for (Iterator iter = classes.iterator(); iter.hasNext();) {
							Object[] info = (Object[]) iter.next();
							IResource res = (IResource)info[0];
							IJavaElement jEl = JavaCore.create(res);
							if(res != null) {
								String name = res.getName();
								name = name.substring(0, name.lastIndexOf("."));
								IMember member;
								if (jEl != null) {
									member = new JDTMember(name, jEl);
								} else {
									member = new JDTMember(name, AJCompilationUnitManager.INSTANCE.getAJCompilationUnit((IFile)res));
								}
								member.setSize(((Integer)info[1]).intValue());
								group.add(member);
								currentMembers.add(member);
								if(defaultPackage) {
									((SimpleMember)member).setFullName(member.getName());
								}							
							}
						}
						if(group.getSize().intValue() > 0) {
							currentGroups.add(group);
						}
					}
				}
			} else if (currentlySelectedJE instanceof IPackageFragment) {
				IPackageFragment packageFragment = (IPackageFragment)currentlySelectedJE;
				boolean defaultPackage = packageFragment.isDefaultPackage();
				IGroup group = new JDTGroup(packageFragment.getElementName());
				if(defaultPackage) {
					group.setName("(default package)");
					group.setTooltip("(default package)");
				}
				List classes = AJDTVisualiserUtils.getAllClasses(packageFragment);
				for (Iterator iter = classes.iterator(); iter.hasNext();) {
					Object[] info = (Object[]) iter.next();
					IResource res = (IResource)info[0];
					if(res != null) {
						IJavaElement jEl = JavaCore.create(res);
						String name = res.getName();
						name = name.substring(0, name.lastIndexOf("."));
						IMember member;
						if (jEl != null) {
							member = new JDTMember(name, jEl);
						} else {
							member = new JDTMember(name, AJCompilationUnitManager.INSTANCE.getAJCompilationUnit((IFile)res));
						}
						member.setSize(((Integer)info[1]).intValue());
						group.add(member);
						currentMembers.add(member);
						if(defaultPackage) {
							((SimpleMember)member).setFullName(member.getName());
						}
					}
				}
				if(group.getSize().intValue() > 0) {
					currentGroups.add(group);
				}
			} else if (currentlySelectedJE instanceof ICompilationUnit) {
				List classes = AJDTVisualiserUtils.getAllClasses((ICompilationUnit)currentlySelectedJE);
				IPackageFragment packageFrag = (IPackageFragment)((ICompilationUnit)currentlySelectedJE).getParent();
				boolean defaultPackage = packageFrag.isDefaultPackage();
				// ?!? Need to confirm a group for the pkg frag is OK in the case of a selection like thiss
				IGroup group = new JDTGroup(packageFrag.getElementName());
				if(defaultPackage) {
					group.setName("(default package)");
					group.setTooltip("(default package)");
				}
				for (Iterator iter = classes.iterator(); iter.hasNext();) {
					Object[] info = (Object[]) iter.next();
					IResource res = (IResource)info[0];
					IJavaElement jEl = JavaCore.create(res);
					String name = res.getName();
					name = name.substring(0, name.lastIndexOf("."));
					IMember member;
					if (jEl != null) {
						member = new JDTMember(name, jEl);
					} else {
						member = new JDTMember(name, AJCompilationUnitManager.INSTANCE.getAJCompilationUnit((IFile)res));
					}
					member.setSize(((Integer)info[1]).intValue());
					group.add(member);
					currentMembers.add(member);
					if(defaultPackage) {
						((SimpleMember)member).setFullName(member.getName());
					}
				}
				if(group.getSize().intValue() > 0) {
					currentGroups.add(group);
				}
			}
		} catch (JavaModelException jme) {
		}
		long etime = System.currentTimeMillis();

		AJDTEventTrace.generalEvent("AJDTContentProvider.updateData() executed - took "+(etime-stime)+"ms");
	}

	

	/**
	 * Override super to reset the colour list when the project changes.
	 * @see org.eclipse.contribution.visualiser.jdtImpl.JDTContentProvider#setCurrentProject(org.eclipse.jdt.core.IJavaProject)
	 */
	protected void setCurrentProject(IJavaProject newProject) {
		if(currentProject == null || !currentProject.equals(newProject)) {
			if(ProviderManager.getMarkupProvider() instanceof AJDTMarkupProvider) {
				((AJDTMarkupProvider)ProviderManager.getMarkupProvider()).resetColours();
			}
			currentProject = newProject;
		}
	}


	/**
	 * 
	 */
	public void reset() {
		currentGroups = null;
		currentMembers = null;		
	}
	
	
	/**
	 * Process a mouse click on a member
	 * @see org.eclipse.contribution.visualiser.interfaces.IContentProvider#processMouseclick(IMember, boolean, int)
	 */
	public boolean processMouseclick(
		IMember member,
		boolean markupWasClicked,
		int buttonClicked) {
		
		if(buttonClicked != 1){
			return true;	
		}
		if(markupWasClicked) {
			return false;
		}
		if (member instanceof JDTMember) {
			IJavaElement jEl = ((JDTMember)member).getResource();
			if (jEl != null) {
				JDTUtils.openInEditor(jEl.getResource(), JDTUtils.getClassDeclLineNum(jEl));
			}
		}
		
		return false;
	}
	
}
