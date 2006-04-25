package org.openoffice.ide.eclipse.core;

import org.eclipse.core.runtime.Status;

public class PluginLogger {

    public static final String ERROR    = "error";
    public static final String WARNING  = "warning";
    public static final String INFO     = "info";
    public static final String DEBUG    = "debug";
   
    static public PluginLogger getInstance() {
    	
    	if (null == __instance){
    		__instance = new PluginLogger();
    	}
    	
    	return __instance;
    }
    
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
	 * This static method is provided to easier log warnings in the eclipse 
	 * error view.
	 * 
	 * @param message Message to print in the warning log view
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
	 * This static method is provided to easier log errors in the eclipse error view
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
    
    public void error(String message){
    	error(message, null);
    }
    
    public void setLevel(String aLevel){
    	if (aLevel != null && (
    			aLevel.equals(DEBUG) ||
    			aLevel.equals(INFO) ||
    			aLevel.equals(WARNING) ||
    			aLevel.equals(ERROR))) {
    		level = aLevel;
    	}
    };
    
    private PluginLogger() {
    	// TODO Load the preferences here
    }
    
    static private PluginLogger __instance;
    private String level = DEBUG;
}
