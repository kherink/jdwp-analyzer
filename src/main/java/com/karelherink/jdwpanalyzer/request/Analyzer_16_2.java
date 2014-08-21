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
package com.karelherink.jdwpanalyzer.request;

import com.karelherink.jdwpanalyzer.model.Node;
import com.karelherink.jdwpanalyzer.model.Packet;
import com.karelherink.jdwpanalyzer.model.PacketAnalyzer;

/**
 * @author karel herink
 */
public class Analyzer_16_2 extends PacketAnalyzer {

    public Analyzer_16_2() {
    }
    
    public Node getPacketInfo(Packet packet) {
		int index = 0;
		Node root = new Node(null, null);
				
		long threadId = getVal(packet, index, objectIDSize);
		index += objectIDSize;
		Node threadInfo = new Node(new Node.Descriptor("ThreadID:"), new Node.Value(new Long(threadId)));
		root.addChild(threadInfo);
		
		long frameId = getVal(packet, index, frameIDSize);
		index += frameIDSize;
		Node frameInfo = new Node(new Node.Descriptor("FrameID:"), new Node.Value(new Long(frameId)));
		root.addChild(frameInfo);
		
		int numValues = (int) getVal(packet, index, 4);
		index += 4;
		Node valuesInfo = new Node(new Node.Descriptor("NumValues:"), new Node.Value(new Integer(numValues)));
		root.addChild(valuesInfo);
		
		for (int i = 0; i < numValues; i++) {			
			int slotId = (int) getVal(packet, index, 4);
			index += 4;
			Node slotInfo = new Node(new Node.Descriptor("SlotID:"), new Node.Value(new Integer(slotId)));
			valuesInfo.addChild(slotInfo);
			
			Value value = getValue(packet, index);
			index += value.getOffset();
			Node valueInfo = new Node(new Node.Descriptor("Value:"), new Node.Value(value));
			valuesInfo.addChild(valueInfo);
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
//		colData = new Vector();
//		colData.add("Thread ID:");
//		colData.add(String.valueOf(getVal(packet, index, objectIDSize)));
//		rowData.add(colData);
//        index += objectIDSize;
//        
//		colData = new Vector();
//		colData.add("Frame ID:");
//		colData.add(String.valueOf(getVal(packet, index, frameIDSize)));
//		rowData.add(colData);
//        index += frameIDSize;
//        
//		int numSlots = (int) getVal(packet, index, 4);
//		index += 4;
//		colData = new Vector();
//		colData.add("Num Values:");
//		colData.add(String.valueOf(numSlots));
//		rowData.add(colData);
//
//		if (numSlots > 0) {
//			colData = new Vector();
//			colData.add("");
//			colData.add("");
//			rowData.add(colData);
//		}
//		
//		for(int i = 0; i < numSlots; i++) {
//			colData = new Vector();
//			colData.add("Slot ID:");
//			colData.add(String.valueOf(getVal(packet, index, 4)));
//			rowData.add(colData);
//	        index += 4;
//
//			Value value = getValue(packet, index);
//            colData = new Vector();
//			colData.add("Value:");
//			colData.add(value.getValue());
//			rowData.add(colData);
//            index += value.getOffset();
//		}
//    	
//		panel.add(new JScrollPane(new JTable(rowData, colNames)), BorderLayout.CENTER);
//		return panel;
//    }
}
