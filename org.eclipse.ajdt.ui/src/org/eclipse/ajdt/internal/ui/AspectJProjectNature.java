/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 *     Matt Chapman - add support for old builder id
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.contribution.jdt.preferences.WeavableProjectListener;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

/**
 * The AspectJ project nature uses the "ajc" builder to compile aspects
 */
public class AspectJProjectNature implements IProjectNature {

	private IProject project;

	// the previous builder id, before the builder was moved to the core plugin
	private static final String OLD_BUILDER = "org.eclipse.ajdt.ui.ajbuilder"; //$NON-NLS-1$

	private static final String JAVA_BUILDER_GENERATE_CLASSES = "org.eclipse.jdt.core.compiler.generateClassFiles"; //$NON-NLS-1$

	/**
	 * Driven when this project nature is 'given' to a project, it adds the
	 * appropriate builder to the project build specification.
	 * 
	 * @todo scan the current list of builders, if it contains
	 *       'org.eclipse.jdt.core.javabuilder' replace that entry with our
	 *       entry, otherwise simply insert our builder as a new entry.
	 */
	public void configure() throws CoreException {

		IProjectDescription projectDescription = project.getDescription();
		ICommand command = projectDescription.newCommand();
		command.setBuilderName(AspectJPlugin.ID_BUILDER);

		ICommand[] buildCommands = projectDescription.getBuildSpec();
		ICommand[] newBuildCommands;
		if (contains(buildCommands, JavaCore.BUILDER_ID)) {
			newBuildCommands = swap(buildCommands, JavaCore.BUILDER_ID, command);
		} else {
			newBuildCommands = insert(buildCommands, command);
		}
		projectDescription.setBuildSpec(newBuildCommands);
		project.setDescription(projectDescription, null);
		try {
		    // ask if weaving service should be enable
		    WeavableProjectListener.weavableNatureAdded(project);
		} catch (Exception e) {
		    
		}
	}

	/**
	 * Remove the AspectJ Builder from the list, replace with the javabuilder
	 */
	public void deconfigure() throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] buildCommands = description.getBuildSpec();
		ICommand command = description.newCommand();
		command.setBuilderName(JavaCore.BUILDER_ID);

		ICommand[] newBuildCommands;
		if (contains(buildCommands, AspectJPlugin.ID_BUILDER)) {
			newBuildCommands = swap(buildCommands, AspectJPlugin.ID_BUILDER,
					command);
		} else if (contains(buildCommands, OLD_BUILDER)) {
			newBuildCommands = swap(buildCommands, OLD_BUILDER, command);
		} else {
			newBuildCommands = remove(buildCommands, AspectJPlugin.ID_BUILDER);
		}

		// we might have started with both builders - one will have been
		// replaced above, we now need to make sure the other one is removed
		// if present
		if (contains(newBuildCommands, AspectJPlugin.ID_BUILDER)) {
			newBuildCommands = remove(newBuildCommands,
					AspectJPlugin.ID_BUILDER);
		}
		if (contains(newBuildCommands, OLD_BUILDER)) {
			newBuildCommands = remove(newBuildCommands, OLD_BUILDER);
		}

		description.setBuildSpec(newBuildCommands);
		project.setDescription(description, null);
	}

	/**
	 * Remove the old AspectJ Builder from the given project
	 */
	public static void removeOldBuilder(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] buildCommands = description.getBuildSpec();
		if (contains(buildCommands, OLD_BUILDER)) {
			ICommand[] newBuildCommands = remove(buildCommands, OLD_BUILDER);
			description.setBuildSpec(newBuildCommands);
			project.setDescription(description, null);
		}
	}

    /**
     * Remove the old AspectJ Builder from the given project
     */
    public static void removeJavaBuilder(IProject project) throws CoreException {
        IProjectDescription description = project.getDescription();
        ICommand[] buildCommands = description.getBuildSpec();
        if (contains(buildCommands, JavaCore.BUILDER_ID)) {
            ICommand[] newBuildCommands = remove(buildCommands, JavaCore.BUILDER_ID);
            description.setBuildSpec(newBuildCommands);
            project.setDescription(description, null);
        }
    }

    /**
	 * Add the new AspectJ Builder to the given project
	 */
	public static void addNewBuilder(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		ICommand[] buildCommands = description.getBuildSpec();
		if (!contains(buildCommands, AspectJPlugin.ID_BUILDER)) {
			ICommand command = description.newCommand();
			command.setBuilderName(AspectJPlugin.ID_BUILDER);
			ICommand[] newBuildCommands = insert(buildCommands, command);
			description.setBuildSpec(newBuildCommands);
			project.setDescription(description, null);
		}
	}

    public static boolean hasNewBuilder(IProject project) throws CoreException {
        ICommand[] buildCommands = project.getDescription().getBuildSpec();
        return contains(buildCommands, AspectJPlugin.ID_BUILDER);
    }

    public static boolean hasJavaBuilder(IProject project) throws CoreException {
        ICommand[] buildCommands = project.getDescription().getBuildSpec();
        return contains(buildCommands, JavaCore.BUILDER_ID);
    }

	/**
	 * @see IProjectNature#getProject
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * @see IProjectNature#setProject
	 */
	public void setProject(IProject value) {
		project = value;
	}

	/**
	 * Check if the given build command list contains a given command
	 */
	private static boolean contains(ICommand[] commands, String builderId) {
		boolean found = false;
		for (int i = 0; i < commands.length; i++) {
			if (commands[i].getBuilderName().equals(builderId)) {
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * In a list of build commands, swap all occurences of one entry for another
	 */
	private static ICommand[] swap(ICommand[] sourceCommands,
			String oldBuilderId, ICommand newCommand) {
		ICommand[] newCommands = new ICommand[sourceCommands.length];
		for (int i = 0; i < sourceCommands.length; i++) {
			if (sourceCommands[i].getBuilderName().equals(oldBuilderId)) {
				newCommands[i] = newCommand;
			} else {
				newCommands[i] = sourceCommands[i];
			}
		}
		return newCommands;
	}

	/**
	 * Insert a new build command at the front of an existing list
	 */
	private static ICommand[] insert(ICommand[] sourceCommands, ICommand command) {
		ICommand[] newCommands = new ICommand[sourceCommands.length + 1];
		newCommands[0] = command;
		for (int i = 0; i < sourceCommands.length; i++) {
			newCommands[i + 1] = sourceCommands[i];
		}
		return newCommands;
	}

	/**
	 * Remove a build command from a list
	 */
	private static ICommand[] remove(ICommand[] sourceCommands, String builderId) {
		ICommand[] newCommands = new ICommand[sourceCommands.length - 1];
		int newCommandIndex = 0;
		for (int i = 0; i < sourceCommands.length; i++) {
			if (!sourceCommands[i].getBuilderName().equals(builderId)) {
				newCommands[newCommandIndex++] = sourceCommands[i];
			}
		}
		return newCommands;
	}

	/**
	 * Does java builder command in given project has got binary file generation disabled.
	 * @param project2
	 * @return
	 * @throws CoreException 
	 */
	public static boolean isClassGenerationDisabled(
			IProject project) throws CoreException {
		Collection resultCommands = findBuilder(project, JavaCore.BUILDER_ID);
		for (Iterator it = resultCommands.iterator(); it.hasNext();) {
			ICommand javaBuilderCommand = (ICommand) it.next();
			Map arguments = javaBuilderCommand.getArguments();
			String value = (String) arguments.get(JAVA_BUILDER_GENERATE_CLASSES);
			if (value == null || !value.equals(Boolean.FALSE.toString())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Find builder commands in given project
	 * 
	 * @param project
	 *            project to search in
	 * @param builderId
	 *            id of the builder to find
	 * @return all commands of specified builder or empty collection if no
	 *         commands for given builder was found
	 * @throws CoreException
	 */
	private static Collection findBuilder(IProject project, String builderId)
			throws CoreException {
		ICommand[] commands = project.getDescription().getBuildSpec();
		Collection resultCommands = new ArrayList();
		for (int i = 0; i < commands.length; i++) {
			if (commands[i].getBuilderName().equals(builderId)) {
				resultCommands.add(commands[i]);
			}
		}
		return resultCommands;
	}
}