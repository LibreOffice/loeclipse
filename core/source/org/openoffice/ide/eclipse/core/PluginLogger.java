package org.openoffice.ide.eclipse.core;

import org.eclipse.core.runtime.Status;

/**
 * This class is the plugin central log singleton. It supports 4 levels of 
 * messages on the contrary of the Java <code>Logger</code> class which 
 * contains 5. This class adds the messages to the Eclipse log view.
 * 
 * @author cbosdonnat
 */
public class PluginLogger {

    public static final String ERROR    = "error";
    public static final String WARNING  = "warning";
    public static final String INFO     = "info";
    public static final String DEBUG    = "debug";

    private static String sLevel = DEBUG;
    
    /**
     * Logs a debug message
     */
    public static void debug(String message) {
    	if (sLevel.equals(DEBUG)) {
    		OOEclipsePlugin.getDefault().getLog().log(new Status(
    				Status.OK, 
    				OOEclipsePlugin.getDefault().getBundle().getSymbolicName(),
    				Status.OK,
    				message,
    				null));
    	}
    }
    
    /**
     * Logs a information message
     */
    public static void info(String message) {
    	if (sLevel.equals(DEBUG) || sLevel.equals(INFO)) {
    		OOEclipsePlugin.getDefault().getLog().log(new Status(
    				Status.INFO, 
    				OOEclipsePlugin.getDefault().getBundle().getSymbolicName(),
    				Status.INFO,
    				message,
    				null));
    	}
    }
    
    /**
	 * Logs a warning message
	 */
    public static void warning(String message) {
    	if (!sLevel.equals(ERROR)) {
    		OOEclipsePlugin.getDefault().getLog().log(new Status(
    				Status.WARNING, 
    				OOEclipsePlugin.getDefault().getBundle().getSymbolicName(),
    				Status.WARNING,
    				message,
    				null));
    	}
    }
    
    /**
	 * Logs an error message an optionaly the stack trace of the exception
	 * which causes the error.
	 *   
	 * @param message Message to print in the error log view
	 * @param e Exception raised. Could be null.
	 */
    public static void error(String message, Throwable e) {
    	
		OOEclipsePlugin.getDefault().getLog().log(new Status(
				Status.ERROR, 
				OOEclipsePlugin.getDefault().getBundle().getSymbolicName(),
				Status.ERROR,
				message,
				e));
    }
    
    /**
     * Logs an error message without cause exception.
     */
    public static void error(String message){
    	error(message, null);
    }
    
    /**
     * Changes the minimum level of the message printed to the log view.
     */
    public static void setLevel(String aLevel){
    	if (aLevel != null && (
    			aLevel.equals(DEBUG) ||
    			aLevel.equals(INFO) ||
    			aLevel.equals(WARNING) ||
    			aLevel.equals(ERROR))) {
    		sLevel = aLevel;
    	}
    };
    
    /**
     * Checks whether the logger will return a message of a certain level 
     * 
     * @param aLevel the level of the message to print
     * @return <code>true</code> if the level is highter or equals to the 
     * 		current log level.
     */
    public static boolean isLevel(String aLevel) {
    	
    	boolean result = false;
    	
    	if (aLevel.equals(ERROR) || 
    			(aLevel.equals(WARNING) && !sLevel.equals(ERROR)) ||
    			(aLevel.equals(INFO) && (sLevel.equals(DEBUG) || sLevel.equals(INFO)))||
    			(aLevel.equals(DEBUG) && sLevel.equals(DEBUG))) {
    		result = true;
    	}
    	
		return result;
	}
}
