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

import com.karelherink.jdwpanalyzer.entity.MethodType;
import com.karelherink.jdwpanalyzer.model.*;

import java.util.List;

/**
 * @author karel herink
 */
public class Analyzer_6_3 extends PacketAnalyzer {

    public Analyzer_6_3() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		List requestChildren = requestPacketAnalyzer.getPacketInfo(request).getChildren();

		Long referenceTypeId = (Long) ((Node) requestChildren.get(0)).getValue().getRealValue();
		Long methId = (Long) ((Node) requestChildren.get(1)).getValue().getRealValue();
		
		MethodType methType = MethodType.getType(referenceTypeId, methId);
		
		Node node = this.getPacketInfo(packet);
		Node byteCodeNode = (Node) node.getChildren().get(0);
		byte[] byteCode = (byte[]) byteCodeNode.getValue().getRealValue();
		methType.setByteCode(byteCode);
	}
	
    public Node getPacketInfo(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		List requestChildren = requestPacketAnalyzer.getPacketInfo(request).getChildren();

		Long referenceTypeId = (Long) ((Node) requestChildren.get(0)).getValue().getRealValue();
		Long methId = (Long) ((Node) requestChildren.get(1)).getValue().getRealValue();
		
		MethodType methType = MethodType.getType(referenceTypeId, methId);
		
		int index = 0;
		int numMethodBytes = (int) getVal(packet, index, 4);
		index += 4;
		byte[] methodBytes = new byte[numMethodBytes]; 
		System.arraycopy(packet.getData(), index, methodBytes, 0, numMethodBytes);
		index += numMethodBytes;
		
		Node byteInfo = new Node (new Node.Descriptor("Bytes:", methType), new Node.Value(methodBytes, Integer.toString(numMethodBytes)));

		return byteInfo;
    }

}
