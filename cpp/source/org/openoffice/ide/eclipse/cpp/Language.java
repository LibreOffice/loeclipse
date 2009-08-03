package org.openoffice.ide.eclipse.cpp;

import org.openoffice.ide.eclipse.core.model.language.ILanguage;
import org.openoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.openoffice.ide.eclipse.core.model.language.ILanguageUI;
import org.openoffice.ide.eclipse.core.model.language.IProjectHandler;

public class Language implements ILanguage {

    @Override
    public ILanguageBuilder getLanguageBuidler() {
        return new CppBuilder( this );
    }

    @Override
    public ILanguageUI getLanguageUI() {
        return new CppUI( );
    }

    @Override
    public IProjectHandler getProjectHandler() {
        return new CppProjectHandler( );
    }

}
