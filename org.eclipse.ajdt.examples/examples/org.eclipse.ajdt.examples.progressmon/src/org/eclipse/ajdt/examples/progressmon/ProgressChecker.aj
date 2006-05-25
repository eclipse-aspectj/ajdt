/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.examples.progressmon;

import java.util.Map;
import java.util.WeakHashMap;

import org.aspectj.lang.JoinPoint.StaticPart;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * An aspect to check for proper usage of the Eclipse IProgressMonitor
 * interface, as dicussed in the following Eclipse Corner article:
 * 
 * "How to Correctly and Uniformly Use Progress Monitors"
 * http://www.eclipse.org/articles/Article-Progress-Monitors/article.html
 */
public aspect ProgressChecker {

	// map to associate each progress monitor instance with a status object
	private static Map<IProgressMonitor, MonitorStatus> monitorMap = new WeakHashMap<IProgressMonitor, MonitorStatus>();

	pointcut callsToBeginTask() : call(void IProgressMonitor.beginTask(..));

	before(IProgressMonitor mon, int ticks) : callsToBeginTask()
			&& args(..,ticks) && target(mon) {
		String location = locationToString(thisEnclosingJoinPointStaticPart,
				thisJoinPointStaticPart);
		MonitorStatus status = monitorMap.get(mon);
		if (status == null) {
			status = new MonitorStatus();
			monitorMap.put(mon, status);
		}
		if (status.doneCalled) {
			String msg = "Call to IProgressMonitor.beginTask() occurred after a call to done()";
			String loc1 = "Location of beginTask() call: " + location;
			String loc2 = "Location of done() call: " + status.doneLocation;
			ProgressCheckerUtil.report(RuleViolations.CALL_BEGIN_AFTER_DONE,
					msg, loc1, loc2);
			return;
		}
		if (status.beginCalled) {
			String msg = "IProgressMonitor.beginTask() has been called twice on the same progress monitor";
			String loc1 = "Location of first beginTask() call: "
					+ status.beginLocation;
			String loc2 = "Location of second beginTask() call: " + location;
			ProgressCheckerUtil.report(RuleViolations.CALL_BEGIN_TWICE, msg,
					loc1, loc2);
			return;
		}

		status.beginCalled = true;
		status.beginLocation = location;
		status.totalWork = ticks;
	}

	pointcut callsToDone() : call(void IProgressMonitor.done(..));

	before(IProgressMonitor mon) : callsToDone() && target(mon) {
		String location = locationToString(thisEnclosingJoinPointStaticPart,
				thisJoinPointStaticPart);
		MonitorStatus status = monitorMap.get(mon);
		if (status == null) {
			status = new MonitorStatus();
			monitorMap.put(mon, status);
		}
		if (status.doneCalled) {
			String msg = "IProgressMonitor.done() has been called twice on the same progress monitor";
			String loc1 = "Location of first done() call: "
					+ status.doneLocation;
			String loc2 = "Location of second done() call: " + location;
			ProgressCheckerUtil.report(RuleViolations.CALL_DONE_TWICE, msg,
					loc1, loc2);
			return;
		}
		if (!status.beginCalled) {
			String msg = "Call to IProgressMonitor.done() occurred without a prior call to beginTask()";
			String loc1 = "Location of done() call: " + location;
			ProgressCheckerUtil.report(RuleViolations.CALL_DONE_WITHOUT_BEGIN,
					msg, loc1, null);
		}
		status.doneCalled = true;
		status.doneLocation = location;
	}

	pointcut callsToWorked() : call(void IProgressMonitor.worked(..));

	before(IProgressMonitor mon, int ticks) : callsToWorked() && args(ticks) && target(mon) {
		String location = locationToString(thisEnclosingJoinPointStaticPart,
				thisJoinPointStaticPart);
		MonitorStatus status = monitorMap.get(mon);
		if (status == null) {
			status = new MonitorStatus();
			monitorMap.put(mon, status);
		}
		if (!status.beginCalled) {
			String msg = "Call to IProgressMonitor.worked() occurred without a prior call to beginTask()";
			String loc1 = "Location of worked() call: " + location;
			ProgressCheckerUtil.report(
					RuleViolations.CALL_WORKED_WITHOUT_BEGIN, msg, loc1, null);
			return;
		}
		if (status.doneCalled) {
			String msg = "IProgressMonitor.worked() has been called after done()";
			String loc1 = "Location of worked() call: " + location;
			String loc2 = "Location of done() call: " + status.doneLocation;
			ProgressCheckerUtil.report(RuleViolations.CALL_WORKED_AFTER_DONE,
					msg, loc1, loc2);
			return;
		}
		status.amountWorked += ticks;
		if (status.amountWorked > status.totalWork) {
			String msg = "Progress monitor has over-reported work units ("
					+ status.amountWorked + " out of " + status.totalWork + ")";
			String loc1 = "Location of last worked() call: " + location;
			ProgressCheckerUtil.report(RuleViolations.OVER_REPORTING, msg,
					loc1, null);
		}
	}

	pointcut callsToNewSubMonitor() : call(SubProgressMonitor.new(..));

	before(IProgressMonitor mon, int ticks) : callsToNewSubMonitor() && (args(mon,ticks,..)){
		String location = locationToString(thisEnclosingJoinPointStaticPart,
				thisJoinPointStaticPart);
		MonitorStatus status = monitorMap.get(mon);
		if (status == null) {
			status = new MonitorStatus();
			monitorMap.put(mon, status);
		}
		if (!status.beginCalled) {
			String msg = "SubProgressMonitor created for a monitor without a beginTask() call";
			String loc1 = "Location of SubProgressMonitor constructor: "
					+ location;
			ProgressCheckerUtil.report(
					RuleViolations.SUBPROGRESS_WITHOUT_BEGIN, msg, loc1, null);
			return;
		}
		if (status.doneCalled) {
			String msg = "SubProgressMonitor created for a monitor which has already called done()";
			String loc1 = "Location of SubProgressMonitor constructor: "
					+ location;
			ProgressCheckerUtil.report(RuleViolations.SUBPROGRESS_AFTER_DONE,
					msg, loc1, null);
			return;
		}
		status.amountWorked += ticks;
		if (status.amountWorked > status.totalWork) {
			String msg = "Progress monitor has over-reported work units ("
					+ status.amountWorked + " out of " + status.totalWork + ")";
			String loc1 = "Location of over-reporting SubProgressMonitor constructor: "
					+ location;
			ProgressCheckerUtil.report(RuleViolations.OVER_REPORTING, msg,
					loc1, null);
		}
	}

	/* convert the location into string form, like a stack trace entry, so that
	 * hyperlinking works in the console view
	 */
	private String locationToString(StaticPart enclosingSP, StaticPart thisSP) {
		return " " + enclosingSP.getSignature().getDeclaringTypeName() + '.'
				+ enclosingSP.getSignature().getName() + '('
				+ thisSP.getSourceLocation().toString() + ')';
	}

	class MonitorStatus {
		boolean beginCalled;

		String beginLocation;

		boolean doneCalled;

		String doneLocation;

		int totalWork;

		int amountWorked;
	}
}
