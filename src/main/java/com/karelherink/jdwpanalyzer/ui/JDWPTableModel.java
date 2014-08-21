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

import com.karelherink.jdwpanalyzer.model.Packet;
import com.karelherink.jdwpanalyzer.model.Request;
import com.karelherink.jdwpanalyzer.model.Response;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author karel herink
 */
public class JDWPTableModel implements TableModel {

    private final static int REQUEST_COL = 0;
    private final static int RESPONSE_COL = 1;
    
    private final static int COL_COUNT = 2;
    
    private List tableModelListeners = new ArrayList();
    private List sequentialRequests;
    
    public JDWPTableModel(List sequentialRequests) {
        this.sequentialRequests = sequentialRequests;
    }
    
    public int getColumnCount() {
        return COL_COUNT;
    }

    public int getRowCount() {
        return sequentialRequests.size();
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Class getColumnClass(int columnIndex) {
        return Packet.class;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || columnIndex < 0 || rowIndex >= this.sequentialRequests.size() || columnIndex >= COL_COUNT)
            return null;
        
        Request request = (Request) this.sequentialRequests.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return request;
            case 1:
                int id = request.getId();
                Response response = Response.getResponse(id);
                if (response == null)
                    return null;
                return response;

            default:
                return null;
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        //NOT SUPPORTED
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case REQUEST_COL:
                return "Requests";
            case RESPONSE_COL:
                return "Responses";

            default:
                return "FIX getColumnName()";
        }
    }

    public void addTableModelListener(TableModelListener l) {
        this.tableModelListeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l) {
        this.tableModelListeners.remove(l);

    }

    public int[] update(Packet packet) {
        int[] position = this.getPacketTablePosition(packet);
        TableModelEvent event = new TableModelEvent(this, position[0], position[0], position[1], TableModelEvent.INSERT);
        for (Iterator iter = this.tableModelListeners.iterator(); iter.hasNext();) {
            TableModelListener listener = (TableModelListener) iter.next();
            listener.tableChanged(event);
        }
        return position;
    }
    
    private int[] getPacketTablePosition(Packet packet) {
        /* [ROW, COL] */
        int[] position = new int[2];
        if (packet.isResponse()) {
            Request request = Request.getRequest(packet.getId());
            position[0] = request.getSequentialRequestNumber();
            position[1] = RESPONSE_COL;
        }
        else {
            Request request = (Request) packet; 
            position[0] = request.getSequentialRequestNumber();
            position[1] = REQUEST_COL;
        }
        return position;
    }
}
