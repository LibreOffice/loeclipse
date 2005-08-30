/*************************************************************************
 *
 * $RCSfile: LabeledRow.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/30 13:24:42 $
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
import org.eclipse.swt.widgets.Label;
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
	 * Constructeur simple, affectant la propriété. Ce constructeur sera
	 * appelé par tous les enfants de la classe.
	 * 
	 * @param property valeur de la propriété associée au champ.
	 */
	public LabeledRow(String property){
		this.property = property;
	}
	
	/**
	 * Créée un champ de base. Ce constructeur ne sera généralement pas surchargé.
	 * 
	 * @param parent Composite parent dans lequel les composants seront ajoutés.
	 * @param property Chaîne contenant le nom de la propriété gérée par le champ.
	 *              Cette valeur est renvoyée lors d'un changement du champ.
	 * @param label Control du label. La plupart des implémentations utilisera un Label,
	 *              mais un Hyperlink ou autre contrôle peuvent utiles.
	 * @param field Control contenant la donnée gérée par le champ.
	 * @param browseText Texte du bouton. Celui-ci n'est pas créée si le texte est 
	 *              <code>null</code>
	 */
	public LabeledRow(Composite parent, String property, Control label,
			          Control field, String browseText){
		this.property = property;
		createContent(parent, label, field, browseText);
	}
	
	public void setLabel(String newLabel){
		((Label)label).setText(newLabel);
	}
	
	/**
	 * Créée le contenu du champ. Cette méthode devrait être appelée par ses descendants
	 * dans un méthode <code>createContent</code> adaptée au champ implémenté.
	 * 
	 * @param parent Composite parent dans lequel les composants seront ajoutés.
	 * @param label Control du label. La plupart des implémentations utilisera un Label,
	 *              mais un Hyperlink ou autre contrôle peuvent utiles.
	 * @param field Control contenant la donnée gérée par le champ.
	 * @param browseText Texte du bouton. Celui-ci n'est pas créée si le texte est 
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
	 * Accesseur de la propriété associée au champ
	 * 
	 * @return Valeur de la propriété associée sous forme de <code>String</code>.
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
	 * Méthode organisant les différents composants dans le composite parent.
	 * Elle tient compte du layout du père et affecte des layoutData conrrespondant
	 * aux layout <code>TableWrapLayout</code> ou <code>GridLayout</code>
	 * 
	 * @param parent composite parent
	 */
	protected void fillRow(Composite parent){
		Layout layout = parent.getLayout();
		
		if (layout instanceof GridLayout){
			// Supposes that the parent layout is a Grid one  
			int span = ((GridLayout)layout).numColumns - 1;
			
			label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			
			int fspan = browse != null ? span -1 : span;

			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = fspan;
			gd.grabExcessHorizontalSpace = (1 == fspan);
			gd.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
			gd.widthHint = 10;
			field.setLayoutData(gd);
			
			if (browse != null){
				browse.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
						GridData.VERTICAL_ALIGN_CENTER));
			}
		}
	}

	
	/**
	 * define the listener that will react to the field changes
	 * 
	 * @param listener field changes listener
	 */
	public void setFieldChangedListener(IFieldChangedListener listener){
		this.listener = listener;
	}
	
	/**
	 * remove the field changes listener
	 *
	 */
	public void  removeFieldChangedlistener(){
		listener = null;
	}
	
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
