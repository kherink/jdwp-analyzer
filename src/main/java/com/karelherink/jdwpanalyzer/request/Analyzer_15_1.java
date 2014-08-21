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

import com.karelherink.jdwpanalyzer.entity.FieldType;
import com.karelherink.jdwpanalyzer.entity.ObjectType;
import com.karelherink.jdwpanalyzer.entity.ReferenceType;
import com.karelherink.jdwpanalyzer.model.Node;
import com.karelherink.jdwpanalyzer.model.Packet;
import com.karelherink.jdwpanalyzer.model.PacketAnalyzer;
import com.karelherink.jdwpanalyzer.model.constants.EventKind;
import com.karelherink.jdwpanalyzer.model.constants.Step;
import com.karelherink.jdwpanalyzer.model.constants.SuspendPolicy;

/**
 * @author karel herink
 */
public class Analyzer_15_1 extends PacketAnalyzer {

    /** Creates a new instance of Analyzer_15_1 */
    public Analyzer_15_1() {
    }
    
    public Node getPacketInfo(Packet packet) {
		int index = 0;

		byte eventKind = packet.getData()[index++];
		Node root = new Node(new Node.Descriptor("EventKind:"), new Node.Value(new Byte(eventKind), EventKind.asString(eventKind)));
		
		byte suspend = packet.getData()[index++];
		Node suspendInfo = new Node(new Node.Descriptor("SuspendPolicy:"), new Node.Value(new Byte(suspend), SuspendPolicy.asString(suspend)));
		root.addChild(suspendInfo);
		
		int numModifiers = (int) getVal(packet, index, 4);
		index += 4;	
		Node modifiersInfo = new Node(new Node.Descriptor("NumModifiers:"), new Node.Value(new Integer(numModifiers)));
		root.addChild(modifiersInfo);
		
        for(int i = 0; i < numModifiers; i++) {
            int mod = packet.getData()[index++];
            switch (mod) {
				case 1:
					int count = (int) getVal(packet, index, 4);
					index += 4;
					Node countInfo = new Node(new Node.Descriptor("Count:"), new Node.Value(new Integer(count)));
					root.addChild(countInfo);
					break;
				case 2:
					int exprId = (int) getVal(packet, index, 4);
					index += 4;
					Node exprIdInfo = new Node(new Node.Descriptor("Conditional-ExprID:"), new Node.Value(new Integer(exprId)));
					root.addChild(exprIdInfo);
					break;
				case 3:
					long objTypeId = getVal(packet, index, objectIDSize);
					index += objectIDSize;
					ObjectType objType = ObjectType.getType(objTypeId);
					Node threadIdInfo = new Node(new Node.Descriptor("ThreadOnly-ThreadID:", objType), new Node.Value(new Long(objTypeId)));
					root.addChild(threadIdInfo);
					break;
				case 4:
					long refId = getVal(packet, index, referenceTypeIDSize);
					index += referenceTypeIDSize;
					ReferenceType refType = ReferenceType.getType(refId);
					Node refIdInfo = new Node(new Node.Descriptor("ClassOnly-RefTypeID:", refType), new Node.Value(new Long(refId)));
					root.addChild(refIdInfo);
					break;
				case 5:
			        int len = (int) getVal(packet, index, 4);
			        index += 4;
					String classMatch = getStr(packet, index, len);
			        index += len;
					Node matchInfo = new Node (new Node.Descriptor("ClassInclude-Pattern:"), new Node.Value(classMatch));
					root.addChild(matchInfo);
					break;
				case 6:
			        len = (int) getVal(packet, index, 4);
			        index += 4;
					String classExclude = getStr(packet, index, len);
			        index += len;
					Node excludeInfo = new Node (new Node.Descriptor("ClassExclude-Pattern:"), new Node.Value(classExclude));
					root.addChild(excludeInfo);
					break;
				case 7:
					Location location = getLocation(packet, index);
					index += locationSize;
					Node locationInfo = location.toNode();
					root.addChild(locationInfo);
					break;
				case 8:
					refId = getVal(packet, index, referenceTypeIDSize);
					index += referenceTypeIDSize;
					boolean reportCaught = getBoolean(packet, index++);
					boolean reportUncaught = getBoolean(packet, index++);
					String desc =(reportCaught ? "Caught=YES" : "Caught=NO") + "/" 
						+ (reportUncaught ? "Uncaught=YES" : "Uncaught=NO");
					refType = ReferenceType.getType(refId);
					Node exceptionInfo = new Node(new Node.Descriptor("Exception:", refType), new Node.Value(desc));
					root.addChild(exceptionInfo);
					break;
				case 9:
					refId = getVal(packet, index, referenceTypeIDSize);
					index += referenceTypeIDSize;
					long fieldId = getVal(packet, index, fieldIDSize);
					index += fieldIDSize;
					FieldType fieldType = FieldType.getType(refId, fieldId);
					Node fieldOnlyInfo = new Node(new Node.Descriptor("FieldOnlyID:", fieldType), new Node.Value(new Long(fieldId)));
					root.addChild(fieldOnlyInfo);
					break;
				case 10:
					long threadId = getVal(packet, index, objectIDSize);
					index += objectIDSize;
					int stepSize = (int) getVal(packet, index, 4);
					index += 4;
					int stepDepth = (int) getVal(packet, index, 4);
					index += 4;
					desc = "ThreadID:" + threadId + "," + Step.sizeAsString(stepSize)
						+ "," + Step.depthAsString(stepDepth);
					ObjectType threadType = ObjectType.getType(threadId);
					Node stepInfo = new Node(new Node.Descriptor("Step:", threadType), new Node.Value(desc));
					root.addChild(stepInfo);
					break;
				case 11:
					long objId = getVal(packet, index, objectIDSize);
					index += objectIDSize;
					ObjectType instance = ObjectType.getType(objId);
					Node instanceInfo = new Node(new Node.Descriptor("InstanceOnly-ObjID:", instance), new Node.Value(new Long(objId)));
					root.addChild(instanceInfo);
					break;

				//this should NEVER happen
				default:
					Node errInfo = new Node(new Node.Descriptor("ERR: Unknown modifier " + mod), null);
					root.addChild(errInfo);
					break;
			}
        }		

		return root;
    }

}

