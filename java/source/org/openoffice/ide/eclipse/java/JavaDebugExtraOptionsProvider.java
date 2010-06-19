package org.openoffice.ide.eclipse.java;

import org.openoffice.ide.eclipse.core.helpers.SystemHelper;
import org.openoffice.ide.eclipse.core.model.config.IExtraOptionsProvider;

/**
 * Adds extra env variables to start OpenOffice with Java setup to run in debug mode.
 * 
 * @author cdan
 * 
 */
public class JavaDebugExtraOptionsProvider implements IExtraOptionsProvider {

    private String mPort;

    /**
     * 
     * @param pPort
     *            the port to listen to.
     */
    public JavaDebugExtraOptionsProvider(String pPort) {
        this.mPort = pPort;
    }

    /**
     * {@inheritDoc}
     */
    public String[] addEnv(String[] pEnv) {
        pEnv = SystemHelper.addEnv(pEnv, "JAVA_TOOL_OPTIONS", "\"-Xdebug\" "
                        + "\"-Xrunjdwp:transport=dt_socket,address=localhost:" + mPort + "\"", null);
        return pEnv;
    }

}
