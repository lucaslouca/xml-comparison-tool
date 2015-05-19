package com.lucaslouca.parser;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.lucaslouca.annotations.XMLPath;
import com.lucaslouca.model.AbstractTicketModel;

public abstract class AbstractTicketParser {
	private String ticketDirPath;
	protected List<AbstractTicketModel> tickets;
	protected Map<String, String> xpathToValueMap;
	protected NamespaceContext nameSpaceContext;
	protected List<IParserListener> parserListeners;

	/**
	 * Constructor
	 * 
	 * @param ticketDirPath
	 */
	public AbstractTicketParser() {
		parserListeners = new ArrayList<IParserListener>();
	}

	/**
	 * Read all .xml files from the given directory and parse them to Java
	 * Objects
	 */
	public void parse() {
		if (ticketDirPath == null || ticketDirPath.isEmpty()) {
			notifyListenersAboutMessage("Please select a folder!");
		} else {
			this.readXMLFilePathsInDirRecursively(ticketDirPath);
		}
	}

	/**
	 * Get all absolute XML file paths in the given directory path, read those
	 * XML files, parse them and create ticket objects
	 * 
	 * @param absoluteDirPath
	 *            absolute path of the directory with the .xml files.
	 */
	private void readXMLFilePathsInDirRecursively(String absoluteDirPath) {
		tickets = new ArrayList<AbstractTicketModel>();

		configureXpaths();

		File dir = new File(absoluteDirPath);
		@SuppressWarnings("unchecked")
		Collection<File> files = FileUtils.listFiles(dir, new String[] { "xml", "XML" }, true);
		String absoluteXMLPath;
		notifyListenersAboutMessage("Found " + files.size() + " .xml files in '" + absoluteDirPath + "'");
		for (File file : files) {
			absoluteXMLPath = file.getAbsolutePath();
			parseXML(absoluteXMLPath);
			addTicket(absoluteXMLPath);
		}

	}

	/**
	 * Parse XML located at absoluteXmlPath and fill the xpathToValueMap Map.
	 * 
	 * @param absoluteXmlPath
	 */
	private void parseXML(String absoluteXmlPath) {
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
	}

	/**
	 * Searches the XML Document for the given XPath and returns the value of
	 * the first element it finds.
	 * 
	 * @param xPathExpression
	 * @param doc
	 *            the XML Document
	 * @return Value of first element that matches the given xpath
	 */
	private String getValueForElementWithXpath(String xPathExpression, Document doc) throws XPathExpressionException {
		String result = null;

		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		xpath.setNamespaceContext(nameSpaceContext);
		XPathExpression expr = xpath.compile(xPathExpression);
		result = (String) expr.evaluate(doc, XPathConstants.STRING);

		return result;
	}

	/**
	 * Searches the given model for XMLPath and sets the value if the xpath
	 * equals the XMLPath.name()
	 * 
	 * @param model
	 * @param value
	 * @param xPath
	 */
	protected void setFieldValueForXpath(AbstractTicketModel model, String value, String xPath) {
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
	 * Add a TicketModel object to the List of tickets. The values for each
	 * field in a ticket are taken from the xpathToValueMap which gets filled in
	 * parseXML()
	 * 
	 * @param absoluteXmlPath
	 *            the path to the xml file
	 */
	private void addTicket(String absoluteXmlPath) {
		// Map to Ticket Object
		AbstractTicketModel ticket = getTicketModelInstance();
		ticket.setAbsoluteXmlPath(absoluteXmlPath);
		String valueForTag;
		for (String xpath : xpathToValueMap.keySet()) {
			valueForTag = xpathToValueMap.get(xpath);
			if (valueForTag == null) {
				notifyListenersAboutMessage("Could not find value for Xpath '" + xpath + "' in XML " + absoluteXmlPath);
			} else {
				setFieldValueForXpath(ticket, valueForTag, xpath);
			}
		}
		tickets.add(ticket);
	}

	/**
	 * Notify any possible listeners about the error
	 */
	protected void notifyListenersAboutMessage(String message) {
		for (IParserListener lsistener : parserListeners) {
			lsistener.parserMessageNotification(message);
		}
	}

	/**
	 * Configure Parser specific XPath and XML stuff like Namespaces, XPaths etc
	 */
	protected abstract void configureXpaths();

	/**
	 * Get Subclass specific Ticket Model Instance
	 * 
	 * @return
	 */
	protected abstract AbstractTicketModel getTicketModelInstance();

	/**
	 * Given an AbstractTicketModel, check whether there exists a
	 * corresponding/mapping ticket.
	 * 
	 * @param searchTicket
	 *            AbstractTicketModel to search for
	 * @return the matching ticket if we have a ticket that maps to the provided
	 *         AbstractTicketModel. Returns null if no match is found.
	 */
	public abstract AbstractTicketModel getCorrespondingTicket(AbstractTicketModel searchTicket);

	/*********************** GETTER/SETTER ***********************/
	public String getTicketDirPath() {
		return ticketDirPath;
	}

	public List<AbstractTicketModel> getTickets() {
		return tickets;
	}

	public void addParserListener(IParserListener listener) {
		parserListeners.add(listener);
	}

	public void setTicketDirPath(String ticketDirPath) {
		this.ticketDirPath = ticketDirPath;
	}

}
