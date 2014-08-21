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
public class Analyzer_16_3 extends PacketAnalyzer {

    public Analyzer_16_3() {
    }
    
	public Node getPacketInfo(Packet packet) {
		int index = 0;

		Value value = getValue(packet, index);
		index += value.getOffset();
		Long objectId = (Long) value.getValue();
		ObjectType objectType = ObjectType.getType(objectId);
		Node singleValueInfo = new Node(new Node.Descriptor("'This' ObjectID:", objectType), new Node.Value(value));
		
		return singleValueInfo;
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
//		colData.add("'This' Object:");
//		colData.add(taggedObjectIDToString(packet, index));
//		rowData.add(colData);
//		index += 1 + objectIDSize;
//    	
//		panel.add(new JScrollPane(new JTable(rowData, colNames)), BorderLayout.CENTER);
//		return panel;
//    }
}
