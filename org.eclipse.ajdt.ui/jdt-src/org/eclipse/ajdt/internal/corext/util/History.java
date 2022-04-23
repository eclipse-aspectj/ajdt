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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.internal.corext.CorextMessages;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaUIException;
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * History stores a list of key, object pairs. The list is bounded at size
 * MAX_HISTORY_SIZE. If the list exceeds this size the eldest element is removed
 * from the list. An element can be added/renewed with a call to <code>accessed(Object)</code>.
 * <p>
 * The history can be stored to/loaded from an xml file.
 */
public abstract class History<T> {
	private static final String DEFAULT_ROOT_NODE_NAME = "histroyRootNode"; //$NON-NLS-1$
	private static final String DEFAULT_INFO_NODE_NAME = "infoNode"; //$NON-NLS-1$
	private static final int MAX_HISTORY_SIZE = 60;

	private static JavaUIException createException(Throwable t, String message) {
		return new JavaUIException(JavaUIStatus.createError(IStatus.ERROR, message, t));
	}

	private final Map<Key<T>, T> fHistory;
	private final Map<Key<T>, Integer> fPositions;
	private final String fFileName;
	private final String fRootNodeName;
	private final String fInfoNodeName;

	public History(String fileName, String rootNodeName, String infoNodeName) {
		fHistory = new LinkedHashMap<Key<T>, T>(80, 0.75f, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry eldest) {
				return size() > MAX_HISTORY_SIZE;
			}
		};

		fFileName = fileName;
		fRootNodeName = rootNodeName;
		fInfoNodeName = infoNodeName;
		fPositions = new Hashtable<>(MAX_HISTORY_SIZE);
	}

	public History(String fileName) {
		this(fileName, DEFAULT_ROOT_NODE_NAME, DEFAULT_INFO_NODE_NAME);
	}

	protected static class Key<K> {
		private final K key;

		public Key(K key) {
			this.key = key;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Key<?> key1 = (Key<?>) o;
			return Objects.equals(key, key1.key);
		}

		@Override
		public int hashCode() {
			return key != null ? key.hashCode() : 0;
		}

		public K getKey() {
			return key;
		}
	}

	public synchronized void accessed(T element) {
		fHistory.put(getKey(element), element);
		rebuildPositions();
	}

	public synchronized boolean contains(T element) {
		return fHistory.containsKey(getKey(element));
	}

	public synchronized boolean containsKey(Key<T> key) {
		return fHistory.containsKey(key);
	}

	public synchronized boolean isEmpty() {
		return fHistory.isEmpty();
	}

	public synchronized T remove(T element) {
		T removed = fHistory.remove(getKey(element));
		rebuildPositions();
		return removed;
	}

	public synchronized T removeKey(Key<T> key) {
		T removed = fHistory.remove(key);
		rebuildPositions();
		return removed;
	}

	/**
	 * Normalized position in history of object denoted by key.
	 * The position is a value between zero and one where zero
	 * means not contained in history and one means newest element
	 * in history. The lower the value the older the element.
	 *
	 * @param key The key of the object to inspect
	 * @return value in [0.0, 1.0] the lower the older the element
	 */
	public synchronized float getNormalizedPosition(Key<T> key) {
		if (!containsKey(key))
			return 0.0f;
		int pos = fPositions.get(key) + 1;
		//containsKey(key) implies fHistory.size()>0
		return (float) pos / (float) fHistory.size();
	}

	/**
	 * Absolute position of object denoted by key in the
	 * history or -1 if !containsKey(key). The higher the
	 * newer.
	 *
	 * @param key The key of the object to inspect
	 * @return value between 0 and MAX_HISTORY_SIZE - 1, or -1
	 */
	public synchronized int getPosition(Key<T> key) {
		if (!containsKey(key))
			return -1;
		return fPositions.get(key);
	}

	public synchronized void load() {
		IPath stateLocation = JavaPlugin.getDefault().getStateLocation().append(fFileName);
		File file = new File(stateLocation.toOSString());
		if (!file.exists())
			return;
		try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
			load(new InputSource(reader));
		}
		catch (IOException | CoreException e) {
			JavaPlugin.log(e);
		}
	}

	public synchronized void save() {
		IPath stateLocation = JavaPlugin.getDefault().getStateLocation().append(fFileName);
		File file = new File(stateLocation.toOSString());
		try (OutputStream out = Files.newOutputStream(file.toPath())) {
			save(out);
		}
		catch (IOException | CoreException e) {
			JavaPlugin.log(e);
		}
		catch (TransformerFactoryConfigurationError e) {
			// The XML library can be misconficgured (e.g. via
			// -Djava.endorsed.dirs=C:\notExisting\xerces-2_7_1)
			JavaPlugin.log(e);
		}
	}

	protected Set<Key<T>> getKeys() {
		return fHistory.keySet();
	}

	protected Collection<T> getValues() {
		return fHistory.values();
	}

	/**
	 * Store <code>T</code> in <code>Element</code>
	 *
	 * @param element  The object to store
	 * @param domElement The Element to store to
	 */
	protected abstract void setAttributes(T element, Element domElement);

	/**
	 * Return a new instance of an Object given <code>element</code>
	 *
	 * @param element The element containing required information to create the Object
	 */
	protected abstract T createFromElement(Element element);

	/**
	 * Get key for element
	 *
	 * @param element The element to calculate a key for, not null
	 * @return The key for element, not null
	 */
	protected abstract Key<T> getKey(T element);

	private void rebuildPositions() {
		fPositions.clear();
		Collection<T> values = fHistory.values();
		int pos = 0;
		for (T element : values)
			fPositions.put(getKey(element), pos++);
	}

	private void load(InputSource inputSource) throws CoreException {
		Element root;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			root = parser.parse(inputSource).getDocumentElement();
		}
		catch (SAXException | IOException | ParserConfigurationException e) {
			throw createException(e, Messages.format(CorextMessages.History_error_read, fFileName));
		}

		if (root == null)
			return;
		if (!root.getNodeName().equalsIgnoreCase(fRootNodeName))
			return;
		NodeList list = root.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element domElement = (Element) node;
				if (domElement.getNodeName().equalsIgnoreCase(fInfoNodeName)) {
					T element = createFromElement(domElement);
					if (element != null)
						fHistory.put(getKey(element), element);
				}
			}
		}
		rebuildPositions();
	}

	private void save(OutputStream stream) throws CoreException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();

			Element rootElement = document.createElement(fRootNodeName);
			document.appendChild(rootElement);

			for (T element : getValues()) {
				Element domElement = document.createElement(fInfoNodeName);
				setAttributes(element, domElement);
				rootElement.appendChild(domElement);
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(stream);

			transformer.transform(source, result);
		}
		catch (TransformerException | ParserConfigurationException e) {
			throw createException(e, Messages.format(CorextMessages.History_error_serialize, fFileName));
		}
	}

}
