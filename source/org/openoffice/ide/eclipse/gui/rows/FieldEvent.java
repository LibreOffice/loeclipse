/*
 * $Id: FieldEvent.java,v 1.1 2005/07/18 19:36:05 cedricbosdo Exp $
 * $Log: FieldEvent.java,v $
 * Revision 1.1  2005/07/18 19:36:05  cedricbosdo
 * First working basis with syntax highlighting and SDK configuration. Some wizards are already begun, but does nothing for the moment.
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 * CVS: ----------------------------------------------------------------------
 * CVS: Committers,
 * CVS:
 * CVS: Please follow these protocols:
 * CVS:
 * CVS: * Please include in the log message
 * CVS: reference(s) by ID / number and/or URL
 * CVS: to any and all relevant OpenOffice.org issue(s).
 * CVS:
 * CVS: * If the code is contributed from outside Sun
 * CVS: then please verify using the list at the following URL
 * CVS: http://www.openoffice.org/copyright/copyrightapproved.html
 * CVS: that Sun has received a signed Copyright Assignment Form
 * CVS: from the submitter.
 * CVS:
 * CVS: Otherwise,
 * CVS: please send an email TO: the submitter; and CC: OOCRequest@eng.sun.com
 * CVS: the letter (CopyRightRequest.txt) to request assignment of copyright to Sun
 * CVS: (http://www.openoffice.org/copyright/assign_copyright.html).
 * CVS:
 * CVS: Please do NOT commit code until you have verified (as detailed above) that
 * CVS: Sun has received a signed Copyright Assignment Form from the submitter.
 * CVS:
 * CVS: * Please send an email TO: the submitter
 * CVS: (particularly, if from outside Sun)
 * CVS: advising that the code has been committed,
 * CVS: and gratefully recognizing the contribution.
 *
 * Revision 1.1  2005/06/13 13:26:44  cbosdonnat
 * Cr�ation de composants simplifi�s pour l'interface graphique
 * 
 */
package org.openoffice.ide.eclipse.gui.rows;

/**
 * @author cbosdonnat
 *
 */
public class FieldEvent {

	private String property;
	private String value;
	
	public FieldEvent(String property, String value){
		this.property = property;
		this.value = value;
	}

	public String getProperty() {
		return property;
	}

	public String getValue() {
		return value;
	}
}
