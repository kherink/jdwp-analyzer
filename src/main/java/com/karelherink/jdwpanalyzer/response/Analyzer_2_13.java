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
import com.karelherink.jdwpanalyzer.model.*;

import java.util.List;

/**
 * @author karel herink
 */
public class Analyzer_2_13 extends PacketAnalyzer {

    public Analyzer_2_13() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		Long referenceTypeId = (Long) requestPacketAnalyzer.getPacketInfo(request).getValue().getRealValue();
		ReferenceType referenceType = ReferenceType.getType(referenceTypeId.longValue());
		
		List children = this.getPacketInfo(packet).getChildren();
		String signature = (String) ((Node) children.get(0)).getValue().getRealValue();
		String genericSignature = (String) ((Node) children.get(1)).getValue().getRealValue();
		referenceType.setSignature(signature);
		referenceType.setSignatureWithGeneric(genericSignature);
	}
	
	public Node getPacketInfo(Packet packet) {
		Node root = new Node(null, null);
		int index = 0;
		
		int len = (int) getVal(packet, index, 4);
		index += 4;
		String signature = getStr(packet, index, len);
		index += len;
		Node sigNode = new Node(new Node.Descriptor("Signature:"), new Node.Value(signature));
		root.addChild(sigNode);
		
		len = (int) getVal(packet, index, 4);
		index += 4;
		String genericSignature = getStr(packet, index, len);
		index += len;
		Node genSigNode = new Node(new Node.Descriptor("GenericSignature:"), new Node.Value(genericSignature));
		root.addChild(genSigNode);
		
		return root;
	}    

}
