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
import com.karelherink.jdwpanalyzer.entity.ReferenceType;
import com.karelherink.jdwpanalyzer.model.Node;
import com.karelherink.jdwpanalyzer.model.Packet;
import com.karelherink.jdwpanalyzer.model.PacketAnalyzer;

/**
 * @author karel herink
 */
public class Analyzer_2_6 extends PacketAnalyzer {

    public Analyzer_2_6() {
    }
    
    public Node getPacketInfo(Packet packet) {
		Node root = new Node (null, null);
		int index = 0;
		
		long refTypeId = getVal(packet, index, referenceTypeIDSize);
		index += referenceTypeIDSize;
		ReferenceType referenceType = ReferenceType.getType(refTypeId);
		Node refInfo = new Node(new Node.Descriptor("RefTypeID:", referenceType), new Node.Value(new Long(refTypeId)));
		root.addChild(refInfo);

		
		int numFields = (int) getVal(packet, index, 4);
		index += 4;
		Node fieldsInfo = new Node (new Node.Descriptor("NumFields:"), new Node.Value(new Integer(numFields)));
		root.addChild(fieldsInfo);
		
		for (int i = 0; i < numFields; i++) {
			long fieldID = (getVal(packet, index, fieldIDSize));
			index += fieldIDSize;
			FieldType fieldType = FieldType.getType(refTypeId, fieldID);
			Node singleFieldInfo = new Node(new Node.Descriptor("FieldID:", fieldType), new Node.Value(new Long(fieldID)));
			fieldsInfo.addChild(singleFieldInfo);
		}
		return root;
	}

}
