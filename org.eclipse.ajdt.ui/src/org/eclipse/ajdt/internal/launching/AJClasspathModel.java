/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.launching;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathGroup;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathMessages;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathModel;
import org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

/**
 * Mostly copied from ClasspathModel, but extends to include an 'Aspect Path'
 * node.
 */
public class AJClasspathModel extends ClasspathModel {

	public static final int ASPECTPATH = 2;

	private ClasspathGroup bootstrapEntries;

	private ClasspathGroup userEntries;

	private ClasspathGroup aspectPathEntries;

	/**
	 * Constructs a new aj classpath model with root entries
	 */
	public AJClasspathModel() {
		super();
		getBootstrapEntry();
		getUserEntry();
		getAspectPathEntry();
	}

	/**
	 * Add a classpath entry
	 */
	public Object addEntry(int entryType, IRuntimeClasspathEntry entry) {
		IClasspathEntry entryParent = null;
		switch (entryType) {
		case BOOTSTRAP:
			entryParent = getBootstrapEntry();
			break;
		case USER:
			entryParent = getUserEntry();
			break;
		case ASPECTPATH:
			entryParent = getAspectPathEntry();
		default:
			break;
		}

		ClasspathEntry newEntry = createEntry(entry, entryParent);
		Iterator entries = childEntries.iterator();
		while (entries.hasNext()) {
			Object element = entries.next();
			if (element instanceof ClasspathGroup) {
				if (((ClasspathGroup) element).contains(newEntry)) {
					return null;
				}
			} else if (element.equals(newEntry)) {
				return null;
			}
		}
		if (entryParent != null) {
			//((ClasspathGroup) entryParent).addEntry(newEntry);
			addEntryToClasspathGroup((ClasspathGroup) entryParent,newEntry);
		} else {
			childEntries.add(newEntry);
		}
		return newEntry;
	}

	/**
	 * Remove all entries
	 */
	public void removeAll() {
		if (bootstrapEntries != null) {
			bootstrapEntries.removeAll();
		}
		if (userEntries != null) {
			userEntries.removeAll();
		}
		if (aspectPathEntries != null) {
			aspectPathEntries.removeAll();
		}
	}

	/**
	 * Copied from super - set bootstrap entries
	 */
	public void setBootstrapEntries(IRuntimeClasspathEntry[] entries) {
		if (bootstrapEntries == null) {
			getBootstrapEntry();
		}
		bootstrapEntries.removeAll();
		for (int i = 0; i < entries.length; i++) {
			//bootstrapEntries.addEntry(new ClasspathEntry(entries[i],
			//		bootstrapEntries));
			addEntryToClasspathGroup(bootstrapEntries,new ClasspathEntry(entries[i],
					bootstrapEntries));
		}
	}

	/**
	 * Copied from super - set user entries
	 */
	public void setUserEntries(IRuntimeClasspathEntry[] entries) {
		if (userEntries == null) {
			getUserEntry();
		}
		userEntries.removeAll();
		for (int i = 0; i < entries.length; i++) {
			//userEntries.addEntry(new ClasspathEntry(entries[i], userEntries));
			addEntryToClasspathGroup(userEntries,new ClasspathEntry(entries[i], userEntries));
		}
	}

	/**
	 * Set the AspectPath entries
	 * 
	 * @param entries -
	 *            classpath entries
	 */
	public void setAspectPathEntries(IRuntimeClasspathEntry[] entries) {
		if (aspectPathEntries == null) {
			getAspectPathEntry();
		}
		aspectPathEntries.removeAll();
		for (int i = 0; i < entries.length; i++) {
			//aspectPathEntries.addEntry(new ClasspathEntry(entries[i],
			//		aspectPathEntries));
			addEntryToClasspathGroup(aspectPathEntries,new ClasspathEntry(entries[i],
					aspectPathEntries));
		}
	}

	private IClasspathEntry getAspectPathEntry() {
		if (aspectPathEntries == null) {
			String name = AspectJUIPlugin
					.getResourceString("Launcher.aspectPath"); //$NON-NLS-1$
			aspectPathEntries = createGroupEntry(new IRuntimeClasspathEntry[0],
					null, name, false, true);
		}
		return aspectPathEntries;
	}

	public IClasspathEntry getBootstrapEntry() {
		if (bootstrapEntries == null) {
			String name = ClasspathMessages.getString("ClasspathModel.0"); //$NON-NLS-1$
			bootstrapEntries = createGroupEntry(new IRuntimeClasspathEntry[0],
					null, name, false, true);
		}
		return bootstrapEntries;
	}

	public IClasspathEntry getUserEntry() {
		if (userEntries == null) {
			String name = ClasspathMessages.getString("ClasspathModel.1"); //$NON-NLS-1$
			userEntries = createGroupEntry(new IRuntimeClasspathEntry[0], null,
					name, false, true);
		}
		return userEntries;
	}

	/**
	 * Copied from super class - create an entry
	 */
	private ClasspathGroup createGroupEntry(IRuntimeClasspathEntry[] entries,
			ClasspathGroup entryParent, String name, boolean canBeRemoved,
			boolean addEntry) {

		ClasspathGroup group = new ClasspathGroup(name, entryParent,
				canBeRemoved);

		for (int i = 0; i < entries.length; i++) {
			//group.addEntry(new ClasspathEntry(entries[i], group));
			addEntryToClasspathGroup(group, new ClasspathEntry(entries[i],
					group));
		}

		if (addEntry) {
			addEntry(group);
		}
		return group;
	}

	/**
	 * Returns the entries of the given type, or an empty collection if none.
	 * 
	 * @param entryType
	 * @return the entries of the given type, or an empty collection if none
	 */
	public IClasspathEntry[] getEntries(int entryType) {
		switch (entryType) {
		case BOOTSTRAP:
			if (bootstrapEntries != null) {
				return bootstrapEntries.getEntries();
			}
			break;
		case USER:
			if (userEntries != null) {
				return userEntries.getEntries();
			}
			break;
		case ASPECTPATH:
			if (aspectPathEntries != null) {
				return aspectPathEntries.getEntries();
			}
		}
		return new IClasspathEntry[0];
	}

	/**
	 * Use this instead of calling group.addEntry(), so that we can use either the Eclipse 3.0 or
	 * 3.1 version of this internal API (the 3.1 version has an extra parameter).
	 * @param group
	 * @param entry
	 */
	private void addEntryToClasspathGroup(ClasspathGroup group,
			IClasspathEntry entry) {
		Class clazz = group.getClass();
		Method m;
		try {
			try {
				m = clazz.getMethod("addEntry",
						new Class[] { IClasspathEntry.class });
				m.invoke(group, new Object[] { entry });
			} catch (NoSuchMethodException e) {
				m = clazz.getMethod("addEntry", new Class[] {
						IClasspathEntry.class, Object.class });
				m.invoke(group, new Object[] { entry, null });
			}
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
	}
}
