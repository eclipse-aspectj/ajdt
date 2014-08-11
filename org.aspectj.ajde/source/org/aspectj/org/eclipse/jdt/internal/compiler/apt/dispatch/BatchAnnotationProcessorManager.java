/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.internal.compiler.apt.dispatch;

import org.aspectj.org.eclipse.jdt.internal.compiler.batch.Main;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

import javax.annotation.processing.Processor;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * Java 6 annotation processor manager used when compiling from the command line
 * or via the javax.tools.JavaCompiler interface.
 *
 * @see org.aspectj.org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeAnnotationProcessorManager
 */
public class BatchAnnotationProcessorManager extends BaseAnnotationProcessorManager {

  /**
   * Processors that have been set by calling CompilationTask.setProcessors().
   */
  private List<Processor> _setProcessors = null;
  private Iterator<Processor> _setProcessorIter = null;

  /**
   * Processors named with the -processor option on the command line.
   */
  private List<String> _commandLineProcessors;
  private Iterator<String> _commandLineProcessorIter = null;

  private ServiceLoader<Processor> _serviceLoader = null;
  private Iterator<Processor> _serviceLoaderIter;

  private ClassLoader _procLoader;

  // Set this to true in order to trace processor discovery when -XprintProcessorInfo is specified
  private final static boolean VERBOSE_PROCESSOR_DISCOVERY = true;
  private boolean _printProcessorDiscovery = false;

  /**
   * Zero-arg constructor so this object can be easily created via reflection.
   * A BatchAnnotationProcessorManager cannot be used until its
   * {@link #configure(Object, String[])} method has been called.
   */
  public BatchAnnotationProcessorManager() {
  }

  @Override
  public void configure(Object batchCompiler, String[] commandLineArguments) {
    if (null != _processingEnv) {
      throw new IllegalStateException(
              "Calling configure() more than once on an AnnotationProcessorManager is not supported"); //$NON-NLS-1$
    }
    BatchProcessingEnvImpl processingEnv = new BatchProcessingEnvImpl(this, (Main) batchCompiler, commandLineArguments);
    _processingEnv = processingEnv;
    _procLoader = processingEnv.getFileManager().getClassLoader(StandardLocation.ANNOTATION_PROCESSOR_PATH);
    parseCommandLine(commandLineArguments);
    _round = 0;
  }

  /**
   * If a -processor option was specified in command line arguments,
   * parse it into a list of qualified classnames.
   *
   * @param commandLineArguments contains one string for every space-delimited token on the command line
   */
  private void parseCommandLine(String[] commandLineArguments) {
    List<String> commandLineProcessors = null;
    for (int i = 0; i < commandLineArguments.length; ++i) {
      String option = commandLineArguments[i];
      if ("-XprintProcessorInfo".equals(option)) { //$NON-NLS-1$
        _printProcessorInfo = true;
        _printProcessorDiscovery = VERBOSE_PROCESSOR_DISCOVERY;
      } else if ("-XprintRounds".equals(option)) { //$NON-NLS-1$
        _printRounds = true;
      } else if ("-processor".equals(option)) { //$NON-NLS-1$
        commandLineProcessors = new ArrayList<String>();
        String procs = commandLineArguments[++i];
        for (String proc : procs.split(",")) { //$NON-NLS-1$
          commandLineProcessors.add(proc);
        }
        break;
      }
    }
    _commandLineProcessors = commandLineProcessors;
    if (null != _commandLineProcessors) {
      _commandLineProcessorIter = _commandLineProcessors.iterator();
    }
  }

  @Override
  public ProcessorInfo discoverNextProcessor() {
    try {
      if (null != _setProcessors) {
        // If setProcessors() was called, use that list until it's empty and then stop.
        if (_setProcessorIter.hasNext()) {
          Processor p = _setProcessorIter.next();
          p.init(_processingEnv);
          ProcessorInfo pi = new ProcessorInfo(p);
          _processors.add(pi);
          if (_printProcessorDiscovery && null != _out) {
            _out.println("API specified processor: " + pi); //$NON-NLS-1$
          }
          return pi;
        }
        return null;
      }

      if (null != _commandLineProcessors) {
        // If there was a -processor option, iterate over processor names,
        // creating and initializing processors, until no more names are found, then stop.
        if (_commandLineProcessorIter.hasNext()) {
          String proc = _commandLineProcessorIter.next();
          try {
            Class<?> clazz = _procLoader.loadClass(proc);
            Object o = clazz.newInstance();
            Processor p = (Processor) o;
            p.init(_processingEnv);
            ProcessorInfo pi = new ProcessorInfo(p);
            _processors.add(pi);
            if (_printProcessorDiscovery && null != _out) {
              _out.println("Command line specified processor: " + pi); //$NON-NLS-1$
            }
            return pi;
          } catch (Exception e) {
            // TODO: better error handling
            throw new AbortCompilation(null, e);
          }
        }
        return null;
      }
      // if no processors were explicitly specified with setProcessors()
      // or the command line, search the processor path with ServiceLoader.
      String resPath = "META-INF/services/" + Processor.class.getName();
      Enumeration<URL> resources = _procLoader.getResources(resPath);
      if (resources != null) {
        while (resources.hasMoreElements()) {
          URL url = resources.nextElement();
          parse(url);
        }
      }
      if (null == _serviceLoader) {
        _serviceLoader = ServiceLoader.load(Processor.class, _procLoader);
        _serviceLoaderIter = _serviceLoader.iterator();
      }
      try {
        if (_serviceLoaderIter.hasNext()) {
          Processor p = _serviceLoaderIter.next();
          p.init(_processingEnv);
          ProcessorInfo pi = new ProcessorInfo(p);
          _processors.add(pi);
          if (_printProcessorDiscovery && null != _out) {
            StringBuilder sb = new StringBuilder();
            sb.append("Discovered processor service "); //$NON-NLS-1$
            sb.append(pi);
            sb.append("\n  supporting "); //$NON-NLS-1$
            sb.append(pi.getSupportedAnnotationTypesAsString());
            sb.append("\n  in "); //$NON-NLS-1$
            sb.append(getProcessorLocation(p));
            _out.println(sb.toString());
          }
          return pi;
        }
      } catch (ServiceConfigurationError e) {
        // TODO: better error handling
        throw new AbortCompilation(null, e);
      }
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
    return null;
  }

  private Iterator<String> parse(URL u)
          throws ServiceConfigurationError, IOException {
    InputStream in = null;
    BufferedReader r = null;
    ArrayList<String> names = new ArrayList<String>();
    try {
      in = u.openStream();
      r = new BufferedReader(new InputStreamReader(in, "utf-8"));
      int lc = 1;
      while ((lc = parseLine(r, lc, names)) >= 0) ;
    } catch (IOException x) {
      throw new IllegalStateException("Error reading configuration file", x);
    } finally {
      try {
        if (r != null) r.close();
        if (in != null) in.close();
      } catch (IOException y) {
        throw new IllegalStateException("Error closing configuration file", y);
      }
    }
    return names.iterator();
  }

  private int parseLine(BufferedReader r, int lc, List<String> names)
          throws IOException, ServiceConfigurationError {
    String ln = r.readLine();
    if (ln == null) {
      return -1;
    }
    int ci = ln.indexOf('#');
    if (ci >= 0) ln = ln.substring(0, ci);
    ln = ln.trim();
    int n = ln.length();
    if (n != 0) {
      if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0))
        throw new IllegalStateException("Illegal configuration-file syntax");
      int cp = ln.codePointAt(0);
      if (!Character.isJavaIdentifierStart(cp))
        throw new IllegalStateException("Illegal provider-class name: " + ln);
      for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
        cp = ln.codePointAt(i);
        if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
          throw new IllegalStateException("Illegal provider-class name: " + ln);
      }
      if (!names.contains(ln))
        names.add(ln);
    }
    return lc + 1;
  }

  /**
   * Used only for debugging purposes.  Generates output like "file:jar:D:/temp/jarfiles/myJar.jar!/".
   * Surely this code already exists in several hundred other places?
   *
   * @return the location whence a processor class was loaded.
   */
  private String getProcessorLocation(Processor p) {
    // Get the classname in a form that can be passed to ClassLoader.getResource(),
    // e.g., "pa/pb/pc/Outer$Inner.class"
    boolean isMember = false;
    Class<?> outerClass = p.getClass();
    StringBuilder innerName = new StringBuilder();
    while (outerClass.isMemberClass()) {
      innerName.insert(0, outerClass.getSimpleName());
      innerName.insert(0, '$');
      isMember = true;
      outerClass = outerClass.getEnclosingClass();
    }
    String path = outerClass.getName();
    path = path.replace('.', '/');
    if (isMember) {
      path = path + innerName;
    }
    path = path + ".class"; //$NON-NLS-1$

    // Find the URL for the class resource and strip off the resource name itself
    String location = _procLoader.getResource(path).toString();
    if (location.endsWith(path)) {
      location = location.substring(0, location.length() - path.length());
    }
    return location;
  }

  @Override
  public void reportProcessorException(Processor p, Exception e) {
    // TODO: if (verbose) report the processor
    throw new AbortCompilation(null, e);
  }

  @Override
  public void setProcessors(Object[] processors) {
    if (!_isFirstRound) {
      throw new IllegalStateException("setProcessors() cannot be called after processing has begun"); //$NON-NLS-1$
    }
    // Cast all the processors here, rather than failing later.
    // But don't call init() until the processor is actually needed.
    _setProcessors = new ArrayList<Processor>(processors.length);
    for (Object o : processors) {
      Processor p = (Processor) o;
      _setProcessors.add(p);
    }
    _setProcessorIter = _setProcessors.iterator();

    // processors set this way take precedence over anything on the command line
    _commandLineProcessors = null;
    _commandLineProcessorIter = null;
  }

}
