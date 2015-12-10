/*************************************************************************
 *
 * $RCSfile: PluginLogger.java,v $
 *
 * $Revision: 1.6 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:30 $
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
package org.openoffice.ide.eclipse.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This class is the plugin central log singleton. It supports 4 levels of
 * messages on the contrary of the Java <code>Logger</code> class which
 * contains 5. This class adds the messages to the Eclipse log view.
 *
 * @author cbosdonnat
 */
public class PluginLogger {

    private static LogLevels sLevel = LogLevels.DEBUG;

    /**
     * Logs a debug message.
     *
     * @param pMessage the message to log
     * @param pExc the exception causing the message
     */
    public static void debug(String pMessage, Throwable pExc) {
        if (sLevel.equals(LogLevels.DEBUG)) {
            OOEclipsePlugin.getDefault().getLog().log(new Status(
                            IStatus.OK,
                            OOEclipsePlugin.getDefault().getBundle().getSymbolicName(),
                            IStatus.OK,
                            pMessage,
                            pExc));
        }
    }

    /**
     * Logs a debug message.
     *
     * @param pMessage the message to log
     */
    public static void debug(String pMessage) {
        debug(pMessage, null);
    }

    /**
     * Logs a information message.
     *
     * @param pMessage the message to log
     */
    public static void info(String pMessage) {
        if (sLevel.equals(LogLevels.DEBUG) || sLevel.equals(LogLevels.INFO)) {
            OOEclipsePlugin.getDefault().getLog().log(new Status(
                            IStatus.INFO,
                            OOEclipsePlugin.getDefault().getBundle().getSymbolicName(),
                            IStatus.INFO,
                            pMessage,
                            null));
        }
    }

    /**
     * Logs a warning message.
     *
     * @param pMessage the message to log
     */
    public static void warning(String pMessage) {
        warning(pMessage, null);
    }

    /**
     * Logs a warning message caused by an exception.
     *
     * @param pMessage the message to log
     * @param pExc exception raised. Could be <code>null</code>
     */
    public static void warning(String pMessage, Throwable pExc) {
        if (!sLevel.equals(LogLevels.ERROR)) {
            OOEclipsePlugin.getDefault().getLog().log(new Status(
                            IStatus.WARNING,
                            OOEclipsePlugin.getDefault().getBundle().getSymbolicName(),
                            IStatus.WARNING,
                            pMessage,
                            pExc));
        }
    }

    /**
     * Logs an error message an optionally the stack trace of the exception
     * which causes the error.
     *
     * @param pMessage Message to print in the error log view
     * @param pExc Exception raised. Could be <code>null</code>.
     */
    public static void error(String pMessage, Throwable pExc) {

        OOEclipsePlugin.getDefault().getLog().log(new Status(
                        IStatus.ERROR,
                        OOEclipsePlugin.getDefault().getBundle().getSymbolicName(),
                        IStatus.ERROR,
                        pMessage,
                        pExc));
    }

    /**
     * Logs an error message without cause exception.
     *
     * @param pMessage Message to print in the error log view
     */
    public static void error(String pMessage) {
        error(pMessage, null);
    }

    /**
     * Changes the minimum level of the message printed to the log view.
     *
     * @param pLevel the level to set
     */
    public static void setLevel(LogLevels pLevel) {
        sLevel = pLevel;
    };

    /**
     * Checks whether the logger will return a message of a certain level.
     *
     * @param pLevel the level of the message to print
     * @return <code>true</code> if the level is higher or equals to the
     *         current log level.
     */
    public static boolean isLevel(LogLevels pLevel) {

        boolean result = false;


        boolean testWarning = pLevel.equals(LogLevels.WARNING) && !sLevel.equals(LogLevels.ERROR);
        boolean testInfo = pLevel.equals(LogLevels.INFO) && (sLevel.equals(LogLevels.DEBUG) ||
                        sLevel.equals(LogLevels.INFO));
        boolean testDebug = pLevel.equals(LogLevels.DEBUG) && sLevel.equals(LogLevels.DEBUG);
        if (pLevel.equals(LogLevels.ERROR) ||
                        testWarning || testInfo || testDebug) {
            result = true;
        }

        return result;
    }
}
