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
import java.util.List;

/**
 * @author karel herink
 */
public class Analyzer_9_5 extends PacketAnalyzer {

    public Analyzer_9_5() {
    }
    
    
	public void updateInternalDataModel(Packet packet) {
		Request request = Request.getRequest(packet.getId());
		PacketAnalyzer requestPacketAnalyzer = AnalyzerManager.createPacketAnalyzer(request);
		Long objTypeId = (Long) requestPacketAnalyzer.getPacketInfo(request).getValue().getRealValue();
		ObjectType objectType = ObjectType.getType(objTypeId);
		
		List nodes = this.getPacketInfo(packet).getChildren();
		
		Long ownerThreadId = (Long) ((Node) nodes.get(0)).getValue().getRealValue();
		objectType.setOwnerThreadId(ownerThreadId);
		
		Integer entrycount = (Integer) ((Node) nodes.get(1)).getValue().getRealValue();
		objectType.setMonitorEntryCount(entrycount);
		
		List waitingThreads = ((Node) nodes.get(2)).getChildren();
		List waitingThreadIds = new ArrayList();
		for (Iterator iter = waitingThreads.iterator(); iter.hasNext();) {
			Node waitThreadNode = (Node) iter.next();
			Long id = (Long) waitThreadNode.getValue().getRealValue();
			waitingThreadIds.add(id);
		}
		objectType.setWaitingThreadIds((Long[]) waitingThreadIds.toArray(new Long[waitingThreadIds.size()]));
	}    
    
    public Node getPacketInfo(Packet packet) {
    	Node root = new Node(null, null);
		int index = 0;
		
		long threadTypeId = getVal(packet, index, objectIDSize);
		index += objectIDSize;
		ObjectType threadType = ObjectType.getType(threadTypeId);
		Node threadInfo = new Node(new Node.Descriptor("ThreadID:", threadType), new Node.Value(new Long(threadTypeId)));
		root.addChild(threadInfo);
		
		int entryCount = (int) getVal(packet, index, 4);
		index += 4;
		Node entryInfo = new Node(new Node.Descriptor("EntryCount:"), new Node.Value(new Integer(entryCount)));
		root.addChild(entryInfo);
		
		int numWaiters = (int) getVal(packet, index, 4);
		index += 4;
		Node waitersInfo = new Node(new Node.Descriptor("NumWaitingThreads:"), new Node.Value(new Integer(numWaiters)));
		root.addChild(waitersInfo);
		
		for (int i = 0; i < numWaiters; i++) {
			long waitingThreadTypeId = getVal(packet, index, objectIDSize);
			index += objectIDSize;
			ObjectType waitingThreadType = ObjectType.getType(waitingThreadTypeId);
			Node waitThreadInfo = new Node(new Node.Descriptor("WaitingThreadID:", waitingThreadType), new Node.Value(new Long(waitingThreadTypeId)));
			waitersInfo.addChild(waitThreadInfo);
		}
		
		return root;
    }    

}
