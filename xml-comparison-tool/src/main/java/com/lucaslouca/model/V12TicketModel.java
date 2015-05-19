package com.lucaslouca.model;

import com.lucaslouca.annotations.XMLPath;
import com.lucaslouca.parser.V12Parser;

public class V12TicketModel extends AbstractTicketModel {
	@XMLPath(name = V12Parser.XPATH_EXTERNAL_REFERENCE)
	public String externalReference;

	@XMLPath(name = V12Parser.XPATH_IMPORTANT)
	public String important;

	public String getImportant() {
		return important;
	}

	public void setImportant(String important) {
		this.important = important;
	}

	public String getExternalReference() {
		return externalReference;
	}

	public void setExternalReference(String externalReference) {
		this.externalReference = externalReference;
	}

}
