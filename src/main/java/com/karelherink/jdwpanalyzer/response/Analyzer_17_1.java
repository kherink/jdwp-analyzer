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
import com.karelherink.jdwpanalyzer.entity.ReferenceType;
import com.karelherink.jdwpanalyzer.model.*;
import com.karelherink.jdwpanalyzer.model.constants.TypeTag;

import java.util.ArrayList;

/**
 * @author karel herink
 */
public class Analyzer_17_1 extends PacketAnalyzer {

	public Analyzer_17_1() {
	}

	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		Long objTypeId = (Long) requestPacketAnalyzer.getPacketInfo(request).getValue().getRealValue();
		ObjectType objectType = ObjectType.getType(objTypeId);

		ArrayList nodes = getPacketInfo(packet).getChildren();
		Node refTypeIdInfo = (Node) nodes.get(1);
		Long refTypeId = (Long) refTypeIdInfo.getValue().getRealValue();
		objectType.setReferenceTypeId(refTypeId);
	}
    
    public Node getPacketInfo(Packet packet) {
		Node root = new Node(null, null);
		int index = 0;
		
		byte typeTag = packet.getData()[index++];
		Node tagInfo = new Node (new Node.Descriptor("TypeTag:"), new Node.Value(new Integer(typeTag), TypeTag.asString(typeTag)));
		root.addChild(tagInfo);
		
		long referenceTypeId = (int) getVal(packet, index, referenceTypeIDSize);
		index += referenceTypeIDSize;
		ReferenceType refType = ReferenceType.getType(referenceTypeId);
		Node referenceTypeIdInfo = new Node (new Node.Descriptor("RefTypeID:", refType), new Node.Value(new Long(referenceTypeId)));
		root.addChild(referenceTypeIdInfo);
		
		return root;
    }

}
