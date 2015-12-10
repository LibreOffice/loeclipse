package org.openoffice.ide.eclipse.core.model.config;

/**
 * Does nothing.
 *
 * @author cdan
 *
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
