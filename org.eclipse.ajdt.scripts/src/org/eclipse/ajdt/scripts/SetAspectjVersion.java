/*******************************************************************************
 * Copyright (c) 2025 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ajdt.scripts;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

/** Usage:
 * <pre>{@code
 * java org.eclipse.ajdt.scripts/src/org/eclipse/ajdt/scripts/SetAspectjVersion.java 1.9.24
 * }</pre>
 */
public final class SetAspectjVersion {
	private final Maven maven;
	
	public static void main(String[] args) {
		Maven maven = new Maven("mvn", getGitRoot());
		SetAspectjVersion instance = new SetAspectjVersion(maven);
		instance.setAspectjVersion(args[0]);
		
	}
	
	private SetAspectjVersion(Maven maven) {
		this.maven = requireNonNull(maven);
	}
	
	private void setAspectjVersion(String version) {
		maven.execute("versions:set-property", "-Dproperty=aspectj.version", "-DnewVersion="+version);
		String snapshotVersion = version.endsWith("-SNAPSHOT") ? version : version + "-SNAPSHOT";
		setVersion("org.aspectj.weaver", snapshotVersion);
		setVersion("org.aspectj.ajde", snapshotVersion);
		setVersion("org.aspectj.runtime", snapshotVersion);
		setVersion("org.aspectj-feature", snapshotVersion);
	}

	private void setVersion(String mavenProjectRelativePath, String version) {
		maven.execute("org.eclipse.tycho:tycho-versions-plugin:set-version", "--file", mavenProjectRelativePath, "-DnewVersion=" + version, "-DupdateVersionRangeMatchingBounds");
	}

	private static final class Maven {
		private final String mavenCommand;
		private final Path workingDirectory;
		
		public Maven(String mavenCommand, Path workingDirectory) {
			this.mavenCommand = requireNonNull(mavenCommand);
			this.workingDirectory = requireNonNull(workingDirectory);
			if (!Files.isDirectory(workingDirectory) || !Files.isWritable(workingDirectory)) {
				throw new IllegalArgumentException(workingDirectory.toString());
			}
			execute("--version");
		}
		
		public void execute(String ... arguments) {
			ArrayList<String> commandLine = new ArrayList<>();
			commandLine.add(mavenCommand);
			commandLine.add("--batch-mode");
			commandLine.addAll(Arrays.asList(arguments));
			ProcessBuilder process = new ProcessBuilder(commandLine).directory(workingDirectory.toFile()).inheritIO();
			try {
				int exitCode = process.start().waitFor();
				if (exitCode != 0) {
					throw new IllegalStateException("Failed to execute " + process.command());
				}
			} catch (InterruptedException | IOException e) {
				throw new AssertionError(e);
			}
			
		}
	}

	private static Path getGitRoot()  {
		try {
			Path classFilePath = Path.of(SetAspectjVersion.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			Path gitRootPath = classFilePath;
			while (!Files.isDirectory(gitRootPath.resolve(".git"))) {
				gitRootPath = gitRootPath.getParent();
				if (gitRootPath == null) {
					throw new IllegalStateException("The script is not launched from AJDT project directory. Searched from: " + classFilePath);
				}
			}
			return gitRootPath;
		} catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
	}
}
