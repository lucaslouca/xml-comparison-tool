package com.accenture.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.accenture.model.AbstractTicketModel;

public abstract class AbstractTicketParserworking {
	private final String ticketDirPath;
	protected List<AbstractTicketModel> tickets;

	/**
	 * Constructor
	 * 
	 * @param ticketDirPath
	 */
	public AbstractTicketParserworking(String ticketDirPath) {
		this.ticketDirPath = ticketDirPath;
		this.readXMLFilePathsInDirRecursively(ticketDirPath);
	}

	/**
	 * Get all absolute XML file paths in the given directory path.
	 * 
	 * @param absoluteDirPath
	 *            absolute path of the directory with the .xml files.
	 */
	protected void readXMLFilePathsInDirRecursively(String absoluteDirPath) {
		tickets = new ArrayList<AbstractTicketModel>();

		File dir = new File(absoluteDirPath);
		@SuppressWarnings("unchecked")
		Collection<File> files = FileUtils.listFiles(dir, new String[] { "xml", "XML" }, true);
		for (File file : files) {
			addTicket(file.getAbsolutePath());
		}

	}

	/**
	 * Get all absolute XML file paths in the given directory path. Direct
	 * childs.
	 * 
	 * @param absoluteDirPath
	 *            absolute path of the directory with the .xml files.
	 */
	@Deprecated
	protected List<String> getXMLFilePathsInDir(String absoluteDirPath) {
		List<String> result = new ArrayList<String>();

		File folder = new File(absoluteDirPath);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			String filePath = listOfFiles[i].getAbsolutePath();
			if (filePath.endsWith(".xml") || filePath.endsWith(".XML")) {
				result.add(filePath);
			}
		}

		return result;
	}

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

	/**
	 * Parse XML file and add a TicketModel object to the List of tickets
	 * 
	 * @param absoluteXmlPath
	 *            the path to the xml file
	 */
	protected abstract void addTicket(String absoluteXmlPath);

	/*********************** GETTER/SETTER ***********************/
	public String getTicketDirPath() {
		return ticketDirPath;
	}

	public List<AbstractTicketModel> getTickets() {
		return tickets;
	}
}
