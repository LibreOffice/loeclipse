/*************************************************************************
 *
 * $RCSfile: IdlcErrorReader.java,v $
 *
 * $Revision: 1.7 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:27 $
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
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
package org.libreoffice.ide.eclipse.core.builders;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.libreoffice.ide.eclipse.core.PluginLogger;

/**
 * Class reading the idlc error output to transform the errors into markers.
 *
 *
 */
public class IdlcErrorReader {

    /**
     * Include error regular expression.
     *
     * <p>
     * <em>cpp: &lt;file&gt;:&lt;line number&gt; some text Could not find
     * include file &lt;missing include&gt;</em>
     * </p>
     */
    private static final String R_IDLCPP_ERROR = "cpp: (\\S+):([0-9]+)(.*:[0-9]+)? (.*)"; //$NON-NLS-1$

    /**
     * Syntax error expression.
     *
     * <p>
     * <em>&lt;file&gt;:&lt;line number&gt; [&lt;offsetStart&gt;,&lt;offsetEnd&gt;] : &lt;message&gt;</em>
     * </p>
     */
    private static final String R_IDLC_ERROR = "(.*):([0-9]+) \\[([0-9]+):([0-9]+)\\] :" + " (WARNING, )?(.*)"; //$NON-NLS-2$

    private static final int IDLC_ERROR_LINE_GROUP = 2;
    private static final int IDLC_ERROR_OFFSET_START_GROUP = 3;
    private static final int IDLC_ERROR_OFFSET_END_GROUP = 4;
    private static final int IDLC_ERROR_MESSAGE_GROUP = 5;

    private static final int IDLCPP_INCLUDE_PATH_GROUP = 4;

    private static final int IDLCPP_OPTIONAL_GROUP = 3;

    /**
     * Stream from which the reader extract the errors.
     */
    private LineNumberReader mReader;
    private InputStreamReader mIn;

    /**
     * File which compilation has been asked.
     */
    private IFile mCompiledFile;

    /**
     * Constructor.
     *
     * @param pStream
     *            the error stream to read
     * @param pFile
     *            the built IDL file
     */
    public IdlcErrorReader(InputStream pStream, IFile pFile) {
        mIn = new InputStreamReader(pStream);
        mReader = new LineNumberReader(mIn);
        mCompiledFile = pFile;
    }

    /**
     * Computes the error into IDL markers.
     */
    public void readErrors() {

        try {
            // Cleans the idlc error previously added
            mCompiledFile.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);

            // Read each line until the stream end (null line)
            String line = mReader.readLine();

            // Mark only the file errors. The changed files will be compiled too

            while (null != line) {

                // Handle the error line
                IMarker marker = analyseIdlcppError(line);

                if (null == marker) {
                    marker = analyseIdlcError(line);
                }

                if (null != marker) {
                    // Keep only the markers for the errors concerning the
                    // file which is compiled
                    if (IResource.FILE == marker.getResource().getType()
                        && !marker.getResource().getProjectRelativePath().toString()
                        .equals(mCompiledFile.getProjectRelativePath().toString())) {
                        marker.delete();
                    }
                }

                line = mReader.readLine();
            }
        } catch (IOException e) {
            PluginLogger.error(Messages.getString("IdlcErrorReader.ErrorReadingError"), e); //$NON-NLS-1$
        } catch (CoreException e) {
            PluginLogger.error(Messages.getString("IdlcErrorReader.MarkerCreationError") //$NON-NLS-1$
                + mCompiledFile.getProjectRelativePath().toString(), e);
        } finally {
            try {
                mReader.close();
                mIn.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * <p>
     * Method that analyzes the error line and return the appropriate marker if it is possible.
     * </p>
     *
     * @param pLine
     *            error line to analyse
     * @return the corresponding marker if the line is an <code>idlc</code> error line. <code>null</code> if there were
     *         problems by creating the marker or if the line isn't an <code>idlc</code> error line.
     */
    private IMarker analyseIdlcError(String pLine) {
        IMarker marker = null;

        Pattern pSyntax = Pattern.compile(R_IDLC_ERROR);
        Matcher mSyntax = pSyntax.matcher(pLine);

        if (!pLine.startsWith("idlc:") && mSyntax.matches()) { //$NON-NLS-1$
            IProject project = mCompiledFile.getProject();

            boolean error = false;
            if (null == mSyntax.group(IDLC_ERROR_MESSAGE_GROUP)) {
                error = true;
            }

            // HELP the groups are indexed from 1. 0 is the whole string
            String filePath = mSyntax.group(1);
            int lineNo = Integer.parseInt(mSyntax.group(IDLC_ERROR_LINE_GROUP));
            int offsetStart = Integer.parseInt(mSyntax.group(IDLC_ERROR_OFFSET_START_GROUP));
            int offsetEnd = Integer.parseInt(mSyntax.group(IDLC_ERROR_OFFSET_END_GROUP));
            String message = mSyntax.group(mSyntax.groupCount());

            // Get a handle on the bad file
            // Create a project relative path
            if (filePath.startsWith(project.getLocation().toOSString())) {
                int pos = project.getLocation().toOSString().length();
                filePath = filePath.substring(pos);
            }

            IFile file = project.getFile(filePath);
            try {
                int severity = IMarker.SEVERITY_WARNING;
                if (error) {
                    severity = IMarker.SEVERITY_ERROR;
                }

                marker = file.createMarker(IMarker.PROBLEM);
                marker.setAttribute(IMarker.SEVERITY, severity);
                marker.setAttribute(IMarker.MESSAGE, message);
                marker.setAttribute(IMarker.LINE_NUMBER, lineNo);
                marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);

                // HELP To print an icon in the editor vertical and overview rulers
                // Set the CHAR_START and CHAR_END attributes. They are relative to the
                // beginning or the end of the document.

                // HELP Without specifying the CHAR END and START, the line is well placed
                // But afterwards, pay attention when the marker should be located under
                // the bad words

                int lineOffset = getLineOffset(lineNo);

                marker.setAttribute(IMarker.CHAR_START, lineOffset + offsetStart - 1);
                marker.setAttribute(IMarker.CHAR_END, lineOffset + offsetEnd);

            } catch (CoreException e) {
                PluginLogger.error(Messages.getString("IdlcErrorReader.MarkerCreationError") //$NON-NLS-1$
                    + file.getProjectRelativePath().toString(), e);
            }
        }

        return marker;
    }

    /**
     * <p>
     * Method that analyzes the IDLC preprocessor error line and return the appropriate marker if it is possible.
     * </p>
     *
     * @param pLine
     *            error line to analyse
     * @return the corresponding marker if the line is an <code>idlc</code> error line. <code>null</code> if there were
     *         problems by creating the marker or if the line isn't an <code>idlc</code> error line.
     */
    private IMarker analyseIdlcppError(String pLine) {
        IMarker marker = null;

        Pattern pInclude = Pattern.compile(R_IDLCPP_ERROR);
        Matcher mInclude = pInclude.matcher(pLine);

        if (mInclude.matches()) {
            IProject project = mCompiledFile.getProject();

            String errorFilePath = mInclude.group(1);
            int lineNo = Integer.parseInt(mInclude.group(IDLC_ERROR_LINE_GROUP));
            String badIncludePath = mInclude.group(IDLCPP_INCLUDE_PATH_GROUP);

            if (null == mInclude.group(IDLCPP_OPTIONAL_GROUP)) {

                IFile errorFile;

                if (errorFilePath.startsWith(".")) { //$NON-NLS-1$
                    // A project local file, that means that the error is in a dependent file
                    errorFile = project.getFile(errorFilePath);

                } else {
                    // The error is in the file which was asked for compilation
                    errorFile = mCompiledFile;
                }

                String message = "idlcpp error: " + badIncludePath; //$NON-NLS-1$

                try {
                    marker = errorFile.createMarker(IMarker.PROBLEM);
                    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                    marker.setAttribute(IMarker.MESSAGE, message);
                    marker.setAttribute(IMarker.LINE_NUMBER, lineNo);
                    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

                    int lineOffset = getLineOffset(lineNo);

                    marker.setAttribute(IMarker.CHAR_START, lineOffset);
                    marker.setAttribute(IMarker.CHAR_END, lineOffset + getLineLength(lineNo));

                } catch (CoreException e) {
                    // Nothing to do. Do not create noise in the logs
                }
            }
        }

        return marker;
    }

    /**
     * Get the offset of the line relatively to the document beginning.
     *
     * @param pLine
     *            the line number
     *
     * @return the computed offset
     */
    private int getLineOffset(int pLine) {
        int offset = 0;

        try {
            // Get the line offset.
            LineNumberReader fileReader = new LineNumberReader(new InputStreamReader(mCompiledFile.getContents()));

            for (int i = 0, length = pLine - 1; i < length; i++) {
                String tmpLine = fileReader.readLine();
                offset += tmpLine.length() + 1;
            }
        } catch (Exception e) {
            // Nothing to report: the marker will be bad placed perhaps...
        }

        return offset;
    }

    /**
     * Get the length of the line.
     *
     * @param pLine
     *            the line number
     *
     * @return the length
     */
    private int getLineLength(int pLine) {
        int lineLen = 0;

        try {
            // Get the line offset.
            LineNumberReader fileReader = new LineNumberReader(new InputStreamReader(mCompiledFile.getContents()));

            for (int i = 0, length = pLine - 1; i < length; i++) {
                fileReader.readLine();
            }

            String line = fileReader.readLine();
            lineLen = line.length();
        } catch (Exception e) {
            // Nothing to report: the marker will be bad placed perhaps...
        }

        return lineLen;
    }
}