package com.lucaslouca.parser;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import com.lucaslouca.model.AbstractTicketModel;
import com.lucaslouca.model.V12TicketModel;
import com.lucaslouca.model.V14TicketModel;

public class V14Parser extends AbstractTicketParser {
	public static final String XPATH_OBJECTID = "//mainDocument/events/mainEvent/object/objectId";
	public static final String XPATH_VERSION_NUMBER = "//mainDocument/contracts/contract/businessObjectId/versionIdentifier/versionRevision/versionNumber";

	@Override
	public AbstractTicketModel getCorrespondingTicket(AbstractTicketModel searchTicket) {
		AbstractTicketModel result = null;

		if (searchTicket instanceof V12TicketModel) {
			// V12 Attributes
			V12TicketModel v12Ticket = (V12TicketModel) searchTicket;
			String externalReferenceV12 = v12Ticket.getExternalReference();

			// Remove prefix 'XYZ_' from external reference
			String[] externalRefComponentsV12 = externalReferenceV12.split("_");
			String externalRefWithoutPrefixV12 = null;
			if (externalRefComponentsV12.length > 1) {
				externalRefWithoutPrefixV12 = externalRefComponentsV12[1];
			}
			String foVersionV12 = v12Ticket.getImportant();

			// V14 Attributes
			String objectIdV14;
			String versionV14;
			SEARCH_OWN_INVENTORY_LOOP: for (AbstractTicketModel abstractTicket : tickets) {
				V14TicketModel v14Ticket = (V14TicketModel) abstractTicket;
				objectIdV14 = v14Ticket.getObjectId();
				versionV14 = v14Ticket.getVersionNumber();

				boolean externalRefMatch = (objectIdV14 != null && externalRefWithoutPrefixV12 != null) && (objectIdV14.equals(externalRefWithoutPrefixV12));
				boolean versionMatch = (foVersionV12 != null && versionV14 != null) && (foVersionV12.equals(versionV14));

				if (externalRefMatch && versionMatch) {
					// Match found
					result = v14Ticket;
					break SEARCH_OWN_INVENTORY_LOOP;
				}
			}
		}
		return result;
	}

	@Override
	protected void configureXpaths() {
		nameSpaceContext = new NamespaceContext() {
			@Override
			public String getNamespaceURI(String prefix) {
				return null;
			}

			@Override
			public Iterator getPrefixes(String val) {
				return null;
			}

			@Override
			public String getPrefix(String uri) {
				return null;
			}
		};

		xpathToValueMap = new HashMap<String, String>();
		xpathToValueMap.put(XPATH_OBJECTID, null);
		xpathToValueMap.put(XPATH_VERSION_NUMBER, null);
	}

	@Override
	protected AbstractTicketModel getTicketModelInstance() {
		return new V14TicketModel();
	}
}
