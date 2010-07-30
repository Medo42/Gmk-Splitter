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
import java.io.StringWriter;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.ganggarrison.gmdec.FileTools;

public class XmlWriter {
	protected Document domDocument;
	protected Stack<Element> openElementStack = new Stack<Element>();

	private void createDocument(String root) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();
			domDocument = impl.createDocument(null, root, null);
			domDocument.setXmlStandalone(true);
		} catch (ParserConfigurationException e) {
			throw new AssertionError();
		}
	}

	public void putElement(String elementName, Object textContent) {
		Element elem = createElement(elementName);
		elem.setTextContent(textContent.toString());
	}

	public void putAttribute(String attributeName, Object value) {
		openElementStack.peek().setAttribute(attributeName, value.toString());
	}

	public void putComment(String comment) {
		Comment node = domDocument.createComment(comment);
		openElementStack.peek().appendChild(node);
	}

	public void putText(String text) {
		Text node = domDocument.createTextNode(text);
		openElementStack.peek().appendChild(node);
	}

	public void startElement(String elementName) {
		Element elem = createElement(elementName);
		openElementStack.push(elem);
	}

	private Element createElement(String elementName) {
		Element elem;
		if (domDocument == null) {
			createDocument(elementName);
			elem = domDocument.getDocumentElement();
		} else {
			elem = domDocument.createElement(elementName);
			openElementStack.peek().appendChild(elem);
		}
		return elem;
	}

	public void endElement() {
		openElementStack.pop();
	}

	public void write(File file) throws IOException {
		String lineSep = System.getProperty("line.separator");
		System.setProperty("line.separator", "\n");
		try {
			Transformer trans = createTransformer();
			writeXml(trans, file);
		} catch (TransformerConfigurationException e) {
			throw new AssertionError(e);
		} catch (TransformerException e) {
			throw new IOException(e);
		} finally {
			System.setProperty("line.separator", lineSep);
		}

	}

	private Transformer createTransformer() throws TransformerConfigurationException {
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		trans.setOutputProperty(OutputKeys.STANDALONE, "no");
		trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		return trans;
	}

	private void writeXml(Transformer trans, File file) throws TransformerException, IOException {
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(domDocument);
		trans.transform(source, result);
		FileTools.writeFile(file, sw.toString());
	}
}
