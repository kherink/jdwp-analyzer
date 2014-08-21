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

import com.karelherink.jdwpanalyzer.entity.ReferenceType;
import com.karelherink.jdwpanalyzer.model.Node;
import com.karelherink.jdwpanalyzer.model.Packet;
import com.karelherink.jdwpanalyzer.model.PacketAnalyzer;
import com.karelherink.jdwpanalyzer.model.constants.ClassStatus;
import com.karelherink.jdwpanalyzer.model.constants.TypeTag;

import java.util.List;

/**
 * @author karel herink
 */
public class Analyzer_1_3 extends PacketAnalyzer {

    public Analyzer_1_3() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		
		List classesInfo = this.getPacketInfo(packet).getChildren();
		for (int i = 0; i < classesInfo.size(); i++) {
			Node singleClassInfo = (Node) classesInfo.get(i);
			Long refTypeId = (Long) singleClassInfo.getValue().getRealValue();
			ReferenceType refType = ReferenceType.getType(refTypeId);
			
			Node typeTag = (Node) singleClassInfo.getChildren().get(0);
			refType.setTypeTag((Byte) typeTag.getValue().getRealValue());
			
			Node typeStatus = (Node) singleClassInfo.getChildren().get(1);
			refType.setStatus((Integer) typeStatus.getValue().getRealValue());
			
			Node sig = (Node) singleClassInfo.getChildren().get(2);
			refType.setSignature((String) sig.getValue().getRealValue());
		}
	}

    public Node getPacketInfo(Packet packet) {
		int index = 0;
				
		int numClasses = (int) getVal(packet, index, 4);
		index += 4;
		Node classesInfo = new Node (new Node.Descriptor("NumClasses:"), new Node.Value(new Integer(numClasses)));
		
		for (int i = 0; i < numClasses; i++) {
			byte typeTag = (byte) (packet.getData()[index++] & 0xff);
			
			long refTypeId = getVal(packet, index, referenceTypeIDSize);
			index += referenceTypeIDSize;
			
			int len = (int) getVal(packet, index, 4);
			index += 4;
			String signature = getStr(packet, index, len);
			index += len;

			int classStatus = (int) getVal(packet, index, 4);
            index += 4;


			ReferenceType referenceType = ReferenceType.getType(refTypeId);
			Node singleClassInfo = new Node(new Node.Descriptor("RefTypeID:", referenceType), new Node.Value(new Long(refTypeId)));
			classesInfo.addChild(singleClassInfo);
			
			Node typeTagInfo = new Node(new Node.Descriptor("TypeTag:"), new Node.Value(new Byte(typeTag), TypeTag.asString(typeTag)));
			singleClassInfo.addChild(typeTagInfo);
			
			Node typeStatusInfo = new Node(new Node.Descriptor("TypeStatus:"), new Node.Value(new Integer(classStatus), ClassStatus.asString(classStatus)));
			singleClassInfo.addChild(typeStatusInfo);
			
			Node signatureInfo = new Node(new Node.Descriptor("Signature:"), new Node.Value(signature));
			singleClassInfo.addChild(signatureInfo);
			
		}
		return classesInfo;
	}
}
