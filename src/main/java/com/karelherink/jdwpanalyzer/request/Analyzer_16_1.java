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
package com.karelherink.jdwpanalyzer.request;

import com.karelherink.jdwpanalyzer.entity.ObjectType;
import com.karelherink.jdwpanalyzer.model.Node;
import com.karelherink.jdwpanalyzer.model.Packet;
import com.karelherink.jdwpanalyzer.model.PacketAnalyzer;
import com.karelherink.jdwpanalyzer.model.constants.Tag;

/**
 * @author karel herink
 */
public class Analyzer_16_1 extends PacketAnalyzer {

    public Analyzer_16_1() {
    }
    
    
    public Node getPacketInfo(Packet packet) {
		int index = 0;
		Node root = new Node(null, null);
				
		long threadId = getVal(packet, index, objectIDSize);
		index += objectIDSize;
		ObjectType objType = ObjectType.getType(threadId);
		Node threadInfo = new Node(new Node.Descriptor("ThreadID:", objType), new Node.Value(new Long(threadId)));
		root.addChild(threadInfo);
		
		long frameId = getVal(packet, index, frameIDSize);
		index += frameIDSize;
		Node frameInfo = new Node(new Node.Descriptor("FrameID:"), new Node.Value(new Long(frameId)));
		root.addChild(frameInfo);
		
		int numVars = (int) getVal(packet, index, 4);
		index += 4;
		Node variablesInfo = new Node(new Node.Descriptor("NumVariables:"), new Node.Value(new Integer(numVars)));
		root.addChild(variablesInfo);
		
		for (int i = 0; i < numVars; i++) {			
			int vfi = (int) getVal(packet, index, 4);
			index += 4;
			Node vfiInfo = new Node(new Node.Descriptor("VariableFrameIndex:"), new Node.Value(new Integer(vfi)));
			variablesInfo.addChild(vfiInfo);
			
			byte variableTag = packet.getData()[index++];
			Node variableTagInfo = new Node(new Node.Descriptor("VariableTag:"), new Node.Value(new Byte(variableTag), Tag.asString(variableTag)));
			variablesInfo.addChild(variableTagInfo);
		}
		
		return root;
	}

}
