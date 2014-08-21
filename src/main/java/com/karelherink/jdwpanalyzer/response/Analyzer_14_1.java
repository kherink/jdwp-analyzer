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
import com.karelherink.jdwpanalyzer.model.constants.TypeTag;

/**
 * @author karel herink
 */
public class Analyzer_14_1 extends PacketAnalyzer {

    public Analyzer_14_1() {
    }
    
    public Node getPacketInfo(Packet packet) {
		int index = 0;
		
        int numClasses = (int) getVal(packet, index, 4);
        index += 4;
		Node root = new Node (new Node.Descriptor("NumClasses:"), new Node.Value(new Integer(numClasses)));
		
		for(int i = 0; i < numClasses; i++) {
			
			byte typeTag = packet.getData()[index++];
			Node typeTagInfo = new Node (new Node.Descriptor("TypeTag:"), new Node.Value(new Integer(typeTag), TypeTag.asString(typeTag)));
			root.addChild(typeTagInfo);
			
			long refTypeId = getVal(packet, index, referenceTypeIDSize);
            index += referenceTypeIDSize;
            ReferenceType refType = ReferenceType.getType(refTypeId);
            Node refTypeInfo = new Node (new Node.Descriptor("RefTypeID:", refType), new Node.Value(new Long(refTypeId)));
            root.addChild(refTypeInfo);
		}
		return root;
    }

}
