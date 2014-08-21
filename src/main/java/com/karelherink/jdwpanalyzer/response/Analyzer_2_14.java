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
import com.karelherink.jdwpanalyzer.entity.ReferenceType;
import com.karelherink.jdwpanalyzer.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author karel herink
 */
public class Analyzer_2_14 extends PacketAnalyzer {

    public Analyzer_2_14() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		Long referenceTypeId = (Long) requestPacketAnalyzer.getPacketInfo(request).getValue().getRealValue();
		ReferenceType referenceType = ReferenceType.getType(referenceTypeId);
		
		List fields = this.getPacketInfo(packet).getChildren();
		List fieldIDs = new ArrayList();
		for (int i = 0; i < fields.size(); i++) {
			Node child = (Node) fields.get(i);
			Long fieldId = (Long) child.getValue().getRealValue();
			fieldIDs.add(fieldId);
			
			FieldType fieldType = FieldType.getType(referenceTypeId, fieldId);
			List grandChildren = child.getChildren();
			fieldType.setName((String) ((Node) grandChildren.get(0)).getValue().getRealValue());
			fieldType.setSignature((String) ((Node) grandChildren.get(1)).getValue().getRealValue());
			fieldType.setSignatureWithGeneric((String) ((Node) grandChildren.get(2)).getValue().getRealValue());
			fieldType.setModBits((Integer) ((Node) grandChildren.get(3)).getValue().getRealValue());
		}
		
		referenceType.setFieldIds((Long[]) fieldIDs.toArray(new Long[fieldIDs.size()]));
	}
    
    public Node getPacketInfo(Packet packet) {
		int index = 0;		
		int numFields = (int) getVal(packet, index, 4);
		index += 4;
		
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		Long referenceTypeId = (Long) requestPacketAnalyzer.getPacketInfo(request).getValue().getRealValue();
		ReferenceType referenceType = ReferenceType.getType(referenceTypeId);

		Node root = new Node (new Node.Descriptor("NumFields:"), new Node.Value(new Integer(numFields)));
		
		for (int i = 0; i < numFields; i++) {
			
			long fieldID = (getVal(packet, index, fieldIDSize));
			index += fieldIDSize;
			FieldType fieldType = FieldType.getType(referenceTypeId, new Long(fieldID));
			Node singleFieldInfo = new Node(new Node.Descriptor("FieldID:", fieldType), new Node.Value(new Long(fieldID)));
			root.addChild(singleFieldInfo);
			
            int len = (int) getVal(packet, index, 4);
            index += 4;
			Node singleFieldNameInfo = new Node(new Node.Descriptor("Name:"), new Node.Value(getStr(packet, index, len)));
            index += len;
            singleFieldInfo.addChild(singleFieldNameInfo);
            
            len = (int) getVal(packet, index, 4);
            index += 4;
			Node singleFieldSignatureInfo = new Node(new Node.Descriptor("Signature:"), new Node.Value(getStr(packet, index, len)));
            index += len;
            singleFieldInfo.addChild(singleFieldSignatureInfo);
            
            len = (int) getVal(packet, index, 4);
            index += 4;
			Node singleFieldGenericSignatureInfo = new Node(new Node.Descriptor("GenericSignature:"), new Node.Value(getStr(packet, index, len)));
            index += len;
            singleFieldInfo.addChild(singleFieldGenericSignatureInfo);
            
            int modBits = (int) getVal(packet, index, 4);
            index += 4;
			Node singleFieldModBitsInfo = new Node(new Node.Descriptor("ModBits:"), new Node.Value(new Integer(modBits), getModBitsAsStr(modBits)));
			singleFieldInfo.addChild(singleFieldModBitsInfo);
		}
		return root;
	}
}
