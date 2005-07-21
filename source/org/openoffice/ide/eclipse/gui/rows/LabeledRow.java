/*************************************************************************
 *
 * $RCSfile: LabeledRow.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/07/21 21:56:23 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the following licenses
 *
 *     - GNU Lesser General Public License Version 2.1
 *     - Sun Industry Standards Source License Version 1.1
 *
 * Sun Microsystems Inc., October, 2000
 *
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 *
 *
 * Sun Industry Standards Source License Version 1.1
 * =================================================
 * The contents of this file are subject to the Sun Industry Standards
 * Source License Version 1.1 (the "License"); You may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.openoffice.org/license.html.
 *
 * Software provided under this License is provided on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 * MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 * See the License for the specific provisions governing your rights and
 * obligations concerning the Software.
 *
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 *
 * Copyright: 2002 by Sun Microsystems, Inc.
 *
 * All Rights Reserved.
 *
 * Contributor(s): Cedric Bosdonnat
 *
 *
 ************************************************************************/
package org.openoffice.ide.eclipse.gui.rows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * TODOC Translate into english and complete the doc
 * 
 * @author cbosdonnat
 *
 */
public abstract class LabeledRow {
		
	protected Control label;
	protected Control field;
	protected Button  browse;
	protected String  property;

	protected IFieldChangedListener listener;
	
	/**
	 * Constructeur simple, affectant la propri�t�. Ce constructeur sera
	 * appel� par tous les enfants de la classe.
	 * 
	 * @param property valeur de la propri�t� associ�e au champ.
	 */
	public LabeledRow(String property){
		this.property = property;
	}
	
	/**
	 * Cr��e un champ de base. Ce constructeur ne sera g�n�ralement pas surcharg�.
	 * 
	 * @param parent Composite parent dans lequel les composants seront ajout�s.
	 * @param property Cha�ne contenant le nom de la propri�t� g�r�e par le champ.
	 *              Cette valeur est renvoy�e lors d'un changement du champ.
	 * @param label Control du label. La plupart des impl�mentations utilisera un Label,
	 *              mais un Hyperlink ou autre contr�le peuvent utiles.
	 * @param field Control contenant la donn�e g�r�e par le champ.
	 * @param browseText Texte du bouton. Celui-ci n'est pas cr��e si le texte est 
	 *              <code>null</code>
	 */
	public LabeledRow(Composite parent, String property, Control label,
			          Control field, String browseText){
		this.property = property;
		createContent(parent, label, field, browseText);
	}
	
	/**
	 * Cr��e le contenu du champ. Cette m�thode devrait �tre appel�e par ses descendants
	 * dans un m�thode <code>createContent</code> adapt�e au champ impl�ment�.
	 * 
	 * @param parent Composite parent dans lequel les composants seront ajout�s.
	 * @param label Control du label. La plupart des impl�mentations utilisera un Label,
	 *              mais un Hyperlink ou autre contr�le peuvent utiles.
	 * @param field Control contenant la donn�e g�r�e par le champ.
	 * @param browseText Texte du bouton. Celui-ci n'est pas cr��e si le texte est 
	 *               <code>null</code>
	 */
	protected void createContent(Composite parent, Control label,
	          Control field, String browseText){
		this.label = label;
		this.field = field;
		if (null != browseText){
			browse = new Button(parent, SWT.PUSH);
			browse.setText(browseText);
		}
		fillRow(parent);
	}
	
	/**
	 * Accesseur de la propri�t� associ�e au champ
	 * 
	 * @return Valeur de la propri�t� associ�e sous forme de <code>String</code>.
	 */
	public String getProperty(){
		return this.property;
	}
	
	/**
	 * Get or calculate the value of this property.
	 * 
	 * @return value
	 */
	public abstract String getValue();
	
	/**
	 * M�thode organisant les diff�rents composants dans le composite parent.
	 * Elle tient compte du layout du p�re et affecte des layoutData conrrespondant
	 * aux layout <code>TableWrapLayout</code> ou <code>GridLayout</code>
	 * 
	 * @param parent composite parent
	 */
	protected void fillRow(Composite parent){
		Layout layout = parent.getLayout();
		
		if (layout instanceof GridLayout){
			// Traitement du cas ou le parent a un GridLayout 
			int span = ((GridLayout)layout).numColumns - 1;
			
			GridData gd = new GridData();
			gd.verticalAlignment = GridData.VERTICAL_ALIGN_END;
			label.setLayoutData(gd);
			
			int fspan = browse != null ? span -1 : span;

			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = fspan;
			gd.grabExcessHorizontalSpace = (fspan == 1);
			gd.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
			gd.widthHint = 10;
			field.setLayoutData(gd);
			
			if (browse != null){
				gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
				gd.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
				browse.setLayoutData(gd);
			}
		}
	}
	
	/*
	 * M�thodes de gestion du listener de champ
	 */
	
	/**
	 * D�finir le listener r�agissant au changement de valeur du champ
	 * 
	 * @param listener listener de changement de champ
	 */
	public void setFieldChangedListener(IFieldChangedListener listener){
		this.listener = listener;
	}
	
	/**
	 * Supprimer le listener de changement de champ actuel
	 *
	 */
	public void  removeFieldChangedlistener(){
		listener = null;
	}
	
	/**
	 * Notifier un changement de la valeur du champ au listener
	 * 
	 * @param e Evenement de champ contenant la propri�t� associ�e au champ
	 *          ainsi que la nouvelle valeur
	 */
	protected void fireFieldChangedEvent(FieldEvent e){
		if (null != listener){
			listener.fieldChanged(e);
		}
	}
	
	/**
	 * Toggle the visibily of the line.
	 * 
	 * @param visible if <code>true</code> the components will visible, otherwise
	 *                they will be hidden.
	 */
	public void setVisible(boolean visible){
		
		GridData gd = (GridData)label.getLayoutData();
		gd.exclude = !visible;
		label.setLayoutData(gd);
		
		gd = (GridData)field.getLayoutData();
		gd.exclude = !visible;
		field.setLayoutData(gd);
		
		if (null != browse){
			gd = (GridData)browse.getLayoutData();
			gd.exclude = !visible;
			browse.setLayoutData(gd);
		}

		label.setVisible(visible);
		field.setVisible(visible);
		if (browse != null){
			browse.setVisible(visible);
		}
	}
	
	/**
	 * Set the enabled state of the field and the browse button if
	 * the latter exists.
	 * 
	 * @param enabled <code>true</code> activate the row, otherwise the
	 *                row is desactivated
	 */
	public void setEnabled(boolean enabled){
		field.setEnabled(enabled);
		if (null != browse){
			browse.setEnabled(enabled);
		}
	}
}
