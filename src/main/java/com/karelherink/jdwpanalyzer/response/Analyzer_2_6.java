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

import com.karelherink.jdwpanalyzer.entity.FieldType;
import com.karelherink.jdwpanalyzer.model.*;

import java.util.List;

/**
 * @author karel herink
 */
public class Analyzer_2_6 extends PacketAnalyzer {

    public Analyzer_2_6() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		
		Node retTypeIdNode = ((Node)requestPacketAnalyzer.getPacketInfo(request).getChildren().get(0));
		Long refTypeId = (Long) retTypeIdNode.getValue().getRealValue();
		
		List fieldIdNodes = ((Node)requestPacketAnalyzer.getPacketInfo(request).getChildren().get(1)).getChildren();
		List fieldValueNodes = this.getPacketInfo(packet).getChildren();
		
		for (int i = 0; i < fieldIdNodes.size(); i++) {
			Long fieldId = (Long) ((Node) fieldIdNodes.get(i)).getValue().getRealValue();
			FieldType fieldType = FieldType.getType(refTypeId, fieldId);
			Value value = (Value) ((Node) fieldValueNodes.get(i)).getValue().getRealValue();
			fieldType.setValue(value);
		}
	}    
    
    public Node getPacketInfo(Packet packet) {
		int index = 0;
				
		int numValues = (int) getVal(packet, index, 4);
		index += 4;
		Node valuesInfo = new Node (new Node.Descriptor("NumValues:"), new Node.Value(new Integer(numValues)));
		
		for (int i = 0; i < numValues; i++) {
			Value value = getValue(packet, index);
			index += value.getOffset();
			Node singleValueInfo = new Node(new Node.Descriptor("Value:"), new Node.Value(value));
			valuesInfo.addChild(singleValueInfo);
		}
		return valuesInfo;
	}

}
