package com.lucaslouca.parser;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import com.lucaslouca.model.AbstractTicketModel;
import com.lucaslouca.model.V12TicketModel;

public class V12Parser extends AbstractTicketParser {
	public static final String XPATH_IMPORTANT = "//test:testDocument/test:testObject/test:keyword[test:name = 'IMPORTANT']/test:value";
	public static final String XPATH_EXTERNAL_REFERENCE = "//test:testDocument/test:testObject/test:externalReference";

	@Override
	public AbstractTicketModel getCorrespondingTicket(AbstractTicketModel searchTicket) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void configureXpaths() {
		nameSpaceContext = new NamespaceContext() {
			@Override
			public String getNamespaceURI(String prefix) {
				return prefix.equals("test") ? "http://www.lucaslouca.com/xml" : null;
			}

			@Override
			public Iterator getPrefixes(String val) {
				return null;
			}

			@Override
			public String getPrefix(String uri) {
				return uri.equals("http://www.lucaslouca.com/xml") ? "test" : null;
			}
		};

		xpathToValueMap = new HashMap<String, String>();
		xpathToValueMap.put(XPATH_EXTERNAL_REFERENCE, null);
		xpathToValueMap.put(XPATH_IMPORTANT, null);
	}

	@Override
	protected AbstractTicketModel getTicketModelInstance() {
		return new V12TicketModel();
	}
}
