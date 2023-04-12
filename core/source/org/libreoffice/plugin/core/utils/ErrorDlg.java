package org.libreoffice.plugin.core.utils;

import java.util.Objects;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Helper for MessageDialog.openError in syncExec<br>
 * usage:
 * Display.getDefault().syncExec(new ErrorDlg(errMsg));
 * 
 * @author Michael Massee
 *
 */

public class ErrorDlg implements Runnable {
    final String errmsg;

    public ErrorDlg(final String msg) {
        Objects.requireNonNull(msg);
        errmsg = msg;
    }

    @Override
    public void run() {
        MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", errmsg);
    }
}
