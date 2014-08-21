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

import com.karelherink.jdwpanalyzer.entity.ReferenceType;
import com.karelherink.jdwpanalyzer.model.Node;
import com.karelherink.jdwpanalyzer.model.Packet;
import com.karelherink.jdwpanalyzer.model.PacketAnalyzer;

import java.util.Iterator;
import java.util.List;

/**
 * @author karel herink
 */
public class Analyzer_1_18 extends PacketAnalyzer {

    public Analyzer_1_18() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		List classes = this.getPacketInfo(packet).getChildren();
		for (Iterator iter = classes.iterator(); iter.hasNext();) {
			Node refTypeInfo = (Node) iter.next();
			ReferenceType refType = (ReferenceType) refTypeInfo.getDescriptor().getAssociatedType();
			
			byte[] classBytes = (byte[]) ((Node)refTypeInfo.getChildren().get(0)).getValue().getRealValue();
			refType.setClassBytes(classBytes);
		}
	}
    
	public Node getPacketInfo(Packet packet) {
		int index = 0;
		
        int numClasses = (int) getVal(packet, index, 4);
        index += 4;
		Node classesInfo = new Node (new Node.Descriptor("NumClasses:"), new Node.Value(new Integer(numClasses)));
		
		for(int i = 0; i < numClasses; i++) {
			long referenceTypeID = getVal(packet, index, referenceTypeIDSize);
			index += referenceTypeIDSize;
			ReferenceType refType = ReferenceType.getType(referenceTypeID);
			Node refTypeInfo = new Node (new Node.Descriptor("RefTypeID:", refType), new Node.Value(new Long(referenceTypeID)));
			classesInfo.addChild(refTypeInfo);
			
			int numBytes = (int) getVal(packet, index, 4);
			index += 4;
			byte[] classBytes = new byte[numBytes]; 
			System.arraycopy(packet.getData(), index, classBytes, 0, numBytes);
			index += numBytes;
			Node byteInfo = new Node (new Node.Descriptor("ClassBytes:"), new Node.Value(classBytes, Integer.toString(numBytes)));
			refTypeInfo.addChild(byteInfo);
		}
		
		return classesInfo;
	}
		
}
