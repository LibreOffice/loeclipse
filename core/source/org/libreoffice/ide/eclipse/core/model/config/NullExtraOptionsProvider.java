package org.libreoffice.ide.eclipse.core.model.config;

/**
 * Does nothing.
 */
public class NullExtraOptionsProvider implements IExtraOptionsProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] addEnv(String[] pEnv) {
        return pEnv;
    }

}
