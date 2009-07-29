package org.openoffice.ide.eclipse.core.editors.main;

import java.util.Locale;

/**
 * Interface to implement in order to get notified of Locale changes in a control.
 * 
 * @author cbosdonnat
 *
 */
public interface ILocaleListener {

    /**
     * The locale selection has changed.
     * 
     * @param pLocale the new locale to use.
     */
    public void selectLocale( Locale pLocale );
    
    /**
     * A locale has been deleted.
     * 
     * @param pLocale the deleted locale
     */
    public void deleteLocale( Locale pLocale );
    
    /**
     * A locale has been added.
     * 
     * @param pLocale the added locale
     */
    public void addLocale( Locale pLocale );
}
