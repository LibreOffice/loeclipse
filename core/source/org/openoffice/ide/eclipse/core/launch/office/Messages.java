package org.openoffice.ide.eclipse.core.launch.office;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.openoffice.ide.eclipse.core.launch.office.messages"; //$NON-NLS-1$
    public static String OfficeLaunchDelegate_LaunchError;
    public static String OfficeLaunchDelegate_LaunchErrorTitle;
    public static String OfficeTab_Options;
    public static String OfficeTab_Configurationerror;
    public static String OfficeTab_ProjectNameLabel;
    public static String OfficeTab_Title;
    public static String OfficeTab_UnoProject;
    public static String OfficeTab_ChkUseCleanUserInstallation;
    public static String OfficeTab_ChkUseCleanUserInstallation_ToolTip;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
