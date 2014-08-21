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

import java.util.Iterator;
import java.util.List;

/**
 * @author karel herink
 */
public class Analyzer_1_14 extends PacketAnalyzer {

    public Analyzer_1_14() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		List children = this.getPacketInfo(packet).getChildren();
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			Node child = (Node) iter.next();
			Long objectId = (Long) child.getValue().getRealValue();
			ObjectType object = ObjectType.getType(objectId);
			object.setDisposeRequested(new Boolean(true));
			iter.next(); //skip refcount
		}
	}
    
	public Node getPacketInfo(Packet packet) {
		int index = 0;
        int numObjects = (int) getVal(packet, index, 4);
        index += 4;
		Node objectsInfo = new Node (new Node.Descriptor("NumObjects:"), new Node.Value(new Integer(numObjects)));
		
		for(int i = 0; i < numObjects; i++) {
			long objectId = getVal(packet, index, objectIDSize);
			index += objectIDSize;
			ObjectType objectType = ObjectType.getType(objectId);
			Node objectInfo = new Node (new Node.Descriptor("ObjectId:", objectType), new Node.Value(new Long(objectId)));
			objectsInfo.addChild(objectInfo);

			int refCount = (int) getVal(packet, index, 4);
			index += 4;
			Node refCountInfo = new Node (new Node.Descriptor("ReferenceCount:"), new Node.Value(new Long(refCount)));
			objectsInfo.addChild(refCountInfo);
		}
		
		return objectsInfo;
	}

}
