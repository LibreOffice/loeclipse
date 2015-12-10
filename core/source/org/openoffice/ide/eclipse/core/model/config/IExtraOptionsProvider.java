package org.openoffice.ide.eclipse.core.model.config;

/**
 * Gets a chance to add some more env variables just before starting an LibreOffice process.
 *
 * @author cdan
 *
 */
public interface IExtraOptionsProvider {

    /**
     * Add your own env entries.
     *
     * @param pEnv
     *            the original env variables.
     * @return the new env variables.
     */
    String[] addEnv(String[] pEnv);
}
