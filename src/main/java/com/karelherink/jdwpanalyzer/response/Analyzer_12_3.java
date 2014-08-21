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

import com.karelherink.jdwpanalyzer.entity.ObjectType;
import com.karelherink.jdwpanalyzer.model.*;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author karel herink
 */
public class Analyzer_12_3 extends PacketAnalyzer {

    public Analyzer_12_3() {
    }
    
	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		Long objTypeId = (Long) requestPacketAnalyzer.getPacketInfo(request).getValue().getRealValue();
		ObjectType objectType = ObjectType.getType(objTypeId);
		
		ArrayList childThreads = new ArrayList();
		ArrayList childThreadGroups = new ArrayList();
		
		ArrayList nodes = this.getPacketInfo(packet).getChildren();
		
		ArrayList threadNodes = ((Node)nodes.get(0)).getChildren();
		for (Iterator iter = threadNodes.iterator(); iter.hasNext();) {
			Node threadNode = (Node) iter.next();
			Long threadId = (Long) threadNode.getValue().getRealValue();
			childThreads.add(threadId);
		}
		objectType.setChildThreadIds((Long[]) childThreads.toArray(new Long[childThreads.size()]));
		
		ArrayList threadGroupNodes = ((Node)nodes.get(1)).getChildren();
		for (Iterator iter = threadGroupNodes.iterator(); iter.hasNext();) {
			Node threadGroupNode = (Node) iter.next();
			Long threadGroupId = (Long) threadGroupNode.getValue().getRealValue();
			childThreads.add(threadGroupId);
		}
		objectType.setChildThreadGroupIds((Long[]) childThreadGroups.toArray(new Long[childThreadGroups.size()]));
	}

	public Node getPacketInfo(Packet packet) {
		int index = 0;
		Node root = new Node(null, null);
		
        int numThreads = (int) getVal(packet, index, 4);
        index += 4;
		Node threadsInfo = new Node (new Node.Descriptor("NumThreads:"), new Node.Value(new Integer(numThreads)));
		
		for(int i = 0; i < numThreads; i++) {
			long threadId = getVal(packet, index, objectIDSize);
			index += objectIDSize;
			ObjectType objectType = ObjectType.getType(threadId);
			Node threadInfo = new Node (new Node.Descriptor("ThreadId:", objectType), new Node.Value(new Long(threadId)));
			threadsInfo.addChild(threadInfo);
		}
		root.addChild(threadsInfo);
		
        int numThreadGroups = (int) getVal(packet, index, 4);
        index += 4;
		Node threadGroupsInfo = new Node (new Node.Descriptor("NumThreadGroups:"), new Node.Value(new Integer(numThreadGroups)));
		
		for(int i = 0; i < numThreadGroups; i++) {
			long threadGroupId = getVal(packet, index, objectIDSize);
			index += objectIDSize;
			ObjectType objectType = ObjectType.getType(threadGroupId);
			Node threadGroupInfo = new Node (new Node.Descriptor("ThreadGroupId:", objectType), new Node.Value(new Long(threadGroupId)));
			threadGroupsInfo.addChild(threadGroupInfo);
		}
		root.addChild(threadGroupsInfo);	
		return root;
    }
    
}
