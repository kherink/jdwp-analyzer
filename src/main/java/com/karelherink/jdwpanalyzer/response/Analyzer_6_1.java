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
public class Analyzer_6_1 extends PacketAnalyzer {

    public Analyzer_6_1() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		List requestChildren = requestPacketAnalyzer.getPacketInfo(request).getChildren();

		Long referenceTypeId = (Long) ((Node) requestChildren.get(0)).getValue().getRealValue();
		Long methId = (Long) ((Node) requestChildren.get(1)).getValue().getRealValue();
		
		MethodType methType = MethodType.getType(referenceTypeId, methId);
		
		List nodes = this.getPacketInfo(packet).getChildren();

		Long startIndex = (Long) ((Node) nodes.get(0)).getValue().getRealValue();
		methType.setStartCodeIndex(startIndex);
		Long endIndex = (Long) ((Node) nodes.get(1)).getValue().getRealValue();
		methType.setEndCodeIndex(endIndex);
		
		Node lineInfo = (Node) nodes.get(2);
		Integer numLines = (Integer) lineInfo.getValue().getRealValue();
		methType.setNumLines(numLines);
		
		List lines = lineInfo.getChildren();
		List lineMappings = new ArrayList();
		for (Iterator iter = lines.iterator(); iter.hasNext();) {
			Node node = (Node) iter.next();
			Long lineCodeIndex = (Long) node.getValue().getRealValue();
			
			node = (Node) iter.next();
			Integer lineNumber = (Integer) node.getValue().getRealValue();
			MethodType.LineMapping mapping = new MethodType.LineMapping(lineCodeIndex, lineNumber);
			lineMappings.add(mapping);
		}
		methType.setLineMapping((MethodType.LineMapping[]) lineMappings.toArray(new MethodType.LineMapping[lineMappings.size()]));
	}
    
    public Node getPacketInfo(Packet packet) {
		Node root = new Node(null, null);
		int index = 0;
		
		long startIndex = getVal(packet, index, 8);
		index += 8;
		Node startIndexInfo = new Node (new Node.Descriptor("StartIndex:"), new Node.Value(new Long(startIndex)));
		root.addChild(startIndexInfo);
		
		long endIndex = getVal(packet, index, 8);
		index += 8;
		Node endIndexInfo = new Node (new Node.Descriptor("EndIndex:"), new Node.Value(new Long(endIndex)));
		root.addChild(endIndexInfo);
		
		int numLines = (int) getVal(packet, index, 4);
		index += 4;
		Node lineInfo = new Node (new Node.Descriptor("NumLines:"), new Node.Value(new Integer(numLines)));
		root.addChild(lineInfo);
		
		for (int i = 0; i < numLines; i++) {
			
			long codeIndex = (getVal(packet, index, 8));
			index += 8;
			Node codeIndexInfo = new Node(new Node.Descriptor("LineCodeIndex:"), new Node.Value(new Long(codeIndex)));
			lineInfo.addChild(codeIndexInfo);
			            
            int lineNum = (int) getVal(packet, index, 4);
            index += 4;
			Node lineNumInfo = new Node(new Node.Descriptor("LineNumber:"), new Node.Value(new Integer(lineNum)));
			lineInfo.addChild(lineNumInfo);
		}
		return root;
	}

}
