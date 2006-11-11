/*************************************************************************
 *
 * $RCSfile: Language.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/11 18:39:35 $
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
package org.openoffice.ide.eclipse.java;

import org.openoffice.ide.eclipse.core.model.language.ILanguage;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.openoffice.ide.eclipse.core.model.language.ILanguageUI;
import org.openoffice.ide.eclipse.core.model.language.IProjectHandler;

/**
 * Implementation for the Java language
 * 
 * @author cbosdonnat
 */
public class Language implements ILanguage {

	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.language.ILanguage#getLanguageBuidler()
	 */
	public ILanguageBuilder getLanguageBuidler() {
		return new JavaBuilder(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.language.ILanguage#getLanguageUI()
	 */
	public ILanguageUI getLanguageUI() {
		return new JavaUI();
	}

	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.language.ILanguage#getProjectHandler()
	 */
	public IProjectHandler getProjectHandler() {
		return new JavaProjectHandler();
	}
}
