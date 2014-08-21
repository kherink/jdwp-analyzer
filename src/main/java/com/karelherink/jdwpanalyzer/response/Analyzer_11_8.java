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
package com.karelherink.jdwpanalyzer.response;

import com.karelherink.jdwpanalyzer.entity.ObjectType;
import com.karelherink.jdwpanalyzer.model.Node;
import com.karelherink.jdwpanalyzer.model.Packet;
import com.karelherink.jdwpanalyzer.model.PacketAnalyzer;

/**
 * @author karel herink
 */
public class Analyzer_11_8 extends PacketAnalyzer {

    public Analyzer_11_8() {
    }
    
    public Node getPacketInfo(Packet packet) {
		int index = 0;
		
        int numMonitors = (int) getVal(packet, index, 4);
        index += 4;
		Node root = new Node (new Node.Descriptor("NumMonitors:"), new Node.Value(new Integer(numMonitors)));
		
		for(int i = 0; i < numMonitors; i++) {
			
			Value value = getValue(packet, index);
			index += value.getOffset();
			
			//TODO - will this work?? The value here should be a Long objectId
			Long monitorObjectId = (Long) value.getValue();
	        ObjectType monitorObjectType = ObjectType.getType(monitorObjectId.longValue());
            Node monitorInfo = new Node (new Node.Descriptor("Monitor:", monitorObjectType), new Node.Value(value));
            
            root.addChild(monitorInfo);
		}
		return root;
    }
    
//    public Component getDetailedView(Packet packet) {
//		int index = 0;
//		JPanel panel = new JPanel();
//		panel.setLayout(new BorderLayout());
//
//		Vector colData;
//		Vector rowData = new Vector();
//		Vector colNames = new Vector();
//
//		colNames.add("Description");
//		colNames.add("Value");
//		
//		int numOwnedMonitors = (int) getVal(packet, index, 4);
//		index += 4;
//		colData = new Vector();
//		colData.add("Num Owned Monitors:");
//		colData.add(String.valueOf(numOwnedMonitors));
//		rowData.add(colData);
//
//		colData = new Vector();
//		colData.add("");
//		colData.add("");
//		rowData.add(colData);
//
//		for(int i = 0; i < numOwnedMonitors; i++) {
//			colData = new Vector();
//			colData.add("Monitor:");
//			Value value = getValue(packet, index);
//			colData.add(value.getValue());
//			rowData.add(colData);
//			index += value.getOffset();
//		}
//		
//		panel.add(new JScrollPane(new JTable(rowData, colNames)), BorderLayout.CENTER);
//		return panel;
//    }
}
