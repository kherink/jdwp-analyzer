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
import com.karelherink.jdwpanalyzer.entity.ReferenceType;
import com.karelherink.jdwpanalyzer.model.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author karel herink
 */
public class Analyzer_2_15 extends PacketAnalyzer {

    public Analyzer_2_15() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		Long referenceTypeId = (Long) requestPacketAnalyzer.getPacketInfo(request).getValue().getRealValue();
		ReferenceType referenceType = ReferenceType.getType(referenceTypeId);
		
		List methods = this.getPacketInfo(packet).getChildren();
		List methodIDs = new ArrayList();
		for (Iterator iter = methods.iterator(); iter.hasNext();) {
			Node child = (Node) iter.next();
			Long methodId = (Long) child.getValue().getRealValue();
			methodIDs.add(methodId);
			
			MethodType methodType = MethodType.getType(referenceTypeId, methodId);
			ArrayList grandChildren = child.getChildren();
			methodType.setName((String) ((Node) grandChildren.get(0)).getValue().getRealValue());
			methodType.setSignature((String) ((Node) grandChildren.get(1)).getValue().getRealValue());
			methodType.setSignatureWithGeneric((String) ((Node) grandChildren.get(2)).getValue().getRealValue());
			methodType.setModBits((Integer) ((Node) grandChildren.get(3)).getValue().getRealValue());
		}
		
		referenceType.setMethodIds((Long[]) methodIDs.toArray(new Long[methodIDs.size()]));
	}
    
    public Node getPacketInfo(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		Long referenceTypeId = (Long) requestPacketAnalyzer.getPacketInfo(request).getValue().getRealValue();

		int index = 0;		
		int numMethods = (int) getVal(packet, index, 4);
		index += 4;
		Node root = new Node (new Node.Descriptor("NumMethods:"), new Node.Value(new Integer(numMethods)));
		
		for (int i = 0; i < numMethods; i++) {
			
			long methodID = (getVal(packet, index, methodIDSize));
			index += methodIDSize;
			MethodType methodType = MethodType.getType(referenceTypeId, new Long(methodID));
			Node singleMethodInfo = new Node(new Node.Descriptor("MethodID:", methodType), new Node.Value(new Long(methodID)));
			root.addChild(singleMethodInfo);
			
            int len = (int) getVal(packet, index, 4);
            index += 4;
			Node singleMethodNameInfo = new Node(new Node.Descriptor("Name:"), new Node.Value(getStr(packet, index, len)));
            index += len;
            singleMethodInfo.addChild(singleMethodNameInfo);
            
            len = (int) getVal(packet, index, 4);
            index += 4;
			Node singleMethodSignatureInfo = new Node(new Node.Descriptor("Signature:"), new Node.Value(getStr(packet, index, len)));
            index += len;
            singleMethodInfo.addChild(singleMethodSignatureInfo);
            
            len = (int) getVal(packet, index, 4);
            index += 4;
			Node singleMethodGenericSignatureInfo = new Node(new Node.Descriptor("GenericSignature:"), new Node.Value(getStr(packet, index, len)));
            index += len;
            singleMethodInfo.addChild(singleMethodGenericSignatureInfo);
            
            int modBits = (int) getVal(packet, index, 4);
            index += 4;			
			Node singleMethodModBitsInfo = new Node(new Node.Descriptor("ModBits:"), new Node.Value(new Integer(modBits), getModBitsAsStr(modBits)));
			singleMethodInfo.addChild(singleMethodModBitsInfo);
		}
		return root;
	}

}
