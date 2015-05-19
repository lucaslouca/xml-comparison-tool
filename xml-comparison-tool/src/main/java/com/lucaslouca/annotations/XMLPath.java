package com.lucaslouca.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotate AbstractTicketModel objects with this annotation.
 * 
 * EXAMPLE:
 * 
 * @XMLPath(name =
 *               "//calypso:calypsoDocument/calypso:calypsoObject/calypso:externalReference"
 *               )
 * 
 *               public String calypsoExternalReference;
 * 
 * 
 *               This way the parser will insert the value of the XML element
 *               located at XPath
 *               "//calypso:calypsoDocument/calypso:calypsoObject/calypso:externalReference"
 *               into the field calypsoExternalReference
 * @author lucas
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface XMLPath {
	String name();
}
