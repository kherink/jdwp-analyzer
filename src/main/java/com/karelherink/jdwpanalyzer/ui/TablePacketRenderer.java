/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.karelherink.jdwpanalyzer.ui;

import com.karelherink.jdwpanalyzer.model.Request;
import com.karelherink.jdwpanalyzer.model.Response;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author karel herink
 */
public class TablePacketRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 3257568412325786424L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp =  super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        //comp.setFont(new Font("Monospaced", Font.BOLD, 12));
        if (value instanceof Response) {
            Response resp = (Response) value;
            if (resp.isError())
                comp.setForeground(StatusColors.RESP_ERR_COLOR);
            else
                comp.setForeground(StatusColors.RESP_OK_COLOR);
        }
        else if (value instanceof Request) {
            //Request request = (Request) value;
            comp.setForeground(StatusColors.REQ_COLOR);
        }
        return comp;
    }
}
