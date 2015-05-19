package com.lucaslouca.model;

import com.lucaslouca.annotations.XMLPath;
import com.lucaslouca.parser.V14Parser;

public class V14TicketModel extends AbstractTicketModel {
	@XMLPath(name = V14Parser.XPATH_OBJECTID)
	public String objectId;

	@XMLPath(name = V14Parser.XPATH_VERSION_NUMBER)
	public String versionNumber;

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(String versionNumber) {
		this.versionNumber = versionNumber;
	}

}
