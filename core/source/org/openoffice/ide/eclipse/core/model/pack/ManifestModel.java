/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Cédric Bosdonnat.
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2009 by Cédric Bosdonnat.
 *
 * All Rights Reserved.
 * 
 ************************************************************************/
package org.openoffice.ide.eclipse.core.model.pack;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Class representing the data contained in the manifest.xml file.
 * 
 * @author Cédric Bosdonnat
 *
 */
public class ManifestModel {
    
    private static final String EXT_XCS = ".xcs"; //$NON-NLS-1$
    private static final String EXT_XCU = ".xcu"; //$NON-NLS-1$
    private static final String EXT_RDB = ".rdb"; //$NON-NLS-1$
    
    private HashMap<String, FileType> mEntries = new HashMap<String, FileType>();
    
    /**
     * Add a file or directory to the package.
     * 
     * <p>This method doesn't know about the different languages
     * contributions to the <code>manifest.xml</code> file.</p>
     * 
     * @param pContent the file or folder to add
     */
    public void addContent( IResource pContent ) {
        if ( pContent instanceof IFile ) {
            IFile file = ( IFile ) pContent;
            if ( pContent.getName().endsWith( EXT_XCS ) ) {
                addConfigurationSchemaFile( file );
            } else if ( pContent.getName().endsWith( EXT_XCU ) ) {
                addConfigurationDataFile( file );
            } else if ( pContent.getName().endsWith( EXT_RDB ) ) {
                addTypelibraryFile( file, "RDB" ); //$NON-NLS-1$
            }
        } else {
            // Recurse on the directory
            IFolder folder = ( IFolder ) pContent;
            IResource[] members;
            try {
                members = folder.members();
                for ( IResource child : members ) {
                    addContent( child );
                }
            } catch (CoreException e) {
            }
        }
    }
    
    /**
     * Add a uno component file, for example a jar, shared library or python file
     * containing the uno implementation. The type of the file defines the 
     * language and should be given as defined in the OOo Developer's Guide, like
     * Java, native, Python.
     * 
     * @param pFile the file to add to the package
     * @param pType the type of the file to add.
     * 
     * @see #addComponentFile(File, String, String) for platform support
     */
    public void addComponentFile( IFile pFile, String pType) {
        addComponentFile( pFile, pType, null );
    }
    
    /**
     * Add a uno component file, for example a jar, shared library or python file
     * containing the uno implementation. 
     * 
     * <p>The type of the file defines the language and should be given as defined
     * in the OOo Developer's Guide, like Java, native, Python.</p>
     * 
     * @param pFile the file to add to the package
     * @param pType the type of the file to add.
     * @param pPlatform optional parameter to use only with native type. Please
     *         refer to the OOo Developer's Guide for more information.
     */
    public void addComponentFile( IFile pFile, String pType, String pPlatform ) {
        FileType type = new FileType( FileType.MIME_UNO_COMPONENT );
        type.addParam( FileType.PARAM_TYPE, pType );
        if ( pPlatform != null && pType.equals( "native" ) ) { //$NON-NLS-1$
            type.addParam(FileType.PARAM_PLATFORM, pPlatform ); 
        }
        String relPath = pFile.getProjectRelativePath().toString();
        
        addEntry( relPath, type );
    }
    
    /**
     * Add a type library to the package. 
     * 
     * <p>Note that by some strange way, a jar dependency can be added 
     * in the package as a type library like RDB files.</p>
     *  
     * @param pFile the file to add 
     * @param pType the type of the file as specified in the OOo Developer's Guide
     */
    public void addTypelibraryFile( IFile pFile, String pType ) {
        FileType type = new FileType( FileType.MIME_UNO_TYPES );
        type.addParam( FileType.PARAM_TYPE, pType );
        String relPath = pFile.getProjectRelativePath().toString();
        
        addEntry( relPath, type );
    }
    
    /**
     * Add a basic library to the package. 
     * 
     * <p>Even if this method may not be used, it is possible.</p>
     * 
     * @param pDir the directory of the basic library.
     */
    public void addBasicLibrary( IFolder pDir ) {
        FileType type = new FileType( FileType.MIME_BASIC_LIB );
        String relPath = pDir.getProjectRelativePath().toString();

        addEntry( relPath, type );
    }
    
    /**
     * Add a dialog library to the package. 
     * 
     * <p>Even if this method may not be used, it is possible.</p>
     * 
     * @param pDir the directory of the dialog library.
     */
    public void addDialogLibrary( IFolder pDir ) {
        FileType type = new FileType( FileType.MIME_DIALOG_LIB );
        String relPath = pDir.getProjectRelativePath().toString();

        addEntry( relPath, type );
    }
    
    /**
     * Add an xcu configuration to the package.
     * 
     * @param pFile the xcu file to add
     */
    public void addConfigurationDataFile( IFile pFile ) {
        if ( pFile.getName( ).endsWith( EXT_XCU ) ) {
            FileType type = new FileType( FileType.MIME_XCU );
            String relPath = pFile.getProjectRelativePath().toString();
            
            addEntry( relPath, type );
        }
    }
    
    /**
     * Add an xcs configuration to the package.
     * 
     * @param pFile the xcs file to add
     */
    public void addConfigurationSchemaFile( IFile pFile ) {
        if ( pFile.getName( ).endsWith( EXT_XCS ) ) {
            FileType type = new FileType( FileType.MIME_XCS );
            String relPath = pFile.getProjectRelativePath().toString();
            
            addEntry( relPath, type );
        }
    }
    
    /**
     * Add a localized description of the package.
     * 
     * @param pDescriptionFile the file containing the description for that locale
     * @param pLocale the locale of the description. Can be <code>null</code>.
     */
    public void addDescription( IFile pDescriptionFile, Locale pLocale ) {
        // write the description to a file
        String localeString = new String();
        if (pLocale != null) {
            localeString = pLocale.toString();
            localeString = localeString.replace("_", "-"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Add the file entry to the manifest
        FileType type = new FileType( FileType.MIME_DESCRIPTION );
        if ( !(0 == localeString.length()) ) {
            type.addParam( FileType.PARAM_LOCALE, localeString );
        }
        String relPath = pDescriptionFile.getProjectRelativePath().toString();

        addEntry( relPath, type );
    }
    
    /**
     * This is the generic method to add an element to the manifest: nothing 
     * is tested here.
     * 
     * @param pRelativePath the path of the file relative to the package
     * @param pType the type of the file to write in the manifest
     */
    private void addEntry( String pRelativePath, FileType pType ) {
        mEntries.put( pRelativePath, pType );
    }
    
    /**
     * Output the manifest.xml file.
     * 
     * @param pOut where to write the manifest.
     * @throws IOException if something happened when writing to the output stream
     */
    public void write( OutputStream pOut ) throws IOException {
        Iterator<Entry<String, FileType> > iter = mEntries.entrySet().iterator();
        String entryPattern = "<manifest:file-entry manifest:full-path=\"{0}\"" + //$NON-NLS-1$
                " manifest:media-type=\"{1}\"/>\n"; //$NON-NLS-1$
        pOut.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".getBytes() ); //$NON-NLS-1$
        pOut.write( "<manifest:manifest>\n".getBytes() ); //$NON-NLS-1$
        while ( iter.hasNext() ) {
            Entry<String, FileType> entry = iter.next();
            pOut.write( MessageFormat.format( entryPattern, entry.getKey(), entry.getValue().toString() ).getBytes() );
        }
        pOut.write( "</manifest:manifest>\n".getBytes() ); //$NON-NLS-1$
        pOut.flush();
    }
}
