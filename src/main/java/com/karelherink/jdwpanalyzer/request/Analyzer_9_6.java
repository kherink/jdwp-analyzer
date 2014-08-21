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

import com.karelherink.jdwpanalyzer.entity.MethodType;
import com.karelherink.jdwpanalyzer.entity.ObjectType;
import com.karelherink.jdwpanalyzer.entity.ReferenceType;
import com.karelherink.jdwpanalyzer.model.Node;
import com.karelherink.jdwpanalyzer.model.Packet;
import com.karelherink.jdwpanalyzer.model.PacketAnalyzer;

/**
 * @author karel herink
 */
public class Analyzer_9_6 extends PacketAnalyzer {

    public Analyzer_9_6() {
    }
    
    
    public Node getPacketInfo(Packet packet) {
		Node root = new Node (null, null);
		int index = 0;
		
		long objTypeId = getVal(packet, index, objectIDSize);
		index += objectIDSize;
		ObjectType objectType = ObjectType.getType(objTypeId);
		Node objInfo = new Node(new Node.Descriptor("RefTypeID:", objectType), new Node.Value(new Long(objTypeId)));
		root.addChild(objInfo);

		long threadId = getVal(packet, index, objectIDSize);
		index += objectIDSize;
		ObjectType threadType = ObjectType.getType(threadId);
		Node threadInfo = new Node(new Node.Descriptor("ThreadID:", threadType), new Node.Value(new Long(threadId)));
		root.addChild(threadInfo);
		
		long refTypeId = getVal(packet, index, referenceTypeIDSize);
		index += referenceTypeIDSize;
		ReferenceType referenceType = ReferenceType.getType(refTypeId);
		Node refInfo = new Node(new Node.Descriptor("ClassID:", referenceType), new Node.Value(new Long(refTypeId)));
		root.addChild(refInfo);
		
		long methodId = getVal(packet, index, methodIDSize);
		index += methodIDSize;
		MethodType methodType = MethodType.getType(refTypeId, methodId);
		Node methodInfo = new Node(new Node.Descriptor("MethodID:", methodType), new Node.Value(new Long(methodId)));
		root.addChild(methodInfo);
		
		int numargs = (int) getVal(packet, index, 4);
		index += 4;
		
		Node argsInfo = new Node (new Node.Descriptor("NumArgs:"), new Node.Value(new Integer(numargs)));
		root.addChild(argsInfo);
		
		for (int i = 0; i < numargs; i++) {
			Value value = getValue(packet, index);
			index += value.getOffset();
			Node singleArgInfo = new Node(new Node.Descriptor("ArgValue:"), new Node.Value(value));
			argsInfo.addChild(singleArgInfo);
		}
		
		int options = (int) getVal(packet, index, 4);
		index += 4;
		Node optionsInfo = new Node(new Node.Descriptor("Options:"), new Node.Value(new Integer(options), getInvokeOptionsAsStr(options)));
		root.addChild(optionsInfo);
		
		return root;
    }    
}
