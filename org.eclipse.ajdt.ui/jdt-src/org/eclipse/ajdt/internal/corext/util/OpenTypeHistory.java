/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.corext.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.CorextMessages;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.TypeFilter;
import org.eclipse.jdt.internal.corext.util.TypeInfoFilter;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.w3c.dom.Element;

/**
 * History for the open type dialog. Object and keys are both {@link TypeNameMatch}s.
 */
public class OpenTypeHistory extends History<TypeNameMatch> {

	private static class TypeHistoryDeltaListener implements IElementChangedListener {
		@Override
		public void elementChanged(ElementChangedEvent event) {
			if (processDelta(event.getDelta()))
				OpenTypeHistory.getInstance().markAsInconsistent();
		}

		/**
		 * Computes whether the history needs a consistency check or not.
		 *
		 * @param delta the Java element delta
		 * @return <code>true</code> if consistency must be checked
		 * <code>false</code> otherwise.
		 */
		private boolean processDelta(IJavaElementDelta delta) {
			IJavaElement elem = delta.getElement();

			boolean isChanged = delta.getKind() == IJavaElementDelta.CHANGED;
			boolean isRemoved = delta.getKind() == IJavaElementDelta.REMOVED;

			switch (elem.getElementType()) {
				case IJavaElement.JAVA_PROJECT:
					if (isRemoved || isChanged && (delta.getFlags() & IJavaElementDelta.F_CLOSED) != 0)
						return true;
					return processChildrenDelta(delta);
				case IJavaElement.PACKAGE_FRAGMENT_ROOT:
					if (
						isRemoved || (
							isChanged && (
								(delta.getFlags() & IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED) != 0 ||
								(delta.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0
							)
						)
					)
						return true;
					return processChildrenDelta(delta);
				case IJavaElement.TYPE:
					if (isChanged && (delta.getFlags() & IJavaElementDelta.F_MODIFIERS) != 0)
						return true;
					// type children can be inner classes: fall through
				case IJavaElement.JAVA_MODEL:
				case IJavaElement.PACKAGE_FRAGMENT:
				case IJavaElement.CLASS_FILE:
					if (isRemoved)
						return true;
					return processChildrenDelta(delta);
				case IJavaElement.COMPILATION_UNIT:
					// Not the primary compilation unit. Ignore it
					if (!JavaModelUtil.isPrimary((ICompilationUnit) elem))
						return false;

					if (isRemoved || (isChanged && isUnknownStructuralChange(delta.getFlags())))
						return true;
					return processChildrenDelta(delta);
				default:
					// fields, methods, imports ect
					return false;
			}
		}

		private boolean isUnknownStructuralChange(int flags) {
			if ((flags & IJavaElementDelta.F_CONTENT) == 0)
				return false;
			return (flags & IJavaElementDelta.F_FINE_GRAINED) == 0;
		}

		/*
		private boolean isPossibleStructuralChange(int flags) {
			return (flags & (IJavaElementDelta.F_CONTENT | IJavaElementDelta.F_FINE_GRAINED)) == IJavaElementDelta.F_CONTENT;
		}
		*/

		private boolean processChildrenDelta(IJavaElementDelta delta) {
			IJavaElementDelta[] children = delta.getAffectedChildren();
			for (IJavaElementDelta child : children) {
				if (processDelta(child))
					return true;
			}
			return false;
		}
	}

	private static class UpdateJob extends Job {
		public static final String FAMILY = UpdateJob.class.getName();

		public UpdateJob() {
			super(CorextMessages.TypeInfoHistory_consistency_check);
		}

		protected IStatus run(IProgressMonitor monitor) {
			OpenTypeHistory history = OpenTypeHistory.getInstance();
			history.internalCheckConsistency(monitor);
			return new Status(IStatus.OK, JavaPlugin.getPluginId(), IStatus.OK, "", null); //$NON-NLS-1$
		}

		public boolean belongsTo(Object family) {
			return FAMILY.equals(family);
		}
	}

	// Needs to be volatile since accesses aren't synchronized.
	private volatile boolean fNeedsConsistencyCheck;
	// Map of cached time stamps
	private final Map<TypeNameMatch, Long> fTimestampMapping;

	private final IElementChangedListener fDeltaListener;
	private final UpdateJob fUpdateJob;

	private static final String FILENAME = "OpenTypeHistory.xml"; //$NON-NLS-1$
	private static final String NODE_ROOT = "typeInfoHistroy"; //$NON-NLS-1$
	private static final String NODE_TYPE_INFO = "typeInfo"; //$NON-NLS-1$
	private static final String NODE_HANDLE = "handle"; //$NON-NLS-1$
	private static final String NODE_MODIFIERS = "modifiers";  //$NON-NLS-1$
	private static final String NODE_TIMESTAMP = "timestamp"; //$NON-NLS-1$

	private static OpenTypeHistory fgInstance;

	public static synchronized OpenTypeHistory getInstance() {
		if (fgInstance == null)
			fgInstance = new OpenTypeHistory();
		return fgInstance;
	}

	public static synchronized void shutdown() {
		if (fgInstance == null)
			return;
		fgInstance.doShutdown();
	}

	private OpenTypeHistory() {
		super(FILENAME, NODE_ROOT, NODE_TYPE_INFO);
		fTimestampMapping = new HashMap<>();
		fNeedsConsistencyCheck = true;
		load();
		fDeltaListener = new TypeHistoryDeltaListener();
		JavaCore.addElementChangedListener(fDeltaListener);
		fUpdateJob = new UpdateJob();
		// It is not necessary anymore that the update job has a rule since
		// markAsInconsistent isn't synchronized anymore. See bugs
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=128399 and
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=135278
		// for details.
		fUpdateJob.setPriority(Job.SHORT);
	}

	public void markAsInconsistent() {
		fNeedsConsistencyCheck = true;
		// cancel the old job. If no job is running this is a NOOP.
		fUpdateJob.cancel();
		fUpdateJob.schedule();
	}

	public boolean needConsistencyCheck() {
		return fNeedsConsistencyCheck;
	}

	public void checkConsistency(IProgressMonitor monitor) throws OperationCanceledException {
		if (!fNeedsConsistencyCheck)
			return;
		if (fUpdateJob.getState() == Job.RUNNING) {
			try {
				Job.getJobManager().join(UpdateJob.FAMILY, monitor);
			}
			catch (OperationCanceledException | InterruptedException e) {
				// Ignore and do the consistency check without
				// waiting for the update job.
			}
		}
		if (!fNeedsConsistencyCheck)
			return;
		internalCheckConsistency(monitor);
	}

	public synchronized boolean contains(TypeNameMatch type) {
		return super.contains(type);
	}

	public synchronized void accessed(TypeNameMatch info) {
		// Fetching the timestamp might not be cheap (remote file system
		// external Jars. So check if we alreay have one.
		if (!fTimestampMapping.containsKey(info))
			fTimestampMapping.put(info, getContainerTimestamp(info));
		super.accessed(info);
	}

	public synchronized TypeNameMatch remove(TypeNameMatch info) {
		fTimestampMapping.remove(info);
		return super.remove(info);
	}

	public synchronized void replace(TypeNameMatch old, TypeNameMatch newMatch) {
		fTimestampMapping.remove(old);
		fTimestampMapping.put(newMatch, getContainerTimestamp(newMatch));
		super.remove(old);
		super.accessed(newMatch);
	}

	public synchronized TypeNameMatch[] getTypeInfos() {
		Collection<TypeNameMatch> values = getValues();
		int size = values.size();
		TypeNameMatch[] result = new TypeNameMatch[size];
		int i = size - 1;
		for (TypeNameMatch value : values)
			result[i--] = value;
		return result;
	}

	public synchronized TypeNameMatch[] getFilteredTypeInfos(TypeInfoFilter filter) {
		Collection<TypeNameMatch> values = getValues();
		List<TypeNameMatch> result = new ArrayList<>();
		for (TypeNameMatch type : values) {
			if ((filter == null || filter.matchesHistoryElement(type)) && !TypeFilter.isFiltered(type.getFullyQualifiedName()))
				result.add(type);
		}
		Collections.reverse(result);
		return result.toArray(new TypeNameMatch[0]);
	}

	@Override
	protected Key<TypeNameMatch> getKey(TypeNameMatch element) {
		return new Key<>(element);
	}

	private synchronized void internalCheckConsistency(IProgressMonitor monitor) throws OperationCanceledException {
		// Setting fNeedsConsistencyCheck is necessary here since
		// markAsInconsistent isn't synchronized.
		fNeedsConsistencyCheck = true;
		List<Key<TypeNameMatch>> typesToCheck = new ArrayList<>(getKeys());
		monitor.beginTask(CorextMessages.TypeInfoHistory_consistency_check, typesToCheck.size());
		monitor.setTaskName(CorextMessages.TypeInfoHistory_consistency_check);
		for (Key<TypeNameMatch> typeKey : typesToCheck) {
			TypeNameMatch type = typeKey.getKey();
			long currentTimestamp = getContainerTimestamp(type);
			Long lastTested = fTimestampMapping.get(type);
			if (lastTested != null && currentTimestamp != IResource.NULL_STAMP && currentTimestamp == lastTested && !isContainerDirty(type))
				continue;
			try {
				IType jType = type.getType();
				if (jType == null || !jType.exists())
					remove(type);
				else {
					// copy over the modifiers since they may have changed
					int modifiers = jType.getFlags();
					if (modifiers != type.getModifiers())
						replace(type, SearchEngine.createTypeNameMatch(jType, modifiers));
					else
						fTimestampMapping.put(type, currentTimestamp);
				}
			}
			catch (JavaModelException e) {
				remove(type);
			}
			if (monitor.isCanceled())
				throw new OperationCanceledException();
			monitor.worked(1);
		}
		monitor.done();
		fNeedsConsistencyCheck = false;
	}

	private long getContainerTimestamp(TypeNameMatch match) {
		try {
			IType type = match.getType();
			IResource resource = type.getResource();
			if (resource != null) {
				URI location = resource.getLocationURI();
				if (location != null) {
					IFileInfo info = EFS.getStore(location).fetchInfo();
					if (info.exists()) {
						// The element could be removed from the build path. So check
						// if the Java element still exists.
						IJavaElement element = JavaCore.create(resource);
						if (element != null && element.exists())
							return info.getLastModified();
					}
				}
			}
			else { // external JAR
				IPackageFragmentRoot root = match.getPackageFragmentRoot();
				IFileInfo info = EFS.getLocalFileSystem().getStore(root.getPath()).fetchInfo();
				if (info.exists())
					return info.getLastModified();
			}
		}
		catch (CoreException e) {
			// Fall through
		}
		return IResource.NULL_STAMP;
	}

	public boolean isContainerDirty(TypeNameMatch match) {
		ICompilationUnit cu = match.getType().getCompilationUnit();
		if (cu == null)
			return false;
		IResource resource = cu.getResource();
		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
		ITextFileBuffer textFileBuffer = manager.getTextFileBuffer(resource.getFullPath());
		if (textFileBuffer != null)
			return textFileBuffer.isDirty();
		return false;
	}

	private void doShutdown() {
		JavaCore.removeElementChangedListener(fDeltaListener);
		save();
	}

	@Override
	protected TypeNameMatch createFromElement(Element type) {
		String handle = type.getAttribute(NODE_HANDLE);
		if (handle == null)
			return null;

		IJavaElement element = JavaCore.create(handle);
		if (!(element instanceof IType))
			return null;

		int modifiers = 0;
		try {
			modifiers = Integer.parseInt(type.getAttribute(NODE_MODIFIERS));
		}
		catch (NumberFormatException e) {
			// take zero
		}
		TypeNameMatch info = SearchEngine.createTypeNameMatch((IType) element, modifiers);
		long timestamp = IResource.NULL_STAMP;
		String timestampValue = type.getAttribute(NODE_TIMESTAMP);
		if (timestampValue != null && timestampValue.length() > 0) {
			try {
				timestamp = Long.parseLong(timestampValue);
			}
			catch (NumberFormatException e) {
				// take null stamp
			}
		}
		if (timestamp != IResource.NULL_STAMP)
			fTimestampMapping.put(info, timestamp);
		return info;
	}

	@Override
	protected void setAttributes(TypeNameMatch element, Element domElement) {
		String handleId = element.getType().getHandleIdentifier();
		domElement.setAttribute(NODE_HANDLE, handleId);
		domElement.setAttribute(NODE_MODIFIERS, Integer.toString(element.getModifiers()));
		Long timestamp = fTimestampMapping.get(element);
		if (timestamp == null)
			domElement.setAttribute(NODE_TIMESTAMP, Long.toString(IResource.NULL_STAMP));
		else
			domElement.setAttribute(NODE_TIMESTAMP, timestamp.toString());
	}

}
