/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.visualiser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.internal.buildconfig.BuildConfiguration;
import org.eclipse.ajdt.internal.buildconfig.BuildConfigurator;
import org.eclipse.ajdt.internal.buildconfig.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.utils.AJDTEventTrace;
import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.contribution.visualiser.core.ProviderManager;
import org.eclipse.contribution.visualiser.interfaces.IGroup;
import org.eclipse.contribution.visualiser.interfaces.IMember;
import org.eclipse.contribution.visualiser.jdtImpl.JDTContentProvider;
import org.eclipse.contribution.visualiser.jdtImpl.JDTGroup;
import org.eclipse.contribution.visualiser.jdtImpl.JDTMember;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleMember;
import org.eclipse.contribution.visualiser.utils.JDTUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
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
						IPackageFragment packageFragment = packageFragments[i];
						List classes = getMembersForPackage(packageFragment);
						if(classes.size() > 0) {
							boolean defaultPackage = packageFragment.isDefaultPackage();
							IGroup group = new JDTGroup(packageFragment.getElementName());
							if(defaultPackage) {
								group.setName("(default package)"); // $NON-NLS-1$
								group.setTooltip("(default package)"); // $NON-NLS-1$
							}
							for (Iterator iter = classes.iterator(); iter.hasNext();) {
								IMember member = (IMember) iter.next();
								group.add(member);
								currentMembers.add(member);
								if(defaultPackage) {
									((SimpleMember)member).setFullName(member.getName());
								}
							}
							currentGroups.add(group);	
						}
					}
				}
			} else if (currentlySelectedJE instanceof IPackageFragment) {
				IPackageFragment packageFragment = (IPackageFragment)currentlySelectedJE;
				List classes = getMembersForPackage(packageFragment);
				if(classes.size() > 0) {
					boolean defaultPackage = packageFragment.isDefaultPackage();
					IGroup group = new JDTGroup(packageFragment.getElementName());
					if(defaultPackage) {
						group.setName("(default package)"); // $NON-NLS-1$
						group.setTooltip("(default package)"); // $NON-NLS-1$
					}
					for (Iterator iter = classes.iterator(); iter.hasNext();) {
						IMember member = (IMember) iter.next();
						group.add(member);
						currentMembers.add(member);
						if(defaultPackage) {
							((SimpleMember)member).setFullName(member.getName());
						}						
					}
					currentGroups.add(group);					
				}
			} else if (currentlySelectedJE instanceof ICompilationUnit) {
				ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(currentProject);
				BuildConfiguration bc = null;
				if(pbc != null) {
					bc = pbc.getActiveBuildConfiguration();
				}
				JDTMember member = null;
				if((bc != null && bc.isIncluded(currentlySelectedJE.getResource())) || bc == null) { 
					String memberName = currentlySelectedJE.getElementName();
					if(memberName.endsWith(".java")){ //$NON-NLS-1$
						memberName = memberName.substring(0, memberName.length() - 5); 					
					} else if(memberName.endsWith(".aj")){ //$NON-NLS-1$
						memberName = memberName.substring(0, memberName.length() - 3); 					
					}							
					member = new JDTMember(memberName, currentlySelectedJE);
					member.setSize(getLength((ICompilationUnit)currentlySelectedJE));
					currentMembers.add(member);
				}
				if(member != null) {
					IPackageFragment packageFrag = (IPackageFragment)((ICompilationUnit)currentlySelectedJE).getParent();
					boolean defaultPackage = packageFrag.isDefaultPackage();
					// ?!? Need to confirm a group for the pkg frag is OK in the case of a selection like thiss
					IGroup group = new JDTGroup(packageFrag.getElementName());
					if(defaultPackage) {
						group.setName("(default package)"); // $NON-NLS-1$
						group.setTooltip("(default package)"); // $NON-NLS-1$
					}
					if(defaultPackage) {
						member.setFullName(member.getName());						
					} 
					group.add(member);
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


	/**
	 * Get all JDT members for the given IPackageFragment (Java package)
	 * @param PF
	 * @return List of JDTMembers
	 */
	public List getMembersForPackage(IPackageFragment PF) {
		List returningClasses = new ArrayList();
		try {
			if (containsUsefulStuff(PF)) {
				ProjectBuildConfigurator pbc = BuildConfigurator.getBuildConfigurator().getProjectBuildConfigurator(currentProject);
				BuildConfiguration bc = null;
				if(pbc != null) {
					bc = pbc.getActiveBuildConfiguration();
				}
				IJavaElement[] ijes = PF.getChildren();
				for (int j = 0; j < ijes.length; j++) {
					if (ijes[j].getElementType()
							== IJavaElement.COMPILATION_UNIT) {
						if((bc != null && bc.isIncluded(ijes[j].getResource())) || bc == null) {  
							String memberName = ijes[j].getElementName();
							if(memberName.endsWith(".java")){ //$NON-NLS-1$
								memberName = memberName.substring(0, memberName.length() - 5); 					
							} else if(memberName.endsWith(".aj")){ //$NON-NLS-1$
								memberName = memberName.substring(0, memberName.length() - 3); 					
							}							
							JDTMember member = new JDTMember(memberName, ijes[j]);
							member.setSize(getLength((ICompilationUnit)ijes[j]));
							returningClasses.add(member);
						}
					}
				}
			}
		} catch (JavaModelException jme) {
		}
		return returningClasses;
	}


}
