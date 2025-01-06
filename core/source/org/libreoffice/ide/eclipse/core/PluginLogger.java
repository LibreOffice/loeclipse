/*************************************************************************
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
package org.libreoffice.ide.eclipse.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class is the plugin central log singleton. It supports 4 levels of messages on the contrary of the Java
 * <code>Logger</code> class which contains 5. This class adds the messages to the Eclipse log view.
 */
public class PluginLogger {

    private static LogLevels sLevel = LogLevels.DEBUG;

    /**
     * Logs a debug message.
     *
     * @param message
     *            the message to log
     * @param exc
     *            the exception causing the message
     */
    public static void debug(String message, Throwable exc) {
        if (sLevel.equals(LogLevels.DEBUG)) {
            OOEclipsePlugin.getDefault().getLog().log(new Status(IStatus.OK,
                OOEclipsePlugin.getDefault().getBundle().getSymbolicName(), IStatus.OK, message, exc));
        }
    }

    /**
     * Logs a debug message.
     *
     * @param message
     *            the message to log
     */
    public static void debug(String message) {
        debug(message, null);
    }

    /**
     * Logs a information message.
     *
     * @param message
     *            the message to log
     */
    public static void info(String message) {
        if (sLevel.equals(LogLevels.DEBUG) || sLevel.equals(LogLevels.INFO)) {
            OOEclipsePlugin.getDefault().getLog().log(new Status(IStatus.INFO,
                OOEclipsePlugin.getDefault().getBundle().getSymbolicName(), IStatus.INFO, message, null));
        }
    }

    /**
     * Logs a warning message.
     *
     * @param message
     *            the message to log
     */
    public static void warning(String message) {
        warning(message, null);
    }

    /**
     * Logs a warning message caused by an exception.
     *
     * @param message
     *            the message to log
     * @param exc
     *            exception raised. Could be <code>null</code>
     */
    public static void warning(String message, Throwable exc) {
        if (!sLevel.equals(LogLevels.ERROR)) {
            OOEclipsePlugin.getDefault().getLog()
                .log(new Status(IStatus.WARNING, OOEclipsePlugin.getDefault().getBundle().getSymbolicName(),
                IStatus.WARNING, message, exc));
        }
    }

    /**
     * Logs an error message an optionally the stack trace of the exception which causes the error.
     *
     * @param message
     *            Message to print in the error log view
     * @param exc
     *            Exception raised. Could be <code>null</code>.
     */
    public static void error(String message, Throwable exc) {

        OOEclipsePlugin.getDefault().getLog().log(new Status(IStatus.ERROR,
            OOEclipsePlugin.getDefault().getBundle().getSymbolicName(), IStatus.ERROR, message, exc));
    }

    /**
     * Logs an error message without cause exception.
     *
     * @param message
     *            Message to print in the error log view
     */
    public static void error(String message) {
        error(message, null);
    }

    /**
     * Changes the minimum level of the message printed to the log view.
     *
     * @param level
     *            the level to set
     */
    public static void setLevel(LogLevels level) {
        sLevel = level;
    };

    /**
     * Checks whether the logger will return a message of a certain level.
     *
     * @param level
     *            the level of the message to print
     * @return <code>true</code> if the level is higher or equals to the current log level.
     */
    public static boolean isLevel(LogLevels level) {

        boolean result = false;

        boolean testWarning = level.equals(LogLevels.WARNING) && !sLevel.equals(LogLevels.ERROR);
        boolean testInfo = level.equals(LogLevels.INFO)
            && (sLevel.equals(LogLevels.DEBUG) || sLevel.equals(LogLevels.INFO));
        boolean testDebug = level.equals(LogLevels.DEBUG) && sLevel.equals(LogLevels.DEBUG);
        if (level.equals(LogLevels.ERROR) || testWarning || testInfo || testDebug) {
            result = true;
        }

        return result;
    }
}
