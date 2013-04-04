//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2007 - Mirko Nasato <mirko@artofsolving.com>
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// http://www.gnu.org/copyleft/lesser.html
//
package com.artofsolving.jodconverter.openoffice.connection;

/**
 * OpenOffice connection using a named pipe
 * <p>
 * <b>Warning!</b> This requires the <i>sal3</i> native library shipped with OpenOffice.org;
 * it must be made available via the <i>java.library.path</i> parameter, e.g.
 * <pre>
 *   java -Djava.library.path=/opt/openoffice.org/program my.App
 * </pre>  
 */
public class PipeOpenOfficeConnection extends AbstractOpenOfficeConnection {

    public static final String DEFAULT_PIPE_NAME = "jodconverter";

    public PipeOpenOfficeConnection() {
        this(DEFAULT_PIPE_NAME);
    }

    public PipeOpenOfficeConnection(String pipeName) {
        super("pipe,name="+ pipeName);
    }
}
