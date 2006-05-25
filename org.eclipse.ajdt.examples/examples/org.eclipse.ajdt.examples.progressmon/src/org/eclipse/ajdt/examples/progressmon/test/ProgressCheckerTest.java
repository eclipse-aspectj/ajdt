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
package org.eclipse.ajdt.examples.progressmon.test;

import junit.framework.TestCase;

import org.eclipse.ajdt.examples.progressmon.IProgressCheckerReporter;
import org.eclipse.ajdt.examples.progressmon.ProgressCheckerUtil;
import org.eclipse.ajdt.examples.progressmon.RuleViolations;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;


public class ProgressCheckerTest extends TestCase {

	private TestProgressCheckerReporter report;

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() throws Exception {
		report = new TestProgressCheckerReporter();
		ProgressCheckerUtil.setReporter(report);
	}

	/**
	 * Tears down the fixture, for example, close a network connection. This
	 * method is called after a test is executed.
	 */
	protected void tearDown() throws Exception {
	}

	private void checkOneLocationReport(RuleViolations expectedCode,
			String expectedMessageSubstring, String expectedLoc1Substring) {
		assertEquals(
				"Did not receive a rule violation report with the expected code",
				expectedCode, report.lastCode);
		assertNotNull("Did not receive a rule violation report with a message",
				report.lastMessage);
		assertNotNull(
				"Did not receive a rule violation report with a first location",
				report.lastLoc1);
		assertTrue("Expected report message to contain: "
				+ expectedMessageSubstring + ". Got: " + report.lastMessage,
				report.lastMessage.indexOf(expectedMessageSubstring) != -1);
		assertTrue("Expected first location to contain: "
				+ expectedLoc1Substring + ". Got: " + report.lastLoc1,
				report.lastLoc1.indexOf(expectedLoc1Substring) != -1);
		assertNull("Expected second location to be null. Got: "
				+ report.lastLoc2, report.lastLoc2);
	}

	private void checkTwoLocationReport(RuleViolations expectedCode,
			String expectedMessageSubstring, String expectedLoc1Substring,
			String expectedLoc2Substring) {
		assertEquals(
				"Did not receive a rule violation report with the expected code",
				expectedCode, report.lastCode);
		assertNotNull("Did not receive a rule violation report with a message",
				report.lastMessage);
		assertNotNull(
				"Did not receive a rule violation report with a first location",
				report.lastLoc1);
		assertNotNull(
				"Did not receive a rule violation report with a second location",
				report.lastLoc2);
		assertTrue("Expected report message to contain: "
				+ expectedMessageSubstring + ". Got: " + report.lastMessage,
				report.lastMessage.indexOf(expectedMessageSubstring) != -1);
		assertTrue("Expected first location to contain: "
				+ expectedLoc1Substring + ". Got: " + report.lastLoc1,
				report.lastLoc1.indexOf(expectedLoc1Substring) != -1);
		assertTrue("Expected second location to contain: "
				+ expectedLoc2Substring + ". Got: " + report.lastLoc2,
				report.lastLoc2.indexOf(expectedLoc2Substring) != -1);
	}

	public void testCallingBeginTaskTwice() {
		IProgressMonitor monitor = new TestProgressMonitor();
		monitor.beginTask("Test", 1);
		monitor.beginTask("Test2", 2); // bad
		monitor.done();
		checkTwoLocationReport(RuleViolations.CALL_BEGIN_TWICE, "beginTask",
				"ProgressCheckerTest.testCallingBeginTaskTwice",
				"ProgressCheckerTest.testCallingBeginTaskTwice");
	}

	public void testCallingBeginTaskTwiceWithSubTask() {
		IProgressMonitor monitor = new TestProgressMonitor();
		monitor.beginTask("Test", 1);
		IProgressMonitor sub = new SubProgressMonitor(monitor, 1);
		sub.beginTask("Test2", 2); // this is ok
		sub.done();
		monitor.done();
		assertNull("Should not have received a rule violation report. Got: "
				+ report.lastMessage, report.lastMessage);
	}

	private void delegateBegin(Object object) {
		if (object instanceof IProgressMonitor) {
			((IProgressMonitor) object).beginTask("Test2", 1); // bad
		}
	}

	public void testCallingBeginTaskTwiceWithDelegation() {
		IProgressMonitor monitor = new TestProgressMonitor();
		monitor.beginTask("Test", 1);
		delegateBegin(monitor);
		monitor.done();
		checkTwoLocationReport(RuleViolations.CALL_BEGIN_TWICE, "beginTask",
				"ProgressCheckerTest.testCallingBeginTaskTwiceWithDelegation",
				"ProgressCheckerTest.delegateBegin");
	}

	public void testCallingBeginTaskAfterDone() {
		IProgressMonitor monitor = new TestProgressMonitor();
		monitor.beginTask("Test", 1);
		monitor.done();
		monitor.beginTask("Test2", 2); // bad
		checkTwoLocationReport(RuleViolations.CALL_BEGIN_AFTER_DONE,
				"beginTask",
				"ProgressCheckerTest.testCallingBeginTaskAfterDone",
				"ProgressCheckerTest.testCallingBeginTaskAfterDone");
	}

	public void testCallingDoneTwice() {
		IProgressMonitor monitor = new TestProgressMonitor();
		monitor.beginTask("Test", 1);
		monitor.done();
		monitor.done();
		checkTwoLocationReport(RuleViolations.CALL_DONE_TWICE, "done",
				"ProgressCheckerTest.testCallingDoneTwice",
				"ProgressCheckerTest.testCallingDoneTwice");
	}

	public void testCallingDoneWithoutBegin() {
		IProgressMonitor monitor = new TestProgressMonitor();
		monitor.done(); // bad
		checkOneLocationReport(RuleViolations.CALL_DONE_WITHOUT_BEGIN, "done",
				"ProgressCheckerTest.testCallingDoneWithoutBegin");
	}

	public void testCallingWorkedAfterDone() {
		IProgressMonitor monitor = new TestProgressMonitor();
		monitor.beginTask("Test", 1);
		monitor.done();
		monitor.worked(1); // bad
		checkTwoLocationReport(RuleViolations.CALL_WORKED_AFTER_DONE, "worked",
				"ProgressCheckerTest.testCallingWorkedAfterDone",
				"ProgressCheckerTest.testCallingWorkedAfterDone");
	}

	public void testOverReporting() {
		IProgressMonitor monitor = new TestProgressMonitor();
		monitor.beginTask("Test", 1);
		monitor.worked(1);
		monitor.worked(1); // bad
		monitor.done();
		checkOneLocationReport(RuleViolations.OVER_REPORTING, "2",
				"ProgressCheckerTest.testOverReporting");
	}

	public void testOverReportingWithSubTask() {
		IProgressMonitor monitor = new TestProgressMonitor();
		monitor.beginTask("Test", 1);
		monitor.worked(1);
		new SubProgressMonitor(monitor, 1); // bad
		monitor.done();
		checkOneLocationReport(RuleViolations.OVER_REPORTING, "2",
				"ProgressCheckerTest.testOverReportingWithSubTask");
	}

	public void testSubProgressWithoutBegin() {
		IProgressMonitor monitor = new TestProgressMonitor();
		new SubProgressMonitor(monitor, 1); // bad
		checkOneLocationReport(RuleViolations.SUBPROGRESS_WITHOUT_BEGIN,
				"SubProgressMonitor",
				"ProgressCheckerTest.testSubProgressWithoutBegin");
	}

	public void testSubProgressAfterDone() {
		IProgressMonitor monitor = new TestProgressMonitor();
		monitor.beginTask("Test", 1);
		monitor.done();
		new SubProgressMonitor(monitor, 1); // bad
		checkOneLocationReport(RuleViolations.SUBPROGRESS_AFTER_DONE,
				"SubProgressMonitor",
				"ProgressCheckerTest.testSubProgressAfterDone");
	}
}

class TestProgressCheckerReporter implements IProgressCheckerReporter {

	public String lastMessage;

	public String lastLoc1;

	public String lastLoc2;

	public RuleViolations lastCode;

	public void reportRuleViolation(RuleViolations code, String message,
			String loc1, String loc2) {
		System.err.println(message);
		System.err.println(loc1);
		if (loc2 != null)
			System.err.println(loc2);
		lastCode = code;
		lastMessage = message;
		lastLoc1 = loc1;
		lastLoc2 = loc2;
	}

}

class TestProgressMonitor implements IProgressMonitor {

	public void beginTask(String name, int totalWork) {
	}

	public void done() {
	}

	public void internalWorked(double work) {
	}

	public boolean isCanceled() {
		return false;
	}

	public void setCanceled(boolean value) {
	}

	public void setTaskName(String name) {
	}

	public void subTask(String name) {
	}

	public void worked(int work) {
	}

}