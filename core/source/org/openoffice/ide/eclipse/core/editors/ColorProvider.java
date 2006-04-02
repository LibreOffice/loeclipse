/*************************************************************************
 *
 * $RCSfile: ColorProvider.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/02 20:13:03 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.core.editors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;

/**
 * This class provides colors to all the other objects
 * 
 * @author cbosdonnat
 *
 */
public class ColorProvider {
	protected Map fColorTable = new HashMap();
	private IPreferenceStore store;
	
	private final IPropertyChangeListener propertyListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			fColorTable.clear();
		}
	};
	
	public ColorProvider() {
		OOEclipsePlugin.getDefault().getPreferenceStore()
			.addPropertyChangeListener(propertyListener);
	    store = OOEclipsePlugin.getDefault().getPreferenceStore();
	}
	
	public void dispose() {
		Iterator e = fColorTable.values().iterator();
		while (e.hasNext()){
		    ( (Color) e.next() ).dispose();
		}
		OOEclipsePlugin.getDefault().getPreferenceStore()
			.removePropertyChangeListener(propertyListener);
	}
	
	public Color getColor(String color_string){
		
	    Color color = (Color)fColorTable.get(color_string);
		if (color == null){
			color = new Color(Display.getCurrent(), PreferenceConverter.getColor(store, color_string));
			fColorTable.put(color_string, color);
		}
		return color;
	}
	
}
