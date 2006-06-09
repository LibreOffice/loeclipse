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
   
    /**
     * Returns the instance of the singleton.
     */
    static public PluginLogger getInstance() {
    	
    	if (null == __instance){
    		__instance = new PluginLogger();
    	}
    	
    	return __instance;
    }
    
    /**
     * Logs a debug message
     */
    public void debug(String message) {
    	if (level.equals(DEBUG)) {
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
    public void info(String message) {
    	if (level.equals(DEBUG) || level.equals(INFO)) {
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
    public void warning(String message) {
    	if (!level.equals(ERROR)) {
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
    public void error(String message, Exception e) {
    	
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
    public void error(String message){
    	error(message, null);
    }
    
    /**
     * Changes the minimum level of the message printed to the log view.
     */
    public void setLevel(String aLevel){
    	if (aLevel != null && (
    			aLevel.equals(DEBUG) ||
    			aLevel.equals(INFO) ||
    			aLevel.equals(WARNING) ||
    			aLevel.equals(ERROR))) {
    		level = aLevel;
    	}
    };
    
    /**
     * Checks whether the logger will return a message of a certain level 
     * 
     * @param aLevel the level of the message to print
     * @return <code>true</code> if the level is highter or equals to the 
     * 		current log level.
     */
    public boolean isLevel(String aLevel) {
    	
    	boolean result = false;
    	
    	if (aLevel.equals(ERROR) || 
    			(aLevel.equals(WARNING) && !level.equals(ERROR)) ||
    			(aLevel.equals(INFO) && (level.equals(DEBUG) || level.equals(INFO)))||
    			(aLevel.equals(DEBUG) && level.equals(DEBUG))) {
    		result = true;
    	}
    	
		return result;
	}
    
    /**
     * A private constructor for the singleton
     */
    private PluginLogger() {
    }
    
    static private PluginLogger __instance;
    private String level = DEBUG;
}
