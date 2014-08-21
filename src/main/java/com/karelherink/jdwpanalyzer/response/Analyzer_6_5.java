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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author karel herink
 */
public class Analyzer_6_5 extends PacketAnalyzer {

    public Analyzer_6_5() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		List requestChildren = requestPacketAnalyzer.getPacketInfo(request).getChildren();

		Long referenceTypeId = (Long) ((Node) requestChildren.get(0)).getValue().getRealValue();
		Long methId = (Long) ((Node) requestChildren.get(1)).getValue().getRealValue();
		
		MethodType methType = MethodType.getType(referenceTypeId, methId);
		
		List nodes = this.getPacketInfo(packet).getChildren();

		Integer argCnt = (Integer) ((Node) nodes.get(0)).getValue().getRealValue();
		methType.setArgCount(argCnt);
		
		Node varsInfo = (Node) nodes.get(1);
		
		List varData = varsInfo.getChildren();
		List variables = new ArrayList();
		for (Iterator iter = varData.iterator(); iter.hasNext();) {
			Node node = (Node) iter.next();
			Long codeIndex = (Long) node.getValue().getRealValue();
			
			node = (Node) iter.next();
			String name = (String) node.getValue().getRealValue();
			node = (Node) iter.next();
			String signature = (String) node.getValue().getRealValue();
			node = (Node) iter.next();
			String genericSignature = (String) node.getValue().getRealValue();

			node = (Node) iter.next();
			Integer lenght = (Integer) node.getValue().getRealValue();
			node = (Node) iter.next();
			Integer frameIndex = (Integer) node.getValue().getRealValue();
			
			MethodType.Variable variable = new MethodType.Variable(codeIndex, name, signature, genericSignature, lenght, frameIndex);
			variables.add(variable);
		}
		methType.setVariables((MethodType.Variable[]) variables.toArray(new MethodType.Variable[variables.size()]));
	}
    
    public Node getPacketInfo(Packet packet) {
		Node root = new Node(null, null);
		int index = 0;
		
		
		int argCnt = (int) getVal(packet, index, 4);
		index += 4;
		Node argCntInfo = new Node (new Node.Descriptor("ArgCount:"), new Node.Value(new Integer(argCnt)));
		root.addChild(argCntInfo);
		
		int numVars = (int) getVal(packet, index, 4);
		index += 4;
		Node varsInfo = new Node (new Node.Descriptor("NumVariables:"), new Node.Value(new Integer(numVars)));
		root.addChild(varsInfo);
		
		for (int i = 0; i < numVars; i++) {
			
			long codeIndex = (getVal(packet, index, 8));
			index += 8;
			Node codeIndexInfo = new Node(new Node.Descriptor("CodeIndex:"), new Node.Value(new Long(codeIndex)));
			varsInfo.addChild(codeIndexInfo);
			
            int len = (int) getVal(packet, index, 4);
            index += 4;
			Node nameInfo = new Node(new Node.Descriptor("Name:"), new Node.Value(getStr(packet, index, len)));
            index += len;
            varsInfo.addChild(nameInfo);
            
            len = (int) getVal(packet, index, 4);
            index += 4;
			Node signatureInfo = new Node(new Node.Descriptor("Signature:"), new Node.Value(getStr(packet, index, len)));
            index += len;
            varsInfo.addChild(signatureInfo);
			            
            len = (int) getVal(packet, index, 4);
            index += 4;
			Node genericSignatureInfo = new Node(new Node.Descriptor("GenericSignature:"), new Node.Value(getStr(packet, index, len)));
            index += len;
            varsInfo.addChild(genericSignatureInfo);
			            
            int lenght = (int) getVal(packet, index, 4);
            index += 4;
			Node lenghtInfo = new Node(new Node.Descriptor("Lenght:"), new Node.Value(new Integer(lenght)));
			varsInfo.addChild(lenghtInfo);
			
            int frameIndex = (int) getVal(packet, index, 4);
            index += 4;
			Node frameIndexInfo = new Node(new Node.Descriptor("FrameIndex:"), new Node.Value(new Integer(frameIndex)));
			varsInfo.addChild(frameIndexInfo);
		}
		return root;
	}
}
