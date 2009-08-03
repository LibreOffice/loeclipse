package org.openoffice.ide.eclipse.cpp;

import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.language.ILanguageUI;
import org.openoffice.ide.eclipse.core.model.language.LanguageWizardPage;

public class CppUI implements ILanguageUI {

    /**
     * There is no need for C++ only options: then no page.
     */
    @Override
    public LanguageWizardPage getWizardPage(UnoFactoryData data) {
        return null;
    }
}
