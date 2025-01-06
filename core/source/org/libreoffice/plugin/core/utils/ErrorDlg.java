/*************************************************************************
 * ErrorDlg.java
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 *
 * Contributor(s): Michael Massee
 ************************************************************************/

package org.libreoffice.plugin.core.utils;

import java.util.Objects;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Helper for MessageDialog.openError in syncExec<br>.
 * usage:
 * Display.getDefault().syncExec(new ErrorDlg(errMsg));
 */

public class ErrorDlg implements Runnable {
    final String mMsg;

    public ErrorDlg(final String msg) {
        Objects.requireNonNull(msg);
        mMsg = msg;
    }

    @Override
    public void run() {
        MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", mMsg);
    }
}
