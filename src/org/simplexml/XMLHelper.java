package org.simplexml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Methods of this class help processing xml files.
 *
 * @author <a href="mailto:acsf.dev@gmail.com">Kay Schröer</a>
 */
public final class XMLHelper {
	private static XPath xpath = XPathFactory.newInstance().newXPath();

	private XMLHelper() {
	}

	/**
	 * Creates a new document with no elements.
	 *
	 * @return document
	 *
	 * @throws ParserConfigurationException
	 */
	public static Document createEmptyDocument() throws ParserConfigurationException {
		return newDocumentBuilder().newDocument();
	}

	/**
	 * Gets the attribute value.
	 *
	 * @param node
	 *            element from which the attribute value will be retrieved
	 * @param name
	 *            attribute name
	 *
	 * @return attribute value or an empty string if the attribute not exists
	 */
	public static String getAttributeValue(Node node, String name) {
		Node attrNode = node.getAttributes().getNamedItem(name);
		if (attrNode != null) {
			return attrNode.getNodeValue();
		} else {
			return "";
		}
	}

	/**
	 * Executes the XPath expression and returns the list of the corresponding
	 * results.
	 *
	 * @param node
	 *            the source element
	 * @param expression
	 *            xpath expression string
	 *
	 * @return list with zero, one or more elements
	 *
	 * @throws XPathExpressionException
	 */
	public static NodeList getNodeList(Node node, String expression) throws XPathExpressionException {
		return (NodeList) xpath.compile(expression).evaluate(node, XPathConstants.NODESET);
	}

	/**
	 * Parses a given xml file.
	 *
	 * @param file
	 *            path or name of the xml file
	 *
	 * @return document
	 *
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	public static Document parse(File file)
			throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		try (FileInputStream fis = new FileInputStream(file)) {
			return parse(fis);
		}
	}

	/**
	 * Parses a given xml stream.
	 *
	 * @param inputStream
	 *            data stream containing the xml structure
	 *
	 * @return document
	 *
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	public static Document parse(InputStream inputStream)
			throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
		Document doc = newDocumentBuilder().parse(inputStream);
		stripWhitespaces(doc);
		return doc;
	}

	/**
	 * Writes the given xml file.
	 *
	 * @param file
	 *            path and name of the xml file
	 * @param document
	 *            document with the data
	 *
	 * @throws IOException
	 * @throws TransformerConfigurationException
	 * @throws TransformerException
	 */
	public static void write(File file, Document document)
			throws IOException, TransformerConfigurationException, TransformerException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			write(fos, document);
		}
	}

	/**
	 * Writes the given xml stream.
	 *
	 * @param outputStream
	 *            data stream to store the xml structure
	 * @param document
	 *            document with the data
	 *
	 * @throws TransformerConfigurationException
	 * @throws TransformerException
	 */
	public static void write(OutputStream outputStream, Document document)
			throws TransformerConfigurationException, TransformerException {
		StreamResult result = new StreamResult(outputStream);
		DOMSource source = new DOMSource(document);
		newTransformer().transform(source, result);
	}

	private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://xml.org/sax/features/namespaces", false);
		factory.setFeature("http://xml.org/sax/features/validation", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		return factory.newDocumentBuilder();
	}

	private static Transformer newTransformer() throws TransformerConfigurationException {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(2));
		return transformer;
	}

	private static void stripWhitespaces(Document document) throws XPathExpressionException {
		NodeList emptyTextNodes = (NodeList) xpath.compile("//text()[normalize-space(.) = '']").evaluate(document,
				XPathConstants.NODESET);
		for (int i = 0; i < emptyTextNodes.getLength(); i++) {
			Node emptyTextNode = emptyTextNodes.item(i);
			emptyTextNode.getParentNode().removeChild(emptyTextNode);
		}
	}
}