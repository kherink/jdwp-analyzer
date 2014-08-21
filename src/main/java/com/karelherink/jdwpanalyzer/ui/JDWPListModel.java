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

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author karel herink
 */
public class JDWPListModel implements ListModel {

    private List listeners;
    private List sequentialPackets;
    
    public JDWPListModel() {
        this.listeners = new ArrayList();
        this.sequentialPackets = new ArrayList(5000);
    }
    
    public int getSize() {
        return this.sequentialPackets.size();
    }

    public Object getElementAt(int index) {
        if (index >= this.getSize())
            return null;
        Packet packet = (Packet) this.sequentialPackets.get(index);
        return packet;
    }

    public void addListDataListener(ListDataListener l) {
        this.listeners.add(l);
    }

    public void removeListDataListener(ListDataListener l) {
        this.listeners.remove(l);
    }

    public int update(Packet packet) {
        this.sequentialPackets.add(packet);
        int position = this.sequentialPackets.size() -1;
        ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, (int) packet.getSequentialPacketNumber(), (int) packet.getSequentialPacketNumber());
        for (Iterator iter = this.listeners.iterator(); iter.hasNext();) {
            ListDataListener listener = (ListDataListener) iter.next();
            listener.contentsChanged(event);
        }
        return position;
    }
}
