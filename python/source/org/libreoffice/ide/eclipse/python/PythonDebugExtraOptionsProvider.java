package org.libreoffice.ide.eclipse.python;

import org.libreoffice.ide.eclipse.core.model.config.IExtraOptionsProvider;
import org.libreoffice.ide.eclipse.core.model.utils.SystemHelper;

/**
 * Adds extra env variables to start LibreOffice with Java setup to run in debug mode.
 */
public class PythonDebugExtraOptionsProvider implements IExtraOptionsProvider {

    private String mPort;
    private String OOO_EXTRA_JAVA_TOOL_OPTIONS = "OOO_EXTRA_JAVA_TOOL_OPTIONS"; //$NON-NLS-1$

    /**
     *
     * @param pPort
     *            the port to listen to.
     */
    public PythonDebugExtraOptionsProvider(String pPort) {
        this.mPort = pPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] addEnv(String[] pEnv) {
        String extraJavaOptEnv = System.getenv(OOO_EXTRA_JAVA_TOOL_OPTIONS);
        if (extraJavaOptEnv == null) {
            extraJavaOptEnv = new String();
        } else {
            extraJavaOptEnv = extraJavaOptEnv.replaceAll("\"", "\\\""); //$NON-NLS-1$//$NON-NLS-2$
        }

        pEnv = SystemHelper.addEnv(pEnv, "JAVA_TOOL_OPTIONS", //$NON-NLS-1$
            extraJavaOptEnv + "\"-Xdebug\" " + //$NON-NLS-1$
                "\"-Xrunjdwp:transport=dt_socket,address=localhost:" + mPort + "\"", //$NON-NLS-1$//$NON-NLS-2$
            null);
        return pEnv;
    }

}
