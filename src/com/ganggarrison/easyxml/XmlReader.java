/*
 * Copyright (C) 2010 Medo <smaxein@googlemail.com>
 * 
 * This file is part of GmkSplitter.
 * GmkSplitter is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */
package com.ganggarrison.easyxml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XmlReader {
	private final Document domDocument;
	private Element currentParent = null;
	private Element currentChild = null;

	public XmlReader(File xmlFile) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			domDocument = builder.parse(xmlFile);
		} catch (SAXException e) {
			throw new IOException(e);
		} catch (ParserConfigurationException e) {
			throw new AssertionError(e);
		}
	}

	public void enterElement(String elementName) {
		elementName = elementName.toLowerCase();
		if (currentParent == null) {
			currentParent = domDocument.getDocumentElement();
		} else {
			currentParent = getNextChildElement();
		}
		currentChild = null;
		if (currentParent == null) {
			throw new IllegalArgumentException("No child element found while attempting to enter element "
					+ elementName);
		}
		if (!elementName.equals(currentParent.getTagName().toLowerCase())) {
			throw new IllegalArgumentException("Unexpected tag name. Expected: " + elementName + ", got:"
					+ currentParent.getTagName());
		}
	}

	public void leaveElement() {
		currentChild = currentParent;
		Node parentNode = currentParent.getParentNode();
		if (!(parentNode instanceof Element)) {
			parentNode = null;
		}
		currentParent = (Element) parentNode;
	}

	public double getDoubleElement(String elementName) {
		return Double.valueOf(getStringElement(elementName));
	}

	public int getIntElement(String elementName) {
		return Integer.valueOf(getStringElement(elementName));
	}

	public boolean getBoolElement(String elementName) {
		return Boolean.valueOf(getStringElement(elementName));
	}

	public String getStringElement(String elementName) {
		elementName = elementName.toLowerCase();
		nextChild();
		if (currentChild == null || !elementName.equals(currentChild.getTagName().toLowerCase())) {
			throw new IllegalArgumentException("Element with name " + elementName + " expected but not found.");
		} else {
			return currentChild.getTextContent();
		}
	}

	public double getDoubleAttribute(String attributeName) {
		return Double.valueOf(getStringAttribute(attributeName));
	}

	public int getIntAttribute(String attributeName) {
		return Integer.valueOf(getStringAttribute(attributeName));
	}

	public boolean getBoolAttribute(String attributeName) {
		return Boolean.valueOf(getStringAttribute(attributeName));
	}

	public String getStringAttribute(String attributeName) {
		if (!currentParent.hasAttribute(attributeName)) {
			throw new IllegalArgumentException("Attribute with name " + attributeName + " expected in element "
					+ currentParent.getTagName() + " but not found.");
		}
		return currentParent.getAttribute(attributeName);
	}

	public String getTextContent() {
		return currentParent.getTextContent();
	}

	public boolean hasNextElement() {
		return getNextChildElement() != null;
	}

	private void nextChild() {
		currentChild = getNextChildElement();
	}

	private Element getNextChildElement() {
		Element nextElement;
		if (currentChild == null) {
			nextElement = getFirstChildElement(currentParent);
		} else {
			nextElement = getChildElementAfter(currentChild);
		}
		return nextElement;
	}

	private Element getFirstChildElement(Element parent) {
		Node firstChild = parent.getFirstChild();
		if (firstChild == null) {
			return null;
		}
		return getChildElementAfter(firstChild);
	}

	private Element getChildElementAfter(Node current) {
		Node cur = current;
		do {
			cur = cur.getNextSibling();
		} while (cur != null && cur.getNodeType() != Node.ELEMENT_NODE);
		return (Element) cur;
	}
}