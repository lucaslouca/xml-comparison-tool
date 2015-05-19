package com.accenture.parser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.accenture.annotations.XMLPath;
import com.accenture.model.AbstractTicketModel;
import com.accenture.model.V12TicketModel;

public class V12Parserworking extends AbstractTicketParser {

	// Ignore Namespaces
	//public static final String FOVERSION_XPATH = "//*[local-name() = 'calypsoDocument']/*[local-name() = 'calypsoObject']/*[local-name() = 'keyword']/*[local-name() = 'name' and text() = 'FOVersion']/../*[local-name() = 'value']";
	//public static final String EXTERNAL_REFERENCE_XPATH = "//*[local-name() = 'calypsoDocument']/*[local-name() = 'calypsoObject']/*[local-name() = 'externalReference']";

	public static final String FOVERSION_XPATH = "//calypso:calypsoDocument/calypso:calypsoObject/calypso:keyword[calypso:name = 'FOVersion']/calypso:value";

	public static final String EXTERNAL_REFERENCE_XPATH = "//calypso:calypsoDocument/calypso:calypsoObject/calypso:externalReference";

	public V12Parserworking(String ticketDirPath) {
		super(ticketDirPath);
	}

	@Override
	public AbstractTicketModel getCorrespondingTicket(AbstractTicketModel searchTicket) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void addTicket(String absoluteXmlPath) {
		Map<String, String> xpathToValueMap = new HashMap<String, String>();
		xpathToValueMap.put(EXTERNAL_REFERENCE_XPATH, null);
		xpathToValueMap.put(FOVERSION_XPATH, null);

		// Parse XML
		File xmlFile = new File(absoluteXmlPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			String searchTagValue;
			for (String xPathExpression : xpathToValueMap.keySet()) {
				searchTagValue = getValueForElementWithXpath(xPathExpression, doc);
				xpathToValueMap.put(xPathExpression, searchTagValue);
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		// Map to Ticket Object
		V12TicketModel ticket = new V12TicketModel();
		ticket.setAbsoluteXmlPath(absoluteXmlPath);
		String valueForTag;
		for (String xpath : xpathToValueMap.keySet()) {
			valueForTag = xpathToValueMap.get(xpath);
			if (valueForTag == null) {
				throw new IllegalArgumentException("Could not find value for Xpath '" + xpath + "' in XML " + absoluteXmlPath);
			} else {
				setFieldValueForXpath(ticket, valueForTag, xpath);
			}
		}
		tickets.add(ticket);
	}

	/**
	 * Searches the given model for XMLPath and sets the value if the xpath
	 * equals the XMLPath.name()
	 * 
	 * @param model
	 * @param value
	 * @param xPath
	 */
	private void setFieldValueForXpath(AbstractTicketModel model, String value, String xPath) {
		for (Field field : model.getClass().getDeclaredFields()) {
			XMLPath xmlTag = field.getAnnotation(XMLPath.class);
			if (xmlTag != null && xmlTag.name().equals(xPath)) {
				try {
					field.set(model, value);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Searches the XML Document for the given tag and returns the value of the
	 * first element it finds with that tag.
	 * 
	 * @param searchTag
	 * @param doc
	 * @return Value of first element that has tag equal to searchTag
	 */
	@Deprecated
	private String getValueForElementWithTag(String searchTag, Document doc) {
		String result = null;

		NodeList nList = doc.getElementsByTagName(searchTag);
		SEARCH_TAG_LOOP: for (int temp = 0; temp < nList.getLength(); temp++) {
			Node node = nList.item(temp);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				result = node.getTextContent();
				break SEARCH_TAG_LOOP;
			}
		}

		return result;
	}

	/**
	 * Searches the XML Document for the given xpath and returns the value of
	 * the first element it finds.
	 * 
	 * @param xPathExpression
	 * @param doc
	 * @return Value of first element that matches the given xpath
	 */
	private String getValueForElementWithXpath(String xPathExpression, Document doc) throws XPathExpressionException {
		String result = null;

		NamespaceContext nameSpaceContext = new NamespaceContext() {
			public String getNamespaceURI(String prefix) {
				return prefix.equals("calypso") ? "http://www.calypso.com/xml" : null;
			}

			public Iterator getPrefixes(String val) {
				return null;
			}

			public String getPrefix(String uri) {
				return uri.equals("http://www.calypso.com/xml") ? "calypso" : null;
			}
		};

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		xpath.setNamespaceContext(nameSpaceContext);
		XPathExpression expr = xpath.compile(xPathExpression);
		//String test = (String) expr.evaluate(doc, XPathConstants.STRING);
		NodeList nList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		SEARCH_TAG_LOOP: for (int temp = 0; temp < nList.getLength(); temp++) {
			Node node = nList.item(temp);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				result = node.getTextContent();
				break SEARCH_TAG_LOOP;
			}
		}

		return result;
	}
}
